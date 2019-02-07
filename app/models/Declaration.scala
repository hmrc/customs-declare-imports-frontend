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

import models.DeclarationActionType.DeclarationActionType
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads}

object DeclarationActionType extends Enumeration {
  type DeclarationActionType = Value

  protected case class Val(displayName: String) extends super.Val
  implicit def valueToDeclarationActionType(v: Value): Val = v.asInstanceOf[Val]

  val SUBMISSION = Val("Submission")
  val CANCELLATIONS = Val("Cancellation")

  implicit val reads = Reads.enumNameReads(DeclarationActionType)
}


case class DeclarationNotification(functionCode: Int, conversationId: String, dateTimeIssued: DateTime)

case class DeclarationAction(dateTimeSent: DateTime, actionType: DeclarationActionType, notifications: Seq[DeclarationNotification] = Seq.empty)

case class Declaration(submittedDateTime: DateTime, localReferenceNumber: Option[String], mrn: Option[String] = None, actions: Seq[DeclarationAction] = Seq.empty)

object Declaration {
  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  implicit val dateTimeReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val declarationNotificationFormat = Json.format[DeclarationNotification]
  implicit val declarationActionFormat = Json.format[DeclarationAction]
  implicit val declarationFormat = Json.format[Declaration]
}
