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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.{GoodsLocation, GoodsLocationAddress}

class GoodsLocationMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  "goodsLocationMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { goodsLocation: GoodsLocation =>

          Form(goodsLocationMapping).fillAndValidate(goodsLocation).fold(
            e => fail("form should not fail"),
            _ mustBe goodsLocation
          )
        }
      }
    }

    "fail" when {

      "name length is greater than 35 characters" in {

        forAll(arbitrary[GoodsLocation], minStringLength(36)) {
          (goodsLocation, invalidName) =>

            val data = goodsLocation.copy(name = Some(invalidName))
            Form(goodsLocationMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Name should be less than or equal to 35 characters"),
              _ => fail("form should not succeed")
            )
        }
      }

      "id length is greater than 3 characters" in {

        forAll(arbitrary[GoodsLocation], minStringLength(36)) {
          (goodsLocation, invalidId) =>

            val data = goodsLocation.copy(id = invalidId)
            Form(goodsLocationMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("ID should be less than or equal to 3 characters"),
              _ => fail("form should not succeed")
            )
        }
      }

      "typeCode is not a valid typeCode" in {

        val badData = stringsExceptSpecificValues(config.Options.goodsLocationTypeCode.map(_._1).toSet)
        forAll(arbitrary[GoodsLocation], badData) {
          (goodsLocation, invalidTypeCode) =>

            val data = goodsLocation.copy(typeCode = Some(invalidTypeCode))
            Form(goodsLocationMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("typeCode is not a valid typeCode"),
              _ => fail("form should not succeed")
            )
        }
      }

      "address is not a valid address" in {

        val badData = minStringLength(10)
        forAll(arbitrary[GoodsLocation], arbitrary[GoodsLocationAddress], badData) {
          (goodsLocation, goodsLocationAddress, invalidPostcode) =>

            val invalidGoodsLocationAddress = goodsLocationAddress.copy(postcodeId = Some(invalidPostcode))
            val data = goodsLocation.copy(address = Some(invalidGoodsLocationAddress))
            Form(goodsLocationMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("postcodeId should be less than or equal to 9 characters"),
              e => fail("form should not succeed")
            )
        }
      }
    }
  }
}
