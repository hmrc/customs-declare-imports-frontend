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

import java.util.UUID

import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.wco.dec.MetaData

trait CustomsDeclarationsApiBehaviours extends CustomsSpec {

  def withCustomsDeclarationsApi(request: MetaData, status: Int = Status.ACCEPTED, conversationId: String = UUID.randomUUID().toString)
                                (test: => Unit): Unit = {
    // TODO
    test
  }

  override protected def customise(builder: GuiceApplicationBuilder): GuiceApplicationBuilder = {
    println("Customs Decs API app customisation")
    super.customise(builder)
  }

}
