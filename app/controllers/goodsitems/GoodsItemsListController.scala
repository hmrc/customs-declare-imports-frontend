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

import config.AppConfig
import controllers.{Actions, CustomsController}
import domain.DeclarationFormats._
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import services.cachekeys.CacheKey
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsItemsListController @Inject()
  (actions: Actions, cacheService: CustomsCacheService)
  (implicit val appConfig: AppConfig, val messagesApi: MessagesApi, ec: ExecutionContext)
extends CustomsController {

  def onPageLoad(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.getByKey(req.eori, CacheKey.govAgencyGoodsItemsList).map(listItems =>
        Ok(views.html.gov_agency_goods_items_list(listItems.getOrElse(Seq.empty))))
  }

  def saveGoodsItem(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      cacheService.getByKey(request.eori, CacheKey.goodsItem).flatMap {
        case Some(goodsItem) =>
          cacheService.upsert(request.eori, CacheKey.govAgencyGoodsItemsList)(() => Seq(goodsItem),
            goodsItem +: _).map(res =>
              Redirect(routes.GoodsItemsListController.onPageLoad()))
        case _ => Future.successful(Redirect(routes.GoodsItemsListController.onPageLoad()))
      }
  }
}
