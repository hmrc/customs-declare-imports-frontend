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
      const.an(42)("   ") must be(false)
    }

    "match alphanumeric string within limit" in {
      const.an(42)(randomString(42)) must be(true)
    }

    "not match string which exceeds length" in {
      const.an(42)(randomString(43)) must be(false)
    }

    "match string with non-alphanumeric characters because of dubious regex" in {
      const.an(9)("¯\\_(ツ)_/¯") must be(true)
    }

  }

  "numeric constraint" should {

    "not match empty string" in {
      const.n(2)("   ") must be(false)
    }

    "not match non numeric string" in {
      const.n(2)(randomFirstName) must be(false)
    }

    "match integer within precision" in {
      const.n(2)("42") must be(true)
    }

    "not match integer beyond precision" in {
      const.n(3)("4224") must be(false)
    }

    "match floating point within precision and scale" in {
      const.n(4,2)("42.24") must be(true)
    }

    "not match floating point beyond precision" in {
      const.n(5, 2)("4224.42") must be(false)
    }

    "not match floating point beyond scale" in {
      const.n(6, 3)("42.2442") must be(false)
    }

  }

  "alpha constraint" should {

    "not match empty string" in {
      const.a(3)("   ") must be(false)
    }

    "not match string containing non alpha characters" in {
      const.a(3)("A4B") must be(false)
    }

    "not match string that is too short" in {
      const.a(4)("foo") must be(false)
    }

    "match an alpha string of the correct length" in {
      const.a(3)("foo") must be(true)
    }

  }

  "optional constraint" should {

    "match an empty string" in {
      const.optional("   ") must be(true)
    }

    "not match a string containing any non-whitespace characters" in {
      const.optional("  foo  ") must be(false)
    }

  }

  "min constraint" should {

    "match string greater than or equal to given required length" in {
      const.min(3)(randomString(3)) must be(true)
    }

    "not match string less than given required length" in {
      const.min(3)(randomString(2)) must be(false)
    }

  }

  "range constraint" should {

    "not match non-numeric string" in {
      const.range(0, 1)(randomString(4)) must be(false)
    }

    "match string containing number equal to lower bound" in {
      const.range(5, 10)("5") must be(true)
    }

    "match string containing number equal to upper bound" in {
      const.range(5, 10)("10") must be(true)
    }

    "match string containing number within range" in {
      const.range(5, 10)("7.5") must be(true)
    }

  }

  "matches constraint" should {

    val regex = "^foo|bar|baz$".r

    "match string that matches given pattern" in {
      const.matches(regex)("foo") must be(true)
    }

    "not match string that does not match given pattern" in {
      const.matches(regex)("quix") must be(false)
    }

  }

}
