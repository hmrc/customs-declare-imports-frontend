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
import uk.gov.hmrc.customs.test.{AuthenticationBehaviours, CustomsPlaySpec, FeatureSwitchBehaviours, WiremockBehaviours}


class GenericControllerSpec extends CustomsPlaySpec with AuthenticationBehaviours with FeatureSwitchBehaviours with WiremockBehaviours {

  val method = "GET"
  val handleMethod = "POST"
  val uri = uriWithContextPath("/submit-declaration/declarant-details")


  s"$method $uri" should {

    "return 200" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) {
          wasOk
        }
      }
    }

    "return HTML" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) {
          wasHtml
        }
      }
    }

    "require authentication" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      notSignedInScenario() {
        accessDeniedRequestScenarioTest(method, uri)
      }
    }

    "be behind a feature switch" in featureScenario(Feature.declaration, FeatureStatus.disabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) {
          wasNotFound
        }
      }
    }

    "include a text input for Declarant Name" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "DeclarantName")
        }
      }
    }

        "include a text input for Declarant Address Line" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
          signedInScenario() {
            userRequestScenario(method, uri) { resp =>
              includesHtmlInput(resp, "text", "DeclarantAddressLine")
            }
          }
        }
  }


}