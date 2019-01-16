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

class RequiredNumericValidatorSpec extends WordSpec with MustMatchers {

  "validate" should {

    "pass given int equal to max implied via precision" in {
      withValidator(2) { validator =>
        validator.validate("99").valid must be(true)
      }
    }

    "pass given int less than max implied via precision" in {
      withValidator(2) { validator =>
        validator.validate("9").valid must be(true)
      }
    }

    "pass given float equal to max implied via precision and scale" in {
      withValidator(4, 2) { validator =>
        validator.validate("99.99").valid must be(true)
      }
    }

    "pass given float less than max implied via precision and scale" in {
      withValidator(4, 2) { validator =>
        validator.validate("9.9").valid must be(true)
      }
    }

    "pass given int equal to explicit max" in {
      withValidator(10, max = 42) { validator =>
        validator.validate("42").valid must be(true)
      }
    }

    "pass given int less than explicit max" in {
      withValidator(10, max = 42) { validator =>
        validator.validate("42").valid must be(true)
      }
    }

    "pass given int equal to explicit min" in {
      withValidator(10, min = 42) { validator =>
        validator.validate("42").valid must be(true)
      }
    }

    "pass given int greater than explicit min" in {
      withValidator(10, min = 24) { validator =>
        validator.validate("42").valid must be(true)
      }
    }

    "pass given float equal to explicit max" in {
      withValidator(4, 2, max = 42) { validator =>
        validator.validate("42.00").valid must be(true)
      }
    }

    "pass given float less than explicit max" in {
      withValidator(4, 2, max = 42) { validator =>
        validator.validate("24.00").valid must be(true)
      }
    }

    "pass given float equal to explicit min" in {
      withValidator(4, 2, min = 42) { validator =>
        validator.validate("42.00").valid must be(true)
      }
    }

    "pass given float greater than explicit min" in {
      withValidator(4, 2, min = 24) { validator =>
        validator.validate("42.00").valid must be(true)
      }
    }

    "fail given an empty string" in {
      withValidator(5) { validator =>
        validator.validate("   ").valid must be(false)
      }
    }

    "fail given int greater than max implied via precision" in {
      withValidator(2) { validator =>
        validator.validate("100").valid must be(false)
      }
    }

    "fail given float greater than max implied via precision" in {
      withValidator(2) { validator =>
        validator.validate("100").valid must be(false)
      }
    }

    "fail given float greater than max implied via scale" in {
      withValidator(4, 1) { validator =>
        validator.validate("9.99").valid must be(false)
      }
    }

    "fail given int greater than explicit max" in {
      withValidator(10, max = 42) { validator =>
        validator.validate("43").valid must be(false)
      }
    }

    "fail given int less than explicit min" in {
      withValidator(10, min = 42) { validator =>
        validator.validate("41").valid must be(false)
      }
    }

    "fail given float greater than explicit max" in {
      withValidator(4, 2, max = 42) { validator =>
        validator.validate("42.01").valid must be(false)
      }
    }

    "fail given float less than explicit min" in {
      withValidator(4, 2, min = 42) { validator =>
        validator.validate("41.99").valid must be(false)
      }
    }

    "have default error message key" in {
      withValidator(3) { validator =>
        validator.validate("42").defaultErrorKey must be("numeric.required")
      }
    }

    "include length range params as message args" in {
      withValidator(3) { validator =>
        validator.validate("foo").args must be(Seq(validator.precision, validator.scale, validator.min, validator.max))
      }
    }

  }

  def withValidator(precision: Int, scale: Int = 0, min: Long = Long.MinValue, max: Long = Long.MaxValue)(test: RequiredNumericValidator => Unit): Unit = {
    test(RequiredNumericValidator(precision, scale, min, max))
  }

}
