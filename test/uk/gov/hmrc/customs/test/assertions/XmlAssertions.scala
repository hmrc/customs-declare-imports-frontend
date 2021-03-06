/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.customs.test.assertions

import java.io.StringReader

import javax.xml.XMLConstants
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory}

import scala.xml.SAXException

trait XmlAssertions extends CustomsAssertions {

  val submitDeclarationSchemas = Seq("/DocumentMetaData_2_DMS.xsd", "/WCO_DEC_2_DMS.xsd")

  val cancelDeclarationSchemas = Seq("/CANCEL_METADATA.xsd","/CANCEL.xsd")

  def beValidXml(xml: String, schemas: Seq[String]): Unit = {
     try {
      validateAgainstSchemaResources(xml, schemas)
    } catch {
      case e: SAXException => fail(e.getMessage)
    }
  }

  def isValidXml(xml: String, schemas: Seq[String]): Boolean = {
    try {
      validateAgainstSchemaResources(xml, schemas)
      true
    } catch {
      case e: SAXException => false
    }
  }

  private def validateAgainstSchemaResources(xml: String, schemas: Seq[String]): Unit = {
    val schema: Schema = {
      val sources = schemas.map(res => getClass.getResource(res).toString).map(systemId => new StreamSource(systemId)).toArray[Source]
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(sources)
    }
    val validator = schema.newValidator()
    validator.validate(new StreamSource(new StringReader(xml)))
  }

}
