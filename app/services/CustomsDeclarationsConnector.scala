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
import domain.auth.SignedInUser
import javax.inject.Singleton
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import repositories.declaration.{Submission, SubmissionRepository}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.wco.dec.MetaData

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class CustomsDeclarationsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient, declarationRepository: SubmissionRepository) {

  def submitImportDeclaration(metaData: MetaData, badgeIdentifier: Option[String] = None)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext, user: SignedInUser): Future[CustomsDeclarationsResponse] =
    postMetaData(appConfig.submitImportDeclarationUri, metaData, badgeIdentifier, saveSubmission)

  def cancelImportDeclaration(metaData: MetaData, badgeIdentifier: Option[String] = None)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] =
    postMetaData(appConfig.cancelImportDeclarationUri, metaData, badgeIdentifier)

  private def postMetaData(uri: String,
                           metaData: MetaData,
                           badgeIdentifier: Option[String] = None,
                           onSuccess: (MetaData, CustomsDeclarationsResponse) => CustomsDeclarationsResponse = { (metaData, resp) => resp })
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] =
    post(uri, metaData.toXml, badgeIdentifier).map(onSuccess(metaData, _))

  //noinspection ConvertExpressionToSAM
  private implicit val responseReader: HttpReads[CustomsDeclarationsResponse] = new HttpReads[CustomsDeclarationsResponse] {
    override def read(method: String, url: String, response: HttpResponse): CustomsDeclarationsResponse = CustomsDeclarationsResponse(
      response.status,
      response.header("X-Conversation-ID")
    )
  }

  private def saveSubmission(meta: MetaData, resp: CustomsDeclarationsResponse)(implicit ec: ExecutionContext, user: SignedInUser): CustomsDeclarationsResponse =
    Await.result(declarationRepository.insert(Submission(user.eori, resp.conversationId, meta.declaration.functionalReferenceId)).map(_ => resp), 3.seconds)

  private[services] def post(uri: String, body: String, badgeIdentifier: Option[String] = None)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] = {
    val headers: Seq[(String, String)] = Seq(
      "X-Client-ID" -> appConfig.developerHubClientId,
      HeaderNames.ACCEPT -> s"application/vnd.hmrc.${appConfig.customsDeclarationsApiVersion}+xml",
      HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
    ) ++ badgeIdentifier.map(id => "X-Badge-Identifier" -> id)
    httpClient.POSTString[CustomsDeclarationsResponse](s"${appConfig.customsDeclarationsEndpoint}$uri", body, headers)(responseReader, hc, ec)
  }

}

case class CustomsDeclarationsResponse(status: Int, conversationId: Option[String])
