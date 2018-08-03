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
import services.CustomsDeclarationsConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationController @Inject()(actions: Actions, client: CustomsDeclarationsConnector, val messagesApi: MessagesApi)(implicit val appConfig: AppConfig, ec: ExecutionContext) extends FrontendController with I18nSupport {

  def showSubmitForm: Action[AnyContent] = (actions.switch(Feature.declaration) andThen actions.auth).async { implicit req =>
    Future.successful(Ok(views.html.declaration_form(Forms.submit)))
  }

  def showCancelForm: Action[AnyContent] = (actions.switch(Feature.cancel) andThen actions.auth).async { implicit req =>
    Future.successful(Ok(views.html.cancel_form(Forms.cancel)))
  }

  def handleSubmitForm: Action[AnyContent] = (actions.switch(Feature.declaration) andThen actions.auth).async { implicit req =>
    val bound = Forms.submit.bindFromRequest()
    bound.fold(
      errors => Future.successful(BadRequest(views.html.declaration_form(errors))),
      success => {
        client.submitImportDeclaration(success.toMetaData, success.badgeId).map { b =>
          Ok(views.html.declaration_acknowledgement(b))
        }
      }
    )
  }

  def handleCancelForm: Action[AnyContent] = (actions.switch(Feature.cancel) andThen actions.auth).async { implicit req =>
    val resultForm = Forms.cancel.bindFromRequest()
    resultForm.fold(
      errorsWithErrors => Future.successful(BadRequest(views.html.cancel_form(errorsWithErrors))),
      success => {
        client.cancelImportDeclaration(success.toMetaData, success.badgeId).map { b =>
          Ok(views.html.declaration_acknowledgement(b))
        }
      }
    )
  }

}
