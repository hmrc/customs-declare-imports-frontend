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

import config.SubmissionJourney
import domain.features.Feature
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.verify
import models.{Cancellation, ChangeReasonCode}
import uk.gov.hmrc.customs.test.assertions.{HtmlAssertions, HttpAssertions}
import uk.gov.hmrc.customs.test.behaviours._
import uk.gov.hmrc.http.HeaderCarrier

class DeclarationControllerSpec extends CustomsSpec
  with AuthenticationBehaviours
  with FeatureBehaviours
  with RequestHandlerBehaviours
  with CustomsDeclarationsApiBehaviours
  with HttpAssertions
  with HtmlAssertions {

  val mrn = randomString(16)
  val get = "GET"
  val post = "POST"
  val submitUri = journeyUri(SubmissionJourney.screens.head)
  val cancelUri = uriWithContextPath(s"/cancel-declaration/$mrn")

  def journeyUri(screen: String): String = uriWithContextPath(s"/submit-declaration/$screen")

  s"$get $submitUri" should {

    "return 200" in withFeatures(enabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>
        withCaching(None)
        withRequest(get, submitUri, headers, session, tags) {
          wasOk
        }
      }
    }

    "return HTML" in withFeatures(enabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequest(get, submitUri, headers, session, tags) {
          wasHtml
        }
      }
    }

    "require authentication" in withFeatures(enabled(Feature.submit)) {
      withoutSignedInUser() { (_, _) =>
        withRequest(get, submitUri) { resp =>
          wasRedirected(ggLoginRedirectUri(submitUri), resp)
        }
      }
    }

    "be behind a feature switch" in withFeatures(disabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequest(get, submitUri, headers, session, tags) {
          wasNotFound
        }
      }
    }
  }

  s"$post $cancelUri" should {
    val payload = Map(
      "changeReasonCode" -> ChangeReasonCode.DUPLICATE.toString,
      "description" -> "a description")
    implicit val hc = HeaderCarrier()

    "return 200 when the form is valid" in withFeatures(enabled(Feature.cancel)) {
      withSignedInUser() { (headers, session, tags) =>
        withImportsBackend
        withRequestAndFormBody(post, cancelUri, headers, session, tags, payload) { resp =>
          wasHtml(resp)
          wasOk(resp)

          verify(mockCustomsDeclarationsConnector).cancelDeclaration(meq(Cancellation(mrn, ChangeReasonCode.withName(payload("changeReasonCode")), payload("description"))))(any(), any())
        }
      }
    }

    val errorsPayload: Map[String, String] = Map.empty

    "return to same page with errors" in withFeatures(enabled(Feature.cancel)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequestAndFormBody(post, cancelUri, headers, session, tags, errorsPayload) { resp =>
          wasBadRequest(resp)
          includesHtmlLink(resp, "#description")
          includesHtmlLink(resp, "#reasonCode")
        }
      }
    }

    "be behind a feature switch" in withFeatures(disabled(Feature.cancel)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequest(post, cancelUri, headers, session, tags) {
          wasNotFound
        }
      }
    }

    "require authentication" in withFeatures(enabled(Feature.cancel)) {
      withoutSignedInUser() { (_, _) =>
        withRequest(post, cancelUri) { resp =>
          wasRedirected(ggLoginRedirectUri(cancelUri), resp)
        }
      }
    }

  }

  s"$get $cancelUri" should {

    "return 200" in withFeatures(enabled(Feature.cancel)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequest(get, cancelUri, headers, session, tags) {
          wasOk
        }
      }
    }

    "return HTML" in withFeatures(enabled(Feature.cancel)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequest(get, cancelUri, headers, session, tags) {
          wasHtml
        }
      }
    }

    "require authentication" in withFeatures(enabled(Feature.cancel)) {
      withoutSignedInUser() { (_, _) =>
        withRequest(get, cancelUri) { resp =>
          wasRedirected(ggLoginRedirectUri(cancelUri), resp)
        }
      }
    }

    "be behind a feature switch" in withFeatures(disabled(Feature.cancel)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequest(get, cancelUri, headers, session, tags) {
          wasNotFound
        }
      }
    }

  }

}
