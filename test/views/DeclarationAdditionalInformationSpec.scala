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

import views.html.declaration_additional_information
import views.html.components.input_text
import forms.DeclarationFormMapping.additionalInformationMapping
import org.scalatest.matchers.{MatchResult, Matcher}
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.wco.dec.AdditionalInformation

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

class DeclarationAdditionalInformationSpec extends ViewSpec with ViewMatchers {

  def view(form: Form[AdditionalInformation] = form): Html = declaration_additional_information(form)
  lazy val form = Form(additionalInformationMapping)

  "view" should {

    "contain statement code field" in {

      val input = input_text(form("statementCode"), "Statement Code")
      view() must include(input)
    }

    "contain statement description field" in {

      val input = input_text(form("statementDescription"), "Statement Description")
      view() must include(input)
    }

    "contain statement type code field" in {

      val input = input_text(form("statementTypeCode"), "Statement Type Code")
      view() must include(input)
    }

    "contain pointer.sequenceNumeric field" in {

      val input = input_text(form("pointer[0].sequenceNumeric"), "Pointer Sequence Number")
      view() must include(input)
    }

    "contain pointer.documentSectionCode field" in {

    }

    "contains pointer.tagId field" in {

    }
  }

}