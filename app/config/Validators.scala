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

import scala.util.matching.Regex

case class ValidationResult(valid: Boolean, defaultErrorKey: String, args: Seq[Any] = Seq.empty)

case class ValidationError(message: String, field: FieldDefinition)

// counter-intuitively, args take max first because min is usually 1, so can be optional
case class RequiredAlphaNumericValidator(maxLength: Int, minLength: Int = 1) extends Validator with MaxLength {
  override def validate(value: String): ValidationResult =
    ValidationResult(an(maxLength)(value) && min(minLength)(value), "alphanumeric.required", Seq(minLength, maxLength))
}

case class OptionalAlphaNumericValidator(maxLength: Int, minLength: Int = 1) extends Validator with MaxLength {
  override def validate(value: String): ValidationResult =
    ValidationResult(optional(value) || (an(maxLength)(value) && min(minLength)(value)),
                     "alphanumeric.optional",
                     Seq(minLength, maxLength))
}

case class RequiredNumericValidator(precision: Int,
                                    scale: Int = 0,
                                    min: Long = Long.MinValue,
                                    max: Long = Long.MaxValue)
    extends Validator
    with MaxLength {
  override val maxLength: Int = precision + 2 // allow for +- and decimal point
  override def validate(value: String): ValidationResult =
    ValidationResult(n(precision, scale)(value) && range(min, max)(value),
                     "numeric.required",
                     Seq(precision, scale, min, max))
}

case class OptionalNumericValidator(precision: Int,
                                    scale: Int = 0,
                                    min: Long = Long.MinValue,
                                    max: Long = Long.MaxValue)
    extends Validator
    with MaxLength {
  override val maxLength: Int = precision + 2 // allow for +- and decimal point
  override def validate(value: String): ValidationResult =
    ValidationResult(optional(value) || (n(precision, scale)(value) && range(min, max)(value)),
                     "numeric.optional",
                     Seq(precision, scale, min, max))
}

case class RequiredAlphaValidator(maxLength: Int) extends Validator with MaxLength {
  override def validate(value: String): ValidationResult =
    ValidationResult(a(maxLength)(value), "alpha.required", Seq(maxLength))
}

case class OptionalAlphaValidator(maxLength: Int) extends Validator with MaxLength {
  override def validate(value: String): ValidationResult =
    ValidationResult(optional(value) || a(maxLength)(value), "alpha.optional", Seq(maxLength))
}

case class RequiredContainsValidator(options: Set[String]) extends Validator {
  override def validate(value: String): ValidationResult =
    ValidationResult(contains(options)(value), "contains.required")
}

case class OptionalContainsValidator(options: Set[String]) extends Validator {
  override def validate(value: String): ValidationResult =
    ValidationResult(optional(value) || contains(options)(value), "contains.optional")
}

trait Validator extends Constraints {
  def validate(value: String): ValidationResult
}

class Constraints {

  private val alpha
    : Regex = "([a-zA-Z])*".r // empty string accepted as valid by schema; length constraint applied separately

  private val alphaNumeric
    : Regex = ".*[^\\s].*".r // yes, I know this is a dumb regex but it is what the XML schema uses!

  private val numeric: Regex = "^\\d*\\.?\\d+$".r

  // this function is designed to aid in the construction of validators for WCO types defined as "an..[some max length]"
  def an(maxLength: Int): String => Boolean =
    str => str.trim.nonEmpty && str.length <= maxLength && matches(alphaNumeric)(str)

  // this function is designed to aid in the construction of validators for WCO types defined as "n..[some precision]" or "n..[some precision],[some scale]"
  def n(precision: Int, scale: Int = 0): String => Boolean =
    str => matches(numeric)(str) && withinPrecisionAndScale(BigDecimal(str), precision, scale)

  // this function is designed to aid in the construction of validators for WCO types defined as "a[some length]"
  def a(length: Int): String => Boolean = str => str.length == length && matches(alpha)(str)

  // this function is designed to be used as a combinator with another constraint for optional fields
  def optional: String => Boolean = str => str.trim.isEmpty

  def min(min: Int): String => Boolean = str => str.trim.length >= min

  def contains(options: Set[String]): String => Boolean = str => options.contains(str)

  def range(min: Long, max: Long): String => Boolean =
    str => matches(numeric)(str) && str.toDouble >= min && str.toDouble <= max

  def matches(regex: Regex): String => Boolean = str => regex.pattern.matcher(str).matches()

  private def withinPrecisionAndScale(bd: BigDecimal, precision: Int, scale: Int): Boolean =
    bd.precision <= precision && bd.scale <= scale

}
