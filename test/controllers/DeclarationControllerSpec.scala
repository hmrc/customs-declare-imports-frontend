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
import repositories.declaration.{ Submission, SubmissionRepository }
import uk.gov.hmrc.customs.test.assertions.{ HtmlAssertions, HttpAssertions }
import uk.gov.hmrc.customs.test.behaviours._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.ReactiveRepository

class DeclarationControllerSpec
    extends CustomsSpec
    with AuthenticationBehaviours
    with FeatureBehaviours
    with RequestHandlerBehaviours
    with CustomsDeclarationsApiBehaviours
    with MongoBehaviours
    with HttpAssertions
    with HtmlAssertions {

  val mrn                                                  = randomString(16)
  val repo                                                 = component[SubmissionRepository]
  override val repositories: Seq[ReactiveRepository[_, _]] = Seq(repo)
  val get                                                  = "GET"
  val post                                                 = "POST"
  val submitUri                                            = journeyUri(SubmissionJourney.screens.head)

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
}
