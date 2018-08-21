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
import controllers.ViewUtils._


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

trait DeclarationValidator extends Constraints{

  def validatePayload(payload: Map[String, Seq[String]]) = {
    val filteredPayload = payload.filter(element => validations.get(element._1).isDefined)
    val results = for (element <- filteredPayload) yield {
      element._1 -> validations.get(element._1).get.apply(element._2.headOption.getOrElse(""))
    }
    results.collect{case (key, Some(value)) => key -> value}
  }

  val refValidations : Map[String, (String) => Option[ValidationError]] =
    Map(referenceNumberUCR1 -> optionalText35MaxConstraint,
      declarantFunctionalReferenceID -> lrnConstraint)


  val declarantDetailsValidations: Map[String, (String) => Option[ValidationError]] =
    Map(declarantName -> optionalText70MaxConstraint,
      declarantAddressLine -> optionalText70MaxConstraint,
      declarantAddressCityName -> optionalText35MaxConstraint,
      declarantAddressCountryCode -> countryConstraint,
      declarantAddressPostcode -> postcodeConstraint,
      declarantEori -> eoriConstraint)

  val exporterDetailsValidations: Map[String, (String) => Option[ValidationError]] =
    Map(exporterName -> optionalText70MaxConstraint,
      exporterAddressLine -> optionalText70MaxConstraint,
      exporterAddressCityName -> optionalText35MaxConstraint,
      exporterAddressCountryCode -> countryConstraint,
      exporterAddressPostcode -> postcodeConstraint,
      exporterEori -> optionalEoriConstraint
    )

  val representativeDetailsValidations: Map[String, (String) => Option[ValidationError]] =
    Map(agentName -> optionalText70MaxConstraint,
      agentAddressLine -> optionalText70MaxConstraint,
      agentAddressCityName -> optionalText35MaxConstraint,
      agentAddressCountryCode -> countryConstraint,
      agentAddressPostcode -> postcodeConstraint,
      agentEori -> optionalEoriConstraint
    )

  val importerDetailsValidations: Map[String, (String) => Option[ValidationError]] =
    Map(importerName -> optionalText70MaxConstraint,
      importerAddressLine -> optionalText70MaxConstraint,
      importerAddressCityName -> optionalText35MaxConstraint,
      importerAddressCountryCode -> countryConstraint,
      importerAddressPostcode -> postcodeConstraint,
      importerEori -> optionalEoriConstraint
    )

  val sellerDetailsValidations: Map[String, (String) => Option[ValidationError]] =
    Map(sellerName -> optionalText70MaxConstraint,
      sellerAddressLine -> optionalText70MaxConstraint,
      sellerAddressCityName -> optionalText35MaxConstraint,
      sellerAddressCountryCode -> countryConstraint,
      sellerAddressPostcode -> postcodeConstraint,
      sellerCommunicationID -> optionalText50MaxConstraint,
      sellerEori -> optionalEoriConstraint
    )

  val buyerDetailsValidations: Map[String, (String) => Option[ValidationError]] =
    Map(buyerName -> optionalText70MaxConstraint,
      buyerAddressLine -> optionalText70MaxConstraint,
      buyerAddressCityName -> optionalText35MaxConstraint,
      buyerAddressCountryCode -> countryConstraint,
      buyerAddressPostcode -> postcodeConstraint,
      buyerCommunicationID -> optionalText50MaxConstraint,
      buyerEori -> optionalEoriConstraint
    )

  val additionalSupplyChainActorsValidations: Map[String, (String) => Option[ValidationError]] =
    Map(aeoMutualRecognitionPartiesID -> optionalEoriConstraint,
      aeoMutualRecognitionPartyRoleCode -> optionalText70MaxConstraint,
      authorisationHolderID -> optionalEoriConstraint,
      authorisationHolderCategoryCode -> optionalText70MaxConstraint
    )

  val validations : Map[String, (String) => Option[ValidationError]] =
    declarantDetailsValidations ++ refValidations ++ exporterDetailsValidations ++ representativeDetailsValidations ++ importerDetailsValidations ++ sellerDetailsValidations ++ buyerDetailsValidations ++ additionalSupplyChainActorsValidations ++ additionalSupplyChainActorsValidations

}

trait Constraints {

  val requiredKey = "input.required"

  private def lettersDigitPattern(input:String,min:Int=1,max:Int=35) =
    if (input.isEmpty) None else validator(input, s"""^[a-zA-Z0-9 ]{$min,$max}$$""", requiredKey)


  def optionalText35MaxConstraint(input:String) = lettersDigitPattern(input)
  def optionalText50MaxConstraint(input:String) = lettersDigitPattern(input=input,max=50)
  def optionalText70MaxConstraint(input:String) = lettersDigitPattern(input=input,max=70)

  def countryConstraint(input:String) = if (input.isEmpty) None else validator(input,s"""^[A-Z]{2}""",requiredKey)
  def postcodeConstraint(input:String) = lettersDigitPattern(input=input,max=9)
  def textInputConstraint(input:String) = validator(input,s""""^[a-zA-Z0-9 ]""",requiredKey)
  def eoriConstraint(input:String) = validator(input,s"""^[a-zA-Z0-9 ]{17}""",requiredKey)
  def lrnConstraint(input:String) = validator(input,s"""^[a-zA-Z0-9 ]{1,22}""",requiredKey)
  def optionalEoriConstraint(input:String) = if (input.isEmpty) None else validator(input,s"""^[a-zA-Z0-9]{17}""",requiredKey)


  def validator = (text: String, regex:String, errMsgKey:String) => {
    Logger.debug(s"Validation information :-> input  = ${text}, regex = ${regex}, errMsg = ${errMsgKey} " )
    if(text.matches(regex)) None
    else Some(ValidationError(errMsgKey))
  }

}

