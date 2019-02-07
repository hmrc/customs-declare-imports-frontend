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

import forms.DeclarationFormMapping.obligationGauranteeMapping
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.listOf
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import uk.gov.hmrc.wco.dec.ObligationGuarantee
import viewmodels.HtmlTable
import views.behaviours.ViewBehaviours
import views.html.add_guarantee_references
import views.html.components.input_text
import views.html.components.table.table

class AddGuaranteeReferencesSpec extends ViewBehaviours with ViewMatchers with PropertyChecks with Generators {

  lazy val form = Form(obligationGauranteeMapping)

  def view(form: Form[ObligationGuarantee] = form, guaranteeReferences: Seq[ObligationGuarantee] = Seq.empty): Html =
    add_guarantee_references(form, guaranteeReferences)(fakeRequest, messages, appConfig)

  val view: () => Html = () => view(form, Seq.empty)

  val messagePrefix = "addGuaranteeReferences"

  "Previous Documents Page" should {

    behave like normalPage(view, messagePrefix)

    "contain reference id field" in {

      val input = input_text(form("referenceId"), messages("addGuaranteeReferences.referenceId"))
      view() must include(input)
    }

    "contain id field" in {

      val input = input_text(form("id"), messages("addGuaranteeReferences.id"))
      view() must include(input)
    }

    "contain amount field" in {

      val input = input_text(form("amount"), messages("addGuaranteeReferences.amountAmount"))
      view() must include(input)
    }

    "contain access code field" in {

      val input = input_text(form("accessCode"), messages("addGuaranteeReferences.accessCode"))
      view() must include(input)
    }

    "contain guarantee office id field" in {

      val input = input_text(form("guaranteeOffice.id"), messages("addGuaranteeReferences.officeId"))
      view() must include(input)
    }

    "display guarantee reference table heading for single item if guarantee reference is available" in {

      forAll { obligationGuarantee: ObligationGuarantee =>
        val obligationGuaranteeSeq = Seq(obligationGuarantee)
        val doc                    = asDocument(view(form, obligationGuaranteeSeq))

        assertContainsText(doc, messages("addGuaranteeReferences.table.heading"))
      }
    }

    "display guarantee reference table heading for multiple items if guarantee references are available" in {

      forAll(listOf(arbitrary[ObligationGuarantee])) { obligationGuarantees =>
        whenever(obligationGuarantees.size > 1) {

          val doc = asDocument(view(form, obligationGuarantees))

          assertContainsText(doc, messages("addGuaranteeReferences.table.multiple.heading", {
            obligationGuarantees.size
          }))
        }
      }
    }

    "display table for guarantee references" in {

      forAll(listOf(arbitrary[ObligationGuarantee])) { obligationGuarantees =>
        whenever(obligationGuarantees.nonEmpty) {

          val tableTitle = obligationGuarantees.size match {
            case 1 => messages(s"$messagePrefix.table.heading")
            case x => messages(s"$messagePrefix.table.multiple.heading", x)
          }

          val htmlTable =
            HtmlTable(
              messages("addGuaranteeReferences.referenceId"),
              messages("addGuaranteeReferences.id"),
              messages("addGuaranteeReferences.amountAmount"),
              messages("addGuaranteeReferences.accessCode"),
              messages("addGuaranteeReferences.officeId")
            )(obligationGuarantees.map { a =>
              (a.referenceId.getOrElse(""),
               a.id.getOrElse(""),
               a.amount.getOrElse(""),
               a.accessCode.getOrElse(""),
               a.guaranteeOffice.flatMap(_.id).getOrElse(""))
            })

          val tableComponent = table(htmlTable, Some(tableTitle))
          val rendered       = view(form, obligationGuarantees)

          rendered must include(tableComponent)
        }
      }
    }
  }
}
