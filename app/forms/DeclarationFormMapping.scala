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

import domain.GoodsItemValueInformation
import play.api.data.Forms._
import uk.gov.hmrc.wco.dec._

object DeclarationFormMapping {

  val govAgencyGoodsItemAddDocumentSubmitterMapping = mapping(
    "name" -> optional(text),
    "roleCode" -> optional(text.verifying("roleCode is only 3 characters", _.length <= 3))
  )(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter.apply)(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter.unapply)

  val amountMapping = mapping(
    "currencyId" -> optional(
      text
        .verifying("Currency ID is not a valid currency", x => config.Options.currencyTypes.exists(_._1 == x))),
    "value" -> optional(
      bigDecimal
        .verifying("Amount cannot be greater than 9999999999999999", _.precision <= 16)
        .verifying("Amount cannot have more than 2 decimal places", _.scale <= 2)
        .verifying("Amount must not be negative", _ >= 0))
  )(Amount.apply)(Amount.unapply)

  val measureMapping = mapping("unitCode" -> optional(text.verifying("unitCode is only 5 characters", _.length <= 5)),
    "value" -> optional(bigDecimal.verifying("value must not be negative", a => a > 0)))(Measure.apply)(Measure.unapply)

  val writeOffMapping = mapping("quantity" -> optional(measureMapping), "amount" -> optional(amountMapping))(WriteOff.apply)(WriteOff.unapply)

  val govtAgencyGoodsItemAddDocMapping = mapping(
    "categoryCode" -> optional(text.verifying("category code is only 3 characters", _.length <= 3)),
    "effectiveDateTime" -> ignored[Option[DateTimeElement]](None),
    "id" -> optional(text),
    "name" -> optional(text),
    "typeCode" -> optional(text.verifying("typeCode is only 3 characters", _.length <= 3)),
    "lpcoExemptionCode" -> optional(text.verifying("lpcoExemptionCode is only 3 characters", _.length <= 3)),
    "submitter" -> optional(govAgencyGoodsItemAddDocumentSubmitterMapping),
    "writeOff" -> optional(writeOffMapping)
  )(GovernmentAgencyGoodsItemAdditionalDocument.apply)(GovernmentAgencyGoodsItemAdditionalDocument.unapply)

  lazy val additionalInformationMapping = mapping(
    "statementCode" -> optional(text
      .verifying("statement code should be less than or equal to 17 characters", _.length <= 17)),
    "statementDescription" -> optional(text.verifying("statement description should be less than or equal to 512 characters", _.length <= 512)),
    "limitDateTime" -> ignored[Option[String]](None),
    "statementTypeCode" -> optional(text.verifying("statement type code should be less than or equal to 3 characters", _.length <= 3)),
    "pointers" -> ignored[Seq[Pointer]](Seq.empty)
  )(AdditionalInformation.apply)(AdditionalInformation.unapply)

  val destinationMapping = mapping("countryCode" -> optional(text.verifying("country code is only 3 characters", _.length <= 3)),
    "regionId" -> optional(text.verifying("regionId code is only 9 characters", _.length <= 9)))(Destination.apply)(Destination.unapply)

  val ucrMapping = mapping("id" -> optional(text.verifying("id should be less than or equal to 35 characters", _.length <= 35)),
    "traderAssignedReferenceId" -> optional(text.verifying("traderAssignedReferenceId should be less than or equal to 35 characters", _.length <= 35)))(Ucr.apply)(Ucr.unapply)

  val exportCountryMapping = mapping("id" -> text.verifying("export Country code should be less than or equal to 2 characters",
    _.length <= 2))(ExportCountry.apply)(ExportCountry.unapply)

  val valuationAdjustmentMapping = mapping("additionCode" -> optional(
    text.verifying("valuationAdjustment should be less than or equal to 4 characters",
      _.length <= 4)))(ValuationAdjustment.apply)(ValuationAdjustment.unapply)

  val goodsItemValueInformationMapping = mapping(
    "customsValueAmount" -> optional(bigDecimal.verifying("customs Value Amount must not be negative", a => a > 0)),
    "sequenceNumeric" -> number(0, 99999),
    "statisticalValueAmount" -> optional(amountMapping),
    "transactionNatureCode" -> optional(number(0, 99999)),
    "destination" -> optional(destinationMapping),
    "ucr" -> optional(ucrMapping),
    "exportCountry" -> optional(exportCountryMapping),
    "valuationAdjustment" -> optional(valuationAdjustmentMapping))(GoodsItemValueInformation.apply)(GoodsItemValueInformation.unapply)


