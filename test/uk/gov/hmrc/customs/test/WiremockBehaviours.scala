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

package uk.gov.hmrc.customs.test

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import play.api.http.Status

trait WiremockBehaviours {
  this: CustomsPlaySpec =>

  val customsDeclarationsApiPort: Int = appConfig.getConfInt("customs-declarations.port", throw new IllegalStateException)

  val customsDeclarationsApiHost: String = appConfig.getConfString("customs-declarations.host", throw new IllegalStateException)

  val customsDeclarationsSubmitUri: String = appConfig.getConfString("customs-declarations.submit-uri", throw new IllegalStateException)

  val customsDeclarationsServer = new WireMockServer(wireMockConfig().port(customsDeclarationsApiPort))

  def withDeclarationSubmissionApi(status: Int = Status.ACCEPTED)(test: => Unit): Unit = {
    customsDeclarationsServer.start()
    WireMock.configureFor(customsDeclarationsApiHost, customsDeclarationsApiPort)
    stubFor(post(customsDeclarationsSubmitUri).willReturn(aResponse().withStatus(status)))
    try {
      test
    } finally {
      customsDeclarationsServer.stop()
    }
  }

}
