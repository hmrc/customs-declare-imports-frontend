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
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.BorderTransportMeans

class BorderTransportMeansMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with Lenses
  with FormMatchers {

  val form = Form(borderTransportMeansMapping)

  "borderTransportMeansMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { means: BorderTransportMeans =>

          form.fillAndValidate(means).fold(
            _ => fail("form should not fail"),
            _ mustBe means
          )
        }
      }
    }

    "fail" when {

      "modeCode is longer than 1 digit" in {

        val badData =
          BorderTransportMeans.modeCode.setArbitrary(some(intOutsideRange(0, 9)))

        forAll(badData) { means =>

          form.fillAndValidate(means).fold(
            _ must haveErrorMessage("Mode of transport at border must be a single digit"),
            _ => fail("form should not succeed")
          )
        }
      }

      "registrationNationalityCode is longer than 2 characters" in {

        val badData =
          BorderTransportMeans.registrationNationalityCode.setArbitrary(some(alphaLongerThan(2)))

        forAll(badData) { means =>

          form.fillAndValidate(means).fold(
            _ must haveErrorMessage("Nationality of active means of transport cannot be longer than 2 characters"),
            _ => fail("form should not succeed")
          )
        }
      }

      "registrationNationalityCode contains non alpha characters" in {

        val badData =
          BorderTransportMeans.registrationNationalityCode.setArbitrary(some(nonAlphaString.suchThat(_.nonEmpty)))

        forAll(badData) { means =>

          form.fillAndValidate(means).fold(
            _ must haveErrorMessage("Nationality of active means of transport can only contain letters"),
            _ => fail("form should not succeed")
          )
        }
      }
    }
  }
}
