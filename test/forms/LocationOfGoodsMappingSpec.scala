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

import domain.LocationOfGoods
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec._

class LocationOfGoodsMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  "locationOfGoodsMapping" should {

    "bind" when {

      "valid values are passed" in {
        forAll { locationOfGoods: LocationOfGoods =>

          Form(locationOfGoodsMapping).fillAndValidate(locationOfGoods).fold(
            e => fail("form should not fail"),
            _ mustBe locationOfGoods
          )
        }
      }
    }

    "fail" when {

      "invalid values are passed" in {

        val badData = minStringLength(18)
        forAll(arbitrary[LocationOfGoods], arbitrary[LoadingLocation], badData) {
          (locationOfGoods, loadingLocation, invalidId) =>

            val invalidLoadingLocation = loadingLocation.copy(id = Some(invalidId))
            val data = locationOfGoods.copy(loadingLocation = Some(invalidLoadingLocation))
            Form(locationOfGoodsMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("id should be less than or equal to 17 characters"),
              e => fail("form should not succeed")
            )
        }
      }
    }
  }
}
