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
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.http.HeaderNames
import play.api.mvc.Action
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex
import scala.xml.NodeSeq

@Singleton
class NotificationController @Inject()()(implicit val appConfig: AppConfig, ec: ExecutionContext) extends BaseController {

  private val t: Regex = "^Bearer abc59609za2q$".r

  // TODO proper OAuth2 authentication via new auth action
  def receive(): Action[NodeSeq] = Action.async(parse.xml) { implicit req =>
    if (req.headers.get(HeaderNames.AUTHORIZATION).getOrElse("") == "Bearer abc59609za2q") {
      Logger.info(s"Received notification: ${req.body.mkString}")
      Future.successful(Ok)
    } else {
      Logger.info(s"Unauthorized call to nofify: ${req.headers.get(HeaderNames.AUTHORIZATION)}")
      Future.successful(Unauthorized)
    }
  }

}
