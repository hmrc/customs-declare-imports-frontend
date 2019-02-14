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

import java.net.ConnectException
import java.util.concurrent.TimeoutException

import akka.actor.ActorSystem
import domain.auth.SignedInUser
import models.Cancellation
import org.scalatest.OptionValues
import play.api.Configuration
import play.api.http.{ContentTypes, HeaderNames, HttpVerbs, Status}
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.WSClient
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.{DefaultWriteResult, WriteResult}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.customs.test.behaviours.CustomsSpec
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.http.{DefaultHttpClient, HttpClient}
import uk.gov.hmrc.wco.dec.MetaData

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class CustomsDeclarationsConnectorImplSpec extends CustomsSpec with OptionValues {

  // any required implicits (++ those in CustomsSpec)

  implicit val mc: ReactiveMongoComponent = component[ReactiveMongoComponent]
  implicit val user: SignedInUser = userFixture(eori = Some(randomString(17)))

  // some basic fixtures and helpers

  val submitUrl: String = s"${appConfig.customsDeclareImportsEndpoint}${appConfig.submitImportDeclarationUri}"

  val cancelUrl: String = s"${appConfig.customsDeclareImportsEndpoint}${appConfig.cancelImportDeclarationUri}"

  val aRandomLocalReferenceNumber: String = randomString(8)

  val declarantLocalReferenceNumber = "Bobby3018"

  val declarantAuthToken = "Bearer BXQ3/Treo4kQCZvVcCqKPkumwTEqDrjsQwNFOBQYFr1SDFlJRHEDF8UN2TvetUGpTYtMEOBk+k3c7YJ2IpIWT21luGvdx12z3wxC6lUn7DSKo0T+AzZMSOX4hoshFqLDgvqSofBGsxdpkTp2Sgv4duWjVJmbg8Iprh5Q09LeEKj9KwIkeIPK/mMlBESjue4V"

  val acceptContentType: String = s"application/vnd.hmrc.${appConfig.customsDeclarationsApiVersion}+xml"

  val aRandomSubmitDeclaration: MetaData = randomSubmitDeclaration

  val aRandomCancelDeclaration: Cancellation = randomCancelDeclaration

  val aRandomWcoCancelDeclaration: MetaData = randomWcoCancelDeclaration

  def acceptedResponse(conversationId: String): HttpResponse = HttpResponse(
    responseStatus = Status.ACCEPTED,
    responseHeaders = Map("X-Conversation-ID" -> Seq(conversationId))
  )

  def otherResponse(status: Int): HttpResponse = HttpResponse(responseStatus = status)

  def submitRequest(submission: MetaData, headers: Map[String, String]): HttpRequest = HttpRequest(submitUrl, submission.toXml.mkString, headers)

  def cancelRequest(cancellation: Cancellation, headers: Map[String, String]): HttpRequest = HttpRequest(cancelUrl, Json.toJson(cancellation).toString, headers)

  def expectingAcceptedResponse(request: HttpRequest, headers: Map[String, String]): Either[Exception, HttpExpectation] = Right(HttpExpectation(
    request,
    acceptedResponse(randomConversationId)
  ))

  def expectingOtherResponse(request: HttpRequest, status: Int, headers: Map[String, String]): Either[Exception, HttpExpectation] = Right(HttpExpectation(
    request,
    otherResponse(status)
  ))

  def expectingFailure(ex: Exception): Either[Exception, HttpExpectation] = Left(ex)

  // the actual test scenarios

  "submit import declaration" should {

    "specify Content-Type as XML in request headers" in simpleAcceptedSubmissionScenario(aRandomSubmitDeclaration) { (_, http, _,  _) =>
      http.requests.head.headers(HeaderNames.CONTENT_TYPE) must be(ContentTypes.XML)
    }

    "specify X-Local-Reference-Number in request headers" in simpleAcceptedSubmissionScenario(aRandomSubmitDeclaration, Some(aRandomLocalReferenceNumber)) { (_, http, _,  _) =>
      http.requests.head.headers("X-Local-Reference-Number") must be(declarantLocalReferenceNumber)
    }

    "send metadata as XML in request body" in simpleAcceptedSubmissionScenario(aRandomSubmitDeclaration) { (_, http, _,  _) =>
      http.requests.head.body must be(aRandomSubmitDeclaration.toXml.mkString)
    }

    "throw gateway timeout exception when request times out" in withoutLocalReferenceNumber() { _ =>
      val ex = new TimeoutException("API is not responding")
      withHttpClient(expectingFailure(ex)) { http =>
          withCustomsDeclarationsConnector(http) { connector =>
            connector.
              submitImportDeclaration(aRandomSubmitDeclaration, "").
              failed.futureValue.
              asInstanceOf[GatewayTimeoutException].
              message must be(http.gatewayTimeoutMessage(HttpVerbs.POST, submitUrl, ex))
          }
      }
    }

    "throw bad gateway exception when request cannot connect" in withoutLocalReferenceNumber() { _ =>
      val ex = new ConnectException("API is down")
      withHttpClient(expectingFailure(ex)) { http =>
          withCustomsDeclarationsConnector(http) { connector =>
            connector.
              submitImportDeclaration(aRandomSubmitDeclaration, "").
              failed.futureValue.
              asInstanceOf[BadGatewayException].
              message must be(http.badGatewayMessage(HttpVerbs.POST, submitUrl, ex))
          }
      }
    }

    "throw upstream 5xx exception when API responds with internal server error" in withoutLocalReferenceNumber() { headers =>
      withHttpClient(expectingOtherResponse(submitRequest(aRandomSubmitDeclaration, headers), Status.INTERNAL_SERVER_ERROR, headers)) { http =>
          withCustomsDeclarationsConnector(http) { connector =>
            val ex = connector.submitImportDeclaration(aRandomSubmitDeclaration, "").failed.futureValue.asInstanceOf[Upstream5xxResponse]
            ex.upstreamResponseCode must be(Status.INTERNAL_SERVER_ERROR)
            ex.reportAs must be(Status.INTERNAL_SERVER_ERROR)
          }
      }
    }

    "throw upstream 4xx exception when API responds with bad request" in withoutLocalReferenceNumber() { headers =>
      withHttpClient(expectingOtherResponse(submitRequest(aRandomSubmitDeclaration, headers), Status.BAD_REQUEST, headers)) { http =>
          withCustomsDeclarationsConnector(http) { connector =>
            val ex = connector.submitImportDeclaration(aRandomSubmitDeclaration, "").failed.futureValue.asInstanceOf[Upstream4xxResponse]
            ex.upstreamResponseCode must be(Status.BAD_REQUEST)
            ex.reportAs must be(Status.INTERNAL_SERVER_ERROR)
          }
      }
    }

    "throw upstream 4xx exception when API responds with unauthhorised" in withoutLocalReferenceNumber() { headers =>
      withHttpClient(expectingOtherResponse(submitRequest(aRandomSubmitDeclaration, headers), Status.UNAUTHORIZED, headers)) { http =>
          withCustomsDeclarationsConnector(http) { connector =>
            val ex = connector.submitImportDeclaration(aRandomSubmitDeclaration, "").failed.futureValue.asInstanceOf[Upstream4xxResponse]
            ex.upstreamResponseCode must be(Status.UNAUTHORIZED)
            ex.reportAs must be(Status.INTERNAL_SERVER_ERROR)
          }
      }
    }

  }

  "cancel import declaration" should {

    "send cancellation as JSON in request body" in simpleCancellationScenario(aRandomCancelDeclaration) { (_, http, _,  _) =>
      http.requests.head.body must be(Json.toJson(aRandomCancelDeclaration).toString)
    }

    "throw gateway timeout exception when request times out" in withoutLocalReferenceNumber() { _ =>
      val ex = new TimeoutException("API is not responding")
      withHttpClient(expectingFailure(ex)) { http =>
          withCustomsDeclarationsConnector(http) { connector =>
            connector.
              cancelDeclaration(aRandomCancelDeclaration).
              failed.futureValue.
              asInstanceOf[GatewayTimeoutException].
              message must be(http.gatewayTimeoutMessage(HttpVerbs.POST, cancelUrl, ex))
          }
      }
    }

    "throw bad gateway exception when request cannot connect" in withoutLocalReferenceNumber() { _ =>
      val ex = new ConnectException("API is down")
      withHttpClient(expectingFailure(ex)) { http =>
          withCustomsDeclarationsConnector(http) { connector =>
            connector.
              cancelDeclaration(aRandomCancelDeclaration).
              failed.futureValue.
              asInstanceOf[BadGatewayException].
              message must be(http.badGatewayMessage(HttpVerbs.POST, cancelUrl, ex))
          }
      }
    }

    "throw upstream 5xx exception when API responds with internal server error" in withoutLocalReferenceNumber() { headers =>
      withHttpClient(expectingOtherResponse(cancelRequest(aRandomCancelDeclaration, headers), Status.INTERNAL_SERVER_ERROR, headers)) { http =>
          withCustomsDeclarationsConnector(http) { connector =>
            val ex = connector.cancelDeclaration(aRandomCancelDeclaration).failed.futureValue.asInstanceOf[Upstream5xxResponse]
            ex.upstreamResponseCode must be(Status.INTERNAL_SERVER_ERROR)
            ex.reportAs must be(Status.INTERNAL_SERVER_ERROR)
          }
      }
    }

    "throw upstream 4xx exception when API responds with bad request" in withoutLocalReferenceNumber() { headers =>
      withHttpClient(expectingOtherResponse(cancelRequest(aRandomCancelDeclaration, headers), Status.BAD_REQUEST, headers)) { http =>
          withCustomsDeclarationsConnector(http) { connector =>
            val ex = connector.cancelDeclaration(aRandomCancelDeclaration).failed.futureValue.asInstanceOf[Upstream4xxResponse]
            ex.upstreamResponseCode must be(Status.BAD_REQUEST)
            ex.reportAs must be(Status.INTERNAL_SERVER_ERROR)
          }
      }
    }

    "throw upstream 4xx exception when API responds with unauthhorised" in withoutLocalReferenceNumber() { headers =>
      withHttpClient(expectingOtherResponse(cancelRequest(aRandomCancelDeclaration, headers), Status.UNAUTHORIZED, headers)) { http =>
          withCustomsDeclarationsConnector(http) { connector =>
            val ex = connector.cancelDeclaration(aRandomCancelDeclaration).failed.futureValue.asInstanceOf[Upstream4xxResponse]
            ex.upstreamResponseCode must be(Status.UNAUTHORIZED)
            ex.reportAs must be(Status.INTERNAL_SERVER_ERROR)
          }
      }
    }

  }

  // the test scenario builders

  def simpleAcceptedSubmissionScenario(submission: MetaData, maybeLocalReferenceNumber: Option[String] = None)
                                      (test: (Map[String, String], MockHttpClient, HttpExpectation,  CustomsDeclarationsConnector) => Unit): Unit = maybeLocalReferenceNumber match {
    case Some(localReferenceNumber) => withLocalReferenceNumber(localReferenceNumber) { headers =>
      val expectation = expectingAcceptedResponse(submitRequest(submission, headers), headers)
      withHttpClient(expectation) { http =>
          withCustomsDeclarationsConnector(http) { connector =>
            whenReady(connector.submitImportDeclaration(submission, declarantLocalReferenceNumber)) { _ =>
              test(headers, http, expectation.right.get, connector)
            }
          }
      }
    }

    case None => withoutLocalReferenceNumber() { headers =>
      val expectation = expectingAcceptedResponse(submitRequest(submission, headers), headers)
      withHttpClient(expectation) { http =>
          withCustomsDeclarationsConnector(http) { connector =>
            whenReady(connector.submitImportDeclaration(submission, "")) { _ =>
              test(headers, http, expectation.right.get, connector)
            }
          }
      }
    }
  }

  def withoutLocalReferenceNumber()(test: Map[String, String] => Unit): Unit = withLocalReferenceNumber(None)(test)

  def withLocalReferenceNumber(localReferenceNumber: String)(test: Map[String, String] => Unit): Unit = withLocalReferenceNumber(Some(localReferenceNumber))(test)

  private def withLocalReferenceNumber(localReferenceNumber: Option[String] = None)(test: Map[String, String] => Unit): Unit = test(
    Map(
      "X-Client-ID" -> appConfig.developerHubClientId,
      HeaderNames.ACCEPT -> s"application/vnd.hmrc.${appConfig.customsDeclarationsApiVersion}+xml",
      HeaderNames.CONTENT_TYPE -> ContentTypes.XML,
      HeaderNames.AUTHORIZATION -> declarantAuthToken
    ) ++ localReferenceNumber.map(id => "X-Local-Reference-Number" -> id)
  )

  private def withJsonHeaders(test: Map[String, String] => Unit): Unit = test(Map(
    "X-Client-ID" -> appConfig.developerHubClientId,
    HeaderNames.ACCEPT -> ContentTypes.JSON,
    HeaderNames.CONTENT_TYPE -> ContentTypes.JSON,
    HeaderNames.AUTHORIZATION -> declarantAuthToken
  ))

  def withHttpClient(throwOrRespond: Either[Exception, HttpExpectation])
                    (test: MockHttpClient => Unit): Unit = {
    test(new MockHttpClient(throwOrRespond, component[Configuration], component[AuditConnector], component[WSClient]))
  }


  def withCustomsDeclarationsConnector(httpClient: HttpClient)
                                      (test: CustomsDeclarationsConnector => Unit): Unit = {
    test(new CustomsDeclarationsConnector(appConfig, httpClient))
  }

  def simpleCancellationScenario(cancellation: Cancellation)
                                (test: (Map[String, String], MockHttpClient, HttpExpectation,  CustomsDeclarationsConnector) => Unit): Unit = withJsonHeaders { headers =>
    val expectation = expectingAcceptedResponse(cancelRequest(cancellation, headers), headers)
    withHttpClient(expectation) { http =>
      withCustomsDeclarationsConnector(http) { connector =>
        whenReady(connector.cancelDeclaration(cancellation)) { _ =>
          test(headers, http, expectation.right.get, connector)
        }
      }
    }
  }

}

