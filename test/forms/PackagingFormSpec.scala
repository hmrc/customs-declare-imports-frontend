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

import forms.DeclarationFormMapping.packagingMapping
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.Packaging

class PackagingFormSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  "packagingMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { packaging: Packaging =>

          Form(packagingMapping).fillAndValidate(packaging).fold(
            _ => fail("form should not fail"),
            success => success mustBe packaging
          )
        }
      }
    }

    "fail" when {

      "marksNumbersId is longer than 512 characters" in {

        forAll(arbitrary[Packaging], minStringLength(513)) {
          (packaging, id) =>

            val data = packaging.copy(marksNumbersId = Some(id))
            Form(packagingMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Shipping Marks should be less than or equal to 512 characters"),
              _ => fail("should not succeed")
            )
        }
      }

      "Quantity is larger than 999999999" in {

        forAll(arbitrary[Packaging], intGreaterThan(999999999)) {
          (packaging, quantity) =>

            val data = packaging.copy(quantity = Some(quantity))
            Form(packagingMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Number of Packages must be greater than 0 and less than 99999999"),
              _ => fail("should not succeed")
            )
        }
      }

      "Quantity is less than or equal to 0" in {

        forAll(arbitrary[Packaging], intLessThan(1)) {
          (packaging, quantity) =>

            val data = packaging.copy(quantity = Some(quantity))
            Form(packagingMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Number of Packages must be greater than 0 and less than 99999999"),
              _ => fail("should not succeed")
            )
        }
      }

      "typeCode length not equal to 2" in {

        forAll(arbitrary[Packaging], minStringLength(3)) {
          (packaging, typeCode) =>
            whenever(typeCode.nonEmpty) {
              val data = packaging.copy(typeCode = Some(typeCode))
              Form(packagingMapping).fillAndValidate(data).fold(
                _ must haveErrorMessage("Type of Packages should be 2 characters"),
                _ => fail("should not succeed")
              )
            }
        }
      }

      "Shipping Marks, Number of Packages or Type for package not supplied" in {
        Form(packagingMapping).bind(Map.empty[String, String]).fold(
          _ must haveErrorMessage("You must provide Shipping Marks, Number of Packages or Type for package to be added"),
          _ => fail("should not succeed")
        )
      }
    }
  }
}
