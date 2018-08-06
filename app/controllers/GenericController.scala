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

import javax.inject.{Singleton, Inject}

import config.AppConfig
import domain.features.Feature
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{AnyContent, Action}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{Future, ExecutionContext}


@Singleton
class GenericController @Inject()(actions: Actions, cache: SessionCacheService)(implicit val messagesApi: MessagesApi,
                    val appConfig: AppConfig, val ec: ExecutionContext) extends FrontendController with I18nSupport with DeclarationValidator{

    val cacheId:String  = "submit-declaration"
  def displayForm(name: String): Action[AnyContent] = (actions.switch(Feature.declaration) andThen actions.auth).async { implicit req =>
    cache.get(cacheId,cacheId).map { data =>
      Ok(views.html.generic_view(name, data.getOrElse(Map())))
    }
  }

  def handleForm(current: String, next: String): Action[AnyContent] = (actions.switch(Feature.declaration) andThen actions.auth).async { implicit req =>
      val payload = req.body.asFormUrlEncoded.get
    val errors = validatePayload(payload)

    errors.size match {
      case 0 => cache.get(cacheId,cacheId).flatMap { cachedData =>
        val allData = cachedData.getOrElse(payload) ++ payload
        cache.put(cacheId, cacheId, (allData)).map(res => Redirect(routes.GenericController.displayForm(next)))
      }
      case _ => Logger.debug("validation errors are --> " + errors.mkString("} {") )
        Future.successful(BadRequest(views.html.generic_view(current,payload,errors)))
    }
  }

}

trait DeclarationValidator  {

  val requiredKey = "input.required"

  def validatePayload(payload: Map[String, Seq[String]]) = {
    val filteredPayload = payload.filter(element => validations.get(element._1).isDefined)
    val results = for (element <- filteredPayload) yield {
      element._1 -> validations.get(element._1).get.apply(element._2.headOption.getOrElse(""))
    }
    results.collect{case (key, Some(value)) => key -> value}
  }

  val refValidations : Map[String, (String) => Option[ValidationError]] =
    Map("ucrTraderAssignedReferenceId" -> digitConstraint,
      "declarationFunctionalReferenceId" -> digitConstraint)


  val declarantDetailsValidations: Map[String, (String) => Option[ValidationError]] =
    Map("DeclarantName" -> textInputConstraint,
      "DeclarantAddressLine" -> textInputConstraint,
      "DeclarantAddressCityName" -> textInputConstraint)

  val validations : Map[String, (String) => Option[ValidationError]] =
    declarantDetailsValidations ++ refValidations

  val lettersDigitPattern = """^(?!\s*$).+"""
  val onlyDigitsPattern = """^(?!\s*$).+"""
  def textInputConstraint(input:String) = validator(input,lettersDigitPattern,requiredKey)
  def digitConstraint(input:String) = validator(input,onlyDigitsPattern,requiredKey)

  def validator = (text: String, regex:String, errMsgKey:String) => {
    Logger.debug(s"Validation information :-> input  = ${text}, regex = ${regex}, errMsg = ${errMsgKey} " )
    if(text.matches(regex)) None
    else Some(ValidationError(errMsgKey))
  }
}

