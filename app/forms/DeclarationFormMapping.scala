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

package forms

import java.text.DecimalFormat

import domain.{GoodsItemValueInformation, InvoiceAndCurrency, References}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.data.Forms.{number, _}
import play.api.data.Mapping
import uk.gov.hmrc.wco.dec._

import scala.util.Try
import scala.util.control.Exception.allCatch

object DeclarationFormMapping {

  def require1Field[T](fs: (T => Option[_])*): T => Boolean =
    t => fs.exists(f => f(t).nonEmpty)

  def requireAllDependantFields[T](primary: T => Option[_])(fs: (T => Option[_])*): T => Boolean =
    t => primary(t).fold(true)(_ => fs.forall(f => f(t).nonEmpty))

  def isAlpha: String => Boolean = _.matches("^[A-Za-z]*$")


  val govAgencyGoodsItemAddDocumentSubmitterMapping = mapping(
    "name" -> optional(text.verifying("Issuing Authority must be less than 70 characters", _.length <= 70)),
    "roleCode" -> optional(text.verifying("roleCode is only 3 characters", _.length <= 3))
  )(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter.apply)(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter.unapply)

  val amountMapping: Mapping[Amount] = mapping(
    "currencyId" -> optional(
      text
        .verifying("Currency ID is not a valid currency", x => config.Options.currencyTypes.exists(_._1 == x))),
    "value" -> optional(
      bigDecimal
        .verifying("Amount cannot be greater than 99999999999999.99", _.precision <= 16)
        .verifying("Amount cannot have more than 2 decimal places", _.scale <= 2)
        .verifying("Amount must not be negative", _ >= 0))
  )(Amount.apply)(Amount.unapply)
    .verifying("Amount is required when currency is provided", requireAllDependantFields[Amount](_.currencyId)(_.value))
    .verifying("Currency is required when amount is provided", requireAllDependantFields[Amount](_.value)(_.currencyId))

  val currencyExchangeMapping: Mapping[CurrencyExchange] = mapping(
    "currencyTypeCode" -> optional(
      text.verifying("CurrencyTypeCode is not a valid currency", x => config.Options.currencyTypes.exists(_._1 == x))),
    "rateNumeric" -> optional(
      bigDecimal
        .verifying("RateNumeric cannot be greater than 9999999.99999", _.precision <= 12)
        .verifying("RateNumeric cannot have more than 5 decimal places", _.scale <= 5)
        .verifying("RateNumeric must not be negative", _ >= 0))
  )(CurrencyExchange.apply)(CurrencyExchange.unapply)
    .verifying("Exchange rate is required when currency is provided", requireAllDependantFields[CurrencyExchange](_.currencyTypeCode)(_.rateNumeric))
    .verifying("Currency ID is required when amount is provided", requireAllDependantFields[CurrencyExchange](_.rateNumeric)(_.currencyTypeCode))

  val invoiceAndCurrencyMapping = mapping(
    "invoice" -> optional(amountMapping),
    "currency" -> optional(currencyExchangeMapping)
  )(InvoiceAndCurrency.apply)(InvoiceAndCurrency.unapply)

  val measureMapping = mapping("unitCode" -> optional(text.verifying("Measurement Unit & Qualifier cannot be more than 5 characters", _.length <= 5)),
    "value" ->
      optional(bigDecimal.verifying("Quantity cannot be greater than 9999999999.999999", _.precision <= 16)
      .verifying("Quantity cannot have more than 6 decimal places", _.scale <= 6)
      .verifying("Quantity must not be negative", _ >= 0)))(Measure.apply)(Measure.unapply)

  val writeOffMapping = mapping("quantity" -> optional(measureMapping), "amount" -> optional(amountMapping))(WriteOff.apply)(WriteOff.unapply)

  case class Date(day: Int, month: Int, year: Int)

  val dateMapping = mapping(
    "day" -> number.verifying("Day is invalid", day => day >= 1 && day <= 31),
    "month" -> number.verifying("Month is invalid", month => month >= 1 && month <= 12),
    "year" -> number.verifying("Year is invalid", _ >= 1900)
  )(Date.apply)(Date.unapply)
    .verifying("Date entered is invalid", isDateValid)

