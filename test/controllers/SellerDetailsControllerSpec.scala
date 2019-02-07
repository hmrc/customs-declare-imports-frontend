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

import domain.auth.{ EORI, SignedInUser }
import forms.DeclarationFormMapping._
import generators.Generators
import org.mockito.ArgumentMatchers.{ eq => eqTo, _ }
import org.mockito.Mockito._
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.behaviours.{ CustomsSpec, EndpointBehaviours }
import uk.gov.hmrc.wco.dec.ImportExportParty
import views.html.seller_details

class SellerDetailsControllerSpec
    extends CustomsSpec
    with PropertyChecks
    with Generators
    with OptionValues
    with EndpointBehaviours {

  val form = Form(importExportPartyMapping)

  def view(form: Form[_] = form): String =
    seller_details(form)(fakeRequest, messages, appConfig).body

  def controller(user: SignedInUser) =
    new SellerDetailsController(new FakeActions(Some(user)), mockCustomsCacheService)

  val sellerGen: Gen[Option[ImportExportParty]] = option(arbitrary[ImportExportParty])
  val uri                                       = "/submit-declaration/seller-details"

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

    "load data from cache" in {

      forAll(arbitrary[SignedInUser], sellerGen) { (user, cacheData) =>
        withCleanCache(EORI(user.eori.value), CacheKey.seller, cacheData) {

          val popForm = cacheData.fold(form)(form.fill)
          val result  = controller(user).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view(popForm)
        }
      }
    }
  }

  "onSubmit" should {

    behave like redirectedEndpoint(uri, "/submit-declaration/buyer-details", POST)
    behave like authenticatedEndpoint(uri, POST)

    "return SEE_OTHER" when {

      "valid data is posted" in {

        forAll { user: SignedInUser =>
          val result = controller(user).onSubmit(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.DeclarationController.displaySubmitForm("buyer-details").url)
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

      "bad data is posted" in {

        val badData =
          for {
            seller <- arbitrary[ImportExportParty]
            id     <- minStringLength(18)
          } yield {
            seller.copy(id = Some(id))
          }

        forAll(arbitrary[SignedInUser], badData) { (user, formData) =>
          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(formData): _*)
          val popForm = form.fillAndValidate(formData)
          val result  = controller(user).onSubmit(request)

          status(result) mustBe BAD_REQUEST
          contentAsString(result) mustBe view(popForm)
        }
      }
    }

    "save data in cache" when {

      "valid data is submitted" in {

        forAll { (user: SignedInUser, formData: ImportExportParty) =>
          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(formData): _*)
          await(controller(user).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce())
            .insert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.seller), eqTo(formData))(any(), any(), any())
        }
      }
    }
  }
}
