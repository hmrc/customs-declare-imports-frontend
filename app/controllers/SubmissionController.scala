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

import config.AppConfig
import domain.declaration._
import domain.features.Feature
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsDeclarationsConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionController @Inject()(actions: Actions, client: CustomsDeclarationsConnector, val messagesApi: MessagesApi)(implicit val appConfig: AppConfig, ec: ExecutionContext) extends FrontendController with I18nSupport {

  private val dateTimePattern = "(20\\d{6})(\\d{6}(Z|[-+]\\d\\d))?"
  private val dateTimePatternErrorMessage = "Date time string does not match required pattern"
  private val dateTimeFormatCodes = Set("102", "304")
  private val dateTimeFormatCodeErrorMessage = "Unknown format code. Must be either '102' or '304'"

  val declarationForm: Form[SubmissionAllInOneForm] = Form(
    mapping(
      "badgeId" -> optional(text),
      "metaData" -> mapping(
        "wcoDataModelVersionCode" -> optional(text),
        "wcoTypeName" -> optional(text),
        "responsibleCountryCode" -> optional(text),
        "responsibleAgencyName" -> optional(text),
        "agencyAssignedCustomizationCode" -> optional(text),
        "agencyAssignedCustomizationVersionCode" -> optional(text),
        "declaration" -> mapping(
          "acceptanceDateTime" -> optional(text(maxLength = 35).verifying(dateTimePatternErrorMessage, _.matches(dateTimePattern))),
          "acceptanceDateTimeFormatCode" -> optional(text.verifying(dateTimeFormatCodeErrorMessage, dateTimeFormatCodes.contains(_))),
          "functionCode" -> optional(number.verifying("Unknown function code. Must be one of 9, 13, or 14", Set(9, 13, 14).contains(_))),
          "functionalReferenceId" -> optional(text(maxLength = 35)),
          "id" -> optional(text(maxLength = 70)),
          "issueDateTime" -> optional(text(maxLength = 35).verifying(dateTimePatternErrorMessage, _.matches(dateTimePattern))),
          "issueDateTimeFormatCode" -> optional(text.verifying(dateTimeFormatCodeErrorMessage, dateTimeFormatCodes.contains(_))),
          "issueLocationId" -> optional(text(maxLength = 5)),
          "typeCode" -> optional(text(maxLength = 3)),
          "goodsItemQuantity" -> optional(number(min = 0, max = 99999)),
          "declarationOfficeId" -> optional(text(maxLength = 17)),
          "invoiceAmount" -> optional(bigDecimal(precision = 16, scale = 3)),
          "invoiceAmountCurrencyId" -> optional(text(maxLength = 3)),
          "loadingListQuantity" -> optional(number(min = 0, max = 99999)),
          "totalGrossMassMeasure" -> optional(bigDecimal(precision = 16, scale = 6)),
          "totalGrossMassMeasureUnitCode" -> optional(text(maxLength = 5)),
          "totalPackageQuantity" -> optional(number(min = 0, max = 99999999)),
          "specificCircumstancesCodeCode" -> optional(text(maxLength = 3)),
          "authentication" -> mapping(
            "authentication" -> optional(text(maxLength = 256)),
            "authenticatorName" -> optional(text(maxLength = 70))
          )(SubmissionAuthenticationForm.apply)(SubmissionAuthenticationForm.unapply),
          "submitter" -> mapping(
            "name" -> optional(text(maxLength = 70)),
            "id" -> optional(text(maxLength = 17)),
            "address" -> mapping(
              "cityName" -> optional(text(maxLength = 35)),
              "countryCode" -> optional(text(minLength = 2, maxLength = 2)),
              "countrySubDivisionCode" -> optional(text(maxLength = 9)),
              "countrySubDivisionName" -> optional(text(maxLength = 35)),
              "line" -> optional(text(maxLength = 70)),
              "postcodeId" -> optional(text(maxLength = 9))
            )(SubmissionAddressForm.apply)(SubmissionAddressForm.unapply)
          )(SubmissionSubmitterForm.apply)(SubmissionSubmitterForm.unapply),
          "additionalDocument" -> mapping(
            "id" -> optional(text(maxLength = 70)),
            "categoryCode" -> optional(text(maxLength = 3)),
            "typeCode" -> optional(text(maxLength = 3))
          )(SubmissionAdditionalDocumentForm.apply)(SubmissionAdditionalDocumentForm.unapply),
          "hack" -> mapping(
            "additionalInformation" -> mapping(
              "statementCode" -> optional(text(maxLength = 17)),
              "statementDescription" -> optional(text(maxLength = 512)),
              "statementTypeCode" -> optional(text(maxLength = 3)),
              "pointer" -> mapping(
                "sequenceNumeric" -> optional(number(min = 0, max = 99999)),
                "documentSectionCode" -> optional(text(maxLength = 3)),
                "tagId" -> optional(text(maxLength = 4))
              )(SubmissionPointerForm.apply)(SubmissionPointerForm.unapply)
            )(SubmissionAdditionalInformationForm.apply)(SubmissionAdditionalInformationForm.unapply),
            "agent" -> mapping(
              "name" -> optional(text(maxLength = 70)),
              "id" -> optional(text(maxLength = 17)),
              "functionCode" -> optional(text(maxLength = 3)),
              "address" -> mapping(
                "cityName" -> optional(text(maxLength = 35)),
                "countryCode" -> optional(text(minLength = 2, maxLength = 2)),
                "countrySubDivisionCode" -> optional(text(maxLength = 9)),
                "countrySubDivisionName" -> optional(text(maxLength = 35)),
                "line" -> optional(text(maxLength = 70)),
                "postcodeId" -> optional(text(maxLength = 9))
              )(SubmissionAddressForm.apply)(SubmissionAddressForm.unapply)
            )(SubmissionAgentForm.apply)(SubmissionAgentForm.unapply),
            "authorisationHolder" -> mapping(
              "id" -> optional(text(maxLength = 17)),
              "categoryCode" -> optional(text(maxLength = 4))
            )(SubmissionAuthorisationHolderForm.apply)(SubmissionAuthorisationHolderForm.unapply),
            "borderTransportMeans" -> mapping(
              "name" -> optional(text(maxLength = 35)),
              "id" -> optional(text(maxLength = 35)),
              "identificationTypeCode" -> optional(text(maxLength = 17)),
              "typeCode" -> optional(text(maxLength = 4)),
              "registrationNationalityCode" -> optional(text(maxLength = 2)),
              "modeCode" -> optional(number(min = 0, max = 9))
            )(SubmissionBorderTransportMeansForm.apply)(SubmissionBorderTransportMeansForm.unapply),
            "currencyExchange" -> mapping(
              "currencyTypeCode" -> optional(text(maxLength = 3)),
              "rateNumeric" -> optional(bigDecimal(precision = 12, scale = 5))
            )(SubmissionCurrencyExchangeForm.apply)(SubmissionCurrencyExchangeForm.unapply)
          )(SubmissionMassiveHackToCreateHugeForm.apply)(SubmissionMassiveHackToCreateHugeForm.unapply)
        )(SubmissionDeclarationForm.apply)(SubmissionDeclarationForm.unapply).verifying("Acceptance Date Time Format Code must be specified when Acceptance Date Time is provided", form => {
          form.acceptanceDateTime.isEmpty || (form.acceptanceDateTime.isDefined && form.acceptanceDateTimeFormatCode.isDefined)
        }).verifying("Issue Date Time Format Code must be specified when Issue Date Time is provided", form => {
          form.issueDateTime.isEmpty || (form.issueDateTime.isDefined && form.issueDateTimeFormatCode.isDefined)
        })
      )(SubmissionMetaDataForm.apply)(SubmissionMetaDataForm.unapply)
    )(SubmissionAllInOneForm.apply)(SubmissionAllInOneForm.unapply)
  )

  def showDeclarationForm: Action[AnyContent] = (actions.switch(Feature.declaration) andThen actions.auth).async { implicit req =>
    Future.successful(Ok(views.html.declaration_form(declarationForm)))
  }

  def handleDeclarationForm: Action[AnyContent] = (actions.switch(Feature.declaration) andThen actions.auth).async { implicit req =>
    val bound = declarationForm.bindFromRequest()
    bound.fold(
      errors => Future.successful(BadRequest(views.html.declaration_form(errors))),
      success => {
        client.submitImportDeclaration(success.toMetaData, success.badgeId).map { b =>
          Ok(views.html.declaration_acknowledgement(b))
        }
      }
    )
  }

}

