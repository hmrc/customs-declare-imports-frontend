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

import com.google.inject.Inject
import config.AppConfig
import domain.MetaDataMapping
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{CustomsCacheService, CustomsDeclarationsConnector, CustomsDeclarationsResponse}
import views.html.submit_failure

import scala.concurrent.{ExecutionContext, Future}

class SubmitController @Inject()(actions: Actions, cache: CustomsCacheService, customsConnector: CustomsDeclarationsConnector)
                                (implicit override val messagesApi: MessagesApi, ec: ExecutionContext, appConfig: AppConfig)
  extends CustomsController {

  def onFailure: Action[AnyContent] = (actions.auth andThen actions.eori andThen actions.lrn) {
    implicit request =>
      Ok(submit_failure())
  }

  def onSubmit: Action[AnyContent] = (actions.auth andThen actions.eori andThen actions.lrn).async {
    implicit request =>

      cache.fetch(request.eori.value)
        .map(_.map(MetaDataMapping.produce))
        .flatMap {
          _.fold(Future.failed[CustomsDeclarationsResponse](new Exception)) { metaData =>
            customsConnector.submitImportDeclaration(metaData, request.lrn)
          }
        }
        .map {
          _ => Redirect(routes.LandingController.displayLandingPage())
        }
        .recover {
          case _ => Redirect(routes.SubmitController.onFailure())
        }
  }
}
