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
import uk.gov.hmrc.wco.dec.{Commodity, Warehouse}

class CommodityMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  val form = Form(commodityMapping)

  "commodityMapping" should {

    "bind" when {

      "valid values are passed in" in {
        forAll { commodity: Commodity =>

          Form(commodityMapping).fillAndValidate(commodity).fold(
            e => fail("form should now fail"),
            _ mustBe commodity
          )
        }
      }
    }

    "fail" when {

      "description is longer than 512 characters" in {
        forAll(arbitrary[Commodity], minStringLength(513)) {
          (commodity, description) =>

            val data = commodity.copy(description = Some(description))
            Form(commodityMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Description should be less than equal to 512 characters"),
              e => fail("form should not succeed")
            )
        }
      }
    }
  }
}
