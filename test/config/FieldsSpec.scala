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

package config

import java.util.ResourceBundle

import org.scalatest.{MustMatchers, WordSpec}
import play.api.i18n.Messages
import play.api.i18n.Messages.UrlMessageSource
import uk.gov.hmrc.customs.test.RandomFixtures
import uk.gov.hmrc.wco.dec.{JacksonMapper, MetaData}

class FieldsSpec extends WordSpec with MustMatchers with RandomFixtures with JacksonMapper {

  private val knownBad: Set[String] = Set(
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[0].additionalProcedure",
    "declaration.typeCode.additional"
  )

  "definitions" should {

    "include declarant name field" in {
      Fields.definitions("declaration.declarant.name") must be(Fields.declarantName)
    }

  }

  Fields.definitions.filterNot(d => knownBad.contains(d._2.name)).foreach { entry =>

    s"field '${entry._2.name}'" should {

      "be serializable and deserializable" in {
        val v = randomInt(99).toString
        val props = Map(entry._2.name -> v)
        val meta = MetaData.fromProperties(props)
        val p = meta.toProperties
        withClue(entry._1) {
          p(entry._1) must be(v)
        }
      }

      s"contains message for key '${entry._2.labelMessageKey}'" in {
        Messages.parse(UrlMessageSource(getClass.getResource("/messages")), "messages").fold(
          ex => throw ex,
          messages => withClue(s"message 'entry._2.labelMessageKey' is blank") {
            messages(entry._2.labelMessageKey).trim.isEmpty must be(false)
          }
        )

      }

    }

  }

}
