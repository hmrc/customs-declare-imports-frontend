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

package views

import forms.DeclarationFormMapping.additionalInformationMapping
import generators.Generators
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.wco.dec.AdditionalInformation
import views.behaviours.ViewBehaviours
import views.html.components.input_text
import views.html.declaration_additional_information

trait ViewMatchers {

  class HtmlContains(right: Html) extends Matcher[Html] {
    override def apply(left: Html): MatchResult =
      MatchResult(
        left.toString.contains(right.toString()),
        s""""${left.toString.take(100)}" did not contain "${right.toString.take(100)}"""",
        s""""${left.toString.take(100)}" contained "${right.toString.take(100)}"""")
  }

  def include(right: Html) = new HtmlContains(right)
}

class DeclarationAdditionalInformationSpec extends ViewBehaviours with ViewMatchers with PropertyChecks with Generators  {

  val emptyAdditionalInfo: Seq[AdditionalInformation] = Seq.empty

  def view(form: Form[AdditionalInformation] = form,
           additionalInformation: Seq[AdditionalInformation] = emptyAdditionalInfo): Html =
    declaration_additional_information(form, additionalInformation)(fakeRequest, messages, appConfig)

  val view: () => Html = () => declaration_additional_information(form, emptyAdditionalInfo)(fakeRequest, messages, appConfig)

  val messagePreFix = "additionalInformation"

  lazy val form = Form(additionalInformationMapping)

  "view" should {

    behave like pageWithoutHeading(view, messagePreFix )

    "have title" in {

      val doc = asDocument(view())

      assertEqualsMessage(doc, "title", s"$messagePreFix.title")
    }

    "have heading" in {

      val doc = asDocument(view())

      assertEqualsMessage(doc, "h2", s"$messagePreFix.header")
    }

    "contain statement code field" in {

      val input = input_text(form("statementCode"), "Statement Code")
      view() must include(input)
    }

    "contain statement description field" in {

      val input = input_text(form("statementDescription"), "Statement Description")
      view() must include(input)
    }

    "contain limit date time field" in {

      val input = input_text(form("limitDateTime"), "Limit Date Time")
      view() must include(input)
    }

    "contain statement type code field" in {

      val input = input_text(form("statementTypeCode"), "Statement Type Code")
      view() must include(input)
    }

    "not display additional information table if additional information is not available" in {

      val doc = asDocument(view(form, emptyAdditionalInfo))

      assertContainsText(doc, messages("additionalInformation.informationNotAvailable"))
    }

    "display additional information table if additional information is available" in {

      forAll { additionalInfo: AdditionalInformation =>
        val additionalInfoSeq = Seq(additionalInfo)
        val doc = asDocument(view(form, additionalInfoSeq))

        assertContainsText(doc, messages("additionalInformation.informationAvailable") + s" ${additionalInfoSeq.size}")
      }
    }

    "display statement code in table" in {

      forAll { additionalInfo: AdditionalInformation =>
        val additionalInfoSeq = Seq(additionalInfo)
        val doc = asDocument(view(form, additionalInfoSeq))

        additionalInfo.statementCode.map(assertContainsText(doc, _))
      }
    }

    "display statement description in table" in {

      forAll { additionalInfo: AdditionalInformation =>
        val additionalInfoSeq = Seq(additionalInfo)
        val doc = asDocument(view(form, additionalInfoSeq))

        additionalInfo.statementDescription.map(assertContainsText(doc, _))
      }
    }

    "display limit date time in table" in {

      forAll { additionalInfo: AdditionalInformation =>
        val additionalInfoSeq = Seq(additionalInfo)
        val doc = asDocument(view(form, additionalInfoSeq))

        additionalInfo.limitDateTime.map(assertContainsText(doc, _))

      }
    }

    "display statement type code in table" in {

      forAll { additionalInfo: AdditionalInformation =>
        val additionalInfoSeq = Seq(additionalInfo)
        val doc = asDocument(view(form, additionalInfoSeq))

        additionalInfo.statementTypeCode.map(assertContainsText(doc, _))
      }
    }
  }
}