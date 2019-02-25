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

import controllers.routes
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.submit_failure

class SubmitFailureSpec extends ViewBehaviours {

  val view: () => Html = () => submit_failure()(fakeRequest, messages, appConfig)
  val messagePrefix = "submitFailure"

  "submit_failure view" should {

    behave like normalPage(view, messagePrefix, "listItem1")
    behave like pageWithBackLink(view)

    "have link to re submit" in {

      val doc = asDocument(view())
      assertContainsLink(doc, messages(s"$messagePrefix.link"), routes.SubmitController.onSubmit().url)
    }

    "have list item 2" in {

      val doc = asDocument(view())
      assertContainsText(doc, messages(s"$messagePrefix.listItem2", messages(s"$messagePrefix.startDecLink")))
    }

    "have start a declaration link" in {

      val doc = asDocument(view())
      assertContainsLink(doc, messages(s"$messagePrefix.startDecLink"), routes.LandingController.displayLandingPage().url)
    }
  }
}
