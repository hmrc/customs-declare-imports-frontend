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

package uk.gov.hmrc.customs.test.behaviours

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

import domain.auth.SignedInUser
import org.mockito.Mockito.when
import org.mockito.{ArgumentMatcher, ArgumentMatchers}
import org.scalatest.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfig, CSRFConfigProvider, CSRFFilter}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AuthConnector, NoActiveSession}
import uk.gov.hmrc.customs.test.CustomsFixtures
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

trait AuthenticationBehaviours extends CustomsSpec with CustomsFixtures with MockitoSugar {

  lazy val randomUser: SignedInUser = userFixture()

  lazy val notLoggedInException: NoActiveSession = new NoActiveSession("A user is not logged in") {}

  lazy val authConnector: AuthConnector = mock[AuthConnector]

  private lazy val cfg: CSRFConfig = component[CSRFConfigProvider].get

  private lazy val token: String = component[CSRFFilter].tokenProvider.generateToken

  def ggLoginRedirectUri(fromUri: String): String = s"/gg/sign-in?continue=${URLEncoder.encode(fromUri, StandardCharsets.UTF_8.displayName())}&origin=${appConfig.appName}"

  lazy val authenticationTags: Map[String, String] = Map(
    Token.NameRequestTag -> cfg.tokenName,
    Token.RequestTag -> token
  )

  //noinspection ConvertExpressionToSAM
  private val noBearerTokenMatcher: ArgumentMatcher[HeaderCarrier] = new ArgumentMatcher[HeaderCarrier] {
    override def matches(hc: HeaderCarrier): Boolean = hc != null && hc.authorization.isEmpty
  }

  private implicit class HelperOps[A](a: A) {

    def ~[B](b: B) = new ~(a, b)
  }

  def withSignedInUser(user: SignedInUser = randomUser)(test: (Map[String, String], Map[String, String], Map[String, String]) => Unit): Unit = {
    when(
      authConnector
        .authorise(
          ArgumentMatchers.argThat(cdsEnrollmentMatcher(user)),
          ArgumentMatchers.eq(credentials and name and email and affinityGroup and internalId and allEnrolments))(ArgumentMatchers.any(), ArgumentMatchers.any()
        )
    ).thenReturn(
      Future.successful(Some(user.credentials) ~ Some(user.name) ~ user.email ~ user.affinityGroup ~ user.internalId ~ user.enrolments)
    )
    test(Map(cfg.headerName -> token), userSession(user), authenticationTags)
  }

  def withoutSignedInUser()(test: (Map[String, String], Map[String, String]) => Unit): Unit = {
    when(
      authConnector
        .authorise(
          ArgumentMatchers.any(),
          ArgumentMatchers.any[Retrieval[_]])(ArgumentMatchers.argThat(noBearerTokenMatcher), ArgumentMatchers.any()
        )
    ).thenReturn(
      Future.failed(notLoggedInException)
    )
    test(Map(cfg.headerName -> token), authenticationTags)
  }

  def userSession(user: SignedInUser): Map[String, String] = Map(
    SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
    SessionKeys.userId -> user.internalId.getOrElse(randomString(8))
  )

  override protected def customise(builder: GuiceApplicationBuilder): GuiceApplicationBuilder =
    super.customise(builder).overrides(bind[AuthConnector].to(authConnector))

  //noinspection ConvertExpressionToSAM
  private def cdsEnrollmentMatcher(user: SignedInUser): ArgumentMatcher[Predicate] = new ArgumentMatcher[Predicate] {
    override def matches(p: Predicate): Boolean = p == SignedInUser.authorisationPredicate && user.enrolments.getEnrolment(SignedInUser.cdsEnrolmentName).isDefined
  }

}
