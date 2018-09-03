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

class RequiredAlphaValidatorSpec extends WordSpec with MustMatchers {

  "validate" should {

    "pass given string equal to required length" in {
      withValidator(3) { validator =>
        validator.validate("foo").valid must be(true)
      }
    }

    "fail given string less than requiredLength" in {
      withValidator(4) { validator =>
        validator.validate("foo").valid must be(false)
      }
    }

    "fail given string greater than required length" in {
      withValidator(2) { validator =>
        validator.validate("foo").valid must be(false)
      }
    }

    "fail given an empty string" in {
      withValidator(5) { validator =>
        validator.validate("   ").valid must be(false)
      }
    }

    "have default error message key" in {
      withValidator(3) { validator =>
        validator.validate("foo").defaultErrorKey must be("alpha.required")
      }
    }

    "include length range params as message args" in {
      withValidator(3) { validator =>
        validator.validate("foo").args must be(Seq(validator.maxLength))
      }
    }

  }

  def withValidator(length: Int)(test: RequiredAlphaValidator => Unit): Unit = {
    test(RequiredAlphaValidator(length))
  }

}
