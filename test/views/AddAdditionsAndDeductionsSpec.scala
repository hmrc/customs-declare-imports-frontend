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
import uk.gov.hmrc.wco.dec.ChargeDeduction
import viewmodels.HtmlTable
import views.behaviours.ViewBehaviours
import views.html.add_additions_and_deductions
import views.html.components.{input_select, input_text}
import views.html.components.table.table

class AddAdditionsAndDeductionsSpec extends ViewBehaviours
  with ViewMatchers
  with PropertyChecks
  with Generators
  with OptionValues {

  val form = Form(chargeDeductionMapping)

  def view(form: Form[ChargeDeduction] = form, charges: Seq[ChargeDeduction] = Seq.empty): Html =
    add_additions_and_deductions(form, charges)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()
  val listView: Seq[ChargeDeduction] => Html = xs => view(charges = xs)

  val messagePrefix = "addAdditionsAndDeductions"

  def tableHeader(chargesCount: Int): String = chargesCount match {
    case 1 => messages(s"$messagePrefix.table.heading")
    case _ => messages(s"$messagePrefix.table.multiple.heading", chargesCount)
  }

  "Add additions and deductions page" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "contain type code field" in {

      forAll(arbitrary[ChargeDeduction], listOf(arbitrary[ChargeDeduction])) {
        (charge, data) =>

          val popForm = form.fillAndValidate(charge)
          val html = view(popForm, data)
          html must include(input_text(popForm("chargesTypeCode"), "Type"))
      }
    }

    "contain currency id field" in {

      forAll(arbitrary[ChargeDeduction], listOf(arbitrary[ChargeDeduction])) {
        (charge, data) =>

          val popForm = form.fillAndValidate(charge)
          val html = view(popForm, data)
          val input =
            input_select(popForm("otherChargeDeductionAmount.currencyId"), "Currency", config.Options.currencyTypes.toMap)

          html must include(input)
      }
    }

    "contain value field" in {

      forAll(arbitrary[ChargeDeduction], listOf(arbitrary[ChargeDeduction])) {
        (charge, data) =>

          val popForm = form.fillAndValidate(charge)
          val html = view(popForm, data)
          html must include(input_text(popForm("otherChargeDeductionAmount.value"), "Value"))
      }
    }

    "contain table of data" in {

      forAll(listOf(arbitrary[ChargeDeduction])) { data =>

        val htmlTable = HtmlTable("Type", "Currency", "Value")(
          data.map(c => (
            c.chargesTypeCode.getOrElse(""),
            c.otherChargeDeductionAmount.flatMap(_.currencyId).getOrElse(""),
            c.otherChargeDeductionAmount.flatMap(_.value).getOrElse("")))
        )

        val html = view(charges = data)

        html must include(table(htmlTable, Some(tableHeader(data.length))))
      }
    }
  }
}