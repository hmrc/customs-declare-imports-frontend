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

import domain.LocationOfGoods
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import views.behaviours.ViewBehaviours
import views.html.components.{input_select, input_text}
import views.html.location_of_goods

class LocationOfGoodsSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(locationOfGoodsMapping)

  def view(form: Form[_] = form): Html = location_of_goods(form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()

  val messagePrefix = "locationOfGoods"

  "Location of goods view" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "display goods location name" in {

      forAll { locationOfGoods: LocationOfGoods =>
        val popForm = form.fillAndValidate(locationOfGoods)
        val input = input_text(popForm("goodsLocation.name"), messages(s"$messagePrefix.goodsLocation.name"))

        view(popForm) must include(input)
      }
    }

    "display goods location id" in {

      forAll { locationOfGoods: LocationOfGoods =>
        val popForm = form.fillAndValidate(locationOfGoods)
        val input = input_text(popForm("goodsLocation.id"), messages(s"$messagePrefix.goodsLocation.id"))

        view(popForm) must include(input)
      }
    }

    "display goods location typeCode" in {

      forAll { locationOfGoods: LocationOfGoods =>
        val popForm = form.fillAndValidate(locationOfGoods)
        val input = input_select(
          popForm("goodsLocation.typeCode"),
          messages(s"$messagePrefix.goodsLocation.typeCode"),
          config.Options.goodsLocationTypeCode)

        view(popForm) must include(input)
      }
    }

    "display goods location address line" in {

      forAll { locationOfGoods: LocationOfGoods =>
        val popForm = form.fillAndValidate(locationOfGoods)
        val input = input_text(popForm("goodsLocation.address.line"), messages(s"$messagePrefix.goodsLocationAddress.line"))

        view(popForm) must include(input)
      }
    }

    "display goods location address postcode line" in {

      forAll { locationOfGoods: LocationOfGoods =>
        val popForm = form.fillAndValidate(locationOfGoods)
        val input = input_text(popForm("goodsLocation.address.postcodeId"), messages(s"$messagePrefix.goodsLocationAddress.postcodeId"))

        view(popForm) must include(input)
      }
    }

    "display goods location address city line" in {

      forAll { locationOfGoods: LocationOfGoods =>
        val popForm = form.fillAndValidate(locationOfGoods)
        val input = input_text(popForm("goodsLocation.address.cityName"),
          messages(s"$messagePrefix.goodsLocationAddress.cityName"))

        view(popForm) must include(input)
      }
    }

    "display goods location address countryCode" in {

      forAll { locationOfGoods: LocationOfGoods =>
        val popForm = form.fillAndValidate(locationOfGoods)
        val input = input_select(
          popForm("goodsLocation.address.countryCode"),
          messages(s"$messagePrefix.goodsLocationAddress.countryCode"),
          config.Options.countryOptions)

        view(popForm) must include(input)
      }
    }

    "display goods location address typeCode" in {

      forAll { locationOfGoods: LocationOfGoods =>
        val popForm = form.fillAndValidate(locationOfGoods)
        val input = input_select(
          popForm("goodsLocation.address.typeCode"),
          messages(s"$messagePrefix.goodsLocationAddress.typeCode"),
          config.Options.goodsLocationTypeCode)

        view(popForm) must include(input)
      }
    }

    "display destination country code" in {

      forAll { locationOfGoods: LocationOfGoods =>
        val popForm = form.fillAndValidate(locationOfGoods)
        val input = input_select(
          popForm("destination.countryCode"),
          messages(s"$messagePrefix.destination.countryCode"),
          config.Options.countryOptions)

        view(popForm) must include(input)
      }
    }

    "display export country id" in {

      forAll { locationOfGoods: LocationOfGoods =>
        val popForm = form.fillAndValidate(locationOfGoods)
        val input = input_select(
          popForm("exportCountry.id"),
          messages(s"$messagePrefix.exportCountry.id"),
          config.Options.countryTypes)

        view(popForm) must include(input)
      }
    }

    "display loading location id" in {

      forAll { locationOfGoods: LocationOfGoods =>
        val popForm = form.fillAndValidate(locationOfGoods)
        val input = input_text(popForm("loadingLocation.id"), messages(s"$messagePrefix.loadingLocation.id"))

        view(popForm) must include(input)
      }
    }
  }
}
