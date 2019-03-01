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

package controllers.goodsitems

import com.google.inject.Inject
import config.AppConfig
import controllers.{Actions, CustomsController}
import domain.DeclarationFormats._
import forms.DeclarationFormMapping._
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import services.cachekeys.CacheKey
import views.html.goods_item_consignee

import scala.concurrent.{ExecutionContext, Future}

class ConsigneeController @Inject()(actions: Actions, cache: CustomsCacheService)
                                   (implicit override val messagesApi: MessagesApi, appConfig: AppConfig, ec: ExecutionContext)
  extends CustomsController {

  val form = Form(namedEntityWithAddressMapping)

  def onPageLoad: Action[AnyContent] = (actions.auth andThen actions.eori andThen actions.goodsItem) {
    implicit request =>

      val popForm = request.goodsItem.consignee.fold(form)(form.fill)
      Ok(goods_item_consignee(popForm))
  }

  def onSubmit: Action[AnyContent] = (actions.auth andThen actions.eori andThen actions.goodsItem).async {
    implicit request =>

      form.bindFromRequest().fold(
        errors    => Future.successful(BadRequest(goods_item_consignee(errors))),
        consignee =>
          cache.insert(request.eori, CacheKey.goodsItem, request.goodsItem.copy(consignee = Some(consignee))).map { _ =>
            Redirect(routes.ConsignorController.onPageLoad())
          }
      )
  }
}