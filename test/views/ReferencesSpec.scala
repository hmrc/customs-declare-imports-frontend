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

import domain.References
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import views.behaviours.ViewBehaviours
import views.html.components.input_text
import views.html.references

class ReferencesSpec extends ViewBehaviours with PropertyChecks with Generators with OptionValues with ViewMatchers {

  val form = Form(referencesMapping)

  def view(form: Form[_] = form): Html = references(form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()
  val messagePrefix          = "references"

  "references view" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "display typeCode input" in {

      forAll { references: References =>
        val popForm = form.fillAndValidate(references)
        val input   = input_text(popForm("typeCode"), messages(s"$messagePrefix.typeCode"))
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display typerCode input" in {

      forAll { references: References =>
        val popForm = form.fillAndValidate(references)
        val input   = input_text(popForm("typerCode"), messages(s"$messagePrefix.typerCode"))
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display traderAssignedReferenceId input" in {

      forAll { references: References =>
        val popForm = form.fillAndValidate(references)
        val input =
          input_text(popForm("traderAssignedReferenceId"), messages(s"$messagePrefix.traderAssignedReferenceId"))
        val html = view(popForm)

        html must include(input)
      }
    }

    "display functionalReferenceId input" in {

      forAll { references: References =>
        val popForm = form.fillAndValidate(references)
        val input   = input_text(popForm("functionalReferenceId"), messages(s"$messagePrefix.functionalReferenceId"))
        val html    = view(popForm)

        html must include(input)
      }
    }

    "display transactionNatureCode input" in {

      forAll { references: References =>
        val popForm = form.fillAndValidate(references)
        val input   = input_text(popForm("transactionNatureCode"), messages(s"$messagePrefix.transactionNatureCode"))
        val html    = view(popForm)

        html must include(input)
      }
    }
  }
}
