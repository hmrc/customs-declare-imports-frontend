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
import org.scalacheck.Gen._
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.behaviours.{ CustomsSpec, EndpointBehaviours }
import uk.gov.hmrc.wco.dec.TransportEquipment
import views.html.container_identification_number

class ContainerIdentificationNumberControllerSpec
    extends CustomsSpec
    with PropertyChecks
    with Generators
    with OptionValues
    with MockitoSugar
    with EndpointBehaviours {

  val form = Form(transportEquipmentMapping)

  def view(form: Form[TransportEquipment] = form, transports: Seq[TransportEquipment] = Seq.empty): String =
    container_identification_number(form, transports)(fakeRequest, messages, appConfig).body

  def controller(user: SignedInUser) =
    new ContainerIdentificationNumberController(new FakeActions(Some(user)), mockCustomsCacheService)

  val listGen = option(listOf(arbitrary[TransportEquipment]))

  val uri = "/submit-declaration/add-container-identification-number"

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

      forAll(arbitrary[SignedInUser], listGen) { (user, data) =>
        withCleanCache(EORI(user.eori.value), CacheKey.containerIdNos, data) {

          val result = controller(user).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view(transports = data.getOrElse(Seq.empty))
        }
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint(uri, POST)
    behave like authenticatedEndpoint(uri, POST)

    "return SEE_OTHER" when {

      "valid data is submitted" in {

        forAll { (user: SignedInUser, transport: TransportEquipment) =>
          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(transport): _*)
          val result  = controller(user).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ContainerIdentificationNumberController.onPageLoad().url)
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

      "invalid data is submitted" in {

        val badData =
          stringsLongerThan(17).map(s => TransportEquipment(0, Some(s)))

        forAll(arbitrary[SignedInUser], badData, listGen) { (user, submitData, cacheData) =>
          withCleanCache(EORI(user.eori.value), CacheKey.containerIdNos, cacheData) {

            val request = fakeRequest.withFormUrlEncodedBody(asFormParams(submitData): _*)
            val popForm = form.fillAndValidate(submitData)
            val result  = controller(user).onSubmit(request)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe view(popForm, cacheData.getOrElse(Seq.empty))
          }
        }
      }
    }

    "save data in cache" when {

      "valid data is submitted " in {

        forAll { (user: SignedInUser, transport: TransportEquipment) =>
          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(transport): _*)
          await(controller(user).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce())
            .upsert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.containerIdNos))(any(), any())(any(),
                                                                                              any(),
                                                                                              any(),
                                                                                              any())
        }
      }
    }
  }
}
