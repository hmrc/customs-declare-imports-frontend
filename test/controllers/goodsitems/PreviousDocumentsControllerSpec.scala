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
import uk.gov.hmrc.wco.dec.PreviousDocument
import views.html.goods_items_previousdocs

import scala.concurrent.Future

class PreviousDocumentsControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with MockitoSugar
  with EndpointBehaviours {

  private def form = Form(previousDocumentMapping)

  val previousDocsPageUri = "/submit-declaration-goods/add-previous-documents"

  private def controller(user: Option[SignedInUser]) =
    new PreviousDocumentsController(new FakeActions(user), mockCustomsCacheService)

  val goodsItemGen = option(arbitrary[GovernmentAgencyGoodsItem])

  def view(form: Form[PreviousDocument] = form, previousDocuments: Seq[PreviousDocument] = Seq()): String =
    goods_items_previousdocs(form, previousDocuments)(fakeRequest, messages, appConfig).body

  ".onPageLoad" should {

    behave like okEndpoint(previousDocsPageUri)
    behave like authenticatedEndpoint(previousDocsPageUri)

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

          when(mockCustomsCacheService.getByKey(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem))(any(), any(), any()))
            .thenReturn(Future.successful(data))
          withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, data) {
            val result = controller(Some(user)).onPageLoad(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe view(previousDocuments = data.map(_.previousDocuments).getOrElse(Seq.empty))
          }
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint(previousDocsPageUri, POST)
    behave like authenticatedEndpoint(previousDocsPageUri, POST)

    "return OK" when {

      "user submits valid data" in {

        forAll { (user: SignedInUser, previousDocument: PreviousDocument, governmentAgencyGoodsItem: GovernmentAgencyGoodsItem) =>
          when(mockCustomsCacheService.getByKey(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem))(any(), any(), any()))
            .thenReturn(Future.successful(Some(governmentAgencyGoodsItem)))
          when(mockCustomsCacheService.cache[GovernmentAgencyGoodsItem](any(), any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(CacheMap("id1", Map.empty)))
          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(previousDocument): _*)
          val result = controller(Some(user)).onSubmit(request)

          status(result) mustBe OK
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
            gp <- arbitrary[PreviousDocument]
            categoryCode <- stringsLongerThan(3)
            typeCode <- stringsLongerThan(3)
            id <- stringsLongerThan(70)
          } yield gp.copy(categoryCode = Some(categoryCode), typeCode = Some(typeCode), id = Some(id))

        forAll(arbitrary[SignedInUser], badData, goodsItemGen) {
          case (user, formData, data) =>
            withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, data) {
              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(formData): _*)
              val badForm = form.fillAndValidate(formData)
              val result = controller(Some(user)).onSubmit(request)

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(badForm, data.map(_.previousDocuments).getOrElse(Seq.empty))
            }
        }
      }
    }

    "saves data in cache" when {

      "valid data is provided" in {

        forAll { (user: SignedInUser, previousDocument: PreviousDocument, governmentAgencyGoodsItem: GovernmentAgencyGoodsItem) =>
          when(mockCustomsCacheService.getByKey(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem))(any(), any(), any()))
            .thenReturn(Future.successful(Some(governmentAgencyGoodsItem)))
          when(mockCustomsCacheService.cache[GovernmentAgencyGoodsItem](any(), any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(CacheMap("id1", Map.empty)))
          val request = fakeRequest.withFormUrlEncodedBody(asFormParams(previousDocument): _*)
          await(controller(Some(user)).onSubmit(request))

          verify(mockCustomsCacheService, atLeastOnce())
            .cache[GovernmentAgencyGoodsItem](eqTo(user.eori.value), eqTo(CacheKey.goodsItem.key), any())(any(), any(), any())
        }
      }
    }
  }
}