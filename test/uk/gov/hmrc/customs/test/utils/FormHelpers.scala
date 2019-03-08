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

package uk.gov.hmrc.customs.test.utils

trait FormHelpers {

  // Converts a product type to a map of values replicating the same structure when
  // posting data from a html form
  def asFormParams(cc: Product): List[(String, String)] =
    cc.getClass.getDeclaredFields.toList
      .map { f =>
        f.setAccessible(true)
        (f.getName, f.get(cc))
      }
      .filterNot(_._1 == "serialVersionUID")
      .filterNot(_._1 == "MODULE$")
      .flatMap {
        case (n, l: List[_]) if l.headOption.exists(_.isInstanceOf[Product]) =>
          l.zipWithIndex.flatMap {
            case (x, i) => asFormParams(x.asInstanceOf[Product]).map { case (k, v) => (s"$n[$i].$k", v) }
          }
        case (n, Some(p: Product)) => asFormParams(p).map { case (k, v) => (s"$n.$k", v) }
        case (n, Some(a))          => List((n, a.toString))
        case (n, None)             => List((n, ""))
        case (n, p: Product)       => asFormParams(p).map { case (k, v) => (s"$n.$k", v) }
        case (n, a)                => List((n, a.toString))
      }
}