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
import uk.gov.hmrc.wco.dec.{Commodity, DutyTaxFee, GovernmentAgencyGoodsItem}
import views.html.goods_items_dutytaxfee

class DutyTaxFeeControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with MockitoSugar
  with EndpointBehaviours {

  private def form = Form(dutyTaxFeeMapping)

  val dutyTaxesPageUri = "/submit-declaration-goods/add-commodity-taxes"

  private def controller(user: Option[SignedInUser]) =
    new DutyTaxFeeController(new FakeActions(user), mockCustomsCacheService)

  val goodsItemGen = option(arbitrary[GovernmentAgencyGoodsItem])

  def view(form: Form[DutyTaxFee] = form, taxes: Seq[DutyTaxFee] = Seq()): String =
    goods_items_dutytaxfee(form, taxes)(fakeRequest, messages, appConfig).body

  ".onPageLoad" should {

    behave like okEndpoint(dutyTaxesPageUri)
    behave like authenticatedEndpoint(dutyTaxesPageUri)

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
            contentAsString(result) mustBe view(taxes= data.flatMap(_.commodity.map(_.dutyTaxFees)).getOrElse(Seq.empty))
          }
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint(dutyTaxesPageUri, POST)
    behave like authenticatedEndpoint(dutyTaxesPageUri, POST)

    "return OK" when {

      "user submits valid data" in {

        forAll { (user: SignedInUser, dutyTaxFee: DutyTaxFee, governmentAgencyGoodsItem: GovernmentAgencyGoodsItem) =>

          withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, Some(governmentAgencyGoodsItem)) {

            val request = fakeRequest.withFormUrlEncodedBody(asFormParams(dutyTaxFee): _*)
            val result = controller(Some(user)).onSubmit(request)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.DutyTaxFeeController.onPageLoad().url)
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

        val badData =
          for {
            dutyTax <- arbitrary[DutyTaxFee]
            typeCode <- stringsLongerThan(3)
          } yield dutyTax.copy(typeCode = Some(typeCode))

        forAll(arbitrary[SignedInUser], badData, goodsItemGen) {
          case (user, formData, data) =>
            withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, data) {
              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(formData): _*)
              val badForm = form.fillAndValidate(formData)
              val result = controller(Some(user)).onSubmit(request)

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(badForm, data.flatMap(_.commodity.map(_.dutyTaxFees)).getOrElse(Seq.empty))
            }
        }
      }
    }

    "saves data in cache" when {

      "valid data is provided" in {

        forAll {
          (user: SignedInUser, dutyTaxFee: DutyTaxFee, governmentAgencyGoodsItem: GovernmentAgencyGoodsItem) =>

            withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, Some(governmentAgencyGoodsItem)) {

              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(dutyTaxFee): _*)
              await(controller(Some(user)).onSubmit(request))

              val commodity = governmentAgencyGoodsItem.commodity.fold(Some(Commodity(dutyTaxFees = Seq(dutyTaxFee))))(d => Some(d.copy(dutyTaxFees =  d.dutyTaxFees :+ dutyTaxFee)))
              val expected = governmentAgencyGoodsItem.copy(commodity = commodity)

              verify(mockCustomsCacheService, atLeastOnce())
                .insert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem), eqTo(expected))(any(), any(), any())
            }
        }
      }
    }
  }
}