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

import uk.gov.hmrc.customs.test.CustomsPlaySpec

class ConstraintsSpec extends CustomsPlaySpec {

  private val const = new Constraints {}

  "alphanumeric constraint" should {

    "not match empty string" in {
      const.an(42).apply("   ") must be(false)
    }

    "match alphanumeric string within limit" in {
      const.an(42).apply(randomString(42)) must be(true)
    }

    "not match string which exceeds length" in {
      const.an(42).apply(randomString(43)) must be(false)
    }

    "match string with non-alphanumeric characters because of dubious regex" in {
      const.an(9).apply("¯\\_(ツ)_/¯") must be(true)
    }

  }

  "numeric constraint" should {

    "not match empty string" in {
      const.n(2).apply("   ") must be(false)
    }

    "not match non numeric string" in {
      const.n(2).apply(randomString(2)) must be(false)
    }

    "match integer within precision" in {
      const.n(2).apply("42") must be(true)
    }

    "not match integer beyond precision" in {
      const.n(3).apply("4224") must be(false)
    }

    "match floating point within precision and scale" in {
      const.n(4,2).apply("42.24") must be(true)
    }

    "not match floating point beyond precision" in {
      const.n(5, 2).apply("4224.42") must be(false)
    }

    "not match floating point beyond scale" in {
      const.n(6, 3).apply("42.2442") must be(false)
    }

  }

  "alpha constraint" should {

    "not match empty string" in {
      const.a(3).apply("   ") must be(false)
    }

    "not match string containing non alpha characters" in {
      const.a(3).apply("A4B") must be(false)
    }

    "not match string that is too short" in {
      const.a(4).apply("foo") must be(false)
    }

    "match an alpha string of the correct length" in {
      const.a(3).apply("foo") must be(true)
    }

  }

  "optional constraint" should {

    "match an empty string" in {
      const.optional.apply("   ") must be(true)
    }

    "not match a string containing any non-whitespace characters" in {
      const.optional.apply("  foo  ") must be(false)
    }

  }

}
