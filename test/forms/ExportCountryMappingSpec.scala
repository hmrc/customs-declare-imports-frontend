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
import uk.gov.hmrc.wco.dec.ExportCountry

class ExportCountryMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  "exportCountryMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { exportCountry: ExportCountry =>

          Form(exportCountryMapping).fillAndValidate(exportCountry).fold(
            e => fail("form should not fail"),
            _ mustBe exportCountry
          )
        }
      }
    }

    "fail" when {

      "ID is not a valid ID" in {

        val badData = stringsExceptSpecificValues(config.Options.countryTypes.map(_._1).toSet)
        forAll(arbitrary[ExportCountry], badData) {
          (exportCountry, invalidId) =>

            val data = exportCountry.copy(id = invalidId)
            Form(exportCountryMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("ID is not a valid ID"),
              _ => fail("form should not succeed")
            )
        }
      }
    }
  }
}
