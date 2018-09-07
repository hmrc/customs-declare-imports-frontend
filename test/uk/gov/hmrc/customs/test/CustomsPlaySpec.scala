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

import akka.stream.Materializer
import akka.util.Timeout
import com.gu.scalatest.JsoupShouldMatchers
import config.AppConfig
import org.jsoup.nodes.Element
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.http.Status
import play.api.libs.concurrent.Execution.Implicits
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

// TODO override configured ports from application.conf with random available ports
trait CustomsPlaySpec extends PlaySpec with OneAppPerSuite with JsoupShouldMatchers with MockitoSugar with ScalaFutures with CustomsFixtures {

  implicit val mat: Materializer = app.materializer
  implicit val ec: ExecutionContext = Implicits.defaultContext
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val patience: PatienceConfig = PatienceConfig(timeout = 5.seconds, interval = 50.milliseconds) // be more patient than the default

  protected val contextPath: String = "/customs-declare-imports"

  class RequestScenario(method: String = "GET", uri: String = s"/$contextPath/", headers: Map[String, String] = Map.empty) {
    val req: FakeRequest[AnyContentAsEmpty.type] = basicRequest(method, uri, headers)
  }

  protected def component[T: ClassTag]: T = app.injector.instanceOf[T]

  protected def basicRequest(method: String = "GET", uri: String = "/", headers: Map[String, String] = Map.empty): FakeRequest[AnyContentAsEmpty.type] = FakeRequest(method, uri).withHeaders(headers.toSeq: _*)

  protected def contentAsHtml(of: Future[Result])(implicit timeout: Timeout): Element = contentAsString(of)(timeout, mat).asBodyFragment

  protected def includesHtmlInput(in: Future[Result], `type`: String, name: String)(implicit timeout: Timeout): Unit = contentAsHtml(in)(timeout) should include element withName("input").withAttrValue("type", `type`).withAttrValue("name", name)

  protected def includesHtmlField(in: Future[Result], `type`: String, name: String)(implicit timeout: Timeout): Unit = contentAsHtml(in)(timeout) should include element withName(`type`).withAttrValue("name", name)

  protected def includesHtmlLink(in: Future[Result], hrefValue: String)(implicit timeout: Timeout): Unit = contentAsHtml(in)(timeout) should include element withName("a").withAttrValue("href", hrefValue)

  protected def requestScenario(method: String = "GET",
                                uri: String = s"/$contextPath/",
                                headers: Map[String, String] = Map.empty)(test: Future[Result] => Unit): Unit = {
    new RequestScenario(method, uri, headers) {
      test(route(app, req).get)
    }
  }

  protected def wasOk(resp: Future[Result]): Unit = status(resp) must be (Status.OK)

  protected def wasRedirected(resp: Future[Result]): Unit = status(resp) must be (Status.SEE_OTHER)

  protected def wasNotFound(resp: Future[Result]): Unit = status(resp) must be (Status.NOT_FOUND)

  protected def wasHtml(resp: Future[Result]): Unit = {
    contentType(resp) must be (Some("text/html"))
    charset(resp) must be (Some("utf-8"))
  }

  protected def uriWithContextPath(path: String): String = s"$contextPath$path"

}
