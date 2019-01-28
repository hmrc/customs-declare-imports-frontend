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
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
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
import uk.gov.hmrc.wco.dec.RoleBasedParty
import views.html.role_based_party

import scala.concurrent.Future

class RoleBasedPartyControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with MockitoSugar
  with OptionValues
  with EndpointBehaviours {

  val form = Form(roleBasedPartyMapping)

  def controller(user: Option[SignedInUser]) =
    new RoleBasedPartyController(new FakeActions(user), mockCustomsCacheService)

  def view(form: Form[RoleBasedParty], roles: Seq[RoleBasedParty]): String =
    role_based_party(form, roles)(fakeRequest, messages, appConfig).body

  val listGen: Gen[Option[List[RoleBasedParty]]] =
    option(listOf(arbitrary[RoleBasedParty]))

  ".onPageLoad" should {

    behave like okEndpoint("/submit-declaration/add-role-based-party")
    behave like authenticatedEndpoint("/submit-declaration/add-role-based-party")

    "return OK" when {

      "user is signed in" in {

        forAll { user: SignedInUser =>

          val result = controller(Some(user)).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view(form, Seq.empty)
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user has no eori number" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(Some(user.user)).onPageLoad(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "load data from cache" in {

      forAll(arbitrary[SignedInUser], listGen) {
        (user, data) =>

          withCleanCache(EORI(user.eori.value), CacheKey.roleBasedParty, data) {

            val result = controller(Some(user)).onPageLoad(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe view(form, data.getOrElse(Seq.empty))
          }
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint("/submit-declaration/add-role-based-party", POST)
    behave like authenticatedEndpoint("/submit-declaration/add-role-based-party", POST)

    "return SEE_OTHER" when {

      "valid data is submitted" in {

        forAll { (user: SignedInUser, role: RoleBasedParty) =>

          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(role): _*)
          val result = controller(Some(user)).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.RoleBasedPartyController.onPageLoad().url)
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user has no eori number" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(Some(user.user)).onSubmit(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "return BAD_REQUEST" when {

      "invalid data is submitted" in {

        val badData = for {
          r  <- arbitrary[RoleBasedParty]
          id <- minStringLength(18)
        } yield r.copy(id = Some(id))

        forAll(arbitrary[SignedInUser], badData, listGen) {
          (user, badData, cacheData) =>

            withCleanCache(EORI(user.eori.value), CacheKey.roleBasedParty, cacheData) {

              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(badData): _*)
              val popForm = form.fillAndValidate(badData)
              val result  = controller(Some(user)).onSubmit(request)

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(popForm, cacheData.getOrElse(Seq.empty))
            }
        }
      }
    }

    "save data in cache" when {

      "valid data is submitted" in {

        forAll { (user: SignedInUser, role: RoleBasedParty) =>

          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(role): _*)
          await(controller(Some(user)).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce())
            .upsert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.roleBasedParty))(any(), any())(any(), any(), any(), any())
        }
      }
    }
  }
}