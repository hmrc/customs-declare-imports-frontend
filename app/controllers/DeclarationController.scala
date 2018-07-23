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
class DeclarationController @Inject()(actions: Actions, client: CustomsDeclarationsConnector, val messagesApi: MessagesApi)(implicit val appConfig: AppConfig, ec: ExecutionContext) extends FrontendController with I18nSupport {

  private val dateTimePattern = "(20\\d{6})(\\d{6}(Z|[-+]\\d\\d))?"
  private val dateTimePatternErrorMessage = "Date time string does not match required pattern"
  private val dateTimeFormatCodes = Set("102", "304")
  private val dateTimeFormatCodeErrorMessage = "Unknown format code. Must be either '102' or '304'"

  val declarationForm: Form[AllInOneForm] = Form(
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
          "functionCode" -> optional(text.verifying("Unknown function code. Must be one of 9, 13, or 14", Set("9", "13", "14").contains(_))),
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
          "totalGrossMassMeasure" -> optional(bigDecimal(precision = 16, scale = 6))
          //      "totalGrossMassMeasureUnitCode" -> optional(text(maxLength = 5))
        )(DeclarationForm.apply)(DeclarationForm.unapply).verifying("Acceptance Date Time Format Code must be specified when Acceptance Date Time is provided", form => {
          form.acceptanceDateTime.isEmpty || (form.acceptanceDateTime.isDefined && form.acceptanceDateTimeFormatCode.isDefined)
        }).verifying("Issue Date Time Format Code must be specified when Issue Date Time is provided", form => {
          form.issueDateTime.isEmpty || (form.issueDateTime.isDefined && form.issueDateTimeFormatCode.isDefined)
        })
      )(MetaDataForm.apply)(MetaDataForm.unapply)
    )(AllInOneForm.apply)(AllInOneForm.unapply)
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

case class AllInOneForm(badgeId: Option[String] = None,
                        metaData: MetaDataForm = MetaDataForm()) {


  private val defaultDateTimeFormatCode = "304"

  def toMetaData: MetaData = MetaData(
    Declaration(
      acceptanceDateTime = metaData.declaration.acceptanceDateTime.map(dt => AcceptanceDateTime(DateTimeString(metaData.declaration.acceptanceDateTimeFormatCode.getOrElse(defaultDateTimeFormatCode), dt))),
      functionCode = metaData.declaration.functionCode,
      functionalReferenceId = metaData.declaration.functionalReferenceId,
      id = metaData.declaration.id,
      issueDateTime = metaData.declaration.issueDateTime.map(dt => IssueDateTime(DateTimeString(metaData.declaration.issueDateTimeFormatCode.getOrElse(defaultDateTimeFormatCode), dt))),
      issueLocationId = metaData.declaration.issueLocationId,
      typeCode = metaData.declaration.typeCode,
      goodsItemQuantity = metaData.declaration.goodsItemQuantity,
      declarationOfficeId = metaData.declaration.declarationOfficeId,
      invoiceAmount = metaData.declaration.invoiceAmount.map(InvoiceAmount(_, metaData.declaration.invoiceAmountCurrencyId)),
      loadingListQuantity = metaData.declaration.loadingListQuantity,
      totalGrossMassMeasure = metaData.declaration.totalGrossMassMeasure.map(MassMeasure(_)) // TODO map in total gross mass measure unit code
    ),
    metaData.wcoDataModelVersionCode,
    metaData.wcoTypeName,
    metaData.responsibleCountryCode,
    metaData.responsibleAgencyName,
    metaData.agencyAssignedCustomizationCode,
    metaData.agencyAssignedCustomizationVersionCode
  )

}

case class MetaDataForm(wcoDataModelVersionCode: Option[String] = None,
                        wcoTypeName: Option[String] = None,
                        responsibleCountryCode: Option[String] = None,
                        responsibleAgencyName: Option[String] = None,
                        agencyAssignedCustomizationCode: Option[String] = None,
                        agencyAssignedCustomizationVersionCode: Option[String] = None,
                        declaration: DeclarationForm = DeclarationForm())

// At present, our form mirrors the declaration XML exactly. Later this may change. Therefore, it is probably useful
// to retain a distinction between view model class and XML model class and map the former to the latter
case class DeclarationForm(acceptanceDateTime: Option[String] = None,
                           acceptanceDateTimeFormatCode: Option[String] = None,
                           functionCode: Option[String] = None,
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
                           totalGrossMassMeasure: Option[BigDecimal] = None
                          /*totalGrossMassMeasureUnitCode: Option[String] = None*/)
