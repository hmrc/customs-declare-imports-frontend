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

package uk.gov.hmrc.customs.test.behaviours

import akka.stream.Materializer
import config.AppConfig
import domain.auth.EORI
import models.{Declaration, DeclarationAction, DeclarationActionType, DeclarationNotification}
import org.joda.time.DateTime
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.concurrent.Execution.Implicits
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.test.FakeRequest
import services.{CustomsCacheService, CustomsDeclarationsConnector, CustomsDeclarationsResponse}
import services.cachekeys.CacheKey
import uk.gov.hmrc.customs.test.utils.FormHelpers
import uk.gov.hmrc.customs.test.{CustomsFixtures, CustomsFutures}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect._

trait CustomsSpec extends PlaySpec
  with OneAppPerSuite
  with CustomsFutures
  with CustomsFixtures
  with MockitoSugar
  with BeforeAndAfterEach
  with FormHelpers {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val mat: Materializer = app.materializer
  implicit lazy val ec: ExecutionContext = Implicits.defaultContext
  implicit lazy val appConfig: AppConfig = component[AppConfig]
  implicit lazy val messagesApi: MessagesApi = component[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(fakeRequest)

  lazy val fakeRequest = FakeRequest("", "")

  override lazy val app: Application = customise(GuiceApplicationBuilder()).build()

  protected def component[T: ClassTag]: T = app.injector.instanceOf[T]

  val mockCustomsCacheService: CustomsCacheService = mock[CustomsCacheService]
  val mockCustomsDeclarationsConnector: CustomsDeclarationsConnector = mock[CustomsDeclarationsConnector]

  def withCaching[T](form: Option[T]): OngoingStubbing[Future[CacheMap]] = {
    when(mockCustomsCacheService.get(any(), any())(any(), any()))
      .thenReturn(Future.successful(None))
    when(mockCustomsCacheService.fetchAndGetEntry[T](any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(form))
    when(mockCustomsCacheService.getByKey[T](any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(form))
    when(mockCustomsCacheService.upsert(any(), any())(any(), any())(any(), any(), any(), any()))
      .thenReturn(Future.successful(()))
    when(mockCustomsCacheService.insert(any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(()))

    when(mockCustomsCacheService.put(any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(CacheMap("id1", Map.empty)))
    when(mockCustomsCacheService.cache[T](any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(CacheMap("id1", Map.empty)))
  }

  def withImportsBackend[T](): OngoingStubbing[Future[Seq[Declaration]]] = {
    reset(mockCustomsDeclarationsConnector)

    val decSeq = Seq(Declaration(DateTime.now, Some("LocalReferenceNumber"), Some("Mrn"),
      Seq(DeclarationAction(DateTime.now(), DeclarationActionType.SUBMISSION, Seq(DeclarationNotification(11, "conversationId", DateTime.now()))))))

    when(mockCustomsDeclarationsConnector.cancelDeclaration(any())(any(), any())).thenReturn(Future.successful(CustomsDeclarationsResponse("conversationId")))
    when(mockCustomsDeclarationsConnector.getDeclarations(any(), any())).thenReturn(Future.successful(decSeq))
  }

  def withCachingUsingKey[T](dataToReturn: Option[T], id: String): OngoingStubbing[Future[CacheMap]]  = {
    when(
      mockCustomsCacheService
        .fetchAndGetEntry[T](ArgumentMatchers.eq(appConfig.appName), ArgumentMatchers.eq(id))(any(), any(), any())
    ).thenReturn(Future.successful(dataToReturn))

    when(mockCustomsCacheService.cache[T](any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(CacheMap(id, Map.empty)))
  }

  def withCleanCache[T](eori: EORI, key: CacheKey[T], data: Option[T])(test: => Unit): Unit = {
    reset(mockCustomsCacheService)

    when(mockCustomsCacheService.getByKey(eqTo(eori), eqTo(key))(any(), any(), any()))
      .thenReturn(Future.successful(data))

    when(mockCustomsCacheService.insert(eqTo(eori), eqTo(key), any())(any(), any(), any()))
      .thenReturn(Future.successful(()))

    test
  }

  def withCacheMap(eori: EORI, cacheMap: Option[CacheMap])(test: => Unit): Unit = {
    reset(mockCustomsCacheService)

    when(mockCustomsCacheService.fetch(eqTo(eori.value))(any(), any()))
      .thenReturn(Future.successful(cacheMap))

    test
  }

  // composite template method to be overridden by sub-types to customise the app
  // NB. when overriding, ALWAYS call super.customise(builder) and operate on the return value!
  protected def customise(builder: GuiceApplicationBuilder): GuiceApplicationBuilder = builder.overrides(
    bind[CustomsCacheService].to(mockCustomsCacheService),
    bind[CustomsDeclarationsConnector].to(mockCustomsDeclarationsConnector))

  override def beforeEach = {
    super.beforeEach()

    when(mockCustomsCacheService.getByKey(any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(None))

    when(mockCustomsCacheService.upsert(any(), any())(any(), any())(any(), any(), any(), any()))
      .thenReturn(Future.successful(()))

    when(mockCustomsCacheService.insert(any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(()))
  }
}
