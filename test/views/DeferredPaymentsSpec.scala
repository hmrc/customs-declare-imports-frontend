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
import uk.gov.hmrc.wco.dec.AdditionalDocument
import viewmodels.HtmlTable
import views.behaviours.ViewBehaviours
import views.html.components.input_text
import views.html.components.table.table
import views.html.deferred_payments

class DeferredPaymentsSpec extends ViewBehaviours
  with ViewMatchers
  with PropertyChecks
  with Generators {

  val emptyAdditonalDocuments: Seq[AdditionalDocument] = Seq.empty

  def view(form: Form[AdditionalDocument] = form,
           additionalDocuments: Seq[AdditionalDocument] = emptyAdditonalDocuments): Html =
    deferred_payments(form, additionalDocuments)(fakeRequest, messages, appConfig)

  val messagePrefix = "addDeferredPayment"

  val view: () => Html = () => deferred_payments(form, emptyAdditonalDocuments)(fakeRequest, messages, appConfig)

  lazy val form = Form(additionalDocumentMapping)

  "Deferred Payments Page" should {
    behave like normalPage(view, messagePrefix)

    "contain id field" in {

      val input = input_text(form("id"), messages("addDeferredPayment.id"))
      view() must include(input)
    }

    "contain category code field" in {

      val input = input_text(form("categoryCode"), messages("addDeferredPayment.categoryCode"))
      view() must include(input)
    }

    "contain type code field" in {

      val input = input_text(form("typeCode"), messages("addDeferredPayment.typeCode"))
      view() must include(input)
    }

    "display deferred payments table heading for single item if deferred payment is available" in {

      forAll { additionalDocument: AdditionalDocument =>

        val additionalDocumentsSeq = Seq(additionalDocument)
        val doc = asDocument(view(form, additionalDocumentsSeq))

        assertContainsText(doc, s"${additionalDocumentsSeq.size} " + messages("addDeferredPayment.table.heading"))
      }
    }

    "display deferred payments table heading for multiple items if deferred payments are available" in {

      forAll(listOf(arbitrary[AdditionalDocument])) { additionalDocuments =>

        whenever(additionalDocuments.size > 1) {

          val doc = asDocument(view(form, additionalDocuments))

          assertContainsText(doc, s"${additionalDocuments.size} " + messages("addDeferredPayment.table.multiple.heading"))
        }
      }
    }

    "display table for deferred payments" in {

      forAll(listOf(arbitrary[AdditionalDocument])) { additionalDocuments =>

        whenever(additionalDocuments.nonEmpty) {

          val htmlTable =
            HtmlTable(messages("addDeferredPayment.id"),
              messages("addDeferredPayment.categoryCode"),
              messages("addDeferredPayment.typeCode"))(additionalDocuments.map(a => (a.id.getOrElse(""), a.categoryCode.getOrElse(""), a.typeCode.getOrElse(""))))
          val tableComponent = table(htmlTable)
          val rendered = view(form, additionalDocuments)

          rendered must include(tableComponent)
        }
      }
    }
  }
}
