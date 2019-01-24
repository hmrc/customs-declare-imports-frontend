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

import forms.DeclarationFormMapping.addPreviousDocumentMapping
import generators.Generators
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import views.behaviours.ViewBehaviours
import views.html.add_previous_documents
import views.html.components.input_text


class AddPreviousDocumentsSpec
  extends ViewBehaviours
    with ViewMatchers
    with PropertyChecks
    with Generators {

  lazy val form = Form(addPreviousDocumentMapping)

  val view: () => Html = () => add_previous_documents(form)(fakeRequest, messages, appConfig)

  val messagePrefix = "addPreviousDocument"

  "Previous Documents Page" should {

    behave like normalPage(view, messagePrefix)

    "contain category code field" in {

      val input = input_text(form("categoryCode"), messages("addPreviousDocument.categoryCode"))
      view() must include(input)
    }

    "contain id field" in {

      val input = input_text(form("id"), messages("addPreviousDocument.id"))
      view() must include(input)
    }

    "contain type code field" in {

      val input = input_text(form("typeCode"), messages("addPreviousDocument.typeCode"))
      view() must include(input)
    }

    "contain line numeric field" in {

      val input = input_text(form("lineNumeric"), messages("addPreviousDocument.lineNumeric"))
      view() must include(input)
    }
  }
}
