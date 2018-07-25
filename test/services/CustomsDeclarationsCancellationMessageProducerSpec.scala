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


class CustomsDeclarationsCancellationMessageProducerSpec extends CustomsPlaySpec with XmlBehaviours with CancellationData{

    val service = new CustomsDeclarationsCancellationMessageProducer {}

  "CancellationService" should {
    "include WCODataModelVersionCode" in validCancellationDeclarationXml() {
      val meta = metadata
      val xml = service.produceDeclarationCancellationMessage(meta)
      (xml \ "WCODataModelVersionCode" ).text  mustBe "versionCode1"
      xml
    }
    "include WCOTypeName" in validCancellationDeclarationXml() {
      val meta = metadata
      val xml = service.produceDeclarationCancellationMessage(meta)
      (xml \ "WCOTypeName" ).text  mustBe "wCOTypeName1"
      xml
    }
    "include AgencyAssignedCustomizationVersionCode" in validCancellationDeclarationXml() {
      val meta = metadata
      val xml = service.produceDeclarationCancellationMessage(meta)
      (xml \ "AgencyAssignedCustomizationVersionCode" ).text  mustBe "agencyAssignedCustomizationVersionCode1"
      xml
    }
    "include Declaration"  in validCancellationDeclarationXml() {
      val meta = metadata
      val xml = service.produceDeclarationCancellationMessage(meta)
      (xml \\ "FunctionCode" ).text  mustBe "13"
      (xml \\ "FunctionalReferenceID" ).text  mustBe "refId-1"
      (xml \\ "ID" ).head.text  mustBe "ID-111"

      xml
    }
    "include Submitter in Declaration"  in validCancellationDeclarationXml() {
      val meta = metadata
      val xml = service.produceDeclarationCancellationMessage(meta)
      println(xml \\"Submitter")
      (xml \\ "Submitter").xml_sameElements(expectedSubmitter) mustBe true

      xml
    }
    "include Amendment in Declaration"  in validCancellationDeclarationXml() {
      val meta = metadata
      val xml = service.produceDeclarationCancellationMessage(meta)
      (xml \\ "Amendment").xml_sameElements(expAmendmentXml) mustBe true
      xml
    }
    "include AdditionalInformation tags in Declaration"  in validCancellationDeclarationXml() {
      val meta = metadata
      val xml = service.produceDeclarationCancellationMessage(meta)
      val declaration = (xml \ "Declaration")
      (declaration \\ "StatementDescription").text mustBe statementDesc
      (declaration \\ "StatementTypeCode").text mustBe statementTypeCode.get
      (declaration \\ "DocumentSectionCode").text mustBe documentSectionCode
      xml
    }
  }

}

trait CancellationData extends CustomsPlaySpec{
//statement desc restriction .*[^\s].*
  val statementDesc = randomString(50)
  val documentSectionCode = randomString(3)
  val changeReasonCode = randomString(3)
  val statementTypeCode = Some(randomString(3))
  val additionalInfo = AdditionalInformation(statementDesc,statementTypeCode,Some(Pointer(Some(documentSectionCode))))
  val submitter = Submitter(Some("submitter-1"),"1111")
  val pointer = Pointer(Some("sectionCode-1"))
  val amendment = Amendment(changeReasonCode)
  //Functioanl code can only be  13, for cancellation
  val declaration = Declaration("13",Some("refId-1"),"ID-111",submitter = submitter,additionalInformation =
    additionalInfo,amendment = amendment)

  val textType = TextType(Some("languageID-wCOTypeName"))

  val expectedSubmitter = <Submitter><Name>submitter-1</Name><ID>1111</ID></Submitter>
  val expAmendmentXml = <Amendment><ChangeReasonCode>{amendment.changeReasonCode}</ChangeReasonCode></Amendment>

  val metadata:MetaData = MetaData("versionCode1","wCOTypeName1","agencyVersionCode1","textType1",
    "agencyAssignedCustomizationVersionCode1",declaration)

}

