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

package uk.gov.hmrc.customs.test.behaviours

import akka.stream.Materializer
import config.AppConfig
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.concurrent.Execution.Implicits
import uk.gov.hmrc.customs.test.{CustomsFixtures, CustomsFutures}

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

trait CustomsSpec extends PlaySpec
  with OneAppPerSuite
  with CustomsFutures
  with CustomsFixtures {

  implicit lazy val mat: Materializer = app.materializer
  implicit lazy val ec: ExecutionContext = Implicits.defaultContext
  implicit lazy val appConfig: AppConfig = component[AppConfig]
  implicit lazy val messages: MessagesApi = component[MessagesApi]

  override lazy val app: Application = customise(GuiceApplicationBuilder()).build()

  protected def component[T: ClassTag]: T = app.injector.instanceOf[T]

  // composite template method to be overridden by sub-types to customise the app
  // NB. when overriding, ALWAYS call super.customise(builder) and operate on the return value!
  protected def customise(builder: GuiceApplicationBuilder): GuiceApplicationBuilder = builder

}
