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
import play.api.http.{HeaderNames, Status}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.NoActiveSession
import uk.gov.hmrc.customs.test.{AuthenticationBehaviours, CustomsPlaySpec, FeatureSwitchBehaviours}

class BeginControllerSpec extends CustomsPlaySpec with AuthenticationBehaviours with FeatureSwitchBehaviours {

  val method = "GET"
  val uri = uriWithContextPath("/")

  s"$method $uri" should {

    "return 200" in featureScenario(Feature.begin, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri, signedInUser) { resp =>
          status(resp) must be (Status.OK)
        }
      }
    }

    "return HTML" in featureScenario(Feature.begin, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri, signedInUser) { resp =>
          contentType(resp) must be (Some("text/html"))
          charset(resp) must be (Some("utf-8"))
        }
      }
    }

    "display message" in featureScenario(Feature.begin, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri, signedInUser) { resp =>
          contentAsHtml(resp) should include element withClass("message").withValue("Well done. You have begun your first step on a long journey.")
        }
      }
    }

    // can't think of a better way to test this right now
    // the assertion here is fairly meaningless: just says we got the one thrown by notSignedInScenaro(), which is a mock
    // sadly, Play's route() test helper doesn't incorporate the ErrorHandler which would handle this
    // therefore, there is little we can assert on other than "the expected exception was thrown"
    // at least this *will* fail if the auth action is removed
    "require authentication" in featureScenario(Feature.begin, FeatureStatus.enabled) {
      notSignedInScenario() {
        val ex = intercept[NoActiveSession] {
          requestScenario(method, uri) { resp =>
            status(resp) must be (Status.SEE_OTHER)
            header(HeaderNames.LOCATION, resp) must be(Some(s"/gg/sign-in?continue=${uri}&origin=customs-declare-imports-frontend"))
          }
        }
        ex must be theSameInstanceAs(notLoggedInException)
      }
    }

    "be behind feature switch" in featureScenario(Feature.begin, FeatureStatus.disabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          status(resp) must be (Status.NOT_FOUND)
        }
      }
    }

  }

}
