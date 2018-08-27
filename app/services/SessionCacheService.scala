/*
 * Copyright 2018 HM Revenue & Customs
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

import com.google.inject.{Inject, Singleton}
import config.AppConfig
import play.api.Logger
import uk.gov.hmrc.http.cache.client.{CacheMap, HttpCaching}
import uk.gov.hmrc.http.{HeaderCarrier, HttpDelete, HttpGet, HttpPut}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionCacheService @Inject()(appConfig: AppConfig, httpClient: HttpClient) extends HttpCaching {

  override def defaultSource: String = appConfig.keyStoreSource

  override def baseUri: String = appConfig.keyStoreUrl

  override def domain: String = appConfig.sessionCacheDomain

  override def http: HttpGet with HttpPut with HttpDelete = httpClient

  // TODO rename cacheId as "cacheName" and "id" as "eori" then store cache name under eori
  def get(cacheId: String, id: String)
         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Map[String, String]]] =
    fetchAndGetEntry[Map[String, String]](defaultSource, cacheId, id)

  def put(cacheId: String, id: String, data: Map[String, String])
         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CacheMap] = cache(defaultSource, cacheId, id, data)

}