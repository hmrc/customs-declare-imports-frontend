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

package config

import controllers.routes
import play.api.mvc.Call

class SubmissionJourney {

  // list of journey screen names as presented in URLs. Note that "submit" is a reserved word!
  val screens: List[String] = List(
    "representative-details",
    "importer-details",
    "seller-details",
    "buyer-details",
    "summary-of-goods",
    "transport",
    "location-of-goods",
    "warehouse-and-customs-offices",
    "delivery-terms",
    "add-previous-documents",
    "check-your-answers"
  )

  // this is where we go "back" to from screen 1
  val start: Call = routes.LandingController.displayLandingPage()

  // this is where we submit the form to after the final screen
  val end: Call = routes.DeclarationController.onSubmitComplete()

  def prev(current: String): Either[Call, String] = current match {
    case left if current == screens.head => Left(start)
    case right if current == screens.last => Right(screens(screens.size - 2))
    case _ => Right(screens.sliding(3).filter(window => window(1) == current).map(found => found.head).toSeq.head)
  }

  def next(current: String, forceLast: Boolean = false): Either[Call, String] = if (forceLast && screens.last != current) {
    Right(screens.last)
  } else current match {
    case left if current == screens.last => Left(end)
    case right if current == screens.head => Right(screens(1))
    case _ => Right(screens.sliding(3).filter(window => window(1) == current).map(found => found.last).toSeq.head)
  }

}

object SubmissionJourney extends SubmissionJourney
