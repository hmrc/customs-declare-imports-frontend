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

import domain.DeclarationFormats._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec._
import services.cachekeys._
import services.cachekeys.CacheKey._
import services.cachekeys.TypedIdentifier._
import typeclasses.Monoid

object MetaDataMapping {

  import Monoid.ops._

  def produce(cacheMap: CacheMap): MetaData = {
    val applied = asMetaData(cacheMap)

    TypedIdentifier.values.foldLeft(Monoid.empty[MetaData])((z, a) => z |+| applied(a))
  }

  private def asMetaData(cache: CacheMap): TypedIdentifier[_] => MetaData = {

    case DeclarantDetailsId =>
      MetaData(declaration = Some(Declaration(declarant = cache.getEntry[ImportExportParty](declarantDetails.key))))

    case ReferencesId => {
      val refData = cache.getEntry[References](references.key)
      MetaData(declaration = Some(Declaration(
        functionCode = Some(9),
        typeCode = refData.map(r => r.typeCode + r.typerCode),
        functionalReferenceId = refData.map(_.functionalReferenceId),
        goodsShipment = Some(GoodsShipment(
          transactionNatureCode = refData.flatMap(_.transactionNatureCode),
          ucr = Some(Ucr(traderAssignedReferenceId = refData.flatMap(_.traderAssignedReferenceId)))
        ))
      )))
    }

    case ExporterId =>
      MetaData(declaration = Some(Declaration(exporter = cache.getEntry[ImportExportParty](exporter.key))))

    case RepresentativeId =>
      MetaData(declaration = Some(Declaration(agent = cache.getEntry[Agent](representative.key))))

    case ImporterId =>
      MetaData(declaration = Some(Declaration(
        goodsShipment = Some(GoodsShipment(importer = cache.getEntry[ImportExportParty](importer.key))))
      ))

    case TradeTermsId =>
      MetaData(declaration = Some(Declaration(
        goodsShipment = Some(GoodsShipment(tradeTerms = cache.getEntry[TradeTerms](tradeTerms.key)))
      )))

    case InvoiceAndCurrencyId => {
      val data = cache.getEntry[InvoiceAndCurrency](invoiceAndCurrency.key)

      MetaData(declaration = Some(Declaration(
        invoiceAmount = data.flatMap(_.invoice),
        currencyExchanges = data.flatMap(_.currency).toSeq
      )))
    }

    case SellerId =>
      MetaData(declaration = Some(Declaration(
        goodsShipment = Some(GoodsShipment(seller = cache.getEntry[ImportExportParty](seller.key)))
      )))

    case BuyerId =>
      MetaData(declaration = Some(Declaration(
        goodsShipment = Some(GoodsShipment(buyer = cache.getEntry[ImportExportParty](buyer.key)))
      )))

    case SummaryOfGoodsId => {
      val data = cache.getEntry[SummaryOfGoods](summaryOfGoods.key)
      MetaData(declaration = Some(Declaration(
        totalPackageQuantity = data.flatMap(_.totalPackageQuantity),
        totalGrossMassMeasure = data.flatMap(_.totalGrossMassMeasure)
      )))
    }

    case TransportId => {
      val data = cache.getEntry[Transport](transport.key)
      MetaData(declaration = Some(Declaration(
        borderTransportMeans = data.flatMap(_.borderTransportMeans),
        goodsShipment = Some(GoodsShipment(
          consignment = Some(Consignment(
            containerCode = data.flatMap(_.containerCode.map(_.toString)),
            arrivalTransportMeans = data.flatMap(_.arrivalTransportMeans)
          ))
        ))
      )))
    }

    case AuthorisationHoldersId =>
      MetaData(declaration = Some(Declaration(
        authorisationHolders = cache.getEntry[Seq[AuthorisationHolder]](authorisationHolders.key).getOrElse(Seq.empty)
      )))

    case GuaranteeReferencesId =>
      MetaData(declaration = Some(Declaration(
        obligationGuarantees = cache.getEntry[Seq[ObligationGuarantee]](guaranteeReferences.key).getOrElse(Seq.empty)
      )))

    case PreviousDocumentsId =>
      MetaData(declaration = Some(Declaration(
        goodsShipment = Some(GoodsShipment(
          previousDocuments = cache.getEntry[Seq[PreviousDocument]](previousDocuments.key).getOrElse(Seq.empty)
      )))))

    case AdditionalDocumentsId =>
      MetaData(declaration = Some(Declaration(
        additionalDocuments = cache.getEntry[Seq[AdditionalDocument]](additionalDocuments.key).getOrElse(Seq.empty)
      )))

    case AdditionalSupplyChainActorsId =>
      MetaData(declaration = Some(Declaration(
        goodsShipment = Some(GoodsShipment(
          aeoMutualRecognitionParties = cache.getEntry[Seq[RoleBasedParty]](additionalSupplyChainActors.key).getOrElse(Seq.empty)
        ))
      )))

    case DomesticDutyTaxPartyId =>
      MetaData(declaration = Some(Declaration(
        goodsShipment = Some(GoodsShipment(
          domesticDutyTaxParties = cache.getEntry[Seq[RoleBasedParty]](domesticDutyTaxParty.key).getOrElse(Seq.empty)
        ))
      )))

    case AdditionsAndDeductionsId =>
      MetaData(declaration = Some(Declaration(
        goodsShipment = Some(GoodsShipment(
          customsValuation = Some(CustomsValuation(
            chargeDeductions = cache.getEntry[Seq[ChargeDeduction]](additionsAndDeductions.key).getOrElse(Seq.empty)
          ))
        ))
      )))

    case ContainerIdNosId =>
      MetaData(declaration = Some(Declaration(
        goodsShipment = Some(GoodsShipment(
          consignment = Some(Consignment(
            transportEquipments = cache.getEntry[Seq[TransportEquipment]](containerIdNos.key).getOrElse(Seq.empty)
          ))
        )))
      ))

    case GuaranteeTypeId =>
      MetaData(declaration = Some(Declaration(
        obligationGuarantees = cache.getEntry[Seq[ObligationGuarantee]](guaranteeTypes.key).getOrElse(Seq.empty)
      )))

    case GovAgencyGoodsItemsListId => {
      val data = cache.getEntry[Seq[GovernmentAgencyGoodsItem]](govAgencyGoodsItemsList.key)

      MetaData(declaration = Some(Declaration(
        goodsItemQuantity = data.map(_.size),
        goodsShipment = Some(GoodsShipment(
          governmentAgencyGoodsItems = data.getOrElse(Seq.empty)
        ))
      )))
    }

    case WarehouseAndCustomsId => {
      val data = cache.getEntry[WarehouseAndCustoms](warehouseAndCustoms.key)

      MetaData(declaration = Some(Declaration(
        goodsShipment = Some(GoodsShipment(warehouse = data.flatMap(_.warehouse))),
        presentationOffice = data.flatMap(_.presentationOffice),
        supervisingOffice = data.flatMap(_.supervisingOffice)
      )))
    }

    case LocationOfGoodsId => {
      val data = cache.getEntry[LocationOfGoods](locationOfGoods.key)
      MetaData(declaration = Some(Declaration(
        goodsShipment = Some(GoodsShipment(
          consignment = Some(Consignment(
            goodsLocation = data.flatMap(_.goodsLocation),
            loadingLocation = data.flatMap(_.loadingLocation)
          )),
          destination = data.flatMap(_.destination),
          exportCountry = data.flatMap(_.exportCountry)
        )))
      ))
    }

    case GovAgencyGoodsItemId          => MetaData()
    case GovAgencyGoodsItemReferenceId => MetaData()
  }
}