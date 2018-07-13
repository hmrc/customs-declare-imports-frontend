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

import domain.declaration.MetaData
import uk.gov.hmrc.customs.test.{CustomsPlaySpec, XmlBehaviours}

class CustomsDeclarationsClientSpec extends CustomsPlaySpec with XmlBehaviours {

  val client = new CustomsDeclarationsClient

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

}
