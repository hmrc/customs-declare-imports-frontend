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
import config.{AppConfig, ErrorHandler}
import domain.auth.{AuthenticatedRequest, EORIRequest, SignedInUser}
import domain.features.Feature.Feature
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.Future

class FakeActions(signedInUser: Option[SignedInUser])(implicit messages: MessagesApi, appConfig: AppConfig) extends Actions {

  override def auth: ActionBuilder[AuthenticatedRequest] with ActionRefiner[Request, AuthenticatedRequest] =
    new ActionBuilder[AuthenticatedRequest] with ActionRefiner[Request, AuthenticatedRequest] {
      override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] =
        Future.successful(signedInUser.map(AuthenticatedRequest(request, _)).toRight(Unauthorized("")))
    }

  override def eori: ActionRefiner[AuthenticatedRequest, EORIRequest] =
    new EORIAction(new ErrorHandler())

  override def switch(feature: Feature): ActionBuilder[Request] with ActionFilter[Request] = ???
}