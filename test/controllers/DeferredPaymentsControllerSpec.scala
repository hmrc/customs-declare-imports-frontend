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
import org.mockito.ArgumentMatchers.{ any, eq => eqTo }
import org.mockito.Mockito.{ atLeastOnce, verify }
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{ listOf, option }
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.behaviours.{ CustomsSpec, EndpointBehaviours }
import uk.gov.hmrc.wco.dec.AdditionalDocument
import views.html.deferred_payments

class DeferredPaymentsControllerSpec
    extends CustomsSpec
    with PropertyChecks
    with Generators
    with OptionValues
    with MockitoSugar
    with EndpointBehaviours {

  def form = Form(additionalDocumentMapping)

  def controller(user: Option[SignedInUser]) =
    new DeferredPaymentsController(new FakeActions(user), mockCustomsCacheService)

  def view(form: Form[AdditionalDocument] = form, additionalDocuments: Seq[AdditionalDocument] = Seq()): String =
    deferred_payments(form, additionalDocuments)(fakeRequest, messages, appConfig).body

  val additionalDocumentGen = option(listOf(arbitrary[AdditionalDocument]))

  ".onPageLoad" should {

    behave like okEndpoint("/submit-declaration/add-deferred-payment")
    behave like authenticatedEndpoint("/submit-declaration/add-deferred-payment")

    "return OK" when {

      "user is signed in" in {

        forAll { signedInUser: SignedInUser =>
          val result = controller(Some(signedInUser)).onPageLoad(fakeRequest)

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

      forAll(arbitrary[SignedInUser], additionalDocumentGen) {
        case (user, data) =>
          withCleanCache(EORI(user.eori.value), CacheKey.additionalDocuments, data) {

            val result = controller(Some(user)).onPageLoad(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe view(additionalDocuments = data.getOrElse(List()))

          }
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint("/submit-declaration/add-deferred-payment", POST)
    behave like authenticatedEndpoint("/submit-declaration/add-deferred-payment", POST)

    "return SEE_OTHER" when {

      "user submits valid data" in {

        forAll { (user: SignedInUser, additionalDocument: AdditionalDocument) =>
          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(additionalDocument): _*)
          val result  = controller(Some(user)).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.DeferredPaymentsController.onPageLoad().url)
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user does not have an eori" in {

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
            ad           <- arbitrary[AdditionalDocument]
            categoryCode <- stringsLongerThan(1)
          } yield ad.copy(categoryCode = Some(categoryCode))

        forAll(arbitrary[SignedInUser], badData, additionalDocumentGen) {
          case (user, data, existingData) =>
            withCleanCache(EORI(user.eori.value), CacheKey.additionalDocuments, existingData) {

              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(data): _*)
              val badForm = form.fillAndValidate(data)
              val result  = controller(Some(user)).onSubmit(request)

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(badForm, existingData.getOrElse(Seq()))
            }
        }
      }
    }

    "saves data in cache" when {

      "valid data is provided" in {

        forAll { (user: SignedInUser, additionalDocument: AdditionalDocument) =>
          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(additionalDocument): _*)
          await(controller(Some(user)).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce())
            .upsert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.additionalDocuments))(any(), any())(any(),
                                                                                                   any(),
                                                                                                   any(),
                                                                                                   any())
        }
      }
    }
  }
}
