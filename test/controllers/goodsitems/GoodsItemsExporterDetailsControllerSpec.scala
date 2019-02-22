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
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.behaviours.{CustomsSpec, EndpointBehaviours}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItem, NamedEntityWithAddress}
import views.html.goodsitems.goods_items_exporter_details

import scala.concurrent.Future

class GoodsItemsExporterDetailsControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with EndpointBehaviours {

  val form = Form(namedEntityWithAddressMapping)

  def view(form: Form[_] = form): String =
    goods_items_exporter_details(form)(fakeRequest, messages, appConfig).body

  def controller(user: Option[SignedInUser], item: Option[GovernmentAgencyGoodsItem]) =
    new GoodsItemsExporterDetailsController(new FakeActions(user, item), mockCustomsCacheService)

  val locationOfGoodsGen: Gen[Option[NamedEntityWithAddress]] = option(arbitrary[NamedEntityWithAddress])
  val uri = "/submit-declaration-goods/goods-item-exporter-details"

  "onPageLoad" should {

    behave like badRequestEndpoint(uri)
    behave like authenticatedEndpoint(uri)

    "return OK" when {

      "user is signed in" in {

        forAll { (user: SignedInUser, goodsItem: GovernmentAgencyGoodsItem) =>

          val result = controller(Some(user), Some(goodsItem)).onPageLoad()(fakeRequest)
          val popForm = goodsItem.consignor.fold(form)(form.fill)

          status(result) mustBe OK
          contentAsString(result) mustBe view(popForm)
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

      forAll(arbitrary[SignedInUser], arbitrary[GovernmentAgencyGoodsItem]) {
        case (user, item) =>

          val result = controller(Some(user), Some(item)).onPageLoad(fakeRequest)
          val popForm = item.consignor.fold(form)(form.fill)

          status(result) mustBe OK
          contentAsString(result) mustBe view(popForm)
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint(uri, POST)
    behave like authenticatedEndpoint(uri, POST)

    "return OK" when {

      "user submits valid data" in {

        forAll { (user: SignedInUser, entity: NamedEntityWithAddress, goodsItem: GovernmentAgencyGoodsItem) =>

          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(entity): _*)
          val result = controller(Some(user), Some(goodsItem)).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.goodsitems.routes.GoodsItemsSellerDetailsController.onPageLoad().url)
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
            entity    <- arbitrary[NamedEntityWithAddress]
            invalidId <- stringsLongerThan(18)
          } yield entity.copy(id = Some(invalidId))

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

        forAll { (user: SignedInUser, entity: NamedEntityWithAddress, goodsItem: GovernmentAgencyGoodsItem) =>

          withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, Some(goodsItem)) {
            val request = fakeRequest.withFormUrlEncodedBody(asFormParams(entity): _*)
            await(controller(Some(user), Some(goodsItem)).onSubmit(request))

            val expected = goodsItem.copy(consignor = Some(entity))

            verify(mockCustomsCacheService, atLeastOnce())
              .insert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem), eqTo(expected))(any(), any(), any())
          }
        }
      }
    }
  }
}
