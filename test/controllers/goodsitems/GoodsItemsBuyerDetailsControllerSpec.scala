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
import forms.DeclarationFormMapping._
import generators.Generators
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary._
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.behaviours.{CustomsSpec, EndpointBehaviours}
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItem, ImportExportParty}
import views.html.goodsitems.goods_items_buyer_details

class GoodsItemsBuyerDetailsControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with EndpointBehaviours {


  val form = Form(importExportPartyMapping)

  def view(form: Form[_]): String =
    goods_items_buyer_details(form)(fakeRequest, messages, appConfig).body

  def controller(user: Option[SignedInUser], item: Option[GovernmentAgencyGoodsItem]) =
    new GoodsItemsBuyerDetailsController(new FakeActions(user, item), mockCustomsCacheService)

  val uri = "/submit-declaration-goods/goods-item-buyer-details"

  "onPageLoad" should {

    behave like badRequestEndpoint(uri)
    behave like authenticatedEndpoint(uri)

    "return OK" when {

      "user is signed in" in {

        forAll { (signedInUser: SignedInUser, goodsItem: GovernmentAgencyGoodsItem) =>

          val result = controller(Some(signedInUser), Some(goodsItem)).onPageLoad()(fakeRequest)

          status(result) mustBe OK
        }
      }
    }

    "return Unauthorized" when {

      "user doesn't have an eori" in {

        forAll { (user: UnauthenticatedUser, goodsItem: GovernmentAgencyGoodsItem) =>

          val result = controller(Some(user.user), Some(goodsItem)).onPageLoad(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "load data from cache" in {

      forAll { (signedInUser: SignedInUser, goodsItem: GovernmentAgencyGoodsItem) =>

        val result = controller(Some(signedInUser), Some(goodsItem)).onPageLoad()(fakeRequest)
        val popForm = goodsItem.buyer.fold(form)(form.fill)

        status(result) mustBe OK
        contentAsString(result) mustBe view(popForm)
      }
    }
  }

  "onSubmit" should {

    behave like badRequestEndpoint(uri, POST)
    behave like authenticatedEndpoint(uri, POST)

    "return OK" when {

      "user submits valid data" in {

        forAll { (user: SignedInUser, importExportParty: ImportExportParty, goodsItem: GovernmentAgencyGoodsItem) =>

          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(importExportParty): _*)
          val result = controller(Some(user), Some(goodsItem)).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage().url)
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user does not have an eori" in {

        forAll { (user: UnauthenticatedUser, goodsItem: GovernmentAgencyGoodsItem) =>

          val result = controller(Some(user.user), Some(goodsItem)).onSubmit(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "return BAD_REQUEST" when {

      "user submits invalid data" in {

        val badData =
          for {
            importExportParty <- arbitrary[ImportExportParty]
            invalidId <- stringsLongerThan(18)
          } yield importExportParty.copy(id = Some(invalidId))

        forAll(arbitrary[SignedInUser], badData, arbitrary[GovernmentAgencyGoodsItem]) {
          case (user, formData, goodsItem) =>
            val request = fakeRequest.withFormUrlEncodedBody(asFormParams(formData): _*)
            val badForm = form.fillAndValidate(formData)
            val result = controller(Some(user), Some(goodsItem)).onSubmit(request)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe view(badForm)
        }
      }
    }

    "saves data in cache" when {

      "valid data is provided" in {

        forAll { (user: SignedInUser, importExportParty: ImportExportParty, goodsItem: GovernmentAgencyGoodsItem) =>

          withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, Some(goodsItem)) {
            val request = fakeRequest.withFormUrlEncodedBody(asFormParams(importExportParty): _*)
            await(controller(Some(user), Some(goodsItem)).onSubmit(request))

            val expected = goodsItem.copy(buyer = Some(importExportParty))

            verify(mockCustomsCacheService, atLeastOnce())
              .insert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem), eqTo(expected))(any(), any(), any())
          }
        }
      }
    }
  }
}
