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
import play.api.{Logger, Application}
import play.api.http.{HeaderNames, Status}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsFormUrlEncoded, AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}
import services.SessionCacheService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrievals.{credentials, _}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

trait AuthenticationBehaviours {
  this: CustomsPlaySpec =>

  lazy val signedInUser: SignedInUser = userFixture()

  lazy val notLoggedInException: NoActiveSession = new NoActiveSession("A user is not logged in") {}

  lazy val mockAuthConnector: AuthConnector = mock[AuthConnector]

  val sessionCacheServiceMock = mock[SessionCacheService]


  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[AuthConnector].to(mockAuthConnector))
    .build()

  class UserRequestScenario(method: String = "GET", uri: String = s"/$contextPath/",
                            user: SignedInUser = signedInUser,
                            headers: Map[String, String] = Map.empty) {
    val req: FakeRequest[AnyContentAsEmpty.type ] = userRequest(method, uri, user, headers)
  }

  class UserRequestSubmitScenario(method: String = "POST", uri: String = s"/$contextPath/",
                            user: SignedInUser = signedInUser,
                            headers: Map[String, String] = Map.empty,
                            payload: Map[String,String]) {

    val req: FakeRequest[AnyContentAsFormUrlEncoded] =
      userRequest(method, uri, user, headers).withFormUrlEncodedBody(payload.toSeq: _*)

  }

  //noinspection ConvertExpressionToSAM
  val noBearerTokenMatcher: ArgumentMatcher[HeaderCarrier] = new ArgumentMatcher[HeaderCarrier] {
    override def matches(hc: HeaderCarrier): Boolean = hc != null && hc.authorization.isEmpty
  }

  //noinspection ConvertExpressionToSAM
  def cdsEnrollmentMatcher(user: SignedInUser): ArgumentMatcher[Predicate] = new ArgumentMatcher[Predicate] {
    override def matches(p: Predicate): Boolean = p == SignedInUser.authorisationPredicate && user.enrolments.getEnrolment(SignedInUser.cdsEnrolmentName).isDefined
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
                                    uri: String = s"/$contextPath/",
                                    user: SignedInUser = signedInUser,
                                    headers: Map[String, String] = Map.empty,
                                    body: Map[String,String]= Map())(test: Future[Result] => Unit): Unit = {
    method match  {
      case "GET" =>
        new UserRequestScenario(method, uri, user,headers) {
          test(route(app, req).get)
        }
      case _ =>
        new UserRequestSubmitScenario(uri= uri, user=user,headers=headers, payload = body) {
          when(sessionCacheServiceMock.put(ArgumentMatchers.any(),ArgumentMatchers.any(),
            ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future.successful(true))
          test(route(app, req).get)
        }
    }
  }

  // can't think of a better way to test this right now
  // the assertion here is fairly meaningless: just says we got the one thrown by notSignedInScenaro(), which is a mock
  // sadly, Play's route() test helper doesn't incorporate the ErrorHandler which would handle this
  // therefore, there is little we can assert on other than "the expected exception was thrown"
  // at least this *will* fail if the auth action is removed
  def accessDeniedRequestScenarioTest(method: String, uri: String): Unit = {
    val ex = intercept[NoActiveSession] {
      requestScenario(method, uri) { resp =>
        status(resp) must be (Status.SEE_OTHER)
        header(HeaderNames.LOCATION, resp) must be(Some(s"/gg/sign-in?continue=$uri&origin=customs-declare-imports-frontend"))
      }
    }
    ex must be theSameInstanceAs notLoggedInException
  }

  protected def userRequest(method: String, uri: String, user: SignedInUser, headers: Map[String, String] = Map.empty):
  FakeRequest[AnyContentAsEmpty.type] = {
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