  val addressMapping = mapping(
    "cityName" -> optional(text.verifying("id should be less than or equal to 35 characters", _.length <= 35)), // max length 35
    "countryCode" -> optional(text.verifying("country Code should be less 2 characters", _.length <= 2)), // 2 chars [a-zA-Z] ISO 3166-1 2-alpha
    "countrySubDivisionCode" -> optional(text.verifying("countrySubDivisionCode should be less than or equal to 9 characters", _.length <= 9)), // max 9 chars
    "countrySubDivisionName" -> optional(text.verifying("countrySubDivisionName should be less than or equal to 35 characters", _.length <= 35)), // max 35 chars
    "line" -> optional(text.verifying("line should be less than or equal to 70 characters", _.length <= 70)), //:max 70 chars
    "postcodeId" -> optional(text.verifying("postcode should be less than or equal to 9 characters", _.length <= 9)) // max 9 chars
  )(Address.apply)(Address.unapply)

  val namedEntityWithAddressMapping = mapping(
    "name" -> optional(text.verifying("name should be less than or equal to 70 characters", _.length <= 70)), //: Option[String] = None, // max 70 chars
    "id" -> optional(text.verifying("id  should be less than or equal to 17 characters", _.length <= 17)), // max 17 chars
    "address" -> optional(addressMapping)
  )(NamedEntityWithAddress.apply)(NamedEntityWithAddress.unapply)

  val roleBasedPartyMapping = mapping(
    "id" -> optional(text.verifying("Role based party id should be less than or equal to 17 characters", _.length <= 17)), // max 17 chars
    "roleCode" -> optional(text.verifying("Role code should be less than or equal to 3 characters", _.length <= 3)) // max 3 chars
  )(RoleBasedParty.apply)(RoleBasedParty.unapply)
    .verifying("You must provide an ID or role code", require1Field[RoleBasedParty](_.id, _.roleCode))

  val governmentProcedureMapping = mapping(
    "currentCode" -> optional(text.verifying("current Code  should be less than or equal to 7 characters", _.length <= 7)), // max 7 chars
    "previousCode" -> optional(text.verifying("previous Code  should be less than or equal to 7 characters", _.length <= 7)) // max 7 chars
  )(GovernmentProcedure.apply)(GovernmentProcedure.unapply)

  val originMapping = mapping(
    "countryCode" -> optional(text.verifying("country Code  should be max of 4 characters", _.length <= 4)), // max 4 chars //expects ISO-3166-1 alpha2 code
    "regionId" -> optional(text.verifying("regionId code should be 9 characters", _.length <= 9)),
    "typeCode" -> optional(text.verifying("typeCode  should be 3 characters", _.length <= 7)) // max 3 chars
  )(Origin.apply)(Origin.unapply)


  val packagingMapping =
    mapping("sequenceNumeric" -> optional(number(0, 99999)), //: Option[Int] = None, // unsigned max 99999
      "marksNumbersId" -> optional(text.verifying("marks Numbers Id should be less than or equal to 512 characters", _.length <= 512)), //: Option[String] = None, // max 512 chars
      "quantity" -> optional(number(0, 99999)), //: Option[Int] = None, // max 99999999
      "typeCode" -> optional(text.verifying("type Code  should be 2 characters", _.length == 2)), //: Option[String] = None, // max 2 chars
      "packingMaterialDescription" -> optional(text.verifying("packing Material Description should be less than or equal to 256 characters", _.length <= 256)), // Option[String] = None, // max 256 chars
      "lengthMeasure" -> optional(longNumber), //: Option[Long] = None, // unsigned int max 999999999999999
      "widthMeasure" -> optional(longNumber), //: Option[Long] = None, // unsigned int max 999999999999999
      "heightMeasure" -> optional(longNumber), //: Option[Long] = None, // unsigned int max 999999999999999
      "volumeMeasure" -> optional(measureMapping))(Packaging.apply)(Packaging.unapply)

  val contactMapping = mapping("name" -> optional(text.verifying("name should be less than or equal to 70 characters", _.length <= 70)) //: Option[String] = None, // max 70 chars
  )(Contact.apply)(Contact.unapply)

  val communicationMapping = mapping(
    "id" -> optional(text.verifying("communication Id should be less than or equal to 70 characters", _.length <= 50)), //: Option[String] = None, // max 50 chars
    "typeCode" -> optional(text.verifying("type Code  should be 3 characters", _.length <= 3)) //: Option[String] = None, // max 3 chars
  )(Communication.apply)(Communication.unapply)

