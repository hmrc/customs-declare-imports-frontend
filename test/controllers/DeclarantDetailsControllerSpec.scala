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
import forms.DeclarationFormMapping._
import generators.Generators
import org.mockito.ArgumentMatchers.{eq=>eqTo, _}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.behaviours.{CustomsSpec, EndpointBehaviours}
import uk.gov.hmrc.wco.dec.ImportExportParty
import views.html.declarant_details

class DeclarantDetailsControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with MockitoSugar
  with EndpointBehaviours {

  def form = Form(importExportPartyMapping)

  def view(form: Form[_] = form): String =
    declarant_details(form)(fakeRequest, messages, appConfig).body

  def controller(user: SignedInUser) =
    new DeclarantDetailsController(new FakeActions(Some(user)), mockCustomsCacheService)

  val declarantGen: Gen[Option[ImportExportParty]] = option(arbitrary[ImportExportParty])
  val uri = "/submit-declaration/declarant-details"

  "onPageLoad" should {

    behave like okEndpoint(uri)
    behave like authenticatedEndpoint(uri)

    "return OK" when {

      "user is signed in" in {

        forAll { user: SignedInUser =>

          val result = controller(user).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view()
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user doesn't have an eori" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(user.user).onPageLoad(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "display data stored cache" in {

      forAll(arbitrary[SignedInUser], declarantGen) {
        (user, declarant) =>

          withCleanCache(EORI(user.eori.value), CacheKey.declarantDetails, declarant) {

            val popForm = declarant.fold(form)(form.fillAndValidate(_))
            val result  = controller(user).onPageLoad(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe view(popForm)
          }
      }
    }
  }

  "onSubmit" should {

    behave like redirectedEndpoint(uri, "/submit-declaration/references", POST)
    behave like authenticatedEndpoint(uri, POST)

    "return SEE_OTHER" when {

      "user posts valid data" in {

        forAll { (user: SignedInUser, declarant: ImportExportParty) =>

          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(declarant): _*)
          val result = controller(user).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ReferencesController.onPageLoad().url)
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user doesn't have an eori" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(user.user).onSubmit(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "return BAD_REQUEST" when {

      "user posts invalid data" in {

        val badData = for {
          declarant <- arbitrary[ImportExportParty]
          id        <- minStringLength(18)
        } yield {
          declarant.copy(id = Some(id))
        }

        forAll(arbitrary[SignedInUser], badData) {
          (user, formData) =>

            val request = fakeRequest.withFormUrlEncodedBody(asFormParams(formData): _*)
            val popForm = form.fillAndValidate(formData)
            val result  = controller(user).onSubmit(request)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe view(popForm)
        }
      }
    }

    "store data in cache" when {

      "user posts valid data" in {

        forAll { (user: SignedInUser, declarant: ImportExportParty) =>

          val request  = fakeRequest.withFormUrlEncodedBody(asFormParams(declarant): _*)
          await(controller(user).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce)
            .insert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.declarantDetails), eqTo(declarant))(any(), any(), any())
        }
      }
    }
  }
}