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

import domain.WarehouseAndCustoms
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import uk.gov.hmrc.wco.dec.TradeTerms
import views.behaviours.ViewBehaviours
import views.html.components.{input_select, input_text}
import views.html.{delivery_terms, warehouse_and_customs_offices}

class WarehouseAndCustomsOfficesSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(warehouseAndCustomsMapping)

  def view(form: Form[_] = form): Html = warehouse_and_customs_offices(form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()

  val messagePrefix = "warehouseAndCustomsOffices"

  val messageKeys = List("h2.warehouseDetails", "h2.customsOfficeDetails")

  "warehouse and customs page" must {

    behave like normalPage(simpleView, messagePrefix, messageKeys: _*)
    behave like pageWithBackLink(simpleView)

    "display warehouse type input" in {

      forAll { warehouseAndCustoms: WarehouseAndCustoms =>

        val popForm = Form(warehouseAndCustomsMapping).fillAndValidate(warehouseAndCustoms)
        val input = input_select(popForm("warehouse.typeCode"),
          messages("warehouseAndCustomsOffices.warehouse.typeCode"),
          config.Options.customsWareHouseTypes)

        view(popForm) must include(input)
      }
    }

    "display warehouse id input" in {

      forAll { warehouseAndCustoms: WarehouseAndCustoms =>

        val popForm = Form(warehouseAndCustomsMapping).fillAndValidate(warehouseAndCustoms)
        val input = input_text(popForm("warehouse.id"),
          messages("warehouseAndCustomsOffices.warehouse.id"))

        view(popForm) must include(input)
      }
    }

    "display presentation office input" in {

      forAll { warehouseAndCustoms: WarehouseAndCustoms =>

        val popForm = Form(warehouseAndCustomsMapping).fillAndValidate(warehouseAndCustoms)
        val input = input_select(popForm("presentationOffice.id"),
          messages("warehouseAndCustomsOffices.presentationOffice"),
          config.Options.supervisingCustomsOffices)

        view(popForm) must include(input)
      }
    }

    "display supervising office input" in {

      forAll {  warehouseAndCustoms: WarehouseAndCustoms =>

        val popForm = Form(warehouseAndCustomsMapping).fillAndValidate(warehouseAndCustoms)
        val input = input_select(popForm("supervisingOffice.id"),
          messages("warehouseAndCustomsOffices.supervisingOffice"),
          config.Options.supervisingCustomsOffices)

        view(popForm) must include(input)
      }
    }
  }
}
