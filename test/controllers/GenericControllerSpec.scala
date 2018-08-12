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
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, AnyContentAsJson}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.{AuthenticationBehaviours, CustomsPlaySpec, FeatureSwitchBehaviours, WiremockBehaviours}


class GenericControllerSpec extends CustomsPlaySpec with AuthenticationBehaviours with FeatureSwitchBehaviours with WiremockBehaviours {

  val method = "GET"
  val handleMethod = "POST"
  val uri = uriWithContextPath("/submit-declaration/declarant-details")
  val submitUri = uriWithContextPath("/submit-declaration/declarant-details/references")


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

  }

  s"$handleMethod $uri" should {

    "return 200" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(handleMethod, submitUri, signedInUser, body =
          AnyContentAsFormUrlEncoded(Map("MetaData_declaration_declarant_name"-> Seq("name1"),
          "MetaData_declaration_declarant_address_line"-> Seq("Address1")))) {
          wasOk
        }
      }
    }

    "return HTML" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(handleMethod, submitUri, signedInUser) {
          wasHtml
        }
      }
    }

    "require authentication" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      notSignedInScenario() {
        accessDeniedRequestScenarioTest(handleMethod, submitUri)
      }
    }

    "be behind a feature switch" in featureScenario(Feature.declaration, FeatureStatus.disabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(handleMethod, submitUri, signedInUser) {
          wasNotFound
        }
      }
    }

  }


  "Declarant Page" should {

    case class ExpectedField(fieldType:String ="input",fieldName:String)

    def declarantScenarios = {
      val declarantName = "MetaData_declaration_declarant_name"
      val declarantAddressLine = "MetaData_declaration_declarant_address_line"
      val declarantAddressCityName = "MetaData_declaration_declarant_address_cityName"
      val declarantAddressCountryCode = "MetaData_declaration_declarant_address_countryCode"
      val declarantAddressPostcode = "MetaData_declaration_declarant_address_postcodeId"
      val declarantId = "MetaData_declaration_declarant_id"

      val declarantPageScenarios:Map[String,ExpectedField] = Map.empty
      declarantPageScenarios + (s"include a text input for  ${declarantName}" -> ExpectedField(fieldName = declarantName),
      s"include a text input for  ${declarantAddressLine}" -> ExpectedField(fieldName = declarantAddressLine),
      s"include a text input for  ${declarantAddressCityName}" -> ExpectedField(fieldName = declarantAddressCityName),
      s"include a select field for  ${declarantAddressCountryCode}" -> ExpectedField("select",fieldName = declarantAddressCountryCode),
      s"include a text input for  ${declarantAddressPostcode}" -> ExpectedField(fieldName = declarantAddressPostcode)
      )
    }

    declarantScenarios.map(scenario =>
      scenario._1 in featureScenario(Feature.declaration, FeatureStatus.enabled) {
        signedInScenario() {
          userRequestScenario(method, uri) { resp =>
            includesHtmlField(resp, scenario._2.fieldType,scenario._2.fieldName)
          }
        }
      }
    )

    "include back link with URL" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlLink(resp, "/customs-declare-imports/start")
        }
      }
    }

  }

}
