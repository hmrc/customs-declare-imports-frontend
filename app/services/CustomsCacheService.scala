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
import domain.auth.EORI
import play.api.libs.json.{Reads, Writes}
import services.cachekeys.CacheKey
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

@Singleton
class CustomsCacheService @Inject()(caching: CustomsHttpCaching, applicationCrypto: ApplicationCrypto) extends ShortLivedCache {

  override implicit val crypto: CompositeSymmetricCrypto = applicationCrypto.JsonCrypto

  override def shortLiveCache: ShortLivedHttpCaching = caching


  def get(cacheName: String, eori: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Map[String, String]]] =
    fetchAndGetEntry[Map[String, String]](cacheName, eori)

  def put(cacheName: String, eori: String, data: Map[String, String])
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CacheMap] = cache(cacheName, eori, data)

  def getByKey[T](cacheId: EORI, key: CacheKey[T])(implicit hc: HeaderCarrier, rds: Reads[T], executionContext: ExecutionContext): Future[Option[T]] =
    fetchAndGetEntry[T](cacheId.value, key.key)

  def upsert[T](cacheId: EORI, key: CacheKey[T])(insert: () => T, update: T => T)
               (implicit hc: HeaderCarrier, rds: Reads[T], wts: Writes[T], executionContext: ExecutionContext): Future[Unit] =
    getByKey(cacheId, key).flatMap { optT =>
      val t = optT.fold(insert())(update)
      this.insert(cacheId, key, t)
    }

  def insert[T](cacheId: EORI, key: CacheKey[T], data: T)
               (implicit hc: HeaderCarrier, wts: Writes[T], executionContext: ExecutionContext): Future[Unit] = {
    cache(cacheId.value, key.key, data).map(_ => ())
  }
}