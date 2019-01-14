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

import domain.features.Feature
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.assertions.{HtmlAssertions, HttpAssertions}
import uk.gov.hmrc.customs.test.behaviours._

class ObligationGuaranteeControllerSpec extends CustomsSpec
  with AuthenticationBehaviours
  with FeatureBehaviours
  with RequestHandlerBehaviours
  with HttpAssertions
  with HtmlAssertions {

  val requestUri = uriWithContextPath("/submit-declaration-guarantees/add-guarantees")
  val get = "GET"
  val postMethod = "POST"

  "ObligationGuranteeController" should {

    "require Authentication" in withFeatures((enabled(Feature.submit))) {
      withoutSignedInUser() {
        withRequest("GET", requestUri) { resp =>
          wasRedirected(ggLoginRedirectUri(requestUri), resp)
        }
      }
    }

    " return 200" in withFeatures((enabled(Feature.submit))) {
      withSignedInUser() { (headers, session, tags) =>
        withCaching(None)
        withRequest("GET", requestUri, headers, session, tags) {
          wasOk
        }
      }
    }

    "display ObligationGurantee fields " in {
      withSignedInUser() { (headers, session, tags) =>
        withCaching(None)
        withRequest("GET", requestUri, headers, session, tags) { resp =>
          contentAsHtml(resp) should include element withValue("amount")
          contentAsHtml(resp) should include element withValue("id")
          contentAsHtml(resp) should include element withValue("referenceId")
          contentAsHtml(resp) should include element withValue("securityDetailsCode")
          contentAsHtml(resp) should include element withValue("accessCode")
          contentAsHtml(resp) should include element withValue("accessCode")
        }
      }
    }

    val invalidPayload = Map(
      "amount" -> "-0",
      "referenceId" -> "name1nasdfghlertghoy asdflgothidlglfdleasdflksdf",
      "id" -> "Address1 Address1 Address1 Address1 Address1 Address1 Address1 Address1",
      "securityDetailsCode" -> "12345678912341234",
      "accessCode" -> "references",
      "submit" -> "Add"
    )

    "validate user input data on click of Add" in withFeatures(enabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>
        withCaching(None)
        withRequestAndFormBody(postMethod, requestUri, headers, session, tags, invalidPayload) { resp =>
          val stringResult = contentAsString(resp)
          stringResult must include("Amount must not be negative")
          stringResult must include("Id should be less than or equal to 35 characters")
          stringResult must include("ReferenceId should be less than or equal to 35 characters")
          stringResult must include("SecurityDetailsCode should be less than or equal to 3 characters")
          stringResult must include("AccessCode should be less than or equal to 4 characters")
          stringResult must include("No Obligation Guarantees Added")
        }
      }
    }
    val emptyValidPayload = Map("submit" -> "Add")
    "empty Obligation guarantee  input is NOT added on click of add" in withFeatures(enabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>
        withCaching(None)
        withRequestAndFormBody(postMethod, requestUri, headers, session, tags, emptyValidPayload) { resp =>
          val stringResult = contentAsString(resp)
          status(resp) must be(Status.OK)

          stringResult must include("No Obligation Guarantees Added")
        }
      }
    }

    val validPayload = Map(
      "amount" -> "10.00",
      "referenceId" -> "name1",
      "id" -> "Address1",
      "securityDetailsCode" -> "123",
      "accessCode" -> "123",
      "submit" -> "Add"
    )

    "valid Obligation guarantee is added on click of add" in withFeatures(enabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequestAndFormBody(postMethod, requestUri, headers, session, tags, validPayload) { resp =>
          val stringResult = contentAsString(resp)
          withCaching(None)
          stringResult must include("Obligation Guarantees added : <td scope=\"row\">1</td>")
        }
      }
    }

    val nextPayload = Map("submit" -> "next")
    "navigate to good items page on click of next " in withFeatures(enabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>
        withCaching(None)
        withRequestAndFormBody(postMethod, requestUri, headers, session, tags, nextPayload) { resp =>
          val header = resp.futureValue.header
          status(resp) must be(SEE_OTHER)
          header.headers.get("Location") must be(Some("/customs-declare-imports/submit-declaration-goods/gov-agency-goods-items"))
        }
      }
    }
    val wrongButton = Map("submit" -> "any")
    "Bad request on incorrect submit button" in withFeatures(enabled(Feature.submit)) {
      withCaching(None)
      withSignedInUser() { (headers, session, tags) =>
        withRequestAndFormBody(postMethod, requestUri, headers, session, tags, wrongButton) { resp =>
          status(resp) must be(BAD_REQUEST)
        }
      }
    }


  }

}
