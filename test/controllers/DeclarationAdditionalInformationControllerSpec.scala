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
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.mvc.Result
import play.api.test.Helpers._
import play.twirl.api.Html
import services.CustomsCacheService
import uk.gov.hmrc.customs.test.behaviours.CustomsSpec
import uk.gov.hmrc.wco.dec.AdditionalInformation
import views.html.declaration_additional_information

import scala.concurrent.Future

class DeclarationAdditionalInformationControllerSpec extends CustomsSpec
  with PropertyChecks
  with Generators
  with OptionValues {

  val cacheService = mock[CustomsCacheService]

  def form = Form(additionalInformationMapping)
  def controller(user: Option[SignedInUser]) =
    new DeclarationAdditionalInformationController(new FakeActions(user), cacheService)

  def view(form: Form[AdditionalInformation], additionalInformation: List[AdditionalInformation]): Html =
    declaration_additional_information(form, additionalInformation)(fakeRequest, messages, appConfig)

  ".onPageLoad" should {

    "load data from cache" in {

      val gen = option(listOf(arbitrary[AdditionalInformation]))

      forAll(arbitrary[SignedInUser], gen) { case (user, data) =>

        when(cacheService.fetchAndGetEntry[List[AdditionalInformation]](eqTo(user.eori.value), eqTo("DeclarationAdditionalInformation"))(any(), any(), any()))
          .thenReturn(Future.successful(data))

        val result: Future[Result] = controller(Some(user)).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe view(form, data.getOrElse(List.empty)).body
      }
    }
  }
}