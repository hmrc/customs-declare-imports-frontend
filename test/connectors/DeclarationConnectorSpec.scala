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

import domain.CustomRequestHeaders
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import uk.gov.hmrc.customs.test.CustomsPlaySpec
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, ExecutionContext}


class DeclarationConnectorSpec extends CustomsPlaySpec {

  private val mockClient = mock[HttpClient]

  val connector = new DeclarationConnector(appConfig,mockClient)
  implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(randomString(255))))
  private val badRequest = new BadRequestException("Bad Request")
  private val internalServerError = new InternalServerException("Internal Server Error")

  "DeclarationsConnector"  should {

    "send a valid declaration request" in {
      mockRequest(Future.successful(HttpResponse(202)))
      val xml = <declaration><consignor><name>my name</name><age>42</age></consignor></declaration>
      val res = Await.result(connector.submitDeclaration(xml,"badge-1"),Duration.Inf)
      res.isRight mustBe true
    }

    "send a valid cancellation request" in {
      mockRequest(Future.successful(HttpResponse(202)))
      val xml = <declaration><consignor><name>my name</name><age>42</age></consignor></declaration>
      val res = Await.result(connector.submitDeclaration(xml,"badge-2",true),Duration.Inf)
      res.isRight mustBe true
    }


    "submit request with a successful response" in {
      mockRequest(Future.successful(HttpResponse(responseStatus=202,responseHeaders =
        Map("X-Conversation-ID"-> Seq("conversationId1")))))
       val res = Await.result(connector.post("customs-declarations/cancel","",CustomRequestHeaders("","")),Duration.Inf)
      res.isRight mustBe true
      res.right.get mustBe "conversationId1"

    }
    "handle badrequest response from api" in {
      mockRequest(Future.failed(badRequest))
      val res = Await.result(connector.post("customs-declarations/cancel","",CustomRequestHeaders("","")),Duration.Inf)
      res.isLeft mustBe true
    }
    "handle other error response from api" in {
      mockRequest(Future.failed(internalServerError))
      val res = Await.result(connector.post("customs-declarations/cancel","",CustomRequestHeaders("","")),Duration.Inf)
      res.isLeft mustBe true
    }

  }

  private def mockRequest(expectedResponse:Future[HttpResponse]) = {
    when(mockClient.POSTString(anyString, anyString, any[Seq[(String,String)]])(
      any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext]))
      .thenReturn(expectedResponse)
  }

}


