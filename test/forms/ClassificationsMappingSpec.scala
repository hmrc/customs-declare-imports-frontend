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

import forms.DeclarationFormMapping._
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.Classification

class ClassificationsMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  val form = Form(classificationMapping)
  "classificationMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { classification: Classification =>
          Form(classificationMapping).fillAndValidate(classification).fold(
            e => fail(s"form should not fail: ${e.errors}"),
            _ mustBe classification)

        }
      }
    }

    "fail" when {

      "Id is longer than 8 characters" in {

        forAll(arbitrary[Classification], stringsLongerThan(8)) {
          (classification, id) =>

            val data = classification.copy(id = Some(id))
            Form(classificationMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Id must be equal to or less than 8 characters"),
              _ => fail("form should not succeed")
            )
        }
      }

      "Type is selected and id is not provided" in {

        forAll(arbitrary[Classification]) {
          (classification) =>
            val data = classification.copy(id = None)
            Form(classificationMapping).bind(Map[String, String]("identificationTypeCode" -> data.identificationTypeCode.getOrElse("CV"))).fold(
              _ must haveErrorMessage("Id is required when Type is provided"),
              _ => fail("form should not succeed")
            )
        }
      }
      "Type is not selected and id is provided" in {

        forAll(arbitrary[Classification]) {
          (classification) =>
            val data = classification.copy(identificationTypeCode = None)
            Form(classificationMapping).bind(Map[String, String]("id" -> data.id.getOrElse("id1"))).fold(
              _ must haveErrorMessage("Type is required when Id is provided"),
              _ => fail("form should not succeed")
            )
        }
      }

      "Id and Type are missing" in {

        Form(classificationMapping).bind(Map[String, String]()).fold(
          _ must haveErrorMessage("Id and Type is required to add classification"),
          _ => fail("form should not succeed")
        )
      }
    }
  }

}