case class SubmissionCurrencyExchangeForm(currencyTypeCode: Option[String] = None,
                                rateNumeric: Option[BigDecimal] = None) {

  def toCurrencyExchange: Option[CurrencyExchange] = if (anyDefined) Some(CurrencyExchange(
    currencyTypeCode, rateNumeric
  )) else None

  private def anyDefined: Boolean = currencyTypeCode.isDefined || rateNumeric.isDefined

}

case class SubmissionBorderTransportMeansForm(name: Option[String] = None,
                                    id: Option[String] = None,
                                    identificationTypeCode: Option[String] = None,
                                    typeCode: Option[String] = None,
                                    registrationNationalityCode: Option[String] = None,
                                    modeCode: Option[Int] = None) {

  def toBorderTransportMeans: Option[BorderTransportMeans] = if (anyDefined) Some(BorderTransportMeans(
    name, id, identificationTypeCode, typeCode, registrationNationalityCode, modeCode
  )) else None

  private def anyDefined: Boolean = name.isDefined ||
    id.isDefined ||
    identificationTypeCode.isDefined ||
    typeCode.isDefined ||
    registrationNationalityCode.isDefined ||
    modeCode.isDefined

}

case class SubmissionAuthorisationHolderForm(id: Option[String] = None,
                                   categoryCode: Option[String] = None) {

  def toAuthorisationHolder: Option[AuthorisationHolder] = if (anyDefined) Some(AuthorisationHolder(
    id, categoryCode
  )) else None

  private def anyDefined: Boolean = id.isDefined || categoryCode.isDefined

}

