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
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import services.cachekeys.CacheKey
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItem, Packaging}

import scala.concurrent.ExecutionContext

@Singleton
class PackagingController @Inject()(actions: Actions, cacheService: CustomsCacheService)
  (implicit appConfig: AppConfig, override val messagesApi: MessagesApi, ec: ExecutionContext)
extends CustomsController {

  val packagingForm: Form[Packaging] = Form(packagingMapping)

  def onPageLoad: Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.getByKey(req.eori, CacheKey.goodsItem).map { goodsItem =>
        Ok(views.html.goods_items_packagings(packagingForm, goodsItem.map(_.packagings).getOrElse(Seq.empty)))
      }
  }

  def onSubmit: Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      packagingForm.bindFromRequest().fold(
        (formWithErrors: Form[Packaging]) =>
          cacheService.getByKey(request.eori, CacheKey.goodsItem).map(goodsItem =>
            BadRequest(views.html.goods_items_packagings(formWithErrors, goodsItem.map(_.packagings).getOrElse(Seq.empty)))),
        form =>
          cacheService.getByKey(request.eori, CacheKey.goodsItem).flatMap { res =>
            val updatedGoodsItem = res match {
              case Some(goodsItem) => goodsItem.copy(packagings = goodsItem.packagings :+ form)
              case None => GovernmentAgencyGoodsItem(packagings = Seq(form), sequenceNumeric = 0)
            }
            cacheService.insert(request.eori, CacheKey.goodsItem, updatedGoodsItem).map { _ =>
              Redirect(routes.PackagingController.onPageLoad())
            }
          })
  }

}