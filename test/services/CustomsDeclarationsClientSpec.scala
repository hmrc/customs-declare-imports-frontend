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

import domain.declaration.{Declaration, MetaData}
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.Writes
import play.api.mvc.Codec
import uk.gov.hmrc.customs.test.{CustomsPlaySpec, XmlBehaviours}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.ws._

import scala.concurrent.{ExecutionContext, Future}

class CustomsDeclarationsClientSpec extends CustomsPlaySpec with XmlBehaviours {

  val client = new CustomsDeclarationsClient(appConfig, app.injector.instanceOf[HttpClient])

  "submit import declaration" should {

    "POST metadata to Customs Declarations" in submitDeclarationScenario(MetaData(Declaration())) { resp =>
      resp.futureValue must be(true)
    }

  }

  "produce declaration message" should {

    "include WCODataModelVersionCode" in validDeclarationXmlScenario() {
      val version = "3.6"
      val meta = MetaData(
        randomValidDeclaration,
        wcoDataModelVersionCode = Some(version)
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "WCODataModelVersionCode").text.trim must be(version)
      xml
    }

    "not include WCODataModelVersionCode" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration,
        wcoDataModelVersionCode = None
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "WCODataModelVersionCode").size must be(0)
      xml
    }

    "include WCOTypeName" in validDeclarationXmlScenario() {
      val name = "DEC"
      val meta = MetaData(
        randomValidDeclaration,
        wcoTypeName = Some(name)
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "WCOTypeName").text.trim must be(name)
      xml
    }

    "not include WCOTypeName" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration,
        wcoTypeName = None
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "WCOTypeName").size must be(0)
      xml
    }

    "include ResponsibleCountryCode" in validDeclarationXmlScenario() {
      val code = "GB"
      val meta = MetaData(
        randomValidDeclaration,
        responsibleCountryCode = Some(code)
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "ResponsibleCountryCode").text.trim must be(code)
      xml
    }

    "not include ResponsibleCountryCode" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration,
        responsibleCountryCode = None
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "ResponsibleCountryCode").size must be(0)
      xml
    }

    "include ResponsibleAgencyName" in validDeclarationXmlScenario() {
      val agency = "HMRC"
      val meta = MetaData(
        randomValidDeclaration,
        responsibleAgencyName = Some(agency)
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "ResponsibleAgencyName").text.trim must be(agency)
      xml
    }

    "not include ResponsibleAgencyName" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration,
        responsibleAgencyName = None
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "ResponsibleAgencyName").size must be(0)
      xml
    }

    "include AgencyAssignedCustomizationCode" in validDeclarationXmlScenario() {
      val code = "foo"
      val meta = MetaData(
        randomValidDeclaration,
        agencyAssignedCustomizationCode = Some(code)
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "AgencyAssignedCustomizationCode").text.trim must be(code)
      xml
    }

    "not include AgencyAssignedCustomizationCode" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration,
        agencyAssignedCustomizationCode = None
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "AgencyAssignedCustomizationCode").size must be(0)
      xml
    }

    "include AgencyAssignedCustomizationVersionCode" in validDeclarationXmlScenario() {
      val code = "v2.1"
      val meta = MetaData(
        randomValidDeclaration,
        agencyAssignedCustomizationVersionCode = Some(code)
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "AgencyAssignedCustomizationVersionCode").text.trim must be(code)
      xml
    }

    "not include AgencyAssignedCustomizationVersionCode" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration,
        agencyAssignedCustomizationVersionCode = None
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "AgencyAssignedCustomizationVersionCode").size must be(0)
      xml
    }

    "always include Declaration" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration
      )
      val xml = client.produceDeclarationMessage(meta)
      (xml \ "Declaration").size must be(1)
      xml
    }

  }

  def submitDeclarationScenario(metaData: MetaData,
                                badgeIdentifier: Option[String] = None,
                                forceServerError: Boolean = false,
                                hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(randomString(255)))))
                               (test: Future[Boolean] => Unit): Unit = {
    val messageProducer = new SubmitImportDeclarationMessageProducer {}
    val expectedUrl: String = s"${appConfig.customsDeclarationsEndpoint}${appConfig.submitImportDeclarationUri}"
    val expectedBody: String = messageProducer.produceDeclarationMessage(metaData).mkString
    val expectedHeaders: Map[String, String] = Map(
      "X-Client-ID" -> appConfig.developerHubClientId,
      HeaderNames.ACCEPT -> "application/vnd.hmrc.2.0+xml",
      HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
    ) ++ badgeIdentifier.map(id => "X-Badge-Identifier" -> id)
    val http = new MockHttpClient(expectedUrl, expectedBody, expectedHeaders, forceServerError)
    val client = new CustomsDeclarationsClient(appConfig, http)
    test(client.submitImportDeclaration(metaData, badgeIdentifier)(hc, ec))
  }

  class MockHttpClient(expectedUrl: String, expectedBody: String, expectedHeaders: Map[String, String], forceServerError: Boolean = false) extends HttpClient with WSGet with WSPut with WSPost with WSDelete with WSPatch {
    override val hooks: Seq[HttpHook] = Seq.empty

    override def POSTString[O](url: String,
                            body: String,
                            headers: Seq[(String, String)])
                           (implicit rds: HttpReads[O],
                            hc: HeaderCarrier,
                            ec: ExecutionContext): Future[O] = (url, body, headers) match {
      case _ if !isValidImportDeclarationXml(body.asInstanceOf[String]) => throw new BadRequestException(s"Expected: valid XML: $expectedBody. \nGot: invalid XML: $body")
      case _ if !isAuthenticated(headers.toMap, hc) => throw new UnauthorizedException("Submission declaration request was not authenticated")
      case _ if forceServerError => throw new InternalServerException("Customs Declarations has gone bad.")
      case _ if url == expectedUrl && body == expectedBody && headers.toMap == expectedHeaders => Future.successful(CustomsDeclarationsResponse(202, Some(randomString(16))).asInstanceOf[O])
      case _ => throw new BadRequestException(s"Expected: \nurl = '$expectedUrl', \nbody = '$expectedBody', \nheaders = '$expectedHeaders'.\nGot: \nurl = '$url', \nbody = '$body', \nheaders = '$headers'.")
    }

    private def isAuthenticated(headers: Map[String, String], hc: HeaderCarrier): Boolean = hc.authorization.isDefined

  }

}
