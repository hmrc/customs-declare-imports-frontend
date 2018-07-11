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

import config.{AppConfig, ErrorHandler}
import domain.features.Feature.Feature
import domain.features.FeatureStatus.FeatureStatus
import domain.features.{Feature, FeatureStatus}
import play.api.http.Status
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.CustomsPlaySpec
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.Future

class ActionsSpec extends CustomsPlaySpec {

  implicit val appConfig = app.injector.instanceOf[AppConfig]

  class SwitchScenario(feature: Feature, status: FeatureStatus) {
    val actions = new Actions(app.injector.instanceOf[ErrorHandler], appConfig)
    val previousStatus = appConfig.featureStatus(feature)
    appConfig.setFeatureStatus(feature, status)
    val controller = new MyController(actions, feature)
  }

  "switch action" should {

    "return as normal for enabled feature" in new SwitchScenario(Feature.all, FeatureStatus.enabled) {
      val res = call(controller.action, FakeRequest())
      status(res) must be (Status.OK)
      appConfig.setFeatureStatus(Feature.all, previousStatus)
    }

    "return not found for disabled feature" in new SwitchScenario(Feature.all, FeatureStatus.disabled) {
      val res = call(controller.action, FakeRequest())
      status(res) must be (Status.NOT_FOUND)
      appConfig.setFeatureStatus(Feature.all, previousStatus)
    }

    "return service unavailable for suspended feature" in new SwitchScenario(Feature.all, FeatureStatus.suspended) {
      val res = call(controller.action, FakeRequest())
      status(res) must be (Status.SERVICE_UNAVAILABLE)
      appConfig.setFeatureStatus(Feature.all, previousStatus)
    }

  }

}

class MyController(actions: Actions, feature: Feature) extends BaseController {

  def action: Action[AnyContent] = actions.switch(feature).async { implicit req =>
    Future.successful(Ok(s"${feature} is enabled"))
  }

}
