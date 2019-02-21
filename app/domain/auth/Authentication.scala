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

package domain.auth

import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, Enrolments}

case class SignedInUser(credentials: Credentials,
                        name: Name,
                        email: Option[String],
                        affinityGroup: Option[AffinityGroup],
                        internalId: Option[String],
                        enrolments: Enrolments) {

  lazy val eori: Option[String] = enrolments.getEnrolment(SignedInUser.cdsEnrolmentName).flatMap(_.getIdentifier(SignedInUser.eoriIdentifierKey)).map(_.value)

  // TODO throw custom exception here and handle in ErrorHandler by redirecting to "enrol" page?
  lazy val requiredEori: String = eori.getOrElse(throw new IllegalStateException("EORI missing"))

}

object SignedInUser {

  val cdsEnrolmentName: String = "HMRC-CUS-ORG"

  val eoriIdentifierKey: String = "EORINumber"

  val authorisationPredicate: Predicate = Enrolment(cdsEnrolmentName)

}

case class AuthenticatedRequest[A](request: Request[A], user: SignedInUser) extends WrappedRequest[A](request)

case class EORI(value: String)

case class EORIRequest[A](request: AuthenticatedRequest[A], eori: EORI) extends WrappedRequest(request) {

  val user: SignedInUser = request.user
}

case class LRNRequest[A](request: EORIRequest[A], lrn: String) extends WrappedRequest(request) {

  val user: SignedInUser = request.user
  val eori: EORI = request.eori
}
