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
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.{AdditionalInformation, AuthorisationHolder, PreviousDocument}

class DeclarationFormMappingSpec extends WordSpec
  with MustMatchers
  with PropertyChecks
  with Generators
  with FormMatchers {

  "additionalInformationForm" should {

    "bind" when {

      "valid values are bound" in {

        forAll { additionalInfo: AdditionalInformation =>

          Form(additionalInformationMapping).fillAndValidate(additionalInfo).fold(
              error      => fail(s"Failed with errors:\n${error.errors.map(_.message).mkString("\n")}"),
              result     => result mustBe additionalInfo
          )
        }
      }

      "fail with invalid statement code" when {

        "statement code length is greater than 17" in {

          forAll(arbitrary[AdditionalInformation], minStringLength(18)) { (additionalInfo, invalidCode) =>

            Form(additionalInformationMapping).fillAndValidate(additionalInfo.copy(statementCode = Some(invalidCode))).fold(
              error => error.error("statementCode") must haveMessage("statement code should be less than or equal to 17 characters"),
              _     => fail("Should not succeed")
            )
          }
        }
      }

      "fail with invalid statement description" when {

        "statement description length is greater than 512" in {

          forAll(arbitrary[AdditionalInformation], minStringLength(513))  { (additionalInfo, invalidDescription) =>

            Form(additionalInformationMapping).fillAndValidate(additionalInfo.copy(statementDescription = Some(invalidDescription))).fold(
              error => error.error("statementDescription") must haveMessage("statement description should be less than or equal to 512 characters"),
              _     => fail("Should not succeed")
            )
          }
        }
      }

      "fail with invalid statement type code" when {

        "statement type code length is greater than 3" in {

          forAll(arbitrary[AdditionalInformation], minStringLength(4))  { (additionalInfo, invalidTypeCode) =>

            Form(additionalInformationMapping).fillAndValidate(additionalInfo.copy(statementTypeCode = Some(invalidTypeCode))).fold(
              error => error.error("statementTypeCode") must haveMessage("statement type code should be less than or equal to 3 characters"),
              _     => fail("Should not succeed")
            )
          }
        }
      }
    }
  }

  "authorisationHolderForm" should {

    "bind" when {

      "valid values are bound" in {

        forAll { authorisationHolder: AuthorisationHolder =>

          Form(authorisationHolderMapping).fillAndValidate(authorisationHolder).fold(
            error => fail(s"Failed with errors:\n${error.errors.map(_.message).mkString("\n")}"),
            result => result mustBe authorisationHolder
          )
        }
      }
    }

    "fail to bind" when {

      "id length is greater than 17" in {

        forAll(arbitrary[AuthorisationHolder], minStringLength(18)) { (authorisationHolder, invalidId) =>

          Form(authorisationHolderMapping).fillAndValidate(authorisationHolder.copy(id = Some(invalidId))).fold(
            error => error.error("id") must haveMessage("ID should be less than or equal to 17 characters"),
            _     => fail("Should not succeed")
          )
        }
      }

      "category code length is greater than 4" in {

        forAll(arbitrary[AuthorisationHolder], minStringLength(5)) { (authorisationHolder, invalidCategoryCode) =>

          Form(authorisationHolderMapping).fillAndValidate(authorisationHolder.copy(categoryCode = Some(invalidCategoryCode))).fold(
            error => error.error("categoryCode") must haveMessage("Category Code should be less than or equal to 4 characters"),
            _     => fail("Should not succeed")
          )
        }
      }

      "both id and category code are missing" in {

        Form(authorisationHolderMapping).bind(Map[String, String]()).fold(
          error => error must haveErrorMessage("You must provide an ID or category code"),
          _     => fail("Should not succeed")
        )
      }
    }
  }

  "addPreviousDocumentMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll(arbitrary[PreviousDocument]) { arbitraryAddPreviousDocument =>

          Form(previousDocumentMapping).fillAndValidate(arbitraryAddPreviousDocument).fold(
            error => fail(s"Failed with errors:\n${error.errors.map(_.message).mkString("\n")}"),
            result => result mustBe arbitraryAddPreviousDocument
          )
        }
      }
    }

    "fail to bind" when {

      "category code length is greater than 1" in {

        forAll(arbitrary[PreviousDocument], minStringLength(2)) { (arbitraryAddPreviousDocument, invalidCategoryCode) =>

          Form(previousDocumentMapping).fillAndValidate(arbitraryAddPreviousDocument.copy(categoryCode = Some(invalidCategoryCode))).fold(
            error => error.error("categoryCode") must haveMessage("Document Category  should be less than or equal to 1 character"),
            _ => fail("Should not succeed")
          )
        }
      }

      "id length is greater than 35" in {

        forAll(arbitrary[PreviousDocument], minStringLength(36)) { (arbitraryAddPreviousDocument, invalidId) =>

          Form(previousDocumentMapping).fillAndValidate(arbitraryAddPreviousDocument.copy(id = Some(invalidId))).fold(
            error => error.error("id") must haveMessage("Document Reference should be less than or equal to 35 characters"),
            _ => fail("Should not succeed")
          )
        }
      }

      "type code length is greater than 3" in {

        forAll(arbitrary[PreviousDocument], minStringLength(4)) { (arbitraryAddPreviousDocument, invalidTypeCode) =>

          Form(previousDocumentMapping).fillAndValidate(arbitraryAddPreviousDocument.copy(typeCode = Some(invalidTypeCode))).fold(
            error => error.error("typeCode") must haveMessage("Previous Document Type should be less than or equal to 3 characters"),
            _ => fail("Should not succeed")
          )
        }
      }

      "line numeric is less than 0" in {

        forAll(arbitrary[PreviousDocument], intBetweenRange(Int.MinValue, 0)) { (arbitraryAddPreviousDocument, invalidLineNumberic) =>

          Form(previousDocumentMapping).fillAndValidate(arbitraryAddPreviousDocument.copy(lineNumeric = Some(invalidLineNumberic))).fold(
            error => error.error("lineNumeric") must haveMessage("Goods Item Identifier should be greater than 0 and less than or equal to 999"),
            _ => fail("Should not succeed")
          )
        }
      }

      "line numeric is greater than 999" in {

        forAll(arbitrary[PreviousDocument], intBetweenRange(1000, Int.MaxValue)) { (arbitraryAddPreviousDocument, invalidLineNumberic) =>

          Form(previousDocumentMapping).fillAndValidate(arbitraryAddPreviousDocument.copy(lineNumeric = Some(invalidLineNumberic))).fold(
            error => error.error("lineNumeric") must haveMessage("Goods Item Identifier should be greater than 0 and less than or equal to 999"),
            _ => fail("Should not succeed")
          )
        }
      }

      "Document Category, Document Reference, Previous Document Type and Goods Item Identifier are missing" in {

        Form(previousDocumentMapping).bind(Map[String, String]()).fold(
          error => error must haveErrorMessage("You must provide a Document Category or Document Reference or Previous Document Type or Goods Item Identifier"),
          _ => fail("Should not succeed")
        )
      }
    }
  }
}