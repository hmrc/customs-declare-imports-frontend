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

package config

import org.scalatest.{MustMatchers, WordSpec}

class SubmissionJourneySpec extends WordSpec with MustMatchers {

  val journey = new SubmissionJourney()

  "previous" should {
    "return starting point given first screen" in {
      journey.prev(journey.screens.head).left.get must be(journey.start)
    }

    "return preceding item" in {
      val str = journey.screens(1)
      val value = journey.prev(str)
      value.right.get must be(journey.screens.head)
    }

    "return preceding item for last screen" in {
      journey.prev(journey.screens.last).right.get must be(journey.screens(journey.screens.size - 2))
    }
  }

  "next" should {
    "return end point given final screen" in {
      journey.next(journey.screens.last).left.get must be(journey.end)
    }

    "return following item" in {
      journey.next(journey.screens.head).right.get must be(journey.screens(1))
    }

    "return last item given forced override" in {
      journey.next(journey.screens.head, true).right.get must be(journey.screens.last)
    }
  }

}
