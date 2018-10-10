/*
 * Copyright 2018 HM Revenue & Customs
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

package domain.auth

import controllers.routes
import org.scalatest.{MustMatchers, WordSpec}
import play.api.mvc.Results
import uk.gov.hmrc.customs.test.CustomsFixtures
import uk.gov.hmrc.play.bootstrap.http.ApplicationException

class SignedInUserSpec extends WordSpec with MustMatchers with CustomsFixtures {

  "eori" should {

    "be NONE for user without CDS enrollment" in {
      userFixture(eori = None).eori must be (None)
    }

    "be SOME for user with CDS enrollment" in {
      val eori = randomString(8)
      userFixture(eori = Some(eori)).eori must be (Some(eori))
    }

  }

  "required EORI" should {

    "throw application exception when not present" in {
      val ex = intercept[ApplicationException] {
        userFixture(eori = None).requiredEori
      }
      ex.result must be(Results.SeeOther(routes.UnauthorisedController.enrol().url))
    }

  }

}
