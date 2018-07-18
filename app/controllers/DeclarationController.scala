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
import domain.declaration.{Declaration, MetaData}
import domain.features.Feature
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsDeclarationsConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationController @Inject()(actions: Actions, client: CustomsDeclarationsConnector, val messagesApi: MessagesApi)(implicit val appConfig: AppConfig, ec: ExecutionContext) extends FrontendController with I18nSupport {

  val declarationForm: Form[DeclarationForm] = Form(
    mapping(
      "wcoDataModelVersionCode" -> optional(text),
      "wcoTypeName" -> optional(text),
      "responsibleCountryCode" -> optional(text),
      "responsibleAgencyName" -> optional(text),
      "agencyAssignedCustomizationCode" -> optional(text),
      "agencyAssignedCustomizationVersionCode" -> optional(text)
    )(DeclarationForm.apply)(DeclarationForm.unapply)
  )

  def showDeclarationForm: Action[AnyContent] = (actions.switch(Feature.declaration) andThen actions.auth).async { implicit req =>
    Future.successful(Ok(views.html.declaration_form(declarationForm)))
  }

  def handleDeclarationForm: Action[AnyContent] = (actions.switch(Feature.declaration) andThen actions.auth).async { implicit req =>
    val bound = declarationForm.bindFromRequest()
    bound.fold(
      errors => Future.successful(BadRequest(views.html.declaration_form(errors))),
      success => {
        client.submitImportDeclaration(success.toMetaData).map { b =>
          Ok(views.html.declaration_acknowledgement(b))
        }
      }
    )
  }

}

// At present, our form mirrors the declaration XML exactly. Later this may change. Therefore, it is probably useful
// to retain a distinction between view model class and XML model class and map the former to the latter
case class DeclarationForm(wcoDataModelVersionCode: Option[String] = None,
                           wcoTypeName: Option[String] = None,
                           responsibleCountryCode: Option[String] = None,
                           responsibleAgencyName: Option[String] = None,
                           agencyAssignedCustomizationCode: Option[String] = None,
                           agencyAssignedCustomizationVersionCode: Option[String] = None) {

  def toMetaData: MetaData = MetaData(
    Declaration(),
    wcoDataModelVersionCode,
    wcoTypeName,
    responsibleCountryCode,
    responsibleAgencyName,
    agencyAssignedCustomizationCode,
    agencyAssignedCustomizationVersionCode
  )

}