case class SubmissionAllInOneForm(badgeId: Option[String] = None,
                        metaData: SubmissionMetaDataForm = SubmissionMetaDataForm()) {

  def toMetaData: MetaData = metaData.toMetaData

}

case class SubmissionMetaDataForm(wcoDataModelVersionCode: Option[String] = None,
                        wcoTypeName: Option[String] = None,
                        responsibleCountryCode: Option[String] = None,
                        responsibleAgencyName: Option[String] = None,
                        agencyAssignedCustomizationCode: Option[String] = None,
                        agencyAssignedCustomizationVersionCode: Option[String] = None,
                        declaration: SubmissionDeclarationForm = SubmissionDeclarationForm()) {

  def toMetaData: MetaData = MetaData(wcoDataModelVersionCode, wcoTypeName, responsibleCountryCode, responsibleAgencyName, agencyAssignedCustomizationCode, agencyAssignedCustomizationVersionCode, declaration.toDeclaration)

}

// At present, our form mirrors the declaration XML exactly. Later this may change. Therefore, it is probably useful
// to retain a distinction between view model class and XML model class and map the former to the latter
case class SubmissionDeclarationForm(acceptanceDateTime: Option[String] = None,
                           acceptanceDateTimeFormatCode: Option[String] = None,
                           functionCode: Option[Int] = None,
                           functionalReferenceId: Option[String] = None,
                           id: Option[String] = None,
                           issueDateTime: Option[String] = None,
                           issueDateTimeFormatCode: Option[String] = None,
                           issueLocationId: Option[String] = None,
                           typeCode: Option[String] = None,
                           goodsItemQuantity: Option[Int] = None,
                           declarationOfficeId: Option[String] = None,
                           invoiceAmount: Option[BigDecimal] = None,
                           invoiceAmountCurrencyId: Option[String] = None,
                           loadingListQuantity: Option[Int] = None,
                           totalGrossMassMeasure: Option[BigDecimal] = None,
                           totalGrossMassMeasureUnitCode: Option[String] = None,
                           totalPackageQuantity: Option[Int] = None,
                           specificCircumstancesCodeCode: Option[String] = None,
                           authentication: SubmissionAuthenticationForm = SubmissionAuthenticationForm(),
                           submitter: SubmissionSubmitterForm = SubmissionSubmitterForm(),
                           additionalDocument: SubmissionAdditionalDocumentForm = SubmissionAdditionalDocumentForm(),
                           hack: SubmissionMassiveHackToCreateHugeForm = SubmissionMassiveHackToCreateHugeForm()) {

  private val defaultDateTimeFormatCode = "304"

  def toDeclaration: Declaration = Declaration(
    acceptanceDateTime = acceptanceDateTime.map(dt => DateTimeElement(DateTimeString(acceptanceDateTimeFormatCode.getOrElse(defaultDateTimeFormatCode), dt))),
    functionCode = functionCode,
    functionalReferenceId = functionalReferenceId,
    id = id,
    issueDateTime = issueDateTime.map(dt => DateTimeElement(DateTimeString(issueDateTimeFormatCode.getOrElse(defaultDateTimeFormatCode), dt))),
    issueLocationId = issueLocationId,
    typeCode = typeCode,
    goodsItemQuantity = goodsItemQuantity,
    declarationOfficeId = declarationOfficeId,
    invoiceAmount = invoiceAmount.map((value: BigDecimal) => Amount(invoiceAmountCurrencyId, Some(value))),
    loadingListQuantity = loadingListQuantity,
    totalGrossMassMeasure = totalGrossMassMeasure.map((value: BigDecimal) => Measure(totalGrossMassMeasureUnitCode, Some(value))),
    totalPackageQuantity = totalPackageQuantity,
    specificCircumstancesCode = specificCircumstancesCodeCode,
    authentication = authentication.toAuthentication,
    additionalDocuments = additionalDocument.toAdditionalDocument.toSeq,
    additionalInformations = hack.additionalInformation.toAdditionalInformation.toSeq,
    agent = hack.agent.toAgent,
    authorisationHolders = hack.authorisationHolder.toAuthorisationHolder.toSeq,
    borderTransportMeans = hack.borderTransportMeans.toBorderTransportMeans,
    currencyExchanges = hack.currencyExchange.toCurrencyExchange.toSeq
  )

}

