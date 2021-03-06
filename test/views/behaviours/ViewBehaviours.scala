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

package views.behaviours

import org.scalacheck.Gen
import org.scalacheck.Gen.listOfN
import org.scalatest.prop.PropertyChecks
import play.twirl.api.HtmlFormat
import views.ViewSpecBase

trait ViewBehaviours extends ViewSpecBase with PropertyChecks {

  def normalPage(view: () => HtmlFormat.Appendable,
    messageKeyPrefix: String,
    expectedGuidanceKeys: String*) = {

    "behave like a page with a heading" when {
      "rendered" must {

        "display the correct page heading" in {
          val doc = asDocument(view())
          assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading")
        }
      }
    }

    "behave like a page with a title" when {
      "rendered" must {

        "display the correct browser title" in {
          val doc = asDocument(view())
          assertEqualsMessage(doc, "title", s"$messageKeyPrefix.title")
        }
      }
    }

    behave like pageWithoutHeading(view, messageKeyPrefix, expectedGuidanceKeys: _*)
  }

  def pageWithoutHeading(view: () => HtmlFormat.Appendable,
    messageKeyPrefix: String,
    expectedGuidanceKeys: String*) = {

    "behave like a normal page" when {
      "rendered" must {
        "have the correct banner title" in {
          val doc = asDocument(view())
          val nav = doc.getElementById("proposition-menu")
          val span = nav.children.first
          span.text mustBe messagesApi("common.service.name")
        }

        "display the correct guidance" in {
          val doc = asDocument(view())
          for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
        }
      }
    }
  }

  def pageWithBackLink(view: () => HtmlFormat.Appendable) = {

    "behave like a page with a back link" must {
      "have a back link" in {
        val doc = asDocument(view())
        assertRenderedById(doc, "link-back")
      }
    }
  }

  def pageWithTableHeadings[A](
    view: Seq[A] => HtmlFormat.Appendable,
    data: Gen[A],
    messageKeyPrefix: String): Unit = {

    "behave like a page with a table" must {


      "display a table heading for single item" in {

        forAll(data) { singleItem =>

          val doc = asDocument(view(Seq(singleItem)))

          assertContainsText(doc, messages(s"$messageKeyPrefix.table.heading"))
        }
      }


      "display a table with multiple items in" in {

        forAll(listOfN(10, data)) { multipleItems =>

          whenever(multipleItems.size > 1) {

            val doc = asDocument(view(multipleItems))

            assertContainsText(doc, messages(s"$messageKeyPrefix.table.multiple.heading", multipleItems.size))
          }
        }
      }
    }
  }
}
