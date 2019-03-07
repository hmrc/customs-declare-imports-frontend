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
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import uk.gov.hmrc.wco.dec.AuthorisationHolder
import viewmodels.HtmlTable
import views.behaviours.ViewBehaviours
import views.html.authorisation_holder
import views.html.components.input_text
import views.html.components.table.table

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

      val input = input_text(form("id"), "Authorisation holder ID")
      view() must include(input)
    }

    "contain category code field" in {

      val input = input_text(form("categoryCode"), "Category code")
      view() must include(input)
    }

    "display authorisation holder table heading for single item if authorisation holder is available" in {

      forAll { authorisationHolder: AuthorisationHolder =>

        val authorisationHolderSeq = Seq(authorisationHolder)
        val doc = asDocument(view(form, authorisationHolderSeq))

        assertContainsText(doc, s"${authorisationHolderSeq.size} " + messages("authorisationHolder.table.heading"))
      }
    }


    "display authorisation holder table heading for multiple items if authorisation holder is available" in {

      forAll(listOf(arbitrary[AuthorisationHolder])) { authorisationHolders =>

        whenever(authorisationHolders.size > 1) {

          val doc = asDocument(view(form, authorisationHolders))

          assertContainsText(doc, s"${authorisationHolders.size} " + messages("authorisationHolder.table.multiple.heading"))
        }
      }
    }

    "display table for authorisation holders" in {

      forAll(listOf(arbitrary[AuthorisationHolder])) { authorisationHolders =>

        whenever(authorisationHolders.nonEmpty) {

          val htmlTable =
            HtmlTable("Authorisation holder ID", "Category code")(authorisationHolders.map(a => (a.id.getOrElse(""), a.categoryCode.getOrElse(""))))
          val tableComponent = table(htmlTable)
          val rendered = view(form, authorisationHolders)

          rendered must include(tableComponent)
        }
      }
    }
  }
}