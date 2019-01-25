/*
 * Copyright 2019 HM Revenue & Customs
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

import domain.features
import domain.features.Feature.Feature
import domain.features.FeatureStatus.FeatureStatus
import domain.features.{Feature, FeatureStatus}
import javax.inject.{Inject, Singleton}
import play.api.Mode.Mode
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}

@Singleton
class AppConfig @Inject()(val runModeConfiguration: Configuration, val environment: Environment) extends ServicesConfig with AppName {

  private val contactHost = runModeConfiguration.getString(s"contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "MyService"

  lazy val assetsPrefix: String = loadConfig(s"assets.url") + loadConfig(s"assets.version")
  lazy val analyticsToken: String = loadConfig(s"google-analytics.token")
  lazy val analyticsHost: String = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl: String = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl: String = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val customsDeclareImportsEndpoint: String = baseUrl("customs-declare-imports")

  lazy val customsDeclarationsApiVersion: String = getConfString("customs-declarations.api-version", throw new IllegalStateException("Missing configuration for Customs Declarations API version"))
  lazy val cancelImportDeclarationUri: String = getConfString("customs-declarations.cancel-uri", throw new IllegalStateException("Missing configuration for Customs Declarations cancel URI"))
  lazy val submitImportDeclarationUri: String = getConfString("customs-declare-imports.submit-uri", throw new IllegalStateException("Missing configuration for Customs Declarations submission URI"))
  lazy val developerHubClientId: String = appName

  lazy val keyStoreSource: String = appName
  lazy val keyStoreUrl: String = baseUrl("keystore")
  lazy val sessionCacheDomain: String = getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
  lazy val defaultFeatureStatus: features.FeatureStatus.Value = FeatureStatus.withName(loadConfig(feature2Key(Feature.default)))
  lazy val submissionCacheId: String = "submit-declaration"

  def featureStatus(feature: Feature): FeatureStatus = sys.props.get(feature2Key(feature)).map(str2FeatureStatus).getOrElse(
    runModeConfiguration.getString(feature2Key(feature)).map(str2FeatureStatus).getOrElse(
      defaultFeatureStatus
    )
  )

  def isFeatureOn(feature: Feature): Boolean = featureStatus(feature) == FeatureStatus.enabled

  def setFeatureStatus(feature: Feature, status: FeatureStatus): Unit = sys.props += (feature2Key(feature) -> status.toString)

  override protected def mode: Mode = environment.mode

  override protected def appNameConfiguration: Configuration = runModeConfiguration

  private def loadConfig(key: String): String = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private def feature2Key(feature: Feature): String = s"microservice.services.$appName.features.$feature"

  private def str2FeatureStatus(str: String): FeatureStatus = FeatureStatus.withName(str)

}