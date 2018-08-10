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
import uk.gov.hmrc.customs.test.{AuthenticationBehaviours, CustomsPlaySpec, FeatureSwitchBehaviours, WiremockBehaviours}

class DeclarationControllerSpec
  extends CustomsPlaySpec
    with AuthenticationBehaviours
    with FeatureSwitchBehaviours
    with WiremockBehaviours {

  val get = "GET"
  val post = "POST"
  val submitUri = uriWithContextPath("/declaration")
  val cancelUri = uriWithContextPath("/cancel-declaration")

  s"$get $submitUri" should {

    "return 200" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(get, submitUri, signedInUser) {
          wasOk
        }
      }
    }

    "return HTML" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(get, submitUri, signedInUser) {
          wasHtml
        }
      }
    }

    "require authentication" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      notSignedInScenario() {
        accessDeniedRequestScenarioTest(get, submitUri)
      }
    }

    "be behind a feature switch" in featureScenario(Feature.declaration, FeatureStatus.disabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(get, submitUri, signedInUser) {
          wasNotFound
        }
      }
    }

  }

  s"$get $cancelUri" should {

    "return 200" in featureScenario(Feature.cancel, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(get, cancelUri, signedInUser) {
          wasOk
        }
      }
    }

    "return HTML" in featureScenario(Feature.cancel, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(get, cancelUri, signedInUser) {
          wasHtml
        }
      }
    }

    "require authentication" in featureScenario(Feature.cancel, FeatureStatus.enabled) {
      notSignedInScenario() {
        accessDeniedRequestScenarioTest(get, cancelUri)
      }
    }

    "be behind a feature switch" in featureScenario(Feature.cancel, FeatureStatus.disabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(get, cancelUri, signedInUser) {
          wasNotFound
        }
      }
    }

  }

}
