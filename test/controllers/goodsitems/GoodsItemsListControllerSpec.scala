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

import domain.GovernmentAgencyGoodsItem
import domain.auth.SignedInUser
import generators.Generators
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen.{listOf, option}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.prop.PropertyChecks
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.assertions.{HtmlAssertions, HttpAssertions}
import uk.gov.hmrc.customs.test.behaviours._
import views.html.gov_agency_goods_items_list

class GoodsItemsListControllerSpec extends CustomsSpec
  with AuthenticationBehaviours
  with FeatureBehaviours
  with RequestHandlerBehaviours
  with HttpAssertions
  with HtmlAssertions
  with Generators
  with BeforeAndAfterEach
  with PropertyChecks {

  val get = "GET"
  val goodsItemsListUri = uriWithContextPath("/submit-declaration-goods/gov-agency-goods-items")
  val addGoodsItemUri = uriWithContextPath("/submit-declaration-goods/save-goods-item")

  val goodsItemsListGen = option(listOf(arbitrary[GovernmentAgencyGoodsItem]))
  val goodsItemGen = option(arbitrary[GovernmentAgencyGoodsItem])

  def view(goodsItemsList: Seq[GovernmentAgencyGoodsItem] = Seq()): String =
    gov_agency_goods_items_list(goodsItemsList)(fakeRequest, messages, appConfig).body

  "onPageLoad" should {

    "require Authentication" in {
      withoutSignedInUser() { (_, _) =>
        withRequest(get, goodsItemsListUri) { resp =>
          wasRedirected(ggLoginRedirectUri(goodsItemsListUri), resp)
        }
      }
    }

    " return 200" in {
      withCaching(None)
      withSignedInUser() { (headers, session, tags) =>
        withRequest(get, goodsItemsListUri, headers, session, tags) {
          wasOk
        }
      }
    }

    "display goodsItem data from cache" in {
      withSignedInUser() { (headers, session, tags) =>
        forAll(goodsItemsListGen) {
          case (data) =>
            withCaching(data)
            withRequest(get, goodsItemsListUri, headers, session, tags) { resp =>
              val content = contentAsString(resp)
              content mustBe view(data.getOrElse(List()))
            }
        }
      }
    }
  }

  "saveGoodsItem" should {

    "require Authentication" in {
      withoutSignedInUser() { (_, _) =>
        withRequest(get, addGoodsItemUri) { resp =>
          wasRedirected(ggLoginRedirectUri(addGoodsItemUri), resp)
        }
      }
    }

    "redirected to goods Items page" in {
      withCaching(None)
      withSignedInUser() { (headers, session, tags) =>
        withRequest(get, addGoodsItemUri, headers, session, tags) { resp =>
          status(resp) must be(SEE_OTHER)
          val header = resp.futureValue.header
          header.headers.get("Location") must be(Some(goodsItemsListUri))
        }
      }
    }
  }
}