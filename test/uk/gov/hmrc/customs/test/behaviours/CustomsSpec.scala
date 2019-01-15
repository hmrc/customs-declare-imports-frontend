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
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.concurrent.Execution.Implicits
import services.CustomsCacheService
import uk.gov.hmrc.customs.test.{CustomsFixtures, CustomsFutures}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait CustomsSpec extends PlaySpec
  with OneAppPerSuite
  with CustomsFutures
  with CustomsFixtures with MockitoSugar {

  implicit lazy val mat: Materializer = app.materializer
  implicit lazy val ec: ExecutionContext = Implicits.defaultContext
  implicit lazy val appConfig: AppConfig = component[AppConfig]
  implicit lazy val messages: MessagesApi = component[MessagesApi]

  override lazy val app: Application = customise(GuiceApplicationBuilder()).build()

  protected def component[T: ClassTag]: T = app.injector.instanceOf[T]

  val mockCustomsCacheService: CustomsCacheService = mock[CustomsCacheService]

  def withCaching[T](form: Option[T]): OngoingStubbing[Future[CacheMap]] = {
    when(mockCustomsCacheService.get(any(), any())(any(), any()))
      .thenReturn(Future.successful(None))
    when(mockCustomsCacheService.fetchAndGetEntry[T](any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(form))

    when(mockCustomsCacheService.put(any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(CacheMap("id1", Map.empty)))
    when(mockCustomsCacheService.cache[T](any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(CacheMap("id1", Map.empty)))
  }

  def withCachingUsingKey[T](dataToReturn: Option[T], id: String): OngoingStubbing[Future[CacheMap]]  = {
    when(
      mockCustomsCacheService
        .fetchAndGetEntry[T](ArgumentMatchers.eq(appConfig.appName), ArgumentMatchers.eq(id))(any(), any(), any())
    ).thenReturn(Future.successful(dataToReturn))

    when(mockCustomsCacheService.cache[T](any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(CacheMap(id, Map.empty)))
  }

  // composite template method to be overridden by sub-types to customise the app
  // NB. when overriding, ALWAYS call super.customise(builder) and operate on the return value!
  protected def customise(builder: GuiceApplicationBuilder): GuiceApplicationBuilder = builder.overrides(
    bind[CustomsCacheService].to(mockCustomsCacheService))

}
