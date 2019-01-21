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

import forms.DeclarationFormMapping.authorisationHolderMapping
import generators.Generators
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import views.behaviours.ViewBehaviours
import views.html.authorisation_holder
import views.html.components.input_text

class AuthorisationHolderSpec
  extends ViewBehaviours
    with ViewMatchers
    with PropertyChecks
    with Generators {

  val view: () => Html = () => authorisation_holder(form)(fakeRequest, messages, appConfig)

  val messagePrefix = "authorisationHolder"

  lazy val form = Form(authorisationHolderMapping)


  "Authorisation Holder Page" should {

    behave like pageWithoutHeading(view, messagePrefix)

    "have title" in {

      val doc = asDocument(view())

      assertEqualsMessage(doc, "title", s"$messagePrefix.title")
    }

    "have heading" in {

      val doc = asDocument(view())

      assertEqualsMessage(doc, "h2", s"$messagePrefix.header")
    }

    "contain id field" in {

      val input = input_text(form("id"), "ID")
      view() must include(input)
    }

    "contain category code field" in {

      val input = input_text(form("categoryCode"), "Category Code")
      view() must include(input)
    }
  }
}


/*
  val emptyAdditionalInfo: Seq[AuthorisationHolder] = Seq.empty

/*
  def view(form: Form[AuthorisationHolder] = form,
  additionalInformation: Seq[AuthorisationHolder] = emptyAdditionalInfo): Html =
    authorisation_holder(form, additionalInformation)(fakeRequest, messages, appConfig)
*/
*/

