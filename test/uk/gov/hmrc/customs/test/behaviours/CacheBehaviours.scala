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

package uk.gov.hmrc.customs.test.behaviours

import domain.GovernmentAgencyGoodsItem
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, Reads, Writes}
import services.CustomsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait CacheBehaviours extends CustomsSpec with BeforeAndAfterEach {

  private lazy val caching = new MockCustomsCacheService()

  def withCachedData[T](cacheName: String, eori: String, data: Map[String, String])
    (test: Future[CacheMap] => Unit)
    (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Unit =
    test(caching.cache(cacheName, eori, data)(hc, executionContext))

  override protected def customise(builder: GuiceApplicationBuilder): GuiceApplicationBuilder =
    super.customise(builder).overrides(bind[CustomsCacheService].to(caching))

  override protected def afterEach(): Unit = caching.clearCache

}

class MockCustomsCacheService extends CustomsCacheService {

  // source -> cacheId -> formId -> value
  val cache: mutable.Map[String, mutable.Map[String, Map[String, String]]] = mutable.Map.empty

  override def get(cacheName: String, eori: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Map[String, String]]] = Future.successful(
    cache.get(cacheName).flatMap(_.get(eori))
  )

  override def put(cacheName: String, eori: String, data: Map[String, String])
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CacheMap] = {
    cache.
      getOrElseUpdate(cacheName, mutable.Map.empty).
      getOrElseUpdate(eori, data)
    Future.successful(CacheMap(cacheName, Map(eori -> Json.toJson(data))))
  }

  def clearCache: Iterable[mutable.Map[String, Map[String, String]]] = cache.keys.map(k => cache.remove(k).get)

  def cache[T](cacheName: String, eori: String, data: Map[String, String])
    (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[CacheMap] = put(cacheName, eori, data)

  def getGoodsItems(eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[List[GovernmentAgencyGoodsItem]]]
  = Future.successful(Some(List(GovernmentAgencyGoodsItem())))

  def getAGoodsItem(eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[GovernmentAgencyGoodsItem]]
  = Future.successful(Some(GovernmentAgencyGoodsItem()))

  def saveGoodsItem(eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]
  = Future.successful(true)

  def putGoodsItem(eori: String, item: GovernmentAgencyGoodsItem = GovernmentAgencyGoodsItem())(implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[CacheMap] = Future.successful(CacheMap("test", Map(eori -> Json.toJson(item))))

  def getForm[A](eori: String, key: String)(implicit reads: Reads[A], hc: HeaderCarrier,
    ec: ExecutionContext): Future[Option[A]] = Future.successful(None)

  def putForm[A](cacheName: String, eori: String, form: A)(implicit hc: HeaderCarrier, ec: ExecutionContext,
    writes: Writes[A]): Future[CacheMap] = Future.successful(CacheMap("test", Map(eori -> Json.toJson(form))))

}
