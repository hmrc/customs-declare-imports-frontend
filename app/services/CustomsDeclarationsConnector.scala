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

import com.google.inject.Inject
import config.AppConfig
import domain.auth.SignedInUser
import javax.inject.Singleton
import models.{Cancellation, Declaration}
import play.api.http.{ContentTypes, HeaderNames, Status}
import play.api.mvc.Codec
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.wco.dec.MetaData

import scala.concurrent.{ExecutionContext, Future}



object BackEndHeaderNames {
  val Authorization = "Authorization"
  val XLrnHeaderName = "X-Local-Reference-Number"
  val XClientIdName = "X-Client-ID"
  val XConversationIdName = "X-Conversation-ID"
}

@Singleton
class CustomsDeclarationsConnector @Inject()(appConfig: AppConfig,
                                                 httpClient: HttpClient) {

  def submitImportDeclaration(metaData: MetaData, localReferenceNumber: String)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext, user: SignedInUser): Future[CustomsDeclarationsResponse] = {
    postMetaData(appConfig.submitImportDeclarationUri, metaData, localReferenceNumber)
  }

  def getDeclarations(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Declaration]] = {
    httpClient.GET[Seq[Declaration]](s"${appConfig.customsDeclareImportsEndpoint}/declarations")
  }

  def cancelDeclaration(cancellation: Cancellation)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] = {
    httpClient.POST[Cancellation, CustomsDeclarationsResponse](s"${appConfig.customsDeclareImportsEndpoint}/cancel", cancellation)
  }

  private def postMetaData(uri: String,
                           metaData: MetaData,
                           localReferenceNumber: String)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] =
    doPost(uri, metaData.toXml, localReferenceNumber)

  //noinspection ConvertExpressionToSAM
  private implicit val responseReader: HttpReads[CustomsDeclarationsResponse] = new HttpReads[CustomsDeclarationsResponse] {
    override def read(method: String, url: String, response: HttpResponse): CustomsDeclarationsResponse = response.status / 100 match {
      case 4 => throw new Upstream4xxResponse(
        message = "Invalid request made to Customs Declarations API",
        upstreamResponseCode = response.status,
        reportAs = Status.INTERNAL_SERVER_ERROR,
        headers = response.allHeaders
      )
      case 5 => throw new Upstream5xxResponse(
        message = "Customs Declarations API unable to service request",
        upstreamResponseCode = response.status,
        reportAs = Status.INTERNAL_SERVER_ERROR
      )
      case _ => CustomsDeclarationsResponse(
        response.header("X-Conversation-ID").getOrElse(
          throw new Upstream5xxResponse(
            message = "Conversation ID missing from Customs Declaration API response",
            upstreamResponseCode = response.status,
            reportAs = Status.INTERNAL_SERVER_ERROR
          )
        )
      )
    }
  }

  private def doPost(uri: String, body: String, localReferenceNumber: String )
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] = {

    httpClient.POSTString[CustomsDeclarationsResponse](
      url = s"${appConfig.customsDeclareImportsEndpoint}$uri",
      body = body,
      headers = headers(localReferenceNumber)
    )(responseReader, hc, ec)
  }


  private def headers(localReferenceNumber: String): Seq[(String, String)] = Seq(
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    BackEndHeaderNames.XLrnHeaderName -> localReferenceNumber
  )

}

case class CustomsDeclarationsResponse(conversationId: String)
