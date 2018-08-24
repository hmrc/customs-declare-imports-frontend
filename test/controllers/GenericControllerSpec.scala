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
import uk.gov.hmrc.customs.test.{AuthenticationBehaviours, CustomsPlaySpec, FeatureSwitchBehaviours}
import uk.gov.hmrc.http.HeaderCarrier

class GenericControllerSpec extends CustomsPlaySpec with AuthenticationBehaviours with FeatureSwitchBehaviours {

  val method = "GET"
  val handleMethod = "POST"
  val uri = uriWithContextPath("/submit-declaration/declarant-details")
  val submitUri = uriWithContextPath("/submit-declaration/declarant-details/references")

  s"$method $uri" should {

    "return 200" in featureScenario(Feature.prototype, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) {
          wasOk
        }
      }
    }

    "return HTML" in featureScenario(Feature.prototype, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) {
          wasHtml
        }
      }
    }

    "require authentication" in featureScenario(Feature.prototype, FeatureStatus.enabled) {
      notSignedInScenario() {
        accessDeniedRequestScenarioTest(method, uri)
      }
    }

    "be behind a feature switch" in featureScenario(Feature.prototype, FeatureStatus.disabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) {
          wasNotFound
        }
      }
    }

  }

  s"$handleMethod $uri" should {
    val payload =
      Map("MetaData_declaration_declarant_name" -> "name1",
        "MetaData_declaration_declarant_address_line" -> "Address1", "MetaData_declaration_declarant_id" -> "12345678912341234")
    implicit val hc = HeaderCarrier()

    "return 303" in featureScenario(Feature.prototype, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(handleMethod, submitUri, signedInUser, Map.empty, payload) {
          wasRedirected
        }
      }
    }
    val errorsPayload =
      Map("MetaData_declaration_declarant_name" -> "name1",
        "MetaData_declaration_declarant_address_line" -> "Address1", "MetaData_declaration_declarant_id" -> "41234")

    "return to same page with errors" in featureScenario(Feature.prototype, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(handleMethod, submitUri, signedInUser, Map.empty, errorsPayload) {
          wasHtml
        }
      }
    }

    "be behind a feature switch" in featureScenario(Feature.prototype, FeatureStatus.disabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(handleMethod, submitUri, signedInUser) {
          wasNotFound
        }
      }
    }

  }

}
