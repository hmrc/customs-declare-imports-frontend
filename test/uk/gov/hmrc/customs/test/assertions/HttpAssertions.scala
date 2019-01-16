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

package uk.gov.hmrc.customs.test.assertions

import java.net.URLEncoder

import play.api.http.{HeaderNames, Status}
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

trait HttpAssertions extends CustomsAssertions {

  def wasNotFound(resp: Future[Result]): Unit = status(resp) must be (Status.NOT_FOUND)

  def wasOk(resp: Future[Result]): Unit = status(resp) must be (Status.OK)

  def wasRedirected(toUri: String, resp: Future[Result]): Unit = {
    status(resp) must be(Status.SEE_OTHER)
    header(HeaderNames.LOCATION, resp) must be(Some(toUri))
  }

  def wasRedirected(resp: Future[Result], location: Option[String] = None): Unit = {
    status(resp) must be (Status.SEE_OTHER)
    header(HeaderNames.LOCATION, resp) must be(location)
  }

}
