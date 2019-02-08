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
import uk.gov.hmrc.wco.dec.TradeTerms
import views.behaviours.ViewBehaviours
import views.html.components.{input_select, input_text}
import views.html.delivery_terms

class DeliveryTermsSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(tradeTermsMapping)

  def view(form: Form[_] = form): Html = delivery_terms(form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()

  val messagePrefix = "deliveryTerms"

  "Delivery terms view" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "display condition code input" in {

      forAll{ tradeTerms: TradeTerms =>

        val popForm = Form(tradeTermsMapping).fillAndValidate(tradeTerms)
        val input =
          input_select(popForm("conditionCode"),
            messages("deliveryTerms.tradeTerms.conditionCode"),
            config.Options.incoTermCodes.toMap,
            hint = Some(messages("deliveryTerms.tradeTerms.conditionCode.hint")))

        view(popForm) must include(input)
      }
    }

    "display location id input" in {

      forAll{ tradeTerms: TradeTerms =>

        val popForm = Form(tradeTermsMapping).fillAndValidate(tradeTerms)
        val input =
          input_text(popForm("locationId"), messages("deliveryTerms.tradeTerms.locationId"))

        view(popForm) must include(input)
      }
    }

    "display location name input" in {

      forAll{ tradeTerms: TradeTerms =>

        val popForm = Form(tradeTermsMapping).fillAndValidate(tradeTerms)
        val input =
          input_select(
            popForm("locationName"),
            messages("deliveryTerms.tradeTerms.locationName"),
            config.Options.countryTypes.toMap
          )
      }
    }
  }
}
