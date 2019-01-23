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
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import viewmodels.Table
import views.html.components.table.{table, table_header, table_row}
import views.ViewSpecBase

class TableSpec extends ViewSpecBase
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  def view(testTable: Table[String, String]): Html = table(testTable)

  "table" should {

    "not display table" when {

      "there are no rows" in {

        forAll { table: Table[String, String] =>

          val doc = asDocument(view(table.copy(rows = List())))
          assert(doc.getElementsByTag("table").isEmpty)
        }
      }
    }

    "display only 1 table" in {

      forAll { table: Table[String, String] =>

        whenever(table.rows.nonEmpty) {

          val doc = asDocument(view(table))
          doc.getElementsByTag("table").size() mustBe 1
        }
      }
    }

    "display table header" in {

      forAll { table: Table[String, String] =>

        whenever(table.rows.nonEmpty) {
          
          val header = table_header(table.header)
          view(table) must include(header)
        }
      }
    }
    
    "display every table row" in {

      forAll { table: Table[String, String] =>

        table.rows.foreach { row =>

          val tableRow = table_row(row)
          view(table) must include(tableRow)
        }
      }
    }
  }
}