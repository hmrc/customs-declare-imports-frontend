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

import javax.inject.{Inject, Singleton}
import config.AppConfig
import domain.wco._
import domain.features.Feature
import domain.wco
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsDeclarationsConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CancelController @Inject()(actions: Actions, client: CustomsDeclarationsConnector, val messagesApi: MessagesApi)
                                (implicit val appConfig: AppConfig, ec: ExecutionContext) extends FrontendController with I18nSupport {

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
          "functionCode" -> number(min = 13, max = 13),
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
            )(CancelPointerForm.apply)(CancelPointerForm.unapply)
          )(CancelAdditionalInformationForm.apply)(CancelAdditionalInformationForm.unapply),
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
    resultForm.fold(
      errorsWithErrors => Future.successful(BadRequest(views.html.cancel_form(errorsWithErrors))),
      success => {
        Logger.debug("successful form submission")
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
    wcoDataModelVersionCode = Some(wcoDataModelVersionCode),
    wcoTypeName = Some(wcoTypeName),
    responsibleCountryCode = Some(responsibleCountryCode),
    responsibleAgencyName = Some(responsibleAgencyName),
    agencyAssignedCustomizationVersionCode = Some(agencyAssignedCustomizationVersionCode),
    declaration = declaration.toDeclaration
  )

}

// At present, our form mirrors the declaration XML exactly. Later this may change. Therefore, it is probably useful
// to retain a distinction between view model class and XML model class and map the former to the latter
case class CancelDeclarationForm(functionCode: Int,
                                 functionalReferenceId: Option[String] = None,
                                 id: String,
                                 submitter: CancelSubmitterForm,
                                 additionalInformation: CancelAdditionalInformationForm,
                                 amendment: AmendmentForm) {


  def toDeclaration: Declaration = Declaration(
    functionCode = Some(functionCode),
    functionalReferenceId = functionalReferenceId,
    id = Some(id),
    submitter = Some(submitter.toSubmitter),
    additionalInformations = Seq(additionalInformation.toAdditionalInformation()),
    amendments = Seq(Amendment(Some(amendment.changeReasonCode)))
  )

}

case class CancelPointerForm(documentSectionCode: Option[String]) {
  def toPointer(): Option[Pointer] = if (documentSectionCode.isDefined) Some(Pointer(documentSectionCode = documentSectionCode)) else None
}

case class CancelAdditionalInformationForm(statementDescription: String, statementTypeCode: Option[String], pointer: CancelPointerForm) {
  def toAdditionalInformation(): AdditionalInformation = AdditionalInformation(statementDescription = Some(statementDescription), statementTypeCode = statementTypeCode, pointers = pointer.toPointer().toSeq)
}


case class CancelSubmitterForm(name: Option[String] = None,
                               id: String) {
  def toSubmitter: NamedEntityWithAddress = wco.NamedEntityWithAddress(name = name, id = Some(id))
}

case class AmendmentForm(changeReasonCode: String)


