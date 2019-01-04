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

package uk.gov.hmrc.customs.test.behaviours

import domain.features.Feature.Feature
import domain.features.FeatureStatus
import domain.features.FeatureStatus.FeatureStatus

trait FeatureBehaviours extends CustomsSpec {

  def withFeatures(features: Map[Feature, FeatureStatus])(test: => Unit): Unit = {
    features.foreach { feature =>
      sys.props += (s"microservice.services.customs-declare-imports-frontend.features.${feature._1}" -> feature._2.toString)
    }
    try {
      test
    } finally {
      features.foreach {
        feature => System.clearProperty(s"microservice.services.customs-declare-imports-frontend.features.${feature._1}")
      }
    }
  }

  def enabled(features: Feature*): Map[Feature, FeatureStatus] = features.map(_ -> FeatureStatus.enabled).toMap

  def disabled(features: Feature*): Map[Feature, FeatureStatus] = features.map(_ -> FeatureStatus.disabled).toMap

  def suspended(features: Feature*): Map[Feature, FeatureStatus] = features.map(_ -> FeatureStatus.suspended).toMap

}
