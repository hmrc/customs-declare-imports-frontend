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
import uk.gov.hmrc.wco.dec.{ChargeDeduction, CustomsValuation, GovernmentAgencyGoodsItem}
import views.html.goods_item_adds_and_deductions

class AdditionsAndDeductionsControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with MockitoSugar
  with EndpointBehaviours {

  private def form = Form(goodsChargeDeductionMapping)

  val additionsDeductionsPageUri = "/submit-declaration-goods/add-charge-deductions"

  private def controller(user: Option[SignedInUser]) =
    new AdditionsAndDeductionsController(new FakeActions(user), mockCustomsCacheService)

  val goodsItemGen = option(arbitrary[GovernmentAgencyGoodsItem])

  def view(form: Form[ChargeDeduction] = form, taxes: Seq[ChargeDeduction] = Seq()): String =
    goods_item_adds_and_deductions(form, taxes)(fakeRequest, messages, appConfig).body

  ".onPageLoad" should {

    behave like okEndpoint(additionsDeductionsPageUri)
    behave like authenticatedEndpoint(additionsDeductionsPageUri)

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
            contentAsString(result) mustBe view(taxes = data.flatMap(_.customsValuation.map(_.chargeDeductions)).getOrElse(Seq.empty))
          }
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint(additionsDeductionsPageUri, POST)
    behave like authenticatedEndpoint(additionsDeductionsPageUri, POST)

    "return OK" when {

      "user submits valid data" in {

        forAll { (user: SignedInUser, chargeDeduction: ChargeDeduction, governmentAgencyGoodsItem: GovernmentAgencyGoodsItem) =>

          withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, Some(governmentAgencyGoodsItem)) {

            val request = fakeRequest.withFormUrlEncodedBody(asFormParams(chargeDeduction): _*)
            val result = controller(Some(user)).onSubmit(request)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.AdditionsAndDeductionsController.onPageLoad().url)
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

        val badData = for {
          charge <- arbitrary[ChargeDeduction]
          code <- minStringLength(4)
        } yield charge.copy(chargesTypeCode = Some(code))

        forAll(arbitrary[SignedInUser], badData, goodsItemGen) {
          case (user, formData, data) =>
            withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, data) {
              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(formData): _*)
              val badForm = form.fillAndValidate(formData)
              val result = controller(Some(user)).onSubmit(request)

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(badForm, data.flatMap(_.customsValuation.map(_.chargeDeductions)).getOrElse(Seq.empty))
            }
        }
      }
    }

    "saves data in cache" when {

      "valid data is provided" in {

        forAll {
          (user: SignedInUser, chargeDeduction: ChargeDeduction, governmentAgencyGoodsItem: GovernmentAgencyGoodsItem) =>

            withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, Some(governmentAgencyGoodsItem)) {

              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(chargeDeduction): _*)
              await(controller(Some(user)).onSubmit(request))

              val customsValuation = governmentAgencyGoodsItem.customsValuation.fold(
                Some(CustomsValuation(chargeDeductions = Seq(chargeDeduction))))(d =>
                Some(CustomsValuation(chargeDeductions = d.chargeDeductions :+ chargeDeduction)))

              val expected = governmentAgencyGoodsItem.copy(customsValuation = customsValuation)

              verify(mockCustomsCacheService, atLeastOnce())
                .insert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem), eqTo(expected))(any(), any(), any())
            }
        }
      }
    }
  }
}