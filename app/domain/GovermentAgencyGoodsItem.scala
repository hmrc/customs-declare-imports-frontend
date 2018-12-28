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

package domain


import play.api.data.Forms
import play.api.data.Forms.{mapping, optional, text}
import play.api.libs.json.Json
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItemAdditionalDocumentSubmitter, GovernmentProcedure, _}


case class GoodsItemValueInformation(
  customsValueAmount: Option[BigDecimal] = None, // scale 16 precision 3

  sequenceNumeric: Int, // unsigned max 99999

  statisticalValueAmount: Option[Amount] = None,

  transactionNatureCode: Option[Int] = None, // unsigned max 99
  buyer: Option[ImportExportParty] = None,

  commodity: Option[Commodity] = None,

  consignee: Option[NamedEntityWithAddress] = None,

  consignor: Option[NamedEntityWithAddress] = None,

  customsValuation: Option[CustomsValuation] = None,

  destination: Option[Destination] = None

)

case class GovernmentAgencyGoodsItem(
  goodsItemValue: Option[GoodsItemValueInformation] = None,

  additionalDocuments: Seq[GovernmentAgencyGoodsItemAdditionalDocument] = Seq.empty,

  additionalInformations: Seq[AdditionalInformation] = Seq.empty,

  aeoMutualRecognitionParties: Seq[RoleBasedParty] = Seq.empty,


  domesticDutyTaxParties: Seq[RoleBasedParty] = Seq.empty,

  exportCountry: Option[ExportCountry] = None,

  governmentProcedures: Seq[GovernmentProcedure] = Seq.empty,

  manufacturers: Seq[NamedEntityWithAddress] = Seq.empty,

  origins: Seq[Origin] = Seq.empty,

  packagings: Seq[Packaging] = Seq.empty,

  previousDocuments: Seq[PreviousDocument] = Seq.empty,

  refundRecipientParties: Seq[NamedEntityWithAddress] = Seq.empty,

  seller: Option[ImportExportParty] = None,

  ucr: Option[Ucr] = None,

  valuationAdjustment: Option[ValuationAdjustment] = None)

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
  implicit val goodsItemValue = Json.format[GoodsItemValueInformation]

  implicit val governmentAgencyGoodsItemFormats = Json.format[GovernmentAgencyGoodsItem]

  val govAgencyGoodsItemAddDocumentSubmitterMapping = mapping(
    "name" -> optional(text),
    "roleCode" -> optional(text.verifying("roleCode is only 3 characters", _.length <= 3))
  )(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter.apply)(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter.unapply)


  val amountMapping = mapping("currencyId" -> optional(text.verifying("currencyId is only 3 characters", _.length <= 3)),
    "value" -> optional(Forms.bigDecimal.verifying("amount must be negative", a => a < 0)))(Amount.apply)(Amount.unapply)

  val measureMapping = mapping("unitCode" -> optional(text.verifying("unitCode is only 5 characters", _.length <= 5)),
    "value" -> optional(Forms.bigDecimal.verifying("amount must be negative", a => a < 0)))(Measure.apply)(Measure.unapply)

  val writeOffMapping = mapping("quantity" -> optional(measureMapping), "amount" -> optional(amountMapping))(WriteOff.apply)(WriteOff.unapply)

  val govtAgencyGoodsItemAddDocMapping = mapping(
    "categoryCode" -> optional(text.verifying("category code is only 3 characters", _.length <= 3)),
    "effectiveDateTime" -> Forms.ignored[Option[DateTimeElement]](None),
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
    "pointers" -> Forms.ignored[Seq[Pointer]](Seq.empty)
  )(AdditionalInformation.apply)(AdditionalInformation.unapply)
}


