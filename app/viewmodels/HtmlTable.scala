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

package viewmodels

sealed abstract case class HtmlTable[A, B](header: HtmlTableRow[A], rows: List[HtmlTableRow[B]])

object HtmlTable {

  def apply[A, B](header: HtmlTableRow[A], rows: List[HtmlTableRow[B]]): Option[HtmlTable[A, B]] =
    if (rows.exists(_.size != header.size)) None
    else Some(new HtmlTable(header, rows) {})

  def apply[A, B](header: A)(values: List[B]): HtmlTable[A, B] =
    new HtmlTable(HtmlTableRow(header), values.map(HtmlTableRow(_))) {}

  def apply[A, B](header1: A, header2: A)(values: List[(B, B)]): HtmlTable[A, B] =
    new HtmlTable(HtmlTableRow(header1, List(header2)), values.map { case (a, b) => HtmlTableRow(a, List(b))}) {}
}

sealed abstract case class HtmlTableRow[A](value: A, values: List[A]) {

  def map[B](f: A => B): HtmlTableRow[B] =
    HtmlTableRow(f(value), values.map(f))

  def foldLeft[B](z: B)(f: (B, A) => B): B =
    values.foldLeft(f(z, value))(f)

  def size: Int =
    foldLeft(0)((b, _) => b + 1)
}

object HtmlTableRow {

  def apply[A](value: A, values: List[A] = List()): HtmlTableRow[A] =
    new HtmlTableRow(value, values) {}
}