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

import domain.SummaryOfGoods
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import views.behaviours.ViewBehaviours
import views.html.components.input_text
import views.html.summary_of_goods

class SummaryOfGoodsSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(summaryOfGoodsMapping)

  def view(form: Form[_] = form): Html =
    summary_of_goods(form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()
  val messagePrefix = "summaryOfGoods"

  def getMessage(key: String): String = messages(s"$messagePrefix.$key")

  "summary_of_goods view" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "display total package input" in {

      forAll { goods: SummaryOfGoods =>

        val popForm = form.fillAndValidate(goods)
        val input   = input_text(popForm("totalPackageQuantity"), getMessage("totalPackage"))

        view(popForm) must include(input)
      }
    }

    "display gross mass input" in {

      forAll { goods: SummaryOfGoods =>

        val popForm = form.fillAndValidate(goods)
        val input   = input_text(popForm("totalGrossMassMeasure.value"), getMessage("grossMass.value"))

        view(popForm) must include(input)
      }
    }
  }
}