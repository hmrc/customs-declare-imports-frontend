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

package controllers

import config.ErrorHandler
import domain.auth.{AuthenticatedRequest, SignedInUser}
import domain.features.Feature
import domain.features.Feature.Feature
import generators.Generators
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import play.api.http.Status
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.behaviours.{AuthenticationBehaviours, CustomsSpec, FeatureBehaviours}
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.Future

class ActionsSpec extends CustomsSpec
  with AuthenticationBehaviours
  with FeatureBehaviours
  with PropertyChecks
  with Generators{

  val errorHandler = component[ErrorHandler]
  val actions = new ActionsImpl(authConnector, errorHandler)

  val switchedController = new MySwitchedController(actions, Feature.start)

  val authenticatedController = new MyAuthedController(actions)

  val eoriController = new MyEoriController(actions)

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

  "eori action" should {

    "return an eori request when user has eori identifier" in {

      forAll { user: SignedInUser =>

        withSignedInUser(user) { (_, _, _) =>

          val result = call(eoriController.action, fakeRequest)

          status(result) mustBe OK
          Some(contentAsString(result)) mustBe user.eori
        }
      }
    }

    "return Unauthorized when user does not have an eori" in {

      forAll { user: UnauthenticatedUser =>

        whenever(user.user.enrolments.getEnrolment("HMRC-CUS-ORG").nonEmpty) {
          withSignedInUser(user.user) { (_, _, _) =>

            val result = call(eoriController.action, fakeRequest)
            val expectedContent = errorHandler.notFoundTemplate(AuthenticatedRequest(fakeRequest, user.user)).body

            status(result) mustBe UNAUTHORIZED
            contentAsString(result) mustBe expectedContent
          }
        }
      }
    }
  }

}

class MySwitchedController(actions: Actions, val feature: Feature) extends BaseController {

  def action: Action[AnyContent] = actions.switch(feature).async { implicit req =>
    Future.successful(Ok(s"$feature is enabled"))
  }

}

class MyAuthedController(actions: Actions) extends BaseController {

  def action: Action[AnyContent] = actions.auth.async { implicit req =>
    Future.successful(Ok(s"${req.user}"))
  }
}

class MyEoriController(actions: Actions) extends BaseController {

  def action: Action[AnyContent] = (actions.auth andThen actions.eori) {
    implicit req =>
      Ok(req.eori.value)
  }
}
