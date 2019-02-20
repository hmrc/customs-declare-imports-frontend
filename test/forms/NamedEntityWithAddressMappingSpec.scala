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
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.{Address, NamedEntityWithAddress}

class NamedEntityWithAddressMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  "namedEntityWithAddressMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { entity: NamedEntityWithAddress =>

          Form(namedEntityWithAddressMapping).fillAndValidate(entity).fold(
            e => fail("form should not fail"),
            _ mustBe entity
          )
        }
      }
    }

    "fail" when {

      "name length is greater than 70 characters" in {

        forAll(arbitrary[NamedEntityWithAddress], minStringLength(71)) {
          (entity, invalidName) =>

            Form(namedEntityWithAddressMapping).fillAndValidate(entity.copy(name = Some(invalidName))).fold(
              _ must haveErrorMessage("Name should be less than or equal to 70 characters"),
              e => fail("form should not succeed")
            )
        }
      }

      "id length is greater than 17 characters" in {

        forAll(arbitrary[NamedEntityWithAddress], minStringLength(18)) {
          (entity, invalidId) =>

            Form(namedEntityWithAddressMapping).fillAndValidate(entity.copy(id = Some(invalidId))).fold(
              _ must haveErrorMessage("ID  should be less than or equal to 17 characters"),
              e => fail("form should not succeed")
            )
        }
      }

      "invalid address is passed" in {

        forAll(arbitrary[NamedEntityWithAddress], arbitrary[Address], minStringLength(36)) {

          (entity, address, invalidCityName) =>

            val invalidAddress = address.copy(cityName = Some(invalidCityName))
            val badData = entity.copy(address = Some(invalidAddress))

            Form(namedEntityWithAddressMapping).fillAndValidate(badData).fold(
              _ must haveErrorMessage("City name should be 35 characters or less"),
              e => fail("form should not succeed")
            )
        }
      }
    }
  }
}
