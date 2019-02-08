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
import uk.gov.hmrc.wco.dec.Warehouse

class WarehouseFormMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  "warehouseMapping" should {

    "bind" when {

      "valid values are passed" in {
        forAll { warehouse: Warehouse =>

          Form(warehouseMapping).fillAndValidate(warehouse).fold(
            e => fail(s"form should not fail: ${e.errors}"),
            _ mustBe warehouse
          )
        }
      }
    }

    "fail" when {

      "id length is greater than 35" in {
        forAll(arbitrary[Warehouse], minStringLength(36)) {
          (warehouse, invalidId) =>

            val data = warehouse.copy(id = Some(invalidId))
            Form(warehouseMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("ID should be less than or equal to 35 characters"),
              e => fail("form should not succed")
            )
        }
      }

      "typeCode is not a typeCode" in {

        val badData = stringsExceptSpecificValues(config.Options.customsWareHouseTypes.map(_._2).toSet)
        forAll(arbitrary[Warehouse], badData) {
          (warehouse, invalidTypeCode) =>

            val data = warehouse.copy(typeCode = invalidTypeCode)
            Form(warehouseMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Type Code is not a valid type code"),
              e => fail("form should not succeed")
            )
        }
      }
    }
  }
}
