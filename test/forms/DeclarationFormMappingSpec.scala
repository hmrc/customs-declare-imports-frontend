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

package forms

import DeclarationFormMapping._
import generators.Generators
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.{Form, FormError}
import uk.gov.hmrc.wco.dec.Pointer
import org.scalacheck.Arbitrary._

class DeclarationFormMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators {

  class ErrorHasMessage(right: String) extends Matcher[Option[FormError]] {
    override def apply(left: Option[FormError]): MatchResult =
      MatchResult(
        left.exists(_.message == right),
        s""""$left" does not contains message "$right"""",
        s""""$left contains message "$right""""
      )
  }

  def haveMessage(right: String) = new ErrorHasMessage(right)

  "pointerForm" should {

    "bind" when {

      "valid values are bound" in {

        forAll { pointer: Pointer =>

          Form(pointerMapping).fillAndValidate(pointer).fold(
            _ => fail("Test should not fail"),
            result => result mustBe pointer
          )
        }
      }

      "fail with invalid sequence numeric" when {

        "sequence number is less than 0" in {

          forAll(arbitrary[Pointer], intLessThan(0)) { (pointer, i) =>

            Form(pointerMapping).fillAndValidate(pointer.copy(sequenceNumeric = Some(i))).fold(
              error => error.error("sequenceNumeric") must haveMessage("Pointer sequence numeric cannot be less than 0"),
              _ => fail("Should not succeed")
            )
          }
        }

        "sequence number is greater than 99999" in {

          forAll(arbitrary[Pointer], intGreaterThan(99999)) { (pointer, i) =>

            Form(pointerMapping).fillAndValidate(pointer.copy(sequenceNumeric = Some(i))).fold(
              error => error.error("sequenceNumeric") must haveMessage("Pointer sequence numeric cannot be greater than 99999"),
              _ => fail("Should not succeed")
            )
          }
        }
      }

      "fail with invalid document section code" when {

        "document section code length is greater than 3" in {

          forAll(arbitrary[Pointer], minStringLength(4)) { (pointer, i) =>

            Form(pointerMapping).fillAndValidate(pointer.copy(documentSectionCode = Some(i))).fold(
              error => error.error("documentSectionCode") must haveMessage("Pointer document section code length cannot be greater than 3"),
              _ => fail("Should not succeed")
            )
          }
        }
      }

      "fail with invalid tag id" when {

        "tag id length is greater than 4" in {

          forAll(arbitrary[Pointer], minStringLength(5)) { (pointer, i) =>

            Form(pointerMapping).fillAndValidate(pointer.copy(tagId = Some(i))).fold(
              error => error.error("tagId") must haveMessage("Pointer tag id length cannot be greater than 4"),
              _ => fail("Should not succeed")
            )
          }
        }
      }
    }
  }
}