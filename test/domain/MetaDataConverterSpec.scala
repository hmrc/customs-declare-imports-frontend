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
import generators.{Generators, Lenses}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import services.cachekeys.CacheKey
import typeclasses.Monoid
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec._

class MetaDataConverterSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with Lenses {

  override implicit val arbitraryCacheMap: Arbitrary[CacheMap] = Arbitrary {
    List(
      CacheMapLens.declarantDetails.set(arbitrary[ImportExportParty]),
      CacheMapLens.references.set(arbitrary[References]),
      CacheMapLens.exporter.set(arbitrary[ImportExportParty]),
      CacheMapLens.representative.set(arbitrary[Agent]),
      CacheMapLens.importer.set(arbitrary[ImportExportParty]),
      CacheMapLens.tradeTerms.set(arbitrary[TradeTerms]),
      CacheMapLens.invoiceAndCurrency.set(arbitrary[InvoiceAndCurrency]),
      CacheMapLens.seller.set(arbitrary[ImportExportParty]),
      CacheMapLens.buyer.set(arbitrary[ImportExportParty]),
      CacheMapLens.summaryOfGoods.set(arbitrary[SummaryOfGoods]),
      CacheMapLens.transport.set(arbitrary[Transport]),
      CacheMapLens.authorisationHolders.set(listOf(arbitrary[AuthorisationHolder])),
      CacheMapLens.guaranteeReferences.set(listOf(arbitrary[ObligationGuarantee])),
      CacheMapLens.previousDocuments.set(listOf(arbitrary[PreviousDocument])),
      CacheMapLens.additionalDocuments.set(listOf(arbitrary[AdditionalDocument])),
      CacheMapLens.additionalSupplyChainActors.set(listOf(arbitrary[RoleBasedParty])),
      CacheMapLens.domesticDutyTaxParty.set(listOf(arbitrary[RoleBasedParty])),
      CacheMapLens.additionsAndDeductions.set(listOf(arbitrary[ChargeDeduction])),
      CacheMapLens.containerIdNos.set(listOf(arbitrary[TransportEquipment])),
      CacheMapLens.guaranteeTypes.set(listOf(arbitrary[ObligationGuarantee]))
    ).foldLeft(const(Monoid.empty[CacheMap])) { case (acc, f) => f(acc) }
  }

