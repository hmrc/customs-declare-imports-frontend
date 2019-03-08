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

package forms

import play.api.data._
import play.api.data.format.Formatter
import play.api.data.Forms.{mapping, _}
import uk.gov.hmrc.wco.dec.{Amount, CurrencyExchange}


trait Formatters {

  private def amountMapping(valueKey: String = "Amount", currencyKey: String = "Currency"): Mapping[Amount] =
    mapping(
      "currencyId" -> optional(
        text
          .verifying(s"$currencyKey is not valid", x => config.Options.currencyTypes.exists(_._1 == x))),
      "value" -> optional(
        bigDecimal
          .verifying(s"$valueKey cannot be greater than 99999999999999.99", _.precision <= 16)
          .verifying(s"$valueKey cannot have more than 2 decimal places", _.scale <= 2)
          .verifying(s"$valueKey must not be negative", _ >= 0))
    )(Amount.apply)(Amount.unapply)

  def amountFormatter(valueKey: String = "Amount", currencyKey: String = "Currency"): Formatter[Amount] =
    new MappingFormatter[Amount](amountMapping(valueKey, currencyKey)) {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Amount] = {

        super.bind(key, data)
          .right.flatMap { x: Amount =>

          if (x.currencyId.exists(_.nonEmpty) && x.value.isEmpty) {
            Left(List(FormError(s"$key.value", s"$valueKey is required when $currencyKey is provided")))
          } else if (!x.currencyId.exists(_.nonEmpty) && x.value.nonEmpty) {
            Left(List(FormError(s"$key.currencyId", s"$currencyKey is required when $valueKey is provided")))
          } else {
            Right(x)
          }
        }
      }
    }

  private val currencyExchangeMapping: Mapping[CurrencyExchange] = mapping(
    "currencyTypeCode" -> optional(
      text.verifying("CurrencyTypeCode is not a valid currency", x => config.Options.currencyTypes.exists(_._1 == x))),
    "rateNumeric" -> optional(
      bigDecimal
        .verifying("RateNumeric cannot be greater than 9999999.99999", _.precision <= 12)
        .verifying("RateNumeric cannot have more than 5 decimal places", _.scale <= 5)
        .verifying("RateNumeric must not be negative", _ >= 0))
  )(CurrencyExchange.apply)(CurrencyExchange.unapply)

  val currencyExchangeFormatter: Formatter[CurrencyExchange] =
    new MappingFormatter[CurrencyExchange](currencyExchangeMapping) {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], CurrencyExchange] = {

        super.bind(key, data)
          .right.flatMap { x =>

          if (x.currencyTypeCode.exists(_.nonEmpty) && x.rateNumeric.isEmpty) {
            Left(Seq(FormError(s"$key.rateNumeric", "Exchange rate is required when currency is provided")))
          } else if (x.rateNumeric.nonEmpty && !x.currencyTypeCode.exists(_.nonEmpty)) {
            Left(Seq(FormError(s"$key.currencyTypeCode", "Currency ID is required when amount is provided")))
          } else {
            Right(x)
          }
        }
      }
    }

  private class MappingFormatter[T](baseMapping: Mapping[T]) extends Formatter[T] {

    private case class Wrapper(value: T)

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {

      mapping(key -> baseMapping)(Wrapper.apply)(Wrapper.unapply)
        .bind(data)
        .right.map(_.value)
    }

    override def unbind(key: String, value: T): Map[String, String] = {

      mapping(key -> baseMapping)(Wrapper.apply)(Wrapper.unapply)
        .unbind(Wrapper(value))
    }
  }
}

object Formatters extends Formatters