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

import domain.{InvoiceAndCurrency, References, WarehouseAndCustoms}
import config.Options
import domain._
import domain.Cancel._
import models.ChangeReasonCode
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.data.Forms._
import play.api.data.Mapping
import uk.gov.hmrc.wco.dec._

import scala.util.Try
import scala.util.control.Exception.allCatch

object DeclarationFormMapping {

  def require1Field[T](fs: (T => Option[_])*): T => Boolean =
    t => fs.exists(f => f(t).nonEmpty)

  def requireAllDependantFields[T](primary: T => Option[_])(fs: (T => Option[_])*): T => Boolean =
    t => primary(t).fold(true)(_ => fs.forall(f => f(t).nonEmpty))

  def isInList(tuples: Seq[(String, String)]): String => Boolean =
    s => tuples.exists(_._1 == s)

  val isAlpha: String => Boolean = _.matches("^[A-Za-z]*$")
  val isInt: String => Boolean = _.matches("^[0-9-]*$")

  val govAgencyGoodsItemAddDocumentSubmitterMapping = mapping(
    "name" -> optional(text.verifying("Issuing Authority must be less than 70 characters", _.length <= 70)),
    "roleCode" -> optional(text.verifying("roleCode is only 3 characters", _.length <= 3))
  )(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter.apply)(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter.unapply)

  val amountMapping: Mapping[Amount] = amountMapping()
  private def amountMapping(valueKey:String = "Amount", currencyKey:String = "Currency"): Mapping[Amount] = mapping(
    "currencyId" -> optional(
      text
        .verifying(s"$currencyKey is not valid", x => config.Options.currencyTypes.exists(_._1 == x))),
    "value" -> optional(
      bigDecimal
        .verifying(s"$valueKey cannot be greater than 99999999999999.99", _.precision <= 16)
        .verifying(s"$valueKey cannot have more than 2 decimal places", _.scale <= 2)
        .verifying(s"$valueKey must not be negative", _ >= 0))
  )(Amount.apply)(Amount.unapply)
    .verifying(s"$valueKey is required when $currencyKey is provided", requireAllDependantFields[Amount](_.currencyId)(_.value))
    .verifying(s"$currencyKey is required when $valueKey is provided", requireAllDependantFields[Amount](_.value)(_.currencyId))

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

  val tradeTermsMapping = mapping(
    "conditionCode" -> optional(
      text.verifying("Condition Code is not a valid condition code", x => config.Options.incoTermCodes.exists(_._1 == x))),
    "countryRelationshipCode" -> ignored[Option[String]](None),
    "description" -> ignored[Option[String]](None),
    "locationId" -> optional(
      text.verifying("Location ID should be less than or equal to 17 characters", _.length <= 17)),
    "locationName" -> optional(
      text.verifying("Location Name should be less than or equal to 37 characters", _.length <= 37)
    ))(TradeTerms.apply)(TradeTerms.unapply)

  val measureMapping: Mapping[Measure] = measureMapping("Quantity")

  private def measureMapping(valueKey: String): Mapping[Measure] = mapping(
    "unitCode" -> optional(text.verifying("Measurement Unit & Qualifier cannot be more than 5 characters", _.length <= 5)),
    "value" ->
      optional(bigDecimal.verifying(s"$valueKey cannot be greater than 9999999999.999999", _.precision <= 16)
      .verifying(s"$valueKey cannot have more than 6 decimal places", _.scale <= 6)
      .verifying(s"$valueKey must not be negative", _ >= 0)))(Measure.apply)(Measure.unapply)

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

  val destinationMapping = mapping(
    "countryCode" -> optional(text.verifying("countryCode is not a valid countryCode", isInList(Options.countryOptions))),
    "regionId" -> ignored[Option[String]](None)
  )(Destination.apply)(Destination.unapply)

  val ucrMapping = mapping("id" -> optional(text.verifying("id should be less than or equal to 35 characters", _.length <= 35)),
    "traderAssignedReferenceId" -> optional(text.verifying("traderAssignedReferenceId should be less than or equal to 35 characters", _.length <= 35)))(Ucr.apply)(Ucr.unapply)

