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

import com.google.inject.{ImplementedBy, Inject}
import config.AppConfig
import domain.auth.SignedInUser
import javax.inject.Singleton
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames, Status}
import play.api.mvc.Codec
import repositories.declaration.{Submission, SubmissionRepository}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.wco.dec.MetaData

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[BackEndSubmissionConnector])
trait CustomsDeclarationsConnector {

  def submitImportDeclaration(metaData: MetaData, badgeIdentifier: Option[String] = None, token: Option[String] = None)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext, user: SignedInUser): Future[CustomsDeclarationsResponse]

  def cancelImportDeclaration(metaData: MetaData, badgeIdentifier: Option[String] = None)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse]

}


object BackEndHeaderNames {
  val Authorization = "Authorization"
  val XLrnHeaderName = "X-Local-Reference-Number"
  val XClientIdName = "X-Client-ID"
  val XConversationIdName = "X-Conversation-ID"
}


@Singleton
class BackEndSubmissionConnector @Inject()(appConfig: AppConfig,
                                           httpClient: HttpClient) extends CustomsDeclarationsConnector {

  override def submitImportDeclaration(metaData: MetaData, localReferenceNumber: Option[String] = None, token: Option[String])
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext, user: SignedInUser): Future[CustomsDeclarationsResponse] = {
    postMetaData(appConfig.submitImportDeclaration2Uri, metaData, localReferenceNumber.get, token.get, onSuccessfulSubmission)
  }

  override def cancelImportDeclaration(metaData: MetaData, badgeIdentifier: Option[String] = None)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] =
    Future.successful(new CustomsDeclarationsResponse("87687686868"))


  private def onSuccess(meta: MetaData, resp: CustomsDeclarationsResponse): Future[CustomsDeclarationsResponse] = Future.successful(resp)

  private def onSuccessfulSubmission(meta: MetaData, resp: CustomsDeclarationsResponse)
                                    (implicit ec: ExecutionContext, user: SignedInUser): Future[CustomsDeclarationsResponse] =
    Future.successful(new CustomsDeclarationsResponse("96896986"))


  private def postMetaData(uri: String,
                           metaData: MetaData,
                           localReferenceNumber: String,
                           token: String,
                           onSuccess: (MetaData, CustomsDeclarationsResponse) => Future[CustomsDeclarationsResponse] = onSuccess)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] =
    doPost(uri, metaData.toXml, localReferenceNumber, token).flatMap(onSuccess(metaData, _))

  private def doPost(uri: String, body: String, localReferenceNumber: String, token: String)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] = {
    Logger.info(s"calling ${appConfig.customsDeclareImportsEndpoint}$uri")
    httpClient.POSTString[CustomsDeclarationsResponse](
      url = s"${appConfig.customsDeclareImportsEndpoint}$uri",
      body = body,
      headers = headers(localReferenceNumber, token)
    )(responseReader, hc, ec)
  }

  private def headers(localReferenceNumber: String, token: String): Seq[(String, String)] = Seq(
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    BackEndHeaderNames.XLrnHeaderName -> localReferenceNumber,
    BackEndHeaderNames.Authorization -> token
  )

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
}

@Singleton
class CustomsDeclarationsConnectorImpl @Inject()(appConfig: AppConfig,
                                                 httpClient: HttpClient,
                                                 submissionRepository: SubmissionRepository)
  extends CustomsDeclarationsConnector {

  override def submitImportDeclaration(metaData: MetaData, badgeIdentifier: Option[String] = None, token: Option[String])
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext, user: SignedInUser): Future[CustomsDeclarationsResponse] = {
    postMetaData(appConfig.submitImportDeclarationUri, metaData, badgeIdentifier, onSuccessfulSubmission)
  }
  override def cancelImportDeclaration(metaData: MetaData, badgeIdentifier: Option[String] = None)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] =
    postMetaData(appConfig.cancelImportDeclarationUri, metaData, badgeIdentifier)



  private def postMetaData(uri: String,
                           metaData: MetaData,
                           badgeIdentifier: Option[String] = None,
                           onSuccess: (MetaData, CustomsDeclarationsResponse) => Future[CustomsDeclarationsResponse] = onSuccess)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] =
    doPost(uri, metaData.toXml, badgeIdentifier).flatMap(onSuccess(metaData, _))

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

  private def onSuccess(meta: MetaData, resp: CustomsDeclarationsResponse): Future[CustomsDeclarationsResponse] = Future.successful(resp)

  private def onSuccessfulSubmission(meta: MetaData, resp: CustomsDeclarationsResponse)
                                    (implicit ec: ExecutionContext, user: SignedInUser): Future[CustomsDeclarationsResponse] =
    submissionRepository.insert(
      Submission(
        eori = user.requiredEori,
        conversationId = resp.conversationId,
        meta.declaration.flatMap(_.functionalReferenceId)
      )
    ).map(_ => resp)

  private def doPost(uri: String, body: String, badgeIdentifier: Option[String] = None)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] = {

    httpClient.POSTString[CustomsDeclarationsResponse](
      url = s"${appConfig.customsDeclarationsEndpoint}$uri",
      body = body,
      headers = headers(badgeIdentifier)
    )(responseReader, hc, ec)
  }


  private def headers(badgeIdentifier: Option[String]): Seq[(String, String)] = Seq(
    "X-Client-ID" -> appConfig.developerHubClientId,
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.${appConfig.customsDeclarationsApiVersion}+xml",
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
  ) ++ badgeIdentifier.map(id => "X-Badge-Identifier" -> id)

}

case class CustomsDeclarationsResponse(conversationId: String)
