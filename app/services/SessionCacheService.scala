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

import com.google.inject.{Singleton, Inject}
import config.AppConfig
import play.api.Logger
import uk.gov.hmrc.http.{HttpDelete, HttpPut, HttpGet, HeaderCarrier}
import uk.gov.hmrc.http.cache.client.HttpCaching
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{Future, ExecutionContext}

@Singleton
class SessionCacheService @Inject()(appConfig: AppConfig, httpClient: HttpClient)extends HttpCaching  {

    override def defaultSource: String = appConfig.keyStoreSource

    override def baseUri: String = appConfig.keyStoreUrl

    override def domain: String = appConfig.sessionCacheDomain

  override def http: HttpGet with HttpPut with HttpDelete = httpClient


  def get(cacheId:String, id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Map[String, Seq[String]]]] = {
    fetchAndGetEntry[Map[String, Seq[String]]](defaultSource, cacheId, id).recover {
      case ex: Throwable => Logger.error(s"cannot fetch  data from Session Cache => " +
        s"cacheId ($cacheId) , id : ${id} ,  \n Exception is ${ex.getMessage}" )
        None
    }
  }

   def put(cacheId:String, id: String, data: Map[String, Seq[String]])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    cache(defaultSource, cacheId, id, data).map { res =>
      true
    }.recover {
      case ex: Throwable => Logger.error(s"cannot save  data to Session Cache => " +
        s"cacheId ($cacheId) , id : ${id} ,  \n Exception is ${ex.getMessage}" )
        throw new RuntimeException(s"Error in caching ${ex.getMessage}")
    }
  }

}