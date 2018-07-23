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

import com.google.inject.Inject
import config.AppConfig
import domain.declaration._
import javax.inject.Singleton
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames, Status}
import play.api.mvc.Codec
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

@Singleton
class CustomsDeclarationsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) extends
SubmitImportDeclarationMessageProducer with CustomsDeclarationsCancellationMessageProducer {

  def submitImportDeclaration(metaData: MetaData, badgeIdentifier: Option[String] = None)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    post(appConfig.submitImportDeclarationUri, produceDeclarationMessage(metaData), badgeIdentifier).map(_.status == Status.ACCEPTED)
  }

  def cancelImportDeclaration(metaData: domain.cancellation.MetaData, badgeIdentifier: Option[String] = None)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    post(appConfig.cancelImportDeclarationUri,produceDeclarationCancellationMessage(metaData),badgeIdentifier).map(
      _.status == Status.ACCEPTED).recover{
      case error: Throwable =>
        Logger.error(s"Error in submitting declaratoin cancellation to API  with the error ${error.getMessage}" ); false
    }
  }

  //noinspection ConvertExpressionToSAM
  private implicit val responseReader: HttpReads[CustomsDeclarationsResponse] = new HttpReads[CustomsDeclarationsResponse] {
    override def read(method: String, url: String, response: HttpResponse): CustomsDeclarationsResponse = CustomsDeclarationsResponse(
      response.status,
      response.header("X-Conversation-ID")
    )
  }

  private[services] def post(uri: String, body: Elem, badgeIdentifier: Option[String] = None)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] = {
    val headers: Seq[(String, String)] = Seq(
      "X-Client-ID" -> appConfig.developerHubClientId,
      HeaderNames.ACCEPT -> s"application/vnd.hmrc.${appConfig.customsDeclarationsApiVersion}+xml",
      HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
    ) ++ badgeIdentifier.map(id => "X-Badge-Identifier" -> id)
    httpClient.POSTString[CustomsDeclarationsResponse](s"${appConfig.customsDeclarationsEndpoint}$uri", body.mkString, headers)(responseReader, hc, ec)
  }

}

case class CustomsDeclarationsResponse(status: Int, conversationId: Option[String])


