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

import org.scalacheck.{ Arbitrary, Gen }
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen._
import org.scalatest.OptionValues
import viewmodels.{ HtmlTable, HtmlTableRow }

trait ViewModelGenerators extends OptionValues {

  implicit val arbitraryTable: Arbitrary[HtmlTable[String, String]] =
    Arbitrary {
      for {
        n      <- choose(1, 50)
        header <- genRow(n)
        rows   <- listOf(genRow(n))
      } yield {
        HtmlTable(header, rows).value
      }
    }

  implicit val arbitraryRow: Arbitrary[HtmlTableRow[String]] =
    Arbitrary(choose(1, 50).flatMap(genRow))

  def genRow(n: Int): Gen[HtmlTableRow[String]] =
    listOfN(n, arbitrary[String])
      .map(xs => HtmlTableRow(xs.headOption.value, xs.tail))
}
