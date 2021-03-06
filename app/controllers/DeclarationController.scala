/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import config._
import domain.Cancel
import domain.DeclarationFormats._
import domain.auth.{AuthenticatedRequest, SignedInUser}
import domain.features.Feature
import forms.DeclarationFormMapping._
import forms.ObligationGuaranteeForm
import javax.inject.{Inject, Singleton}
import models.{Cancellation, Declaration}
import org.joda.time.DateTime
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.{CustomsCacheService, CustomsDeclarationsConnector}
import uk.gov.hmrc.http.{HeaderCarrier, Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItem, MetaData}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationController @Inject()(actions: Actions, client: CustomsDeclarationsConnector, cache: CustomsCacheService)(implicit val messagesApi: MessagesApi, val appConfig: AppConfig,
  ec: ExecutionContext) extends FrontendController with I18nSupport {

  val GOV_AGENCY_GOODS_ITEMS_LIST_CACHE_KEY = "GovAgencyGoodsItemsList"

  private val navigationKeys = Set("next-page", "last-page", "force-last")

  private val permissibleKeys: Set[String] = Fields.definitions.keySet ++ navigationKeys

  private val knownBad: Set[String] = Set(
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[0].additionalProcedure",
    "declaration.typeCode.additional"
  )

  val cancelForm: Form[Cancel] = Form(cancelMapping)

  def displaySubmitForm(name: String): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async { implicit req =>
    cache.get(appConfig.submissionCacheId, req.user.eori.get).map { data =>
      if(req.request.uri.endsWith("guarantee-type")) Redirect(routes.ObligationGuaranteeController.display())
      else Ok(views.html.generic_view(name, data.getOrElse(Map.empty)))
    }
  }

  def displaySubmitConfirmation(conversationId: String): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth) {
    implicit req => {
      Ok(views.html.submit_confirmation(new Declaration(DateTime.now, Some("Local Reference Number"), Some("MRN"))))
    }
  }

  def handleSubmitForm(name: String): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async { implicit req =>
    val data: Map[String, String] = formDataAsMap()
    implicit val errors: Map[String, Seq[ValidationError]] = validate(data)
    Logger.info("Errors: " + errors.mkString("\n"))
    if (errors.isEmpty) {
      cacheSubmission(data) { (_, _) =>
        Future.successful(Redirect(routes.DeclarationController.displaySubmitForm(data("next-page"))))
      }
    } else { invalid(name, data) }
  }

  def onSubmitComplete: Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async { implicit req =>
    val data: Map[String, String] = formDataAsMap()
    implicit val user: SignedInUser = req.user

    implicit val errors: Map[String, Seq[ValidationError]] = validate(data)
    if (errors.isEmpty) {
      cacheSubmission(data ++ Map("force-last" -> "false")) { (merged, _) =>
        val props = merged.filterNot(entry => navigationKeys.contains(entry._1) || knownBad.contains(entry._1) || entry._2.trim.isEmpty)
        updateMetaData(MetaData.fromProperties(props)).flatMap({ maybeMetaData =>
          maybeMetaData match {
              case Some(metadata) => {
                metadata.declaration.flatMap(declaration => declaration.functionalReferenceId)
                  .fold(Future.successful(InternalServerError("Lrn Is Required"))) {
                    localReferenceNumber =>
                    client.submitImportDeclaration(metadata, localReferenceNumber).map { resp =>
                      Redirect(routes.DeclarationController.displaySubmitConfirmation(resp.conversationId))
                    }
                }
              }
              case None => {
               Future.successful(InternalServerError("MetaData is required"))
             }
          }

        })

      }
    } else { invalid(data("last-page"), data) }
  }

  def displayCancelForm(mrn: String): Action[AnyContent] = (actions.auth andThen actions.eori) { implicit req =>
    Ok(views.html.cancel_form(mrn, cancelForm))
  }

  def handleCancelForm(mrn: String): Action[AnyContent] = (actions.auth andThen actions.eori).async { implicit req =>
    cancelForm.bindFromRequest().fold(
      errors  => Future.successful(BadRequest(views.html.cancel_form(mrn, errors))),
      success =>
        client
          .cancelDeclaration(Cancellation(mrn, success.changeReasonCode, success.description))
          .map(_ => Ok(views.html.cancel_confirmation()))
          .recover {
            case Upstream4xxResponse(_, _, _, _) | Upstream5xxResponse(_, _, _) =>
              Redirect(routes.DeclarationController.displayCancelFailure(mrn))
          }
      )
  }

  def displayCancelFailure(mrn: String): Action[AnyContent] = (actions.auth andThen actions.eori) { implicit req =>
    Ok(views.html.cancel_failure(mrn))
  }

  private def invalid(name: String, data: Map[String, String])(implicit req: AuthenticatedRequest[AnyContent], errors: Map[String, Seq[ValidationError]]): Future[Result] = Future.successful(BadRequest(views.html.generic_view(name, data)))

  private def cacheSubmission(data: Map[String, String])(f: (Map[String, String], CacheMap) => Future[Result])(implicit req: AuthenticatedRequest[AnyContent]): Future[Result] = cache.get(appConfig.submissionCacheId, req.user.requiredEori).flatMap { existing =>
    val merged = existing.getOrElse(Map.empty) ++ data
    cache.put(appConfig.submissionCacheId, req.user.requiredEori, merged).flatMap { cached =>
      f(merged, cached)
    }
  }

  private def validate(data: Map[String, String]): Map[String, Seq[ValidationError]] = data.filter(entry => Fields.definitions.keySet.contains(entry._1)).map { field =>
    val maybeField = Fields.definitions.get(field._1)
    val validators = maybeField.map(_.validators).getOrElse(Seq.empty)
    val results = validators.map(_.validate(field._2))
    val failures = results.filterNot(_.valid)
    val fieldErrors: Seq[ValidationError] = failures.
      map(err => ValidationError(errorMessageKey(Fields.definitions(field._1), err), Fields.definitions(field._1)))
    field._1 -> fieldErrors
  }.filterNot(_._2.isEmpty)

  private def errorMessageKey(field: FieldDefinition, result: ValidationResult): String = messagesApi(Seq(s"${field.labelKey}.${result.defaultErrorKey}", result.defaultErrorKey), result.args: _*)

  private def formDataAsMap()(implicit req: Request[AnyContent]): Map[String, String] = req.body.asFormUrlEncoded.map { form =>
    form.
      map(field => field._1 -> field._2.headOption).
      filter(entry => permissibleKeys.contains(entry._1) && entry._2.isDefined).
      map(field => field._1 -> field._2.get)
  }.getOrElse(Map.empty)

  private def updateMetaData(metaData: MetaData)(implicit req: AuthenticatedRequest[AnyContent], hc: HeaderCarrier) = {
    cache.fetchAndGetEntry[ObligationGuaranteeForm](req.user.eori.get,"ObligationGuarantees").flatMap( obligationGuaranteeForm =>
    cache.fetchAndGetEntry[List[GovernmentAgencyGoodsItem]](req.user.eori.get, GOV_AGENCY_GOODS_ITEMS_LIST_CACHE_KEY).map(_.map { items =>
      val decGoodsItems: Seq[GovernmentAgencyGoodsItem] =
        items.map(rec => GovernmentAgencyGoodsItem(customsValueAmount = rec.customsValueAmount,
        sequenceNumeric = rec.sequenceNumeric,
        statisticalValueAmount = rec.statisticalValueAmount,
        transactionNatureCode = rec.transactionNatureCode,
        additionalDocuments = rec.additionalDocuments,
        additionalInformations= rec.additionalInformations,
        aeoMutualRecognitionParties = rec.aeoMutualRecognitionParties,
        buyer  = rec.buyer,
          commodity = rec.commodity,
        consignee = rec.consignee,
          consignor = rec.consignor,
          customsValuation = rec.customsValuation,
          destination = rec.destination,
          domesticDutyTaxParties = rec.domesticDutyTaxParties,
          exportCountry = rec.exportCountry,
          governmentProcedures = rec.governmentProcedures,
          manufacturers = rec.manufacturers,
          origins = rec.origins,
          packagings = rec.packagings,
          previousDocuments = rec.previousDocuments,
          refundRecipientParties = rec.refundRecipientParties,
          seller = rec.seller,
          ucr = rec.ucr,
          valuationAdjustment = rec.valuationAdjustment))

      val goodsShipmentNew = metaData
        .declaration
        .flatMap(_.goodsShipment.map(rec => rec.copy(governmentAgencyGoodsItems = decGoodsItems)))

      metaData.copy(declaration = metaData.declaration.map(_.copy(goodsShipment = goodsShipmentNew, obligationGuarantees = obligationGuaranteeForm.get.guarantees)))
    }))
  }

}
