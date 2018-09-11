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

package test.controllers

import config.AppConfig
import controllers.Actions
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import reactivemongo.bson.{BSONDocument, BSONString}
import repositories.declaration.SubmissionRepository
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext

@Singleton
class TestingUtilitiesController @Inject()(actions: Actions, submissionRepository: SubmissionRepository)
                                          (implicit val appConfig: AppConfig, ec: ExecutionContext) extends BaseController {

  def displaySubmissions(eori: String): Action[AnyContent] = Action.async { implicit req =>
    submissionRepository.findByEori(eori).map { found =>
      Ok(Json.toJson(found))
    }
  }

  def setMrnOnSubmission(eori: String, conversationId: String, mrn: String): Action[AnyContent] = Action.async { implicit req =>
    submissionRepository.atomicUpdate(BSONDocument("conversationId" -> BSONString(conversationId)), BSONDocument("$set" -> BSONDocument("mrn" -> mrn))).map {
      case Some(update) => Accepted(Json.toJson(update.updateType.savedValue))
      case None => NotFound
    }
  }

}
