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
import org.scalatest.OptionValues
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
  with EndpointBehaviours {

  def form = Form(authorisationHolderMapping)

  def controller(user: Option[SignedInUser]) =
    new AuthorisationHoldersController(new FakeActions(user), mockCustomsCacheService)

  def view(form: Form[AuthorisationHolder] = form, authHolders: Seq[AuthorisationHolder] = Seq()): String =
    authorisation_holder(form, authHolders)(fakeRequest, messages, appConfig).body

  ".onPageLoad" should {

    behave like authenticatedEndpoint("/submit-declaration-goods/add-authorisation-holder")

    "return OK" when {

      "user is signed in" in {

        forAll { user: SignedInUser =>

          when(mockCustomsCacheService.getByKey(any(), any())(any(), any(), any())).thenReturn(Future.successful(None))

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

      val gen = option(listOf(arbitrary[AuthorisationHolder]))

      forAll(arbitrary[SignedInUser], gen) {
        case (user, data) =>

          when(mockCustomsCacheService.getByKey(eqTo(EORI(user.eori.value)), eqTo(CacheKey.authorisationHolders))(any(), any(), any()))
            .thenReturn(Future.successful(data))

          val result = controller(Some(user)).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view(authHolders = data.getOrElse(List()))
      }
    }
  }
}