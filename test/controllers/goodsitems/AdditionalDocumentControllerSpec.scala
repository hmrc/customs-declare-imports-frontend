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
import uk.gov.hmrc.wco.dec.GovernmentAgencyGoodsItemAdditionalDocument
import views.html.gov_agency_goods_items_add_docs

import scala.concurrent.Future

class AdditionalDocumentControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with MockitoSugar
  with EndpointBehaviours {

  private def form = Form(govtAgencyGoodsItemAddDocMapping)

  val additionalDocsPageUri = "/submit-declaration-goods/add-gov-agency-goods-items-additional-docs"

  private def controller(user: Option[SignedInUser]) =
    new AdditionalDocumentController(new FakeActions(user), mockCustomsCacheService)

  val goodsItemGen = option(arbitrary[GovernmentAgencyGoodsItem])

  def view(form: Form[GovernmentAgencyGoodsItemAdditionalDocument] = form, additionalDocs: Seq[GovernmentAgencyGoodsItemAdditionalDocument] = Seq()): String =
    gov_agency_goods_items_add_docs(form, additionalDocs)(fakeRequest, messages, appConfig).body

  ".onPageLoad" should {

    behave like okEndpoint(additionalDocsPageUri)
    behave like authenticatedEndpoint(additionalDocsPageUri)

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
            contentAsString(result) mustBe view(additionalDocs = data.map(_.additionalDocuments).getOrElse(Seq.empty))
          }
      }
    }
  }

  ".onSubmit" should {

    behave like badRequestEndpoint(additionalDocsPageUri, POST)
    behave like authenticatedEndpoint(additionalDocsPageUri, POST)

    "return OK" when {
      "user submits valid data" in {
        forAll (arbitrary[SignedInUser],arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], goodsItemGen) { case (user: SignedInUser, additionalDocument, goodsItem) =>
          withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, goodsItem) {
            val request = fakeRequest.withFormUrlEncodedBody(asFormParams(additionalDocument.copy(effectiveDateTime = None)): _*)
            val result = controller(Some(user)).onSubmit(request)
            status(result) mustBe OK
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
            gp <- arbitrary[GovernmentAgencyGoodsItemAdditionalDocument]
            categoryCode <- stringsLongerThan(6)
            id <- minStringLength(36)
            typeCode <- minStringLength(4)
          } yield gp.copy(categoryCode = Some(categoryCode), effectiveDateTime = None, id = Some(id), typeCode = Some(typeCode))

        forAll(arbitrary[SignedInUser], badData, goodsItemGen) {
          case (user, formData, data) =>
            withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, data) {
              val request = fakeRequest.withFormUrlEncodedBody(asFormParams(formData): _*)
              val badForm = form.fillAndValidate(formData)
              val result = controller(Some(user)).onSubmit(request)

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(badForm, data.map(_.additionalDocuments).getOrElse(Seq.empty))
            }
        }
      }
    }

    "saves data in cache" when {

      "valid data is provided" in {

        forAll { (user: SignedInUser, additionalDoc: GovernmentAgencyGoodsItemAdditionalDocument, goodsItem: GovernmentAgencyGoodsItem) =>
          withCleanCache(EORI(user.eori.value), CacheKey.goodsItem, Some(goodsItem)) {
            val request = fakeRequest.withFormUrlEncodedBody(asFormParams(additionalDoc.copy(effectiveDateTime = None)): _*)
            await(controller(Some(user)).onSubmit(request))

            verify(mockCustomsCacheService, atLeastOnce())
              .insert(eqTo(EORI(user.eori.value)), eqTo(CacheKey.goodsItem), any())(any(), any(), any())
          }
        }
      }
    }
  }
}