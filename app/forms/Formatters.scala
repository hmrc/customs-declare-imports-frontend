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
import play.api.data.Forms._
import uk.gov.hmrc.wco.dec.Amount


trait Formatters {

  def amountFormatter(valueKey: String = "Amount", currencyKey: String = "Currency"): Formatter[Amount] =
    new Formatter[Amount] {

      private case class Wrapper(value: Amount)

      private val amountMapping: Mapping[Amount] = mapping(
        "currencyId" -> optional(
          text
            .verifying(s"$currencyKey is not valid", x => config.Options.currencyTypes.exists(_._1 == x))),
        "value" -> optional(
          bigDecimal
            .verifying(s"$valueKey cannot be greater than 99999999999999.99", _.precision <= 16)
            .verifying(s"$valueKey cannot have more than 2 decimal places", _.scale <= 2)
            .verifying(s"$valueKey must not be negative", _ >= 0))
      )(Amount.apply)(Amount.unapply)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Amount] = {

        mapping(key -> amountMapping)(Wrapper.apply)(Wrapper.unapply)
          .bind(data)
          .right.map(_.value)
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

      override def unbind(key: String, value: Amount): Map[String, String] = {

        mapping(key -> amountMapping)(Wrapper.apply)(Wrapper.unapply)
         .unbind(Wrapper(value))
      }
    }
}

object Formatters extends Formatters