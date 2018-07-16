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

import config.ErrorHandler
import domain.features.Feature.Feature
import domain.features.{Feature, FeatureStatus}
import play.api.http.Status
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.{AuthenticationBehaviours, CustomsPlaySpec, FeatureSwitchBehaviours}
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.Future

class ActionsSpec extends CustomsPlaySpec with AuthenticationBehaviours with FeatureSwitchBehaviours {

  val actions = new Actions(mockAuthConnector, app.injector.instanceOf[ErrorHandler])

  val switchedController = new MySwitchedController(actions, Feature.start)

  val authenticatedController = new MyAuthedController(actions)

  "switch action" should {

    "return as normal for enabled feature" in featureScenario(switchedController.feature, FeatureStatus.enabled) {
      val res = call(switchedController.action, basicRequest())
      status(res) must be(Status.OK)
      contentAsString(res) must be (s"${switchedController.feature} is enabled")
    }

    "return not found for disabled feature" in featureScenario(switchedController.feature, FeatureStatus.disabled) {
      val res = call(switchedController.action, basicRequest())
      status(res) must be(Status.NOT_FOUND)
    }

    "return service unavailable for suspended feature" in featureScenario(switchedController.feature, FeatureStatus.suspended) {
      val res = call(switchedController.action, basicRequest())
      status(res) must be(Status.SERVICE_UNAVAILABLE)
    }

  }

  "auth action" should {

    "return as normal when user signed in with sufficient enrolments" in signedInScenario() {
      val res = call(authenticatedController.action, basicRequest())
      status(res) must be(Status.OK)
      contentAsString(res) must be(signedInUser.toString)
    }

  }

}

class MySwitchedController(actions: Actions, val feature: Feature) extends BaseController {

  def action: Action[AnyContent] = actions.switch(feature).async { implicit req =>
    Future.successful(Ok(s"${feature} is enabled"))
  }

}

class MyAuthedController(actions: Actions) extends BaseController {

  def action: Action[AnyContent] = actions.auth.async { implicit req =>
    Future.successful(Ok(s"${req.user}"))
  }

}
