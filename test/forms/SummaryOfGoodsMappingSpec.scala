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

import domain.SummaryOfGoods
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalacheck.Arbitrary._
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers

class SummaryOfGoodsMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  val form = Form(summaryOfGoodsMapping)

  "summaryOfGoodsMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { goods: SummaryOfGoods =>

          form.fillAndValidate(goods).fold(
            _ => fail("form should not fail"),
            _ mustBe goods
          )
        }
      }
    }

    "fail" when {

      "totalPackageQuantity is more than 99999999" in {

        forAll(arbitrary[SummaryOfGoods], intGreaterThan(99999999)) {
          (goods, quantity) =>

            val data = goods.copy(totalPackageQuantity = Some(quantity))
            form.fillAndValidate(data).fold(
              _ must haveErrorMessage("Total packages cannot be greater than 99,999,999"),
              _ => fail("form should not succeed")
            )
        }
      }

      "totalPackageQuantity is less than 0" in {

        forAll(arbitrary[SummaryOfGoods], intLessThan(0)) {
          (goods, quantity) =>

            val data = goods.copy(totalPackageQuantity = Some(quantity))
            form.fillAndValidate(data).fold(
              _ must haveErrorMessage("Total packages cannot be less than 0"),
              _ => fail("form should not succeed")
            )
        }
      }

      "totalPackageQuantity is empty" in {

        form.bind(Map[String, String]()).fold(
          _ must haveErrorMessage("Total packages is required"),
          _ => fail("form should not succeed")
        )
      }
    }
  }
}