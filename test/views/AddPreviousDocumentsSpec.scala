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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.listOf
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import uk.gov.hmrc.wco.dec.PreviousDocument
import viewmodels.HtmlTable
import views.behaviours.ViewBehaviours
import views.html.add_previous_documents
import views.html.components.input_text
import views.html.components.table.table


class AddPreviousDocumentsSpec
  extends ViewBehaviours
    with ViewMatchers
    with PropertyChecks
    with Generators {

  val emptyPreviousDocuments: Seq[PreviousDocument] = Seq.empty

  def view(form: Form[PreviousDocument] = form,
           previousDocuments: Seq[PreviousDocument] = emptyPreviousDocuments): Html =
    add_previous_documents(form, previousDocuments)(fakeRequest, messages, appConfig)
  lazy val form = Form(previousDocumentMapping)

  val view: () => Html = () => add_previous_documents(form, emptyPreviousDocuments)(fakeRequest, messages, appConfig)

  val messagePrefix = "addPreviousDocument"

  "Previous Documents Page" should {

    behave like normalPage(view, messagePrefix)

    "contain category code field" in {

      val input = input_text(form("categoryCode"), messages("addPreviousDocument.categoryCode"))
      view() must include(input)
    }

    "contain id field" in {

      val input = input_text(form("id"), messages("addPreviousDocument.id"))
      view() must include(input)
    }

    "contain type code field" in {

      val input = input_text(form("typeCode"), messages("addPreviousDocument.typeCode"))
      view() must include(input)
    }

    "contain line numeric field" in {

      val input = input_text(form("lineNumeric"), messages("addPreviousDocument.lineNumeric"))
      view() must include(input)
    }

    "not display previous documents table if previous document is not available" in {

      val doc = asDocument(view(form, emptyPreviousDocuments))

      assertContainsText(doc, messages("addPreviousDocument.table.empty"))
    }

    "display previous documents table heading for single item if previous document is available" in {

      forAll{ previousDocument: PreviousDocument =>

        val previousDocumentsSeq = Seq(previousDocument)
        val doc = asDocument(view(form, previousDocumentsSeq))

        assertContainsText(doc, s"${previousDocumentsSeq.size} " + messages("addPreviousDocument.table.heading"))
      }
    }

    "display previous documents table heading for multiple items if previous documents are available" in {

      forAll(listOf(arbitrary[PreviousDocument])) { previousDocuments =>

        whenever(previousDocuments.size > 1) {

          val doc = asDocument(view(form, previousDocuments))

          assertContainsText(doc, s"${previousDocuments.size} " + messages("addPreviousDocument.table.multiple.heading"))
        }
      }
    }

    "display table for previous documents" in {

      forAll(listOf(arbitrary[PreviousDocument])) { previousDocuments =>

        whenever(previousDocuments.nonEmpty) {

          val htmlTable =
            HtmlTable(messages("addPreviousDocument.categoryCode"),
              messages("addPreviousDocument.id"),
              messages("addPreviousDocument.typeCode"),
              messages("addPreviousDocument.lineNumeric"))(previousDocuments.map(a => (a.categoryCode.getOrElse(""),
              a.id.getOrElse(""),
              a.typeCode.getOrElse(""),
              a.lineNumeric.getOrElse(""))))
          val tableComponent = table(htmlTable)
          val rendered = view(form, previousDocuments)

          rendered must include(tableComponent)
        }
      }
    }
  }
}
