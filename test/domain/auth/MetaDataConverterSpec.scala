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
import org.scalacheck.Arbitrary._
import org.scalacheck.Arbitrary
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import services.cachekeys.CacheKey
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.{Agent, ImportExportParty, TradeTerms}

class MetaDataConverterSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with Lenses {

  implicit val arbitraryCacheMap: Arbitrary[CacheMap] =
    Arbitrary(arbitrary[String].map(CacheMap(_, Map())))

  implicit class OptionOps[A](val option: Option[A]) {

    def >>[B](f: A => Option[B]): Option[B] = option.flatMap(f)
  }

  "asMetaData" should {

    "convert using DeclarantDetailsId" in {

      val cacheMapGen = CacheMapLens.declarantDetails.setArbitrary(arbitrary[ImportExportParty])

      forAll(cacheMapGen) { cacheMap =>

        val data = cacheMap.getEntry[ImportExportParty](CacheKey.declarantDetails.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.declarantDetails.identifier)
          .declaration
          .flatMap(_.declarant) mustBe data
      }
    }

    "convert using ReferencesId" in {

      val cacheMapGen = CacheMapLens.references.setArbitrary(arbitrary[References])

      forAll(cacheMapGen) { cacheMap =>

        val data = cacheMap.getEntry[References](CacheKey.references.key)

        val dec = MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.references.identifier)
          .declaration

        dec.flatMap(_.typeCode) mustBe data.flatMap(d => d.typeCode.flatMap(a => d.typerCode.map(b => a + b)))
        dec.flatMap(_.functionalReferenceId) mustBe data.flatMap(_.functionalReferenceId)
        dec.flatMap(_.goodsShipment.flatMap(_.transactionNatureCode)) mustBe data.flatMap(_.transactionNatureCode)
        dec.flatMap(_.goodsShipment.flatMap(_.ucr.flatMap(_.traderAssignedReferenceId))) mustBe
          data.flatMap(_.traderAssignedReferenceId)
      }
    }

    "convert using ExporterId" in {

      val cacheMapGen = CacheMapLens.exporter.setArbitrary(arbitrary[ImportExportParty])

      forAll(cacheMapGen) { cacheMap =>

        val data = cacheMap.getEntry[ImportExportParty](CacheKey.exporter.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.exporter.identifier)
          .declaration
          .flatMap(_.exporter) mustBe data
      }
    }

    "convert using RepresentativeId" in {

      val cacheMapGen = CacheMapLens.representative.setArbitrary(arbitrary[Agent])

      forAll(cacheMapGen) { cacheMap =>

        val data = cacheMap.getEntry[Agent](CacheKey.representative.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.representative.identifier)
          .declaration
          .flatMap(_.agent) mustBe data
      }
    }

    "convert using ImporterId" in {

      val cacheMapGen = CacheMapLens.importer.setArbitrary(arbitrary[ImportExportParty])

      forAll(cacheMapGen) { cacheMap =>

        val data = cacheMap.getEntry[ImportExportParty](CacheKey.importer.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.importer.identifier)
          .declaration
          .flatMap(_.goodsShipment)
          .flatMap(_.importer) mustBe data
      }
    }

    "convert using TradeTermsId" in {

      val cacheMapGen = CacheMapLens.tradeTerms.setArbitrary(arbitrary[TradeTerms])

      forAll(cacheMapGen) { cacheMap =>

        val data = cacheMap.getEntry[TradeTerms](CacheKey.tradeTerms.key)

        MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.tradeTerms.identifier)
          .declaration
          .flatMap(_.goodsShipment)
          .flatMap(_.tradeTerms) mustBe data
      }
    }

    "convert using InvoiceAndCurrencyId" in {

      val cacheMapGen = CacheMapLens.invoiceAndCurrency.setArbitrary(arbitrary[InvoiceAndCurrency])

      forAll(cacheMapGen) { cacheMap =>

        val data = cacheMap.getEntry[InvoiceAndCurrency](CacheKey.invoiceAndCurrency.key)

        val dec = MetaDataConverter
          .asMetaData(cacheMap)(CacheKey.invoiceAndCurrency.identifier)
          .declaration


      }
    }
  }
}