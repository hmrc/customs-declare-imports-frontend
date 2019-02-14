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


import ai.x.play.json.Jsonx
import forms.ObligationGuaranteeForm
import uk.gov.hmrc.wco.dec._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

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
  implicit val exportCountryFormats = Json.format[ExportCountry]
  implicit val governmentProcedureFormats = Json.format[GovernmentProcedure]
  implicit val originFormats = Json.format[Origin]
  implicit val packagingFormats = Json.format[Packaging]
  implicit val tradeTermsFormats = Json.format[TradeTerms]
  implicit val previousDocumentFormats = Json.format[PreviousDocument]
  implicit val ucrFormats = Json.format[Ucr]
  implicit val valuationAdjustmentFormats = Json.format[ValuationAdjustment]
  implicit val authorisationHolderFormats = Json.format[AuthorisationHolder]
  implicit val additionalDocumentFormats = Json.format[AdditionalDocument]
  implicit val agentsFormats = Json.format[Agent]
  implicit val currencyExchangeFormats = Json.format[CurrencyExchange]
  implicit val borderTransportMeansFormats = Json.format[BorderTransportMeans]
  implicit val transportMeansFormats = Json.format[TransportMeans]

  implicit val governmentAgencyGoodsItemFormats = Jsonx.formatCaseClass[GovernmentAgencyGoodsItem]

//  // TODO: Add bilbo baggings tests
//  implicit val governmentAgencyGoodsItemReads = new Reads[GovernmentAgencyGoodsItem] {
//
//    override def reads(json: JsValue): JsResult[GovernmentAgencyGoodsItem] =
//      (
//        (__ \ "customsValueAmount").readNullable[BigDecimal] and
//        (__ \ "sequenceNumeric").read[Int] and
//        (__ \ "statisticalValueAmount").readNullable[Amount] and
//        (__ \ "transactionNatureCode").readNullable[Int] and
//        (__ \ "additionalDocuments").read[Seq[GovernmentAgencyGoodsItemAdditionalDocument]] and
//        (__ \ "additionalInformations").read[Seq[AdditionalInformation]] and
//        (__ \ "aeoMutualRecognitionParties").read[Seq[RoleBasedParty]] and
//        (__ \ "buyer").readNullable[ImportExportParty] and
//        (__ \ "commodity").readNullable[Commodity] and
//        (__ \ "consignee").readNullable[NamedEntityWithAddress] and
//        (__ \ "consignor").readNullable[NamedEntityWithAddress] and
//        (__ \ "customsValuation").readNullable[CustomsValuation] and
//        (__ \ "destination").readNullable[Destination] and
//        (__ \ "domesticDutyTaxParties").read[Seq[RoleBasedParty]] and
//        (__ \ "exportCountry").readNullable[ExportCountry] and
//        (__ \ "governmentProcedures").read[Seq[GovernmentProcedure]] and
//        (__ \ "manufacturers").read[Seq[NamedEntityWithAddress]] and
//        (__ \ "origins").read[Seq[Origin]] and
//        (__ \ "packagings").read[Seq[Packaging]] and
//        (__ \ "previousDocuments").read[Seq[PreviousDocument]] and
//        (__ \ "refundRecipientParties").read[Seq[NamedEntityWithAddress]] and
//        (__ \ "seller").readNullable[ImportExportParty] and
//        (__ \ "ucr").readNullable[Ucr] and
//        (__ \ "valuationAdjustment").readNullable[ValuationAdjustment]
//      ) { case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x) =>
//        GovernmentAgencyGoodsItem.apply(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,v,w,x)
//      }
//  }
//
//  implicit val governmentAgencyGoodsItemWrites = new Writes[GovernmentAgencyGoodsItem] {
//    override def writes(o: GovernmentAgencyGoodsItem): JsValue =
//      Json.obj(
//        "customsValueAmount" -> Json.toJson(o.customsValueAmount),
//        "sequenceNumeric" -> Json.toJson(o.sequenceNumeric),
//        "statisticalValueAmount" -> Json.toJson(o.statisticalValueAmount),
//        "transactionNatureCode" -> Json.toJson(o.transactionNatureCode),
//        "additionalDocuments" -> Json.toJson(o.additionalDocuments),
//        "additionalInformations" -> Json.toJson(o.additionalInformations),
//        "aeoMutualRecognitionParties" -> Json.toJson(o.aeoMutualRecognitionParties),
//        "buyer" -> Json.toJson(o.buyer),
//        "commodity" -> Json.toJson(o.commodity),
//        "consignee" -> Json.toJson(o.consignee),
//        "consignor" -> Json.toJson(o.consignor),
//        "customsValuation" -> Json.toJson(o.customsValuation),
//        "destination" -> Json.toJson(o.destination),
//        "domesticDutyTaxParties" -> Json.toJson(o.domesticDutyTaxParties),
//        "exportCountry" -> Json.toJson(o.exportCountry),
//        "governmentProcedures" -> Json.toJson(o.governmentProcedures),
//        "manufacturers" -> Json.toJson(o.manufacturers),
//        "origins" -> Json.toJson(o.origins),
//        "packagings" -> Json.toJson(o.packagings),
//        "previousDocuments" -> Json.toJson(o.previousDocuments),
//        "refundRecipientParties" -> Json.toJson(o.refundRecipientParties),
//        "seller" -> Json.toJson(o.seller),
//        "ucr" -> Json.toJson(o.ucr),
//        "valuationAdjustment" -> Json.toJson(o.valuationAdjustment)
//      )
//  }
}

trait ObligationGuaranteeFormats {

  implicit val officeFormats = Json.format[Office]
  implicit val guaranteeFormats = Json.format[ObligationGuarantee]
  implicit val guaranteeFormFormats = Json.format[ObligationGuaranteeForm]
}