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

import domain.cancellation._
import uk.gov.hmrc.customs.test.{XmlBehaviours, CustomsPlaySpec}

import scala.xml.NodeSeq


class CustomsDeclarationsCancellationMessageProducerSpec extends CustomsPlaySpec with XmlBehaviours with CancellationData{

    val service = new CustomsDeclarationsCancellationService()

  "CancellationService" should {
    "include WCODataModelVersionCode" in validCancellationDeclarationXml() {
      val version = "3.6"
      val meta = metadata
      val xml = service.produceDeclarationCancellationMessage(meta)
    // values mustBe  List("listID-Vcode","AgencyId-Vcode","Agencyname-Vcode","listname-raghu","versionID-Vcode","langid-Vcode","schemeuri-Vcode")
      //(xml \ "WCODataModelVersionCode" ).map(_.text)  mustBe Seq("listID-VcodeAgencyId-VcodeAgencyname-Vcodelistname-raghuversionID-Vcodelangid-Vcodeschemeuri-Vcode")
      xml
    }
  }

}

trait CancellationData {
  val wcoDataModelVersionCode = CodeType(Some("listID-Vcode"),Some("AgencyId-Vcode"),Some("Agencyname-Vcode"),
    Some("listname-raghu"),Some("versionID-Vcode"),None,Some("langid-Vcode"),None,Some("schemeuri-Vcode"))
  val responsibleCountryCode = CodeType(Some("listID-CCode"),Some("AgencyId-CCode"),Some("Agencyname-CCode"),
    Some("listname-raghu"),Some("versionID-CCode"),None,Some("langid-CCode"),None,Some("schemeuri-CCode"))
  val agencyAssignedCustomizationVersionCode = CodeType(Some("listID-ACode"),Some("AgencyId-ACode"),Some("Agencyname-ACode"),
    Some("listname-raghu"),Some("versionID-ACode"),None,Some("langid-ACode"),None,Some("schemeuri-ACode"))

  val additionalInfo = AdditionalInformation("statementDesc-1",Some("typecode-1"),Some(Pointer(Some("section_code1234"))))
  val submitter = Some(Submitter(Some("submitter-1"),"1111"))
  val pointer = Pointer(Some("sectionCode-1"))
  val amendment = Amendment("resoncode-1")
  val declaration = Declaration("functionCode-1",Some("refId-1"),"ID-111",submitter = submitter,additionalInformation =
    additionalInfo,amendment = amendment)

  val textType = TextType(Some("languageID-wCOTypeName"))

  val metadata = MetaData(wcoDataModelVersionCode,textType,responsibleCountryCode,textType,
    agencyAssignedCustomizationVersionCode,declaration)

}