case class HttpExpectation(req: HttpRequest, resp: HttpResponse)

case class HttpRequest(url: String, body: String, headers: Map[String, String])

class MockHttpClient(throwOrRespond: Either[Exception, HttpExpectation], config: Configuration, auditConnector: AuditConnector, wsClient: WSClient)
  extends DefaultHttpClient(config, auditConnector, wsClient, ActorSystem("testActorSystem")) {

  val requests: mutable.Buffer[services.HttpRequest] = mutable.Buffer.empty

  override def doPostString(url: String, body: String, headers: Seq[(String, String)])
                           (implicit hc: HeaderCarrier): Future[HttpResponse] = {
    requests += services.HttpRequest(url, body, headers.toMap)
    throwOrRespond.fold(
      ex => Future.failed(ex),
      respond => {
        val validateUrl = (url == respond.req.url)
        val validateBody = (body == respond.req.body)
        if ( validateUrl && validateBody) {
          Future.successful(respond.resp)
        } else {
         Future.failed(new RuntimeException("Unable to match mock parameters"))
        }
      }
    )
  }

  override def doPost[A](url: String, body: A, headers: Seq[(String, String)])(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = {
    val jsonBody = Json.toJson(body).toString
    requests += services.HttpRequest(url, jsonBody, headers.toMap)
    throwOrRespond.fold(
      ex => Future.failed(ex),
      respond => {
        val validateUrl = url == respond.req.url
        val validateBody = jsonBody == respond.req.body
        if ( validateUrl && validateBody) {
          Future.successful(respond.resp)
        } else {
          Future.failed(new RuntimeException("Unable to match mock parameters"))
        }
      }
    )
  }

}
