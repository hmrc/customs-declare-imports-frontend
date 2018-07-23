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

import scala.xml.{NodeSeq, Elem}


class CustomsDeclarationsCancellationService extends CustomsDeclarationsCancellationMessageProducer

trait CustomsDeclarationsCancellationMessageProducer {
  private[services] def produceDeclarationCancellationMessage(metaData: MetaData): Elem = <md:MetaData xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                                                                           xmlns="urn:wco:datamodel:WCO:DEC-DMS:2"
                                                                                           xmlns:md="urn:wco:datamodel:WCO:DocumentMetaData-DMS:2">
    {wcoDataModelVersionCode(metaData)}
    {wCOTypeName(metaData)}
    {responsibleCountryCode(metaData)}
    {responsibleAgencyName(metaData)}
    {agencyAssignedCustomizationVersionCode(metaData)}
    <Declaration>
      {declaration(metaData.declaration)}
    </Declaration>
  </md:MetaData>

  private def wcoDataModelVersionCode(metaData: MetaData):NodeSeq =
    <md:WCODataModelVersionCode>{metaData.wCODataModelVersionCode}</md:WCODataModelVersionCode>

  private def wCOTypeName(metaData: MetaData): Elem =
    <md:WCOTypeName>{metaData.wCOTypeName}</md:WCOTypeName>


  private def responsibleCountryCode(metaData: MetaData) :Elem =
    <md:ResponsibleCountryCode>{metaData.responsibleCountryCode}</md:ResponsibleCountryCode>

  private def responsibleAgencyName(metaData: MetaData) :Elem =
    <md:ResponsibleAgencyName>{metaData.responsibleAgencyName}</md:ResponsibleAgencyName>

  private def agencyAssignedCustomizationVersionCode(metaData: MetaData) :Elem =
    <md:AgencyAssignedCustomizationVersionCode>{ metaData.agencyAssignedCustomizationVersionCode}</md:AgencyAssignedCustomizationVersionCode>


  private def additionalInformation(aditionalInfo:AdditionalInformation):NodeSeq = {
    <AdditionalInformation>
      <StatementDescription>{aditionalInfo.statementDescription}</StatementDescription>
      {aditionalInfo.statementTypeCode.map{code => <StatementTypeCode>{code}</StatementTypeCode>}.getOrElse("")}
      {aditionalInfo.pointer.map { pointer =>
      <Pointer>
        {pointer.documentSectionCode.map { code => <DocumentSectionCode>{code}</DocumentSectionCode>
      }.getOrElse("")}
      </Pointer>
    }.getOrElse("")}
    </AdditionalInformation>
  }

  private def amendment(amendment: Amendment):Elem =
    <Amendment><ChangeReasonCode>{amendment.changeReasonCode}</ChangeReasonCode></Amendment>


  private def declaration(declaration: Declaration) = {
      <FunctionCode>{declaration.functionCode}</FunctionCode> ++
     declaration.functionalReferenceID.map(refId => <FunctionalReferenceID>{refId}</FunctionalReferenceID>) ++
      <ID>{declaration.id}</ID> ++
      <TypeCode>{declaration.typeCode}</TypeCode> ++
      declaration.submitter.map (
      res => <Submitter>{res.name.map(name => <Name>{name}</Name>).getOrElse("")}<ID>{res.ID}</ID></Submitter>) ++
      {additionalInformation(declaration.additionalInformation)} ++ {amendment(declaration.amendment)}
  }

}


