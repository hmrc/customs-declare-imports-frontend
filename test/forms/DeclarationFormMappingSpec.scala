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

import forms.DeclarationFormMapping._
import generators.Generators
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.AdditionalInformation

class DeclarationFormMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  "additionalInformationForm" should {

    "bind" when {

      "valid values are bound" in {

        forAll { additionalInfo: AdditionalInformation =>

          Form(additionalInformationMapping).fillAndValidate(additionalInfo).fold(
              error      => fail(s"Failed with errors:\n${error.errors.map(_.message).mkString("\n")}"),
              result     => result mustBe additionalInfo
          )
        }
      }

      "fail with invalid statement code" when {

        "statement code length is greater than 17" in {

          forAll(arbitrary[AdditionalInformation], minStringLength(18)) { (additionalInfo, invalidCode) =>

            Form(additionalInformationMapping).fillAndValidate(additionalInfo.copy(statementCode = Some(invalidCode))).fold(
              error => error.error("statementCode") must haveMessage("statement code should be less than or equal to 17 characters"),
              _     => fail("Should not succeed")
            )
          }
        }
      }

      "fail with invalid statement description" when {

        "statement description length is greater than 512" in {

          forAll(arbitrary[AdditionalInformation], minStringLength(513))  { (additionalInfo, invalidDescription) =>

            Form(additionalInformationMapping).fillAndValidate(additionalInfo.copy(statementDescription = Some(invalidDescription))).fold(
              error => error.error("statementDescription") must haveMessage("statement description should be less than or equal to 512 characters"),
              _     => fail("Should not succeed")
            )
          }
        }
      }

      "fail with invalid statement type code" when {

        "statement type code length is greater than 3" in {

          forAll(arbitrary[AdditionalInformation], minStringLength(4))  { (additionalInfo, invalidTypeCode) =>

            Form(additionalInformationMapping).fillAndValidate(additionalInfo.copy(statementTypeCode = Some(invalidTypeCode))).fold(
              error => error.error("statementTypeCode") must haveMessage("statement type code should be less than or equal to 3 characters"),
              _     => fail("Should not succeed")
            )
          }
        }
      }
    }
  }
}