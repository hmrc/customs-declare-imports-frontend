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

import uk.gov.hmrc.wco.dec.MetaData

trait FieldDefinition {
  val name: String
  val labelKey: Option[String]
  val htmlId: Option[String]
  val hintKey: Option[String]
  val validators: Seq[Validator]

  def applyValue(value: String, metaData: MetaData): MetaData = metaData // for now, by default, do nothing (use JUEL to implement?)

  def maxLength: Option[Int] = validators.filter(_.isInstanceOf[MaxLength]).map(_.asInstanceOf[MaxLength].maxLength).reduceLeftOption(_ min _)

  def id(num: Option[Int] = None): String = htmlId.getOrElse(name.replaceAll("[.\\[\\]]", "_")) + num.map(n => "_" + n).getOrElse("")

  def labelMessageKey: String = labelKey.getOrElse(name)
}

trait MultipleChoice {
  val optional: Boolean // whether to include an "empty" option in addition to the specified options
  val options: Seq[(String, String)]
}

trait DefaultValue {
  val default: Option[String]
}

case class TextInput(name: String,
                     labelKey: Option[String] = None,
                     htmlId: Option[String] = None,
                     hintKey: Option[String] = None,
                     validators: Seq[Validator] = Seq.empty) extends FieldDefinition

case class SelectInput(name: String,
                       options: Seq[(String, String)],
                       optional: Boolean = true,
                       default: Option[String]= None,
                       labelKey: Option[String] = None,
                       htmlId: Option[String] = None,
                       hintKey: Option[String] = None,
                       validators: Seq[Validator] = Seq.empty) extends FieldDefinition with MultipleChoice with DefaultValue

case class RadioInput(name: String,
                      options: Seq[(String, String)],
                      inline: Boolean = false,
                      default: Option[String] = None,
                      labelKey: Option[String] = None,
                      htmlId: Option[String] = None,
                      hintKey: Option[String] = None,
                      validators: Seq[Validator] = Seq.empty) extends FieldDefinition with MultipleChoice with DefaultValue {
  override val optional = false
}

// this is a marker interface which can be used in HTML to drive incorporation of maxLength attribute via FieldDefinition.maxLength
trait MaxLength {
  val maxLength: Int
}
