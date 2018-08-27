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

import config._
import domain.features.Feature
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GenericController @Inject()(actions: Actions, cache: SessionCacheService)
                                 (implicit val messagesApi: MessagesApi, val appConfig: AppConfig, val ec: ExecutionContext) extends FrontendController with I18nSupport {

  private val cacheId: String = "submit-declaration"

  def displayForm(name: String): Action[AnyContent] = (actions.switch(Feature.prototype) andThen actions.auth).async { implicit req =>
    cache.get(cacheId, req.user.eori.get).map { data =>
      Ok(views.html.generic_view(name, data.getOrElse(Map.empty)))
    }
  }

  def handleForm(name: String): Action[AnyContent] = (actions.switch(Feature.prototype) andThen actions.auth).async { implicit req =>
    val data: Map[String, String] = req.body.asFormUrlEncoded.map { form =>
      form.
        map(field => field._1 -> field._2.headOption).
        filter(_._2.isDefined).
        map(field => field._1 -> field._2.get)
    }.getOrElse(Map.empty)
    Logger.info("Data: " + data.mkString("\n"))
    implicit val errors: Map[String, Seq[ValidationError]] = validate(data)
    Logger.info("Errs: " + errors.mkString("\n"))
    errors.isEmpty match {
      case true => {
        cache.get(cacheId, req.user.requiredEori).flatMap { cached =>
          val merged = cached.getOrElse(Map.empty) ++ data
          cache.put(cacheId, req.user.requiredEori, merged).map { _ =>
            Redirect(routes.GenericController.displayForm(data("next-page")))
          }
        }
      }
      case false => {
        Future.successful(BadRequest(views.html.generic_view(name, data)))
      }
    }
  }

  // TODO implement onComplete handler in GenericController
  def onComplete: Action[AnyContent] = (actions.switch(Feature.prototype) andThen actions.auth).async { implicit req =>
    Future.successful(Ok)
  }

  private def validate(data: Map[String, String]): Map[String, Seq[ValidationError]] = data.filter(entry => Fields.definitions.keySet.contains(entry._1)).map { field =>
    val maybeField = Fields.definitions.get(field._1)
    val validators = maybeField.map(_.validators).getOrElse(Seq.empty)
    val results = validators.map(_.validate(field._2))
    val failures = results.filterNot(_.valid)
    val fieldErrors: Seq[ValidationError] = failures.
      map(err => ValidationError(errorMessageKey(Fields.definitions(field._1), err), Fields.definitions(field._1)))
    field._1 -> fieldErrors
  }.filterNot(_._2.isEmpty)

  private def errorMessageKey(field: FieldDefinition, result: ValidationResult): String = messagesApi(Seq(s"${field.labelKey}.${result.defaultErrorKey}", result.defaultErrorKey), result.args:_*)

}

