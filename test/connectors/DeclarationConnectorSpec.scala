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

  val connector = new DeclarationConnector(mockClient)
  implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(randomString(255))))
  private val badrequest = new BadRequestException("Bad Request")
  private val internalServerError = new InternalServerException("Internal Server Error")

  "DeclarationsConnector"  should {

    "submit request with a successful response" in {
      mockRequest(Future.successful(HttpResponse(202)))
       val res = Await.result(connector.post("customs-declarations/cancel","",CustomRequestHeaders("","")),Duration.Inf)
      res mustBe true

    }
    "handle badrequest response from api" in {
      mockRequest(Future.failed(badrequest))
      val res = Await.result(connector.post("customs-declarations/cancel","",CustomRequestHeaders("","")),Duration.Inf)
      res mustBe false
    }
    "handle other error response from api" in {
      mockRequest(Future.failed(internalServerError))
      val res = Await.result(connector.post("customs-declarations/cancel","",CustomRequestHeaders("","")),Duration.Inf)
      res mustBe false
    }

  }

  private def mockRequest(expectedResponse:Future[HttpResponse]) = {
    when(mockClient.POSTString(anyString, anyString, any[Seq[(String,String)]])(
      any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext]))
      .thenReturn(expectedResponse)
  }

}


