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

package domain


import play.api.data.Forms.{mapping, optional, text, _}
import play.api.libs.json.Json
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItemAdditionalDocumentSubmitter, GovernmentProcedure, NamedEntityWithAddress, _}


case class GoodsItemValueInformation(
  customsValueAmount: Option[BigDecimal] = None, // scale 16 precision 3

  sequenceNumeric: Int, // unsigned max 99999

  statisticalValueAmount: Option[Amount] = None,

  transactionNatureCode: Option[Int] = None, // unsigned max 99

  destination: Option[Destination] = None,

  ucr: Option[Ucr] = None,

  exportCountry: Option[ExportCountry] = None,

  valuationAdjustment: Option[ValuationAdjustment] = None
)

case class GovernmentAgencyGoodsItem(
  goodsItemValue: Option[GoodsItemValueInformation] = None,

  additionalDocuments: Seq[GovernmentAgencyGoodsItemAdditionalDocument] = Seq.empty,

  additionalInformations: Seq[AdditionalInformation] = Seq.empty,

  aeoMutualRecognitionParties: Seq[RoleBasedParty] = Seq.empty,

  domesticDutyTaxParties: Seq[RoleBasedParty] = Seq.empty,

  governmentProcedures: Seq[GovernmentProcedure] = Seq.empty,

  manufacturers: Seq[NamedEntityWithAddress] = Seq.empty,

  origins: Seq[Origin] = Seq.empty,

  packagings: Seq[Packaging] = Seq.empty,

  previousDocuments: Seq[PreviousDocument] = Seq.empty,

  refundRecipientParties: Seq[NamedEntityWithAddress] = Seq.empty,

  buyer: Option[ImportExportParty] = None,

  commodity: Option[Commodity] = None,

  consignee: Option[NamedEntityWithAddress] = None,

  consignor: Option[NamedEntityWithAddress] = None,

  customsValuation: Option[CustomsValuation] = None,

  seller: Option[ImportExportParty] = None)

object GovernmentAgencyGoodsItem {

  implicit val measureFormats = Json.format[Measure]
  implicit val amountFormats = Json.format[Amount]
  implicit val paymentFormats = Json.format[Payment]
  implicit val dateTimeStringFormats = Json.format[DateTimeString]
  implicit val dateTimeElementFormats = Json.format[DateTimeElement]
  implicit val pointerFormats = Json.format[Pointer]
  implicit val governmentAgencyGoodsItemAdditionalDocumentSubmitterFormats = Json.format[GovernmentAgencyGoodsItemAdditionalDocumentSubmitter]
  implicit val writeOffFormats = Json.format[WriteOff]
  implicit val governmentAgencyGoodsItemAdditionalDocumentFormats = Json.format[GovernmentAgencyGoodsItemAdditionalDocument]
  implicit val additionalInformationFormats = Json.format[AdditionalInformation]
  implicit val roleBasedPartyFormats = Json.format[RoleBasedParty]
  implicit val addressFormats = Json.format[Address]
  implicit val namedEntityWithAddressFormats = Json.format[NamedEntityWithAddress]
  implicit val contactFormats = Json.format[Contact]
  implicit val communicationFormats = Json.format[Communication]
  implicit val importExportPartyFormats = Json.format[ImportExportParty]
  implicit val classificationFormats = Json.format[Classification]
  implicit val dangerousFormats = Json.format[DangerousGoods]
  implicit val dutyTaxFeeFormats = Json.format[DutyTaxFee]
  implicit val goodsMeasureFormats = Json.format[GoodsMeasure]
  implicit val invoiceLineFormats = Json.format[InvoiceLine]
  implicit val sealFormats = Json.format[Seal]
  implicit val transportEquipmentFormats = Json.format[TransportEquipment]
  implicit val commodityFormats = Json.format[Commodity]
  implicit val chargeDeductionFormats = Json.format[ChargeDeduction]
  implicit val customsValuationFormats = Json.format[CustomsValuation]
  implicit val destinationFormats = Json.format[Destination]
  implicit val ExportCountryFormats = Json.format[ExportCountry]

