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
import config.AppConfig
import domain.CustomRequestHeaders
import play.api.Logger
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.xml.Elem

/**
 * Created by raghu on 13/07/18.
 */
@Singleton
class DeclarationConnector @Inject()(appConfig: AppConfig, val httpClient: HttpClient)
{ val submitUrl: String = appConfig.submitImportDeclarationEndpoint
   val cancelUrl: String = appConfig.cancelDeclarationEndpointURl
   val clientId:String = appConfig.devHubClientId

  def submitDeclaration (xml:Elem, badgeIdentifier:String, isCancel:Boolean = false)(implicit hc: HeaderCarrier) = {
    val url :String = if(isCancel) submitUrl else cancelUrl
    post(url, xml.mkString,CustomRequestHeaders(clientId, badgeIdentifier))
  }

  def post(url:String, body:String, headers:CustomRequestHeaders)(implicit hc: HeaderCarrier) = {

    httpClient.POSTString(url,body,headers.cdsExtraHeaders).map(res => if(res.status == 202)
      Right(res.header(headers.xConversationIdName).getOrElse(Left(false))) else Left(false)).recover{
      case badRequest:BadRequestException =>
        Logger.error("Bad Request from API call in connector " + badRequest.getMessage); Left(false)
      case th: Throwable => Logger.error("Error in Connector " + th.getMessage); Left(false)
    }

  }

}
