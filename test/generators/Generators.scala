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

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import uk.gov.hmrc.wco.dec.Pointer

trait Generators {

  implicit val arbitraryPointer: Arbitrary[Pointer] = Arbitrary {
    for {
      sequenceNumeric     <- option(arbitrary[Int].suchThat(x => x >= 0 && x <= 99999))
      documentSectionCode <- option(arbitrary[String].map(_.take(3)))
      tagId               <- option(arbitrary[String].map(_.take(4)))
    } yield Pointer(sequenceNumeric, documentSectionCode, tagId)
  }

  def intGreaterThan(min: Int): Gen[Int] =
    choose(min + 1, Int.MaxValue)

  def intLessThan(max: Int): Gen[Int] =
    choose(Int.MinValue, max - 1)

  def minStringLength(length: Int): Gen[String] =
    arbitrary[String].suchThat(_.size >= length)

}
