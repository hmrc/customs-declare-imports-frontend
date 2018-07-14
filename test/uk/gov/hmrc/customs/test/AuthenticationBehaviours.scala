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

package uk.gov.hmrc.customs.test

import java.util.UUID

import domain.auth.SignedInUser
import org.mockito.Mockito.when
import org.mockito.{ArgumentMatcher, ArgumentMatchers}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrievals.{credentials, _}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

trait AuthenticationBehaviours {
  this: CustomsPlaySpec =>

  lazy val signedInUser = userFixture()

  lazy val notLoggedInException = new NoActiveSession("A user is not logged in") {}

  lazy val mockAuthConnector: AuthConnector = mock[AuthConnector]

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[AuthConnector].to(mockAuthConnector))
    .build()

  class UserRequestScenario(method: String = "GET", uri: String = s"/${contextPath}/", headers: Map[String, String] = Map.empty, user: SignedInUser = signedInUser) {
    val req = userRequest(method, uri, user, headers)
  }

  //noinspection ConvertExpressionToSAM
  val noBearerTokenMatcher: ArgumentMatcher[HeaderCarrier] = new ArgumentMatcher[HeaderCarrier] {
    override def matches(hc: HeaderCarrier): Boolean = hc != null && hc.authorization.isEmpty
  }

  //noinspection ConvertExpressionToSAM
  def cdsEnrollmentMatcher(user: SignedInUser): ArgumentMatcher[Predicate] = new ArgumentMatcher[Predicate] {
    override def matches(p: Predicate): Boolean = p == Enrolment("HMRC-CUS-ORG") && user.enrolments.getEnrolment("HMRC-CUS-ORG").isDefined
  }

  def signedInScenario(user: SignedInUser = signedInUser)(test: => Unit): Unit = {
    when(
      mockAuthConnector
        .authorise(
          ArgumentMatchers.argThat(cdsEnrollmentMatcher(user)),
          ArgumentMatchers.eq(credentials and name and email and affinityGroup and internalId and allEnrolments))(ArgumentMatchers.any(), ArgumentMatchers.any()
        )
    ).thenReturn(
      Future.successful(new ~(new ~(new ~(new ~(new ~(user.credentials, user.name), user.email), user.affinityGroup), user.internalId), user.enrolments))
    )
    test
  }

  def notSignedInScenario()(test: => Unit): Unit = {
    when(
      mockAuthConnector
        .authorise(
          ArgumentMatchers.any(),
          ArgumentMatchers.any[Retrieval[_]])(ArgumentMatchers.argThat(noBearerTokenMatcher), ArgumentMatchers.any()
        )
    ).thenReturn(
      Future.failed(notLoggedInException)
    )
    test
  }

  protected def userRequestScenario(method: String = "GET",
                                uri: String = s"/${contextPath}/",
                                user: SignedInUser = signedInUser,
                                headers: Map[String, String] = Map.empty)(test: (Future[Result]) => Unit): Unit = {
    new UserRequestScenario(method, uri, headers, user) {
      test(route(app, req).get)
    }
  }

  protected def userRequest(method: String, uri: String, user: SignedInUser, headers: Map[String, String] = Map.empty): FakeRequest[AnyContentAsEmpty.type] = {
    val session: Map[String, String] = Map(
      SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
      SessionKeys.userId -> user.internalId.getOrElse(randomString(8))
    )
    val cfg = app.injector.instanceOf[CSRFConfigProvider].get
    val token = app.injector.instanceOf[CSRFFilter].tokenProvider.generateToken
    val tags = Map(
      Token.NameRequestTag -> cfg.tokenName,
      Token.RequestTag -> token
    )
    FakeRequest(method, uri).
      withHeaders((Map(cfg.headerName -> token) ++ headers).toSeq: _*).
      withSession(session.toSeq: _*).copyFakeRequest(tags = tags)
  }

}
