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
import domain.GovernmentAgencyGoodsItem
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
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.GovernmentProcedure
import views.html.goods_items_government_procedures

import scala.concurrent.Future

class GovernmentProceduresControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with MockitoSugar
  with EndpointBehaviours {

  private def form = Form(governmentProcedureMapping)

  val addGovtProcedureCodesPageUri = "/submit-declaration-goods/add-government-procedures"

  private def controller(user: Option[SignedInUser]) =
    new GovernmentProceduresController(new FakeActions(user), mockCustomsCacheService)

  val goodsItemGen = option(arbitrary[GovernmentAgencyGoodsItem])

  def view(form: Form[GovernmentProcedure] = form, govProcedures: Seq[GovernmentProcedure] = Seq()): String =
    goods_items_government_procedures(form, govProcedures)(fakeRequest, messages, appConfig).body

  ".onPageLoad" should {

    behave like okEndpoint(addGovtProcedureCodesPageUri)
    behave like authenticatedEndpoint(addGovtProcedureCodesPageUri)

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
            contentAsString(result) mustBe view(govProcedures = data.map(_.governmentProcedures).getOrElse(Seq.empty))
          }
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint(addGovtProcedureCodesPageUri, POST)
    behave like authenticatedEndpoint(addGovtProcedureCodesPageUri, POST)

    "return OK" when {

      "user submits valid data" in {

        forAll { (user: SignedInUser, governmentProcedure: GovernmentProcedure, governmentAgencyGoodsItem: GovernmentAgencyGoodsItem) =>
          when(mockCustomsCacheService.getByKey(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem))(any(), any(), any()))
            .thenReturn(Future.successful(Some(governmentAgencyGoodsItem)))
          when(mockCustomsCacheService.cache[GovernmentAgencyGoodsItem](any(), any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(CacheMap("id1", Map.empty)))
          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(governmentProcedure): _*)
          val result = controller(Some(user)).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.GovernmentProceduresController.onPageLoad().url)        }
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
            gp <- arbitrary[GovernmentProcedure]
            current <- stringsLongerThan(2)
            previous <- stringsLongerThan(2)
          } yield gp.copy(currentCode = Some(current), previousCode = Some(previous))

        forAll(arbitrary[SignedInUser], badData, goodsItemGen) {
          case (user, invalidFormData, data) =>
            withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, data) {

              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(invalidFormData): _*)
              val badForm = form.fillAndValidate(invalidFormData)
              val result = controller(Some(user)).onSubmit(request)

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(badForm, data.map(_.governmentProcedures).getOrElse(Seq.empty))
            }
        }
      }
    }

    "saves data in cache" when {

      "valid data is provided" in {

        forAll { (user: SignedInUser, governmentProcedure: GovernmentProcedure, governmentAgencyGoodsItem: GovernmentAgencyGoodsItem) =>
          when(mockCustomsCacheService.getByKey(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem))(any(), any(), any()))
            .thenReturn(Future.successful(Some(governmentAgencyGoodsItem)))
          when(mockCustomsCacheService.cache[GovernmentAgencyGoodsItem](any(), any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(CacheMap("id1", Map.empty)))
          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(governmentProcedure): _*)
          await(controller(Some(user)).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce())
            .cache[GovernmentAgencyGoodsItem](eqTo(user.eori.value), eqTo(CacheKey.goodsItem.key), any())(any(), any(), any())
        }
      }
    }
  }
}