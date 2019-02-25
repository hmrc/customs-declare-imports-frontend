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
import uk.gov.hmrc.wco.dec.ImportExportParty
import views.behaviours.ViewBehaviours
import views.html.components.{input_select, input_text}
import views.html.goodsitems.goods_items_seller_details


class GoodsItemsSellerDetailsSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(importExportPartyMapping)

  def view(form: Form[_] = form): Html = goods_items_seller_details(form)(fakeRequest, messages, appConfig)

  val simpleview: () => Html = () => view()

  val messagePrefix = "governmentAgencyGoodsItem.sellerDetails"

  "government agency goods items seller details page" must {

    behave like normalPage(simpleview, messagePrefix)
    behave like pageWithBackLink(simpleview)

    "display name field" in {

      forAll { importExportParty: ImportExportParty =>

        val popForm = Form(importExportPartyMapping).fillAndValidate(importExportParty)
        val input = input_text(popForm("name"), messages("governmentAgencyGoodsItem.sellerDetails.name"))

        view(popForm) must include(input)
      }
    }

    "display id field" in {

      forAll { importExportParty: ImportExportParty =>

        val popForm = Form(importExportPartyMapping).fillAndValidate(importExportParty)
        val input = input_text(popForm("id"), messages("governmentAgencyGoodsItem.sellerDetails.id"))

        view(popForm) must include(input)
      }
    }

    "display address line field" in {

      forAll { importExportParty: ImportExportParty =>

        val popForm = Form(importExportPartyMapping).fillAndValidate(importExportParty)
        val input = input_text(popForm("address.line"), messages("governmentAgencyGoodsItem.sellerDetails.address.line"))

        view(popForm) must include(input)
      }
    }

    "display city name field" in {

      forAll { importExportParty: ImportExportParty =>

        val popForm = Form(importExportPartyMapping).fillAndValidate(importExportParty)
        val input = input_text(popForm("address.cityName"), messages("governmentAgencyGoodsItem.sellerDetails.address.cityName"))

        view(popForm) must include(input)
      }
    }


    "display country type input" in {

      forAll { importExportParty: ImportExportParty =>

        val popForm = Form(importExportPartyMapping).fillAndValidate(importExportParty)
        val input = input_select(popForm("address.countryCode"),
          messages("governmentAgencyGoodsItem.sellerDetails.address.country"),
          config.Options.countryOptions)
        view(popForm) must include(input)
      }
    }

    "display postcode field" in {

      forAll { importExportParty: ImportExportParty =>

        val popForm = Form(importExportPartyMapping).fillAndValidate(importExportParty)
        val input = input_text(popForm("address.postcodeId"), messages("governmentAgencyGoodsItem.sellerDetails.address.postcode"))

        view(popForm) must include(input)
      }
    }

    "display communications id field" in {

      forAll { importExportParty: ImportExportParty =>

        val popForm = Form(importExportPartyMapping).fillAndValidate(importExportParty)
        val input = input_text(popForm("communications.id"), messages("governmentAgencyGoodsItem.sellerDetails.communication.id"))

        view(popForm) must include(input)
      }
    }
  }
}
