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
import uk.gov.hmrc.wco.dec.GovernmentAgencyGoodsItem
import views.behaviours.ViewBehaviours
import views.html.components.{input_select, input_text}
import views.html.goodsitems.goods_items_details

class GoodsItemsDetailsSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(goodsItemDetailsMapping)

  def view(form: Form[_] = form): Html = goods_items_details(form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()

  val messagePrefix = "governmentAgencyGoodsItem.goodsItemDetails"

  "government agency goods item details page" must {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "display sequence number input" in {

      forAll { goodsItem: GovernmentAgencyGoodsItem =>

        val popForm = Form(goodsItemDetailsMapping).fillAndValidate(goodsItem)
        val input = input_text(
          popForm("sequenceNumeric"), messages("governmentAgencyGoodsItem.goodsItemDetails.sequenceNumeric")
        )
        view(popForm) must include(input)
      }
    }

    "display traderAssignedReferenceID field" in {

      forAll { goodsItem: GovernmentAgencyGoodsItem =>

        val popForm = Form(goodsItemDetailsMapping).fillAndValidate(goodsItem)
        val input = input_text(
          popForm("ucr.traderAssignedReferenceId"), messages("governmentAgencyGoodsItem.goodsItemDetails.traderAssignedReferenceId")
        )
        view(popForm) must include(input)
      }
    }

    "display additionCode field" in {

      forAll { goodsItem: GovernmentAgencyGoodsItem =>

        val popForm = Form(goodsItemDetailsMapping).fillAndValidate(goodsItem)
        val input = input_text(
          popForm("valuationAdjustment.additionCode"), messages("governmentAgencyGoodsItem.goodsItemDetails.additionCode")
        )
        view(popForm) must include(input)
      }
    }

    "display countryCode field" in {

      forAll { goodsItem: GovernmentAgencyGoodsItem =>

        val popForm = Form(goodsItemDetailsMapping).fillAndValidate(goodsItem)

        val input = input_select(
          popForm("destination.countryCode"),
          messages("governmentAgencyGoodsItem.goodsItemDetails.countryCode"),
          config.Options.countryOptions
        )
        view(popForm) must include(input)
      }
    }

    "display exportCountry id field" in {

      forAll { goodsItem: GovernmentAgencyGoodsItem =>

        val popForm = Form(goodsItemDetailsMapping).fillAndValidate(goodsItem)

        val input = input_select(
          popForm("exportCountry.id"),
          messages("governmentAgencyGoodsItem.goodsItemDetails.exportCountry"),
          config.Options.countryOptions
        )
        view(popForm) must include(input)
      }
    }

    "display methodCode field" in {

      forAll { goodsItem: GovernmentAgencyGoodsItem =>

        val popForm = Form(goodsItemDetailsMapping).fillAndValidate(goodsItem)
        val input = input_text(
          popForm("customsValuation.methodCode"), messages("governmentAgencyGoodsItem.goodsItemDetails.methodCode")
        )
        view(popForm) must include(input)
      }
    }

    "display transactionNatureCode field" in {

      forAll { goodsItem: GovernmentAgencyGoodsItem =>

        val popForm = Form(goodsItemDetailsMapping).fillAndValidate(goodsItem)
        val input = input_text(
          popForm("transactionNatureCode"), messages("governmentAgencyGoodsItem.goodsItemDetails.transactionNatureCode")
        )
        view(popForm) must include(input)
      }
    }

    "display valueAmount field" in {

      forAll { goodsItem: GovernmentAgencyGoodsItem =>

        val popForm = Form(goodsItemDetailsMapping).fillAndValidate(goodsItem)
        val input = input_text(
          popForm("statisticalValueAmount.value"), messages("governmentAgencyGoodsItem.goodsItemDetails.value")
        )
        view(popForm) must include(input)
      }
    }

    "display valueAmountCurrencyId field" in {

      forAll { goodsItem: GovernmentAgencyGoodsItem =>

        val popForm = Form(goodsItemDetailsMapping).fillAndValidate(goodsItem)

        val input = input_select(
          popForm("statisticalValueAmount.currencyId"),
          messages("governmentAgencyGoodsItem.goodsItemDetails.currencyId"),
          config.Options.currencyTypes
        )
        view(popForm) must include(input)
      }
    }
  }
}
