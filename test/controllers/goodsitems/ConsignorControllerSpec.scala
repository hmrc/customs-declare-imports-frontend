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

package controllers.goodsitems

import controllers.FakeActions
import domain.auth.{EORI, SignedInUser}
import generators.{Generators, Lenses}
import forms.DeclarationFormMapping._
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.behaviours.{CustomsSpec, EndpointBehaviours}
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItem, NamedEntityWithAddress}
import views.html.goods_item_consignor

class ConsignorControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with Lenses
  with EndpointBehaviours {

  type GoodsItem = GovernmentAgencyGoodsItem

  val form = Form(namedEntityWithAddressMapping)

  def controller(user: SignedInUser, item: Option[GoodsItem]) =
    new ConsignorController(new FakeActions(Some(user), item), mockCustomsCacheService)

  def view(form: Form[_] = form): String =
    goods_item_consignor(form)(fakeRequest, messages, appConfig).body

  val uri = "/submit-declaration-goods/consignor"

  "onPageLoad" should {

    behave like badRequestEndpoint(uri)
    behave like authenticatedEndpoint(uri)

    "return OK" when {

      "user is signed in" in {

        forAll { (user: SignedInUser, item: GoodsItem) =>

          val popForm = item.consignee.map(form.fill).getOrElse(form)
          val result  = controller(user, Some(item)).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view(popForm)
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user doesn't have an eori" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(user.user, None).onPageLoad(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }

      }
    }

    "return BAD_REQUEST" when {

      "goods item isn't in cache" in {

        forAll { user: SignedInUser =>

          val result = controller(user, None).onPageLoad(fakeRequest)

          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }

  "onSubmit" should {

    behave like badRequestEndpoint(uri, POST)
    behave like authenticatedEndpoint(uri, POST)

    "return SEE_OTHER" when {

      "user is signed in" in {

        forAll { (user: SignedInUser, consignor: NamedEntityWithAddress, item: GoodsItem) =>

          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(consignor): _*)
          val result  = controller(user, Some(item)).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.GoodsItemsListController.onPageLoad().url)
        }
      }
    }

    "return UNAUTHORISED" when {

      "user doesn't have an eori" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(user.user, None).onSubmit(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }

      }
    }

    "return BAD_REQUEST" when {

      "no goods item is in cache" in {

        forAll { user: SignedInUser =>

          val result = controller(user, None).onSubmit(fakeRequest)

          status(result) mustBe BAD_REQUEST
        }
      }

      "user posted bad data" in {

        val badData = NamedEntityWithAddress.id.setArbitrary(some(minStringLength(18)))

        forAll(arbitrary[SignedInUser], badData, arbitrary[GoodsItem]) {
          (user, consignor, item) =>

            val popForm = form.fillAndValidate(consignor)
            val request = fakeRequest.withFormUrlEncodedBody(asFormParams(consignor): _*)
            val result  = controller(user, Some(item)).onSubmit(request)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe view(popForm)
        }
      }
    }

    "save data in cache" when {

      "valid data is posted" in {

        forAll { (user: SignedInUser, consignor: NamedEntityWithAddress, item: GoodsItem) =>

          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(consignor): _*)
          val expectedData = item.copy(consignor = Some(consignor))
          await(controller(user, Some(item)).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce())
            .insert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem), eqTo(expectedData))(any(), any(), any())
        }
      }
    }
  }
}