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
import config.{AppConfig, ErrorHandler}
import domain.References
import domain.auth.{AuthenticatedRequest, EORIRequest, LRNRequest, SignedInUser, _}
import domain.features.Feature.Feature
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.mvc.Results._
import play.api.mvc._
import services.CustomsCacheService
import services.cachekeys.CacheKey
import uk.gov.hmrc.wco.dec.GovernmentAgencyGoodsItem

import scala.concurrent.{ExecutionContext, Future}

class FakeActions(signedInUser: Option[SignedInUser], item: Option[GovernmentAgencyGoodsItem] = None, localReferenceNumber: Option[String] = None)
                 (implicit messages: MessagesApi, appConfig: AppConfig, ec: ExecutionContext)
  extends Actions with MockitoSugar {

  private val mockCustomsCache = mock[CustomsCacheService]

  when(mockCustomsCache.getByKey(any(), eqTo(CacheKey.references))(any(), any(), any()))
    .thenReturn(Future.successful(localReferenceNumber.map(References(None, None, None, _, None))))

  when(mockCustomsCache.getByKey(any(), eqTo(CacheKey.goodsItem))(any(), any(), any()))
    .thenReturn(Future.successful(item))

  override def auth: ActionBuilder[AuthenticatedRequest] with ActionRefiner[Request, AuthenticatedRequest] =
    new ActionBuilder[AuthenticatedRequest] with ActionRefiner[Request, AuthenticatedRequest] {
      override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] =
        Future.successful(signedInUser.map(AuthenticatedRequest(request, _)).toRight(Unauthorized("")))
    }

  override def eori: ActionRefiner[AuthenticatedRequest, EORIRequest] =
    new EORIAction(new ErrorHandler())

  override def lrn: ActionRefiner[EORIRequest, LRNRequest] =
    new LRNAction(new ErrorHandler(), mockCustomsCache)

  override def goodsItem: ActionRefiner[EORIRequest, GoodsItemRequest] =
    new GoodsItemAction(new ErrorHandler(), mockCustomsCache)

  override def switch(feature: Feature): ActionBuilder[Request] with ActionFilter[Request] = ???
}