  def isDateValid(): Date => Boolean = date => {
    val df = new DecimalFormat("00")
    (allCatch[DateTime] opt (DateTime.parse(s"${date.year}${df.format(date.month)}${df.format(date.day)}",
      DateTimeFormat.forPattern("yyyyMMdd")))).isDefined
  }

  val dateTimeElementMapping = mapping(
    "date" -> dateMapping
  )(date => DateTimeElement(DateTimeString("102", s"${date.year}/${date.month}/${date.day}")))((
  d: DateTimeElement) => toDate(d.dateTimeString.value))

  def toDate(dateString: String): Option[Date] = {
    def toInt(s: String): Option[Int] = Try(s.toInt).toOption

    for {
      day <- dateString.split("/").lastOption.flatMap(toInt)
      month <- dateString.split("/").lift(1).flatMap(toInt)
      year <- dateString.split("/").headOption.flatMap(toInt)
    } yield {
      Date(day, month, year)
    }
  }

  val govtAgencyGoodsItemAddDocMapping = mapping(
    "categoryCode" -> optional(text.verifying("Category must be 1 character", _.length == 1)), // 3 in schema
    "effectiveDateTime" -> optional(dateTimeElementMapping),
    "id" -> optional(text.verifying("Identifier must be less than 35 characters", _.length <= 35)),
    "name" -> optional(text.verifying("Status Reason must be less than 35 characters", _.length <= 35)),
    "typeCode" -> optional(text.verifying("Type is only 3 characters", _.length == 3)),
    "lpcoExemptionCode" -> optional(text.verifying("Status must be 2 characters", (str => str.length == 2 && isAlpha(str)))),
    "submitter" -> optional(govAgencyGoodsItemAddDocumentSubmitterMapping),
    "writeOff" -> optional(writeOffMapping)
  )(GovernmentAgencyGoodsItemAdditionalDocument.apply)(GovernmentAgencyGoodsItemAdditionalDocument.unapply)
    .verifying("You must provide input for Category or Identifier or status or Status Reason or Issuing Authority and Date of Validity or Quantity and Measurement Unit & Qualifier",
      require1Field[GovernmentAgencyGoodsItemAdditionalDocument](_.categoryCode, _.effectiveDateTime, _.id, _.name,
        _.lpcoExemptionCode, _.submitter, _.typeCode, _.writeOff))

  lazy val additionalInformationMapping = mapping(
    "statementCode" -> optional(text.verifying("Code should be less than or equal to 5 characters", _.length <= 5)), // max 17 in schema
    "statementDescription" -> optional(text.verifying("Description should be less than or equal to 512 characters", _.length <= 512)),
    "limitDateTime" -> ignored[Option[String]](None),
    "statementTypeCode" -> optional(text.verifying("statement type code should be less than or equal to 3 characters", _.length <= 3)),
    "pointers" -> ignored[Seq[Pointer]](Seq.empty)
  )(AdditionalInformation.apply)(AdditionalInformation.unapply)
    .verifying("You must provide Code or Description", require1Field[AdditionalInformation](_.statementCode, _.statementDescription))

  val destinationMapping = mapping("countryCode" -> optional(text.verifying("country code is only 3 characters", _.length <= 3)),
    "regionId" -> optional(text.verifying("regionId code is only 9 characters", _.length <= 9)))(Destination.apply)(Destination.unapply)

  val ucrMapping = mapping("id" -> optional(text.verifying("id should be less than or equal to 35 characters", _.length <= 35)),
    "traderAssignedReferenceId" -> optional(text.verifying("traderAssignedReferenceId should be less than or equal to 35 characters", _.length <= 35)))(Ucr.apply)(Ucr.unapply)

  val exportCountryMapping = mapping("id" -> text.verifying("export Country code should be less than or equal to 2 characters",
    _.length <= 2))(ExportCountry.apply)(ExportCountry.unapply)

  val valuationAdjustmentMapping = mapping("additionCode" -> optional(
    text.verifying("valuationAdjustment should be less than or equal to 4 characters",
      _.length <= 4)))(ValuationAdjustment.apply)(ValuationAdjustment.unapply)

