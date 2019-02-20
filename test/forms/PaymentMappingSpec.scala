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
import uk.gov.hmrc.wco.dec.{Amount, Payment}

class PaymentMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with Lenses
  with FormMatchers {

  val form = Form(paymentMapping)

  "paymentMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { payment: Payment =>

          Form(paymentMapping).fillAndValidate(payment).fold(
            e => fail(s"form should not fail: ${e.errors}"),
            success => success mustBe payment
          )
        }
      }
    }

    "fail" when {

      "Payable Tax Amount - Currency entered is not valid" in {

        val badData = stringsExceptSpecificValues(config.Options.currencyTypes.map(_._2).toSet)
        forAll(arbitrary[Payment],arbitrary[Amount], badData) {
          (payment, amount, currency) =>

            val invalidAmount = amount.copy(currencyId = Some(currency))
            val data = payment.copy(taxAssessedAmount = Some(invalidAmount))
            Form(paymentMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Total - Currency is not valid"),
              _ => fail("form should not succeed")
            )
        }
      }

      "Payable Tax Amount has a precision greater than 16" in {

        forAll(arbitrary[Payment], arbitrary[Amount], decimal(17, 30, 0)) {
          (payment, amount, deduction) =>

            val invalidAmount = amount.copy(value = Some(deduction))
            val data = payment.copy(paymentAmount = Some(invalidAmount))
            Form(paymentMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Payable Tax Amount cannot be greater than 99999999999999.99"),
              _ => fail("form should not succeed")
            )
        }
      }

      "Payable Tax Amount has a scale greater than 2" in {

        val badData = choose(3, 10).flatMap(posDecimal(16, _))

        forAll(arbitrary[Payment],arbitrary[Amount], badData) {
          (payment, amount, deduction) =>
            val invalidAmount = amount.copy(value = Some(deduction))
            val data = payment.copy(paymentAmount = Some(invalidAmount))
            Form(paymentMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Payable Tax Amount cannot have more than 2 decimal places"),
              _ => fail("form should not succeed")
            )
        }
      }

      "Payable Tax Amount is less than 0" in {

        forAll(arbitrary[Payment], arbitrary[Amount], intLessThan(0)) {
          (payment, amount, deduction) =>
            val invalidAmount = amount.copy(value = Some(BigDecimal(deduction)))
            val data = payment.copy(paymentAmount = Some(invalidAmount))
            Form(paymentMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Payable Tax Amount must not be negative"),
              _ => fail("form should not succeed")
            )
        }
      }

      "Payable Tax Amount exists but with no Currency" in {

        forAll(arbitrary[Amount]) {
          (amount) =>

          whenever(amount.value.nonEmpty) {
            Form(paymentMapping).bind(Map("paymentAmount.value" -> amount.value.fold("")(_.toString))).fold(
              _ must haveErrorMessage("Currency is required when Payable Tax Amount is provided"),
              _ => fail("form should not succeed")
            )
          }
        }
      }

        "Payable Tax Amount - Currency with no  Payable Tax Amount" in {

          forAll { amount: Amount =>

            whenever(amount.currencyId.nonEmpty) {

              Form(paymentMapping).bind(Map("paymentAmount.currencyId" -> amount.currencyId.getOrElse(""))).fold(
                _ must haveErrorMessage("Payable Tax Amount is required when Currency is provided"),
                _ => fail("form should not succeed")
              )
            }
          }
        }
    }
  }
}