  val exportCountryMapping = mapping(
    "id" -> text.verifying("ID is not a valid ID", isInList(Options.countryTypes))
  )(ExportCountry.apply)(ExportCountry.unapply)

  val loadingLocationMapping = mapping(
    "name" -> ignored[Option[String]](None),
    "id" -> optional(text.verifying("id should be less than or equal to 17 characters", _.length <= 17))
  )(LoadingLocation.apply)(LoadingLocation.unapply)

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
    "valuationAdjustment" -> optional(valuationAdjustmentMapping)) {
      (a, b, c, d, e, f, g, h) =>
        GovernmentAgencyGoodsItem(
          customsValueAmount = a,
          sequenceNumeric = b,
          statisticalValueAmount = c,
          transactionNatureCode = d,
          destination = e,
          ucr = f,
          exportCountry = g,
          valuationAdjustment = h)
    } { g =>
      Some((g.customsValueAmount, g.sequenceNumeric, g.statisticalValueAmount, g.transactionNatureCode,
        g.destination, g.ucr, g.exportCountry, g.valuationAdjustment))
    }


  val addressMapping = mapping(
    "cityName" -> optional(text.verifying("City name should be 35 characters or less", _.length <= 35)),
    "countryCode" -> optional(text.verifying("Country code is invalid", code => config.Options.countryOptions.exists(_._1 == code))),
    "countrySubDivisionCode" -> optional(text.verifying("Country sub division code should be 9 characters or less", _.length <= 9)),
    "countrySubDivisionName" -> optional(text.verifying("Country sub division name should be 35 characters or less", _.length <= 35)),
    "line" -> optional(text.verifying("Line should be 70 characters or less", _.length <= 70)),
    "postcodeId" -> optional(text.verifying("Postcode should be 9 characters or less", _.length <= 9))
  )(Address.apply)(Address.unapply)

  val namedEntityWithAddressMapping = mapping(
    "name" -> optional(text.verifying("Name should be less than or equal to 70 characters", _.length <= 70)), //: Option[String] = None, // max 70 chars
    "id" -> optional(text.verifying("ID  should be less than or equal to 17 characters", _.length <= 17)), // max 17 chars
    "address" -> optional(addressMapping)
  )(NamedEntityWithAddress.apply)(NamedEntityWithAddress.unapply)

  val roleBasedPartyMapping = mapping(
    "id" -> optional(text.verifying("Identifier should be less than or equal to 17 characters", _.length <= 17)), // max 17 chars
    "roleCode" -> optional(text.verifying("Role code should be 3 characters and must contain only A-Z characters", (code => code.length <= 3 && isAlpha(code)))) // max 3 chars
  )(RoleBasedParty.apply)(RoleBasedParty.unapply)
    .verifying("You must provide an Identifier or Role code", require1Field[RoleBasedParty](_.id, _.roleCode))

  val governmentProcedureMapping = mapping(
    "currentCode" -> optional(text.verifying("Current code should be less than or equal to 2 characters", _.length <= 2)), // max 7 chars
    "previousCode" -> optional(text.verifying("Previous code  should be less than or equal to 2 characters", _.length <= 2)) // max 7 chars
  )(GovernmentProcedure.apply)(GovernmentProcedure.unapply)
    .verifying("To add procedure codes you must provide Current Code or Previous code", require1Field[GovernmentProcedure](_.currentCode, _.previousCode))

  val originMapping = mapping(
    "countryCode" -> text.verifying("Country of origin is invalid", code => config.Options.countryOptions.exists(_._1 == code)), // max 4 chars //expects ISO-3166-1 alpha2 code
    "typeCode" -> optional(number.verifying("Origin type must be a digit and should be between 1-9", (i => i > 0 &&  i <= 9))) // max 3 chars
  )((countryCode:String, typeCode:Option[Int]) => Origin(Some(countryCode), None, typeCode.map(_.toString)))(Origin.unapply(_).map(o=> (o._1.getOrElse(""), o._3.map(_.toInt))))
  val packagingMapping =
    mapping("sequenceNumeric" -> ignored[Option[Int]](None), //: Option[Int] = None, // unsigned max 99999
      "marksNumbersId" -> optional(text.verifying("Shipping Marks should be less than or equal to 512 characters", _.length <= 512)), //: Option[String] = None, // max 512 chars
      "quantity" -> optional(number.verifying("Number of Packages must be greater than 0 and less than 99999999", (q => q > 0 && q.toString.length <= 8))), //: Option[Int] = None, // max 99999999
      "typeCode" -> optional(text.verifying("Type of Packages should be 2 characters", _.length == 2)), //: Option[String] = None, // max 2 chars
      "packingMaterialDescription" -> ignored[Option[String]](None), // Option[String] = None, // max 256 chars
      "lengthMeasure" -> ignored[Option[Long]](None), //: Option[Long] = None, // unsigned int max 999999999999999
      "widthMeasure" -> ignored[Option[Long]](None), //: Option[Long] = None, // unsigned int max 999999999999999
      "heightMeasure" -> ignored[Option[Long]](None), //: Option[Long] = None, // unsigned int max 999999999999999
      "volumeMeasure" -> ignored[Option[Measure]](None))(Packaging.apply)(Packaging.unapply)
      .verifying("You must provide Shipping Marks, Number of Packages or Type for package to be added", require1Field[Packaging](_.marksNumbersId, _.quantity, _.typeCode))


  val contactMapping = mapping(
    "name" -> optional(text.verifying("name should be less than or equal to 70 characters", _.length <= 70)) //: Option[String] = None, // max 70 chars
  )(Contact.apply)(Contact.unapply)

  val communicationMapping = mapping(
    "id" -> optional(text.verifying("Phone number should be 50 characters or less", _.length <= 50)), //: Option[String] = None, // max 50 chars
    "typeCode" -> optional(text.verifying("Type code should be 3 characters or less", _.length <= 3)) //: Option[String] = None, // max 3 chars
  )(Communication.apply)(Communication.unapply)

  val importExportPartyMapping = mapping(
    "name" -> optional(text.verifying("Name should have 70 characters or less", _.length <= 70)),
    "id" -> optional(text.verifying("EORI number should have 17 characters or less", _.length <= 17)),
    "address" -> optional(addressMapping),
    "contacts" -> seq(contactMapping),
    "communications" -> seq(communicationMapping)
  )(ImportExportParty.apply)(ImportExportParty.unapply)

  val chargeDeductionMapping = mapping(
    "chargesTypeCode" -> optional(text.verifying("Charges code should be less than or equal to 2 characters", _.length <= 2)
                                            .verifying("Charges code must contain only A-Z characters", isAlpha)),
    "otherChargeDeductionAmount" -> optional(amountMapping("Value","Currency")) // Option[Amount] = None
  )(ChargeDeduction.apply)(ChargeDeduction.unapply)
    .verifying("Charges code, currency id or amount are required", require1Field[ChargeDeduction](_.chargesTypeCode, _.otherChargeDeductionAmount))

  val goodsChargeDeductionMapping = mapping(
    "chargesTypeCode" -> optional(text.verifying("Type should be 2 characters", _.length == 2)
                                            .verifying("Type must contain only A-Z characters", isAlpha)),
    "otherChargeDeductionAmount" -> optional(amountMapping("Value","Currency")) // Option[Amount] = None
  )(ChargeDeduction.apply)(ChargeDeduction.unapply)
    .verifying("Type or Currency and Value are required", require1Field[ChargeDeduction](_.chargesTypeCode, _.otherChargeDeductionAmount))


  val customsValuationMapping = mapping("methodCode" -> optional(text.verifying(" Charges code should be less than or equal to 3 characters", _.length <= 3)), // max 3 chars; not valid outside GovernmentAgencyGoodsItem
    "freightChargeAmount" -> optional(bigDecimal), // default(bigDecimal, None),
    "chargeDeductions" -> seq(goodsChargeDeductionMapping))(CustomsValuation.apply)(CustomsValuation.unapply)


  val officeMapping = mapping("id" -> optional(text
    .verifying("Office id should be less than or equal to 8 characters", _.length <= 8))
  )(Office.apply)(Office.unapply)
  
  val warehouseMapping = mapping(
    "id" -> optional(text.verifying("ID should be less than or equal to 35 characters", _.length <= 35)),
    "typeCode" -> text.verifying("Type Code is not a valid type code",
      x => config.Options.customsWareHouseTypes.exists(_._2 == x))
  )(Warehouse.apply)(Warehouse.unapply)

  val warehouseAndCustomsMapping = mapping(
    "warehouse" -> optional(warehouseMapping),
    "presentationOffice" -> optional(officeMapping),
    "supervisingOffice" -> optional(officeMapping)
  )(WarehouseAndCustoms.apply)(WarehouseAndCustoms.unapply)

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
    "functionalReferenceId" ->
      text.verifying("LRN must be 22 characters or less", _.length <= 22),
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

  val invoiceLineMapping = mapping(
    "itemChargeAmount" -> optional(amountMapping)
  )(InvoiceLine.apply)(InvoiceLine.unapply)

  val goodsMeasureMapping = mapping(
    "grossMassMeasure" -> optional(measureMapping),
    "netWeightMeasure" -> optional(measureMapping),
    "tariffQuantity" -> optional(measureMapping)
  )(GoodsMeasure.apply)(GoodsMeasure.unapply)

  val summaryOfGoodsMapping = mapping(
    "totalPackageQuantity" -> optional(
      number
        .verifying("Total packages cannot be greater than 99,999,999", _ <= 99999999)
        .verifying("Total packages cannot be less than 0", _ >= 0)
    ).verifying("Total packages is required", _.nonEmpty),
    "totalGrossMassMeasure" -> optional(measureMapping("Gross mass"))
  )(SummaryOfGoods.apply)(SummaryOfGoods.unapply)

  val borderTransportMeansMapping = mapping(
    "name" -> ignored[Option[String]](None),
    "id" -> ignored[Option[String]](None),
    "identificationTypeCode" -> ignored[Option[String]](None),
    "typeCode" -> ignored[Option[String]](None),
    "registrationNationalityCode" ->
      optional(text
        .verifying("Nationality of active means of transport is invalid", isInList(Options.countryOptions))),
    "modeCode" ->
      optional(number
        .verifying("Mode of transport at border is invalid", i => isInList(Options.transportModeTypes)(i.toString)))
  )(BorderTransportMeans.apply)(BorderTransportMeans.unapply)

  val transportMeansMapping = mapping(
    "name" -> ignored[Option[String]](None),
    "id" ->
      optional(text.verifying("ID No. cannot be longer than 35 characters", _.length <= 35)),
    "identificationTypeCode" ->
      optional(text
        .verifying("Type of identification is invalid", isInList(Options.transportMeansIdentificationTypes))),
    "typeCode" -> ignored[Option[String]](None),
    "modeCode" ->
      optional(number
        .verifying("Inland mode of transport is invalid", i => isInList(Options.transportModeTypes)(i.toString)))
  )(TransportMeans.apply)(TransportMeans.unapply)

  val transportMapping = mapping(
    "containerCode" ->
      optional(number.verifying("Container must be a single digit", _.toString.length <= 1)),
    "borderTransportMeans" -> optional(borderTransportMeansMapping),
    "arrivalTransportMeans" -> optional(transportMeansMapping)
  )(Transport.apply)(Transport.unapply)

  val goodsLocationAddressMapping = mapping(
    "typeCode" -> optional(text.verifying("typeCode is not a valid typeCode", isInList(Options.goodsLocationTypeCode))),
    "cityName" -> optional(text.verifying("cityName should be less than or equal to 35 characters", _.length <= 35)),
    "countryCode" -> optional(text.verifying("countryCode is not a valid countryCode", isInList(Options.countryOptions))),
    "line" -> optional(text.verifying("Line should be less than or equal to 70 characters", _.length <= 70)),
    "postcodeId" -> optional(text.verifying("postcodeId should be less than or equal to 9 characters", _.length <= 9))
  )(GoodsLocationAddress.apply)(GoodsLocationAddress.unapply)

  val goodsLocationMapping = mapping(
    "name" -> optional(text.verifying("Name should be less than or equal to 35 characters", _.length <= 35)),
    "id" -> text.verifying("ID should be less than or equal to 3 characters", _.length <= 3),
    "typeCode" -> optional(text.verifying("typeCode is not a valid typeCode", isInList(Options.goodsLocationTypeCode))),
    "address" -> optional(goodsLocationAddressMapping)
  )(GoodsLocation.apply)(GoodsLocation.unapply)

  val locationOfGoodsMapping = mapping(
    "goodsLocation" -> optional(goodsLocationMapping),
    "destination" -> optional(destinationMapping),
    "exportCountry" -> optional(exportCountryMapping),
    "loadingLocation" -> optional(loadingLocationMapping)
  )(LocationOfGoods.apply)(LocationOfGoods.unapply)

  val paymentMapping  = mapping(
    "methodCode" -> optional(text.verifying("Method of Payment must be a single character or less", _.length < 2)
                        .verifying("Method of Payment must contains only A-Z characters", isAlpha)), // alpha max 1 in tariff
    "taxAssessedAmount" -> optional(amountMapping("Total","Total - Currency")),
    "paymentAmount" -> optional(amountMapping("Payable Tax Amount", "Currency"))
  )(Payment.apply)(Payment.unapply)

  val dutyTaxFeeMapping = mapping (
    "adValoremTaxBaseAmount" -> ignored[Option[Amount]](None),
    "deductAmount" -> ignored[Option[Amount]](None),
    "dutyRegimeCode" -> optional(text.verifying("Duty regime code should be less than or equal to 3 characters", _.length <= 3)),
    "specificTaxBaseQuantity" -> optional(measureMapping),
    "taxRateNumeric" -> optional(bigDecimal
      .verifying("Tax Rate cannot be greater than 99999999999999.999", _.precision <= 17)
      .verifying("Tax Rate cannot have more than 3 decimal places", _.scale <= 3)
      .verifying("Tax Rate must not be negative", _ >= 0)),
    "typeCode" -> optional(text.verifying("Tax Type should be 3 characters", _.length == 3)),
    "quotaOrderId" -> optional(text.verifying("Quota order number should be 6 characters", _.length == 6)),
    "payment" -> optional(paymentMapping)
  )(DutyTaxFee.apply)(DutyTaxFee.unapply)
    .verifying("One of Tax type, Tax base, Tax rate, Payable tax amount Total and Method of payment is required to add commodity duty tax", require1Field[DutyTaxFee](_.specificTaxBaseQuantity, _.taxRateNumeric, _.payment, _.typeCode))

  val cancelMapping = mapping(
    "changeReasonCode" -> of[ChangeReasonCode],
    "description" -> nonEmptyText.verifying("Description cannot be longer than 512 characters", _.length <= 512)
  )(Cancel.apply)(Cancel.unapply)

  val classificationMapping = mapping(
    "id" -> optional(text.verifying("Id must be less than 5 characters", _.length <= 4)),
    "identificationTypeCode" -> optional(text)
  )((id,typeCode) => Classification(id,None,typeCode,None))(Classification.unapply(_).map(c => (c._1,c._3)))
    .verifying("Id and Type is required to add classification", require1Field[Classification](_.id, _.identificationTypeCode))
    .verifying("Type is required when Id is provided", requireAllDependantFields[Classification](_.id)(_.identificationTypeCode))
    .verifying("Id is required when Type is provided", requireAllDependantFields[Classification](_.identificationTypeCode)(_.id))

  val commodityMapping = mapping(
    "description" -> optional(text.verifying("Description should be less than equal to 512 characters", _.length <= 512)),
    "classifications" -> seq(classificationMapping),
    "dangerousGoods" -> ignored[Seq[DangerousGoods]](Seq.empty),
    "dutyTaxFees" -> seq(dutyTaxFeeMapping),
    "goodsMeasure" -> optional(goodsMeasureMapping),
    "invoiceLine" -> optional(invoiceLineMapping),
    "transportEquipments" -> seq(transportEquipmentMapping)
  )(Commodity.apply)(Commodity.unapply)
}

case class ObligationGuaranteeForm(guarantees: Seq[ObligationGuarantee] = Seq.empty)


