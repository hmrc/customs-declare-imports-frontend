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

package config

import domain.features.{Feature, FeatureStatus}
import play.api.Environment
import uk.gov.hmrc.customs.test.{CustomsPlaySpec, FeatureSwitchBehaviours}

class AppConfigSpec extends CustomsPlaySpec with FeatureSwitchBehaviours {

  val cfg = app.injector.instanceOf[AppConfig]

  "the config" should {

    "have assets prefix" in {
      cfg.assetsPrefix must be ("http://localhost:9032/assets/4.3.1")
    }

    "have analytics token" in {
      cfg.analyticsToken must be ("N/A")
    }

    "have analytics host" in {
      cfg.analyticsHost must be ("auto")
    }

    "have 'report a problem' partial URL" in {
      cfg.reportAProblemPartialUrl must be ("http://localhost:9250/contact/problem_reports_ajax?service=MyService")
    }

    "have 'report a problem' non-JS URL" in {
      cfg.reportAProblemNonJSUrl must be ("http://localhost:9250/contact/problem_reports_nonjs?service=MyService")
    }

    "have default feature status" in {
      cfg.defaultFeatureStatus must be (FeatureStatus.disabled)
    }

    "expose the environment" in {
      cfg.environment must be (app.injector.instanceOf[Environment])
    }

    "have a submit import declarations uri" in {
      cfg.submitImportDeclarationUri must be ("/")
    }

    "have a cancel import declarations uri" in {
      cfg.cancelImportDeclarationUri must be ("/cancellation-requests")
    }

    "have customs declarations endpoint" in {
      cfg.customsDeclarationsEndpoint must be ("http://localhost:9820")
    }

    "have customs declarations API version" in {
      cfg.customsDeclarationsApiVersion must be ("2.0")
    }

    "have HMRC Developer Hub Client ID" in {
      cfg.developerHubClientId must be (cfg.appName)
    }

  }

  "feature status" should {

    "indicate default status for unset feature" in {
      cfg.featureStatus(Feature.start) must be (cfg.defaultFeatureStatus)
    }

    "fall back to system properties for unconfigured feature" in featureScenario(Feature.start, FeatureStatus.enabled) {
      cfg.featureStatus(Feature.start) must be (FeatureStatus.enabled)
    }

  }

  "set feature status" should {

    // for the sake of explicitness and thoroughness, don't use featureScenario test harness for this one
    "override app config" in {
      val newStatus = cfg.defaultFeatureStatus match {
        case FeatureStatus.enabled => FeatureStatus.disabled
        case _ => FeatureStatus.enabled
      }
      cfg.setFeatureStatus(Feature.default, newStatus)
      cfg.featureStatus(Feature.default) must be (newStatus)
      System.clearProperty("microservice.services.customs-declare-imports-frontend.features.default")
      cfg.featureStatus(Feature.default) must be (cfg.defaultFeatureStatus)
    }

  }

  "is feature on" should {

    "return true for enabled feature" in featureScenario(Feature.start, FeatureStatus.enabled) {
      cfg.isFeatureOn(Feature.start) must be (true)
    }

    "return false for disabled feature" in featureScenario(Feature.start, FeatureStatus.disabled) {
      cfg.isFeatureOn(Feature.start) must be (false)
    }

    "return false for suspended feature" in featureScenario(Feature.start, FeatureStatus.suspended) {
      cfg.isFeatureOn(Feature.start) must be (false)
    }

  }

}
