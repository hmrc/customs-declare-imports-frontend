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

import generators.Generators
import forms.DeclarationFormMapping._
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import uk.gov.hmrc.wco.dec.NamedEntityWithAddress
import views.behaviours.ViewBehaviours
import views.html.goods_item_consignor
import views.html.helpers.address

class GoodsItemConsignorSpec extends ViewSpecBase
  with ViewBehaviours
  with ViewMatchers
  with PropertyChecks
  with Generators {

  val form = Form(namedEntityWithAddressMapping)

  def view(form: Form[_] = form): Html =
    goods_item_consignor(form)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()
  val messagePrefix = "goods_item.consignor"

  "view" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "display all fields" in {

      forAll { entity: NamedEntityWithAddress =>

        val popForm = form.fill(entity)
        val fields  = address(popForm("address"), messagePrefix)

        view(popForm) must include(fields)
      }
    }
  }
}