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

import domain.Transport
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import views.behaviours.ViewBehaviours
import views.html.components.{input_select, input_text}
import views.html.transport

class TransportSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(transportMapping)

  def view(form: Form[_] = form): Html = transport(form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()
  val messagePrefix = "transport"

  def getMessage(key: String): String = messages(s"$messagePrefix.$key")

  "transport view" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "display container code input" in {

      forAll { transport: Transport =>

        val popForm = form.fillAndValidate(transport)
        val input   = input_text(popForm("containerCode"), getMessage("containerCode"))

        view(popForm) must include(input)
      }
    }

    "display border transport means mode code input" in {

      forAll { transport: Transport =>

        val popForm = form.fillAndValidate(transport)
        val input   =
          input_select(
            popForm("borderTransportMeans.modeCode"),
            getMessage("borderTransportMeans.modeCode"),
            config.Options.transportModeTypes.toMap
          )

        view(popForm) must include(input)
      }
    }

    "display arrival transport means mode code input" in {

      forAll { transport: Transport =>

        val popForm = form.fillAndValidate(transport)
        val input   =
          input_select(
            popForm("arrivalTransportMeans.modeCode"),
            getMessage("arrivalTransportMeans.modeCode"),
            config.Options.transportModeTypes.toMap
          )

        view(popForm) must include(input)
      }
    }

    "display arrival transport means id type code input" in {

      forAll { transport: Transport =>

        val popForm = form.fillAndValidate(transport)
        val input   =
          input_select(
            popForm("arrivalTransportMeans.identificationTypeCode"),
            getMessage("arrivalTransportMeans.identificationTypeCode"),
            config.Options.transportMeansIdentificationTypes.toMap
          )

        view(popForm) must include(input)
      }
    }

    "display arrival transport mean id input" in {

      forAll { transport: Transport =>

        val popForm = form.fillAndValidate(transport)
        val input   = input_text(popForm("arrivalTransportMeans.id"), getMessage("arrivalTransportMeans.id"))

        view(popForm) must include(input)
      }
    }

    "display border transport means nationality code input" in {

      forAll { transport: Transport =>

        val popForm = form.fillAndValidate(transport)
        val input   =
          input_select(
            popForm("borderTransportMeans.registrationNationalityCode"),
            getMessage("borderTransportMeans.registrationNationalityCode"),
            config.Options.countryOptions.toMap
          )

        view(popForm) must include(input)
      }
    }
  }
}