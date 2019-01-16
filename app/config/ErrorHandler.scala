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
import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.{Request, RequestHeader, Result, Results}
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession}
import uk.gov.hmrc.http.{Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import play.api.mvc.Results.Status

@Singleton
class ErrorHandler @Inject()(implicit val messagesApi: MessagesApi, val appConfig: AppConfig) extends FrontendErrorHandler with AuthRedirects {

  override def config: Configuration = appConfig.runModeConfiguration

  override def env: Environment = appConfig.environment

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html =
    views.html.error_template(pageTitle, heading, message)

  override def resolveError(rh: RequestHeader, ex: Throwable): Result = {
    implicit val req: Request[_] = Request(rh, "")
    ex match {
      case _: NoActiveSession => toGGLogin(rh.uri)
      case _: InsufficientEnrolments => Results.SeeOther(routes.UnauthorisedController.enrol().url)
      case e: Upstream4xxResponse => new Status(e.reportAs)(views.html.api_4xx_error())
      case e: Upstream5xxResponse => new Status(e.reportAs)(views.html.api_5xx_error())
      case _ => super.resolveError(rh, ex)
    }
  }
}

// TODO handle Upstream4xxResponse and Upstream5xxResponse exceptions (thrown by API connector)
