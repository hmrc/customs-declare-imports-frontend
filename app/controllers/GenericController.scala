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

    cache.get(req.user.eori.get,cacheId).map { data =>
      Ok(views.html.generic_view(name, data.getOrElse(Map.empty)))
    }
  }

  def handleForm(current: String, next: String): Action[AnyContent] = (actions.switch(Feature.declaration) andThen actions.auth).async { implicit req =>
    val payload = req.body.asFormUrlEncoded.get
    implicit val errors = validatePayload(payload)
    errors.size match {
      case 0 => cache.get(req.user.eori.get,cacheId).flatMap { cachedData =>
        val allData = cachedData.getOrElse(payload) ++ payload
        cache.put(req.user.eori.get, cacheId, (allData)).map(res => Redirect(routes.GenericController.displayForm(next)))
      }
      case _ => Logger.debug("validation errors are --> { " + errors.mkString("} {") )
        Future.successful(BadRequest(views.html.generic_view(current,payload)))
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
    Map("ucrTraderAssignedReferenceId" -> optionalText70MaxConstraint,
      "declarationFunctionalReferenceId" -> optionalText70MaxConstraint)


  val declarantDetailsValidations: Map[String, (String) => Option[ValidationError]] =
    Map("MetaData_declaration_declarant_name" -> optionalText70MaxConstraint,
      "MetaData_declaration_declarant_address_line" -> optionalText70MaxConstraint,
      "MetaData_declaration_declarant_address_cityName" -> optionalText35MaxConstraint,
      "MetaData_declaration_declarant_address_countryCode" -> countryConstraint,
      "MetaData_declaration_declarant_address_postcodeId" -> postcodeConstraint,
      "MetaData_declaration_declarant_id" -> eoriConstraint)

  val validations : Map[String, (String) => Option[ValidationError]] =
    declarantDetailsValidations ++ refValidations

  private def lettersDigitPattern(input:String,min:Int=1,max:Int=35) =
    if (input.isEmpty) None else validator(input, s"""^[a-zA-Z0-9]{$min,$max}$$""", requiredKey)


  def optionalText35MaxConstraint(input:String) = lettersDigitPattern(input)
  def optionalText70MaxConstraint(input:String) = lettersDigitPattern(input=input,max=70)

  def countryConstraint(input:String) = validator(input,s"""^[A-Z]{2}""",requiredKey)
  def postcodeConstraint(input:String) = lettersDigitPattern(input=input,max=9)
  def textInputConstraint(input:String) = validator(input,s""""^[a-zA-Z0-9]""",requiredKey)
  def eoriConstraint(input:String) = validator(input,s"""^[a-zA-Z0-9]{17}""",requiredKey)


  def validator = (text: String, regex:String, errMsgKey:String) => {
    Logger.debug(s"Validation information :-> input  = ${text}, regex = ${regex}, errMsg = ${errMsgKey} " )
    if(text.matches(regex)) None
    else Some(ValidationError(errMsgKey))
  }
}

