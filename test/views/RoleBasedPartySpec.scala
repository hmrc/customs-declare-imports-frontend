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

import controllers.routes
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import uk.gov.hmrc.wco.dec.RoleBasedParty
import viewmodels.HtmlTable
import views.behaviours.ViewBehaviours
import views.html.components.input_text
import views.html.components.table.table

class RoleBasedPartySpec extends ViewBehaviours
  with ViewMatchers
  with PropertyChecks
  with Generators {

  val form = Form(roleBasedPartyMapping)

  def view(form: Form[RoleBasedParty], roles: Seq[RoleBasedParty], messageKeyPrefix: String): Html =
    views.html.role_based_party(
      form,
      roles,
      messageKeyPrefix,
      routes.StartController.displayStartPage(),
      routes.StartController.displayStartPage()
    )(fakeRequest, messages, appConfig)

  def view(messageKeyPrefix: String): () => Html = () => view(form, Seq.empty, messageKeyPrefix)
  def listView(messageKeyPrefix: String): Seq[RoleBasedParty] => Html = xs => view(form, xs, messageKeyPrefix)

  val messageKeyPrefixes =
    List("additionalSupplyChainActor", "domesticDutyTaxParties")

  for (messageKeyPrefix <- messageKeyPrefixes) {

    s"Role Based Party page for '$messageKeyPrefix'" should {

      behave like normalPage(view(messageKeyPrefix), messageKeyPrefix)
      behave like pageWithBackLink(view(messageKeyPrefix))
      behave like pageWithTableHeadings(listView(messageKeyPrefix), arbitrary[RoleBasedParty], messageKeyPrefix)

      "contain Identifier field" in {

        forAll { roleBasedParty: RoleBasedParty =>

          val popForm = form.fillAndValidate(roleBasedParty)
          val html = view(popForm, Seq.empty, messageKeyPrefix)
          val input = input_text(popForm("id"), "Identifier")

          html must include(input)
        }
      }

      "contain roleCode field" in {

        forAll { roleBasedParty: RoleBasedParty =>

          val popForm = form.fillAndValidate(roleBasedParty)
          val html = view(popForm, Seq.empty, messageKeyPrefix)
          val input = input_text(popForm("roleCode"), "Role code")

          html must include(input)
        }
      }

      "contain table of data" in {

        forAll(listOf(arbitrary[RoleBasedParty])) { roles =>

          val htmlTable =
            table(HtmlTable("Identifier", "Role code")(roles.map(r => (r.id.getOrElse(""), r.roleCode.getOrElse("")))))
          val html = listView(messageKeyPrefix)(roles)

          html must include(htmlTable)
        }
      }
    }
  }
}