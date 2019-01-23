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

// there is no guarantee that the header and rows contain the same number of
// elements - possible solution would be incorporate refined types or shapeless
case class Table[A, B](header: TableRow[A], rows: List[TableRow[B]])

case class TableRow[A](value: A, values: List[A]) {

  def map[B](f: A => B): TableRow[B] =
    TableRow(f(value), values.map(f))

  def foldLeft[B](z: B)(f: (B, A) => B): B =
    values.foldLeft(f(z, value))(f)
}