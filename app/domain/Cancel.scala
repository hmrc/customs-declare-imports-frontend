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

package domain

import models.ChangeReasonCode
import play.api.data.format.Formatter
import play.api.data.FormError

case class Cancel(changeReasonCode: ChangeReasonCode, description: String)

object Cancel {

  implicit def changeRequestCodeFormat: Formatter[ChangeReasonCode] = new Formatter[ChangeReasonCode] {
    override def bind(key: String, data: Map[String, String]) =
      data.get(key)
        .flatMap(name => ChangeReasonCode.withNameOption(name))
        .toRight(Seq(FormError(key, "error.required", Nil)))

    override def unbind(key: String, value: ChangeReasonCode) = Map(key -> value.toString)
  }

  def changeReasonCodes: Seq[(String, String)] =
    ChangeReasonCode.values.foldLeft(Map.empty: Map[String, String]) { (m, crc) => m + (crc.entryName -> crc.displayName) }.toSeq
}
