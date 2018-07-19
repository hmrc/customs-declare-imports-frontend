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
import domain.declaration.MetaData
import javax.inject.Singleton
import play.api.http.{ContentTypes, HeaderNames, Status}
import play.api.mvc.Codec
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

@Singleton
class CustomsDeclarationsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) extends SubmitImportDeclarationMessageProducer {

  def submitImportDeclaration(metaData: MetaData, badgeIdentifier: Option[String] = None)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    post(appConfig.submitImportDeclarationUri, produceDeclarationMessage(metaData), badgeIdentifier).map(_.status == Status.ACCEPTED)
  }

  // TODO implement cancel import declaration in CustomsDeclarationClient
//  def cancelImportDeclaration(someType: SomeType, badgeIdentifier: Option[String] = None): Future[Boolean or CustomsDeclarationsResponse] = ???

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

trait SubmitImportDeclarationMessageProducer {

  private[services] def produceDeclarationMessage(metaData: MetaData): Elem = <md:MetaData xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                                                                           xmlns="urn:wco:datamodel:WCO:DEC-DMS:2"
                                                                                           xmlns:md="urn:wco:datamodel:WCO:DocumentMetaData-DMS:2">
    {wcoDataModelVersionCode(metaData)}{wcoTypeName(metaData)}{responsibleCountryCode(metaData)}{responsibleAgencyName(metaData)}{agencyAssignedCustomizationCode(metaData)}{agencyAssignedCustomizationVersionCode(metaData)}{declaration(metaData)}
  </md:MetaData>

  private def wcoDataModelVersionCode(metaData: MetaData): Elem = metaData.wcoDataModelVersionCode.map { version: String =>
    <md:WCODataModelVersionCode>
      {version}
    </md:WCODataModelVersionCode>
  }.orNull

  private def wcoTypeName(metaData: MetaData): Elem = metaData.wcoTypeName.map { name: String =>
    <md:WCOTypeName>
      {name}
    </md:WCOTypeName>
  }.orNull

  private def responsibleCountryCode(metaData: MetaData): Elem = metaData.responsibleCountryCode.map { code: String =>
    <md:ResponsibleCountryCode>
      {code}
    </md:ResponsibleCountryCode>
  }.orNull

  private def responsibleAgencyName(metaData: MetaData): Elem = metaData.responsibleAgencyName.map { name: String =>
    <md:ResponsibleAgencyName>
      {name}
    </md:ResponsibleAgencyName>
  }.orNull

  private def agencyAssignedCustomizationCode(metaData: MetaData): Elem = metaData.agencyAssignedCustomizationCode.map { code: String =>
    <md:AgencyAssignedCustomizationCode>
      {code}
    </md:AgencyAssignedCustomizationCode>
  }.orNull

  private def agencyAssignedCustomizationVersionCode(metaData: MetaData): Elem = metaData.agencyAssignedCustomizationVersionCode.map { code: String =>
    <md:AgencyAssignedCustomizationVersionCode>
      {code}
    </md:AgencyAssignedCustomizationVersionCode>
  }.orNull

  private def declaration(metaData: MetaData): Elem = <Declaration></Declaration>

}