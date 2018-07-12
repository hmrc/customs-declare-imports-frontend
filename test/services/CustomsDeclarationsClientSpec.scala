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
      val meta = new MetaData(
        wcoDataModelVersionCode = Some(version)
      )
      val xml = client.produceDeclarationMessage(meta, randomValidDeclaration)
      (xml \ "WCODataModelVersionCode").text.trim must be (version)
    }

    "not include WCODataModelVersionCode" in {
      val meta = new MetaData(
        wcoDataModelVersionCode = None
      )
      val xml = client.produceDeclarationMessage(meta, randomValidDeclaration)
      (xml \ "WCODataModelVersionCode").size must be (0)
    }

  }

}
