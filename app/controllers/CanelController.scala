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

import javax.inject.{Singleton, Inject}

import config.AppConfig
import domain.cancellation._
import domain.features.Feature
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{AnyContent, Action}
import services.CustomsDeclarationsConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{Future, ExecutionContext}


@Singleton
class CancelController @Inject()(actions: Actions, client: CustomsDeclarationsConnector,val messagesApi: MessagesApi)
(implicit val appConfig: AppConfig, ec: ExecutionContext) extends FrontendController with I18nSupport{

  val cancelForm: Form[AllInOneCancelForm] = Form(
    mapping(
      "badgeId" -> optional(text),
      "metaData" -> mapping(
        "wcoDataModelVersionCode" -> nonEmptyText,
        "wcoTypeName" -> nonEmptyText,
        "responsibleCountryCode" -> nonEmptyText,
        "responsibleAgencyName" -> nonEmptyText,
        "agencyAssignedCustomizationVersionCode" -> nonEmptyText,
        "declaration" -> mapping(
          "functionCode" -> nonEmptyText.verifying("Unknown function code. Must be 13", Set("13").contains(_)),
          "functionalReferenceId" -> optional(text(maxLength = 35)),
          "id" -> nonEmptyText(maxLength = 70),
          "submitter" -> mapping(
            "name" -> optional(text(maxLength = 70)),
            "id" -> text(maxLength = 17)
          )(CancelSubmitterForm.apply)(CancelSubmitterForm.unapply),
          "additionalInformation" -> mapping(
            "statementDescription" -> nonEmptyText(maxLength = 512),
            "statementTypeCode" -> optional(text(maxLength = 3)),
            "pointer" -> mapping(
            "documentSectionCode" -> optional(text(maxLength = 3))
            )(PointerForm.apply)(PointerForm.unapply)
          )(AdditionalInformationForm.apply)(AdditionalInformationForm.unapply),
          "amendment" -> mapping(
          "changeReasonCode" -> nonEmptyText
          )(AmendmentForm.apply)(AmendmentForm.unapply)
        )(CancelDeclarationForm.apply)(CancelDeclarationForm.unapply)
      )(CancelMetaDataForm.apply)(CancelMetaDataForm.unapply)
    )(AllInOneCancelForm.apply)(AllInOneCancelForm.unapply)
  )

  def showCancelForm: Action[AnyContent] = (actions.switch(Feature.cancel) andThen actions.auth).async { implicit req =>
    Future.successful(Ok(views.html.cancel_form(cancelForm)))
  }

  def submitCancelForm: Action[AnyContent] = (actions.switch(Feature.cancel) andThen actions.auth).async { implicit req =>
    val resultForm = cancelForm.bindFromRequest()
    Logger.debug("Form Submitted" + resultForm.errors.mkString("--"))
    resultForm.fold (
        errorsWithErrors => Future.successful(BadRequest(views.html.cancel_form(errorsWithErrors))),
        success => { Logger.debug("successful form submission")
          client.cancelImportDeclaration(success.toMetaData, success.badgeId).map { b =>
            Logger.debug("Submission sent to api successfully ")
            Ok(views.html.declaration_acknowledgement(b))
          }
    }
    )
  }

}

case class AllInOneCancelForm(badgeId: Option[String] = None,
                        metaData: CancelMetaDataForm) {

  def toMetaData: MetaData = metaData.toCancelMetaData

}

case class CancelMetaDataForm(wcoDataModelVersionCode: String,
                        wcoTypeName: String,
                        responsibleCountryCode: String,
                        responsibleAgencyName: String,
                              agencyAssignedCustomizationVersionCode: String,
                        declaration: CancelDeclarationForm) {

  def toCancelMetaData: MetaData = MetaData(
    wcoDataModelVersionCode,
    wcoTypeName,
    responsibleCountryCode,
    responsibleAgencyName,
    agencyAssignedCustomizationVersionCode,
    declaration.toDeclaration)

}

// At present, our form mirrors the declaration XML exactly. Later this may change. Therefore, it is probably useful
// to retain a distinction between view model class and XML model class and map the former to the latter
case class CancelDeclarationForm(functionCode: String,
                           functionalReferenceId: Option[String] = None,
                           id: String,
                           submitter: CancelSubmitterForm,
                           additionalInformation: AdditionalInformationForm,
                           amendment:AmendmentForm) {


  def toDeclaration: Declaration = Declaration(functionCode,
    functionalReferenceId,
    id,
    submitter = submitter.toSubmitter,
    additionalInformation = additionalInformation.toAdditionalInformation(),
    amendment = Amendment(amendment.changeReasonCode)
  )

}

case class PointerForm(documentSectionCode: Option[String])  {
  def toPointer() = if(documentSectionCode.isDefined) Some(Pointer(documentSectionCode)) else None
}

case class AdditionalInformationForm(statementDescription: String, statementTypeCode: Option[String], pointer: PointerForm)
{
  def toAdditionalInformation() = AdditionalInformation(statementDescription, statementTypeCode, pointer.toPointer())
}


case class CancelSubmitterForm(name: Option[String] = None,
                         id: String) {
  def toSubmitter: Submitter = Submitter(name,id)}

case class AmendmentForm(changeReasonCode: String)


