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

package views.components

import generators.Generators
import forms.DeclarationFormMapping._
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.{Field, Form}
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import uk.gov.hmrc.wco.dec.NamedEntityWithAddress
import views.ViewSpecBase
import views.html.components.input_text
import views.html.helpers.{address, namedEntityWithAddress}

class NamedEntityWithAddressSpec extends ViewSpecBase
  with ViewMatchers
  with PropertyChecks
  with Generators
  with OptionValues {

  val emptyForm: Form[NamedEntityWithAddress] = Form(namedEntityWithAddressMapping)
  val messageKey = "consignee"

  def view(form: Form[_], messageKey: String = messageKey): Html =
    namedEntityWithAddress(form, messageKey)(messages)

  "view" should {

    "display name field" in {

      forAll { entity: NamedEntityWithAddress =>

        val form  = emptyForm.fill(entity)
        val input = input_text(form("name"), messages(s"$messageKey.name"))

        view(form) must include(input)
      }
    }

    "display id field" in {

      forAll { entity: NamedEntityWithAddress =>

        val form  = emptyForm.fill(entity)
        val input = input_text(form("id"), messages(s"$messageKey.id"))

        view(form) must include(input)
      }
    }

    "display address fields" in {

      forAll { entity: NamedEntityWithAddress =>

        val form          = emptyForm.fill(entity)
        val addressFields = address(form("address"), messageKey)

        view(form) must include(addressFields)
      }
    }
  }
}