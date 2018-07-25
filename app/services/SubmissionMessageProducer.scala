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

import scala.xml.Elem

trait SubmissionMessageProducer {

  private[services] def produceDeclarationMessage(metaData: MetaData): Elem = <md:MetaData xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                                                                           xmlns="urn:wco:datamodel:WCO:DEC-DMS:2"
                                                                                           xmlns:md="urn:wco:datamodel:WCO:DocumentMetaData-DMS:2"
                                                                                           xmlns:udt="urn:wco:datamodel:WCO:Declaration_DS:DMS:2">
    {wcoDataModelVersionCode(metaData)}
    {wcoTypeName(metaData)}
    {responsibleCountryCode(metaData)}
    {responsibleAgencyName(metaData)}
    {agencyAssignedCustomizationCode(metaData)}
    {agencyAssignedCustomizationVersionCode(metaData)}
    {declaration(metaData)}
  </md:MetaData>

  private def wcoDataModelVersionCode(metaData: MetaData): Elem = metaData.wcoDataModelVersionCode.map { version: String =>
    <md:WCODataModelVersionCode>{version}</md:WCODataModelVersionCode>
  }.orNull

  private def wcoTypeName(metaData: MetaData): Elem = metaData.wcoTypeName.map { name: String =>
    <md:WCOTypeName>{name}</md:WCOTypeName>
  }.orNull

  private def responsibleCountryCode(metaData: MetaData): Elem = metaData.responsibleCountryCode.map { code: String =>
    <md:ResponsibleCountryCode>{code}</md:ResponsibleCountryCode>
  }.orNull

  private def responsibleAgencyName(metaData: MetaData): Elem = metaData.responsibleAgencyName.map { name: String =>
    <md:ResponsibleAgencyName>{name}</md:ResponsibleAgencyName>
  }.orNull

  private def agencyAssignedCustomizationCode(metaData: MetaData): Elem = metaData.agencyAssignedCustomizationCode.map { code: String =>
    <md:AgencyAssignedCustomizationCode>{code}</md:AgencyAssignedCustomizationCode>
  }.orNull

  private def agencyAssignedCustomizationVersionCode(metaData: MetaData): Elem = metaData.agencyAssignedCustomizationVersionCode.map { code: String =>
    <md:AgencyAssignedCustomizationVersionCode>{code}</md:AgencyAssignedCustomizationVersionCode>
  }.orNull

  private def declaration(metaData: MetaData): Elem = <Declaration>
    {acceptanceDateTime(metaData)}
    {functionCode(metaData)}
    {functionalReferenceId(metaData)}
    {declarationId(metaData)}
    {issueDateTime(metaData)}
    {issueLocationId(metaData)}
    {typeCode(metaData)}
    {goodsItemQuantity(metaData)}
    {declarationOfficeId(metaData)}
    {invoiceAmount(metaData)}
    {loadingListQuantity(metaData)}
    {totalGrossMassMeasure(metaData)}
    {totalPackageQuantity(metaData)}
    {specificCircustancesCodeCode(metaData)}
    {authentication(metaData)}
    {submitter(metaData)}
    {metaData.declaration.additionalDocuments.map(additionalDocument)}
    {metaData.declaration.additionalInformations.map(additionalInformation)}
    {agent(metaData)}
    {metaData.declaration.authorisationHolders.map(authorisationHolder)}
    {borderTransportMeans(metaData)}
    {metaData.declaration.currencyExchanges.map(currencyExchange)}
  </Declaration>

  private def acceptanceDateTime(metaData: MetaData): Elem = metaData.declaration.acceptanceDateTime.map { dateTime =>
    <AcceptanceDateTime>
      <udt:DateTimeString formatCode={dateTime.dateTimeString.formatCode}>{dateTime.dateTimeString.value}</udt:DateTimeString>
    </AcceptanceDateTime>
  }.orNull

