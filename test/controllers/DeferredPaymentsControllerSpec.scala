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
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{listOf, option}
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.behaviours.{CustomsSpec, EndpointBehaviours}
import uk.gov.hmrc.wco.dec.AdditionalDocument
import views.html.deferred_payments

import scala.concurrent.Future



class DeferredPaymentsControllerSpec extends CustomsSpec
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

          when(mockCustomsCacheService.getByKey(eqTo(EORI(user.eori.value)), eqTo(CacheKey.additionalDocuments))(any(), any(), any()))
            .thenReturn(Future.successful(data))

          val result = controller(Some(user)).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view(additionalDocuments = data.getOrElse(List()))
      }
    }
  }

}
