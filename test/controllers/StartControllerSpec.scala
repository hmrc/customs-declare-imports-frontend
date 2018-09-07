/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers

import domain.features.Feature
import uk.gov.hmrc.customs.test.assertions.{HtmlAssertions, HttpAssertions}
import uk.gov.hmrc.customs.test.behaviours.{CustomsSpec, FeatureBehaviours, RequestHandlerBehaviours}

class StartControllerSpec extends CustomsSpec
  with RequestHandlerBehaviours
  with FeatureBehaviours
  with HttpAssertions
  with HtmlAssertions {

  val method = "GET"
  val uri = uriWithContextPath("/start")

  s"$method $uri" should {

    "return 200" in withFeatures(enabled(Feature.start)) {
      withRequest(method, uri) { wasOk }
    }

    "return HTML" in withFeatures(enabled(Feature.start)) {
      withRequest(method, uri) { wasHtml }
    }

    "display 'Manage my import declarations' message" in withFeatures(enabled(Feature.start)) {
      withRequest(method, uri) { resp =>
        contentAsHtml(resp) should include element withName("h1").withValue(messages("startpage.titleAndHeading"))
      }
    }

    "include link to begin page" in withFeatures(enabled(Feature.start, Feature.landing)) {
      withRequest(method, uri) { resp =>
        contentAsHtml(resp) should include element withName("a").withClass("button-start").withAttrValue("href", routes.LandingController.displayLandingPage().url)
      }
    }

    "be behind feature switch" in withFeatures(disabled(Feature.start)) {
      withRequest(method, uri) { wasNotFound }
    }

    "include a message when begin page is not on" in withFeatures(enabled(Feature.start) ++ disabled(Feature.landing)) {
      withRequest(method, uri) { resp =>
        contentAsHtml(resp) should include element withClass("message").withValue(messages("landingpage.unavailable"))
      }
    }

  }

}
