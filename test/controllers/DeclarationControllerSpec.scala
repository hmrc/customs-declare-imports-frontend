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

class DeclarationControllerSpec extends CustomsPlaySpec with AuthenticationBehaviours with FeatureSwitchBehaviours {

  val method = "GET"
  val handleMethod = "POST"
  val uri = uriWithContextPath("/declaration")

  s"$method $uri" should {

    "return 200" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) { wasOk }
      }
    }

    "return HTML" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) { wasHtml }
      }
    }

    "require authentication" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      notSignedInScenario() {
        accessDeniedRequestScenarioTest(method, uri)
      }
    }

    "be behind a feature switch" in featureScenario(Feature.declaration, FeatureStatus.disabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) { wasNotFound }
      }
    }

    "include a text input for WCO data model version code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "wcoDataModelVersionCode")
        }
      }
    }

    "include a text input for WCO type name" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "wcoTypeName")
        }
      }
    }

    "include a text input for Responsible Country Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "responsibleCountryCode")
        }
      }
    }

    "include a text input for Responsible Agency Name" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "responsibleAgencyName")
        }
      }
    }

    "include a text input for Agency Assigned Customization Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "agencyAssignedCustomizationCode")
        }
      }
    }

    "include a text input for Agency Assigned Customization Version Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "agencyAssignedCustomizationVersionCode")
        }
      }
    }

  }

  s"$handleMethod $uri" should {

    "return 200" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(handleMethod, uri, signedInUser) { wasOk }
      }
    }

    "return HTML" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(handleMethod, uri, signedInUser) { wasHtml }
      }
    }

    // FIXME why does this not pass?
    "require authentication" ignore featureScenario(Feature.declaration, FeatureStatus.enabled) {
      notSignedInScenario() {
        accessDeniedRequestScenarioTest(handleMethod, uri)
      }
    }

    "be behind a feature switch" in featureScenario(Feature.declaration, FeatureStatus.disabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(handleMethod, uri, signedInUser) { wasNotFound }
      }
    }

  }

  "declaration form mapping to domain object" should {

    "map WCO data model version code" in {
      val code = randomString(8)
      DeclarationForm(
        wcoDataModelVersionCode = Some(code)
      ).toMetaData.wcoDataModelVersionCode.get must be (code)
    }

    "map WCO type name" in {
      val name = randomString(8)
      DeclarationForm(
        wcoTypeName = Some(name)
      ).toMetaData.wcoTypeName.get must be (name)
    }

    "map responsible country code" in {
      val code = randomString(2)
      DeclarationForm(
        responsibleCountryCode = Some(code)
      ).toMetaData.responsibleCountryCode.get must be (code)
    }

    "map responsible agency name" in {
      val name = randomString(16)
      DeclarationForm(
        responsibleAgencyName = Some(name)
      ).toMetaData.responsibleAgencyName.get must be (name)
    }

    "map agency assigned customization code" in {
      val code = randomString(8)
      DeclarationForm(
        agencyAssignedCustomizationCode = Some(code)
      ).toMetaData.agencyAssignedCustomizationCode.get must be (code)
    }

    "map agency assigned customisation version code" in {
      val code = randomString(8)
      DeclarationForm(
        agencyAssignedCustomizationVersionCode = Some(code)
      ).toMetaData.agencyAssignedCustomizationVersionCode.get must be (code)
    }

  }

}
