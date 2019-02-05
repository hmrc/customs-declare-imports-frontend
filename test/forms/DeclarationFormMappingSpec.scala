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
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import uk.gov.hmrc.customs.test.FormMatchers
import uk.gov.hmrc.wco.dec.{GovernmentProcedure, _}

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
            error => fail(s"Failed with errors:\n${error.errors.map(_.message).mkString("\n")}"),
            result => result mustBe additionalInfo
          )
        }
      }

      "fail with invalid statement code" when {

        "Code , Description are missing in additional information" in {

          Form(additionalInformationMapping).bind(Map[String, String]()).fold(
            _ must haveErrorMessage("You must provide Code or Description"),
            _ => fail("Should not succeed")
          )
        }

        "statement code length is greater than 5" in {

          forAll(arbitrary[AdditionalInformation], minStringLength(6)) { (additionalInfo, invalidCode) =>

            Form(additionalInformationMapping).fillAndValidate(additionalInfo.copy(statementCode = Some(invalidCode))).fold(
              error => error.error("statementCode") must haveMessage("Code should be less than or equal to 5 characters"),
              _ => fail("Should not succeed")
            )
          }
        }
      }

      "fail with invalid statement description" when {

        "statement description length is greater than 512" in {

          forAll(arbitrary[AdditionalInformation], minStringLength(513)) { (additionalInfo, invalidDescription) =>

            Form(additionalInformationMapping).fillAndValidate(additionalInfo.copy(statementDescription = Some(invalidDescription))).fold(
              error => error.error("statementDescription") must haveMessage("Description should be less than or equal to 512 characters"),
              _ => fail("Should not succeed")
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
            _ => fail("Should not succeed")
          )
        }
      }

      "category code length is greater than 4" in {

        forAll(arbitrary[AuthorisationHolder], minStringLength(5)) { (authorisationHolder, invalidCategoryCode) =>

          Form(authorisationHolderMapping).fillAndValidate(authorisationHolder.copy(categoryCode = Some(invalidCategoryCode))).fold(
            error => error.error("categoryCode") must haveMessage("Category Code should be less than or equal to 4 characters"),
            _ => fail("Should not succeed")
          )
        }
      }

      "both id and category code are missing" in {

        Form(authorisationHolderMapping).bind(Map[String, String]()).fold(
          error => error must haveErrorMessage("You must provide an ID or category code"),
          _ => fail("Should not succeed")
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

  "roleBasedPartyMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { roleParty: RoleBasedParty =>

          Form(roleBasedPartyMapping).fillAndValidate(roleParty).fold(
            _ => fail("form should not fail"),
            success => success mustBe roleParty
          )
        }
      }
    }

    "fail" when {

      "id is longer than 17 characters" in {

        forAll(arbitrary[RoleBasedParty], minStringLength(18)) {
          (roleParty, id) =>

            val data = roleParty.copy(id = Some(id))
            Form(roleBasedPartyMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Role based party id should be less than or equal to 17 characters"),
              _ => fail("should not succeed")
            )
        }
      }

      "roleCode is longer than 3 characters" in {

        forAll(arbitrary[RoleBasedParty], minStringLength(4)) {
          (roleParty, roleCode) =>

            val data = roleParty.copy(roleCode = Some(roleCode))
            Form(roleBasedPartyMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Role code should be less than or equal to 3 characters"),
              _ => fail("should not succeed")
            )
        }
      }

      "neither id or roleCode is supplied" in {

        Form(roleBasedPartyMapping).bind(Map.empty[String, String]).fold(
          _ must haveErrorMessage("You must provide an ID or role code"),
          _ => fail("should not succeed")
        )
      }
    }
  }

  "transportEquipmentMapping" should {

    "bind" when {

      "valid values are provided" in {

        forAll { transport: TransportEquipment =>

          Form(transportEquipmentMapping).fillAndValidate(transport).fold(
            _ => fail("form should not fail"),
            _ mustBe transport
          )
        }
      }
    }

    "fail" when {

      "id is longer than 17 characters" in {

        forAll(stringsLongerThan(17)) { id =>

          Form(transportEquipmentMapping).bind(Map("id" -> id)).fold(
            _ must haveErrorMessage("Container Identification number must be 17 characters or less"),
            _ => fail("form should fail")
          )
        }
      }

      "id is not provided" in {

        Form(transportEquipmentMapping).bind(Map[String, String]()).fold(
          _ must haveErrorMessage("Container Identification number is required"),
          _ => fail("form should fail")
        )
      }
    }
  }

  "amountMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { amount: Amount =>

          Form(amountMapping).fillAndValidate(amount).fold(
            e => fail(s"form should not fail: ${e.errors}"),
            success => success mustBe amount
          )
        }
      }
    }

    "fail" when {

      "currencyId is not a currency" in {

        val badData = stringsExceptSpecificValues(config.Options.currencyTypes.map(_._2).toSet)
        forAll(arbitrary[Amount], badData) {
          (amount, currency) =>

            val data = amount.copy(currencyId = Some(currency))
            Form(amountMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Currency ID is not a valid currency"),
              _ => fail("form should not succeed")
            )
        }
      }

      "value has a precision greater than 16" in {

        forAll(arbitrary[Amount], decimal(17, 30, 0)) {
          (amount, deduction) =>

            val data = amount.copy(value = Some(deduction))
            Form(amountMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Amount cannot be greater than 99999999999999.99"),
              _ => fail("form should not succeed")
            )
        }
      }

      "value has a scale greater than 2" in {

        val badData = choose(3, 10).flatMap(posDecimal(16, _))

        forAll(arbitrary[Amount], badData) {
          (amount, deduction) =>

            val data = amount.copy(value = Some(deduction))
            Form(amountMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Amount cannot have more than 2 decimal places"),
              _ => fail("form should not succeed")
            )
        }
      }

      "value is less than 0" in {

        forAll(arbitrary[Amount], intLessThan(0)) {
          (amount, deduction) =>

            val data = amount.copy(value = Some(BigDecimal(deduction)))
            Form(amountMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Amount must not be negative"),
              _ => fail("form should not succeed")
            )
        }
      }

      "has a currency with no value" in {

        forAll { amount: Amount =>

          whenever(amount.currencyId.nonEmpty) {

            Form(amountMapping).bind(Map("currencyId" -> amount.currencyId.getOrElse(""))).fold(
              _ must haveErrorMessage("Amount is required when currency is provided"),
              _ => fail("form should not succeed")
            )
          }
        }
      }

      "has a value with no currency" in {

        forAll { amount: Amount =>

          whenever(amount.value.nonEmpty) {

            Form(amountMapping).bind(Map("value" -> amount.value.fold("")(_.toString))).fold(
              _ must haveErrorMessage("Currency is required when amount is provided"),
              _ => fail("form should not succeed")
            )
          }
        }
      }
    }
  }

  "chargeDeductionMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { charge: ChargeDeduction =>

          Form(chargeDeductionMapping).fillAndValidate(charge).fold(
            e => fail(s"form should not fail: ${e.errors}"),
            _ mustBe charge
          )
        }
      }
    }

    "fail" when {

      "type code is longer than 3 characters" in {

        forAll(arbitrary[ChargeDeduction], stringsLongerThan(2)) {
          (charge, typeCode) =>

            val data = charge.copy(chargesTypeCode = Some(typeCode))
            Form(chargeDeductionMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Charges code should be less than or equal to 2 characters"),
              _ => fail("form should not succeed")
            )
        }
      }

      "type code and amount are missing" in {

        Form(chargeDeductionMapping).bind(Map[String, String]()).fold(
          _ must haveErrorMessage("Charges code, currency id or amount are required"),
          _ => fail("form should not succeed")
        )
      }
    }
  }

  "GovernmentProcedure Mapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { govermentProcedure: GovernmentProcedure =>
          Form(governmentProcedureMapping).fillAndValidate(govermentProcedure).fold(
            _ => fail("form should not fail"),
            success => success mustBe govermentProcedure
          )
        }
      }
    }
    "Current Code and Previous Code are missing" in {

      Form(governmentProcedureMapping).bind(Map[String, String]()).fold(
        _ must haveErrorMessage("To add procedure codes you must provide Current Code or Previous code"),
        _ => fail("Should not succeed")
      )
    }

    "currentCode is longer than 2 characters" in {

      forAll(arbitrary[GovernmentProcedure], minStringLength(2)) {
        (governmentProcedure, code) =>

          val data = governmentProcedure.copy(currentCode = Some(code))
          Form(governmentProcedureMapping).fillAndValidate(data).fold(
            _ must haveErrorMessage("Current code should be less than or equal to 2 characters"),
            _ => fail("should not succeed")
          )
      }
    }

    "previousCode is longer than 2 characters" in {

      forAll(arbitrary[GovernmentProcedure], minStringLength(2)) {
        (governmentProcedure, code) =>

          val data = governmentProcedure.copy(previousCode = Some(code))
          Form(governmentProcedureMapping).fillAndValidate(data).fold(
            _ must haveErrorMessage("Previous code  should be less than or equal to 2 characters"),
            _ => fail("should not succeed")
          )
      }
    }
  }

  "Date" should {
    "bind" when {
      "valid values are bound" in {
        forAll { date: Date =>
          Form(dateMapping).fillAndValidate(date).fold(
            error => fail(s"Failed with errors:\n${error.errors.map(_.message).mkString("\n")}"),
            result => result mustBe date
          )
        }
      }
    }
    "fail" when {
      "Invalid day is entered" in {
        forAll(arbitrary[Date], intBetweenRange(35, 60)) { (date, day) =>

          Form(dateMapping).fillAndValidate(date.copy(day = Some(day))).fold(
            _.error("date.day") must haveMessage("Day is invalid"),
            _ => fail("Should not succeed")
          )
        }
      }
      "Invalid month is entered" in {
        forAll(arbitrary[Date], intBetweenRange(12, 30)) { (date, month) =>
          val invalidDate = date.copy(month = Some(month))
          Form(dateMapping).fillAndValidate(invalidDate).fold(
            _.error("date.month") must haveMessage("Month is invalid"),
            _ => fail("Should not succeed")
          )
        }
      }
      "Invalid year is entered" in {
        forAll(arbitrary[Date], intBetweenRange(1890, 1899)) { (date, year) =>
          val invalidDate = date.copy(year = Some(year))
          Form(dateMapping).fillAndValidate(invalidDate).fold(
            _.error("date.year") must haveMessage("Year is invalid"),
            _ => fail("Should not succeed")
          )
        }
      }
    }
  }

  "MeasureMapping" should {

    "bind" when {

      "valid values are passed" in {

        forAll { measure: Measure =>

          Form(measureMapping).fillAndValidate(measure).fold(
            e => fail(s"form should not fail: ${e.errors}"),
            success => success mustBe measure
          )
        }
      }
    }

    "fail" when {

      "unitCode is greater than 5" in {

        val badData = stringsLongerThan(5)
        forAll(arbitrary[Measure], badData) {
          (measure, unitCode) =>

            val data = measure.copy(unitCode = Some(unitCode))
            Form(measureMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Measurement Unit & Qualifier cannot be more than 5 characters"),
              _ => fail("form should not succeed")
            )
        }
      }

      "value has a precision greater than 16" in {

        forAll(arbitrary[Measure], decimal(17, 30, 0)) {
          (measure, deduction) =>

            val data = measure.copy(value = Some(deduction))
            Form(measureMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Quantity cannot be greater than 99999999999999.999999"),
              _ => fail("form should not succeed")
            )
        }
      }

      "value has a scale greater than 6" in {

        val badData = choose(7, 10).flatMap(posDecimal(10, _))

        forAll(arbitrary[Measure], badData) {
          (measure, deduction) =>

            val data = measure.copy(value = Some(deduction))
            Form(measureMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Quantity cannot have more than 6 decimal places"),
              _ => fail("form should not succeed")
            )
        }
      }

      "value is less than 0" in {

        forAll(arbitrary[Measure], intLessThan(0)) {
          (measure, deduction) =>

            val data = measure.copy(value = Some(BigDecimal(deduction)))
            Form(measureMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Quantity must not be negative"),
              _ => fail("form should not succeed")
            )
        }
      }

      "has a currency with no value" in {

        forAll { amount: Amount =>

          whenever(amount.currencyId.nonEmpty) {

            Form(amountMapping).bind(Map("currencyId" -> amount.currencyId.getOrElse(""))).fold(
              _ must haveErrorMessage("Amount is required when currency is provided"),
              _ => fail("form should not succeed")
            )
          }
        }
      }

      "has a value with no currency" in {

        forAll { amount: Amount =>

          whenever(amount.value.nonEmpty) {

            Form(amountMapping).bind(Map("value" -> amount.value.fold("")(_.toString))).fold(
              _ must haveErrorMessage("Currency is required when amount is provided"),
              _ => fail("form should not succeed")
            )
          }
        }
      }
    }
  }


  "GoodsItemsAdditionalDocumentsForm" should {

    "bind" when {

      "valid values are bound" in {

        forAll { additionalDocument: GovernmentAgencyGoodsItemAdditionalDocument =>

          Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument).fold(
            error => fail(s"Failed with errors:\n${error.errors.map(_.message).mkString("\n")}"),
            result => result mustBe additionalDocument
          )
        }
      }
    }
  }


  "fail when all fields are missing" when {
    "Category, Identifier, status, Status Reason, Issuing Authority, Date of Validity , Quantity, Measurement Unit & Qualifier" in {

      Form(govtAgencyGoodsItemAddDocMapping).bind(Map[String, String]()).fold(
        _ must haveErrorMessage("You must provide input for Category or Identifier or status or Status Reason or Issuing Authority and Date of Validity or Quantity and Measurement Unit & Qualifier"),
        _ => fail("Should not succeed")
      )
    }
  }
  "fail for invalid data conditions for all fields " when {
    "categoryCode length is greater than 1" in {

      forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], stringsLongerThan(2)) { (additionalDocument, invalidCode) =>
        Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(categoryCode = Some(invalidCode))).fold(
          _.error("categoryCode") must haveMessage("Category must be 1 character"),
          _ => fail("Should not succeed")
        )
      }
    }

    "Identifer length is greater than 35" in {

      forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], stringsLongerThan(35)) { (additionalDocument, invalidId) =>
        Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(id = Some(invalidId))).fold(
          _.error("id") must haveMessage("Identifier must be less than 35 characters"),
          _ => fail("Should not succeed")
        )
      }
    }
    "name length is greater than 35" in {

      forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], stringsLongerThan(35)) { (additionalDocument, invalidName) =>
        Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(name = Some(invalidName))).fold(
          _.error("name") must haveMessage("Status Reason must be less than 35 characters"),
          _ => fail("Should not succeed")
        )
      }
    }

    "Typecode length is greater than 3" in {

      forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], stringsLongerThan(3)) { (additionalDocument, invalidCode) =>
        Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(typeCode = Some(invalidCode))).fold(
          _.error("typeCode") must haveMessage("Type is only 3 characters"),
          _ => fail("Should not succeed")
        )
      }
    }

    "lpcoExemptionCode length is greater than 2" in {

      forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], stringsLongerThan(2)) { (additionalDocument, invalidId) =>
        Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(lpcoExemptionCode = Some(invalidId))).fold(
          _.error("lpcoExemptionCode") must haveMessage("Status must be 2 characters"),
          _ => fail("Should not succeed")
        )
      }
    }

    "submitter.name length is greater than 70" in {

      forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], stringsLongerThan(70)) { (additionalDocument, submitterName) =>
        Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(submitter = Some(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(
          Some(submitterName))))).fold(
          _.error("submitter.name") must haveMessage("Issuing Authority must be less than 70 characters"),
          _ => fail("Should not succeed")
        )
      }
    }
    "writeOff.quantity.unitCode length is greater than 5" in {

      forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], stringsLongerThan(5)) { (additionalDocument, unitCode) =>
        Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(writeOff = Some(WriteOff(Some(Measure(Some(unitCode))))))).fold(
          _.error("writeOff.quantity.unitCode") must haveMessage("Measurement Unit & Qualifier cannot be more than 5 characters"),
          _ => fail("Should not succeed")
        )
      }
    }
  }
}