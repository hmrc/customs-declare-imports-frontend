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

import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._

trait CustomsFutures extends ScalaFutures {

  implicit lazy val patience: PatienceConfig = PatienceConfig(timeout = 5.seconds, interval = 50.milliseconds) // be more patient than the default

}
