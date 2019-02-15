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

package test.controllers

import config.AppConfig
import controllers.Actions
import domain.auth.SignedInUser
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.Action
import services.CustomsDeclarationsConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.MetaData

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestingUtilitiesController @Inject()(actions: Actions, connector: CustomsDeclarationsConnector)
                                          (implicit val appConfig: AppConfig, ec: ExecutionContext) extends FrontendController {

  def submitDeclarationXml: Action[String] = actions.auth.async(parse.tolerantText) { implicit authenticatedRequest =>
    implicit val user: SignedInUser = authenticatedRequest.user

    val maybeLrn = MetaData.fromXml(authenticatedRequest.body).declaration.flatMap(_.functionalReferenceId)

    maybeLrn.fold(Future.successful(BadRequest("Local Reference Number is required in metadata"))){ lrn =>
        connector.submitImportDeclaration(MetaData.fromXml(authenticatedRequest.body), lrn).map { resp =>
          Created(resp.conversationId)
        }recover{
          case e: Throwable =>
            Logger.error("Error calling backend", e)
            InternalServerError("Error calling backend")
        }
    }
  }

}

case class SubmissionWrapper(conversationId: String, lrn: Option[String] = None, mrn: Option[String] = None)

object SubmissionWrapper {

  implicit val format: OFormat[SubmissionWrapper] = Json.format[SubmissionWrapper]

}
