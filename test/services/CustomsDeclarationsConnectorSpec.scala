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

import java.util.UUID

import domain.wco._
import org.scalatest.BeforeAndAfterEach
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import repositories.declaration.SubmissionRepository
import uk.gov.hmrc.customs.test.{CustomsPlaySpec, XmlBehaviours}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.ws._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class CustomsDeclarationsConnectorSpec extends CustomsPlaySpec with XmlBehaviours with BeforeAndAfterEach {

  val eori = Some(randomString(16))
  val lrn = Some(randomString(35))
  val repo = component[SubmissionRepository]
  val conversationId = randomString(80)
  implicit val user = userFixture(eori = eori)

  "CustomsDeclarationsConnector " should {

    "POST metadata to Customs Declarations" in submitDeclarationScenario(MetaData(declaration = Declaration())) { resp =>
      resp.futureValue must be(true)
    }

    "POST declaration cancellation payload successfully" in cancelDeclarationScenario(randomCancelDeclaration) { resp =>
      resp.futureValue must be(true)
    }

    "save declaration on acceptance" in submitDeclarationScenario(metaData = MetaData(declaration = Declaration(
      functionalReferenceId = lrn
    )), conversationId = conversationId) { resp =>
      Await.result(resp, 1.second)
      val found = repo.findByEori(eori).futureValue
      found.length must be(1)
      found(0).eori must be(eori)
      found(0).lrn must be(lrn)
      found(0).conversationId must be(Some(conversationId))
    }

  }

  override protected def afterEach(): Unit = repo.removeAll()

  def submitDeclarationScenario(metaData: MetaData,
                                badgeIdentifier: Option[String] = None,
                                forceServerError: Boolean = false,
                                hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(randomString(255)))),
                                conversationId: String = UUID.randomUUID().toString)
                               (test: Future[Boolean] => Unit): Unit = {
    val expectedUrl: String = s"${appConfig.customsDeclarationsEndpoint}${appConfig.submitImportDeclarationUri}"
    val expectedBody: String = metaData.toXml
    val expectedHeaders: Map[String, String] = Map(
      "X-Client-ID" -> appConfig.developerHubClientId,
      HeaderNames.ACCEPT -> s"application/vnd.hmrc.${appConfig.customsDeclarationsApiVersion}+xml",
      HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
    ) ++ badgeIdentifier.map(id => "X-Badge-Identifier" -> id)
    val http = new MockHttpClient(expectedUrl, expectedBody, expectedHeaders, forceServerError, conversationId)
    val client = new CustomsDeclarationsConnector(appConfig, http, component[SubmissionRepository])
    test(client.submitImportDeclaration(metaData, badgeIdentifier)(hc, ec, user))
  }

  def cancelDeclarationScenario(metaData: MetaData,
                                badgeIdentifier: Option[String] = None,
                                forceServerError: Boolean = false,
                                hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(randomString(255)))))
                               (test: Future[Boolean] => Unit): Unit = {
    val expectedUrl: String = s"${appConfig.customsDeclarationsEndpoint}${appConfig.cancelImportDeclarationUri}"
    val expectedBody: String = metaData.toXml
    val expectedHeaders: Map[String, String] = Map(
      "X-Client-ID" -> appConfig.developerHubClientId,
      HeaderNames.ACCEPT -> s"application/vnd.hmrc.${appConfig.customsDeclarationsApiVersion}+xml",
      HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
    ) ++ badgeIdentifier.map(id => "X-Badge-Identifier" -> id)
    val http = new MockHttpClient(expectedUrl, expectedBody, expectedHeaders, forceServerError)
    val client = new CustomsDeclarationsConnector(appConfig, http, component[SubmissionRepository])
    test(client.cancelImportDeclaration(metaData, badgeIdentifier)(hc, ec))
  }

  class MockHttpClient(expectedUrl: String, expectedBody: String, expectedHeaders: Map[String, String], forceServerError: Boolean = false, conversationId: String = UUID.randomUUID().toString) extends HttpClient with WSGet with WSPut with WSPost with WSDelete with WSPatch {
    override val hooks: Seq[HttpHook] = Seq.empty

    override def POSTString[O](url: String,
                               body: String,
                               headers: Seq[(String, String)])
                              (implicit rds: HttpReads[O],
                               hc: HeaderCarrier,
                               ec: ExecutionContext): Future[O] = (url, body, headers) match {
      case _ if !isValidImportDeclarationXml(body) => throw new BadRequestException(s"Expected: valid XML: $expectedBody. \nGot: invalid XML: $body")
      case _ if !isAuthenticated(headers.toMap, hc) => throw new UnauthorizedException("Submission declaration request was not authenticated")
      case _ if forceServerError => throw new InternalServerException("Customs Declarations has gone bad.")
      case _ if url == expectedUrl && body == expectedBody && headers.toMap == expectedHeaders => Future.successful(CustomsDeclarationsResponse(202, Some(conversationId)).asInstanceOf[O])
      case _ => throw new BadRequestException(s"Expected: \nurl = '$expectedUrl', \nbody = '$expectedBody', \nheaders = '$expectedHeaders'.\nGot: \nurl = '$url', \nbody = '$body', \nheaders = '$headers'.")
    }

    private def isAuthenticated(headers: Map[String, String], hc: HeaderCarrier): Boolean = hc.authorization.isDefined

  }

}
