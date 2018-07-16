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

package connectors

import javax.inject.Singleton

import com.google.inject.Inject
import domain.CustomRequestHeaders
import play.api.Logger
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

/**
 * Created by raghu on 13/07/18.
 */
@Singleton
class DeclarationConnector @Inject()(override val httpClient: HttpClient) extends Connector

trait Connector {

  def httpClient:HttpClient

  def post(url :String , xml:String, headers:CustomRequestHeaders)(implicit hc: HeaderCarrier) = {

    httpClient.POSTString(url,xml,headers.cdsExtraHeaders).map(res => if(res.status == 202) true else false).recover{
      case badRequest:BadRequestException =>
        Logger.error("Bad Request from API call in connector " + badRequest.getMessage); false
      case th: Throwable => Logger.error("Error in Connector " + th.getMessage); false
    }

  }

}
