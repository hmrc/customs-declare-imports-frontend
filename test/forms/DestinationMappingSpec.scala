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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.Destination

class DestinationMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  "destinationMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { destination: Destination =>

          Form(destinationMapping).fillAndValidate(destination).fold(
            e => fail("form should not fail"),
            _ mustBe destination
          )
        }
      }
    }

    "fail" when {

      "countryCode is not a valid countryCode" in {

        val badData = stringsExceptSpecificValues(config.Options.countryOptions.map(_._1).toSet)
        forAll(arbitrary[Destination], badData) {
          (destination, invalidCountryCode) =>

            val data = destination.copy(countryCode = Some(invalidCountryCode))
            Form(destinationMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("countryCode is not a valid countryCode"),
              _ => fail("form should not succeed")
            )
        }
      }
    }
  }
}
