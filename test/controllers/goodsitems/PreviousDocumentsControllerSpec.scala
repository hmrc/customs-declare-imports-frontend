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
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItem, PreviousDocument}
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

  private def controller(user: Option[SignedInUser], item: Option[GovernmentAgencyGoodsItem]) =
    new PreviousDocumentsController(new FakeActions(user, item), mockCustomsCacheService)

  val goodsItemGen = option(arbitrary[GovernmentAgencyGoodsItem])

  def view(form: Form[PreviousDocument] = form, previousDocuments: Seq[PreviousDocument] = Seq()): String =
    goods_items_previousdocs(form, previousDocuments)(fakeRequest, messages, appConfig).body

  ".onPageLoad" should {

    behave like badRequestEndpoint(previousDocsPageUri)
    behave like authenticatedEndpoint(previousDocsPageUri)

    "return OK" when {

      "user is signed in" in {

        forAll { (user: SignedInUser, goodsItem: GovernmentAgencyGoodsItem) =>

          val result = controller(Some(user), Some(goodsItem)).onPageLoad()(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view(previousDocuments = goodsItem.previousDocuments)
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

      forAll(arbitrary[SignedInUser], goodsItemGen, arbitrary[GovernmentAgencyGoodsItem]) {
        case (user, data, item) =>
            val result = controller(Some(user), Some(item)).onPageLoad(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe view(previousDocuments = item.previousDocuments)
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
          val result = controller(Some(user), Some(governmentAgencyGoodsItem)).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.PreviousDocumentsController.onPageLoad().url)        }
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
            gp <- arbitrary[PreviousDocument]
            categoryCode <- stringsLongerThan(3)
            typeCode <- stringsLongerThan(3)
            id <- stringsLongerThan(70)
          } yield gp.copy(categoryCode = Some(categoryCode), typeCode = Some(typeCode), id = Some(id))

        forAll(arbitrary[SignedInUser], badData, goodsItemGen, arbitrary[GovernmentAgencyGoodsItem]) {
          case (user, formData, data, item) =>
              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(formData): _*)
              val badForm = form.fillAndValidate(formData)
              val result = controller(Some(user), Some(item)).onSubmit(request)

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(badForm, item.previousDocuments)
        }
      }
    }

    "saves data in cache" when {

      "valid data is provided" in {

        forAll { (user: SignedInUser, previousDocument: PreviousDocument, governmentAgencyGoodsItem: GovernmentAgencyGoodsItem) =>

          withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, Some(governmentAgencyGoodsItem)) {
            val request = fakeRequest.withFormUrlEncodedBody(asFormParams(previousDocument): _*)
            await(controller(Some(user), Some(governmentAgencyGoodsItem)).onSubmit(request))

            val prevDocs = governmentAgencyGoodsItem.previousDocuments :+ previousDocument
            val expected = governmentAgencyGoodsItem.copy(previousDocuments = prevDocs)

            verify(mockCustomsCacheService, atLeastOnce())
              .insert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem), eqTo(expected))(any(), any(), any())
          }
        }
      }
    }
  }
}