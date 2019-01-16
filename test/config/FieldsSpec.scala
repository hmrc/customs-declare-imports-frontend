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

package config

import org.scalatest.{MustMatchers, WordSpec}
import play.api.i18n.Messages
import play.api.i18n.Messages.UrlMessageSource
import uk.gov.hmrc.customs.test.CustomsFixtures
import uk.gov.hmrc.wco.dec.{JacksonMapper, MetaData}

import scala.util.matching.Regex

class FieldsSpec extends WordSpec with MustMatchers with CustomsFixtures with JacksonMapper {

  // FIXME resolve issues around known bad mappings
  private val knownBad: Set[String] = Set(
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[0].additionalProcedure",
    "declaration.typeCode.additional"
  )

  private val acceptableId: Regex = "^[a-zA-Z]+[a-zA-Z0-9_-]*$".r

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

      // HTML5 allows practically anything as an ID attribute value. However, for the sake of sanity (giving due
      // consideration to things like CSS and JavaScript which might want to reference them), we should be a little
      // more strict. The HTML4 defintion which stipulates that ID tokens must begin with a letter ([A-Za-z])
      // and may be followed by any number of letters, digits ([0-9]), hyphens ("-"), underscores ("_"), colons (":"),
      // and periods ("."). For our purposes, we should go also exclude periods and colons.
      "have a sane ID attribute value" in {
        withClue(s"ID attribute value '${entry._2.id()}' matches required regex") {
          acceptableId.pattern.matcher(entry._2.id()).matches() must be(true)
        }
      }

    }

  }

}
