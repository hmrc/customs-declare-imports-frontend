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

import domain.metadata.MetaData
import uk.gov.hmrc.customs.test.CustomsPlaySpec

class CustomsDeclarationsClientSpec extends CustomsPlaySpec {

  val client = new CustomsDeclarationsClient

  "produce declaration message" should {

    "include WCODataModelVersionCode" in {
      val version = "3.6"
      val meta = MetaData(
        wcoDataModelVersionCode = Some(version)
      )
      val xml = client.produceDeclarationMessage(meta, randomValidDeclaration)
      (xml \ "WCODataModelVersionCode").text.trim must be (version)
    }

    "not include WCODataModelVersionCode" in {
      val meta = MetaData(
        wcoDataModelVersionCode = None
      )
      val xml = client.produceDeclarationMessage(meta, randomValidDeclaration)
      (xml \ "WCODataModelVersionCode").size must be (0)
    }

    "include WCOTypeName" in {
      val name = "DEC"
      val meta = MetaData(
        wcoTypeName = Some(name)
      )
      val xml = client.produceDeclarationMessage(meta, randomValidDeclaration)
      (xml \ "WCOTypeName").text.trim must be (name)
    }

    "not include WCOTypeName" in {
      val meta = MetaData(
        wcoTypeName = None
      )
      val xml = client.produceDeclarationMessage(meta, randomValidDeclaration)
      (xml \ "WCOTypeName").size must be (0)
    }

    "include ResponsibleCountryCode" in {
      val code = "GB"
      val meta = MetaData(
        responsibleCountryCode = Some(code)
      )
      val xml = client.produceDeclarationMessage(meta, randomValidDeclaration)
      (xml \ "ResponsibleCountryCode").text.trim must be (code)
    }

    "not include ResponsibleCountryCode" in {
      val meta = MetaData(
        responsibleCountryCode = None
      )
      val xml = client.produceDeclarationMessage(meta, randomValidDeclaration)
      (xml \ "ResponsibleCountryCode").size must be (0)
    }

    "include ResponsibleAgencyName" in {
      val agency = "HMRC"
      val meta = MetaData(
        responsibleAgencyName = Some(agency)
      )
      val xml = client.produceDeclarationMessage(meta, randomValidDeclaration)
      (xml \ "ResponsibleAgencyName").text.trim must be (agency)
    }

    "not include ResponsibleAgencyName" in {
      val meta = MetaData(
        responsibleAgencyName = None
      )
      val xml = client.produceDeclarationMessage(meta, randomValidDeclaration)
      (xml \ "ResponsibleAgencyName").size must be (0)
    }

    "include AgencyAssignedCustomizationCode" in {
      val code = "v2.1"
      val meta = MetaData(
        agencyAssignedCustomizationCode = Some(code)
      )
      val xml = client.produceDeclarationMessage(meta, randomValidDeclaration)
      (xml \ "AgencyAssignedCustomizationCode").text.trim must be (code)
    }

    "not include AgencyAssignedCustomizationCode" in {
      val meta = MetaData(
        agencyAssignedCustomizationCode = None
      )
      val xml = client.produceDeclarationMessage(meta, randomValidDeclaration)
      (xml \ "AgencyAssignedCustomizationCode").size must be (0)
    }

  }

}
