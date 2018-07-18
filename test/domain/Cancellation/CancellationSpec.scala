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

package domain.Cancellation

import domain.cancellation.{Pointer, AdditionalInformation, Amendment, CodeType}
import uk.gov.hmrc.customs.test.CustomsPlaySpec

import scala.xml.NodeSeq

/**
 * Created by raghu on 18/07/18.
 */
class CancellationSpec extends CustomsPlaySpec with CancellationData with CancellationXmlElements{

  "CancellationSpec" should {
    "create a CodeType Xml Element" in {

      val element = codeType.toXml()
      element.map(println)
      element mustBe cancellationServiceElem
    }
    "create a AdditionalInformation Xml Element Seq" in {
      val elementSeq = additionalInfo.toXml()
      elementSeq.map(println)
      elementSeq mustBe additionalInformationSeq
    }
  }

}

trait CancellationData {
  val codeType = CodeType(Some("listID-1"),Some("AgencyId-1"),Some("Agencyname-1"),Some("listname-raghu"),
    Some("versionID-1"),None,Some("langid-1"),None,Some("schemeuri-1"))
  val additionalInfo = AdditionalInformation("statementDesc-1",Some("typecode-1"),Some(Pointer(Some("section_code1234"))))
}

trait CancellationXmlElements {
  val cancellationServiceElem:NodeSeq = <md:listID>listID-1</md:listID>
    <md:listAgencyID>AgencyId-1</md:listAgencyID>
    <md:listAgencyName>Agencyname-1</md:listAgencyName>
    <md:listName>listname-raghu</md:listName>
    <md:listVersionID>versionID-1</md:listVersionID>
      <md:name/>
    <md:languageID>langid-1</md:languageID>
      <md:listURI/>
    <md:listSchemeURI>schemeuri-1</md:listSchemeURI>

  val additionalInformationSeq:NodeSeq = <md:StatementDescription>statementDesc-1</md:StatementDescription>
    <md:StatementTypeCode>typecode-1</md:StatementTypeCode><md:Pointer><md:DocumentSectionCode>section_code1234</md:DocumentSectionCode></md:Pointer>
}


