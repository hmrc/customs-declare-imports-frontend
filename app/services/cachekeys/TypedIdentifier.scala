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

import domain.{InvoiceAndCurrency, References, SummaryOfGoods, _}
import uk.gov.hmrc.wco.dec._
import enumeratum._

sealed trait TypedIdentifier[A] extends EnumEntry

object TypedIdentifier extends Enum[TypedIdentifier[_]] {

  final case object DeclarantDetailsId            extends TypedIdentifier[ImportExportParty]
  final case object ReferencesId                  extends TypedIdentifier[References]
  final case object ExporterId                    extends TypedIdentifier[ImportExportParty]
  final case object RepresentativeId              extends TypedIdentifier[Agent]
  final case object ImporterId                    extends TypedIdentifier[ImportExportParty]
  final case object TradeTermsId                  extends TypedIdentifier[TradeTerms]
  final case object InvoiceAndCurrencyId          extends TypedIdentifier[InvoiceAndCurrency]
  final case object SellerId                      extends TypedIdentifier[ImportExportParty]
  final case object BuyerId                       extends TypedIdentifier[ImportExportParty]
  final case object SummaryOfGoodsId              extends TypedIdentifier[SummaryOfGoods]
  final case object TransportId                   extends TypedIdentifier[Transport]
  final case object AuthorisationHoldersId        extends TypedIdentifier[Seq[AuthorisationHolder]]
  final case object GuaranteeReferencesId         extends TypedIdentifier[Seq[ObligationGuarantee]]
  final case object PreviousDocumentsId           extends TypedIdentifier[Seq[PreviousDocument]]
  final case object AdditionalDocumentsId         extends TypedIdentifier[Seq[AdditionalDocument]]
  final case object AdditionalSupplyChainActorsId extends TypedIdentifier[Seq[RoleBasedParty]]
  final case object DomesticDutyTaxPartyId        extends TypedIdentifier[Seq[RoleBasedParty]]
  final case object AdditionsAndDeductionsId      extends TypedIdentifier[Seq[ChargeDeduction]]
  final case object ContainerIdNosId              extends TypedIdentifier[Seq[TransportEquipment]]
  final case object GuaranteeTypeId               extends TypedIdentifier[Seq[ObligationGuarantee]]
  final case object GovAgencyGoodsItemsListId     extends TypedIdentifier[Seq[GovernmentAgencyGoodsItem]]
  final case object GovAgencyGoodsItemId          extends TypedIdentifier[GovernmentAgencyGoodsItem]
  final case object GovAgencyGoodsItemReferenceId extends TypedIdentifier[GovernmentAgencyGoodsItem]
  final case object WarehouseAndCustomsId         extends TypedIdentifier[WarehouseAndCustoms]

  override def values = findValues
}