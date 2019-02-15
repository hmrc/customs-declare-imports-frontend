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
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import services.cachekeys.CacheKey
import uk.gov.hmrc.wco.dec.{NamedEntityWithAddress, _}

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class GovernmentAgencyGoodsItemsController @Inject()
  (actions: Actions, cacheService: CustomsCacheService)
  (implicit val appConfig: AppConfig, val messagesApi: MessagesApi, ec: ExecutionContext)
extends CustomsController {

  val governmentAgencyGoodsItemForm: Form[GovernmentAgencyGoodsItem] = Form(goodsItemValueInformationMapping)
  val namedEntityWithAddressForm: Form[NamedEntityWithAddress] = Form(namedEntityWithAddressMapping)

  val goodsItemValueInformationKey = "goodsItemValueInformation"


  def showGoodsItemValuePage(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](req.eori.value,
        goodsItemValueInformationKey).map {
        case Some(form) => Ok(views.html.goods_item_value(governmentAgencyGoodsItemForm.fill(form)))
        case _ => Ok(views.html.goods_item_value(governmentAgencyGoodsItemForm))
      }
  }

  def submitGoodsItemValueSection(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      governmentAgencyGoodsItemForm.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(views.html.goods_item_value(formWithErrors))),
        form => {
          Logger.info("goodsItemValue form --->" + form)

          cacheService.cache[GovernmentAgencyGoodsItem](request.eori.value, CacheKey.goodsItem.key, form).map {
            _ => Redirect(routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage())
          }
        })
  }

  def showGoodsItemPage(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](request.eori.value, CacheKey.goodsItem.key).map(res =>
        Ok(views.html.gov_agency_goods_items(res)))
  }


  def showNamedEntryAddressParties(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.getByKey(req.eori, CacheKey.goodsItem).map { goodsItem =>
        Ok(views.html.goods_items_named_entity_parties(namedEntityWithAddressForm, goodsItem.map(_.manufacturers).getOrElse(Seq.empty)))
      }
  }

  def handleNamedEntityPartiesSubmit(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      namedEntityWithAddressForm.bindFromRequest().fold(
        (formWithErrors: Form[NamedEntityWithAddress]) =>
          Future.successful(BadRequest(views.html.goods_items_named_entity_parties(formWithErrors, List.empty))),
        form =>
          cacheService.getByKey(request.eori, CacheKey.goodsItem).flatMap { res =>
            val updatedGoodsItem = res match {
              case Some(goodsItem) => goodsItem.copy(manufacturers = goodsItem.manufacturers :+ form)
              case None => GovernmentAgencyGoodsItem(manufacturers = Seq(form), sequenceNumeric = 0)
            }

            cacheService.cache[GovernmentAgencyGoodsItem](request.eori.value, CacheKey.goodsItem.key, updatedGoodsItem).map { _ =>
              Ok(views.html.goods_items_named_entity_parties(namedEntityWithAddressForm, updatedGoodsItem.manufacturers))
            }
          })
  }
}
