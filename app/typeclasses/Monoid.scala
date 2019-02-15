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

trait MonoidInstances extends ProductTypeClassCompanion[Monoid]
  with LowPriorityImplicitsMonoidInstances {

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

  implicit def cacheMapMonoid: Monoid[CacheMap] =
    new Monoid[CacheMap] {
      override def empty: CacheMap = CacheMap("", Map())

      override def append(l: CacheMap, r: CacheMap): CacheMap =
        if (r == empty) l
        else CacheMap(r.id, l.data ++ r.data)
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

trait LowPriorityImplicitsMonoidInstances {

  implicit def rightBiasedOptionMonoid[A]: Monoid[Option[A]] =
    new Monoid[Option[A]] {
      override def empty: Option[A] = None

      override def append(l: Option[A], r: Option[A]): Option[A] =
        r.fold(l)(Some(_))
    }
}