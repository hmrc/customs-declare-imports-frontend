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
import org.scalatest.enablers.Length
import uk.gov.hmrc.wco.dec.{AdditionalInformation, Pointer}

trait Generators extends SignedInUserGen {
  
  implicit val arbitraryAdditionalInfo: Arbitrary[AdditionalInformation] = Arbitrary {
    for {
      statementCode         <- option(arbitrary[String].map(_.take(17)))
      statementDescription  <- option(arbitrary[String].map(_.take(512)))
      statementTypeCode     <- option(arbitrary[String].map(_.take(3)))
    } yield AdditionalInformation(statementCode, statementDescription, statementTypeCode)
  }

  def intGreaterThan(min: Int): Gen[Int] =
    choose(min + 1, Int.MaxValue)

  def intLessThan(max: Int): Gen[Int] =
    choose(Int.MinValue, max - 1)

  def minStringLength(length: Int): Gen[String] =
    for {
      i <- choose(length, length + 500)
      n <- listOfN(i, arbitrary[Char])
    } yield n.mkString

}
