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

import domain.declaration._
import uk.gov.hmrc.customs.test.{CustomsPlaySpec, XmlBehaviours}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

class SubmitImportDeclarationMessageProducerSpec extends CustomsPlaySpec with XmlBehaviours {

  val connector = new CustomsDeclarationsConnector(appConfig, app.injector.instanceOf[HttpClient])

  "produce declaration message" should {

    "include WCODataModelVersionCode" in validDeclarationXmlScenario() {
      val version = "3.6"
      val meta = MetaData(
        randomValidDeclaration,
        wcoDataModelVersionCode = Some(version)
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "WCODataModelVersionCode").text.trim must be(version)
      xml
    }

    "not include WCODataModelVersionCode" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration,
        wcoDataModelVersionCode = None
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "WCODataModelVersionCode").size must be(0)
      xml
    }

    "include WCOTypeName" in validDeclarationXmlScenario() {
      val name = "DEC"
      val meta = MetaData(
        randomValidDeclaration,
        wcoTypeName = Some(name)
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "WCOTypeName").text.trim must be(name)
      xml
    }

    "not include WCOTypeName" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration,
        wcoTypeName = None
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "WCOTypeName").size must be(0)
      xml
    }

    "include ResponsibleCountryCode" in validDeclarationXmlScenario() {
      val code = "GB"
      val meta = MetaData(
        randomValidDeclaration,
        responsibleCountryCode = Some(code)
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "ResponsibleCountryCode").text.trim must be(code)
      xml
    }

    "not include ResponsibleCountryCode" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration,
        responsibleCountryCode = None
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "ResponsibleCountryCode").size must be(0)
      xml
    }

    "include ResponsibleAgencyName" in validDeclarationXmlScenario() {
      val agency = "HMRC"
      val meta = MetaData(
        randomValidDeclaration,
        responsibleAgencyName = Some(agency)
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "ResponsibleAgencyName").text.trim must be(agency)
      xml
    }

    "not include ResponsibleAgencyName" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration,
        responsibleAgencyName = None
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "ResponsibleAgencyName").size must be(0)
      xml
    }

    "include AgencyAssignedCustomizationCode" in validDeclarationXmlScenario() {
      val code = "foo"
      val meta = MetaData(
        randomValidDeclaration,
        agencyAssignedCustomizationCode = Some(code)
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "AgencyAssignedCustomizationCode").text.trim must be(code)
      xml
    }

    "not include AgencyAssignedCustomizationCode" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration,
        agencyAssignedCustomizationCode = None
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "AgencyAssignedCustomizationCode").size must be(0)
      xml
    }

    "include AgencyAssignedCustomizationVersionCode" in validDeclarationXmlScenario() {
      val code = "v2.1"
      val meta = MetaData(
        randomValidDeclaration,
        agencyAssignedCustomizationVersionCode = Some(code)
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "AgencyAssignedCustomizationVersionCode").text.trim must be(code)
      xml
    }

    "not include AgencyAssignedCustomizationVersionCode" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration,
        agencyAssignedCustomizationVersionCode = None
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "AgencyAssignedCustomizationVersionCode").size must be(0)
      xml
    }

    "always include Declaration" in validDeclarationXmlScenario() {
      val meta = MetaData(
        randomValidDeclaration
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration").size must be(1)
      xml
    }

    "include AcceptanceDateTime" in validDeclarationXmlScenario() {
      val formatCode = randomDateTimeFormatCode
      val dateTime = randomDateTimeString
      val meta = MetaData(
        Declaration(
          acceptanceDateTime = Some(AcceptanceDateTime(DateTimeString(formatCode, dateTime)))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "AcceptanceDateTime" \ "DateTimeString").text.trim must be(dateTime)
      (xml \ "Declaration" \ "AcceptanceDateTime" \ "DateTimeString" \ "@formatCode").text.trim must be(formatCode)
      xml
    }

    "include FunctionCode" in validDeclarationXmlScenario() {
      val code = randomDeclarationFunctionCode
      val meta = MetaData(
        Declaration(
          functionCode = Some(code)
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "FunctionCode").text.trim must be(code)
      xml
    }

    "include FunctionalReferenceID" in validDeclarationXmlScenario() {
      val id = randomString(35)
      val meta = MetaData(
        Declaration(
          functionalReferenceId = Some(id)
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "FunctionalReferenceID").text.trim must be(id)
      xml
    }

    "include ID" in validDeclarationXmlScenario() {
      val id = randomString(70)
      val meta = MetaData(
        Declaration(
          id = Some(id)
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "ID").text.trim must be(id)
      xml
    }

    "include IssueDateTime" in validDeclarationXmlScenario() {
      val formatCode = randomDateTimeFormatCode
      val dateTime = randomDateTimeString
      val meta = MetaData(
        Declaration(
          issueDateTime = Some(IssueDateTime(DateTimeString(formatCode, dateTime)))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "IssueDateTime" \ "DateTimeString").text.trim must be(dateTime)
      (xml \ "Declaration" \ "IssueDateTime" \ "DateTimeString" \ "@formatCode").text.trim must be(formatCode)
      xml
    }

    "include IssueLocationID" in validDeclarationXmlScenario() {
      val id = randomString(5)
      val meta = MetaData(
        Declaration(
          issueLocationId = Some(id)
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "IssueLocationID").text.trim must be(id)
      xml
    }

    "include TypeCode" in validDeclarationXmlScenario() {
      val code = randomString(3)
      val meta = MetaData(
        Declaration(
          typeCode = Some(code)
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "TypeCode").text.trim must be(code)
      xml
    }

    "include GoodsItemQuantity" in validDeclarationXmlScenario() {
      val quantity = randomInt(100000)
      val meta = MetaData(
        Declaration(
          goodsItemQuantity = Some(quantity)
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "GoodsItemQuantity").text.trim.toInt must be(quantity)
      xml
    }

    "include DeclarationOfficeID" in validDeclarationXmlScenario() {
      val id = randomString(17)
      val meta = MetaData(
        Declaration(
          declarationOfficeId = Some(id)
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "DeclarationOfficeID").text.trim must be(id)
      xml
    }

    "include InvoiceAmount without currencyID attribute" in validDeclarationXmlScenario() {
      val amount = randomBigDecimal
      val meta = MetaData(
        Declaration(
          invoiceAmount = Some(InvoiceAmount(amount))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "InvoiceAmount").text.trim must be(amount.toString)
      (xml \ "Declaration" \ "InvoiceAmount" \ "@currencyID").size must be(0)
      xml
    }

    "include InvoiceAmount with currencyID attribute" in validDeclarationXmlScenario() {
      val amount = randomBigDecimal
      val currency = randomISO4217CurrencyCode
      val meta = MetaData(
        Declaration(
          invoiceAmount = Some(InvoiceAmount(amount, Some(currency)))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "InvoiceAmount").text.trim must be(amount.toString)
      (xml \ "Declaration" \ "InvoiceAmount" \ "@currencyID").text.trim must be(currency)
      xml
    }

    "include LoadingListQuantiy" in validDeclarationXmlScenario() {
      val quantity = randomInt(100000)
      val meta = MetaData(
        Declaration(
          loadingListQuantity = Some(quantity)
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "LoadingListQuantity").text.trim.toInt must be(quantity)
      xml
    }

    "include TotalGrossMassMeasure" in validDeclarationXmlScenario() {
      val total = randomBigDecimal
      val meta = MetaData(
        Declaration(
          totalGrossMassMeasure = Some(MassMeasure(total))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "TotalGrossMassMeasure").text.trim must be(total.toString)
      xml
    }

    "include total gross mass measure unit code" in validDeclarationXmlScenario() {
      val total = randomBigDecimal
      val code = randomString(3)
      val meta = MetaData(
        Declaration(
          totalGrossMassMeasure = Some(MassMeasure(total, Some(code)))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "TotalGrossMassMeasure" \ "@unitCode").text.trim must be(code)
      xml
    }

    "include TotalPackageQuantity" in validDeclarationXmlScenario() {
      val total = randomInt(100000000)
      val meta = MetaData(
        Declaration(
          totalPackageQuantity = Some(total)
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "TotalPackageQuantity").text.trim.toInt must be(total)
      xml
    }

    "include SpecificCircumstancesCodeCode" in validDeclarationXmlScenario() {
      val code = randomString(3)
      val meta = MetaData(
        Declaration(
          specificCircumstancesCodeCode = Some(code)
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "SpecificCircumstancesCodeCode").text.trim must be(code)
      xml
    }

    "include Authentication Authentication" in validDeclarationXmlScenario() {
      val auth = randomString(255)
      val meta = MetaData(
        Declaration(
          authentication = Some(Authentication(
            authentication = Some(auth)
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "Authentication" \ "Authentication").text.trim must be(auth)
      xml
    }

    "include Authentication Authenticator Name" in validDeclarationXmlScenario() {
      val auth = randomString(70)
      val meta = MetaData(
        Declaration(
          authentication = Some(Authentication(
            authenticator = Some(Authenticator(
              name = Some(auth)
            ))
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "Authentication" \ "Authenticator" \ "Name").text.trim must be(auth)
      xml
    }

    "include Submitter Name" in validDeclarationXmlScenario() {
      val name = randomString(70)
      val meta = MetaData(
        Declaration(
          submitter = Some(Submitter(
            name = Some(name)
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "Submitter" \ "Name").text.trim must be(name)
      xml
    }

    "include Submitter ID" in validDeclarationXmlScenario() {
      val id = randomString(17)
      val meta = MetaData(
        Declaration(
          submitter = Some(Submitter(
            id = Some(id)
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "Submitter" \ "ID").text.trim must be(id)
      xml
    }

    "include Submitter Address CityName" in validDeclarationXmlScenario() {
      val name = randomString(35)
      val meta = MetaData(
        Declaration(
          submitter = Some(Submitter(
            address = Some(Address(
              cityName = Some(name)
            ))
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "Submitter" \ "Address" \ "CityName").text.trim must be(name)
      xml
    }

    "include Submitter Address CountryCode" in validDeclarationXmlScenario() {
      val code = randomISO3166Alpha2CountryCode
      val meta = MetaData(
        Declaration(
          submitter = Some(Submitter(
            address = Some(Address(
              countryCode = Some(code)
            ))
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "Submitter" \ "Address" \ "CountryCode").text.trim must be(code)
      xml
    }

    "include Submitter Address CountrySubDivisionCode" in validDeclarationXmlScenario() {
      val code = randomString(9)
      val meta = MetaData(
        Declaration(
          submitter = Some(Submitter(
            address = Some(Address(
              countrySubDivisionCode = Some(code)
            ))
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "Submitter" \ "Address" \ "CountrySubDivisionCode").text.trim must be(code)
      xml
    }

    "include Submitter Address CountrySubDivisionName" in validDeclarationXmlScenario() {
      val name = randomString(35)
      val meta = MetaData(
        Declaration(
          submitter = Some(Submitter(
            address = Some(Address(
              countrySubDivisionName = Some(name)
            ))
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "Submitter" \ "Address" \ "CountrySubDivisionName").text.trim must be(name)
      xml
    }

    "include Submitter Address Line" in validDeclarationXmlScenario() {
      val line = randomString(35)
      val meta = MetaData(
        Declaration(
          submitter = Some(Submitter(
            address = Some(Address(
              line = Some(line)
            ))
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "Submitter" \ "Address" \ "Line").text.trim must be(line)
      xml
    }

    "include Submitter Address PostcodeID" in validDeclarationXmlScenario() {
      val id = randomString(9)
      val meta = MetaData(
        Declaration(
          submitter = Some(Submitter(
            address = Some(Address(
              postcodeId = Some(id)
            ))
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "Submitter" \ "Address" \ "PostcodeID").text.trim must be(id)
      xml
    }

    "include Additional Document ID" in validDeclarationXmlScenario() {
      val id = randomString(70)
      val meta = MetaData(
        Declaration(
          additionalDocuments = Seq(AdditionalDocument(
            id = Some(id)
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "AdditionalDocument" \ "ID").text.trim must be(id)
      xml
    }

    "include Additional Document CategoryCode" in validDeclarationXmlScenario() {
      val code = randomString(3)
      val meta = MetaData(
        Declaration(
          additionalDocuments = Seq(AdditionalDocument(
            categoryCode = Some(code)
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "AdditionalDocument" \ "CategoryCode").text.trim must be(code)
      xml
    }

    "include Additional Document TypeCode" in validDeclarationXmlScenario() {
      val code = randomString(3)
      val meta = MetaData(
        Declaration(
          additionalDocuments = Seq(AdditionalDocument(
            typeCode = Some(code)
          ))
        )
      )
      val xml = connector.produceDeclarationMessage(meta)
      (xml \ "Declaration" \ "AdditionalDocument" \ "TypeCode").text.trim must be(code)
      xml
    }

  }

}
