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
import services.cachekeys.TypedIdentifier
import services.cachekeys.TypedIdentifier._
import services.cachekeys.CacheKey._

object MetaDataConverter {

  def asMetaData(cache: CacheMap): TypedIdentifier[_] => MetaData = {
    case DeclarantDetailsId =>
      MetaData(declaration = Some(Declaration(declarant = cache.getEntry[ImportExportParty](declarantDetails.key))))

    case ReferencesId => {
      val refData = cache.getEntry[References](references.key)
      MetaData(declaration = Some(Declaration(
        typeCode = refData.flatMap(r => r.typeCode.flatMap(a => r.typerCode.map(b => a + b))),
        functionalReferenceId = refData.flatMap(_.functionalReferenceId),
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
      MetaData(declaration = Some(Declaration(goodsShipment = Some(GoodsShipment(importer = cache.getEntry[ImportExportParty](importer.key))))))

    case TradeTermsId =>
      MetaData(declaration = Some(Declaration(goodsShipment = Some(GoodsShipment(tradeTerms = cache.getEntry[TradeTerms](tradeTerms.key))))))

    case InvoiceAndCurrencyId => {
      val data = cache.getEntry[InvoiceAndCurrency](invoiceAndCurrency.key)

      MetaData(declaration = Some(Declaration(
        invoiceAmount = data.flatMap(_.invoice),
        currencyExchanges = data.flatMap(_.currency).toSeq
      )))
    }

    case SellerId =>
      MetaData(declaration = Some(Declaration(goodsShipment = Some(GoodsShipment(seller = cache.getEntry[ImportExportParty](seller.key))))))

    case BuyerId =>
      MetaData(declaration = Some(Declaration(goodsShipment = Some(GoodsShipment(buyer = cache.getEntry[ImportExportParty](buyer.key))))))

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


    case _ => throw new MatchError("Hide exhaustivity warnings, remove before merging!!!!!!")
  }

}