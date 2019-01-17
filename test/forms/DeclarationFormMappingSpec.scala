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
import org.scalacheck.Arbitrary
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.{Form, FormError}
import uk.gov.hmrc.wco.dec.{AdditionalInformation, Pointer}
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

  "additionalInformationForm" should {

    "bind" when {

      "valid values are bound" in {

        forAll { additionalInfo: AdditionalInformation =>

          Form(additionalInformationMapping).fillAndValidate(additionalInfo).fold(
              _          => fail("Test should not fail"),
              result     => result mustBe additionalInfo
          )
        }
      }

      "fail with invalid statement code" when {

        "statement code length is greater than 17" in {

          forAll(arbitrary[AdditionalInformation], minStringLength(17)) { (additionalInfo, invalidCode) =>

            Form(additionalInformationMapping).fillAndValidate(additionalInfo.copy(statementCode = Some(invalidCode))).fold(
              error => error.error("statementCode") must haveMessage("statement Code should be less than or equal to 17 characters"),
              _     => fail("Should not succeed")
            )
          }
        }
      }

      "fail with invalid statement description" when {

        "statement description length is greater than 512" in {

          forAll(arbitrary[AdditionalInformation], minStringLength(512))  { (additionalInfo, invalidDescription) =>

            Form(additionalInformationMapping).fillAndValidate(additionalInfo.copy(statementDescription = Some(invalidDescription))).fold(
              error => error.error("statementDescription") must haveMessage("statement Description should be less than or equal to 512 characters"),
              _     => fail("Should not succeed")
            )
          }
        }
      }

      "fail with invalid statement type code" when {

        "statement type code length is greater than 3" in {

          forAll(arbitrary[AdditionalInformation], minStringLength(3))  { (additionalInfo, invalidTypeCode) =>

            Form(additionalInformationMapping).fillAndValidate(additionalInfo.copy(statementTypeCode = Some(invalidTypeCode))).fold(
              error => error.error("statementTypeCode") must haveMessage("statement Type Code should be less than or equal to 3 characters"),
              _     => fail("Should not succeed")
            )
          }
        }
      }

    }
  }
}