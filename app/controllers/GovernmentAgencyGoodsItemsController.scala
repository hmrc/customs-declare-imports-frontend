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
import domain.{GoodsItemValueInformation, GovernmentAgencyGoodsItem}
import forms.DeclarationFormMapping._
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import services.cachekeys.CacheKey
import uk.gov.hmrc.wco.dec.{NamedEntityWithAddress, _}

import scala.concurrent.Future


@Singleton
class GovernmentAgencyGoodsItemsController @Inject()(actions: Actions, cacheService: CustomsCacheService)
  (implicit val appConfig: AppConfig, val messagesApi: MessagesApi) extends CustomsController {

  val goodsItemValueInformationForm: Form[GoodsItemValueInformation] = Form(goodsItemValueInformationMapping)
  val originsForm: Form[Origin] = Form(originMapping)
  val namedEntityWithAddressForm: Form[NamedEntityWithAddress] = Form(namedEntityWithAddressMapping)

  val goodsItemValueInformationKey = "goodsItemValueInformation"


  def showGoodsItemValuePage(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.fetchAndGetEntry[GoodsItemValueInformation](req.eori.value,
        goodsItemValueInformationKey).map {
        case Some(form) => Ok(views.html.goods_item_value(goodsItemValueInformationForm.fill(form)))
        case _ => Ok(views.html.goods_item_value(goodsItemValueInformationForm))
      }
  }

  def submitGoodsItemValueSection(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      goodsItemValueInformationForm.bindFromRequest().fold(
        (formWithErrors: Form[GoodsItemValueInformation]) =>
          Future.successful(BadRequest((views.html.goods_item_value(formWithErrors)))),
        form => {
          Logger.info("goodsItemValue form --->" + form)
          val updatedGoodsItem = GovernmentAgencyGoodsItem(goodsItemValue = Some(form))

          cacheService.cache[GovernmentAgencyGoodsItem](request.eori.value, CacheKey.goodsItem.key, updatedGoodsItem).map {
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

  def showOrigins(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.getByKey(req.eori, CacheKey.goodsItem).map {
        case Some(goodsItem) => Ok(views.html.goods_items_origins(originsForm, goodsItem.origins))
        case _ => Ok(views.html.goods_items_origins(originsForm, Seq.empty))
      }
  }


  def handleOriginsSubmit(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      originsForm.bindFromRequest().fold(
        (formWithErrors: Form[Origin]) =>
          Future.successful(BadRequest(views.html.goods_items_origins(formWithErrors, List.empty))),
        form =>
          cacheService.getByKey(request.eori, CacheKey.goodsItem).flatMap { res =>
            val updatedGoodsItem = res match {
              case Some(goodsItem) => goodsItem.copy(origins = goodsItem.origins :+ form)
              case None => GovernmentAgencyGoodsItem(origins = Seq(form))
            }

            cacheService.cache[GovernmentAgencyGoodsItem](request.eori.value, CacheKey.goodsItem.key,
              updatedGoodsItem).map { _ =>
              Ok(views.html.goods_items_origins(originsForm, updatedGoodsItem.origins))
            }
          })
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
              case None => GovernmentAgencyGoodsItem(manufacturers = Seq(form))
            }

            cacheService.cache[GovernmentAgencyGoodsItem](request.eori.value, CacheKey.goodsItem.key, updatedGoodsItem).map { _ =>
              Ok(views.html.goods_items_named_entity_parties(namedEntityWithAddressForm, updatedGoodsItem.manufacturers))
            }
          })
  }
}