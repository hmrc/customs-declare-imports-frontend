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
import domain.features.Feature
import domain.features.Feature.Feature
import play.api.http.Status
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.behaviours.{AuthenticationBehaviours, CustomsSpec, FeatureBehaviours}
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.Future

class ActionsSpec extends CustomsSpec with AuthenticationBehaviours with FeatureBehaviours {

  val actions = new Actions(authConnector, component[ErrorHandler])

  val switchedController = new MySwitchedController(actions, Feature.start)

  val authenticatedController = new MyAuthedController(actions)

  "switch action" should {

    "return as normal for enabled feature" in withFeatures(enabled(switchedController.feature)) {
      val res = call(switchedController.action, FakeRequest())
      status(res) must be(Status.OK)
      contentAsString(res) must be (s"${switchedController.feature} is enabled")
    }

    "return not found for disabled feature" in withFeatures(disabled(switchedController.feature)) {
      val res = call(switchedController.action, FakeRequest())
      status(res) must be(Status.NOT_FOUND)
    }

    "return service unavailable for suspended feature" in withFeatures(suspended(switchedController.feature)) {
      val res = call(switchedController.action, FakeRequest())
      status(res) must be(Status.SERVICE_UNAVAILABLE)
    }

  }

  "auth action" should {

    "return as normal when user signed in with sufficient enrolments" in withSignedInUser() { (_, _, _) =>
      val res = call(authenticatedController.action, FakeRequest())
      status(res) must be(Status.OK)
      contentAsString(res) must be(randomUser.toString)
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
