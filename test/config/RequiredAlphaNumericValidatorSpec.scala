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

class RequiredAlphaNumericValidatorSpec extends WordSpec with MustMatchers {

  "validate" should {

    "pass given string equal to length limit" in {
      withValidator(3) { validator =>
        validator.validate("foo").valid must be(true)
      }
    }

    "pass given string less than length limit" in {
      withValidator(4) { validator =>
        validator.validate("foo").valid must be(true)
      }
    }

    "pass given string equal to minimum length" in {
      withValidator(5, 3) { validator =>
        validator.validate("foo").valid must be(true)
      }
    }

    "fail given string greater than length limit" in {
      withValidator(2) { validator =>
        validator.validate("foo").valid must be(false)
      }
    }

    "fail given string less than minimum length" in {
      withValidator(5, 4) { validator =>
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
        validator.validate("foo").defaultErrorKey must be("alphanumeric.required")
      }
    }

    "include length range params as message args" in {
      withValidator(3) { validator =>
        validator.validate("foo").args must be(Seq(validator.minLength, validator.maxLength))
      }
    }

  }

  def withValidator(maxLength: Int, minLength: Int = 1)(test: RequiredAlphaNumericValidator => Unit): Unit = {
    test(RequiredAlphaNumericValidator(maxLength, minLength))
  }

}
