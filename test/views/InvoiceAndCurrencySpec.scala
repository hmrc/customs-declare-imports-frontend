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

import domain.InvoiceAndCurrency
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import views.behaviours.ViewBehaviours
import views.html.components.{input_select, input_text}
import views.html.invoice_and_currency

class InvoiceAndCurrencySpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(invoiceAndCurrencyMapping)

  def view(form: Form[_] = form): Html = invoice_and_currency(form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()

  val messagePrefix = "invoiceAndCurrency"

  "Invoice and Currency view" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "display invoice currency" in {

      forAll { invoiceAndCurrency: InvoiceAndCurrency =>
        val popForm = form.fillAndValidate(invoiceAndCurrency)
        val input   = input_select(
          popForm("invoice.currencyId"),
          messages(s"$messagePrefix.invoiceCurrencyId"),
          config.Options.currencyTypes.toMap)

        view(popForm) must include(input)
      }
    }

    "display invoice amount" in {

      forAll { invoiceAndCurrency: InvoiceAndCurrency =>
        val popForm = form.fillAndValidate(invoiceAndCurrency)
        val input   = input_text(popForm("invoice.value"), messages(s"$messagePrefix.invoiceAmount"))

        view(popForm) must include(input)
      }
    }

    "display currency id" in {

      forAll { invoiceAndCurrency: InvoiceAndCurrency =>
        val popForm = form.fillAndValidate(invoiceAndCurrency)
        val input   = input_select(
          popForm("currency.currencyTypeCode"),
          messages(s"$messagePrefix.exchangeCurrencyId"),
          config.Options.currencyTypes.toMap)

        view(popForm) must include(input)
      }
    }

    "display exchange rate" in {

      forAll { invoiceAndCurrency: InvoiceAndCurrency =>
        val popForm = form.fillAndValidate(invoiceAndCurrency)
        val input   = input_text(popForm("currency.rateNumeric"), messages(s"$messagePrefix.exchangeCurrencyAmount"))

        view(popForm) must include(input)
      }
    }
  }
}
