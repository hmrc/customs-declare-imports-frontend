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
import uk.gov.hmrc.customs.test.{AuthenticationBehaviours, CustomsPlaySpec, FeatureSwitchBehaviours}

class BeginControllerSpec extends CustomsPlaySpec with AuthenticationBehaviours with FeatureSwitchBehaviours {

  val method = "GET"
  val uri = uriWithContextPath("/")

  s"$method $uri" should {

    "return 200" in featureScenario(Feature.begin, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri, signedInUser) { wasOk }
      }
    }

    "return HTML" in featureScenario(Feature.begin, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri, signedInUser) { wasHtml }
      }
    }

    "display message" in featureScenario(Feature.begin, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri, signedInUser) { resp =>
          contentAsHtml(resp) should include element withClass("message").withValue("Well done. You have begun your first step on a long journey.")
        }
      }
    }

    "require authentication" in featureScenario(Feature.begin, FeatureStatus.enabled) {
      notSignedInScenario() {
        accessDeniedRequestScenarioTest(method, uri)
      }
    }

    "be behind feature switch" in featureScenario(Feature.begin, FeatureStatus.disabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { wasNotFound }
      }
    }

  }

}