  val goodsItemValueInformationMapping = mapping(
    "customsValueAmount" -> optional(bigDecimal.verifying("customs Value Amount must not be negative", a => a > 0)),
    "sequenceNumeric" -> number(0, 99999),
    "statisticalValueAmount" -> optional(amountMapping),
    "transactionNatureCode" -> optional(number(0, 99999)),
    "destination" -> optional(destinationMapping),
    "ucr" -> optional(ucrMapping),
    "exportCountry" -> optional(exportCountryMapping),
    "valuationAdjustment" -> optional(valuationAdjustmentMapping))(GoodsItemValueInformation.apply)(GoodsItemValueInformation.unapply)


  val addressMapping = mapping(
    "cityName" -> optional(text.verifying("City name should be 35 characters or less", _.length <= 35)),
    "countryCode" -> optional(text.verifying("Country code is invalid", code => config.Options.countryOptions.exists(_._1 == code))),
    "countrySubDivisionCode" -> optional(text.verifying("Country sub division code should be 9 characters or less", _.length <= 9)),
    "countrySubDivisionName" -> optional(text.verifying("Country sub division name should be 35 characters or less", _.length <= 35)),
    "line" -> optional(text.verifying("Line should be 70 characters or less", _.length <= 70)),
    "postcodeId" -> optional(text.verifying("Postcode should be 9 characters or less", _.length <= 9))
  )(Address.apply)(Address.unapply)

  val namedEntityWithAddressMapping = mapping(
    "name" -> optional(text.verifying("name should be less than or equal to 70 characters", _.length <= 70)), //: Option[String] = None, // max 70 chars
    "id" -> optional(text.verifying("id  should be less than or equal to 17 characters", _.length <= 17)), // max 17 chars
    "address" -> optional(addressMapping)
  )(NamedEntityWithAddress.apply)(NamedEntityWithAddress.unapply)

  val roleBasedPartyMapping = mapping(
    "id" -> optional(text.verifying("Role based party id should be less than or equal to 17 characters", _.length <= 17)), // max 17 chars
    "roleCode" -> optional(text.verifying("Role code should be less than or equal to 3 characters", _.length <= 3)) // max 3 chars
  )(RoleBasedParty.apply)(RoleBasedParty.unapply)
    .verifying("You must provide an ID or role code", require1Field[RoleBasedParty](_.id, _.roleCode))

  val governmentProcedureMapping = mapping(
    "currentCode" -> optional(text.verifying("Current code should be less than or equal to 2 characters", _.length <= 2)), // max 7 chars
    "previousCode" -> optional(text.verifying("Previous code  should be less than or equal to 2 characters", _.length <= 2)) // max 7 chars
  )(GovernmentProcedure.apply)(GovernmentProcedure.unapply)
    .verifying("To add procedure codes you must provide Current Code or Previous code", require1Field[GovernmentProcedure](_.currentCode, _.previousCode))

  val originMapping = mapping(
    "countryCode" -> optional(text.verifying("country Code  should be max of 4 characters", _.length <= 4)), // max 4 chars //expects ISO-3166-1 alpha2 code
    "regionId" -> optional(text.verifying("regionId code should be 9 characters", _.length <= 9)),
    "typeCode" -> optional(text.verifying("typeCode  should be 3 characters", _.length <= 7)) // max 3 chars
  )(Origin.apply)(Origin.unapply)


  val packagingMapping =
    mapping("sequenceNumeric" -> optional(number(0, 99999)), //: Option[Int] = None, // unsigned max 99999
      "marksNumbersId" -> optional(text.verifying("marks Numbers Id should be less than or equal to 512 characters", _.length <= 512)), //: Option[String] = None, // max 512 chars
      "quantity" -> optional(number(0, 99999)), //: Option[Int] = None, // max 99999999
      "typeCode" -> optional(text.verifying("type Code  should be 2 characters", _.length == 2)), //: Option[String] = None, // max 2 chars
      "packingMaterialDescription" -> optional(text.verifying("packing Material Description should be less than or equal to 256 characters", _.length <= 256)), // Option[String] = None, // max 256 chars
      "lengthMeasure" -> optional(longNumber), //: Option[Long] = None, // unsigned int max 999999999999999
      "widthMeasure" -> optional(longNumber), //: Option[Long] = None, // unsigned int max 999999999999999
      "heightMeasure" -> optional(longNumber), //: Option[Long] = None, // unsigned int max 999999999999999
      "volumeMeasure" -> optional(measureMapping))(Packaging.apply)(Packaging.unapply)

