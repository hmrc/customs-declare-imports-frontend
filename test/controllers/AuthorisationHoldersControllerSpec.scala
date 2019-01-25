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

import domain.auth.{EORI, SignedInUser}
import domain.DeclarationFormats._
import forms.DeclarationFormMapping._
import generators.Generators
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.behaviours.{CustomsSpec, EndpointBehaviours}
import uk.gov.hmrc.wco.dec.AuthorisationHolder
import views.html.authorisation_holder

import scala.concurrent.Future

class AuthorisationHoldersControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with MockitoSugar
  with EndpointBehaviours
  with BeforeAndAfterEach {

  def form = Form(authorisationHolderMapping)

  def controller(user: Option[SignedInUser]) =
    new AuthorisationHoldersController(new FakeActions(user), mockCustomsCacheService)

  def view(form: Form[AuthorisationHolder] = form, authHolders: Seq[AuthorisationHolder] = Seq()): String =
    authorisation_holder(form, authHolders)(fakeRequest, messages, appConfig).body

  val authHoldersGen = option(listOf(arbitrary[AuthorisationHolder]))

  ".onPageLoad" should {

    behave like okEndpoint("/submit-declaration/add-authorisation-holder")
    behave like authenticatedEndpoint("/submit-declaration/add-authorisation-holder")

    "return OK" when {

      "user is signed in" in {

        forAll { user: SignedInUser =>

          val result = controller(Some(user)).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view()
        }
      }
    }

    "return Unauthorized" when {

      "user doesn't have an eori" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(Some(user.user)).onPageLoad(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "load data from cache" in {

      forAll(arbitrary[SignedInUser], authHoldersGen) {
        case (user, data) =>

          when(mockCustomsCacheService.getByKey(eqTo(EORI(user.eori.value)), eqTo(CacheKey.authorisationHolders))(any(), any(), any()))
            .thenReturn(Future.successful(data))

          val result = controller(Some(user)).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view(authHolders = data.getOrElse(List()))
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint("/submit-declaration/add-authorisation-holder", POST)
    behave like authenticatedEndpoint("/submit-declaration/add-authorisation-holder", POST)

    "return SEE_OTHER" when {

      "user submits valid data" in {

        forAll { (user: SignedInUser, authHolder: AuthorisationHolder) =>

          val request = fakeRequest.withFormUrlEncodedBody(toFormParams(authHolder): _*)
          val result  = controller(Some(user)).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AuthorisationHoldersController.onPageLoad().url)
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user does not have en eori" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(Some(user.user)).onSubmit(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "return BAD_REQUEST" when {

      "user submits invalid data" in {

        val badData =
          for {
            ah <- arbitrary[AuthorisationHolder]
            id <- stringsLongerThan(17)
          } yield ah.copy(id = Some(id))

        forAll(arbitrary[SignedInUser], badData, authHoldersGen) {
          case (user, data, existingData) =>

            when(mockCustomsCacheService
              .getByKey(eqTo(EORI(user.eori.value)), eqTo(CacheKey.authorisationHolders))(any(), any(), any()))
              .thenReturn(Future.successful(existingData))

            val request = fakeRequest.withFormUrlEncodedBody(toFormParams(data): _*)
            val badForm = form.fillAndValidate(data)
            val result  = controller(Some(user)).onSubmit(request)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe view(badForm, existingData.getOrElse(Seq()))
        }
      }
    }

    "saves data in cache" when {

      "valid data is provided" in {

        forAll { (user: SignedInUser, authHolder: AuthorisationHolder) =>

          val request = fakeRequest.withFormUrlEncodedBody(toFormParams(authHolder): _*)
          await(controller(Some(user)).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce())
            .upsert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.authorisationHolders))(any(), any())(any(), any(), any(), any())
        }
      }
    }
  }
}