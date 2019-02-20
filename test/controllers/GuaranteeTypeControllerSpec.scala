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
import uk.gov.hmrc.wco.dec.ObligationGuarantee
import views.html.guarantee_type

class GuaranteeTypeControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with MockitoSugar
  with EndpointBehaviours {

  val form = Form(guaranteeTypeMapping)

  def view(form: Form[_] = form, details: Seq[ObligationGuarantee] = Seq.empty): String =
    guarantee_type(form, details, details.length < 9)(fakeRequest, messages, appConfig).body

  def controller(user: SignedInUser) =
    new GuaranteeTypeController(new FakeActions(Some(user)), mockCustomsCacheService)

  val listGen: Gen[Option[Seq[ObligationGuarantee]]] = option(listOf(arbitrary[ObligationGuarantee]))
  val uri = "/submit-declaration/add-guarantee-type"

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

      "user doesn't have an eori" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(user.user).onPageLoad(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "load data from cache" in {

      forAll(arbitrary[SignedInUser], listGen) {
        (user, data) =>

          withCleanCache(EORI(user.eori.value), CacheKey.guaranteeTypes, data) {

            val result = controller(user).onPageLoad(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe view(details = data.getOrElse(Seq.empty))
          }
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint(uri, POST)
    behave like authenticatedEndpoint(uri, POST)

    "return SEE_OTHER" when {

      "valid data is posted" in {

        forAll { (user: SignedInUser, guarantee: GuaranteeType) =>

          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(guarantee.value): _*)
          val result  = controller(user).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.GuaranteeTypeController.onPageLoad().url)
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

      "invalid data is posted" in {

        val badData =
          arbitrary[String]
            .suchThat(_.length > 1)
            .map(s => ObligationGuarantee(securityDetailsCode = Some(s)))

        forAll(arbitrary[SignedInUser], badData, listGen) {
          (user, formData, cacheData) =>

            withCleanCache(EORI(user.eori.value), CacheKey.guaranteeTypes, cacheData) {

              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(formData): _*)
              val popForm = form.fillAndValidate(formData)
              val result  = controller(user).onSubmit(request)

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(popForm, cacheData.getOrElse(Seq.empty))
            }

        }
      }
    }

    "save data in cache" when {

      "valid data is posted" in {

        forAll { (user: SignedInUser, guarantee: GuaranteeType) =>

          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(guarantee.value): _*)
          await(controller(user).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce)
            .upsert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.guaranteeTypes))(any(), any())(any(), any(), any(), any())
        }
      }
    }
  }

}