  private def functionCode(metaData: MetaData): Elem = metaData.declaration.functionCode.map { code =>
    <FunctionCode>{code}</FunctionCode>
  }.orNull

  private def functionalReferenceId(metaData: MetaData): Elem = metaData.declaration.functionalReferenceId.map { id =>
    <FunctionalReferenceID>{id}</FunctionalReferenceID>
  }.orNull

  private def declarationId(metaData: MetaData): Elem = metaData.declaration.id.map { id =>
    <ID>{id}</ID>
  }.orNull

  private def issueDateTime(metaData: MetaData): Elem = metaData.declaration.issueDateTime.map { dateTime =>
    <IssueDateTime>
      <udt:DateTimeString formatCode={dateTime.dateTimeString.formatCode}>{dateTime.dateTimeString.value}</udt:DateTimeString>
    </IssueDateTime>
  }.orNull

  private def issueLocationId(metaData: MetaData): Elem = metaData.declaration.issueLocationId.map { id =>
    <IssueLocationID>{id}</IssueLocationID>
  }.orNull

  private def typeCode(metaData: MetaData): Elem = metaData.declaration.typeCode.map { code =>
    <TypeCode>{code}</TypeCode>
  }.orNull

  private def goodsItemQuantity(metaData: MetaData): Elem = metaData.declaration.goodsItemQuantity.map { quantity =>
    <GoodsItemQuantity>{quantity}</GoodsItemQuantity>
  }.orNull

  private def declarationOfficeId(metaData: MetaData): Elem = metaData.declaration.declarationOfficeId.map { id =>
    <DeclarationOfficeID>{id}</DeclarationOfficeID>
  }.orNull

  private def invoiceAmount(metaData: MetaData): Elem = metaData.declaration.invoiceAmount.map { amount =>
    if (amount.currencyId.isDefined) {
      <InvoiceAmount currencyID={amount.currencyId.get}>{amount.value}</InvoiceAmount>
    } else {
      <InvoiceAmount>{amount.value}</InvoiceAmount>
    }
  }.orNull

  private def loadingListQuantity(metaData: MetaData): Elem = metaData.declaration.loadingListQuantity.map { quantity =>
    <LoadingListQuantity>{quantity}</LoadingListQuantity>
  }.orNull

  private def totalGrossMassMeasure(metaData: MetaData): Elem = metaData.declaration.totalGrossMassMeasure.map { total =>
    if (total.unitCode.isDefined) {
      <TotalGrossMassMeasure unitCode={total.unitCode.get}>{total.value}</TotalGrossMassMeasure>
    } else {
      <TotalGrossMassMeasure>{total.value}</TotalGrossMassMeasure>
    }
  }.orNull

  private def totalPackageQuantity(metaData: MetaData): Elem = metaData.declaration.totalPackageQuantity.map { quantity =>
    <TotalPackageQuantity>{quantity}</TotalPackageQuantity>
  }.orNull

  private def specificCircustancesCodeCode(metaData: MetaData): Elem = metaData.declaration.specificCircumstancesCodeCode.map { code =>
    <SpecificCircumstancesCodeCode>{code}</SpecificCircumstancesCodeCode>
  }.orNull

  private def authentication(metaData: MetaData): Elem = metaData.declaration.authentication.map { auth =>
    <Authentication>
      {auth.authentication.map(auth => <Authentication>{auth}</Authentication>).orNull}
      {auth.authenticator.map(auth => auth.name.map(name => <Authenticator><Name>{name}</Name></Authenticator>).orNull).orNull}
    </Authentication>
  }.orNull

  private def submitter(metaData: MetaData): Elem = metaData.declaration.submitter.map { submitter =>
    <Submitter>
      {submitter.name.map(name => <Name>{name}</Name>).orNull}
      {submitter.id.map(id => <ID>{id}</ID>).orNull}{address(submitter.address)}
    </Submitter>
  }.orNull

