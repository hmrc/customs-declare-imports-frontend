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
import domain.auth.{AuthenticatedRequest, SignedInUser}
import domain.features.Feature
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request, Result}
import repositories.declaration.SubmissionRepository
import services.{CustomsCacheService, CustomsDeclarationsConnector}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItem, MetaData}
import domain.ObligationGuarantee._
import forms.ObligationGuaranteeForm

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationController @Inject()(actions: Actions, client: CustomsDeclarationsConnector, cache: CustomsCacheService,
  submissionRepository: SubmissionRepository)(implicit val messagesApi: MessagesApi, val appConfig: AppConfig,
  ec: ExecutionContext) extends FrontendController with I18nSupport {

  val GOV_AGENCY_GOODS_ITEMS_LIST_CACHE_KEY = "GovAgencyGoodsItems"

  private val navigationKeys = Set("next-page", "last-page", "force-last")

  private val permissibleKeys: Set[String] = Fields.definitions.keySet ++ navigationKeys

  private val knownBad: Set[String] = Set(
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[0].additionalProcedure",
    "declaration.typeCode.additional"
  )

  def displaySubmitForm(name: String): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async { implicit req =>
    cache.get(appConfig.submissionCacheId, req.user.eori.get).map { data =>
      if(req.request.uri.endsWith("guarantee-type")) Redirect(routes.ObligationGuaranteeController.display())
      else Ok(views.html.generic_view(name, data.getOrElse(Map.empty)))
    }
  }

  def displaySubmitConfirmation(conversationId: String): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async { implicit req =>
    submissionRepository.getByConversationId(conversationId).map {
      case Some(submission) => Ok(views.html.submit_confirmation(submission))
      case None => NotFound(views.html.error_template("Submission Not Found", "Submission Not Found", "We're sorry but we couldn't find that submission.")) // TODO throw specific ApplicationException type to be handled via ErrorHandler
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
    } else invalid(name, data)
  }

  def onSubmitComplete: Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async { implicit req =>
    val data: Map[String, String] = formDataAsMap()
    implicit val user: SignedInUser = req.user
    implicit val errors: Map[String, Seq[ValidationError]] = validate(data)
    if (errors.isEmpty) {
      cacheSubmission(data ++ Map("force-last" -> "false")) { (merged, _) =>
        val props = merged.filterNot(entry => navigationKeys.contains(entry._1) || knownBad.contains(entry._1) || entry._2.trim.isEmpty)
        updateMetaData(MetaData.fromProperties(props)).flatMap( metadata =>
          client.submitImportDeclaration(metadata.get).map { resp =>
          Redirect(routes.DeclarationController.displaySubmitConfirmation(resp.conversationId))
        })

      }
    } else invalid(data("last-page"), data)
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

  //Obviously rubbish at memory and performance
  private def updateMetaData(metaData: MetaData)(implicit req: AuthenticatedRequest[AnyContent], hc: HeaderCarrier) = {
    cache.getForm[ObligationGuaranteeForm](req.user.eori.get,"ObligationGuarantees").flatMap( obligationGuaranteeForm =>
    cache.getGoodsItems(req.user.eori.get).map(_.map { items =>
      val decGoodsItems: Seq[GovernmentAgencyGoodsItem] =
        items.map(rec => GovernmentAgencyGoodsItem(customsValueAmount = rec.goodsItemValue.get.customsValueAmount,
        sequenceNumeric = rec.goodsItemValue.get.sequenceNumeric,
        statisticalValueAmount = rec.goodsItemValue.get.statisticalValueAmount,
        transactionNatureCode = rec.goodsItemValue.get.transactionNatureCode,
        additionalDocuments = rec.additionalDocuments,
        additionalInformations= rec.additionalInformations,
        aeoMutualRecognitionParties = rec.aeoMutualRecognitionParties,
        buyer  = rec.buyer,
          commodity = rec.commodity,
        consignee = rec.consignee,
          consignor = rec.consignor,
          customsValuation = rec.customsValuation,
          destination = rec.goodsItemValue.get.destination,
          domesticDutyTaxParties = rec.domesticDutyTaxParties,
          exportCountry = rec.goodsItemValue.get.exportCountry,
          governmentProcedures = rec.governmentProcedures,
          manufacturers = rec.manufacturers,
          origins = rec.origins,
          packagings = rec.packagings,
          previousDocuments = rec.previousDocuments,
          refundRecipientParties = rec.refundRecipientParties,
          seller = rec.seller,
          ucr = rec.goodsItemValue.get.ucr,
          valuationAdjustment = rec.goodsItemValue.get.valuationAdjustment))
      val goodsShipmentNew = metaData.declaration.goodsShipment.map(rec => rec.copy(governmentAgencyGoodsItems = decGoodsItems))
      metaData.copy(declaration = metaData.declaration.copy(goodsShipment = goodsShipmentNew, obligationGuarantees = obligationGuaranteeForm.get.guarantees))
    }))
  }

}
