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

import config.SubmissionJourney
import domain.features.{Feature, FeatureStatus}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.{AuthenticationBehaviours, CustomsPlaySpec, FeatureSwitchBehaviours}

class DeclarationPageSubmissionSpec extends CustomsPlaySpec with AuthenticationBehaviours with FeatureSwitchBehaviours {

  val method = "GET"
  val handleMethod = "POST"
  val uri: String = uriWithContextPath("/submit-declaration/declarant-details")
  val submitUri: String = uriWithContextPath("/submit-declaration/declarant-details")

  "Declarant Page" should {

    case class ExpectedField(fieldType: String = "input", fieldName: String)

    def declarantScenarios: Map[String, ExpectedField] = {
      val declarantName = "declaration.declarant.name"
      val declarantAddressLine = "declaration.declarant.address.line"
      val declarantAddressCityName = "declaration.declarant.address.cityName"
      val declarantAddressCountryCode = "declaration.declarant.address.countryCode"
      val declarantAddressPostcode = "declaration.declarant.address.postcodeId"
      val declarantId = "declaration.declarant.id"

      val declarantPageScenarios: Map[String, ExpectedField] = Map.empty
      declarantPageScenarios + (s"include a text input for  $declarantName" -> ExpectedField(fieldName = declarantName),
        s"include a text input for  $declarantAddressLine" -> ExpectedField(fieldName = declarantAddressLine),
        s"include a text input for  $declarantAddressCityName" -> ExpectedField(fieldName = declarantAddressCityName),
        s"include a select field for  $declarantAddressCountryCode" -> ExpectedField("select", fieldName = declarantAddressCountryCode),
        s"include a text input for  $declarantAddressPostcode" -> ExpectedField(fieldName = declarantAddressPostcode)
      )
    }

    declarantScenarios.foreach(scenario =>
      scenario._1 in featureScenario(Feature.prototype, FeatureStatus.enabled) {
        signedInScenario() {
          userRequestScenario(method, uri) { resp =>
            includesHtmlField(resp, scenario._2.fieldType, scenario._2.fieldName)
          }
        }
      }
    )

    "include back link with URL" in featureScenario(Feature.prototype, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlLink(resp, SubmissionJourney.start.url)
        }
      }
    }

  }

}
