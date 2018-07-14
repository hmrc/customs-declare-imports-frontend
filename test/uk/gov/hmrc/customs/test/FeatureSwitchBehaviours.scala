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

package uk.gov.hmrc.customs.test

import domain.features.Feature.Feature
import domain.features.FeatureStatus
import domain.features.FeatureStatus.FeatureStatus

trait FeatureSwitchBehaviours {
  this: CustomsPlaySpec =>

  def featureScenario(feature: Feature, status: FeatureStatus = FeatureStatus.enabled)(test: => Unit): Unit = featureScenario(Map(feature -> status))(test)

  def featureScenario(features: Seq[Feature], status: FeatureStatus)(test: => Unit): Unit = featureScenario(features.map { feature =>
    (feature, status)
  }.toMap)(test)

  def featureScenario(features: Map[Feature, FeatureStatus])(test: => Unit): Unit = {
    features.foreach(entry => sys.props += (s"microservice.services.customs-declare-imports-frontend.features.${entry._1}" -> entry._2.toString))
    try {
      test
    } finally {
      features.foreach(entry => System.clearProperty(s"microservice.services.customs-declare-imports-frontend.features.${entry._1}"))
    }
  }

}
