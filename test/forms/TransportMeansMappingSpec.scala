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
import org.scalacheck.Gen._
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.TransportMeans

class TransportMeansMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with Lenses
  with FormMatchers {

  val form = Form(transportMeansMapping)

  "transportMeansMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { means: TransportMeans =>

          form.fillAndValidate(means).fold(
            _ => fail("form should not fail"),
            _ mustBe means
          )
        }
      }
    }

    "fail" when {

      "modeCode is longer than 1 character" in {

        val badData =
          TransportMeans.modeCode.setArbitrary(some(intOutsideRange(0, 9)))

        forAll(badData) { means =>

          form.fillAndValidate(means).fold(
            _ must haveErrorMessage("Mode of transport at border must be a single digit"),
            _ => fail("form should not succeed")
          )
        }
      }

      "identificationTypeCode is longer than 2 digits" in {

        val badData =
          TransportMeans.identificationTypeCode.setArbitrary(some(numStrLongerThan(2)))

        forAll(badData) { means =>

          form.fillAndValidate(means).fold(
            _ must haveErrorMessage("Type of identification cannot be longer than 2 digits"),
            _ => fail("form should not succeed")
          )
        }
      }

      "identificationTypeCode is non numeric" in {

        val badData =
          TransportMeans.identificationTypeCode.setArbitrary(some(alphaLessThan(2)))

        forAll(badData) { means =>

          form.fillAndValidate(means).fold(
            _ must haveErrorMessage("Type of identification must be a number"),
            _ => fail("form should not succeed")
          )
        }
      }

      "id is longer than 35 characters" in {

        val badData =
          TransportMeans.id.setArbitrary(some(minStringLength(36)))

        forAll(badData) { means =>

          form.fillAndValidate(means).fold(
            _ must haveErrorMessage("ID No. cannot be longer than 35 characters"),
            _ => fail("form should not succeed")
          )
        }
      }
    }
  }
}