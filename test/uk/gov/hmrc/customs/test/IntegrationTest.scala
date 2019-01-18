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

package uk.gov.hmrc.customs.test

import repositories.declaration.SubmissionRepository
import uk.gov.hmrc.customs.test.assertions.{HtmlAssertions, HttpAssertions}
import uk.gov.hmrc.customs.test.behaviours._
import uk.gov.hmrc.mongo.ReactiveRepository

trait IntegrationTest extends AuthenticationBehaviours
  with FeatureBehaviours
  with RequestHandlerBehaviours
  with CustomsDeclarationsApiBehaviours
  with HttpAssertions
  with HtmlAssertions
  with MongoBehaviours {

  val repo = component[SubmissionRepository]
  override val repositories: Seq[ReactiveRepository[_, _]] = Seq(repo)
}