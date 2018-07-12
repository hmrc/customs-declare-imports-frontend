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

import domain.declaration.Declaration
import domain.metadata.MetaData

import scala.xml.Elem

class CustomsDeclarationsClient {

  def produceDeclarationMessage(metaData: MetaData, declaration: Declaration): Elem = <md:MetaData xmlns="urn:wco:datamodel:WCO:DEC-DMS:2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                                                                                   xmlns:clm63055="urn:un:unece:uncefact:codelist:standard:UNECE:AgencyIdentificationCode:D12B"
                                                                                                   xmlns:ds="urn:wco:datamodel:WCO:MetaData_DS-DMS:2" xmlns:md="urn:wco:datamodel:WCO:DocumentMetaData-DMS:2">
    {wcoDataModelVersionCode(metaData)}
  </md:MetaData>

  private def wcoDataModelVersionCode(metaData: MetaData): Elem = metaData.wcoDataModelVersionCode.map { version =>
    <md:WCODataModelVersionCode>
      {version}
    </md:WCODataModelVersionCode>
  }.orNull

}
