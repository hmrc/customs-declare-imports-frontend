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
import uk.gov.hmrc.wco.dec.AuthorisationHolder
import views.behaviours.ViewBehaviours
import views.html.authorisation_holder
import views.html.components.input_text

class AuthorisationHolderSpec
  extends ViewBehaviours
    with ViewMatchers
    with PropertyChecks
    with Generators {

  val emptyAuthorisationHolder: Seq[AuthorisationHolder] = Seq.empty

  def view(form: Form[AuthorisationHolder] = form,
           authorisationHolder: Seq[AuthorisationHolder] = emptyAuthorisationHolder): Html =
    authorisation_holder(form, authorisationHolder)(fakeRequest, messages, appConfig)

  val view: () => Html = () => authorisation_holder(form, emptyAuthorisationHolder)(fakeRequest, messages, appConfig)

  val messagePrefix = "authorisationHolder"

  lazy val form = Form(authorisationHolderMapping)


  "Authorisation Holder Page" should {

    behave like normalPage(view, messagePrefix)

    "contain id field" in {

      val input = input_text(form("id"), "ID")
      view() must include(input)
    }

    "contain category code field" in {

      val input = input_text(form("categoryCode"), "Category Code")
      view() must include(input)
    }

    "not display authorisation holder table if authorisation holder is not available" in {

      val doc = asDocument(view(form, emptyAuthorisationHolder))

      assertContainsText(doc, messages("authorisationHolder.table.empty"))
    }

    "display authorisation holder table heading for single item if authorisation holder is available" in {

      forAll { authorisationHolder: AuthorisationHolder =>
        val authorisationHolderSeq = Seq(authorisationHolder)
        val doc = asDocument(view(form, authorisationHolderSeq))

        assertContainsText(doc, s"${authorisationHolderSeq.size} " + messages("authorisationHolder.table.heading"))
      }
    }


    "display authorisation holder table heading for multiple items if authorisation holder is available" in {

      forAll { authorisationHolder: AuthorisationHolder =>
        val authorisationHolderSeq = Seq(authorisationHolder, authorisationHolder)
        val doc = asDocument(view(form, authorisationHolderSeq))

        assertContainsText(doc, s"${authorisationHolderSeq.size} " + messages("authorisationHolder.table.multiple.heading"))
      }
    }

    "display authorisation holder id in the table when available" in {

      forAll { authorisationHolder: AuthorisationHolder =>
        val authorisationHolderSeq = Seq(authorisationHolder)
        val doc = asDocument(view(form, authorisationHolderSeq))

        authorisationHolder.id.map(assertContainsText(doc, _))
      }
    }

    "display authorisation holder category code in the table when available" in {

      forAll { authorisationHolder: AuthorisationHolder =>
        val authorisationHolderSeq = Seq(authorisationHolder)
        val doc = asDocument(view(form, authorisationHolderSeq))

        authorisationHolder.categoryCode.map(assertContainsText(doc, _))
      }
    }
  }
}