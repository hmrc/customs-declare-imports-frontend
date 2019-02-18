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
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.DutyTaxFee

class DutyTaxFeeMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with Lenses
  with FormMatchers {

  val form = Form(dutyTaxFeeMapping)

  "dutyTaxFeePaymentMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { taxfee: DutyTaxFee =>
          Form(dutyTaxFeeMapping).fillAndValidate(taxfee).fold(
            e => fail(s"form should not fail: ${e.errors}"),
            success => success mustBe taxfee
          )
        }
      }
    }

    "fail" when {

      "Tax Rate has a precision greater than 17" in {

        forAll(arbitrary[DutyTaxFee], decimal(18, 30, 0)) {

          (taxFee, deduction) =>
            val data = taxFee.copy(taxRateNumeric = Some(deduction))
            Form(dutyTaxFeeMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Tax Rate cannot be greater than 99999999999999.999"),
              _ => fail("form should not succeed")
            )
        }
      }

      "Tax Rate has a scale greater than 2" in {

        val badData = choose(4, 10).flatMap(posDecimal(16, _))

        forAll(arbitrary[DutyTaxFee], badData) {
          (taxFee, deduction) =>
            val data = taxFee.copy(taxRateNumeric = Some(deduction))
            Form(dutyTaxFeeMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Tax Rate cannot have more than 3 decimal places"),
              _ => fail("form should not succeed")
            )
        }
      }

      "Payable Tax Amount is less than 0" in {

        forAll(arbitrary[DutyTaxFee], intLessThan(0)) {
          (taxFee, deduction) =>
            val data = taxFee.copy(taxRateNumeric = Some(BigDecimal(deduction)))
            Form(dutyTaxFeeMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Tax Rate must not be negative"),
              _ => fail("form should not succeed")
            )
        }
      }

      "Tax Type is larger than 3 chars" in {

        forAll(arbitrary[DutyTaxFee], minStringLength(4)) {
          (taxFee, typeCode) =>
            val data = taxFee.copy(typeCode = Some(typeCode))
            Form(dutyTaxFeeMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Tax Type should be 3 characters"),
              _ => fail("form should not succeed")
            )
        }
      }
      "Tax Type is less than 3 chars" in {

        forAll(arbitrary[DutyTaxFee], stringsWithMaxLength(2)) {
          (taxFee, typeCode) =>
            val data = taxFee.copy(typeCode = Some(typeCode))
            Form(dutyTaxFeeMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Tax Type should be 3 characters"),
              _ => fail("form should not succeed")
            )
        }
      }
    }
  }
}