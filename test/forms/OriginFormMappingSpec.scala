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

import forms.DeclarationFormMapping.originMapping
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.Origin

class OriginFormMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  "originMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { origin: Origin =>

          Form(originMapping).fillAndValidate(origin).fold(
            _ => fail("form should not fail"),
            success => success mustBe origin
          )
        }
      }
    }

    "fail" when {

      "country code is not valid" in {

        forAll(arbitrary[Origin], arbitrary[String]) {
          (address, countryCode) =>

            whenever(!config.Options.countryOptions.exists(_._1 == countryCode)) {

              val data = address.copy(countryCode = Some(countryCode))
              Form(originMapping).fillAndValidate(data).fold(
                _ must haveErrorMessage("Country of origin is invalid"),
                _ => fail("form should not succeed")
              )
            }
        }
      }

      "Origin is larger than 9" in {

        forAll(arbitrary[Origin], intsAboveValue(10)) {
          (origin, code) =>
            val data = origin.copy(typeCode = Some(code.toString))
            Form(originMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Origin type must be a digit and should be between 1-9"),
              _ => fail("should not succeed")
            )
        }
      }

      "Origin is below 1" in {

        forAll(arbitrary[Origin], intsBelowValue(1)) {
          (origin, code) =>

            val data = origin.copy(typeCode = Some(code.toString))
            Form(originMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Origin type must be a digit and should be between 1-9"),
              _ => fail("should not succeed")
            )
        }
      }
    }
  }
}
