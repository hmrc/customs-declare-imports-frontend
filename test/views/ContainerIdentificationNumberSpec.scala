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

import forms.DeclarationFormMapping._
import generators.Generators
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import uk.gov.hmrc.wco.dec.TransportEquipment
import viewmodels.HtmlTable
import views.behaviours.ViewBehaviours
import views.html.components.input_text
import views.html.components.table.table
import views.html.container_identification_number

class ContainerIdentificationNumberSpec extends ViewBehaviours
  with PropertyChecks
  with Generators
  with OptionValues
  with ViewMatchers {

  val form = Form(transportEquipmentMapping)

  def view(form: Form[TransportEquipment] = form, transports: Seq[TransportEquipment] = Seq.empty): Html =
    container_identification_number(form, transports)(fakeRequest, messages, appConfig)

  val simpleView: () => Html = () => view()
  val listView: Seq[TransportEquipment] => Html = xs => view(transports = xs)

  val messagePrefix = "containerIdentificationNumber"

  "Container identification number page" should {

    behave like normalPage(simpleView, messagePrefix)
    behave like pageWithBackLink(simpleView)

    "contain id field" in {

      forAll { transport: TransportEquipment =>

        val popForm = form.fillAndValidate(transport)
        val input = input_text(popForm("id"), "Container identification number")

        view(popForm) must include(input)
      }
    }

    "display table component" in {

      forAll(listOf(arbitrary[TransportEquipment])) { transports =>

        whenever(transports.nonEmpty) {

          val caption = transports.length match {
            case 1 => messages(s"$messagePrefix.table.heading")
            case x => messages(s"$messagePrefix.table.multiple.heading", x)
          }

          val htmlTable =
            HtmlTable("Container identification number")(transports.map(_.id.getOrElse("")))
          val renderedTable = table(htmlTable, Some(caption))

          view(transports = transports) must include(renderedTable)
        }
      }
    }
  }
}