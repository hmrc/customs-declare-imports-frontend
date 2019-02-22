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
import org.jsoup.nodes.Document
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import uk.gov.hmrc.customs.test.{CustomsFixtures, ViewMatchers}
import views.behaviours.ViewBehaviours
import views.html.cancel_confirmation

class CancelConfirmationSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers
  with CustomsFixtures {

  def doc: Document = asDocument(cancel_confirmation()(fakeRequest, messages, appConfig))

  "cancel confirmation" should {

    "display the correct browser title" in {
      assertEqualsValue(doc, "title", messages("cancelconfirmpage.titleAndHeading"))
    }

    "display the correct page heading" in {
      assertEqualsValue(doc, "h1", messages("cancelconfirmpage.titleAndHeading"))
    }

    "display the next steps" in {
      assertEqualsValue(doc, "h2", messages("confirmationpage.whatHappensNext"))
    }

    "display the link back to the landing page" in {
      val href = controllers.routes.LandingController.displayLandingPage().url

      assertContainsLink(doc, messages("confirmationpage.p.part2"), href)
    }

  }

}
