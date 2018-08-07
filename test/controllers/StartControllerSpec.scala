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

import domain.features.{Feature, FeatureStatus}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.{CustomsPlaySpec, FeatureSwitchBehaviours}

class StartControllerSpec extends CustomsPlaySpec with FeatureSwitchBehaviours {

  val method = "GET"
  val uri = uriWithContextPath("/start")

  s"$method $uri" should {

    "return 200" in featureScenario(Feature.start, FeatureStatus.enabled) {
      requestScenario(method, uri) { wasOk }
    }

    "return HTML" in featureScenario(Feature.start, FeatureStatus.enabled) {
      requestScenario(method, uri) { wasHtml }
    }

    "display 'hello world' message" in featureScenario(Feature.start, FeatureStatus.enabled) {
      requestScenario(method, uri) { resp =>
        contentAsHtml(resp) should include element withName("h1").withValue("Hello from customs-declare-imports-frontend !")
      }
    }

    "include link to begin page" in featureScenario(Seq(Feature.start, Feature.begin), FeatureStatus.enabled) {
      requestScenario(method, uri) { resp =>
        contentAsHtml(resp) should include element withName("a").withClass("button-start").withAttrValue("href", routes.LandingController.displayLandingPage().url)
      }
    }

    "be behind feature switch" in featureScenario(Feature.start, FeatureStatus.disabled) {
      requestScenario(method, uri) { wasNotFound }
    }

    "include a message when begin page is not on" in featureScenario(Map(Feature.start -> FeatureStatus.enabled, Feature.begin -> FeatureStatus.disabled)) {
      requestScenario(method, uri) { resp =>
        contentAsHtml(resp) should include element withClass("message").withValue("Sorry, you cannot begin today.")
      }
    }

  }

}
