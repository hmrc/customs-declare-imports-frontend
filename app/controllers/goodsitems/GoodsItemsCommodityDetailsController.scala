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
import forms.DeclarationFormMapping._
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import services.cachekeys.CacheKey
import uk.gov.hmrc.wco.dec.Commodity
import views.html.goodsitems.goods_items_commodity_details

import scala.concurrent.{ExecutionContext, Future}

class GoodsItemsCommodityDetailsController @Inject()(actions: Actions, cacheService: CustomsCacheService)
                                                    (implicit appConfig: AppConfig, override val messagesApi: MessagesApi, ec: ExecutionContext)
  extends CustomsController {

  val form = Form(commodityMapping)

  def onPageLoad: Action[AnyContent] = (actions.auth andThen actions.eori andThen actions.goodsItem) { implicit req =>

    val popForm = req.goodsItem.commodity.fold(form)(form.fill)
    Ok(goods_items_commodity_details(popForm))
  }

  def onSubmit: Action[AnyContent] = (actions.auth andThen actions.eori andThen actions.goodsItem).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[Commodity]) =>
          Future.successful(BadRequest(goods_items_commodity_details(formWithErrors))),
        commodity => {
          val updatedGoodsItem = request.goodsItem.copy(commodity = Some(commodity))

          cacheService.insert(request.eori, CacheKey.goodsItem, updatedGoodsItem).map { _ =>
            Redirect(controllers.routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage())
          }
        })
  }
}
