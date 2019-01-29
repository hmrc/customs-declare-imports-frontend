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
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext
import uk.gov.hmrc.wco.dec.{NamedEntityWithAddress, _}

import scala.concurrent.Future


@Singleton
class GovernmentAgencyGoodsItemsController @Inject()(actions: Actions, cacheService: CustomsCacheService)
  (implicit val appConfig: AppConfig, val messagesApi: MessagesApi) extends CustomsController {

  val additionalDocumentform: Form[GovernmentAgencyGoodsItemAdditionalDocument] = Form(govtAgencyGoodsItemAddDocMapping)
  val additionalInformationform: Form[AdditionalInformation] = Form(additionalInformationMapping)
  val goodsItemValueInformationForm: Form[GoodsItemValueInformation] = Form(goodsItemValueInformationMapping)
  val roleBasedPartiesForm: Form[RoleBasedParty] = Form(roleBasedPartyMapping)
  val originsForm: Form[Origin] = Form(originMapping)
  val namedEntityWithAddressForm: Form[NamedEntityWithAddress] = Form(namedEntityWithAddressMapping)
  val packagingForm: Form[Packaging] = Form(packagingMapping)
  val previousDocumentForm: Form[PreviousDocument] = Form(previousDocumentMapping)

  val goodsItemValueInformationKey = "goodsItemValueInformation"


  def showGoodsItemValuePage(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.fetchAndGetEntry[GoodsItemValueInformation](req.user.eori.get,
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

          cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, CacheKey.goodsItem.key, updatedGoodsItem).map {
            _ => Redirect(routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage())
          }
        })
  }

  def showGoodsItemPage(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](request.user.eori.get, CacheKey.goodsItem.key).map(res =>
        Ok(views.html.gov_agency_goods_items(res)))
  }

  def showPreviousDocuments(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.getByKey(req.eori, CacheKey.goodsItem).map { goodsItem =>
        Ok(views.html.goods_items_previousdocs(previousDocumentForm, goodsItem.map(_.previousDocuments).getOrElse(Seq.empty)))
      }
  }

  def showPackagings(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.getByKey(req.eori, CacheKey.goodsItem).map { goodsItem =>
        Ok(views.html.goods_items_packagings(packagingForm, goodsItem.map(_.packagings).getOrElse(Seq.empty)))
      }
  }

  def showNamedEntryAddressParties(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.getByKey(req.eori, CacheKey.goodsItem).map { goodsItem =>
        Ok(views.html.goods_items_named_entity_parties(namedEntityWithAddressForm, goodsItem.map(_.manufacturers).getOrElse(Seq.empty)))
      }
  }

  def showRoleBasedParties(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.getByKey(req.eori, CacheKey.goodsItem).map {goodsItem =>
        Ok(views.html.goods_items_role_based_parties(roleBasedPartiesForm, goodsItem.map(_.aeoMutualRecognitionParties).getOrElse(Seq.empty)))
      }
  }


  def showOrigins(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.getByKey(req.eori, CacheKey.goodsItem).map {
        case Some(goodsItem) => Ok(views.html.goods_items_origins(roleBasedPartiesForm, goodsItem.origins))
        case _ => Ok(views.html.goods_items_origins(roleBasedPartiesForm, Seq.empty))
      }
  }

  def showGoodsItemsAdditionalInformations(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.getByKey(req.eori, CacheKey.goodsItem).map {
        case Some(goodsItem) => Ok(views.html.goods_items_add_additional_informations(additionalInformationform, goodsItem.additionalInformations))
        case _ => Ok(views.html.goods_items_add_additional_informations(additionalInformationform, Seq.empty))
      }
  }

  def showGovAgencyGoodsItemsAdditionalDocuments(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit req =>
      cacheService.getByKey(req.eori, CacheKey.goodsItem).map {
        case Some(goodsItem) => Ok(views.html.gov_agency_goods_items_add_docs(additionalDocumentform, goodsItem.additionalDocuments))
        case _ => Ok(views.html.gov_agency_goods_items_add_docs(additionalDocumentform, Seq.empty))
      }
  }

  def handleGoodsItemsAdditionalInformationsSubmit(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      additionalInformationform.bindFromRequest().fold(
        (formWithErrors: Form[AdditionalInformation]) =>
          Future.successful(BadRequest(views.html.goods_items_add_additional_informations(formWithErrors, List.empty))),
        form =>
          cacheService.getByKey(request.eori, CacheKey.goodsItem).flatMap { res =>
            val updatedGoodsItem = res match {
              case Some(goodsItem) => goodsItem.copy(additionalInformations = goodsItem.additionalInformations :+ form)
              case None => GovernmentAgencyGoodsItem(additionalInformations = Seq(form))
            }

            cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, CacheKey.goodsItem.key, updatedGoodsItem).map { _ =>
              Ok(views.html.goods_items_add_additional_informations(additionalInformationform, updatedGoodsItem.additionalInformations))
            }
          })
  }

  def handleGovAgencyGoodsItemsAdditionalDocumentsSubmit(): Action[AnyContent] =
    (actions.auth andThen actions.eori).async {
      implicit request =>
        additionalDocumentform.bindFromRequest().fold(
          (formWithErrors: Form[GovernmentAgencyGoodsItemAdditionalDocument]) =>
            Future.successful(BadRequest(views.html.gov_agency_goods_items_add_docs(formWithErrors, List.empty))),
          form =>
            cacheService.getByKey(request.eori, CacheKey.goodsItem).flatMap { res =>
              val updatedGoodsItem = res match {
                case Some(goodsItem) => goodsItem.copy(additionalDocuments = goodsItem.additionalDocuments :+ form)
                case None => GovernmentAgencyGoodsItem(additionalDocuments = Seq(form))
              }

              cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, CacheKey.goodsItem.key, updatedGoodsItem).map {
                _ =>
                  Ok(views.html.gov_agency_goods_items_add_docs(additionalDocumentform, updatedGoodsItem.additionalDocuments))
              }
            })

    }

  def handleRoleBasedPartiesSubmit(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      roleBasedPartiesForm.bindFromRequest().fold(
        (formWithErrors: Form[RoleBasedParty]) =>
          Future.successful(BadRequest(views.html.goods_items_role_based_parties(formWithErrors, List.empty))),
        form =>
          cacheService.getByKey(request.eori, CacheKey.goodsItem).flatMap { res =>
            val updatedGoodsItem = res match {
              case Some(goodsItem) => goodsItem.copy(aeoMutualRecognitionParties = goodsItem.aeoMutualRecognitionParties :+ form)
              case None => GovernmentAgencyGoodsItem(aeoMutualRecognitionParties = Seq(form))
            }

            cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, CacheKey.goodsItem.key,
              updatedGoodsItem).map { _ =>
              Ok(views.html.goods_items_role_based_parties(roleBasedPartiesForm, updatedGoodsItem.aeoMutualRecognitionParties))
            }
          })
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

            cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, CacheKey.goodsItem.key,
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

            cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, CacheKey.goodsItem.key, updatedGoodsItem).map { _ =>
              Ok(views.html.goods_items_named_entity_parties(namedEntityWithAddressForm, updatedGoodsItem.manufacturers))
            }
          })
  }

  def handlePackagingsSubmit(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      packagingForm.bindFromRequest().fold(
        (formWithErrors: Form[Packaging]) =>
          Future.successful(BadRequest(views.html.goods_items_packagings(formWithErrors, List.empty))),
        form =>
          cacheService.getByKey(request.eori, CacheKey.goodsItem).flatMap { res =>
            Logger.info("NamedEntityWithAddress form --->" + form)
            val updatedGoodsItem = res match {
              case Some(goodsItem) => goodsItem.copy(packagings = goodsItem.packagings :+ form)
              case None => GovernmentAgencyGoodsItem(packagings = Seq(form))
            }

            cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, CacheKey.goodsItem.key, updatedGoodsItem).map { _ =>
              Ok(views.html.goods_items_packagings(packagingForm, updatedGoodsItem.packagings))
            }
          })
  }

  def handlePreviousDcoumentsSubmit(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      previousDocumentForm.bindFromRequest().fold(
        (formWithErrors: Form[PreviousDocument]) =>
          Future.successful(BadRequest(views.html.goods_items_previousdocs(formWithErrors, List.empty))),
        form =>
          cacheService.getByKey(request.eori, CacheKey.goodsItem).flatMap { res =>
            val updatedGoodsItem = res match {
              case Some(goodsItem) => goodsItem.copy(previousDocuments = goodsItem.previousDocuments :+ form)
              case None => GovernmentAgencyGoodsItem(previousDocuments = Seq(form))
            }

            cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, CacheKey.goodsItem.key, updatedGoodsItem).map { _ =>
              Ok(views.html.goods_items_previousdocs(previousDocumentForm, updatedGoodsItem.previousDocuments))
            }
          })
  }
}