  implicit val governmentProcedureFormats = Json.format[GovernmentProcedure]
  implicit val originFormats = Json.format[Origin]
  implicit val packagingFormats = Json.format[Packaging]
  implicit val previousDocumentFormats = Json.format[PreviousDocument]
  implicit val ucrFormats = Json.format[Ucr]
  implicit val valuationAdjustmentFormats = Json.format[ValuationAdjustment]
  implicit val goodsItemValueFormats = Json.format[GoodsItemValueInformation]
  implicit val goodsItemValueReadsFormats = Json.reads[GoodsItemValueInformation]

  implicit val governmentAgencyGoodsItemFormats = Json.format[GovernmentAgencyGoodsItem]

  val govAgencyGoodsItemAddDocumentSubmitterMapping = mapping(
    "name" -> optional(text),
    "roleCode" -> optional(text.verifying("roleCode is only 3 characters", _.length <= 3))
  )(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter.apply)(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter.unapply)


  val amountMapping = mapping("currencyId" -> optional(text.verifying("currencyId is only 3 characters", _.length <= 3)),
    "value" -> optional(bigDecimal.verifying("amount must not be negative", a => a < 0)))(Amount.apply)(Amount.unapply)

  val measureMapping = mapping("unitCode" -> optional(text.verifying("unitCode is only 5 characters", _.length <= 5)),
    "value" -> optional(bigDecimal.verifying("amount must not be negative", a => a > 0)))(Measure.apply)(Measure.unapply)

  val writeOffMapping = mapping("quantity" -> optional(measureMapping), "amount" -> optional(amountMapping))(WriteOff.apply)(WriteOff.unapply)

  val govtAgencyGoodsItemAddDocMapping = mapping(
    "categoryCode" -> optional(text.verifying("category code is only 3 characters", _.length <= 3)),
    "effectiveDateTime" -> ignored[Option[DateTimeElement]](None),
    "id" -> optional(text),
    "name" -> optional(text),
    "typeCode" -> optional(text.verifying("typeCode is only 3 characters", _.length <= 3)),
    "lpcoExemptionCode" -> optional(text.verifying("lpcoExemptionCode is only 3 characters", _.length <= 3)),
    "submitter" -> optional(govAgencyGoodsItemAddDocumentSubmitterMapping),
    "writeOff" -> optional(writeOffMapping)
  )(GovernmentAgencyGoodsItemAdditionalDocument.apply)(GovernmentAgencyGoodsItemAdditionalDocument.unapply)

  val additionalInformationMapping = mapping(
    "statementCode" -> optional(text.verifying("statement Code should be less than or equal to 17 characters", _.length <= 17)),
    "statementDescription" -> optional(text.verifying("statement Description should be less than or equal to 512 characters", _.length <= 512)),
    "statementTypeCode" -> optional(text.verifying("statement Type Code should be less than or equal to 3 characters", _.length <= 3)),
    "pointers" -> ignored[Seq[Pointer]](Seq.empty)
  )(AdditionalInformation.apply)(AdditionalInformation.unapply)

  val destinationMapping = mapping("countryCode" -> optional(text.verifying("country code is only 3 characters", _.length <= 3)),
    "regionId" -> optional(text.verifying("regionId code is only 9 characters", _.length <= 9)))(Destination.apply)(Destination.unapply)

  val ucrMapping = mapping("id" -> optional(text.verifying("id should be less than or equal to 35 characters", _.length <= 35)),
    "traderAssignedReferenceId" -> optional(text.verifying("traderAssignedReferenceId should be less than or equal to 35 characters", _.length <= 35)))(Ucr.apply)(Ucr.unapply)

  val exportCountryMapping = mapping("id" -> text.verifying("export Country code should be less than or equal to 2 characters",
    _.length <= 2))(ExportCountry.apply)(ExportCountry.unapply)

