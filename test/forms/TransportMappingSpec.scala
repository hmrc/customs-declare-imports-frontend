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

import domain.Transport
import forms.DeclarationFormMapping._
import generators.{Generators, Lenses}
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers

class TransportMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with Lenses
  with FormMatchers {

  val form = Form(transportMapping)

  "transportMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { transport: Transport =>

          form.fillAndValidate(transport).fold(
            _ => fail("form should not fail"),
            _ mustBe transport
          )
        }
      }
    }

    "fail" when {

      "containerCode is longer than 1 digit" in {

        val badData =
          Transport.containerCode.setArbitrary(some(intOutsideRange(0, 9)))

        forAll(badData) { transport =>

          form.fillAndValidate(transport).fold(
            _ must haveErrorMessage("Container must be a single digit"),
            _ => fail("form should not succeed")
          )
        }
      }
    }
  }
}