  private def address(address: Option[Address]): Elem = address.map { addr =>
    <Address>
      {addr.cityName.map(name => <CityName>{name}</CityName>).orNull}
      {addr.countryCode.map(code => <CountryCode>{code}</CountryCode>).orNull}
      {addr.countrySubDivisionCode.map(code => <CountrySubDivisionCode>{code}</CountrySubDivisionCode>).orNull}
      {addr.countrySubDivisionName.map(name => <CountrySubDivisionName>{name}</CountrySubDivisionName>).orNull}
      {addr.line.map(line => <Line>{line}</Line>).orNull}
      {addr.postcodeId.map(id => <PostcodeID>{id}</PostcodeID>).orNull}
    </Address>
  }.orNull

  private def additionalDocument(additionalDocument: AdditionalDocument): Elem = <AdditionalDocument>
    {additionalDocument.id.map(id => <ID>{id}</ID>).orNull}
    {additionalDocument.categoryCode.map(code => <CategoryCode>{code}</CategoryCode>).orNull}
    {additionalDocument.typeCode.map(code => <TypeCode>{code}</TypeCode>).orNull}
  </AdditionalDocument>

  private def additionalInformation(additionalInformation: AdditionalInformation): Elem = <AdditionalInformation>
    {additionalInformation.statementCode.map(code => <StatementCode>{code}</StatementCode>).orNull}
    {additionalInformation.statementDescription.map(desc => <StatementDescription>{desc}</StatementDescription>).orNull}
    {additionalInformation.statementTypeCode.map(code => <StatementTypeCode>{code}</StatementTypeCode>).orNull}
    {additionalInformation.pointers.map(pointer)}
  </AdditionalInformation>

  private def pointer(pointer: Pointer): Elem = <Pointer>
    {pointer.sequenceNumeric.map(num => <SequenceNumeric>{num}</SequenceNumeric>).orNull}
    {pointer.documentSectionCode.map(code => <DocumentSectionCode>{code}</DocumentSectionCode>).orNull}
    {pointer.tagId.map(id => <TagID>{id}</TagID>).orNull}
  </Pointer>

  private def agent(metaData: MetaData): Elem = metaData.declaration.agent.map { agent =>
    <Agent>
      {agent.name.map(name => <Name>{name}</Name>).orNull}
      {agent.id.map(id => <ID>{id}</ID>).orNull}
      {agent.functionCode.map(code => <FunctionCode>{code}</FunctionCode>).orNull}
      {address(agent.address)}
    </Agent>
  }.orNull

  private def authorisationHolder(authorisationHolder: AuthorisationHolder): Elem = <AuthorisationHolder>
    {authorisationHolder.id.map(id => <ID>{id}</ID>).orNull}
    {authorisationHolder.categoryCode.map(code => <CategoryCode>{code}</CategoryCode>).orNull}
  </AuthorisationHolder>

  private def borderTransportMeans(metaData: MetaData): Elem = metaData.declaration.borderTransportMeans.map { means =>
    <BorderTransportMeans>
      {means.name.map(name => <Name>{name}</Name>).orNull}
      {means.id.map(id => <ID>{id}</ID>).orNull}
      {means.typeCode.map(code => <TypeCode>{code}</TypeCode>).orNull}
      {means.identificationTypeCode.map(code => <IdentificationTypeCode>{code}</IdentificationTypeCode>).orNull}
      {means.registrationNationalityCode.map(code => <RegistrationNationalityCode>{code}</RegistrationNationalityCode>).orNull}
      {means.modeCode.map(code => <ModeCode>{code}</ModeCode>).orNull}
    </BorderTransportMeans>
  }.orNull

  private def currencyExchange(currencyExchange: CurrencyExchange): Elem = <CurrencyExchange>
    {currencyExchange.currencyTypeCode.map(code => <CurrencyTypeCode>{code}</CurrencyTypeCode>).orNull}
    {currencyExchange.rateNumeric.map(rate => <RateNumeric>{rate}</RateNumeric>).orNull}
  </CurrencyExchange>

}
