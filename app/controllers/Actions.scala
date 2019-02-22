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

import com.google.inject.ImplementedBy
import config.{AppConfig, ErrorHandler}
import domain.auth._
import domain.DeclarationFormats._
import domain.features.Feature.Feature
import domain.features.FeatureStatus
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Reads
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{Configuration, Environment}
import services.CustomsCacheService
import services.cachekeys.CacheKey
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.wco.dec.GovernmentAgencyGoodsItem

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ActionsImpl])
trait Actions {

  def auth: ActionBuilder[AuthenticatedRequest] with ActionRefiner[Request, AuthenticatedRequest]

  def eori: ActionRefiner[AuthenticatedRequest, EORIRequest]

  def goodsItem: ActionRefiner[EORIRequest, GoodsItemRequest]

  def switch(feature: Feature): ActionBuilder[Request] with ActionFilter[Request]
}

@Singleton
class ActionsImpl @Inject()(authConnector: AuthConnector, errorHandler: ErrorHandler, cache: CustomsCacheService)
                           (implicit val appConfig: AppConfig, ec: ExecutionContext) extends Actions {

  def auth: ActionBuilder[AuthenticatedRequest] with ActionRefiner[Request, AuthenticatedRequest] = new AuthAction(authConnector)

  def eori: ActionRefiner[AuthenticatedRequest, EORIRequest] = new EORIAction(errorHandler)

  def goodsItem: ActionRefiner[EORIRequest, GoodsItemRequest] = new GoodsItemAction(errorHandler, cache)

  def switch(feature: Feature): ActionBuilder[Request] with ActionFilter[Request] = new ActionBuilder[Request] with ActionFilter[Request] {

    def filter[A](input: Request[A]): Future[Option[Result]] = Future.successful(
      appConfig.featureStatus(feature) match {
        case FeatureStatus.enabled => None
        case FeatureStatus.disabled => Some(NotFound(errorHandler.notFoundTemplate(input)))
        case FeatureStatus.suspended => Some(ServiceUnavailable(errorHandler.notFoundTemplate(input)))
      }
    )
  }
}

class AuthAction(auth: AuthConnector)(implicit val appConfig: AppConfig, ec: ExecutionContext)
  extends ActionBuilder[AuthenticatedRequest]
    with ActionRefiner[Request, AuthenticatedRequest]
    with AuthorisedFunctions
    with AuthRedirects {

  override def authConnector: AuthConnector = auth

  override def config: Configuration = appConfig.runModeConfiguration

  override def env: Environment = appConfig.environment

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    authorised(SignedInUser.authorisationPredicate)
      .retrieve(credentials and name and email and affinityGroup and internalId and allEnrolments) {
        case Some(credentials) ~ Some(name) ~ email ~ affinityGroup ~ internalId ~ allEnrolments => Future.successful(Right(AuthenticatedRequest(
          request, SignedInUser(credentials, name, email, affinityGroup, internalId, allEnrolments)
        )))
      }
  }

}

class EORIAction(errorHandler: ErrorHandler) extends ActionRefiner[AuthenticatedRequest, EORIRequest] {

  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, EORIRequest[A]]] =
    Future.successful(
      request.user.eori
        .map(e => EORIRequest(request, EORI(e)))
        .toRight(Unauthorized(errorHandler.notFoundTemplate(request))))
}

class GoodsItemAction(errorHandler: ErrorHandler, cache: CustomsCacheService)
                     (implicit ec: ExecutionContext) extends ActionRefiner[EORIRequest, GoodsItemRequest] {

  override protected def refine[A](request: EORIRequest[A]): Future[Either[Result, GoodsItemRequest[A]]] = {
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, Some(request.session), Some(request))
    cache.getByKey(request.eori, CacheKey.goodsItem).map { goodsItem =>
      goodsItem match {
        case Some(good) => Right(GoodsItemRequest(request, good))
        case None => Left(BadRequest(errorHandler.badRequestTemplate(request)))
      }
    }
  }
}
