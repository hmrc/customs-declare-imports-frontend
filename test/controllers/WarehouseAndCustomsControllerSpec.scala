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

import domain.WarehouseAndCustoms
import domain.auth.{EORI, SignedInUser}
import forms.DeclarationFormMapping._
import generators.Generators
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{atLeastOnce, verify}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.behaviours.{CustomsSpec, EndpointBehaviours}
import uk.gov.hmrc.wco.dec.Warehouse
import views.html.warehouse_and_customs_offices

class WarehouseAndCustomsControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with EndpointBehaviours {

  val form = Form(warehouseAndCustomsMapping)

  def view(form: Form[_] = form): String = warehouse_and_customs_offices(form)(fakeRequest, messages, appConfig).body

  def controller(user: SignedInUser) =
    new WarehouseAndCustomsController(new FakeActions(Some(user)), mockCustomsCacheService)

  val warehouseAndCustomsGen: Gen[Option[WarehouseAndCustoms]] = option(arbitrary[WarehouseAndCustoms])

  val uri = "/submit-declaration/warehouse-and-customs-offices"

  "onPageLoad" should {

    behave like okEndpoint(uri, "GET")
    behave like authenticatedEndpoint(uri)

    "return OK" when {

      "user is signed in " in {

        forAll { user: SignedInUser =>

          val result = controller(user).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view()
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user doesn't have an eori" in {
        forAll { unauthenticatedUser: UnauthenticatedUser =>

          val result = controller(unauthenticatedUser.user).onPageLoad(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "load data from cache" in {

      forAll(arbitrary[SignedInUser], warehouseAndCustomsGen) {
        (signedInUser, cacheData) =>

          withCleanCache(EORI(signedInUser.eori.value), CacheKey.warehouseAndCustoms, cacheData) {

            val popForm = cacheData.fold(form)(form.fill)
            val result = controller(signedInUser).onPageLoad(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe view(popForm)
          }
      }
    }
  }

  "onSubmit" should {

    behave like redirectedEndpoint(uri, "/submit-declaration/delivery-terms", POST)
    behave like authenticatedEndpoint(uri, POST)

    "return SEE_OTHER" when {

      "valid data is posted" in {

        forAll { user: SignedInUser =>

          val result = controller(user).onSubmit(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.DeliveryTermsController.onPageLoad().url)
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
            invalidWarehouse    <- arbitrary[Warehouse]
            warehouseAndCustoms <- arbitrary[WarehouseAndCustoms]
            invalidId           <- minStringLength(36)
          } yield {
            warehouseAndCustoms.copy(warehouse = Some(invalidWarehouse.copy(id = Some(invalidId))))
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

    "save data in cache" when {

      "valid data is submitted" in {

        forAll { (user: SignedInUser, formData: WarehouseAndCustoms) =>

          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(formData): _*)
          await(controller(user).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce)
            .insert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.warehouseAndCustoms), eqTo(formData))(any(), any(), any())
        }
      }
    }
  }
}
