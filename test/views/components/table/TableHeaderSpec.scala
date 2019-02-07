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

package views.components.table

import generators.Generators
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.twirl.api.Html
import viewmodels.HtmlTableRow
import views.ViewSpecBase
import views.html.components.table._

class TableHeaderSpec extends ViewSpecBase with PropertyChecks with Generators with OptionValues {

  def view[A](row: HtmlTableRow[A]) =
    Html(s"<table>${table_header(row)}</table>")

  "table_header" should {

    "display only a single tr" in {

      forAll { row: HtmlTableRow[String] =>
        val doc = asDocument(view(row))
        doc.getElementsByTag("tr").size() mustBe 1
      }
    }

    "display th's equal to number of values" in {

      forAll { row: HtmlTableRow[String] =>
        val doc = asDocument(view(row))
        doc.getElementsByTag("th").size() mustBe row.values.length + 1
      }
    }

    "display content of each value" in {

      forAll { row: HtmlTableRow[String] =>
        val doc = asDocument(view(row))
        row.map(value => assertContainsText(doc, value))
      }
    }
  }
}
