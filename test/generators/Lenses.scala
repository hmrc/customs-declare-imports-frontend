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

package generators

import domain.DeclarationFormats._
import domain.{InvoiceAndCurrency, References, SummaryOfGoods, Transport}
import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.OptionValues
import play.api.libs.json._
import services.cachekeys.CacheKey
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec._

trait Lenses extends OptionValues {

  case class GenLens[S, A](
    private val pGet: S => A,
    private val pSet: A => S => S
  ) {

    def get: Gen[S] => Gen[A] = _.map(pGet)

    def set: Gen[A] => Gen[S] => Gen[S] =
      a => _.flatMap(ss => a.map(aa => pSet(aa)(ss)))

    def setArbitrary(a: Gen[A])(implicit ev: Arbitrary[S]): Gen[S] =
      set(a)(arbitrary[S])
  }

  object BorderTransportMeansLens {

    val modeCode: GenLens[BorderTransportMeans, Option[Int]] =
      GenLens(_.modeCode, a => _.copy(modeCode = a))

    val registrationNationalityCode: GenLens[BorderTransportMeans, Option[String]] =
      GenLens(_.registrationNationalityCode, a => _.copy(registrationNationalityCode = a))
  }

  object TransportMeansLens {

    val modeCode: GenLens[TransportMeans, Option[Int]] =
      GenLens(_.modeCode, a => _.copy(modeCode = a))

    val identificationTypeCode: GenLens[TransportMeans, Option[String]] =
      GenLens(_.identificationTypeCode, a => _.copy(identificationTypeCode = a))

    val id: GenLens[TransportMeans, Option[String]] =
      GenLens(_.id, a => _.copy(id = a))
  }

  object TransportLens {

    val containerCode: GenLens[Transport, Option[Int]] =
      GenLens(_.containerCode, a => _.copy(containerCode = a))
  }

  object SummaryOfGoodsLens {

    val totalPackageQuantity: GenLens[SummaryOfGoods, Option[Int]] =
      GenLens(_.totalPackageQuantity, a => _.copy(totalPackageQuantity = a))
  }

  object CacheMapLens {

    private def mapProperty[T: Reads: Writes](id: CacheKey[T]): GenLens[CacheMap, T] =
      GenLens(_.getEntry[T](id.key).value, a => s => CacheMap(s.id, s.data + (id.key -> Json.toJson(a))))

    val declarantDetails: GenLens[CacheMap, ImportExportParty] = mapProperty(CacheKey.declarantDetails)
    val references: GenLens[CacheMap, References] = mapProperty(CacheKey.references)
    val exporter: GenLens[CacheMap, ImportExportParty] = mapProperty(CacheKey.exporter)
    val representative: GenLens[CacheMap, Agent] = mapProperty(CacheKey.representative)
    val importer: GenLens[CacheMap, ImportExportParty] = mapProperty(CacheKey.importer)
    val tradeTerms: GenLens[CacheMap, TradeTerms] = mapProperty(CacheKey.tradeTerms)
    val invoiceAndCurrency: GenLens[CacheMap, InvoiceAndCurrency] = mapProperty(CacheKey.invoiceAndCurrency)
  }
}