  val contactMapping = mapping(
    "name" -> optional(text.verifying("name should be less than or equal to 70 characters", _.length <= 70)) //: Option[String] = None, // max 70 chars
  )(Contact.apply)(Contact.unapply)

  val communicationMapping = mapping(
    "id" -> optional(text.verifying("communication Id should be less than or equal to 70 characters", _.length <= 50)), //: Option[String] = None, // max 50 chars
    "typeCode" -> optional(text.verifying("type Code  should be 3 characters", _.length <= 3)) //: Option[String] = None, // max 3 chars
  )(Communication.apply)(Communication.unapply)

  val importExportPartyMapping = mapping(
    "name" -> optional(text.verifying("Name should have 70 characters or less", _.length <= 70)),
    "id" -> optional(text.verifying("EORI number should have 17 characters or less", _.length <= 17)),
    "address" -> optional(addressMapping),
    "contacts" -> seq(contactMapping),
    "communications" -> seq(communicationMapping)
  )(ImportExportParty.apply)(ImportExportParty.unapply)

  val chargeDeductionMapping = mapping(
    "chargesTypeCode" -> optional(text.verifying("Charges code should be less than or equal to 2 characters", _.length <= 2)),
    "otherChargeDeductionAmount" -> optional(amountMapping) // Option[Amount] = None
  )(ChargeDeduction.apply)(ChargeDeduction.unapply)
    .verifying("Charges code, currency id or amount are required", require1Field[ChargeDeduction](_.chargesTypeCode, _.otherChargeDeductionAmount))


  val customsValuationMapping = mapping("methodCode" -> optional(text.verifying(" Charges code should be less than or equal to 3 characters", _.length <= 3)), // max 3 chars; not valid outside GovernmentAgencyGoodsItem
    "freightChargeAmount" -> optional(bigDecimal), // default(bigDecimal, None),
    "chargeDeductions" -> seq(chargeDeductionMapping))(CustomsValuation.apply)(CustomsValuation.unapply)


  val officeMapping = mapping("id" -> optional(text
    .verifying("Office id should be less than or equal to 8 characters", _.length <= 8))
  )(Office.apply)(Office.unapply)


  val obligationGauranteeMapping =
    mapping("amount" -> optional(bigDecimal
        .verifying("Amount cannot be greater than 99999999999999.99", _.precision <= 16)
        .verifying("Amount cannot have more than 2 decimal places", _.scale <= 2)
        .verifying("Amount must not be negative", _ >= 0)),
      "id" -> optional(text.verifying("Id should be less than or equal to 35 characters", _.length <= 35)), //max schema length is 70
      "referenceId" -> optional(text.verifying("ReferenceId should be less than or equal to 35 characters", _.length <= 35)),
      "securityDetailsCode" -> optional(text.verifying("SecurityDetailsCode should be less than or equal to 3 characters", _.length <= 3)),
      "accessCode" -> optional(text.verifying("AccessCode should be less than or equal to 4 characters", _.length <= 4)),
      "guaranteeOffice" -> optional(officeMapping))(ObligationGuarantee.apply)(ObligationGuarantee.unapply)
      .verifying("You must provide a Deferred Reference ID or ID or Amount of import duty and other charges or Access Code or Customs office of guarantee",
        require1Field[ObligationGuarantee](_.referenceId, _.id, _.amount, _.accessCode, _.guaranteeOffice))

  val guaranteesFormMapping = mapping("guarantees" -> seq(obligationGauranteeMapping))(ObligationGuaranteeForm.apply)(ObligationGuaranteeForm.unapply)

  val guaranteeTypeMapping =
    mapping(
      "securityDetailsCode" ->
        optional(text.verifying("Security details code must be 1 character", _.length == 1))
          .verifying("Security details code is required", _.nonEmpty)
    )(ObligationGuarantee.apply(None, None, None, _, None, None))(ObligationGuarantee.unapply(_).map(_._4))

