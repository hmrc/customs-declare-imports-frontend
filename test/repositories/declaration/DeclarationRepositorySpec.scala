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

package repositories.declaration

import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.customs.test.CustomsPlaySpec

class DeclarationRepositorySpec extends CustomsPlaySpec with BeforeAndAfterEach {

  val repo = app.injector.instanceOf[DeclarationRepository]

  "repo" should {

    "save declaration with EORI and timestamp" in {
      /*
      The first time an import declaration is submitted, we save it with the user's EORI, their LRN (if provided)
      and the conversation ID we received from the customs-declarations API response, generating a timestamp to record
      when this occurred.
       */
      val eori = randomString(8)
      val lrn = randomString(70)
      val conversationId = randomString(80)
      val before = System.currentTimeMillis()
      repo.insert(DeclarationEntity(
        eori,
        conversationId,
        Some(lrn)
      )).futureValue.ok must be(true)

      // we can now display a list of all the declarations belonging to the current user, searching by EORI
      val found = repo.findByEori(eori).futureValue
      found.length must be(1)
      found(0).eori must be(eori)
      found(0).submissionConversationId must be(conversationId)
      found(0).lrn.get must be(lrn)

      // a timestamp has been generated representing "creation time" of case class instance
      found(0).submittedTimestamp must (be >= before).and(be <= System.currentTimeMillis())
    }

  }

  override protected def afterEach(): Unit = repo.removeAll()

}
