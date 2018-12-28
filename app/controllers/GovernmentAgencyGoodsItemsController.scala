/*
 * Copyright 2018 HM Revenue & Customs
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
import domain.GovernmentAgencyGoodsItem
import domain.GovernmentAgencyGoodsItem._
import domain.features.Feature
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.wco.dec.{AdditionalInformation, GovernmentAgencyGoodsItemAdditionalDocument}

import scala.concurrent.Future


@Singleton
class GovernmentAgencyGoodsItemsController @Inject()(actions: Actions, cache: CustomsCacheService)
  (implicit val appConfig: AppConfig, val messagesApi: MessagesApi) extends CustomsController {

  val additionalDocumentform: Form[GovernmentAgencyGoodsItemAdditionalDocument] = Form(govtAgencyGoodsItemAddDocMapping)
  val additionalInformationform: Form[AdditionalInformation] = Form(additionalInformationMapping)


  def showGoodsItems(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cache.getGoodsItems(req.user.eori.get).flatMap(listItems =>
        cache.putGoodsItem(req.user.eori.get).map(_ =>
          Ok(views.html.gov_agency_goods_items_list(listItems.getOrElse(List.empty)))))
  }

  def submitGoodsItems(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      val optionSelected = req.body.asFormUrlEncoded.get("submit").headOption
      optionSelected match {
        case Some("AddGoodsItem") =>
          cache.getGoodsItems(req.user.eori.get).flatMap(listItems =>
            cache.putGoodsItem(req.user.eori.get).map(_ =>
              Ok(views.html.gov_agency_goods_items_list(listItems.getOrElse(List.empty)))))

        case Some("next") => Future.successful(Redirect(routes.DeclarationController.displaySubmitForm("check-your-answers")))
        case _ => Logger.error("wrong selection => " + optionSelected.get);
          Future.successful(BadRequest("This action is not allowed"))
      }

  }

  def showGoodsItemPage(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cache.getAGoodsItem(req.user.eori.get).map(res =>
        Ok(views.html.gov_agency_goods_items(res)))
  }

  def saveGoodsItem() = (actions.switch(Feature.submit) andThen actions.auth).async { implicit request =>
    val optionSelected = request.body.asFormUrlEncoded.get("add").headOption
    optionSelected match {
      case Some("AddGovernmentAgencyGoodsItemAdditionalDocument") => Future.successful(
        Redirect(controllers.routes.GovernmentAgencyGoodsItemsController.showGovAgencyGoodsItemsAdditionalDocuments()))
      case Some("AddAdditionalInformation") => Future.successful(
        Redirect(controllers.routes.GovernmentAgencyGoodsItemsController.showGoodsItemsAdditionalInformations()))

      case Some("AddRoleBasedParty") => Future.successful(Ok("Clicked AddRoleBasedParty"))
      case Some("AddGovernmentProcedures") => Future.successful(Ok("Clicked AddGovernmentProcedures"))
      case Some("AddOrigins") => Future.successful(Ok("Clicked AddOrigins"))
      case Some("AddPackagings") => Future.successful(Ok("Clicked Add Packagings"))
      case Some("AddPreviousDocuments") => Future.successful(Ok("Clicked AddPreviousDocuments"))
      case Some("AddRefundRecipientParties") => Future.successful(Ok("Clicked AddRefundRecipientParties"))
      case Some("SaveGoodsItem") =>
        cache.saveGoodsItem(request.user.eori.get).map { _ => Redirect(controllers.routes.GovernmentAgencyGoodsItemsController.showGoodsItems()) }

      case _ => Logger.error("wrong selection => " + optionSelected.get);
        Future.successful(BadRequest("This action is not allowed"))
    }
  }

  def showGoodsItemsAdditionalInformations(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cache.getAGoodsItem(req.user.eori.get).map { res =>
        val docs: List[AdditionalInformation] = if (res.isDefined) {
          res.get.additionalInformations.toList
        } else List.empty
        Ok(views.html.goods_items_add_additional_informations(additionalInformationform, docs))
      }
  }

  def handleGoodsItemsAdditionalInformationsSubmit() = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit request =>
      additionalInformationform.bindFromRequest().fold(
        (formWithErrors: Form[AdditionalInformation]) =>
          Future.successful(BadRequest(views.html.goods_items_add_additional_informations(formWithErrors, List.empty))),
        form =>
          cache.getAGoodsItem(request.user.eori.get).flatMap { res =>
            Logger.info("additionalInformationform form --->" + form)
            val updatedGoodsItem = if (res.isDefined)
              res.get.copy(additionalInformations = res.get.additionalInformations :+ form)
            else GovernmentAgencyGoodsItem(additionalInformations = Seq(form))

            cache.putGoodsItem(request.user.eori.get, updatedGoodsItem).map { _ =>
              Redirect(controllers.routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage())
            }
          }
      )
  }

  def handleGovAgencyGoodsItemsAdditionalDocumentsSubmit() = (actions.switch(Feature.submit) andThen actions.auth).async { implicit request =>
    additionalDocumentform.bindFromRequest().fold(
      (formWithErrors: Form[GovernmentAgencyGoodsItemAdditionalDocument]) =>
        Future.successful(BadRequest(views.html.gov_agency_goods_items_add_docs(formWithErrors, List.empty))),
      form =>
        cache.getAGoodsItem(request.user.eori.get).flatMap { res =>
          Logger.info("additionalDocumentform  --->" + form)
          val updatedGovernmentAgencyGoodsItem = if (res.isDefined)
            res.get.copy(additionalDocuments = res.get.additionalDocuments :+ form)
          else GovernmentAgencyGoodsItem(additionalDocuments = Seq(form))

          cache.putGoodsItem(request.user.eori.get, updatedGovernmentAgencyGoodsItem).map { _ =>
            Redirect(controllers.routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage())
          }
        }
    )
  }

  def showGovAgencyGoodsItemsAdditionalDocuments(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      cache.getAGoodsItem(req.user.eori.get).map { res =>
        val docs: List[GovernmentAgencyGoodsItemAdditionalDocument] = if (res.isDefined) {
          res.get.additionalDocuments.toList
        } else List.empty
        Ok(views.html.gov_agency_goods_items_add_docs(additionalDocumentform, docs))
      }
  }

}
