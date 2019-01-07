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

package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.AppConfig
import domain.{DeclarationFormats, GovernmentAgencyGoodsItem}
import domain.DeclarationFormats.governmentAgencyGoodsItemFormats
import play.api.libs.json.{Format, Reads, Writes}
import uk.gov.hmrc.crypto.{ApplicationCrypto, CompositeSymmetricCrypto}
import uk.gov.hmrc.http.cache.client.{CacheMap, ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.http.{HeaderCarrier, HttpDelete, HttpGet, HttpPut}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsHttpCaching @Inject()(cfg: AppConfig, httpClient: HttpClient) extends ShortLivedHttpCaching {

  override def defaultSource: String = cfg.keyStoreSource

  override def baseUri: String = cfg.keyStoreUrl

  override def domain: String = cfg.sessionCacheDomain

  override def http: HttpGet with HttpPut with HttpDelete = httpClient

}

@ImplementedBy(classOf[CustomsCacheServiceImpl])
trait CustomsCacheService {

  def get(cacheName: String, eori: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Map[String, String]]]

  def put(cacheName: String, eori: String, data: Map[String, String])
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CacheMap]

  def getGoodsItems(eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[List[GovernmentAgencyGoodsItem]]]

  def getAGoodsItem(eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[GovernmentAgencyGoodsItem]]

  def saveGoodsItem(eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]

  def putGoodsItem(eori: String, item: GovernmentAgencyGoodsItem = GovernmentAgencyGoodsItem())(implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[CacheMap]

  def getForm[A](eori: String, key: String)(implicit format: Format[A], hc: HeaderCarrier,
    ec: ExecutionContext): Future[Option[A]]

  def putForm[A](eori: String, cacheId: String, form: A)(implicit format: Format[A], hc: HeaderCarrier, ec: ExecutionContext): Future[CacheMap]
}

@Singleton
class CustomsCacheServiceImpl @Inject()(caching: CustomsHttpCaching, applicationCrypto: ApplicationCrypto) extends CustomsCacheService with ShortLivedCache {

  override implicit val crypto: CompositeSymmetricCrypto = applicationCrypto.JsonCrypto

  override def shortLiveCache: ShortLivedHttpCaching = caching

  val GOV_AGENCY_GOODS_ITEMS_LIST_CACHE_KEY = "GovAgencyGoodsItems"
  val GOV_AGENCY_GOODS_ITEM_CACHE_KEY = "GovAgencyGoodsItem"

  override def get(cacheName: String, eori: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Map[String, String]]] =
    fetchAndGetEntry[Map[String, String]](cacheName, eori)

  override def put(cacheName: String, eori: String, data: Map[String, String])
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CacheMap] = cache(cacheName, eori, data)

  def getGoodsItems(eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[List[GovernmentAgencyGoodsItem]]]
  = fetchAndGetEntry[List[GovernmentAgencyGoodsItem]](eori, GOV_AGENCY_GOODS_ITEMS_LIST_CACHE_KEY)

  def getAGoodsItem(eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[GovernmentAgencyGoodsItem]]
  = fetchAndGetEntry[GovernmentAgencyGoodsItem](eori, GOV_AGENCY_GOODS_ITEM_CACHE_KEY)

  def saveGoodsItem(eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    fetchAndGetEntry[GovernmentAgencyGoodsItem](eori, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).flatMap {
      goodsItem =>
        fetchAndGetEntry[List[GovernmentAgencyGoodsItem]](eori, GOV_AGENCY_GOODS_ITEMS_LIST_CACHE_KEY).flatMap {
          goodsItemsList =>
            val listToSave = if (goodsItemsList.isDefined && goodsItem.isDefined)
              goodsItemsList.get :+ goodsItem.get
            else if (goodsItemsList.isEmpty && goodsItem.isDefined)
              List(goodsItem.get)
            else
              List.empty

            cache[List[GovernmentAgencyGoodsItem]](eori, GOV_AGENCY_GOODS_ITEMS_LIST_CACHE_KEY, listToSave).map(res => true)

        }
    }

  def putGoodsItem(eori: String, item: GovernmentAgencyGoodsItem = GovernmentAgencyGoodsItem())(implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[CacheMap] =
    cache[GovernmentAgencyGoodsItem](eori, GOV_AGENCY_GOODS_ITEM_CACHE_KEY, item)

  def getForm[A](eori: String, key: String)(implicit format: Format[A], hc: HeaderCarrier, ec: ExecutionContext):
  Future[Option[A]] = fetchAndGetEntry[A](eori, key)

  def putForm[A](eori: String, cacheId: String, form: A)(implicit format: Format[A], hc: HeaderCarrier, ec: ExecutionContext): Future[CacheMap] = cache(eori, cacheId, form)
}