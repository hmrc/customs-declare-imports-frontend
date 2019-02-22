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

import domain.Cancel
import forms.DeclarationFormMapping.cancelMapping
import generators.Generators
import models.ChangeReasonCode
import org.scalatest.{MustMatchers, WordSpec}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers

class CancelMappingSpec extends WordSpec
    with MustMatchers
    with PropertyChecks
    with Generators
    with FormMatchers {

  "cancelMapping" should {
    "bind" when {
      "valid values are bound" in {
        forAll { cancel: Cancel =>

          Form(cancelMapping).fillAndValidate(cancel).fold(
            _ => fail("form should not fail"),
            success => success mustBe cancel
          )
        }
      }
    }

    "fail" when {
      "changeReasonCode is not supplied" in {
        forAll { description: String =>
          Form(cancelMapping).bind(Map("description" -> description)).fold(
            _ must haveErrorMessage("error.required"),
            _ => fail("should not succeed")
          )
        }
      }

      "changeReasonCode is not valid" in {
        forAll(arbitrary[String], stringsWithMaxLength(512)) { (changeReasonCode, description) =>
          val data = Map("changeReasonCode" -> changeReasonCode, "description" -> description)

          Form(cancelMapping).bind(data).fold(
            _ must haveErrorMessage("error.required"),
            _ => fail("should not succeed")
          )
        }
      }

      "description is not supplied" in {
        forAll { changeReasonCode: ChangeReasonCode =>
          Form(cancelMapping).bind(Map("changeReasonCode" -> changeReasonCode.toString)).fold(
            _ must haveErrorMessage("error.required"),
            _ => fail("should not succeed")
          )
        }
      }

      "description is longer than 512 chars" in {
        forAll(arbitrary[Cancel], minStringLength(513)) { (cancel, description) =>
          val data = cancel.copy(description = description)

          Form(cancelMapping).fillAndValidate(data).fold(
            _ must haveErrorMessage("Description cannot be longer than 512 characters"),
            _ => fail("should not succeed")
          )
        }
      }
    }
  }
}
