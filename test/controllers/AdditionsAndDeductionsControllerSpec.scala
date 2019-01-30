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
import uk.gov.hmrc.wco.dec.ChargeDeduction
import views.html.add_additions_and_deductions

class AdditionsAndDeductionsControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with MockitoSugar
  with EndpointBehaviours {

  val form = Form(chargeDeductionMapping)

  def view(form: Form[ChargeDeduction] = form, charges: Seq[ChargeDeduction] = Seq.empty): String =
    add_additions_and_deductions(form, charges)(fakeRequest, messages, appConfig).body

  def controller(user: SignedInUser) =
    new AdditionsAndDeductionsController(new FakeActions(Some(user)), mockCustomsCacheService)

  val listGen: Gen[Option[List[ChargeDeduction]]] = option(listOf(arbitrary[ChargeDeduction]))

  val uri = "/submit-declaration/add-additions-and-deductions"

  ".onPageLoad" should {

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

      "user has no eori number" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(user.user).onPageLoad(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "load data from cache" in {

      forAll(arbitrary[SignedInUser], listGen) {
        (user, data) =>

          withCleanCache(EORI(user.eori.value), CacheKey.additionsAndDeductions, data) {

            val result = controller(user).onPageLoad(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe view(charges = data.getOrElse(Seq.empty))
          }
      }
    }
  }

  ".onSubmit" should {

    "return SEE_OTHER" when {

      "valid data is submitted" in {

        forAll { (user: SignedInUser, charge: ChargeDeduction) =>

          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(charge): _*)
          val result  = controller(user).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AdditionsAndDeductionsController.onPageLoad().url)
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user has no eori" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(user.user).onSubmit(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "return BAD_REQUEST" when {

      "invalid data is submitted" in {

        val badData = for {
          charge <- arbitrary[ChargeDeduction]
          code   <- minStringLength(4)
        } yield charge.copy(chargesTypeCode = Some(code))

        forAll(arbitrary[SignedInUser], badData, listGen) {
          (user, submittedData, cachedData) =>

            withCleanCache(EORI(user.eori.value), CacheKey.additionsAndDeductions, cachedData) {

              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(submittedData): _*)
              val popForm = form.fillAndValidate(submittedData)
              val result  = controller(user).onSubmit(request)

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(popForm, cachedData.getOrElse(Seq.empty))
            }
        }

      }
    }

    "save data in cache" when {

      "valid data is submitted" in {

        forAll { (user: SignedInUser, charge: ChargeDeduction) =>

          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(charge): _*)
          await(controller(user).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce())
            .upsert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.additionsAndDeductions))(any(), any())(any(), any(), any(), any())
        }
      }
    }
  }
}