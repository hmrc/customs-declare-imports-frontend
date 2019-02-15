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

package views

import controllers.routes
import models.{Declaration, DeclarationAction, DeclarationActionType, DeclarationNotification}
import org.joda.time.DateTime
import org.scalatest.prop.PropertyChecks
import play.twirl.api.Html
import uk.gov.hmrc.customs.test.ViewMatchers
import views.behaviours.ViewBehaviours
import views.html.landing

class LandingSpec extends ViewBehaviours with ViewMatchers with PropertyChecks {

  def view(declarations: Seq[Declaration]): Html = landing(declarations)(fakeRequest, messages, appConfig)
  lazy val view: () => Html = () => view(Seq.empty)

  val messagePrefix = "landingpage"
  val lrn = "A Local Reference Number"
  val mrn = "Some unique MRN"
  val now = DateTime.now
  val submissionAction = DeclarationAction(now, DeclarationActionType.SUBMISSION, Seq(DeclarationNotification(1, "conversationId1", now)))
  val cancellationAction = DeclarationAction(now, DeclarationActionType.CANCELLATION, Seq(DeclarationNotification(2, "conversationId2", now)))

  class Setup(declarations: Seq[Declaration] = Seq.empty) {
    val doc = asDocument(view(declarations))
  }

  "Landing page" when {
    behave like normalPage(view, messagePrefix)

    "there are no declarations" should {
      lazy val decls = Seq.empty

      "contain a button to create a new declaration" in new Setup(decls) {
        val href = routes.DeclarationController.displaySubmitForm("declarant-details").url

        assertContainsLink(doc, messages("common.button.createNew"), href)
      }

      "contain the empty submission text" in new Setup(decls) {
        assertContainsMessage(doc, "landingpage.submissions.empty")
      }
    }

    "there is a single new declaration" should {
      lazy val decls = Seq(Declaration(now, Some(lrn), None, Seq(DeclarationAction(now, DeclarationActionType.SUBMISSION))))

      "contain the LRN" in new Setup(decls) {
        assertContainsText(doc, s"LRN $lrn")
      }

      "not contain the MRN" in new Setup(decls) {
        doc.text() must not include(s"MRN")
      }

      "contain the submitted metadata" in new Setup(decls) {
        assertContainsText(doc, s"Submitted on ${now.toString("dd MMMM YYYY HH:mm")}")
      }

      "not contain any notifications" in new Setup(decls) {
        doc.text() must not include("Submission notifications")
        doc.text() must not include("Cancellation notifications")
      }

      "not contain a cancel button for the declaration" in new Setup(decls) {
        doc.text() must not include(s"Cancel declaration $lrn")
      }
    }

    "there is a single acknowledged declaration" should {
      lazy val decls = Seq(Declaration(now, Some(lrn), Some(mrn), actions = Seq(submissionAction)))

      "contain the LRN" in new Setup(decls) {
        assertContainsText(doc, s"LRN $lrn")
      }

      "contain the MRN" in new Setup(decls) {
        assertContainsText(doc, s"MRN $mrn")
      }

      "contain the submitted metadata" in new Setup(decls) {
        assertContainsText(doc, s"Submitted on ${now.toString("dd MMMM YYYY HH:mm")}")
      }

      "contain the submission notification header" in new Setup(decls) {
        assertContainsText(doc, "Submission notifications")
      }

      "contain the submission action notification message and datetime" in new Setup(decls) {
        val functionCode = submissionAction.notifications.head.functionCode
        val message = messages(s"declaration.notification.$functionCode")
        assertContainsText(doc, s"$message ${now.toString("dd MMMM YYYY")} ${now.toString("HH:mm")}")
      }

      "not contain the cancellation notification header" in new Setup(decls) {
        doc.text() must not include("Cancellation notifications")
      }

      "contain a cancel button" in new Setup(decls) {
        assertContainsText(doc, s"Cancel declaration $lrn")
      }
    }

    "there is a single cancelled declaration" should {
      lazy val decls = Seq(Declaration(now, Some(lrn), Some(mrn), Seq(submissionAction, cancellationAction)))

      "contain the LRN" in new Setup(decls) {
        assertContainsText(doc, s"LRN $lrn")
      }

      "contain the MRN" in new Setup(decls) {
        assertContainsText(doc, s"MRN $mrn")
      }

      "contain the submitted metadata" in new Setup(decls) {
        assertContainsText(doc, s"Submitted on ${now.toString("dd MMMM YYYY HH:mm")}")
      }

      "contain the submission notification header" in new Setup(decls) {
        assertContainsText(doc, "Submission notifications")
      }

      "contain the submission action notification message and datetime" in new Setup(decls) {
        val functionCode = submissionAction.notifications.head.functionCode
        val message = messages(s"declaration.notification.$functionCode")
        assertContainsText(doc, s"$message ${now.toString("dd MMMM YYYY")} ${now.toString("HH:mm")}")
      }

      "contain the cancellation notification header" in new Setup(decls) {
        assertContainsText(doc, "Cancellation notifications")
      }

      "contain the cancellation action notification message and datetime" in new Setup(decls) {
        val functionCode = cancellationAction.notifications.head.functionCode
        val message = messages(s"declaration.notification.$functionCode")
        assertContainsText(doc, s"$message ${now.toString("dd MMMM YYYY")} ${now.toString("HH:mm")}")
      }

      "not contain a cancel button" in new Setup(decls) {
        doc.text() must not include(s"Cancel declaration $lrn")
      }
    }

    "there are multiple declarations" should {
      val decl1 = Declaration(now, Some("AALRNAA"), None)
      val decl2 = Declaration(now.minusMinutes(1), Some("BBLRNBB"))
      val decl3 = Declaration(now.minusMinutes(2), Some("CCLRNCC"))
      lazy val decls = Seq(decl1, decl2, decl3)

      "list each individual declaration" in new Setup(decls) {
        assertContainsText(doc, decl1.submittedDateTime.toString("dd MMMM YYYY HH:mm"))
        assertContainsText(doc, decl1.localReferenceNumber.get)

        assertContainsText(doc, decl2.submittedDateTime.toString("dd MMMM YYYY HH:mm"))
        assertContainsText(doc, decl2.localReferenceNumber.get)

        assertContainsText(doc, decl3.submittedDateTime.toString("dd MMMM YYYY HH:mm"))
        assertContainsText(doc, decl3.localReferenceNumber.get)
      }
    }
  }
}
