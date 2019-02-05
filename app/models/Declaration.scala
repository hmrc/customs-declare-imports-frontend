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

import play.api.libs.json.Json

case class DeclarationNotification(functionCode: Int, conversationId: String, dateTimeIssued: Long)

case class DeclarationAction(dateTimeSent: Long, notifications: Seq[DeclarationNotification] = Seq.empty)

case class Declaration(submittedDateTime: Long, localReferenceNumber: Option[String], mrn: Option[String] = None, actions: Seq[DeclarationAction] = Seq.empty)

object Declaration {
  implicit val declarationNotificationFormat = Json.format[DeclarationNotification]
  implicit val declarationActionFormat = Json.format[DeclarationAction]
  implicit val declarationFormat = Json.format[Declaration]
}
