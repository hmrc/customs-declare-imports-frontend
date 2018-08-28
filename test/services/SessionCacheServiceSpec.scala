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

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.customs.test.{AuthenticationBehaviours, CustomsPlaySpec}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class SessionCacheServiceSpec extends CustomsPlaySpec with AuthenticationBehaviours {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val aKey = randomString(16)
  val aValue = randomString(48)

  class CacheScenario[E](cacheSource: String = appConfig.keyStoreSource, cacheName: String = appConfig.submissionCacheId, eori: String = signedInUser.requiredEori, cachedData: Map[String, String] = Map.empty) {

    var putData: Option[Map[String, Map[String, Map[String, Map[String, String]]]]] = None

    val service = new SessionCacheService(appConfig, component[HttpClient]) {

      override def fetchAndGetEntry[T](source: String, cacheId: String, key: String)
                                      (implicit hc: HeaderCarrier, rds: Reads[T], executionContext: ExecutionContext): Future[Option[T]] = (source, cacheId, key) match {
        case legit if (source == cacheSource && cacheId == cacheName && key == eori) => Future.successful(Some(cachedData.asInstanceOf[T]))
        case _ => super.fetchAndGetEntry(source, cacheId, key)(hc, rds, executionContext)
      }

      override def cache[A](source: String, cacheId: String, formId: String, body: A)
                           (implicit wts: Writes[A], hc: HeaderCarrier, executionContext: ExecutionContext): Future[CacheMap] = (source, cacheId, formId) match {
        case legit if (source == cacheSource && cacheId == cacheName && formId == eori) => {
          putData = Some(Map(source -> Map(cacheId -> Map(formId -> body.asInstanceOf[Map[String, String]]))))
          Future.successful(CacheMap(randomString(8), Map.empty))
        }
        case _ => super.cache(source, cacheId, formId, body)(wts, hc, executionContext)
      }
    }
  }

  "get" should {

    "return existing data from cache" in new CacheScenario[Map[String, String]](cachedData = Map(aKey -> aValue)) {
      service.get(appConfig.submissionCacheId, signedInUser.requiredEori).futureValue.get(aKey) must be(aValue)
    }

  }

  "put" should {

    "place given data in named cache for identified user" in new CacheScenario[Map[String, String]]() {
      whenReady(service.put(appConfig.submissionCacheId, signedInUser.requiredEori, Map(aKey -> aValue))) { _ =>
        putData.get(appConfig.keyStoreSource)(appConfig.submissionCacheId)(signedInUser.requiredEori)(aKey) must be(aValue)
      }
    }

  }

}
