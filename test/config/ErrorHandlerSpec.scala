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

package config

import play.api.http.{HeaderNames, Status}
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.NoActiveSession
import uk.gov.hmrc.customs.test.CustomsPlaySpec

class ErrorHandlerSpec extends CustomsPlaySpec {

  val handler = new ErrorHandler(app.injector.instanceOf[MessagesApi])
  val req = FakeRequest("GET", "/foo")

  "resolve error" should {

    "handle no active session authorisation exception" in {
      val res = handler.resolveError(req, new NoActiveSession("A user is not logged in") {})
      res.header.status must be (Status.SEE_OTHER)
      res.header.headers.get(HeaderNames.LOCATION) must be(Some("/gg/sign-in?continue=%2Ffoo&origin=customs-declare-imports-frontend"))
    }

    "handle insufficient enrolments authorisation exception" in {
      // TODO handle InsufficientEnrolments exception
    }

  }

}
