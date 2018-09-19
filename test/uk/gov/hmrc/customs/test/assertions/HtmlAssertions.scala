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

package uk.gov.hmrc.customs.test.assertions

import com.gu.scalatest.JsoupShouldMatchers
import org.jsoup.nodes.Element
import play.api.http.{ContentTypes, HttpVerbs}
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._

import scala.concurrent.Future

trait HtmlAssertions extends CustomsAssertions with JsoupShouldMatchers {

  def wasHtml(of: Future[Result]): Unit = of.futureValue.body.contentType must be(Some(ContentTypes.HTML))

  def contentAsHtml(of: Future[Result]): Element = contentAsString(of).asBodyFragment

  def includesHtmlInput(in: Future[Result], name: String, `type`: String = "text", value: String = ""): Unit =
    contentAsHtml(in) should include element withName("input").
      withAttrValue("type", `type`).
      withAttrValue("name", name).
      withAttrValue("value", value)

  def includesHtmlLink(in: Future[Result], href: String): Unit =
    contentAsHtml(in) should include element withName("a").
      withAttrValue("href", href)

  def includeForm(in: Future[Result], action: Call, method: String = HttpVerbs.GET): Unit =
    contentAsHtml(in) should include element withName("form").
      withAttrValue("action", action.url).
      withAttrValue("method", method)

  def includeHtmlTag(in: Future[Result], name: String, value: String): Unit =
    contentAsHtml(in) should include element withName(name).withValue(value)

}
