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

import config.SubmissionJourney
import domain.Cancel
import domain.auth.SignedInUser
import domain.features.Feature
import forms.DeclarationFormMapping._
import generators.Generators
import models.Cancellation
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.assertions.{HtmlAssertions, HttpAssertions}
import uk.gov.hmrc.customs.test.behaviours._
import uk.gov.hmrc.http.{Upstream4xxResponse, Upstream5xxResponse}
import views.html.{cancel_failure, cancel_form}

import scala.concurrent.Future

class DeclarationControllerSpec extends CustomsSpec
  with AuthenticationBehaviours
  with FeatureBehaviours
  with RequestHandlerBehaviours
  with HttpAssertions
  with HtmlAssertions
  with PropertyChecks
  with Generators
  with EndpointBehaviours {

  val mrn = randomString(16)
  val get = "GET"
  val post = "POST"
  val submitUri = journeyUri(SubmissionJourney.screens.head)
  val cancelUri = s"/cancel-declaration/$mrn"
  val form = Form[Cancel](cancelMapping)

  def journeyUri(screen: String): String = uriWithContextPath(s"/submit-declaration/$screen")

  def controller(user: SignedInUser): DeclarationController =
    new DeclarationController(new FakeActions(Some(user)), mockCustomsDeclarationsConnector, mockCustomsCacheService)

  def view(form: Form[Cancel] = form): String = cancel_form(mrn, form)(fakeRequest, messages, appConfig).body

  s"$get $submitUri" should {

    "return 200" in withFeatures(enabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>
        withCaching(None)
        withRequest(get, submitUri, headers, session, tags) {
          wasOk
        }
      }
    }

    "return HTML" in withFeatures(enabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequest(get, submitUri, headers, session, tags) {
          wasHtml
        }
      }
    }

    "require authentication" in withFeatures(enabled(Feature.submit)) {
      withoutSignedInUser() { (_, _) =>
        withRequest(get, submitUri) { resp =>
          wasRedirected(ggLoginRedirectUri(submitUri), resp)
        }
      }
    }

    "be behind a feature switch" in withFeatures(disabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequest(get, submitUri, headers, session, tags) {
          wasNotFound
        }
      }
    }
  }

  "handleCancelForm" should {

    behave like authenticatedEndpoint(cancelUri, POST)

    "cancel the declaration and return 200 OK" when {

      "valid data is posted" in {
        withImportsBackend

        forAll(arbitrary[SignedInUser], arbitrary[Cancel]) { (user, formData) =>

          val cancellation = Cancellation(mrn, formData.changeReasonCode, formData.description)
          val params       = Seq("changeReasonCode" -> formData.changeReasonCode.entryName, "description" -> formData.description)
          val request      = fakeRequest.withFormUrlEncodedBody(params: _*)
          val result       = controller(user).handleCancelForm(mrn)(request)

          status(result) mustBe OK
          verify(mockCustomsDeclarationsConnector).cancelDeclaration(meq(cancellation))(any(), any())
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user doesn't have an eori" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(user.user).handleCancelForm(mrn)(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "return SEE_OTHER" when {

      "backend fails" in {

        val responseGen = Gen.oneOf(Upstream4xxResponse("", 400, 0), Upstream5xxResponse("", 500, 0))

        forAll(arbitrary[SignedInUser], arbitrary[Cancel], arbitrary[String], responseGen) {
          (user, formData, mrn, response) =>

            when(mockCustomsDeclarationsConnector.cancelDeclaration(any())(any(), any()))
              .thenReturn(Future.failed(response))

            val params  = Seq("changeReasonCode" -> formData.changeReasonCode.entryName, "description" -> formData.description)
            val request = fakeRequest.withFormUrlEncodedBody(params: _*)
            val result  = controller(user).handleCancelForm(mrn)(request)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.DeclarationController.displayCancelFailure(mrn).url)
        }
      }
    }

    "return BAD_REQUEST" when {

      "bad data is posted" in {
        withImportsBackend

        val badData = for {
          changeReasonCode <- arbitrary[String]
          description      <- minStringLength(513)
        } yield Map("changeReasonCode" -> changeReasonCode, "description" -> description)

        forAll(arbitrary[SignedInUser], badData) { (user, formData) =>

          val request = fakeRequest.withFormUrlEncodedBody(formData.toSeq: _*)
          val popForm = form.bind(formData)
          val result  = controller(user).handleCancelForm(mrn)(request)

          status(result) mustBe BAD_REQUEST
          contentAsString(result) mustBe view(popForm)
          verify(mockCustomsDeclarationsConnector, never).cancelDeclaration(any())(any(), any())
        }
      }
    }

  }

  "displayCancelForm" should {

    behave like okEndpoint(cancelUri, "GET")
    behave like authenticatedEndpoint(cancelUri)

    "return OK" when {

      "user is signed in " in {

        forAll { user: SignedInUser =>

          val result = controller(user).displayCancelForm(mrn)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view()
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user doesn't have an eori" in {
        forAll { unauthenticatedUser: UnauthenticatedUser =>

          val result = controller(unauthenticatedUser.user).displayCancelForm(mrn)(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

  }

  "displayCancelFailure" should {

    def view(mrn: String): String =
      cancel_failure(mrn)(fakeRequest, messages, appConfig).body

    val uri = "/cancel-declaration/1234/failed"

    behave like okEndpoint(uri)
    behave like authenticatedEndpoint(uri)

    "return OK" when {

      "user is signed in" in {

        forAll { (user: SignedInUser, mrn: String) =>

          val result = controller(user).displayCancelFailure(mrn)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view(mrn)
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user doesn't have an eori" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(user.user).displayCancelFailure("")(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }
  }
}
