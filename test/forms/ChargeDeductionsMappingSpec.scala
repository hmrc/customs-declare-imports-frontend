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
import generators.{Generators, Lenses}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.ChargeDeduction

class ChargeDeductionsMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  val form = Form(goodsChargeDeductionMapping)
  "goodsChargeDeductionMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { charge: ChargeDeduction =>
          whenever(charge.chargesTypeCode.exists(_.length == 2)) {
            Form(goodsChargeDeductionMapping).fillAndValidate(charge).fold(
              e => fail(s"form should not fail: ${e.errors}"),
              _ mustBe charge)
          }

        }
      }
    }

    "fail" when {

      "Type is longer than 2 characters" in {

        forAll(arbitrary[ChargeDeduction], stringsLongerThan(3)) {
          (charge, typeCode) =>

            val data = charge.copy(chargesTypeCode = Some(typeCode))
            Form(goodsChargeDeductionMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Type should be 2 characters"),
              _ => fail("form should not succeed")
            )
        }
      }

      "Type has alphanumeric characters" in {

        forAll(arbitrary[ChargeDeduction], nonAlphaString) {
          (charge, typeCode) =>
          whenever(typeCode.nonEmpty) {
            val data = charge.copy(chargesTypeCode = Some(typeCode))
            Form(goodsChargeDeductionMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Type must contain only A-Z characters"),
              _ => fail("form should not succeed")
            )
          }

        }
      }

      "Type Currency and Value are missing" in {

        Form(goodsChargeDeductionMapping).bind(Map[String, String]()).fold(
          _ must haveErrorMessage("Type or Currency and Value are required"),
          _ => fail("form should not succeed")
        )
      }
    }
  }

}