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
import forms.DeclarationFormMapping._
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import services.cachekeys.CacheKey
import views.html.summary_of_goods

import scala.concurrent.{ExecutionContext, Future}

class SummaryOfGoodsController @Inject()
  (actions: Actions, cache: CustomsCacheService)
  (implicit override val messagesApi: MessagesApi, appConfig: AppConfig, ec: ExecutionContext)
extends CustomsController {

  val form = Form(summaryOfGoodsMapping)

  def onPageLoad: Action[AnyContent] = (actions.auth andThen actions.eori).async { implicit req =>

    cache.getByKey(req.eori, CacheKey.summaryOfGoods).map { summary =>

      val popForm = summary.fold(form)(form.fill)
      Ok(summary_of_goods(popForm))
    }
  }

  def onSubmit: Action[AnyContent] = (actions.auth andThen actions.eori).async { implicit req =>

    form.bindFromRequest().fold(
      errors =>
        Future.successful(BadRequest(summary_of_goods(errors))),

      summary =>
        cache
          .insert(req.eori, CacheKey.summaryOfGoods, summary)
          .map(_ => Redirect(routes.TransportController.onPageLoad()))
    )
  }
}