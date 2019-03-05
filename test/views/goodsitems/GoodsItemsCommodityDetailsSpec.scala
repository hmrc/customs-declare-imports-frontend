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

package views.goodsitems

import forms.DeclarationFormMapping._
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import uk.gov.hmrc.wco.dec.Commodity
import views.behaviours.ViewBehaviours
import views.html.components.{input_select, input_text}
import views.html.goodsitems.goods_items_commodity_details

class GoodsItemsCommodityDetailsSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(commodityMapping)

  def view(form: Form[_] = form): Html = goods_items_commodity_details(form)(fakeRequest, messages, appConfig)

  val simpleview: () => Html = () => view()

  val messagePrefix = "governmentAgencyGoodsItem.commodityDetails"

  "government agency goods items commodity details page" must {

    behave like normalPage(simpleview, messagePrefix)
    behave like pageWithBackLink(simpleview)

    "display currency id field" in {

      forAll { commodity: Commodity =>

        val popForm = Form(commodityMapping).fillAndValidate(commodity)
        val input = input_select(popForm("invoiceLine.itemChargeAmount.currencyId"),
          messages("governmentAgencyGoodsItem.commodityDetails.currencyId"),
          config.Options.currencyTypes)

        view(popForm) must include(input)
      }
    }

    "display currency amount input" in {
      forAll { commodity: Commodity =>

        val popForm = Form(commodityMapping).fillAndValidate(commodity)
        val input = input_text(popForm("invoiceLine.itemChargeAmount.value"),
          messages("governmentAgencyGoodsItem.commodityDetails.value"))

        view(popForm) must include(input)
      }
    }

    "display net weight measure input" in {
      forAll { commodity: Commodity =>

        val popForm = Form(commodityMapping).fillAndValidate(commodity)
        val input = input_text(popForm("goodsMeasure.netWeightMeasure.value"),
          messages("governmentAgencyGoodsItem.commodityDetails.netWeightMeasure"))

        view(popForm) must include(input)
      }
    }

    "display tariff quantity input" in {
      forAll { commodity: Commodity =>

        val popForm = Form(commodityMapping).fillAndValidate(commodity)
        val input = input_text(popForm("goodsMeasure.tariffQuantity.value"),
          messages("governmentAgencyGoodsItem.commodityDetails.tariffQuantity"))

        view(popForm) must include(input)
      }
    }

    "display gross mass measure input" in {
      forAll { commodity: Commodity =>

        val popForm = Form(commodityMapping).fillAndValidate(commodity)
        val input = input_text(popForm("goodsMeasure.grossMassMeasure.value"),
          messages("governmentAgencyGoodsItem.commodityDetails.grossMassMeasure"))

        view(popForm) must include(input)
      }
    }

    "display description input" in {
      forAll { commodity: Commodity =>

        val popForm = Form(commodityMapping).fillAndValidate(commodity)
        val input = input_text(popForm("description"),
          messages("governmentAgencyGoodsItem.commodityDetails.description"))

        view(popForm) must include(input)
      }
    }
  }
}
