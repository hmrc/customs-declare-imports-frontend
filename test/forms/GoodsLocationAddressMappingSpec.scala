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
import uk.gov.hmrc.wco.dec.GoodsLocationAddress

class GoodsLocationAddressMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  "goodsLocationAddressMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { goodsLocationAddress: GoodsLocationAddress =>

          Form(goodsLocationAddressMapping).fillAndValidate(goodsLocationAddress).fold(
            e => fail("form should not fail"),
            _ mustBe goodsLocationAddress
          )
        }
      }
    }

    "fail" when {

      "typeCode is not a valid typeCode" in {

        val badData = stringsExceptSpecificValues(config.Options.goodsLocationTypeCode.map(_._1).toSet)
        forAll(arbitrary[GoodsLocationAddress], badData) {
          (goodsLocationAddress, invalidTypeCode) =>

            val data = goodsLocationAddress.copy(typeCode = Some(invalidTypeCode))
            Form(goodsLocationAddressMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("typeCode is not a valid typeCode"),
              _ => fail("form should not succeed")
            )
        }
      }

      "cityName length is greater than 35 characters" in {

        forAll(arbitrary[GoodsLocationAddress], minStringLength(36)) {
          (goodsLocationAddress, invalidLocationId) =>

            val data = goodsLocationAddress.copy(cityName = Some(invalidLocationId))
            Form(goodsLocationAddressMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("cityName should be less than or equal to 35 characters"),
              _ => fail("form should not succeed")
            )
        }
      }

      "countryCode is not a valid countryCode" in {

        val badData = stringsExceptSpecificValues(config.Options.countryOptions.map(_._1).toSet)
        forAll(arbitrary[GoodsLocationAddress], badData) {
          (goodsLocationAddress, invalidCountryCode) =>

            val data = goodsLocationAddress.copy(countryCode = Some(invalidCountryCode))
            Form(goodsLocationAddressMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("countryCode is not a valid countryCode"),
              _ => fail("form should not succeed")
            )
        }
      }

      "line length is greater than 70 characters" in {

        forAll(arbitrary[GoodsLocationAddress], minStringLength(71)) {
          (goodsLocationAddress, invalidLine) =>

            val data = goodsLocationAddress.copy(line = Some(invalidLine))
            Form(goodsLocationAddressMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Line should be less than or equal to 70 characters"),
              _ => fail("form should not succeed")
            )
        }
      }

      "postcodeId length is greater than 9 characters" in {

        forAll(arbitrary[GoodsLocationAddress], minStringLength(10)) {
          (goodsLocationAddress, invalidPostcode) =>

            val data = goodsLocationAddress.copy(postcodeId = Some(invalidPostcode))
            Form(goodsLocationAddressMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("postcodeId should be less than or equal to 9 characters"),
              _ => fail("form should not succeed")
            )
        }
      }
    }
  }
}
