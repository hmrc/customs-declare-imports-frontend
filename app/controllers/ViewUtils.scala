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

import play.api.data.validation.ValidationError
import play.api.i18n.Messages

import scala.collection.mutable


object ViewUtils {

  //Declarant fields
  val declarantName = "MetaData_declaration_declarant_name"
  val declarantAddressLine = "MetaData_declaration_declarant_address_line"
  val declarantAddressCityName = "MetaData_declaration_declarant_address_cityName"
  val declarantAddressCountryCode = "MetaData_declaration_declarant_address_countryCode"
  val declarantAddressPostcode = "MetaData_declaration_declarant_address_postcodeId"
  val declarantEori = "MetaData_declaration_declarant_id"

  //Exporter fields
  val exporterName = "MetaData_declaration_exporter_name"
  val exporterAddressLine = "MetaData_declaration_exporter_address_line"
  val exporterAddressCityName = "MetaData_declaration_exporter_address_cityName"
  val exporterAddressCountryCode = "MetaData_declaration_exporter_address_countryCode"
  val exporterAddressPostcode = "MetaData_declaration_exporter_address_postcodeId"
  val exporterEori = "MetaData_declaration_exporter_id"

  //Represetative fields
  val agentName = "MetaData_declaration_agent_name"
  val agentAddressLine = "MetaData_declaration_agent_address_line"
  val agentAddressCityName = "MetaData_declaration_agent_address_cityName"
  val agentAddressCountryCode = "MetaData_declaration_agent_address_countryCode"
  val agentAddressPostcode = "MetaData_declaration_agent_address_postcodeId"
  val agentEori = "MetaData_declaration_agent_id"
  val agentFunctionCode = "MetaData_declaration_agent_functionCode"

  //Importer fields
  val importerName = "MetaData_declaration_importer_name"
  val importerAddressLine = "MetaData_declaration_importer_address_line"
  val importerAddressCityName = "MetaData_declaration_importer_address_cityName"
  val importerAddressCountryCode = "MetaData_declaration_importer_address_countryCode"
  val importerAddressPostcode = "MetaData_declaration_importer_address_postcodeId"
  val importerEori = "MetaData_declaration_importer_id"

  //Seller fields
  val sellerName = "MetaData_declaration_seller_name"
  val sellerAddressLine = "MetaData_declaration_seller_address_line"
  val sellerAddressCityName = "MetaData_declaration_seller_address_cityName"
  val sellerAddressCountryCode = "MetaData_declaration_seller_address_countryCode"
  val sellerAddressPostcode = "MetaData_declaration_seller_address_postcodeId"
  val sellerCommunicationID = "MetaData_declaration_seller_communications_id"
  val sellerEori = "MetaData_declaration_seller_id"

  //Buyer fields
  val buyerName = "MetaData_declaration_buyer_name"
  val buyerAddressLine = "MetaData_declaration_buyer_address_line"
  val buyerAddressCityName = "MetaData_declaration_buyer_address_cityName"
  val buyerAddressCountryCode = "MetaData_declaration_buyer_address_countryCode"
  val buyerAddressPostcode = "MetaData_declaration_buyer_address_postcodeId"
  val buyerCommunicationID = "MetaData_declaration_buyer_communications_id"
  val buyerEori = "MetaData_declaration_buyer_id"

  //Additional supply chain actors fields
  val aeoMutualRecognitionPartiesID = "MetaData_declaration_aeoMutualRecognitionParties_id"
  val aeoMutualRecognitionPartyRoleCode = "MetaData_declaration_aeoMutualRecognitionParties_roleCode"
  val authorisationHolderID = "MetaData_declaration_authorisationHolders_id"
  val authorisationHolderCategoryCode = "MetaData_declaration_authorisationHolders_categoryCode"

  //references screen fields
 //functionalReferenceId: Option[String] = None, // max 35 chars
  val declarantFunctionalReferenceID = "MetaData_declaration_functionalReferenceID"
  //traderAssignedReferenceId: Option[String] = None) // max 35 chars
  val referenceNumberUCR1 = "MetaData_declaration_goodsShipment_ucr_traderAssignedReferenceId"
  //TODO:governmentAgencyGoodsItems is a seq of elements needs mapping the sequence
  //traderAssignedReferenceId: Option[String] = None) // max 35 chars
  val referenceNumberUCR2 = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_ucr_traderAssignedReferenceID"
  //typeCode: Option[String] = None, // max 3 chars; MUST be "INV" in cancellation use case
  //TODO: Same xml element mapped for declarationType and additionalDeclarationType
  val declarationType = "MetaData_declaration_typeCode"
  //typeCode: Option[String] = None, // max 3 chars; MUST be "INV" in cancellation use case
  val additionalDeclarationType = "MetaData_declaration_typeCode"

  //Previous document page fields
  val previousDocumentsDocumentCategory = "MetaData_declaration_previousDocuments_categoryCode"
  val previousDocumentsDocumentTypeCode = "MetaData_declaration_previousDocuments_typeCode"
  val previousDocumentsPreviousDocumentReference = "MetaData_declaration_previousDocuments_id"
  val previousDocumentsDocumentGoodsItemIdentifier = "MetaData_declaration_previousDocuments_lineNumeric"

  //Procedure Codes fields
  val requestedProcedureCode = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_governmentProcedures_currentCode"
  val previousProcedureCode = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_governmentProcedures_previousCode"
  val additionalProcedure = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_governmentProcedures_additionalProcedure"

  //Additional fiscal references fields

  // TODO these are left as optional until the question re the "OSR" status is answered.  Possibly that by choosing a "free circulation" 
  // code earlier might make this mandatory, and therefore require conditional validation
  val additionalFiscalReferencesId = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_domesticDutyTaxParties_id"
  val additionalFiscalReferencesRoleCode = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_domesticDutyTaxParties_roleCode"

  //Identification of goods fields
  val goodsShipmentGovernmentAgencyGoodsItemCommodityDescription = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_commodity_description"
  val goodsShipmentGovernmentAgencyGoodsItemPackagingMarksNumbersID = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_packagings_marksNumbersId"

  //Valuation fields
  val valuationIndicators = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_valuationAdjustment_additionCode" 
  val valuationItemPrice = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_commodity_invoiceLine_itemChargeAmount_value"
  val valuationItemPriceCurrency = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_commodity_invoiceLine_itemChargeAmount_currencyId"

  // TODO blocked until clarified
//  val valuationExchangeRate = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_"
//  val valuationMethodType = ?
// val valuationPreference = ?

  //Tax fields
  val goodsShipmentGovernmentAgencyGoodsItemCommodityDutyTaxFeeTypeCode = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_commodity_dutyTaxFees_typeCode"
  val goodsShipmentGovernmentAgencyGoodsItemCommodityDutyTaxFeeSpecificTaxBaseQuantity = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_commodity_dutyTaxFees_specificTaxBaseQuantity"
  val goodsShipmentGovernmentAgencyGoodsItemCommodityDutyTaxFeePaymentPaymentAmount = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_commodity_dutyTaxFees_payment_paymentAmount"
  val goodsShipmentGovernmentAgencyGoodsItemCommodityDutyTaxFeePaymentTaxAssessedAmount = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_commodity_dutyTaxFees_payment_taxAssessedAmount"
  val goodsShipmentGovernmentAgencyGoodsItemCommodityDutyTaxFeePaymenMethodCode = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_commodity_dutyTaxFees_payment_methodCode"

  def getError(key: String, errors: Map[String, ValidationError])(implicit messages: Messages) = {
    if(errors.get(key).isDefined) {
      errors.get(key).get.messages.map(messages(_))}
  }
}

