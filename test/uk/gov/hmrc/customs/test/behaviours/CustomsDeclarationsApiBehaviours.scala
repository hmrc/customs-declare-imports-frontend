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

package uk.gov.hmrc.customs.test.behaviours

import java.io.StringReader
import java.util.UUID

import domain.auth.SignedInUser
import javax.xml.XMLConstants
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory}
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.{CustomsDeclarationsConnector, CustomsDeclarationsResponse}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.wco.dec.MetaData

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.SAXException

trait CustomsDeclarationsApiBehaviours extends CustomsSpec {

  private lazy val connector = new MockCustomsDeclarationsConnector()

  def withCustomsDeclarationsApiSubmission(request: MetaData)
                                          (test: Future[CustomsDeclarationsResponse] => Unit): Unit = {
    test(connector.addExpectedSubmission(request))
  }

  def withCustomsDeclarationsApiCancellation(request: MetaData)(test: Future[CustomsDeclarationsResponse] => Unit): Unit = {
    test(connector.addExpectedCancellation(request))
  }

  override protected def customise(builder: GuiceApplicationBuilder): GuiceApplicationBuilder =
    super.customise(builder).overrides(bind[CustomsDeclarationsConnector].to(connector))

}

class MockCustomsDeclarationsConnector extends CustomsDeclarationsConnector {

  val expectedSubmissions: mutable.Map[MetaData, Future[CustomsDeclarationsResponse]] = mutable.Map.empty

  val expectedCancellations: mutable.Map[MetaData, Future[CustomsDeclarationsResponse]] = mutable.Map.empty

  val submissionSchemas = Seq("/DocumentMetaData_2_DMS.xsd", "/WCO_DEC_2_DMS.xsd")

  val cancellationSchemas = Seq("/CANCEL_METADATA.xsd","/CANCEL.xsd")

  override def submitImportDeclaration(metaData: MetaData, badgeIdentifier: Option[String])
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext, user: SignedInUser): Future[CustomsDeclarationsResponse] =
    expectedSubmissions.getOrElse(metaData, throw new IllegalArgumentException("Unexpected API submission call"))

  override def cancelImportDeclaration(metaData: MetaData, badgeIdentifier: Option[String])
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] =
    expectedCancellations.getOrElse(metaData, throw new IllegalArgumentException("Unexpected API cancellation call"))

  def addExpectedSubmission(meta: MetaData): Future[CustomsDeclarationsResponse] = {
    val resp = toResponse(meta, submissionSchemas)
    expectedSubmissions.put(meta, resp)
    resp
  }

  def addExpectedCancellation(meta: MetaData): Future[CustomsDeclarationsResponse] = {
    val resp = toResponse(meta, cancellationSchemas)
    expectedCancellations.put(meta, resp)
    resp
  }

  private def toResponse(meta: MetaData, schemas: Seq[String]): Future[CustomsDeclarationsResponse] = Future.successful(
    if (isValidImportDeclarationXml(meta.toXml, schemas)) CustomsDeclarationsResponse(Status.ACCEPTED, conversationId)
    else CustomsDeclarationsResponse(Status.BAD_REQUEST, None)
  )


  private def conversationId: Option[String] = Some(UUID.randomUUID().toString)

  protected def isValidImportDeclarationXml(xml: String, schemas: Seq[String]): Boolean = {
    try {
      validateAgainstSchemaResources(xml, schemas)
      true
    } catch {
      case _: SAXException => false
    }
  }

  private def validateAgainstSchemaResources(xml: String, schemas: Seq[String]): Unit = {
    val schema: Schema = {
      val sources = schemas.map(res => getClass.getResource(res).toString).map(systemId => new StreamSource(systemId)).toArray[Source]
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(sources)
    }
    val validator = schema.newValidator()
    validator.validate(new StreamSource(new StringReader(xml)))
  }

}