  val authorisationHolderMapping =
    mapping(
      "id" ->
        optional(text.verifying("ID should be less than or equal to 17 characters", _.length <= 17)),
      "categoryCode" ->
        optional(text.verifying("Category Code should be less than or equal to 4 characters", _.length <= 4))
    )(AuthorisationHolder.apply)(AuthorisationHolder.unapply)
      .verifying("You must provide an ID or category code", require1Field[AuthorisationHolder](_.id, _.categoryCode))

  val previousDocumentMapping = mapping(
    "categoryCode" -> optional(text.verifying("Document Category  should be less than or equal to 1 character", _.length <= 1)), //: Option[String] = None, // max 3 chars
    "id" -> optional(text.verifying("Document Reference should be less than or equal to 35 characters", _.length <= 35)), //: Option[String] = None, // max 70 chars
    "typeCode" -> optional(text.verifying("Previous Document Type should be less than or equal to 3 characters", _.length <= 3)), //: Option[String] = None, // max 3 chars
    "lineNumeric" -> optional(number
      .verifying("Goods Item Identifier should be greater than 0 and less than or equal to 999", lineNumeric => (lineNumeric > 0 && lineNumeric <= 999))) //: Option[Int] = None, // max 99999999
  )(PreviousDocument.apply)(PreviousDocument.unapply)
    .verifying("You must provide a Document Category or Document Reference or Previous Document Type or Goods Item Identifier", require1Field[PreviousDocument](_.categoryCode, _.id, _.typeCode, _.lineNumeric))

  val additionalDocumentMapping = mapping(
    "id" -> optional(text.verifying("Deferred Payment ID should be less than or equal to 7 characters", _.length <= 7)),
    "categoryCode" -> optional(text.verifying("Deferred Payment Category should be less than or equal to 1 character", _.length <= 1)),
    "typeCode" -> optional(text.verifying("Deferred Payment Type should be less than or equal to 3 characters", _.length <= 3))
  )(AdditionalDocument.apply)(AdditionalDocument.unapply)
    .verifying("You must provide a Deferred Payment ID or Deferred Payment Category or Deferred Payment Type", require1Field[AdditionalDocument](_.id, _.categoryCode, _.typeCode))

  val transportEquipmentMapping = mapping(
    "sequenceNumeric" -> ignored[Int](1),
    "id" -> optional(text.verifying("Container Identification number must be 17 characters or less", _.size <= 17))
      .verifying("Container Identification number is required", _.nonEmpty),
    "seals" -> ignored[Seq[Seal]](Seq.empty)
  )(TransportEquipment.apply)(TransportEquipment.unapply)

  val referencesMapping = mapping(
    "typeCode" -> optional(
      text
        .verifying("Declaration type must be 2 characters or less", _.length <= 2)
        .verifying("Declaration type must contains only A-Z characters", isAlpha)),
    "typerCode" -> optional(
      text
        .verifying("Additional declaration type must be a single character", _.length <= 1)
        .verifying("Additional declaration type must contains only A-Z characters", isAlpha)),
    "traderAssignedReferenceId" -> optional(
      text.verifying("Reference Number/UCR must be 35 characters or less", _.length <= 35)),
    "functionalReferenceId" -> optional(
      text.verifying("LRN must be 22 characters or less", _.length <= 22)),
    "transactionNatureCode" -> optional(
      number.verifying("Nature of transaction must be contain 2 digits or less", _.toString.length <= 2))
  )(References.apply)(References.unapply)

  val agentMapping = mapping(
    "name" -> optional(text.verifying("Name should have 70 characters or less", _.length <= 70)),
    "id" -> optional(text.verifying("EORI number should have 17 characters or less", _.length <= 17)),
    "functionCode" -> optional(
      text.verifying("Status code is not valid", s => config.Options.agentFunctionCodes.exists(_._1 == s))),
    "address" -> optional(addressMapping)
  )(Agent.apply)(Agent.unapply)
}

case class ObligationGuaranteeForm (guarantees: Seq[ObligationGuarantee] = Seq.empty)


