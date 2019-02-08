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
import uk.gov.hmrc.wco.dec.TradeTerms

class TradeTermsFormMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  "tradeTermsMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { tradeTerms: TradeTerms =>

          Form(tradeTermsMapping).fillAndValidate(tradeTerms).fold(
            e => fail(s"form should not fail: ${e.errors}"),
            _ mustBe tradeTerms
          )
        }
      }
    }

    "fail" when {

      "conditionCode is not a conditionCode" in {

        val badData = stringsExceptSpecificValues(config.Options.incoTermCodes.map(_._2).toSet)
        forAll(arbitrary[TradeTerms], badData) {
          (tradeTerms, invalidConditionCode) =>

            val data = tradeTerms.copy(conditionCode = Some(invalidConditionCode))
            Form(tradeTermsMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Condition Code is not a valid condition code"),
              _ => fail("form should not succeed")
            )
        }
      }

      "locationId length is greater than 17" in {

        forAll(arbitrary[TradeTerms], minStringLength(18)) {
          (tradeTerms, invalidLocationId) =>

            val data = tradeTerms.copy(locationId = Some(invalidLocationId))
            Form(tradeTermsMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Location ID should be less than or equal to 17 characters"),
              _ => fail("form should not succeed")
            )
        }
      }

      "locationName is not a locationName" in {

        forAll(arbitrary[TradeTerms], minStringLength(38)) {
          (tradeTerms, invalidLocationName) =>

            val data = tradeTerms.copy(locationName = Some(invalidLocationName))
            Form(tradeTermsMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Location Name should be less than or equal to 37 characters"),
              _ => fail("form should not succeed")
            )
        }
      }
    }
  }
}
