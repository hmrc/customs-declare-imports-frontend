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

import domain.MetaDataMapping
import domain.auth.{EORI, SignedInUser}
import generators.Generators
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import play.api.test.Helpers._
import services.CustomsDeclarationsResponse
import uk.gov.hmrc.customs.test.behaviours.{CustomsSpec, EndpointBehaviours}
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.submit_failure

import scala.concurrent.Future

class SubmitControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with EndpointBehaviours {

  def view(): String = submit_failure()(fakeRequest, messages, appConfig).body

  def controller(user: SignedInUser, lrn: Option[String]) =
    new SubmitController(new FakeActions(Some(user), localReferenceNumber = lrn), mockCustomsCacheService, mockCustomsDeclarationsConnector)

  "onFailure" should {

    "return OK" when {
      "user is signed in" in {

        forAll(arbitrary[SignedInUser], nonEmptyString) { (user, lrn) =>

          val result = controller(user, Some(lrn)).onFailure(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view()
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user doesn't have an eori" in {

        forAll(arbitrary[UnauthenticatedUser], nonEmptyString) { (user, lrn) =>

          val result = controller(user.user, Some(lrn)).onFailure(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "return BAD_REQUEST" when {

      "user doesn't have a lrn" in {

        forAll { user: SignedInUser =>

          val result = controller(user, None).onFailure(fakeRequest)

          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }
  
  "onSubmit" should {

    val submitUri = "/submit-declaration/submit"

    behave like badRequestEndpoint(submitUri)
    behave like authenticatedEndpoint(submitUri)

    "return SEE_OTHER" when {

      "connector returns success" in {

        forAll(arbitrary[SignedInUser], nonEmptyString, arbitrary[CacheMap]) { (user, lrn, cacheMap) =>

          withCacheMap(EORI(user.eori.value), Some(cacheMap)) {

            when(mockCustomsDeclarationsConnector
              .submitImportDeclaration(eqTo(MetaDataMapping.produce(cacheMap)), eqTo(lrn))(any(), any())
            ).thenReturn(Future.successful(CustomsDeclarationsResponse("")))

            val result = controller(user, Some(lrn)).onSubmit(fakeRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.LandingController.displayLandingPage().url)
          }
        }
      }

      "connector returns failed" in {

        forAll(arbitrary[SignedInUser], nonEmptyString, arbitrary[CacheMap]) { (user, lrn, cacheMap) =>

          withCacheMap(EORI(user.eori.value), Some(cacheMap)) {

            when(mockCustomsDeclarationsConnector
              .submitImportDeclaration(eqTo(MetaDataMapping.produce(cacheMap)), eqTo(lrn))(any(), any()))
              .thenReturn(Future.failed(new Exception))

            val result = controller(user, Some(lrn)).onSubmit(fakeRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.SubmitController.onFailure().url)
          }
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user doesn't have an eori" in {

        forAll(arbitrary[UnauthenticatedUser], some(nonEmptyString)) { (user, lrn) =>

          val result = controller(user.user, lrn).onSubmit(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "return BadRequest" when {

      "user doesn't have an lrn" in {

        forAll { user: SignedInUser =>

          val result = controller(user, None).onSubmit(fakeRequest)

          status(result) mustBe BAD_REQUEST
        }
      }
    }

    "redirect to session expired page" when {

      "no cache map exists in cache" in {

        forAll { (user: SignedInUser, lrn: NonEmptyString) =>

          withCacheMap(EORI(user.eori.value), None) {
            val result = controller(user, Some(lrn.value)).onSubmit(fakeRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.SubmitController.onFailure().url)
          }
        }
      }
    }
  }
}