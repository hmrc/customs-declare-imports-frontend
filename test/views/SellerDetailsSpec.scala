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
import views.html.seller_details

class SellerDetailsSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(importExportPartyMapping)

  def view(form: Form[_] = form): Html =
    seller_details(form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()
  val messagePrefix = "sellerDetails"

  def getMessage(key: String): String = messages(s"$messagePrefix.$key")

  "seller_details view" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "display name input" in {

      forAll { seller: ImportExportParty =>

        val popForm = form.fillAndValidate(seller)
        val input   = input_text(popForm("name"), getMessage("name"))

        view(popForm) must include(input)
      }
    }

    "display address line input" in {

      forAll { seller: ImportExportParty =>

        val popForm = form.fillAndValidate(seller)
        val input   = input_text(popForm("address.line"), getMessage("address.line"))

        view(popForm) must include(input)
      }
    }

    "display address city name input" in {

      forAll { seller: ImportExportParty =>

        val popForm = form.fillAndValidate(seller)
        val input   = input_text(popForm("address.cityName"), getMessage("address.city"))

        view(popForm) must include(input)
      }
    }

    "display address country input" in {

      forAll { seller: ImportExportParty =>

        val popForm = form.fillAndValidate(seller)
        val input   =
          input_select(popForm("address.countryCode"), getMessage("address.country"), config.Options.countryOptions)

        view(popForm) must include(input)
      }
    }

    "display address postcode input" in {

      forAll { seller: ImportExportParty =>

        val popForm = form.fillAndValidate(seller)
        val input   = input_text(popForm("address.postcodeId"), getMessage("address.postcode"))

        view(popForm) must include(input)
      }
    }

    "display communication phone number input" in {

      forAll { seller: ImportExportParty =>

        val popForm = form.fillAndValidate(seller)
        val input   = input_text(popForm("communications[0].id"), getMessage("communication.id"))

        view(popForm) must include(input)
      }
    }

    "display id input" in {

      forAll { seller: ImportExportParty =>

        val popForm = form.fillAndValidate(seller)
        val input   = input_text(
          popForm("id"),
          getMessage("id"),
          hint = Some(messages("common.hints.eori")))

        view(popForm) must include(input)
      }
    }
  }
}