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
import domain.features.Feature
import domain.{GoodsItemValueInformation, GovernmentAgencyGoodsItem}
import forms.DeclarationFormMapping._
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext
import uk.gov.hmrc.wco.dec.{NamedEntityWithAddress, _}

import scala.concurrent.Future


@Singleton
class GovernmentAgencyGoodsItemsController @Inject()(actions: Actions, cacheService: CustomsCacheService)
  (implicit val appConfig: AppConfig, val messagesApi: MessagesApi) extends CustomsController {

  val additionalDocumentform: Form[GovernmentAgencyGoodsItemAdditionalDocument] = Form(govtAgencyGoodsItemAddDocMapping)
  val additionalInformationform: Form[AdditionalInformation] = Form(additionalInformationMapping)
  val goodsItemValueInformationForm: Form[GoodsItemValueInformation] = Form(goodsItemValueInformationMapping)
  val governmentProcedureForm: Form[GovernmentProcedure] = Form(governmentProcedureMapping)
  val roleBasedPartiesForm: Form[RoleBasedParty] = Form(roleBasedPartyMapping)
  val originsForm: Form[Origin] = Form(originMapping)
  val namedEntityWithAddressForm: Form[NamedEntityWithAddress] = Form(namedEntityWithAddressMapping)
  val packagingForm: Form[Packaging] = Form(packagingMapping)
  val previousDocumentForm: Form[PreviousDocument] = Form(previousDocumentMapping)

  val goodsItemValueInformationKey = "goodsItemValueInformation"
  val GOV_AGENCY_GOODS_ITEMS_LIST_CACHE_KEY = "GovAgencyGoodsItems"
  val GOV_AGENCY_GOODS_ITEM_CACHE_KEY = "GovAgencyGoodsItem"


  def showGoodsItemValuePage(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cacheService.fetchAndGetEntry[GoodsItemValueInformation](req.user.eori.get, goodsItemValueInformationKey)(hc, goodsItemValueFormats, MdcLoggingExecutionContext.fromLoggingDetails).map {
        case Some(form) => Ok(views.html.goods_item_value(goodsItemValueInformationForm.fill(form)))
        case _ => Ok(views.html.goods_item_value(goodsItemValueInformationForm))
      }
  }

  def submitGoodsItemValueSection(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit request =>
      goodsItemValueInformationForm.bindFromRequest().fold(
        (formWithErrors: Form[GoodsItemValueInformation]) =>
          Future.successful(BadRequest((views.html.goods_item_value(formWithErrors)))),
        form => {
          Logger.info("goodsItemValue form --->" + form)
          val updatedGoodsItem = GovernmentAgencyGoodsItem(goodsItemValue = Some(form))

          cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY, updatedGoodsItem).map {
            _ => Redirect(routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage())
          }
        })

  }

  def showGoodsItemPage(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit request =>
      cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).map(res =>
        Ok(views.html.gov_agency_goods_items(res)))
  }

  def saveGoodsItem(): Action[AnyContent] = (actions.auth andThen actions.eori).async {
    implicit request =>
      val optionSelected = request.body.asFormUrlEncoded.get("add").headOption
      optionSelected match {
        case Some("AddGovernmentAgencyGoodsItemAdditionalDocument") => Future.successful(
          Redirect(routes.GovernmentAgencyGoodsItemsController.showGovAgencyGoodsItemsAdditionalDocuments()))
        case Some("AddAdditionalInformation") => Future.successful(
          Redirect(routes.GovernmentAgencyGoodsItemsController.showGoodsItemsAdditionalInformations()))
        case Some("AddMutualRecognitionParties") => Future.successful(
          Redirect(routes.GovernmentAgencyGoodsItemsController.showRoleBasedParties()))
        case Some("AddDomesticDutyTaxParties") => Future.successful(
          Redirect(routes.GovernmentAgencyGoodsItemsController.showRoleBasedParties()))
        case Some("AddGovernmentProcedures") => Future.successful(
          Redirect(routes.GovernmentAgencyGoodsItemsController.showGovernmentProcedures()))
        case Some("AddOrigins") => Future.successful(
          Redirect(routes.GovernmentAgencyGoodsItemsController.showOrigins()))
        case Some("AddManufacturers") => Future.successful(
          Redirect(routes.GovernmentAgencyGoodsItemsController.showNamedEntryAddressParties()))
        case Some("AddPackagings") => Future.successful(
          Redirect(routes.GovernmentAgencyGoodsItemsController.showPackagings()))
        case Some("AddPreviousDocuments") => Future.successful(
          Redirect(routes.GovernmentAgencyGoodsItemsController.showPreviousDocuments()))
        case Some("AddRefundRecipientParties") => Future.successful(
          Redirect(routes.GovernmentAgencyGoodsItemsController.showNamedEntryAddressParties()))
        case Some("SaveGoodsItem") => Future.successful(
          Redirect(goodsitems.routes.GoodsItemsListController.saveGoodsItem()))

        case _ => Logger.error("wrong selection => " + optionSelected.get)
          Future.successful(BadRequest("This request is not allowed"))
      }
  }

  def showPreviousDocuments(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](req.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).map { res =>
        val docs: Seq[PreviousDocument] = if (res.isDefined) {
          res.get.previousDocuments
        } else Seq.empty
        Ok(views.html.goods_items_previousdocs(previousDocumentForm, docs))
      }
  }

  def showPackagings(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](req.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).map { res =>
        val docs: Seq[Packaging] = if (res.isDefined) {
          res.get.packagings
        } else Seq.empty
        Ok(views.html.goods_items_packagings(packagingForm, docs))
      }
  }

  def showNamedEntryAddressParties(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](req.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).map { res =>
        val docs: Seq[NamedEntityWithAddress] = if (res.isDefined) {
          res.get.manufacturers
        } else Seq.empty
        Ok(views.html.goods_items_named_entity_parties(namedEntityWithAddressForm, docs))
      }
  }

  def showRoleBasedParties(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](req.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).map { res =>
        val docs: Seq[RoleBasedParty] = if (res.isDefined) {
          res.get.aeoMutualRecognitionParties
        } else Seq.empty
        Ok(views.html.goods_items_role_based_parties(roleBasedPartiesForm, docs))
      }
  }

  def showGovernmentProcedures(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](req.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).map { res =>
        val docs: Seq[GovernmentProcedure] = if (res.isDefined) {
          res.get.governmentProcedures
        } else Seq.empty
        Ok(views.html.goods_items_government_procedures(governmentProcedureForm, docs))
      }
  }

  def showOrigins(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](req.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).map { res =>
        val docs: Seq[Origin] = if (res.isDefined) {
          res.get.origins
        } else Seq.empty
        Ok(views.html.goods_items_origins(roleBasedPartiesForm, docs))
      }
  }

  def showGoodsItemsAdditionalInformations(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](req.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).map { res =>
        val docs: Seq[AdditionalInformation] = if (res.isDefined) {
          res.get.additionalInformations
        } else Seq.empty
        Ok(views.html.goods_items_add_additional_informations(additionalInformationform, docs))
      }
  }

  def showGovAgencyGoodsItemsAdditionalDocuments(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](req.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).map { res =>
        val docs: Seq[GovernmentAgencyGoodsItemAdditionalDocument] = if (res.isDefined) {
          res.get.additionalDocuments
        } else Seq.empty
        Ok(views.html.gov_agency_goods_items_add_docs(additionalDocumentform, docs))
      }
  }

  def handleGoodsItemsAdditionalInformationsSubmit(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit request =>
      val optionSelected = request.body.asFormUrlEncoded.get("submit").headOption
      optionSelected match {
        case Some("Add") =>
          additionalInformationform.bindFromRequest().fold(
            (formWithErrors: Form[AdditionalInformation]) =>
              Future.successful(BadRequest(views.html.goods_items_add_additional_informations(formWithErrors, List.empty))),
            form =>
              cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).flatMap { res =>
                Logger.info("additionalInformationform form --->" + form)
                val updatedGoodsItem = if (res.isDefined)
                  res.get.copy(additionalInformations = res.get.additionalInformations :+ form)
                else GovernmentAgencyGoodsItem(additionalInformations = Seq(form))

                cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY, updatedGoodsItem).map { _ =>
                  Ok(views.html.goods_items_add_additional_informations(additionalInformationform, updatedGoodsItem.additionalInformations))
                }
              })

        case Some("next") => Future.successful(Redirect(routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage()))
        case _ => Logger.error("wrong selection => " + optionSelected.get)
          Future.successful(BadRequest("This action is not allowed"))
      }
  }

  def handleGovernmentProceduresSubmit(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit request =>
      val optionSelected = request.body.asFormUrlEncoded.get("submit").headOption
      optionSelected match {
        case Some("Add") =>
          governmentProcedureForm.bindFromRequest().fold(
            (formWithErrors: Form[GovernmentProcedure]) =>
              Future.successful(BadRequest(views.html.goods_items_government_procedures(formWithErrors, List.empty))),
            form =>
              cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).flatMap { res =>
                Logger.info("GovernmentProcedureForm form --->" + form)
                val updatedGoodsItem = if (res.isDefined)
                  res.get.copy(governmentProcedures = res.get.governmentProcedures :+ form)
                else GovernmentAgencyGoodsItem(governmentProcedures = Seq(form))

                cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY, updatedGoodsItem).map {
                  _ => Ok((views.html.goods_items_government_procedures(governmentProcedureForm, updatedGoodsItem.governmentProcedures)))
                }
              })

        case Some("next") => Future.successful(Redirect(routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage()))
        case _ => Logger.error("wrong selection => " + optionSelected.get)
          Future.successful(BadRequest("This action is not allowed"))
      }
  }

  def handleGovAgencyGoodsItemsAdditionalDocumentsSubmit(): Action[AnyContent] =
    (actions.switch(Feature.submit) andThen actions.auth).async {
      implicit request =>
        val optionSelected = request.body.asFormUrlEncoded.get("submit").headOption
        optionSelected match {
          case Some("Add") =>
            additionalDocumentform.bindFromRequest().fold(
              (formWithErrors: Form[GovernmentAgencyGoodsItemAdditionalDocument]) =>
                Future.successful(BadRequest(views.html.gov_agency_goods_items_add_docs(formWithErrors, List.empty))),
              form =>
                cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).flatMap { res =>
                  Logger.info("additionalDocumentform  --->" + form)
                  val updatedGoodsItem = {
                    if (res.isDefined) {
                      res.get.copy(additionalDocuments = res.get.additionalDocuments :+ form)
                    }
                    else {
                      GovernmentAgencyGoodsItem(additionalDocuments = Seq(form))
                    }
                  }

                  cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY, updatedGoodsItem).map {
                    _ =>
                      Ok(views.html.gov_agency_goods_items_add_docs(additionalDocumentform, updatedGoodsItem.additionalDocuments))
                  }
                })
          case Some("next") => Future.successful(Redirect(routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage()))
          case _ => Logger.error("wrong selection => " + optionSelected.get)
            Future.successful(BadRequest("This action is not allowed"))
        }
    }

  def handleRoleBasedPartiesSubmit(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit request =>
      val optionSelected = request.body.asFormUrlEncoded.get("submit").headOption
      optionSelected match {
        case Some("Add") =>
          roleBasedPartiesForm.bindFromRequest().fold(
            (formWithErrors: Form[RoleBasedParty]) =>
              Future.successful(BadRequest(views.html.goods_items_role_based_parties(formWithErrors, List.empty))),
            form =>
              cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).flatMap { res =>
                Logger.info("roleBasedPartiesForm form --->" + form)
                val updatedGoodsItem = if (res.isDefined)
                  res.get.copy(aeoMutualRecognitionParties = res.get.aeoMutualRecognitionParties :+ form)
                else GovernmentAgencyGoodsItem(aeoMutualRecognitionParties = Seq(form))

                cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY,
                  updatedGoodsItem).map { _ =>
                  Ok(views.html.goods_items_role_based_parties(roleBasedPartiesForm, updatedGoodsItem.aeoMutualRecognitionParties))
                }
              })
        case Some("next") => Future.successful(Redirect(routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage()))
        case _ => Logger.error("wrong selection => " + optionSelected.get)
          Future.successful(BadRequest("This action is not allowed"))
      }
  }

  def handleOriginsSubmit(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit request =>
      val optionSelected = request.body.asFormUrlEncoded.get("submit").headOption
      optionSelected match {
        case Some("Add") =>
          originsForm.bindFromRequest().fold(
            (formWithErrors: Form[Origin]) =>
              Future.successful(BadRequest(views.html.goods_items_origins(formWithErrors, List.empty))),
            form =>
              cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).flatMap { res =>
                Logger.info("originsForm form --->" + form)
                val updatedGoodsItem = if (res.isDefined)
                  res.get.copy(origins = res.get.origins :+ form)
                else GovernmentAgencyGoodsItem(origins = Seq(form))

                cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY,
                  updatedGoodsItem).map { _ =>
                  Ok(views.html.goods_items_origins(originsForm, updatedGoodsItem.origins))
                }
              })
        case Some("next") => Future.successful(Redirect(routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage()))
        case _ => Logger.error("wrong selection => " + optionSelected.get)
          Future.successful(BadRequest("This action is not allowed"))
      }
  }

  def handleNamedEntityPartiesSubmit(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit request =>
      val optionSelected = request.body.asFormUrlEncoded.get("submit").headOption
      optionSelected match {
        case Some("Add") =>
          namedEntityWithAddressForm.bindFromRequest().fold(
            (formWithErrors: Form[NamedEntityWithAddress]) =>
              Future.successful(BadRequest(views.html.goods_items_named_entity_parties(formWithErrors, List.empty))),
            form =>
              cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).flatMap { res =>
                Logger.info("NamedEntityWithAddress form --->" + form)
                val updatedGoodsItem = if (res.isDefined)
                  res.get.copy(manufacturers = res.get.manufacturers :+ form)
                else GovernmentAgencyGoodsItem(manufacturers = Seq(form))

                cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY, updatedGoodsItem).map { _ =>
                  Ok(views.html.goods_items_named_entity_parties(namedEntityWithAddressForm, updatedGoodsItem.manufacturers))
                }
              })
        case Some("next") => Future.successful(Redirect(routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage()))
        case _ => Logger.error("wrong selection => " + optionSelected.get)
          Future.successful(BadRequest("This action is not allowed"))
      }
  }

  def handlePackagingsSubmit(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit request =>
      val optionSelected = request.body.asFormUrlEncoded.get("submit").headOption
      optionSelected match {
        case Some("Add") =>
          packagingForm.bindFromRequest().fold(
            (formWithErrors: Form[Packaging]) =>
              Future.successful(BadRequest(views.html.goods_items_packagings(formWithErrors, List.empty))),
            form =>
              cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).flatMap { res =>
                Logger.info("NamedEntityWithAddress form --->" + form)
                val updatedGoodsItem = if (res.isDefined)
                  res.get.copy(packagings = res.get.packagings :+ form)
                else GovernmentAgencyGoodsItem(packagings = Seq(form))

                cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY, updatedGoodsItem).map { _ =>
                  Ok(views.html.goods_items_packagings(packagingForm, updatedGoodsItem.packagings))
                }
              })
        case Some("next") => Future.successful(Redirect(routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage()))
        case _ => Logger.error("wrong selection => " + optionSelected.get)
          Future.successful(BadRequest("This action is not allowed"))
      }
  }

  def handlePreviousDcoumentsSubmit(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit request =>
      val optionSelected = request.body.asFormUrlEncoded.get("submit").headOption
      optionSelected match {
        case Some("Add") =>
          previousDocumentForm.bindFromRequest().fold(
            (formWithErrors: Form[PreviousDocument]) =>
              Future.successful(BadRequest(views.html.goods_items_previousdocs(formWithErrors, List.empty))),
            form =>
              cacheService.fetchAndGetEntry[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY).flatMap { res =>
                Logger.info("previousDocumentForm form --->" + form)
                val updatedGoodsItem = if (res.isDefined)
                  res.get.copy(previousDocuments = res.get.previousDocuments :+ form)
                else GovernmentAgencyGoodsItem(previousDocuments = Seq(form))

                cacheService.cache[GovernmentAgencyGoodsItem](request.user.eori.get, GOV_AGENCY_GOODS_ITEM_CACHE_KEY, updatedGoodsItem).map { _ =>
                  Ok(views.html.goods_items_previousdocs(previousDocumentForm, updatedGoodsItem.previousDocuments))
                }
              })
        case Some("next") => Future.successful(Redirect(routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage()))
        case _ => Logger.error("wrong selection => " + optionSelected.get)
          Future.successful(BadRequest("This action is not allowed"))
      }
  }
}