  val valuationAdjustmentMapping = mapping("additionCode" -> optional(
    text.verifying("valuationAdjustment should be less than or equal to 4 characters",
      _.length <= 2)))(ValuationAdjustment.apply)(ValuationAdjustment.unapply)

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
    "cityName" -> optional(text.verifying("id should be less than or equal to 35 characters", _.length <= 35)), // max length 35
    "countryCode" -> optional(text.verifying("country Code should be less 2 characters", _.length <= 2)), // 2 chars [a-zA-Z] ISO 3166-1 2-alpha
    "countrySubDivisionCode" -> optional(text.verifying("countrySubDivisionCode should be less than or equal to 9 characters", _.length <= 9)), // max 9 chars
    "countrySubDivisionName" -> optional(text.verifying("countrySubDivisionName should be less than or equal to 35 characters", _.length <= 35)), // max 35 chars
    "line" -> optional(text.verifying("line should be less than or equal to 70 characters", _.length <= 70)), //:max 70 chars
    "postcodeId" -> optional(text.verifying("postcode should be less than or equal to 9 characters", _.length <= 9)) // max 9 chars
  )(Address.apply)(Address.unapply)

  val namedEntityWithAddressMapping = mapping(
    "name" -> optional(text.verifying("name should be less than or equal to 70 characters", _.length <= 70)), //: Option[String] = None, // max 70 chars
    "id" -> optional(text.verifying("id  should be less than or equal to 17 characters", _.length <= 17)), // max 17 chars
    "address" -> optional(addressMapping)
  )(NamedEntityWithAddress.apply)(NamedEntityWithAddress.unapply)

  val roleBasedPartyMapping = mapping(
    "id" -> optional(text.verifying("role based party id  should be less than or equal to 17 characters", _.length <= 17)), // max 17 chars
    "roleCode" -> optional(text.verifying("role Code  should be less than or equal to 3 characters", _.length <= 3)) // max 3 chars
  )(RoleBasedParty.apply)(RoleBasedParty.unapply)

  val governmentProcedureMapping = mapping(
    "currentCode" -> optional(text.verifying("current Code  should be less than or equal to 7 characters", _.length <= 7)), // max 7 chars
    "previousCode" -> optional(text.verifying("previous Code  should be less than or equal to 7 characters", _.length <= 7)) // max 7 chars
  )(GovernmentProcedure.apply)(GovernmentProcedure.unapply)

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

  val previousDocumentMapping = mapping(
    "categoryCode" -> optional(text.verifying("category Code  should be less than 4 characters", _.length <= 3)), //: Option[String] = None, // max 3 chars
    "id" -> optional(text.verifying("previous documents Id should be less than or equal to 70 characters", _.length <= 70)), //: Option[String] = None, // max 70 chars
    "typeCode" -> optional(text.verifying("type Code  should be 3 characters", _.length <= 3)), //: Option[String] = None, // max 3 chars
    "lineNumeric" -> optional(number(0, 99999)) //: Option[Int] = None, // max 99999999
  )(PreviousDocument.apply)(PreviousDocument.unapply)

  val contactMapping = mapping("name" -> optional(text.verifying("name should be less than or equal to 70 characters", _.length <= 70)) //: Option[String] = None, // max 70 chars
  )(Contact.apply)(Contact.unapply)

  val communicationMapping = mapping(
    "id" -> optional(text.verifying("communication Id should be less than or equal to 70 characters", _.length <= 50)), //: Option[String] = None, // max 50 chars
    "typeCode" -> optional(text.verifying("type Code  should be 3 characters", _.length <= 3)) //: Option[String] = None, // max 3 chars
  )(Communication.apply)(Communication.unapply)

  val importExportPartyMapping = mapping(
    "name" -> optional(text.verifying(" Import Export name should be less than or equal to 70 characters", _.length <= 70)), //: Option[String] = None, // max 70 chars
    "id" -> optional(text.verifying(" Import Export party Id should be less than or equal to 70 characters", _.length <= 17)), //: Option[String] = None, // max 17 chars
    "address" -> optional(addressMapping),
    "contacts" -> seq(contactMapping),
    "communications" -> seq(communicationMapping))(ImportExportParty.apply)(ImportExportParty.unapply)

  val chargeDeductionMapping = mapping(
    "chargesTypeCode" -> optional(text.verifying(" Charges code should be less than or equal to 3 characters", _.length <= 3)),
  "otherChargeDeductionAmount" ->  optional(amountMapping) // Option[Amount] = None
  )(ChargeDeduction.apply)(ChargeDeduction.unapply)


  val customsValuationMapping = mapping( "methodCode" ->  optional(text.verifying(" Charges code should be less than or equal to 3 characters", _.length <= 3)),// max 3 chars; not valid outside GovernmentAgencyGoodsItem
    "freightChargeAmount" -> optional(bigDecimal),//default(bigDecimal, None),
    "chargeDeductions" -> seq(chargeDeductionMapping))(CustomsValuation.apply)(CustomsValuation.unapply)


}


