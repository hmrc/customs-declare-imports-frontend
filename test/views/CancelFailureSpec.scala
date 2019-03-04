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
import org.scalatest.prop.PropertyChecks
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import views.behaviours.ViewBehaviours
import views.html.cancel_failure

class CancelFailureSpec extends ViewSpecBase
  with ViewMatchers
  with ViewBehaviours
  with PropertyChecks
  with Generators {

  def view(mrn: String): Html = cancel_failure(mrn)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view("")
  val messagePrefix = "cancelFailure"

  "cancel_failure" should {

    behave like normalPage(simpleView, messagePrefix, "listHeading", "listItem2")

    "have back link" in {

      forAll { mrn: String =>

        val doc  = asDocument(view(mrn))
        val href = controllers.routes.DeclarationController.displayCancelForm(mrn).url

        assertContainsLink(doc, messages(s"$messagePrefix.listItem1"), href)
      }
    }

    "have the third list item" in {

      val doc  = asDocument(simpleView())

      assertContainsText(doc, messages(s"$messagePrefix.listItem3", messages(s"$messagePrefix.startDecLink")))
    }

    "have start declaration link" in {

      val doc  = asDocument(simpleView())
      val href = controllers.routes.LandingController.displayLandingPage().url

      assertContainsLink(doc, messages(s"$messagePrefix.startDecLink"), href)
    }
  }
}