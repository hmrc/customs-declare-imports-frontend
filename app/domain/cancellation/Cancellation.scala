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

import java.lang.reflect.Field

import play.api.Logger

import scala.xml.{NodeSeq, XML}


sealed trait XmlTransform {
  def toXml(isCapitalise: Boolean = true): NodeSeq = {
    getClass.getDeclaredFields.map(field => {
      field setAccessible true
      val fieldName = if (isCapitalise) field.getName.capitalize else field.getName
      if(fieldName.endsWith("outer"))
      Logger.debug("fieldName -> " + fieldName)
      val xmltag = s"<md:${fieldName}>${getValue(field: Field)}</md:${fieldName}>"
      Logger.debug("xml tag String " + xmltag)
      val xmlElem = XML.loadString(xmltag)
      Logger.debug("xml Element " + xmlElem)
      xmlElem
    }).toSeq

  }

  private def getValue(field: Field) =
    if (field.get(this).isInstanceOf[Option[Any]]) {
      field.get(this).asInstanceOf[Option[Any]].getOrElse("")
    } else field.get(this)

}

case class TextType(languageID: Option[String]) extends XmlTransform


case class CodeType(listID: Option[String],
                    listAgencyID: Option[String],
                    listAgencyName: Option[String],
                    listName: Option[String],
                    listVersionID: Option[String],
                    name: Option[String],
                    languageID: Option[String],
                    listURI: Option[String],
                    listSchemeURI: Option[String]) extends XmlTransform {
  override def toXml(isCapitalise: Boolean = false): NodeSeq = super.toXml(false)
}

case class Submitter(name: Option[String], ID: String) extends XmlTransform

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
case class Pointer(documentSectionCode: Option[String]) extends XmlTransform

/*
DocumentSectionCode
  <xs:restriction base="udt:CodeType">
  <xs:maxLength value="3"/>
  <xs:pattern value=".*[^\s].*"/>
  </xs:restriction>
*/

case class XmlAdditionalInformation(statementDescription: String, statementTypeCode: Option[String]) extends XmlTransform


case class AdditionalInformation(statementDescription: String, statementTypeCode: Option[String], pointer: Option[Pointer]) {

  def toXml(): NodeSeq = XmlAdditionalInformation(statementDescription, statementTypeCode).toXml() ++ <md:Pointer>{
                                                      pointer.get.toXml()}</md:Pointer>
}

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
case class Amendment(changeReasonCode: String) extends XmlTransform

/*ChangeReasonCode
       <xs:restriction base="udt:CodeType">
        <xs:maxLength value="3"/>
        <xs:pattern value=".*[^\s].*"/>
      </xs:restriction>
* */
case class XmlDeclaration(functionCode: String,
                                   functionalReferenceID: Option[String],
                                   id: String,
                                   typeCode: String = "INV",
                                   submitter: Submitter)

case class Declaration(functionCode: String,
                       functionalReferenceID: Option[String],
                       id: String,
                       typeCode: String = "INV",
                       submitter: Submitter,
                       additionalInformation: AdditionalInformation,
                       amendment: Amendment) extends XmlTransform
{
  def toXml() = {

  }
}

case class MetaData(wCODataModelVersionCode: Option[CodeType],
                    wCOTypeName: Option[CodeType],
                    responsibleCountryCode: Option[CodeType],
                    responsibleAgencyName: Option[CodeType],
                    agencyAssignedCustomizationVersionCode: Option[CodeType],
                    declaration: Declaration) extends XmlTransform


/*
ID restriction
<xs:simpleContent>
<xs:restriction base="udt:IDType">
<xs:maxLength value="70"/>
<xs:pattern value=".*[^\s].*"/>
</xs:restriction>
</xs:simpleContent>
*/
