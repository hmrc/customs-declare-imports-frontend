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

package uk.gov.hmrc.customs.test.behaviours

import config.ErrorHandler
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

trait RequestHandlerBehaviours extends CustomsSpec {

  val errorHandler: ErrorHandler = component[ErrorHandler]

  val contextPath: String = "/customs-declare-imports"

  def uriWithContextPath(path: String): String = s"$contextPath$path"

  def withRequest(method: String, uri: String,
                  headers: Map[String, String] = Map.empty,
                  session: Map[String, String] = Map.empty,
                  tags: Map[String, String] = Map.empty)
                 (test: Future[Result] => Unit): Unit = {
    val r = FakeRequest(method, uri).
      withHeaders(headers.toSeq: _*).
      withSession(session.toSeq: _*).
      copyFakeRequest(tags = tags)
    val res: Future[Result] = route(app, r).get.recover {
      case e: Exception => errorHandler.resolveError(r, e)
    }
    test(res)
  }

}