  val importExportPartyMapping = mapping(
    "name" -> optional(text.verifying(" Import Export name should be less than or equal to 70 characters", _.length <= 70)), //: Option[String] = None, // max 70 chars
    "id" -> optional(text.verifying(" Import Export party Id should be less than or equal to 70 characters", _.length <= 17)), //: Option[String] = None, // max 17 chars
    "address" -> optional(addressMapping),
    "contacts" -> seq(contactMapping),
    "communications" -> seq(communicationMapping))(ImportExportParty.apply)(ImportExportParty.unapply)

  val chargeDeductionMapping = mapping(
    "chargesTypeCode" -> optional(text.verifying("Charges code should be less than or equal to 3 characters", _.length <= 3)),
    "otherChargeDeductionAmount" -> optional(amountMapping) // Option[Amount] = None
  )(ChargeDeduction.apply)(ChargeDeduction.unapply)
    .verifying("Charges code, currency id or amount are required", require1Field[ChargeDeduction](_.chargesTypeCode, _.otherChargeDeductionAmount))


  val customsValuationMapping = mapping("methodCode" -> optional(text.verifying(" Charges code should be less than or equal to 3 characters", _.length <= 3)), // max 3 chars; not valid outside GovernmentAgencyGoodsItem
    "freightChargeAmount" -> optional(bigDecimal), // default(bigDecimal, None),
    "chargeDeductions" -> seq(chargeDeductionMapping))(CustomsValuation.apply)(CustomsValuation.unapply)


  val officeMapping = mapping("id" -> optional(text.verifying("Office id should be less than or equal to 35 characters",
    _.length <= 35)))(Office.apply)(Office.unapply)


  val obligationGauranteeMapping =
    mapping("amount" -> optional(bigDecimal.verifying("Amount must not be negative", a => a > 0)),
      "id" -> optional(text.verifying("Id should be less than or equal to 35 characters", _.length <= 70)),
      "referenceId" -> optional(text.verifying("ReferenceId should be less than or equal to 35 characters", _.length <= 35)),
      "securityDetailsCode" -> optional(text.verifying("SecurityDetailsCode should be less than or equal to 3 characters", _.length <= 3)),
      "accessCode" -> optional(text.verifying("AccessCode should be less than or equal to 4 characters", _.length <= 4)),
      "guaranteeOffice" -> optional(officeMapping))(ObligationGuarantee.apply)(ObligationGuarantee.unapply)

 val guaranteesFormMapping = mapping("guarantees" -> seq(obligationGauranteeMapping))(ObligationGuaranteeForm.apply)(ObligationGuaranteeForm.unapply)

  val authorisationHolderMapping =
    mapping(
      "id" ->
        optional(text.verifying("ID should be less than or equal to 17 characters", _.length <= 17)),
      "categoryCode" ->
        optional(text.verifying("Category Code should be less than or equal to 4 characters", _.length <= 4))
    )(AuthorisationHolder.apply)(AuthorisationHolder.unapply)
      .verifying("You must provide an ID or category code", require1Field[AuthorisationHolder](_.id, _.categoryCode))

  val previousDocumentMapping = mapping(
    "categoryCode" -> optional(text.verifying("Document Category  should be less than or equal to 1 character", _.length <= 1)), //: Option[String] = None, // max 3 chars
    "id" -> optional(text.verifying("Document Reference should be less than or equal to 35 characters", _.length <= 35)), //: Option[String] = None, // max 70 chars
    "typeCode" -> optional(text.verifying("Previous Document Type should be less than or equal to 3 characters", _.length <= 3)), //: Option[String] = None, // max 3 chars
    "lineNumeric" -> optional(number
      .verifying("Goods Item Identifier should be greater than 0 and less than or equal to 999", lineNumeric => (lineNumeric > 0 && lineNumeric <= 999))) //: Option[Int] = None, // max 99999999
  )(PreviousDocument.apply)(PreviousDocument.unapply)
    .verifying("You must provide a Document Category or Document Reference or Previous Document Type or Goods Item Identifier", require1Field[PreviousDocument](_.categoryCode, _.id, _.typeCode, _.lineNumeric))

  def require1Field[T](fs: (T => Option[_])*): T => Boolean =
    t => fs.exists(f => f(t).nonEmpty)
}

case class ObligationGuaranteeForm (guarantees: Seq[ObligationGuarantee] = Seq.empty)


