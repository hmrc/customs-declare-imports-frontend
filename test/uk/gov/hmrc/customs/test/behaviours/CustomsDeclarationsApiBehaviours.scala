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

package uk.gov.hmrc.customs.test.behaviours

import java.util.UUID

import config.AppConfig
import domain.auth.SignedInUser
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.{CustomsDeclarationsConnector, CustomsDeclarationsResponse}
import uk.gov.hmrc.customs.test.assertions.XmlAssertions
import uk.gov.hmrc.http.{HeaderCarrier, Upstream4xxResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.wco.dec.MetaData

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait CustomsDeclarationsApiBehaviours extends CustomsSpec with MockitoSugar{

  val mockAppConfig: AppConfig = mock[AppConfig]
  val mockHttpClient: HttpClient = mock[HttpClient]
  private lazy val connector = new MockCustomsDeclarationsConnector(mockAppConfig, mockHttpClient)

  def withCustomsDeclarationsApiSubmission(request: MetaData)
                                          (test: Future[CustomsDeclarationsResponse] => Unit): Unit = {
    test(connector.addExpectedSubmission(request))
  }

  override protected def customise(builder: GuiceApplicationBuilder): GuiceApplicationBuilder =
    super.customise(builder).overrides(bind[CustomsDeclarationsConnector].to(connector))

}



class MockCustomsDeclarationsConnector(mockConfig : AppConfig, mockhttpClient: HttpClient) extends CustomsDeclarationsConnector(appConfig = mockConfig,
                                                                              httpClient = mockhttpClient)
  with XmlAssertions {

  val expectedSubmissions: mutable.Map[MetaData, Future[CustomsDeclarationsResponse]] = mutable.Map.empty

  val expectedCancellations: mutable.Map[MetaData, Future[CustomsDeclarationsResponse]] = mutable.Map.empty

  val submissionSchemas: Seq[String] = Seq("/DocumentMetaData_2_DMS.xsd", "/WCO_DEC_2_DMS.xsd")

  val cancellationSchemas: Seq[String] = Seq("/CANCEL_METADATA.xsd","/CANCEL.xsd")

  override def submitImportDeclaration(metaData: MetaData, localReferenceNumber: String)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext, user: SignedInUser): Future[CustomsDeclarationsResponse] =
    expectedSubmissions.getOrElse(metaData, throw new IllegalArgumentException("Unexpected API submission call"))


  def addExpectedSubmission(meta: MetaData): Future[CustomsDeclarationsResponse] = {
    val resp = toResponse(meta, submissionSchemas)
    expectedSubmissions.put(meta, resp)
    resp
  }

  private def toResponse(meta: MetaData, schemas: Seq[String]): Future[CustomsDeclarationsResponse] =
    if (isValidXml(meta.toXml, schemas)) {
      Future.successful(CustomsDeclarationsResponse(conversationId))
    } else {
      Future.failed(Upstream4xxResponse("You sent bad XML", Status.BAD_REQUEST, Status.INTERNAL_SERVER_ERROR))
    }

  private def conversationId: String = UUID.randomUUID().toString

}
