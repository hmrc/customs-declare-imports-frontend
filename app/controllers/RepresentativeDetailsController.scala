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
import domain.DeclarationFormats._
import forms.DeclarationFormMapping._
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{ Action, AnyContent }
import services.CustomsCacheService
import services.cachekeys.CacheKey
import views.html.representative_details

import scala.concurrent.Future

class RepresentativeDetailsController @Inject()(actions: Actions, cache: CustomsCacheService)(
    implicit override val messagesApi: MessagesApi,
    appConfig: AppConfig
) extends CustomsController {

  val form = Form(agentMapping)

  def onPageLoad: Action[AnyContent] = (actions.auth andThen actions.eori).async { implicit req =>
    cache.getByKey(req.eori, CacheKey.representative).map { representative =>
      val popForm = representative.fold(form)(form.fill)
      Ok(representative_details(popForm))
    }
  }

  def onSubmit: Action[AnyContent] = (actions.auth andThen actions.eori).async { implicit req =>
    form
      .bindFromRequest()
      .fold(
        errors => Future.successful(BadRequest(representative_details(errors))),
        representative =>
          cache.insert(req.eori, CacheKey.representative, representative).map { _ =>
            Redirect(routes.ImporterDetailsController.onPageLoad())
        }
      )
  }
}
