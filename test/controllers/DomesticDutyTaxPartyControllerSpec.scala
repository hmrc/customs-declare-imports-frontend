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

import domain.auth.{EORI, SignedInUser}
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.behaviours.{CustomsSpec, EndpointBehaviours}
import uk.gov.hmrc.wco.dec.RoleBasedParty
import views.html.role_based_party

class DomesticDutyTaxPartyControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues
  with EndpointBehaviours {

  def form = Form(roleBasedPartyMapping)

  def controller(user: SignedInUser) =
    new DomesticDutyTaxPartyController(new FakeActions(Some(user)), mockCustomsCacheService)

  def view(form: Form[RoleBasedParty] = form, roles: Seq[RoleBasedParty] = Seq.empty): String =
    role_based_party(form, roles, "domesticDutyTaxParties")(fakeRequest, messages, appConfig).body

  val listGen: Gen[Option[List[RoleBasedParty]]] = option(listOf(arbitrary[RoleBasedParty]))

  ".onPageLoad" should {

    behave like okEndpoint("/submit-declaration/add-domestic-duty-tax-party")
    behave like authenticatedEndpoint("/submit-declaration/add-domestic-duty-tax-party")

    "return OK" when {

      "user is signed in" in {

        forAll { user: SignedInUser =>

          val result = controller(user).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe view()
        }
      }
    }

    "return UNAUTHORIZED" when {

      "user has no eori number" in {

        forAll { user: UnauthenticatedUser =>

          val result = controller(user.user).onPageLoad(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "load data from cache" in {

      forAll(arbitrary[SignedInUser], listGen) {
        (user, data) =>

          withCleanCache(EORI(user.eori.value), CacheKey.domesticDutyTaxParty, data) {

            val result = controller(user).onPageLoad(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe view(roles = data.getOrElse(Seq.empty))
          }
      }
    }
  }
}