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

import uk.gov.hmrc.customs.test.assertions.{HtmlAssertions, HttpAssertions}
import uk.gov.hmrc.customs.test.behaviours.{CustomsSpec, RequestHandlerBehaviours}

class UnauthorisedControllerSpec extends CustomsSpec
  with RequestHandlerBehaviours
  with HttpAssertions
  with HtmlAssertions {

  val method = "GET"
  val uri = uriWithContextPath("/enrol")

  s"$method $uri" should {

    "return 200" in withRequest(method, uri) {
      wasOk
    }

    "return HTML" in withRequest(method, uri) {
      wasHtml
    }

    "display message" in withRequest(method, uri) { resp =>
      contentAsHtml(resp) should include element withName("h1").withValue(messagesApi("enrolmentpage.titleAndHeading"))
    }

  }

}
