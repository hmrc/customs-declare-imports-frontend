/*
 * Copyright 2018 HM Revenue & Customs
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
import domain.features.Feature
import domain.wco.{AdditionalInformation, Amendment, Declaration, MetaData}
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.{CustomsDeclarationsConnector, SessionCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationController @Inject()(actions: Actions, client: CustomsDeclarationsConnector, cache: SessionCacheService)
                                     (implicit val messagesApi: MessagesApi, val appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController with I18nSupport {

  def displaySubmitForm(name: String): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async { implicit req =>
    cache.get(appConfig.submissionCacheId, req.user.eori.get).map { data =>
      Ok(views.html.generic_view(name, data.getOrElse(Map.empty)))
    }
  }

  def handleSubmitForm(name: String): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async { implicit req =>
    val data: Map[String, String] = req.body.asFormUrlEncoded.map { form =>
      form.
        map(field => field._1 -> field._2.headOption).
        filter(_._2.isDefined).
        map(field => field._1 -> field._2.get)
    }.getOrElse(Map.empty)
    Logger.info("Data: " + data.mkString("\n"))
    implicit val errors: Map[String, Seq[ValidationError]] = validate(data)
    Logger.info("Errs: " + errors.mkString("\n"))
    errors.isEmpty match {
      case true => {
        cache.get(appConfig.submissionCacheId, req.user.requiredEori).flatMap { cached =>
          val merged = cached.getOrElse(Map.empty) ++ data
          cache.put(appConfig.submissionCacheId, req.user.requiredEori, merged).map { _ =>
            Redirect(routes.DeclarationController.displaySubmitForm(data("next-page")))
          }
        }
      }
      case false => {
        Future.successful(BadRequest(views.html.generic_view(name, data)))
      }
    }
  }

  // TODO implement onComplete handler in GenericController
  def onSubmitComplete: Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async { implicit req =>
    Future.successful(Ok)
  }

  def showCancelForm: Action[AnyContent] = (actions.switch(Feature.cancel) andThen actions.auth).async { implicit req =>
    Future.successful(Ok(views.html.cancel_form(Cancel.form)))
  }

  def handleCancelForm: Action[AnyContent] = (actions.switch(Feature.cancel) andThen actions.auth).async { implicit req =>
    val resultForm = Cancel.form.bindFromRequest()
    resultForm.fold(
      errorsWithErrors => Future.successful(BadRequest(views.html.cancel_form(errorsWithErrors))),
      success => {
        client.cancelImportDeclaration(success.toMetaData).map { b =>
          Ok(views.html.cancel_confirmation(b))
        }
      }
    )
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

  private def errorMessageKey(field: FieldDefinition, result: ValidationResult): String = messagesApi(Seq(s"${field.labelKey}.${result.defaultErrorKey}", result.defaultErrorKey), result.args:_*)

}

// cancel declaration form objects

object Cancel {
  val form: Form[CancelForm] = Form[CancelForm](mapping(
    "metaData" -> mapping(
      "wcoDataModelVersionCode" -> nonEmptyText,
      "wcoTypeName" -> nonEmptyText,
      "responsibleCountryCode" -> nonEmptyText,
      "responsibleAgencyName" -> nonEmptyText,
      "agencyAssignedCustomizationVersionCode" -> nonEmptyText,
      "declaration" -> mapping(
        "typeCode" -> nonEmptyText(maxLength = 3).verifying("Type Code must be 'INV'", str => "INV" == str),
        "functionCode" -> number(min = 13, max = 13),
        "functionalReferenceId" -> optional(text(maxLength = 35)),
        "id" -> nonEmptyText(maxLength = 70),
        "additionalInformation" -> mapping(
          "statementDescription" -> nonEmptyText(maxLength = 512)
        )(CancelAdditionalInformationForm.apply)(CancelAdditionalInformationForm.unapply),
        "amendment" -> mapping(
          "changeReasonCode" -> nonEmptyText
        )(CancelAmendmentForm.apply)(CancelAmendmentForm.unapply)
      )(CancelDeclarationForm.apply)(CancelDeclarationForm.unapply)
    )(CancelMetaDataForm.apply)(CancelMetaDataForm.unapply)
  )(CancelForm.apply)(CancelForm.unapply))
}

case class CancelForm(metaData: CancelMetaDataForm) {

  def toMetaData: MetaData = metaData.toCancelMetaData

}

case class CancelMetaDataForm(wcoDataModelVersionCode: String,
                              wcoTypeName: String,
                              responsibleCountryCode: String,
                              responsibleAgencyName: String,
                              agencyAssignedCustomizationVersionCode: String,
                              declaration: CancelDeclarationForm) {

  def toCancelMetaData: MetaData = MetaData(
    wcoDataModelVersionCode = Some(wcoDataModelVersionCode),
    wcoTypeName = Some(wcoTypeName),
    responsibleCountryCode = Some(responsibleCountryCode),
    responsibleAgencyName = Some(responsibleAgencyName),
    agencyAssignedCustomizationVersionCode = Some(agencyAssignedCustomizationVersionCode),
    declaration = declaration.toDeclaration
  )

}

case class CancelDeclarationForm(typeCode: String = "INV",
                                 functionCode: Int = 13,
                                 functionalReferenceId: Option[String] = None,
                                 id: String,
                                 additionalInformation: CancelAdditionalInformationForm,
                                 amendment: CancelAmendmentForm) {

  def toDeclaration: Declaration = Declaration(
    typeCode = Some(typeCode),
    functionCode = Some(functionCode),
    functionalReferenceId = functionalReferenceId,
    id = Some(id),
    additionalInformations = Seq(AdditionalInformation(statementDescription = Some(additionalInformation.statementDescription))),
    amendments = Seq(Amendment(Some(amendment.changeReasonCode)))
  )

}

case class CancelAdditionalInformationForm(statementDescription: String)

case class CancelAmendmentForm(changeReasonCode: String)
