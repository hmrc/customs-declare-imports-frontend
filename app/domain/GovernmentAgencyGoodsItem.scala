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


import forms.ObligationGuaranteeForm
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

object DeclarationFormats extends ObligationGuaranteeFormats {

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
  implicit val tradeTermsFormats = Json.format[TradeTerms]
  implicit val previousDocumentFormats = Json.format[PreviousDocument]
  implicit val ucrFormats = Json.format[Ucr]
  implicit val valuationAdjustmentFormats = Json.format[ValuationAdjustment]
  implicit val goodsItemValueFormats = Json.format[GoodsItemValueInformation]

  implicit val governmentAgencyGoodsItemFormats = Json.format[GovernmentAgencyGoodsItem]

  implicit val authorisationHolderFormats = Json.format[AuthorisationHolder]

  implicit val additionalDocumentFormats = Json.format[AdditionalDocument]

  implicit val agentsFormats = Json.format[Agent]

  implicit val currencyExchangeFormats = Json.format[CurrencyExchange]
  implicit val borderTransportMeansFormats = Json.format[BorderTransportMeans]
  implicit val transportMeansFormats = Json.format[TransportMeans]

}

trait ObligationGuaranteeFormats {
  implicit val officeFormats = Json.format[Office]
  implicit val guaranteeFormats = Json.format[ObligationGuarantee]
  implicit val guaranteeFormFormats = Json.format[ObligationGuaranteeForm]

}