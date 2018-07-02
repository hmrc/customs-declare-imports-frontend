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
import play.api.http.Status
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.ControllerSpec

class HelloWorldControllerSpec extends ControllerSpec {

  val req = FakeRequest("GET", "/")
  val messages = app.injector.instanceOf[MessagesApi]
  val config = app.injector.instanceOf[AppConfig]
  val controller = new HelloWorld(messages, config)

  "GET /" should {
    "return 200" in {
      val result = call(controller.helloWorld, req)
      status(result) must be (Status.OK)
    }

    "return HTML" in {
      val result = call(controller.helloWorld, req)
      contentType(result) must be (Some("text/html"))
      charset(result) must be (Some("utf-8"))
    }

  }
}
