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

package controllers

import domain.auth.SignedInUser
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.behaviours.CustomsSpec
import uk.gov.hmrc.wco.dec.AuthorisationHolder
import views.html.authorisation_holder

class AuthorisationHoldersControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues {

  def form = Form(authorisationHolderMapping)

  def controller(user: Option[SignedInUser]) =
    new AuthorisationHoldersController(new FakeActions(user), mockCustomsCacheService)

  def view(form: Form[AuthorisationHolder] = form, authHolders: Seq[AuthorisationHolder] = Seq()): String =
    authorisation_holder(form, authHolders)(fakeRequest, messages, appConfig).body

  ".onPageLoad" should {

    "return OK" when {

      "user is signed in" in {

        forAll { user: SignedInUser =>

          val result = controller(Some(user)).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view()
        }
      }
    }
  }
}