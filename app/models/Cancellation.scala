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

import models.ChangeReasonCode.ChangeReasonCode
import play.api.libs.json.{Format, Json, Reads, Writes}

object ChangeReasonCode extends Enumeration(1) {
  type ChangeReasonCode = Value

  protected case class Val(displayName: String) extends super.Val
  implicit def valueToChangeReasonCode(v: Value): Val = v.asInstanceOf[Val]

  val NO_LONGER_REQUIRED = Val("Declaration is no longer required")
  val DUPLICATE = Val("Duplicate declaration")
  val OTHER = Val("Other")

  implicit val format = Format(Reads.enumNameReads(ChangeReasonCode), Writes.enumNameWrites)
}

case class Cancellation(mrn: String, changeReasonCode: ChangeReasonCode, description: String)

object Cancellation {
  implicit val format = Json.format[Cancellation]
}
