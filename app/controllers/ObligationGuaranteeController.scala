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
import forms.DeclarationFormMapping._
import forms.ObligationGuaranteeForm
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.wco.dec.ObligationGuarantee

import scala.concurrent.Future

@Singleton
class ObligationGuaranteeController @Inject()(actions: Actions, cache: CustomsCacheService)
  (implicit val appConfig: AppConfig, val messagesApi: MessagesApi) extends CustomsController {

  val obligationGuaranteesForm: Form[ObligationGuarantee] = Form(obligationGauranteeMapping)

  val guaranteeTypeFormKey = "ObligationGuarantees"

  def display(): Action[AnyContent] = actions.auth.async {
    implicit req =>
      cache.fetchAndGetEntry[ObligationGuaranteeForm](req.user.eori.get, guaranteeTypeFormKey).map {
        case Some(cachedGuarantees) =>
          Ok(views.html.obligation_guarantee(obligationGuaranteesForm, cachedGuarantees.guarantees))
        case _ => Ok(views.html.obligation_guarantee(obligationGuaranteesForm, Seq.empty))
      }
  }

  def submit(): Action[AnyContent] = (actions.switch(Feature.submit) andThen actions.auth).async {
    implicit req =>
      val optionSelected = req.body.asFormUrlEncoded.get("submit").headOption
      optionSelected match {
        case Some("Add") =>
          obligationGuaranteesForm.bindFromRequest().fold(
            (formWithErrors: Form[ObligationGuarantee]) =>
              Future.successful(BadRequest((views.html.obligation_guarantee(formWithErrors, Seq.empty)))),
            form => {
              if (form.accessCode.isDefined || form.id.isDefined || form.amount.isDefined || form.referenceId.isDefined ||
                form.securityDetailsCode.isDefined) {
                cache.fetchAndGetEntry[ObligationGuaranteeForm](req.user.eori.get, guaranteeTypeFormKey).flatMap {
                  case Some(cached) =>
                    val updatedGuarantees = cached.copy(cached.guarantees :+ form)
                    cache.cache[ObligationGuaranteeForm](req.user.eori.get, guaranteeTypeFormKey, updatedGuarantees).map { _ =>
                      Ok(views.html.obligation_guarantee(obligationGuaranteesForm, updatedGuarantees.guarantees))
                    }
                  case _ =>
                    cache.cache[ObligationGuaranteeForm](req.user.eori.get, guaranteeTypeFormKey, ObligationGuaranteeForm(Seq(form))).map { _ =>
                      Ok(views.html.obligation_guarantee(obligationGuaranteesForm, Seq[ObligationGuarantee](form)))
                    }
                }
              }
              else
                Future.successful(Ok(views.html.obligation_guarantee(obligationGuaranteesForm, Seq.empty)))
            })
        case Some("next") => Future.successful(Redirect(goodsitems.routes.GoodsItemsListController.onPageLoad()))
        case _ => Logger.error("wrong selection => " + optionSelected.get)
          Future.successful(BadRequest("This action is not allowed"))

      }
  }

}
