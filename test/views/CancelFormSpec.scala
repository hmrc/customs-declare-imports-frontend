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

import domain.Cancel
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.{CustomsFixtures, ViewMatchers}
import views.behaviours.ViewBehaviours
import views.html.components.{error_summary, input_select, input_textarea}
import views.html.cancel_form

class CancelFormSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers
  with CustomsFixtures {

  val mrn = randomString(16)
  val form: Form[Cancel] = Form(cancelMapping)

  def view(form: Form[Cancel] = form): Html =
    cancel_form(mrn, form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()

  val messagePrefix = "cancelpage"

  "cancel form" when {

    "form data is valid" should {

      "display the correct page heading" in {
        val doc = asDocument(view())
        val titleAndHeader = messages(s"$messagePrefix.titleAndHeader")

        assertEqualsValue(doc, "h1", s"$titleAndHeader $mrn")
      }

      "display the correct browser title" in {
        val doc = asDocument(view())
        val titleAndHeader = messages(s"$messagePrefix.titleAndHeader")

        assertEqualsValue(doc, "title", s"$titleAndHeader $mrn")
      }

      "display the cancellation reason code dropdown" in {
        forAll { cancel: Cancel =>

          val popForm = form.fillAndValidate(cancel)
          val select = input_select(popForm("changeReasonCode"), messages(s"$messagePrefix.cancellationCode"), Cancel.changeReasonCodes)
          val html = view(popForm)

          html must include(select)
        }
      }

      "display the description" in {
        forAll { cancel: Cancel =>

          val popForm = form.fillAndValidate(cancel)
          val textarea = input_textarea(popForm("description"), messages(s"$messagePrefix.tellUsWhy"), hint = Some(messages(s"$messagePrefix.tellUsWhyHint")), inputClass = Some("form-control-3-4"), charLimit = Some(512))
          val html = view(popForm)

          html must include(textarea)
        }
      }
    }
  }

  "form data is invalid" should {
    "display error summary" in {
      val popForm = form.bind(Map.empty[String, String])
      val errorSummary = error_summary(form.errors)
      val html = view(popForm)

      html must include(errorSummary)
    }

    "display cancellation reason code error" in {
      val popForm = form.bind(Map.empty[String, String])
      val errorMessage = popForm.errors.find(_.key == "changeReasonCode").get.message
      val doc = asDocument(view(popForm))

      assertContainsMessage(doc, errorMessage)
    }

    "display description error" in {
      val popForm = form.bind(Map.empty[String, String])
      val errorMessage = popForm.errors.find(_.key == "description").get.message
      val doc = asDocument(view(popForm))

      assertContainsMessage(doc, errorMessage)
    }
  }

}
