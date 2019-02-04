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

import config.RadioOption
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import uk.gov.hmrc.wco.dec.Agent
import views.behaviours.ViewBehaviours
import views.html.components.{input_radio, input_select, input_text}
import views.html.representative_details

class RepresentativeDetailsSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(agentMapping)

  def view(form: Form[_] = form): Html =
    representative_details(form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()
  val messagePrefix = "representativeDetails"

  val statusCodes: Seq[RadioOption] = RadioOption.fromTuples(config.Options.agentFunctionCodes)

  "representative_details view" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "display name input" in {

      forAll { agent: Agent =>

        val popForm = form.fillAndValidate(agent)
        val input   = input_text(popForm("name"), messages(s"$messagePrefix.name"))
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display address line input" in {

      forAll { agent: Agent =>

        val popForm = form.fillAndValidate(agent)
        val input   = input_text(popForm("address.line"), messages(s"$messagePrefix.address.line"))
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display address city input" in {

      forAll { agent: Agent =>

        val popForm = form.fillAndValidate(agent)
        val input   = input_text(popForm("address.cityName"), messages(s"$messagePrefix.address.cityName"))
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display address country input" in {

      forAll { agent: Agent =>

        val popForm = form.fillAndValidate(agent)
        val input   = input_select(popForm("address.countryCode"), messages(s"$messagePrefix.address.country"), config.Options.countryOptions.toMap)
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display address postcode input" in {

      forAll { agent: Agent =>

        val popForm = form.fillAndValidate(agent)
        val input   = input_text(popForm("address.postcodeId"), messages(s"$messagePrefix.address.postcode"))
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display id input" in {

      forAll { agent: Agent =>

        val popForm = form.fillAndValidate(agent)
        val input   = input_text(popForm("id"), messages(s"$messagePrefix.id"))
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display status code input" in {

      forAll { agent: Agent =>

        val popForm = form.fillAndValidate(agent)
        val input   = input_radio(popForm("functionCode"), messages(s"$messagePrefix.statusCode"), inputs = statusCodes)
        val html    = view(popForm)

        html must include(input)
      }
    }
  }
}