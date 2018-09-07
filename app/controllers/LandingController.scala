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

package controllers

import config.AppConfig
import domain.features.Feature
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import repositories.declaration.SubmissionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

@Singleton
class LandingController @Inject()(submissionRepository: SubmissionRepository, actions: Actions)
                                 (implicit val appConfig: AppConfig, val messagesApi: MessagesApi) extends CustomsController {

  def displayLandingPage: Action[AnyContent] = (actions.switch(Feature.landing) andThen actions.auth).async { implicit req =>
    submissionRepository.findByEori(req.user.eori).map { submissions =>
      Ok(views.html.landing(submissions.sortBy(_.submittedTimestamp)))
    }
  }

}