  "asMetaData" should {

    "convert using DeclarantDetailsId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[ImportExportParty](CacheKey.declarantDetails.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.declarantDetails.identifier)
          .declaration
          .flatMap(_.declarant) mustBe data
      }
    }

    "convert using ReferencesId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[References](CacheKey.references.key)

        val dec = MetaDataConverter.asMetaData(cacheMap)(CacheKey.references.identifier).declaration

        dec.flatMap(_.typeCode) mustBe data.flatMap(d => d.typeCode.flatMap(a => d.typerCode.map(b => a + b)))
        dec.flatMap(_.functionalReferenceId) mustBe data.flatMap(_.functionalReferenceId)
        dec.flatMap(_.goodsShipment.flatMap(_.transactionNatureCode)) mustBe data.flatMap(_.transactionNatureCode)
        dec.flatMap(_.goodsShipment.flatMap(_.ucr.flatMap(_.traderAssignedReferenceId))) mustBe
          data.flatMap(_.traderAssignedReferenceId)
      }
    }

    "convert using ExporterId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[ImportExportParty](CacheKey.exporter.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.exporter.identifier)
          .declaration
          .flatMap(_.exporter) mustBe data
      }
    }

    "convert using RepresentativeId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[Agent](CacheKey.representative.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.representative.identifier)
          .declaration
          .flatMap(_.agent) mustBe data
      }
    }

    "convert using ImporterId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[ImportExportParty](CacheKey.importer.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.importer.identifier)
          .declaration
          .flatMap(_.goodsShipment)
          .flatMap(_.importer) mustBe data
      }
    }

    "convert using TradeTermsId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[TradeTerms](CacheKey.tradeTerms.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.tradeTerms.identifier)
          .declaration
          .flatMap(_.goodsShipment)
          .flatMap(_.tradeTerms) mustBe data
      }
    }

    "convert using InvoiceAndCurrencyId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[InvoiceAndCurrency](CacheKey.invoiceAndCurrency.key)

        val dec = MetaDataConverter.asMetaData(cacheMap)(CacheKey.invoiceAndCurrency.identifier).declaration

        dec.flatMap(_.invoiceAmount) mustBe data.flatMap(_.invoice)
        dec.flatMap(_.currencyExchanges.headOption) mustBe data.flatMap(_.currency)
      }
    }

    "convert using SellerId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[ImportExportParty](CacheKey.seller.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.seller.identifier)
          .declaration
          .flatMap(_.goodsShipment)
          .flatMap(_.seller) mustBe data
      }
    }

    "convert using BuyerId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[ImportExportParty](CacheKey.buyer.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.buyer.identifier)
          .declaration
          .flatMap(_.goodsShipment)
          .flatMap(_.buyer) mustBe data
      }
    }

    "convert using SummaryOfGoodsId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[SummaryOfGoods](CacheKey.summaryOfGoods.key)

        val dec = MetaDataConverter.asMetaData(cacheMap)(CacheKey.summaryOfGoods.identifier).declaration

        dec.flatMap(_.totalPackageQuantity) mustBe data.flatMap(_.totalPackageQuantity)
        dec.flatMap(_.totalGrossMassMeasure) mustBe data.flatMap(_.totalGrossMassMeasure)
      }
    }

    "convert using TransportId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[Transport](CacheKey.transport.key)

        val dec = MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.transport.identifier)
          .declaration

        val consignment = dec.flatMap(_.goodsShipment).flatMap(_.consignment)

        dec.flatMap(_.borderTransportMeans) mustBe data.flatMap(_.borderTransportMeans)
        consignment.flatMap(_.containerCode) mustBe data.flatMap(_.containerCode.map(_.toString))
        consignment.flatMap(_.arrivalTransportMeans) mustBe data.flatMap(_.arrivalTransportMeans)
      }
    }

    "convert using AuthorisationHoldersId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[Seq[AuthorisationHolder]](CacheKey.authorisationHolders.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.authorisationHolders.identifier)
          .declaration
          .map(_.authorisationHolders) mustBe data
      }
    }

    "convert using GuaranteeReferencesId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[Seq[ObligationGuarantee]](CacheKey.guaranteeReferences.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.guaranteeReferences.identifier)
          .declaration
          .map(_.obligationGuarantees) mustBe data
      }
    }

    "convert using PreviousDocumentsId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[Seq[PreviousDocument]](CacheKey.previousDocuments.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.previousDocuments.identifier)
          .declaration
          .flatMap(_.goodsShipment)
          .map(_.previousDocuments) mustBe data
      }
    }

    "convert using AdditionalDocumentsId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[Seq[AdditionalDocument]](CacheKey.additionalDocuments.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.additionalDocuments.identifier)
          .declaration
          .map(_.additionalDocuments) mustBe data
      }
    }

    "convert using AdditionalSupplyChainActorsId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[Seq[RoleBasedParty]](CacheKey.additionalSupplyChainActors.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.additionalSupplyChainActors.identifier)
          .declaration
          .flatMap(_.goodsShipment)
          .map(_.aeoMutualRecognitionParties) mustBe data
      }
    }

    "convert using DomesticDutyTaxPartyId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[Seq[RoleBasedParty]](CacheKey.domesticDutyTaxParty.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.domesticDutyTaxParty.identifier)
          .declaration
          .flatMap(_.goodsShipment)
          .map(_.domesticDutyTaxParties) mustBe data
      }
    }

    "convert using AdditionsAndDeductionsId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[Seq[ChargeDeduction]](CacheKey.additionsAndDeductions.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.additionsAndDeductions.identifier)
          .declaration
          .flatMap(_.goodsShipment)
          .flatMap(_.customsValuation)
          .map(_.chargeDeductions) mustBe data
      }
    }

    "convert using ContainerIdNosId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[Seq[TransportEquipment]](CacheKey.containerIdNos.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.containerIdNos.identifier)
          .declaration
          .flatMap(_.goodsShipment)
          .flatMap(_.consignment)
          .map(_.transportEquipments) mustBe data
      }
    }

    "convert using GuaranteeTypeId" in {

      forAll { cacheMap: CacheMap =>

        val data = cacheMap.getEntry[Seq[ObligationGuarantee]](CacheKey.guaranteeTypes.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.guaranteeTypes.identifier)
          .declaration
          .map(_.obligationGuarantees) mustBe data
      }
    }
  }
}