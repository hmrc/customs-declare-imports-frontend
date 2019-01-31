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
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import uk.gov.hmrc.wco.dec.ObligationGuarantee
import viewmodels.HtmlTable
import views.behaviours.ViewBehaviours
import views.html.components.input_text
import views.html.components.table.table
import views.html.guarantee_type

class GuaranteeTypeSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(guaranteeTypeMapping)

  def view(form: Form[_] = form, details: Seq[ObligationGuarantee] = Seq.empty): Html =
    guarantee_type(form, details)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()
  val messagePrefix = "guaranteeType"

  "Security details code view" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "render security details code input" in {

      forAll { details: SecurityDetailsCode =>

        val popForm = form.fillAndValidate(details.value)
        val input   = input_text(popForm("securityDetailsCode"), "Security details code")
        val html    = view(popForm)

        html must include(input)
      }
    }

    "render table of all security details codes" in {

      val dataGen = listOf(arbitrary[SecurityDetailsCode]).map(_.map(_.value))

      forAll(dataGen) { details =>

        whenever(details.nonEmpty) {

          val tableTitle = details.size match {
            case 1 => messages(s"$messagePrefix.table.heading")
            case x => messages(s"$messagePrefix.table.multiple.heading", x)
          }

          val htmlTable = HtmlTable("Security details code")(details.map(_.securityDetailsCode))
          val expected = table(htmlTable, Some(tableTitle))
          val html = view(details = details)

          html must include(expected)
        }
      }
    }
  }
}