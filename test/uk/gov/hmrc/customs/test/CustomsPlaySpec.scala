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

import com.gu.scalatest.JsoupShouldMatchers
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

trait CustomsPlaySpec extends PlaySpec with OneAppPerSuite with JsoupShouldMatchers {

  implicit val mat = app.materializer

  protected val contextPath: String = "/customs-declare-imports"

  class RequestScenario(method: String = "GET", uri: String = s"/${contextPath}/", headers: Map[String, String] = Map.empty) {
    val req = FakeRequest(method, uri).withHeaders(headers.toSeq:_*)
  }

  protected def requestScenario(method: String = "GET", uri: String = s"/${contextPath}/", headers: Map[String, String] = Map.empty)(test: (Future[Result]) => Unit): Unit = {
    new RequestScenario(method, uri, headers) {
      test(route(app, req).get)
    }
  }

  protected def uriWithContextPath(path: String): String = s"${contextPath}${path}"

}
