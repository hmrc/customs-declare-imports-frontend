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
import uk.gov.hmrc.customs.test.assertions.{ HtmlAssertions, HttpAssertions }
import uk.gov.hmrc.customs.test.behaviours.{
  AuthenticationBehaviours,
  CustomsSpec,
  FeatureBehaviours,
  RequestHandlerBehaviours
}

class LandingControllerSpec
    extends CustomsSpec
    with RequestHandlerBehaviours
    with AuthenticationBehaviours
    with FeatureBehaviours
    with HttpAssertions
    with HtmlAssertions {

  val method = "GET"
  val uri    = uriWithContextPath("/")

  s"$method $uri" should {

    "return 200" in withFeatures(enabled(Feature.landing)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequest(method, uri, headers, session, tags) {
          wasOk
        }
      }
    }

    "return HTML" in withFeatures(enabled(Feature.landing)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequest(method, uri, headers, session, tags) {
          wasHtml
        }
      }
    }

    "display message" in withFeatures(enabled(Feature.landing)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequest(method, uri, headers, session, tags) { resp =>
          contentAsHtml(resp) should include element withName("h1").withValue(messagesApi("common.importDeclarations"))
        }
      }
    }

    "require authentication" in withFeatures(enabled(Feature.landing)) {
      withoutSignedInUser() { (_, _) =>
        withRequest(method, uri) { resp =>
          wasRedirected(ggLoginRedirectUri(uri), resp)
        }
      }
    }

    "be behind feature switch" in withFeatures(disabled(Feature.landing)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequest(method, uri) {
          wasNotFound
        }
      }
    }

  }

}