case class SubmissionAuthenticationForm(authentication: Option[String] = None,
                              authenticatorName: Option[String] = None) {

  def toAuthentication: Option[Authentication] = (authentication, authenticatorName) match {
    case (Some(auth), Some(name)) => Some(Authentication(Some(auth), Some(Authenticator(Some(name)))))
    case (Some(auth), None) => Some(Authentication(Some(auth)))
    case (None, Some(name)) => Some(Authentication(None, Some(Authenticator(Some(name)))))
    case _ => None
  }

}

case class SubmissionSubmitterForm(name: Option[String] = None,
                         id: Option[String] = None,
                         address: SubmissionAddressForm = SubmissionAddressForm()) {

  def toSubmitter: NamedEntityWithAddress = NamedEntityWithAddress(
    name = name,
    id = id,
    address = address.toAddress
  )

}

case class SubmissionAddressForm(cityName: Option[String] = None,
                       countryCode: Option[String] = None,
                       countrySubDivisionCode: Option[String] = None,
                       countrySubDivisionName: Option[String] = None,
                       line: Option[String] = None,
                       postcodeId: Option[String] = None) {

  def toAddress: Option[Address] = if (anyDefined) Some(Address(
    cityName, countryCode, countrySubDivisionCode, countrySubDivisionName, line, postcodeId
  )) else None

  private def anyDefined: Boolean = cityName.isDefined ||
    countryCode.isDefined ||
    countrySubDivisionCode.isDefined ||
    countrySubDivisionName.isDefined ||
    line.isDefined ||
    postcodeId.isDefined

}

case class SubmissionAdditionalDocumentForm(id: Option[String] = None, // max 70 chars
                                             categoryCode: Option[String] = None, // max 3 chars
                                             typeCode: Option[String] = None) { // max 3 chars

  def toAdditionalDocument: Option[AdditionalDocument] = if (anyDefined) Some(AdditionalDocument(
    id, categoryCode, typeCode
  )) else None

  private def anyDefined: Boolean = id.isDefined ||
    categoryCode.isDefined ||
    typeCode.isDefined

}

case class SubmissionMassiveHackToCreateHugeForm(additionalInformation: SubmissionAdditionalInformationForm = SubmissionAdditionalInformationForm(),
                                       agent: SubmissionAgentForm = SubmissionAgentForm(),
                                       authorisationHolder: SubmissionAuthorisationHolderForm = SubmissionAuthorisationHolderForm(),
                                       borderTransportMeans: SubmissionBorderTransportMeansForm = SubmissionBorderTransportMeansForm(),
                                       currencyExchange: SubmissionCurrencyExchangeForm = SubmissionCurrencyExchangeForm())

case class SubmissionAdditionalInformationForm(statementCode: Option[String] = None,
                                     statementDescription: Option[String] = None,
                                     statementTypeCode: Option[String] = None,
                                     pointer: SubmissionPointerForm = SubmissionPointerForm()) {

  def toAdditionalInformation: Option[AdditionalInformation] = if (anyDefined) Some(AdditionalInformation(
    statementCode, statementDescription, statementTypeCode, pointer.toPointer.toSeq
  )) else None

  private def anyDefined: Boolean = statementCode.isDefined ||
    statementDescription.isDefined ||
    statementTypeCode.isDefined ||
    pointer.toPointer.isDefined

}

case class SubmissionPointerForm(sequenceNumeric: Option[Int] = None,
                       documentSectionCode: Option[String] = None,
                       tagId: Option[String] = None) {

  def toPointer: Option[Pointer] = if (anyDefined) Some(Pointer(
    sequenceNumeric, documentSectionCode, tagId
  )) else None

  private def anyDefined: Boolean = sequenceNumeric.isDefined ||
    documentSectionCode.isDefined ||
    tagId.isDefined

}

case class SubmissionAgentForm(name: Option[String] = None,
                     id: Option[String] = None,
                     functionCode: Option[String] = None,
                     address: SubmissionAddressForm = SubmissionAddressForm()) {

  def toAgent: Option[Agent] = if (anyDefined) Some(Agent(
    name, id, functionCode, address.toAddress
  )) else None

  private def anyDefined: Boolean = name.isDefined ||
    id.isDefined ||
    functionCode.isDefined ||
    address.toAddress.isDefined

}
