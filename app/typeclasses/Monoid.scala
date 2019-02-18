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

package typeclasses

import shapeless._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec._

trait Monoid[T] {

  def empty: T

  def append(l: T, r: T): T
}

object Monoid extends MonoidInstances {

  def apply[T](implicit ev: Monoid[T]): Monoid[T] = ev

  def empty[T: Monoid]: T = implicitly[Monoid[T]].empty

  object ops {

    implicit class MonoidOps[T](val left: T) extends AnyVal {

      def |+|(right: T)(implicit ev: Monoid[T]): T = ev.append(left, right)
    }
  }
}

trait MonoidInstances extends ProductTypeClassCompanion[Monoid] {

  import Monoid.ops._

  implicit def recursiveOptionMonoid[A: Monoid]: Monoid[Option[A]] =
    new Monoid[Option[A]] {
      override def empty: Option[A] = None

      override def append(l: Option[A], r: Option[A]): Option[A] =
        l.fold(r)(a => r.fold(l)(b => Some(a |+| b)))
    }

  implicit def seqMonoid[A]: Monoid[Seq[A]] =
    new Monoid[Seq[A]] {
      override def empty: Seq[A] = Seq.empty

      override def append(l: Seq[A], r: Seq[A]): Seq[A] =
        l ++ r
    }

  implicit val cacheMapMonoid: Monoid[CacheMap] =
    new Monoid[CacheMap] {
      override def empty: CacheMap = CacheMap("", Map())

      override def append(l: CacheMap, r: CacheMap): CacheMap =
        if (r == empty) l
        else CacheMap(r.id, l.data ++ r.data)
    }

  private def rightBiasedMonoid[A]: Monoid[Option[A]] = new Monoid[Option[A]] {
    override def empty: Option[A] = None

    override def append(l: Option[A], r: Option[A]): Option[A] =
      r.fold(l)(_ => r)
  }

  implicit val dateTimeElementMonoid: Monoid[Option[DateTimeElement]] = rightBiasedMonoid
  implicit val intMonoid: Monoid[Option[Int]] = rightBiasedMonoid
  implicit val stringMonoid: Monoid[Option[String]] = rightBiasedMonoid
  implicit val bigDecimalMonoid: Monoid[Option[BigDecimal]] = rightBiasedMonoid
  implicit val itineraryMonoid: Monoid[Option[Itinerary]] = rightBiasedMonoid
  implicit val goodsLocationMonoid: Monoid[Option[GoodsLocation]] = rightBiasedMonoid
  implicit val sealMonoid: Monoid[Option[Seal]] = rightBiasedMonoid
  implicit val transportEquipmentMonoid: Monoid[Option[TransportEquipment]] = rightBiasedMonoid
  implicit val exportCountryMonoid: Monoid[Option[ExportCountry]] = rightBiasedMonoid
  implicit val warehouseMonoid: Monoid[Option[Warehouse]] = rightBiasedMonoid

  implicit val declarationMonoid = new Monoid[Declaration] {
    override def empty: Declaration = Declaration()

    override def append(l: Declaration, r: Declaration): Declaration = {
      def f[A](l: A, r: A)(implicit ev: Monoid[A]): A = l |+| r

      Declaration(
        l.acceptanceDateTime |+| r.acceptanceDateTime,
        l.functionCode |+| r.functionCode,
        l.functionalReferenceId |+| r.functionalReferenceId,
        l.id |+| r.id,
        l.issueDateTime |+| r.issueDateTime,
        l.issueLocationId |+| r.issueLocationId,
        l.typeCode |+| r.typeCode,
        l.goodsItemQuantity |+| r.goodsItemQuantity,
        l.declarationOfficeId |+| r.declarationOfficeId,
        l.invoiceAmount |+| r.invoiceAmount,
        l.loadingListQuantity |+| r.loadingListQuantity,
        l.totalGrossMassMeasure |+| r.totalGrossMassMeasure,
        l.totalPackageQuantity |+| r.totalPackageQuantity,
        l.specificCircumstancesCode |+| r.specificCircumstancesCode,
        l.authentication |+| r.authentication,
        l.submitter |+| r.submitter,
        l.additionalDocuments |+| r.additionalDocuments,
        l.additionalInformations |+| r.additionalInformations,
        l.agent |+| r.agent,
        l.amendments |+| r.amendments,
        l.authorisationHolders |+| r.authorisationHolders,
        l.borderTransportMeans |+| r.borderTransportMeans,
        l.currencyExchanges |+| r.currencyExchanges,
        l.declarant |+| r.declarant,
        l.exitOffice |+| r.exitOffice,
        l.exporter |+| r.exporter,
        l.goodsShipment |+| r.goodsShipment,
        l.obligationGuarantees |+| r.obligationGuarantees,
        l.presentationOffice |+| r.presentationOffice,
        l.supervisingOffice |+| r.supervisingOffice
      )
    }
  }

  object typeClass extends ProductTypeClass[Monoid] {

    override def product[H, T <: HList](ch: Monoid[H], ct: Monoid[T]): Monoid[H :: T] =
      new Monoid[H :: T] {
        override def empty: H :: T = ch.empty :: ct.empty

        override def append(l: H :: T, r: H :: T): H :: T =
          ch.append(l.head, r.head) :: ct.append(l.tail, r.tail)
      }

    override def emptyProduct: Monoid[HNil] =
      new Monoid[HNil] {
        override def empty: HNil = HNil

        override def append(l: HNil, r: HNil): HNil = HNil
      }

    override def project[F, G](instance: => Monoid[G], to: F => G, from: G => F): Monoid[F] =
      new Monoid[F] {
        override def empty: F = from(instance.empty)

        override def append(l: F, r: F): F =
          from(instance.append(to(l), to(r)))
      }
  }
}