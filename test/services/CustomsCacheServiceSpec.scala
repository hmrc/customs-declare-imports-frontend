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

import domain.GovernmentAgencyGoodsItem
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.crypto.{ApplicationCrypto, Protected}
import uk.gov.hmrc.customs.test.behaviours.{AuthenticationBehaviours, CustomsSpec}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItemAdditionalDocument, GovernmentAgencyGoodsItemAdditionalDocumentSubmitter}

import scala.concurrent.{ExecutionContext, Future}

class CustomsCacheServiceSpec extends CustomsSpec with AuthenticationBehaviours {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val aKey = randomString(16)
  val aValue = randomString(48)

  class CacheScenario[E](cacheSource: String = appConfig.keyStoreSource, cacheName: String = appConfig.submissionCacheId, eori: String = randomUser.requiredEori, cachedData: Map[String, String] = Map.empty) {

    var putData: Option[Map[String, Map[String, Map[String, Map[String, String]]]]] = None

    val service = new CustomsCacheServiceImpl(new CustomsHttpCaching(appConfig, component[HttpClient]) {
      override def fetchAndGetEntry[T](source: String, cacheId: String, key: String)
        (implicit hc: HeaderCarrier, rds: Reads[T], executionContext: ExecutionContext): Future[Option[T]] = (source, cacheId, key) match {
        case legit if (source == cacheSource && cacheId == cacheName && key == eori) => Future.successful(Some(Protected(cachedData).asInstanceOf[T]))
        case _ => super.fetchAndGetEntry(source, cacheId, key)(hc, rds, executionContext)
      }

      override def cache[A](source: String, cacheId: String, formId: String, body: A)
        (implicit wts: Writes[A], hc: HeaderCarrier, executionContext: ExecutionContext): Future[CacheMap] = (source, cacheId, formId) match {
        case legit if (source == cacheSource && cacheId == cacheName && formId == eori) => {
          putData = Some(Map(source -> Map(cacheId -> Map(formId -> body.asInstanceOf[Protected[Map[String, String]]].decryptedValue))))
          Future.successful(CacheMap(randomString(8), Map.empty))
        }
        case _ => super.cache(source, cacheId, formId, body)(wts, hc, executionContext)
      }
    }, component[ApplicationCrypto])
  }

  "get" should {

    "return existing data from cache" in new CacheScenario[Map[String, String]](cachedData = Map(aKey -> aValue)) {
      service.get(appConfig.submissionCacheId, randomUser.requiredEori).futureValue.get(aKey) must be(aValue)
    }

  }

  "put" should {

    "place given data in named cache for identified user" in new CacheScenario[Map[String, String]]() {
      whenReady(service.put(appConfig.submissionCacheId, randomUser.requiredEori, Map(aKey -> aValue))) { _ =>
        putData.get(appConfig.keyStoreSource)(appConfig.submissionCacheId)(randomUser.requiredEori)(aKey) must be(aValue)
      }
    }

  }

  "test" should {
    "convert GovernmentAgencyGoodsItem to a map of elements" in {
      val item = GovernmentAgencyGoodsItem(additionalDocuments = Seq(
        GovernmentAgencyGoodsItemAdditionalDocument(categoryCode = Some("3"), id = Some("id1"),
          submitter = Some(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(name = Some("submitter name 1"), Some("3)")))
        ),
        GovernmentAgencyGoodsItemAdditionalDocument(categoryCode = Some("4"), id = Some("id2"),
          submitter = Some(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(name = Some("submitter name 2"), Some("4)")))
        )))
      println(getCCParams(item).mkString("\n"))
    }

  }

  def getCCParams(cc: AnyRef) =
    (Map[String, Any]() /: cc.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc))
    }

}
