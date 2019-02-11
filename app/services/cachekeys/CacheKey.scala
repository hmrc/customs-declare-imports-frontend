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

package services.cachekeys

import domain.{GovernmentAgencyGoodsItem, InvoiceAndCurrency, References, SummaryOfGoods, _}
import uk.gov.hmrc.wco.dec._

trait Identifier[A]

case class CacheKey[A](key: String, identifier: Identifier[A])

object CacheKey {

  val declarantDetails = CacheKey("DeclarantDetails", new Identifier[ImportExportParty] {})

  val references = CacheKey("References", new Identifier[References] {})

  val exporter = CacheKey("Exporter", new Identifier[ImportExportParty] {})

  val representative = CacheKey("Representative", new Identifier[Agent] {})

  val importer = CacheKey("Importer", new Identifier[ImportExportParty] {})

  val tradeTerms = CacheKey("TradeTerms", new Identifier[TradeTerms] {})

  val invoiceAndCurrency = CacheKey("InvoiceAndCurrency", new Identifier[InvoiceAndCurrency] {})

  val seller = CacheKey("Seller", new Identifier[ImportExportParty] {})

  val buyer = CacheKey("Buyer", new Identifier[ImportExportParty] {})

  val summaryOfGoods = CacheKey("SummaryOfGoods", new Identifier[SummaryOfGoods] {})

  val transport = CacheKey("Transport", new Identifier[Transport] {})

  val authorisationHolders = CacheKey("AuthorisationHolders", new Identifier[Seq[AuthorisationHolder]] {})

  val guaranteeReference = CacheKey("GuaranteeReferences", new Identifier[Seq[ObligationGuarantee]] {})

  val previousDocuments = CacheKey("PreviousDocuments", new Identifier[Seq[PreviousDocument]] {})

  val additionalDocuments = CacheKey("AdditionalDocuments", new Identifier[Seq[AdditionalDocument]] {})

  val additionalSupplyChainActors = CacheKey("AdditionalSupplyChainActors", new Identifier[Seq[RoleBasedParty]] {})
  val domesticDutyTaxParty = CacheKey("DomesticDutyTaxParty", new Identifier[Seq[RoleBasedParty]] {})

  val additionsAndDeductions = CacheKey("AdditionsAndDeductions", new Identifier[Seq[ChargeDeduction]] {})

  val containerIdNos = CacheKey("ContainerIdNos", new Identifier[Seq[TransportEquipment]] {})

  val guaranteeType = CacheKey("GuaranteeType", new Identifier[Seq[ObligationGuarantee]] {})

  val govAgencyGoodsItemsList = CacheKey("GovAgencyGoodsItemsList", new Identifier[Seq[GovernmentAgencyGoodsItem]] {})
  val goodsItem = CacheKey("GovAgencyGoodsItem", new Identifier[GovernmentAgencyGoodsItem] {})
  val govAgencyGoodsItemReference = CacheKey("GovAgencyGoodsItemReference", new Identifier[GovernmentAgencyGoodsItem] {})

  val warehouseAndCustoms = CacheKey("WarehouseAndCustoms", new Identifier[WarehouseAndCustoms] {})

}