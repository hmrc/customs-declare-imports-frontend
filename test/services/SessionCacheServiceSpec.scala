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

import play.api.libs.json.{JsObject, JsString, Reads, Writes}
import uk.gov.hmrc.customs.test.{CustomsPlaySpec, XmlBehaviours}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class SessionCacheServiceSpec extends CustomsPlaySpec with XmlBehaviours {

  val expected = Map("DeclarantName" -> "Declarant1", "DeclarantAddressLine" -> "AddressLine1")
  val mockresult = Some(expected)

  val data = CacheMap("id1", Map("form1" -> new JsObject(Map("field1" -> JsString("value1")))))

  "SessionCacheService" should {

    "get data from SessionCache" in cacheFetchScenario("submit-declaration", "submit-declaration") { resp =>
      resp.futureValue.get must be(expected)
    }

    "save data to SessionCache" in cacheDataScenario("submit-declaration", "submit-declaration") { resp =>
      resp.futureValue must be(data)
    }

  }

  val service = new SessionCacheService(appConfig, app.injector.instanceOf[HttpClient]) {
    override def fetchAndGetEntry[T](source: String, cacheId: String, key: String)
                                    (implicit hc: HeaderCarrier, rds: Reads[T], executionContext: ExecutionContext): Future[Option[T]] =
      if (cacheId == "submit-declaration") Future.successful(mockresult.asInstanceOf[Option[T]])
      else Future.successful(None.asInstanceOf[Option[T]])

    override def cache[A](source: String, cacheId: String, formId: String, body: A)
                         (implicit wts: Writes[A], hc: HeaderCarrier, executionContext: ExecutionContext): Future[CacheMap] =
      if (cacheId == "submit-declaration") Future.successful(data)
      else Future.failed(throw new RuntimeException("Error processing"))
  }

  def cacheFetchScenario(cacheId: String, id: String,
                         forceServerError: Boolean = false,
                         hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(randomString(255)))))
                        (test: Future[Option[Map[String, String]]] => Unit): Unit = {
    val expectedCacheId: String = "submit-declaration"

    test(service.get(cacheId, id)(hc, ec))
  }

  def cacheDataScenario(cacheId: String, id: String,
                        forceServerError: Boolean = false,
                        hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(randomString(255)))))
                       (test: Future[CacheMap] => Unit): Unit = {
    val expectedCacheId: String = "submit-declaration"

    val testDataToCache = Map("DeclarantName" -> "Declarant1", "DeclarantAddressLine" -> "AddressLine1")
    test(service.put(cacheId, id, testDataToCache)(hc, ec))
  }


}
