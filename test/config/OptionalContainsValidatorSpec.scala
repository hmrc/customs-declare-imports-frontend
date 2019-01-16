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

package config

import org.scalatest.{MustMatchers, WordSpec}

class OptionalContainsValidatorSpec extends WordSpec with MustMatchers {

  val someOpts = Set("foo", "bar", "baz")

  "validate" should {

    "pass given options contains value" in {
      withValidator(someOpts) { validator =>
        validator.validate("foo").valid must be(true)
      }
    }

    "pass given empty string" in {
      withValidator(someOpts) { validator =>
        validator.validate("   ").valid must be(true)
      }
    }

    "fail given options do not contain value" in {
      withValidator(someOpts) { validator =>
        validator.validate("quix").valid must be(false)
      }
    }

    "have default message key" in {
      withValidator(someOpts) { validator =>
        validator.validate("foo").defaultErrorKey must be("contains.optional")
      }
    }

  }

  def withValidator(opts: Set[String])(test: OptionalContainsValidator => Unit): Unit = {
    test(OptionalContainsValidator(opts))
  }

}
