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
import uk.gov.hmrc.wco.dec.ObligationGuarantee
import views.html.add_guarantee_references

class AddGuaranteeReferencesControllerSpec
    extends CustomsSpec
    with PropertyChecks
    with Generators
    with OptionValues
    with MockitoSugar
    with EndpointBehaviours {

  def form = Form(obligationGauranteeMapping)

  def controller(user: Option[SignedInUser]) =
    new AddGuaranteeReferencesController(new FakeActions(user), mockCustomsCacheService)

  def view(form: Form[ObligationGuarantee] = form, guaranteeReferences: Seq[ObligationGuarantee] = Seq()): String =
    add_guarantee_references(form, guaranteeReferences)(fakeRequest, messages, appConfig).body

  val guaranteeReferencesGen = option(listOf(arbitrary[ObligationGuarantee]))

  ".onPageLoad" should {

    behave like okEndpoint("/submit-declaration/add-guarantee-references")
    behave like authenticatedEndpoint("/submit-declaration/add-guarantee-references")

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

      forAll(arbitrary[SignedInUser], guaranteeReferencesGen) {
        case (user, data) =>
          withCleanCache(EORI(user.eori.value), CacheKey.guaranteeReference, data) {

            val result = controller(Some(user)).onPageLoad(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe view(guaranteeReferences = data.getOrElse(List()))
          }
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint("/submit-declaration/add-guarantee-references", POST)
    behave like authenticatedEndpoint("/submit-declaration/add-guarantee-references", POST)

    "return SEE_OTHER" when {

      "user submits valid data" in {

        forAll { (user: SignedInUser, obligationGuarantee: ObligationGuarantee) =>
          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(obligationGuarantee): _*)
          val result  = controller(Some(user)).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AddGuaranteeReferencesController.onPageLoad().url)
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
            og         <- arbitrary[ObligationGuarantee]
            accessCode <- stringsLongerThan(4)
          } yield og.copy(accessCode = Some(accessCode))

        forAll(arbitrary[SignedInUser], badData, guaranteeReferencesGen) {
          case (user, data, existingData) =>
            withCleanCache(EORI(user.eori.value), CacheKey.guaranteeReference, existingData) {

              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(data): _*)
              val badForm = form.fillAndValidate(data)
              val result  = controller(Some(user)).onSubmit(request)

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(badForm, existingData.getOrElse(Seq()))
            }
        }
      }
    }

    "save data in cache" when {

      "valid data is submitted" in {

        forAll { (user: SignedInUser, obligationGuarantee: ObligationGuarantee) =>
          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(obligationGuarantee): _*)
          await(controller(Some(user)).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce)
            .upsert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.guaranteeReference))(any(), any())(any(),
                                                                                                  any(),
                                                                                                  any(),
                                                                                                  any())
        }
      }
    }
  }
}
