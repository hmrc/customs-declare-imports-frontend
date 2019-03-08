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
import uk.gov.hmrc.wco.dec.GovernmentAgencyGoodsItem

class GoodsItemDetailsMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  val form = Form(goodsItemDetailsMapping)

  "goodsItemDetailsMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { goodsItem: GovernmentAgencyGoodsItem =>

          Form(goodsItemDetailsMapping).fillAndValidate(goodsItem).fold(
            e => fail("form should not fail"),
            _ mustBe goodsItem
          )
        }
      }
    }

    "fail" when {

      "sequenceNumeric is greater than 3 digits long" in {

        forAll(arbitrary[GovernmentAgencyGoodsItem], intGreaterThan(999)) {
          (goodsItem, invalidSeqNum) =>

            val badData = goodsItem.copy(sequenceNumeric = invalidSeqNum)

            Form(goodsItemDetailsMapping).fillAndValidate(badData).fold(

              _ must haveErrorMessage("Goods item number must contain 3 digits or less"),
              e => fail("form should not succeed")
            )
        }
      }
    }
  }
}
