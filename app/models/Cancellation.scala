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

package models

import enumeratum._
import enumeratum.EnumEntry.UpperSnakecase
import play.api.libs.json._

sealed abstract class ChangeReasonCode(val displayName: String) extends EnumEntry with UpperSnakecase

object ChangeReasonCode extends Enum[ChangeReasonCode]{
  val values = findValues

  case object NoLongerRequired extends ChangeReasonCode("Declaration is no longer required")
  case object Duplicate        extends ChangeReasonCode("Duplicate declaration")
  case object Other            extends ChangeReasonCode("Other")

  implicit val formats = Format(reads, writes)

  private def writes = new Writes[ChangeReasonCode] {
    def writes(v: ChangeReasonCode) = JsString(v.entryName)
  }

  private def reads: Reads[ChangeReasonCode] = new Reads[ChangeReasonCode] {
    def reads(json: JsValue): JsResult[ChangeReasonCode] = json match {
      case JsString(s) => ChangeReasonCode.withNameOption(s).fold[JsResult[ChangeReasonCode]](JsError("error.expected.validenumvalue"))(JsSuccess(_))
      case _ => JsError("error.expected.enumstring")
    }
  }
}

case class Cancellation(mrn: String, changeReasonCode: ChangeReasonCode, description: String)

object Cancellation {
  implicit val format = Json.format[Cancellation]
}
