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

import config.AppConfig
import domain.DeclarationFormats._
import forms.DeclarationFormMapping._
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import services.cachekeys.CacheKey
import views.html.deferred_payments

import scala.concurrent.ExecutionContext

class DeferredPaymentsController @Inject()
  (actions: Actions, cacheService: CustomsCacheService)
  (implicit val appConfig: AppConfig, val messagesApi: MessagesApi, ec: ExecutionContext)
extends CustomsController {

  def form = Form(additionalDocumentMapping)

  def onPageLoad: Action[AnyContent] = (actions.auth andThen actions.eori).async { implicit req =>
    cacheService.getByKey(req.eori, CacheKey.additionalDocuments).map { additionalDocuments =>
      Ok(deferred_payments(form, additionalDocuments.getOrElse(Seq())))
    }
  }

  def onSubmit: Action[AnyContent] = (actions.auth andThen actions.eori).async { implicit req =>
    form.bindFromRequest().fold(
      errors =>
        cacheService.getByKey(req.eori, CacheKey.additionalDocuments).map { additionalDocuments =>
          BadRequest(deferred_payments(errors, additionalDocuments.getOrElse(Seq())))
        },

      additionalDocuments =>
        cacheService
          .upsert(req.eori, CacheKey.additionalDocuments)
          (() => Seq(additionalDocuments), additionalDocuments +: _)
          .map { _ =>
            Redirect(routes.DeferredPaymentsController.onPageLoad())
          }
    )
  }
}
