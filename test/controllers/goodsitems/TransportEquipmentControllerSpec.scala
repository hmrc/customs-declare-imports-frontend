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
import org.scalacheck.Gen._
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.behaviours.{CustomsSpec, EndpointBehaviours}
import uk.gov.hmrc.wco.dec.{Commodity, GovernmentAgencyGoodsItem, TransportEquipment}
import views.html.goods_item_transportequipment

class TransportEquipmentControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with MockitoSugar
  with EndpointBehaviours {

  private def form = Form(transportEquipmentMapping)

  val transportEquipmenntPageUri = "/submit-declaration-goods/add-container-details"

  private def controller(user: Option[SignedInUser]) =
    new TransportEquipmentController(new FakeActions(user), mockCustomsCacheService)

  val goodsItemGen = option(arbitrary[GovernmentAgencyGoodsItem])

  def view(form: Form[TransportEquipment] = form, transportEquipments: Seq[TransportEquipment] = Seq()): String =
    goods_item_transportequipment(form, transportEquipments)(fakeRequest, messages, appConfig).body

  ".onPageLoad" should {

    behave like okEndpoint(transportEquipmenntPageUri)
    behave like authenticatedEndpoint(transportEquipmenntPageUri)

    "return OK" when {

      "user is signed in" in {

        forAll { user: SignedInUser =>

          val result = controller(Some(user)).onPageLoad()(fakeRequest)

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

      forAll(arbitrary[SignedInUser], goodsItemGen) {
        case (user, data) =>
          withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, data) {
            val result = controller(Some(user)).onPageLoad(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe view(transportEquipments = data.flatMap(_.commodity.map(_.transportEquipments)).getOrElse(Seq.empty))
          }
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint(transportEquipmenntPageUri, POST)
    behave like authenticatedEndpoint(transportEquipmenntPageUri, POST)

    "return OK" when {

      "user submits valid data" in {

        forAll { (user: SignedInUser, transport: TransportEquipment, governmentAgencyGoodsItem: GovernmentAgencyGoodsItem) =>

          withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, Some(governmentAgencyGoodsItem)) {

            val request = fakeRequest.withFormUrlEncodedBody(asFormParams(transport): _*)
            val result = controller(Some(user)).onSubmit(request)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.TransportEquipmentController.onPageLoad().url)
          }
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user does not have an eori" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(Some(user.user)).onSubmit(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "return BAD_REQUEST" when {

      "user submits invalid data" in {

        val badData = stringsLongerThan(17).map(s => TransportEquipment(0, Some(s)))

        forAll(arbitrary[SignedInUser], badData, goodsItemGen) {
          case (user, formData, data) =>
            withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, data) {
              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(formData): _*)
              val badForm = form.fillAndValidate(formData)
              val result = controller(Some(user)).onSubmit(request)

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(badForm, data.flatMap(_.commodity.map(_.transportEquipments)).getOrElse(Seq.empty))
            }
        }
      }
    }

    "saves data in cache" when {

      "valid data is provided" in {

        forAll {
          (user: SignedInUser, transport: TransportEquipment, governmentAgencyGoodsItem: GovernmentAgencyGoodsItem) =>

            withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, Some(governmentAgencyGoodsItem)) {
            println("1.GovernmentAgencyGoodsItem --> " + governmentAgencyGoodsItem.toString)
              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(transport): _*)
              await(controller(Some(user)).onSubmit(request))

              val commodity = governmentAgencyGoodsItem.commodity.fold(Some(Commodity(transportEquipments =
                Seq(transport))))(com => Some(com.copy(transportEquipments =
                com.transportEquipments :+ transport.copy(com.transportEquipments.maxBy(_.sequenceNumeric).sequenceNumeric + 1))))
              val expected = governmentAgencyGoodsItem.copy(commodity = commodity)
              println("3.EXP GovernmentAgencyGoodsItem --> " + expected.toString)

              verify(mockCustomsCacheService, atLeastOnce())
                .insert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem), eqTo(expected))(any(), any(), any())
            }
        }
      }
    }
  }
}