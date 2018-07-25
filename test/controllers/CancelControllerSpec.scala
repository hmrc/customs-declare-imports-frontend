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

import domain.features.{FeatureStatus, Feature}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.{WiremockBehaviours, FeatureSwitchBehaviours, AuthenticationBehaviours, CustomsPlaySpec}


class CancelControllerSpec extends CustomsPlaySpec with AuthenticationBehaviours with FeatureSwitchBehaviours with WiremockBehaviours {

  val method = "GET"
  val handleMethod = "POST"
  val uri = uriWithContextPath("/cancel-declaration")


  s"$method $uri" should {

    "return 200" in featureScenario(Feature.cancel, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) {
          wasOk
        }
      }
    }

    "return HTML" in featureScenario(Feature.cancel, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) {
          wasHtml
        }
      }
    }

    "require authentication" in featureScenario(Feature.cancel, FeatureStatus.enabled) {
      notSignedInScenario() {
        accessDeniedRequestScenarioTest(method, uri)
      }
    }

    "be behind a feature switch" in featureScenario(Feature.cancel, FeatureStatus.disabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) {
          wasNotFound
        }
      }
    }

    "include a text input for WCO data model version code" in featureScenario(Feature.cancel, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.wcoDataModelVersionCode")
        }
      }
    }

        "include a text input for WCO type name" in featureScenario(Feature.cancel, FeatureStatus.enabled) {
          signedInScenario() {
            userRequestScenario(method, uri) { resp =>
              includesHtmlInput(resp, "text", "metaData.wcoTypeName")
            }
          }
        }
    /*
           "include a text input for Responsible Country Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
             signedInScenario() {
               userRequestScenario(method, uri) { resp =>
                 includesHtmlInput(resp, "text", "metaData.responsibleCountryCode")
               }
             }
           }

           "include a text input for Responsible Agency Name" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
             signedInScenario() {
               userRequestScenario(method, uri) { resp =>
                 includesHtmlInput(resp, "text", "metaData.responsibleAgencyName")
               }
             }
           }

           "include a text input for Agency Assigned Customization Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
             signedInScenario() {
               userRequestScenario(method, uri) { resp =>
                 includesHtmlInput(resp, "text", "metaData.agencyAssignedCustomizationCode")
               }
             }
           }

           "include a text input for Agency Assigned Customization Version Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
             signedInScenario() {
               userRequestScenario(method, uri) { resp =>
                 includesHtmlInput(resp, "text", "metaData.agencyAssignedCustomizationVersionCode")
               }
             }
           }

           "include a text input for Badge ID" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
             signedInScenario() {
               userRequestScenario(method, uri) { resp =>
                 includesHtmlInput(resp, "text", "badgeId")
               }
             }
           }

           "include a text input for Acceptance Date Time" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
             signedInScenario() {
               userRequestScenario(method, uri) { resp =>
                 includesHtmlInput(resp, "text", "metaData.declaration.acceptanceDateTime")
               }
             }
           }

           "include a text input for Acceptance Date Time format code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
             signedInScenario() {
               userRequestScenario(method, uri) { resp =>
                 includesHtmlInput(resp, "text", "metaData.declaration.acceptanceDateTimeFormatCode")
               }
             }
           }

           "include a text input for Function Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
             signedInScenario() {
               userRequestScenario(method, uri) { resp =>
                 includesHtmlInput(resp, "text", "metaData.declaration.functionCode")
               }
             }
           }

           "include a text input for Functional Reference ID" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
             signedInScenario() {
               userRequestScenario(method, uri) { resp =>
                 includesHtmlInput(resp, "text", "metaData.declaration.functionalReferenceId")
               }
             }
           }

           "include a text input for ID" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
             signedInScenario() {
               userRequestScenario(method, uri) { resp =>
                 includesHtmlInput(resp, "text", "metaData.declaration.id")
               }
             }
           }*/
  }


}
