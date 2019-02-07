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
import controllers.{ Actions, CustomsController }
import domain.DeclarationFormats._
import domain.GovernmentAgencyGoodsItem
import forms.DeclarationFormMapping._
import javax.inject.{ Inject, Singleton }
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{ Action, AnyContent }
import services.CustomsCacheService
import services.cachekeys.CacheKey
import uk.gov.hmrc.wco.dec.PreviousDocument

@Singleton
class PreviousDocumentsController @Inject()(actions: Actions, cacheService: CustomsCacheService)(
    implicit appConfig: AppConfig,
    override val messagesApi: MessagesApi
) extends CustomsController {

  def previousDocumentForm: Form[PreviousDocument] = Form(previousDocumentMapping)

  def onPageLoad: Action[AnyContent] = (actions.auth andThen actions.eori).async { implicit req =>
    cacheService.getByKey(req.eori, CacheKey.goodsItem).map { goodsItem =>
      Ok(
        views.html.goods_items_previousdocs(previousDocumentForm,
                                            goodsItem.map(_.previousDocuments).getOrElse(Seq.empty))
      )
    }
  }

  def onSubmit: Action[AnyContent] = (actions.auth andThen actions.eori).async { implicit request =>
    previousDocumentForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[PreviousDocument]) =>
          cacheService
            .getByKey(request.eori, CacheKey.goodsItem)
            .map(
              goodsItem =>
                BadRequest(
                  views.html.goods_items_previousdocs(formWithErrors,
                                                      goodsItem.map(_.previousDocuments).getOrElse(Seq.empty))
              )
          ),
        form =>
          cacheService.getByKey(request.eori, CacheKey.goodsItem).flatMap { res =>
            val updatedGoodsItem = res match {
              case Some(goodsItem) => goodsItem.copy(previousDocuments = goodsItem.previousDocuments :+ form)
              case None            => GovernmentAgencyGoodsItem(previousDocuments = Seq(form))
            }

            cacheService
              .cache[GovernmentAgencyGoodsItem](request.eori.value, CacheKey.goodsItem.key, updatedGoodsItem)
              .map { _ =>
                Redirect(routes.PreviousDocumentsController.onPageLoad())
              }
        }
      )
  }

}
