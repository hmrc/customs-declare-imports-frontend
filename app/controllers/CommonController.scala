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
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{AnyContent, Action}
import services.CustomsDeclarationsConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import play.api.data.Forms._

import scala.concurrent.{Future, ExecutionContext}


@Singleton
class CommonController @Inject()(actions: Actions, client: CustomsDeclarationsConnector,val messagesApi: MessagesApi)
(implicit val appConfig: AppConfig, ec: ExecutionContext) extends FrontendController with I18nSupport{

  val commonFormMappings =
    mapping("title"-> text,
    "fields" -> list[Field](mapping( "label"-> text,
    "name" -> text,
                                    "fieldType" -> text,
                                    "fieldValue" -> optional(text),
                                    "inputClass" -> text,
                                    "divClass" -> text,
                                    "id" -> text)(Field.apply)(Field.unapply)),
    "formName" -> text,
    "url" -> text,
    "formAction" -> text,
    "id" -> text,
    "nextViewId" -> text)(CommonForm.apply)(CommonForm.unapply)

  val commonForm:Form[CommonForm] = Form(commonFormMappings)

  def showForm: Action[AnyContent] = Action.async { implicit req =>
    val defaultForm:Form[CommonForm] = commonForm.fill(ViewFormMappings.listings.get("1").get)
    Future.successful(Ok(views.html.common_view(defaultForm)))
  }

  def submitForm: Action[AnyContent] = Action.async { implicit req =>
    val form = commonForm.bindFromRequest()
    Logger.debug("<<<<<<<<<<Form errors >>>>>>>" + form.errors.mkString("++"))
    form.fold (
      errorsWithErrors => Future.successful(BadRequest(views.html.common_view(errorsWithErrors))),
      success => { Logger.debug("successful form navigation  Fields are --> " + success.fields.mkString("> <"))
        Future.successful(Ok(views.html.common_view(commonForm.fill(ViewFormMappings.listings.get(form.get.nextViewId).get))))
      }
    )
  }
}

object ViewFormMappings {

  val commonMetadataFormFields = List(Field("WCO Data Model Version Code","wcoDataModelVersionCode","input",None,id = "1"),
    Field("wco Type Name","wcoTypeName","input",Some("value2"),id = "2"),
    Field("Responsible Country Code","responsibleCountryCode","textarea",None,id = "3"),
    Field("Responsible Agency Name","responsibleAgencyName","textarea",None,id = "4"),
    Field("Agency Assigned Version Code","agencyAssignedCustomizationVersionCode","input",Some("value3"),id = "5"))

  val submitterFormFields = List(Field("Submitter Name","Name","input",None,id = "1"),
    Field("Submitter ID","Id","input",None,id = "2"))

  val CommonMetadataForm = CommonForm("Declaration Details",commonMetadataFormFields ,  "CommonMetadataForm",  "url1", "formAction1","1","2")

  val CommonSubmitterForm = CommonForm("Submitter Details",submitterFormFields ,  "CommonSubmitterForm",  "url2", "formAction2","2","3")

  val listings = Map(CommonMetadataForm.id-> CommonMetadataForm,
  CommonSubmitterForm.id -> CommonSubmitterForm)
}

case class CommonForm(title:String, fields:List[Field], formName:String, url:String, formAction:String, id:String, nextViewId:String)


case class Field(
                label:String,
                  name:String,
                 fieldType:String,
                 fieldValue:Option[String],
                 inputClass:String ="form-control",
                 divClass:String= "form-field",
                 id:String)

case class CommonMetadataForm(wcoDataModelVersionCode: String,
          wcoTypeName: String,
          responsibleCountryCode: String,
          responsibleAgencyName: String,
          agencyAssignedCustomizationVersionCode: String,
          declaration: CancelDeclarationForm)

case class CommonSubmitterForm(name: Option[String] = None,id: String)
