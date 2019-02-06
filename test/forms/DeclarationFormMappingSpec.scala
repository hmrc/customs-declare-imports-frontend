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

import domain.{InvoiceAndCurrency, References}
import forms.DeclarationFormMapping._
import generators.Generators
import org.scalacheck.Arbitrary._
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

  "addAdditionalDocumentMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll(arbitrary[AdditionalDocument]) { arbitraryAddAdditionalDocument =>

          Form(additionalDocumentMapping).fillAndValidate(arbitraryAddAdditionalDocument).fold(
            error => fail(s"Failed with errors:\n${error.errors.map(_.message).mkString("\n")}"),
            result => result mustBe arbitraryAddAdditionalDocument
          )
        }
      }
    }

    "fail to bind" when {

      "id length is greater than 7" in {

        forAll(arbitrary[AdditionalDocument], intBetweenRange(9999999, Int.MaxValue)) { (arbitraryAdditionalDocument, invalidId) =>

          Form(additionalDocumentMapping).fillAndValidate(arbitraryAdditionalDocument.copy(id = Some(invalidId.toString))).fold(
            error => error.error("id") must haveMessage("Deferred Payment ID should be less than or equal to 7 characters"),
            _ => fail("Should not succeed")
          )
        }
      }

      "categoryCode length is greater than 1" in {

        forAll(arbitrary[AdditionalDocument], minStringLength(2)) { (arbitraryAdditionalDocument, invalidCategoryCode) =>

          Form(additionalDocumentMapping).fillAndValidate(arbitraryAdditionalDocument.copy(categoryCode = Some(invalidCategoryCode))).fold(
            error => error.error("categoryCode") must haveMessage("Deferred Payment Category should be less than or equal to 1 character"),
            _ => fail("Should not succeed")
          )
        }
      }

      "typeCode length is greater than 3" in {

        forAll(arbitrary[AdditionalDocument], minStringLength(4)) { (arbitraryAdditionalDocument, invalidTypeCode) =>

          Form(additionalDocumentMapping).fillAndValidate(arbitraryAdditionalDocument.copy(typeCode = Some(invalidTypeCode))).fold(
            error => error.error("typeCode") must haveMessage("Deferred Payment Type should be less than or equal to 3 characters"),
            _ => fail("Should not succeed")
          )
        }
      }

      "Deferred Payment ID, Deferred Payment Category, Deferred Payment Type are missing" in {

        Form(additionalDocumentMapping).bind(Map[String, String]()).fold(
          error => error must haveErrorMessage("You must provide a Deferred Payment ID or Deferred Payment Category or Deferred Payment Type"),
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

      forAll(arbitrary[GovernmentProcedure], minStringLength(3)) {
        (governmentProcedure, code) =>

          val data = governmentProcedure.copy(currentCode = Some(code))
          Form(governmentProcedureMapping).fillAndValidate(data).fold(
            _ must haveErrorMessage("Current code should be less than or equal to 2 characters"),
            _ => fail("should not succeed")
          )
      }
    }

    "previousCode is longer than 2 characters" in {

      forAll(arbitrary[GovernmentProcedure], minStringLength(3)) {
        (governmentProcedure, code) =>

          val data = governmentProcedure.copy(previousCode = Some(code))
          Form(governmentProcedureMapping).fillAndValidate(data).fold(
            _ must haveErrorMessage("Previous code  should be less than or equal to 2 characters"),
            _ => fail("should not succeed")
          )
      }
    }
  }

  "obligationGauranteeMapping" should {

    "bind" when {
      "valid values are bound" in {
        forAll { guarantee: GuaranteeType =>

          Form(guaranteeTypeMapping).fillAndValidate(guarantee.value).fold(
            _ => fail("form should not fail"),
            _ mustBe guarantee.value)
        }
      }
    }

    "fail" when {

      "security code is longer than 1 character" in {

        forAll { s: String =>

          whenever(s.length > 1) {

            Form(guaranteeTypeMapping).bind(Map("securityDetailsCode" -> s)).fold(
              _ must haveErrorMessage("Security details code must be 1 character"),
              _ => fail("form should not succeed")
            )
          }
        }
      }

      "security code has not been provided" in {

        Form(guaranteeTypeMapping).bind(Map[String, String]()).fold(
          _ must haveErrorMessage("Security details code is required"),
          _ => fail("form should not succeed")
        )
      }
    }
  }

  "currencyExchangeMapping" should {

    "bind" when {

      "valid values are bound" in {
        forAll { currencyExchange: CurrencyExchange =>

          Form(currencyExchangeMapping).fillAndValidate(currencyExchange).fold(
            _ => fail("Should not fail"),
            success => success mustBe currencyExchange
          )
        }
      }
    }

    "fail" when {
      "currencyTypeCode is not a currency" in {

        val badData = stringsExceptSpecificValues(config.Options.currencyTypes.map(_._2).toSet)

        forAll(arbitrary[CurrencyExchange], badData) { (currencyExchange, badData) =>

          val data = currencyExchange.copy(currencyTypeCode = Some(badData))
          Form(currencyExchangeMapping).fillAndValidate(data).fold(
            _ must haveErrorMessage("CurrencyTypeCode is not a valid currency"),
            _ => fail("Form should not succeed")
          )
        }
      }

      "rateNumeric has a precision greater than 12" in {

        forAll(arbitrary[CurrencyExchange], decimal(13, 30, 0)) {
          (currencyEnchange, invalidRateNumeric) =>

            val data = currencyEnchange.copy(rateNumeric = Some(invalidRateNumeric))

            Form(currencyExchangeMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("RateNumeric cannot be greater than 9999999.99999"),
              _ => fail("Form should not succeed")
            )
        }
      }

      "rateNumeric has a scale greater than 5" in {
        val badData = choose(6, 16).flatMap(posDecimal(12, _))
        forAll(arbitrary[CurrencyExchange], badData) {
          (currencyExchange, invalidRateNumeric) =>
            val data = currencyExchange.copy(rateNumeric = Some(invalidRateNumeric))

            Form(currencyExchangeMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("RateNumeric cannot have more than 5 decimal places"),
              _ => fail("Form should not fail")
            )
        }
      }

      "rateNumeric has a negative value" in {

        forAll(arbitrary[CurrencyExchange], intLessThan(0)) {
          (currencyExchange, invalidRateNumeric) =>
            val data = currencyExchange.copy(rateNumeric = Some(BigDecimal(invalidRateNumeric)))

            Form(currencyExchangeMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("RateNumeric must not be negative"),
              _ => fail("Form should not fail")
            )
        }
      }

      "has a currencyTypeCode with no rateNumeric" in {

        forAll { currencyExchange: CurrencyExchange =>

          whenever(currencyExchange.currencyTypeCode.nonEmpty) {

            Form(currencyExchangeMapping).bind(Map("currencyTypeCode" -> currencyExchange.currencyTypeCode.getOrElse(""))).fold(
              _ must haveErrorMessage("Exchange rate is required when currency is provided"),
              _ => fail("form should not succeed")
            )
          }
        }
      }

      "has a rateNumeric with no currencyTypeCode" in {

        forAll { currencyExchange: CurrencyExchange =>

          whenever(currencyExchange.rateNumeric.nonEmpty) {

            Form(currencyExchangeMapping).bind(Map("rateNumeric" -> currencyExchange.rateNumeric.fold("")(_.toString))).fold(
              _ must haveErrorMessage("Currency ID is required when amount is provided"),
              _ => fail("form should not succeed")
            )
          }
        }
      }
    }
  }

  "guaranteeTypeMapping" should {
    "bind" when {

      "valid values are bound" in {

        forAll(arbitrary[ObligationGuarantee]) { arbitraryObligationGuarantee =>

          Form(obligationGauranteeMapping).fillAndValidate(arbitraryObligationGuarantee).fold(
            error => fail(s"Failed with errors:\n${error.errors.map(_.message).mkString("\n")}"),
            result => result mustBe arbitraryObligationGuarantee
          )
        }
      }
    }

    "fail to bind" when {

      "reference id length is greater than 35" in {
        forAll(arbitrary[ObligationGuarantee], stringsLongerThan(35)) { (arbitraryObligationGuarantee, invalidReferenceId) =>

          Form(obligationGauranteeMapping).fillAndValidate(arbitraryObligationGuarantee.copy(referenceId = Some(invalidReferenceId))).fold(
            error => error.error("referenceId") must haveMessage("ReferenceId should be less than or equal to 35 characters"),
            _ => fail("Should not succeed")
          )
        }
      }

      "amount has a precision greater than 16" in {

        forAll(arbitrary[ObligationGuarantee], decimal(17, 30, 0)) {
          (arbitraryObligationGuarantee, invalidAmount) =>

            val data = arbitraryObligationGuarantee.copy(amount = Some(invalidAmount))
            Form(obligationGauranteeMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Amount cannot be greater than 99999999999999.99"),
              _ => fail("Should not succeed")
            )
        }
      }

      "amount has a scale greater than 2" in {

        val badData = choose(3, 10).flatMap(posDecimal(16, _))

        forAll(arbitrary[ObligationGuarantee], badData) {
          (arbitraryObligationGuarantee, invalidAmount) =>

            val data = arbitraryObligationGuarantee.copy(amount = Some(invalidAmount))
            Form(obligationGauranteeMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Amount cannot have more than 2 decimal places"),
              _ => fail("Should not succeed")
            )
        }
      }

      "amount is less than 0" in {

        forAll(arbitrary[ObligationGuarantee], intLessThan(0)) {
          (arbitraryObligationGuarantee, invalidAmount) =>

            val data = arbitraryObligationGuarantee.copy(amount = Some(BigDecimal(invalidAmount)))
            Form(obligationGauranteeMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Amount must not be negative"),
              _ => fail("Should not succeed")
            )
        }
      }

      "access code length is greater than 4" in {

        forAll(arbitrary[ObligationGuarantee], stringsLongerThan(4)) {
          (arbitraryObligationGuarantee, invalidAccessCode) =>

            Form(obligationGauranteeMapping).fillAndValidate(arbitraryObligationGuarantee.copy(accessCode = Some(invalidAccessCode))).fold(
              error => error.error("accessCode") must haveMessage("AccessCode should be less than or equal to 4 characters"),
              _     => fail("Should not succeed")
            )
        }
      }

      "guarantee office identifier length is greater than 8" in {

        forAll(arbitrary[ObligationGuarantee], stringsLongerThan(8)) {
          (arbitraryObligationGuarantee, invalidOfficeId) =>

            Form(obligationGauranteeMapping).fillAndValidate(arbitraryObligationGuarantee.copy(guaranteeOffice = Some(Office(Some(invalidOfficeId))))).fold(
              error => error.error("guaranteeOffice.id") must haveMessage("Office id should be less than or equal to 8 characters"),
              _ => fail("Should not succeed")
            )
        }
      }
    }
  }

  "addressMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { address: Address =>

          Form(addressMapping).fillAndValidate(address).fold(
            _ => fail("form should not fail"),
            _ mustBe address
          )
        }
      }
    }

    "fail" when {

      "cityName has more than 35 characters" in {

        forAll(arbitrary[Address], minStringLength(36)) {
          (address, cityName) =>

            val data = address.copy(cityName = Some(cityName))
            Form(addressMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("City name should be 35 characters or less"),
              _ => fail("form should not succeed")
            )
        }

      }

      "country code is not valid" in {

        forAll(arbitrary[Address], arbitrary[String]) {
          (address, countryCode) =>

            whenever(!config.Options.countryOptions.exists(_._1 == countryCode)) {

              val data = address.copy(countryCode = Some(countryCode))
              Form(addressMapping).fillAndValidate(data).fold(
                _ must haveErrorMessage("Country code is invalid"),
                _ => fail("form should not succeed")
              )
            }
        }
      }

      "country sub division code has more than 9 characters" in {

        forAll(arbitrary[Address], minStringLength(10)) {
          (address, code) =>

            val data = address.copy(countrySubDivisionCode = Some(code))
            Form(addressMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Country sub division code should be 9 characters or less"),
              _ => fail("form should not succeed")
            )
        }
      }

      "country sub division name has more than 70 characters" in {

        forAll(arbitrary[Address], minStringLength(36)) {
          (address, name) =>

            val data = address.copy(countrySubDivisionName = Some(name))
            Form(addressMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Country sub division name should be 35 characters or less"),
              _ => fail("form should not succeed")
            )
        }
      }

      "line has more than 70 characters" in {

        forAll(arbitrary[Address], minStringLength(71)) {
          (address, line) =>

            val data = address.copy(line = Some(line))
            Form(addressMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Line should be 70 characters or less"),
              _ => fail("form should not succeed")
            )
        }
      }

      "postcode has more than 9 characters" in {

        forAll(arbitrary[Address], minStringLength(10)) {
          (address, postcode) =>

            val data = address.copy(postcodeId = Some(postcode))
            Form(addressMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Postcode should be 9 characters or less"),
              _ => fail("form should not succeed")
            )
        }
      }
    }
  }

  "importExportPartyMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { party: ImportExportParty =>

          Form(importExportPartyMapping).fillAndValidate(party).fold(
            _ => fail("form should not fail"),
            _ mustBe party
          )
        }
      }
    }

    "fail" when {

      "name has more than 70 characters" in {

        forAll(arbitrary[ImportExportParty], minStringLength(71)) {
          (party, name) =>

            val data = party.copy(name = Some(name))
            Form(importExportPartyMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Name should have 70 characters or less"),
              _ => fail("form should not succeed")
            )
        }
      }

      "id has more than 17 characters" in {

        forAll(arbitrary[ImportExportParty], minStringLength(71)) {
          (party, id) =>

            val data = party.copy(id = Some(id))
            Form(importExportPartyMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("EORI number should have 17 characters or less"),
              _ => fail("form should not succeed")
            )
        }
      }
    }
  }

  "agentMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { agent: Agent =>

          Form(agentMapping).fillAndValidate(agent).fold(
            e => fail(s"form should not fail: $e"),
            _ mustBe agent
          )
        }
      }
    }

    "fail" when {

      "name has more than 70 characters" in {

        forAll(arbitrary[Agent], minStringLength(71)) {
          (agent, name) =>

            val data = agent.copy(name = Some(name))
            Form(agentMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Name should have 70 characters or less"),
              _ => fail("form should not succeed")
            )
        }
      }

      "id has more than 17 characters" in {

        forAll(arbitrary[Agent], minStringLength(71)) {
          (agent, id) =>

            val data = agent.copy(id = Some(id))
            Form(agentMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("EORI number should have 17 characters or less"),
              _ => fail("form should not succeed")
            )
        }
      }

      "functionCode is not in list" in {

        forAll(arbitrary[Agent], nonEmptyString.map(_.take(1))) {
          (agent, code) =>

            whenever(!config.Options.agentFunctionCodes.contains(code)) {

              val data = agent.copy(functionCode = Some(code))
              Form(agentMapping).fillAndValidate(data).fold(
                _ must haveErrorMessage("Status code is not valid"),
                _ => fail("form should not succeed")
              )
            }
        }
      }
    }
  }

  "referencesMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { references: References =>

          Form(referencesMapping).fillAndValidate(references).fold(
            e => fail(s"form should not fail: $e"),
            _ mustBe references
          )
        }
      }
    }

    "fail" when {

      "typeCode has more than 2 characters" in {

        forAll(arbitrary[References], minStringLength(3)) {
          (references, typeCode) =>

            val data = references.copy(typeCode = Some(typeCode))
            Form(referencesMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Declaration type must be 2 characters or less"),
              _ => fail("form should not succeed")
            )
        }
      }

      "typeCode has non alpha characters" in {

        forAll(arbitrary[References], nonAlphaString) {
          (references, typeCode) =>

            whenever(typeCode.nonEmpty) {

              val data = references.copy(typeCode = Some(typeCode.take(2)))
              Form(referencesMapping).fillAndValidate(data).fold(
                _ must haveErrorMessage("Declaration type must contains only A-Z characters"),
                _ => fail("form should not succeed")
              )
            }
        }
      }

      "typerCode has more than 1 character" in {

        forAll(arbitrary[References], minStringLength(2)) {
          (references, typerCode) =>

            val data = references.copy(typerCode = Some(typerCode))
            Form(referencesMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Additional declaration type must be a single character"),
              _ => fail("form should not succeed")
            )
        }
      }

      "typerCode has non alpha characters" in {

        forAll(arbitrary[References], nonAlphaString) {
          (references, typerCode) =>

            whenever(typerCode.nonEmpty) {

              val data = references.copy(typerCode = Some(typerCode.take(1)))
              Form(referencesMapping).fillAndValidate(data).fold(
                _ must haveErrorMessage("Additional declaration type must contains only A-Z characters"),
                _ => fail("form should not succeed")
              )
            }
        }
      }

      "traderAssignedReferenceId has more than 35 characters" in {

        forAll(arbitrary[References], minStringLength(36)) {
          (references, traderId) =>

            val data = references.copy(traderAssignedReferenceId = Some(traderId))
            Form(referencesMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Reference Number/UCR must be 35 characters or less"),
              _ => fail("form should not succeed")
            )
        }
      }

      "functionalReferenceId has more than 22 characters" in {

        forAll(arbitrary[References], minStringLength(23)) {
          (references, refId) =>

            val data = references.copy(functionalReferenceId = Some(refId))
            Form(referencesMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("LRN must be 22 characters or less"),
              _ => fail("form should not succeed")
            )
        }
      }

      "transactionNatureCode contains more than 2 digits" in {

        forAll(arbitrary[References], intOutsideRange(-9, 99)) {
          (references, natureCode) =>

            val data = references.copy(transactionNatureCode = Some(natureCode))
            Form(referencesMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Nature of transaction must be contain 2 digits or less"),
              _ => fail("form should not succeed")
            )
        }
      }
    }
  }

  "invoiceAndCurrencyMapping" should {

    "bind" when {

      "valid values are bound" in {
        forAll { invoiceAndCurrency: InvoiceAndCurrency =>

          Form(invoiceAndCurrencyMapping).fillAndValidate(invoiceAndCurrency).fold(
            e => fail("Form should not fail"),
            _ mustBe invoiceAndCurrency
          )
        }
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

        forAll(arbitrary[Date], intGreaterThan(31)) { (date, day) =>

          Form(dateMapping).fillAndValidate(date.copy(day = day)).fold(
            _.error("day") must haveMessage("Day is invalid"),
            _ => fail("Should not succeed")
          )
        }
      }

      "Invalid month is entered" in {

        forAll(arbitrary[Date], intGreaterThan(12)) { (date, month) =>

          val invalidDate = date.copy(month = month)
          Form(dateMapping).fillAndValidate(invalidDate).fold(
            _.error("month") must haveMessage("Month is invalid"),
            _ => fail("Should not succeed")
          )
        }
      }

      "Invalid year is entered" in {

        forAll(arbitrary[Date], intLessThan(1900)) { (date, year) =>

          val invalidDate = date.copy(year = year)
          Form(dateMapping).fillAndValidate(invalidDate).fold(
            _.error("year") must haveMessage("Year is invalid"),
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

        val badData = minStringLength(6)
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

        forAll(arbitrary[Measure], decimal(16, 30, 0)) {
          (measure, deduction) =>

            val data = measure.copy(value = Some(deduction))
            Form(measureMapping).fillAndValidate(data).fold(
              _ must haveErrorMessage("Quantity cannot be greater than 9999999999.999999"),
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

        forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], minStringLength(3)) { (additionalDocument, invalidCode) =>
          Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(categoryCode = Some(invalidCode))).fold(
            _.error("categoryCode") must haveMessage("Category must be 1 character"),
            _ => fail("Should not succeed")
          )
        }
      }

      "identifier length is greater than 35" in {

        forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], minStringLength(36)) { (additionalDocument, invalidId) =>
          Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(id = Some(invalidId))).fold(
            _.error("id") must haveMessage("Identifier must be less than 35 characters"),
            _ => fail("Should not succeed")
          )
        }
      }

      "name length is greater than 35" in {

        forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], minStringLength(36)) { (additionalDocument, invalidName) =>
          Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(name = Some(invalidName))).fold(
            _.error("name") must haveMessage("Status Reason must be less than 35 characters"),
            _ => fail("Should not succeed")
          )
        }
      }

      "typecode length is greater than 3" in {

        forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], minStringLength(4)) { (additionalDocument, invalidCode) =>
          Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(typeCode = Some(invalidCode))).fold(
            _.error("typeCode") must haveMessage("Type is only 3 characters"),
            _ => fail("Should not succeed")
          )
        }
      }

      "lpcoExemptionCode length is greater than 2" in {

        forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], minStringLength(3)) { (additionalDocument, invalidId) =>
          Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(lpcoExemptionCode = Some(invalidId))).fold(
            _.error("lpcoExemptionCode") must haveMessage("Status must be 2 characters"),
            _ => fail("Should not succeed")
          )
        }
      }
      "lpcoExemptionCode length is 2 but not alpha characters" in {

        forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], nonAlphaString) { (additionalDocument, invalidId) =>
          whenever(invalidId.nonEmpty) {
            Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(lpcoExemptionCode = Some(invalidId.take(2)))).fold(
              _.error("lpcoExemptionCode") must haveMessage("Status must be 2 characters"),
              _ => fail("Should not succeed")
            )
          }
        }
      }

      "submitter.name length is greater than 70" in {

        forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], minStringLength(71)) { (additionalDocument, submitterName) =>
          Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(submitter = Some(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(
            Some(submitterName))))).fold(
            _.error("submitter.name") must haveMessage("Issuing Authority must be less than 70 characters"),
            _ => fail("Should not succeed")
          )
        }
      }

      "writeOff.quantity.unitCode length is greater than 5" in {

        forAll(arbitrary[GovernmentAgencyGoodsItemAdditionalDocument], minStringLength(6)) { (additionalDocument, unitCode) =>
          Form(govtAgencyGoodsItemAddDocMapping).fillAndValidate(additionalDocument.copy(writeOff = Some(WriteOff(Some(Measure(Some(unitCode))))))).fold(
            _.error("writeOff.quantity.unitCode") must haveMessage("Measurement Unit & Qualifier cannot be more than 5 characters"),
            _ => fail("Should not succeed")
          )
        }
      }
    }
  }
}