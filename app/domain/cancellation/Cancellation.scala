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

package domain.cancellation

case class TextType(languageID: Option[String])


case class CodeType(listID: Option[String],
                    listAgencyID: Option[String],
                    listAgencyName: Option[String],
                    listName: Option[String],
                    listVersionID: Option[String],
                    name: Option[String],
                    languageID: Option[String],
                    listURI: Option[String],
                    listSchemeURI: Option[String])

case class Submitter(name: Option[String], ID: String)

/*
Submitter name
<xs:restriction base="udt:TextType">
<xs:maxLength value="70"/>
<xs:pattern value=".*[^\s].*"/>
</xs:restriction>
 Submitter ID
<xs:restriction base="udt:IDType">
<xs:maxLength value="17"/>
<xs:pattern value=".*[^\s].*"/>
</xs:restriction>
*/
case class Pointer(documentSectionCode: Option[String])

/*
DocumentSectionCode
  <xs:restriction base="udt:CodeType">
  <xs:maxLength value="3"/>
  <xs:pattern value=".*[^\s].*"/>
  </xs:restriction>
*/


case class AdditionalInformation(statementDescription: String, statementTypeCode: Option[String], pointer: Option[Pointer])

/* StatementDescription
    <xs:restriction base="udt:IDType">
    <xs:maxLength value="17"/>
    <xs:pattern value=".*[^\s].*"/>
      </xs:restriction>
   StatementTypeCode
      <xs:restriction base="udt:CodeType">
        <xs:maxLength value="3"/>
        <xs:pattern value=".*[^\s].*"/>
      </xs:restriction>

*/
case class Amendment(changeReasonCode: String)

/*ChangeReasonCode
       <xs:restriction base="udt:CodeType">
        <xs:maxLength value="3"/>
        <xs:pattern value=".*[^\s].*"/>
      </xs:restriction>
* */

case class Declaration(functionCode: String,
                       functionalReferenceID: Option[String],
                       id: String,
                       typeCode: String = "INV",
                       submitter: Option[Submitter],
                       additionalInformation: AdditionalInformation,
                       amendment: Amendment)


case class MetaData(wCODataModelVersionCode: String,
                    wCOTypeName: String,
                    responsibleCountryCode: String,
                    responsibleAgencyName: String,
                    agencyAssignedCustomizationVersionCode: String,
                    declaration: Declaration)


/*
ID restriction
<xs:simpleContent>
<xs:restriction base="udt:IDType">
<xs:maxLength value="70"/>
<xs:pattern value=".*[^\s].*"/>
</xs:restriction>
</xs:simpleContent>
*/
