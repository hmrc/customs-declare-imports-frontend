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

package views

import forms.DeclarationFormMapping._
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import uk.gov.hmrc.wco.dec.ImportExportParty
import views.behaviours.ViewBehaviours
import views.html.components.{input_select, input_text}
import views.html.declarant_details

class DeclarantDetailsSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(importExportPartyMapping)

  def view(form: Form[_] = form): Html =
    declarant_details(form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()

  val messagePrefix = "declarantDetails"

  "declarant details" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "display name field" in {

      forAll { party: ImportExportParty =>

        val popForm = form.fillAndValidate(party)
        val input   = input_text(popForm("name"), "Name")
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display address line" in {

      forAll { party: ImportExportParty =>

        val popForm = form.fillAndValidate(party)
        val input   = input_text(popForm("address.line"), "Street and Number")
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display address city name" in {

      forAll { party: ImportExportParty =>

        val popForm = form.fillAndValidate(party)
        val input   = input_text(popForm("address.cityName"), "City")
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display address country code" in {

      forAll { party: ImportExportParty =>

        val popForm = form.fillAndValidate(party)
        val input   = input_select(popForm("address.countryCode"), "Country", config.Options.countryOptions.toMap)
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display address postcode" in {

      forAll { party: ImportExportParty =>

        val popForm = form.fillAndValidate(party)
        val input   = input_text(popForm("address.postcodeId"), "Postcode")
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display id" in {

      forAll { party: ImportExportParty =>

        val popForm = form.fillAndValidate(party)
        val input   = input_text(popForm("id"), "Identification No.")
        val html    = view(popForm)

        html must include(input)
      }
    }
  }
}