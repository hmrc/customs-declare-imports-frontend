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

package uk.gov.hmrc.customs.test

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.{ MatchResult, Matcher }
import play.twirl.api.Html

trait ViewMatchers {

  private def asDoc(html: Html): Document = Jsoup.parse(html.body)

  class HtmlContains(right: Html) extends Matcher[Html] {
    override def apply(left: Html): MatchResult =
      MatchResult(
        left.toString.contains(right.toString()),
        s""""${left.toString.take(100)}" did not contain "${right.toString.take(100)}"""",
        s""""${left.toString.take(100)}" contained "${right.toString.take(100)}""""
      )
  }

  class HtmlContainsId(id: String) extends Matcher[Html] {
    override def apply(html: Html): MatchResult =
      MatchResult(asDoc(html).getElementById(id) != null,
                  s""""${html.toString.take(100)}" did not contain id "$id"""",
                  s""""${html.toString.take(100)}" contained "$id"""")
  }

  def include(right: Html) = new HtmlContains(right)

  def includeById(id: String) = new HtmlContainsId(id)
}
