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

package generators

import domain.{GoodsItemValueInformation, GovernmentAgencyGoodsItem}
import forms.ObligationGuaranteeForm
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.{Arbitrary, Gen, Shrink}
import uk.gov.hmrc.wco.dec._

trait Generators extends SignedInUserGen with ViewModelGenerators {

  implicit val dontShrinkStrings: Shrink[String] = Shrink.shrinkAny
  implicit val dontShrinkDecimals: Shrink[BigDecimal] = Shrink.shrinkAny

  def genIntersperseString(gen: Gen[String], value: String, frequencyV: Int = 1, frequencyN: Int = 10): Gen[String] = {

    val genValue: Gen[Option[String]] = Gen.frequency(frequencyN -> None, frequencyV -> Gen.const(Some(value)))

    for {
      seq1 <- gen
      seq2 <- Gen.listOfN(seq1.length, genValue)
    } yield {
      seq1.toSeq.zip(seq2).foldRight("") {
        case ((n, Some(v)), m) =>
          m + n + v
        case ((n, _), m) =>
          m + n
      }
    }
  }

  def intsInRangeWithCommas(min: Int, max: Int): Gen[String] = {
    val numberGen = choose[Int](min, max)
    genIntersperseString(numberGen.toString, ",")
  }

  def intsLargerThanMaxValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (x => x > Int.MaxValue)

  def intsSmallerThanMinValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (x => x < Int.MinValue)

  def nonNumerics: Gen[String] =
    alphaStr suchThat (_.size > 0)

  def decimals: Gen[String] =
    arbitrary[BigDecimal]
      .suchThat(_.abs < Int.MaxValue)
      .suchThat(!_.isValidInt)
      .map(_.formatted("%f"))

  def intsBelowValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ < value)

  def intsAboveValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ > value)

  def intsOutsideRange(min: Int, max: Int): Gen[Int] =
    arbitrary[Int] suchThat (x => x < min || x > max)

  def nonBooleans: Gen[String] =
    arbitrary[String]
      .suchThat(_.nonEmpty)
      .suchThat(_ != "true")
      .suchThat(_ != "false")

  def nonEmptyString: Gen[String] =
    arbitrary[String] suchThat (_.nonEmpty)

  def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars <- listOfN(length, arbitrary[Char])
    } yield chars.mkString

  def stringsLongerThan(minLength: Int): Gen[String] =
    arbitrary[String] suchThat (_.length > minLength)

  def stringsExceptSpecificValues(excluded: Set[String]): Gen[String] =
    nonEmptyString suchThat (!excluded.contains(_))

  def currencyGen: Gen[String] = oneOf(config.Options.currencyTypes.map(_._2))

  implicit val arbitraryOffice: Arbitrary[Office] = Arbitrary {
    for {
      id <- option(arbitrary[String].map(_.take(17)))
    } yield Office(id)
  }

  implicit val arbitraryObligationGuarantee: Arbitrary[ObligationGuarantee] = Arbitrary {
    for {
      amount <- option(arbitrary[BigDecimal].map(_.max(9999999999999999.99999)))
      id <- option(arbitrary[String].map(_.take(70)))
      referenceId <- option(arbitrary[String].map(_.take(35)))
      securityDetailsCode <- option(arbitrary[String].map(_.take(3)))
      accessCode <- option(arbitrary[String].map(_.take(4)))
      office <- option(arbitraryOffice.arbitrary)
    } yield ObligationGuarantee(amount, id, referenceId, securityDetailsCode, accessCode, office)
  }

  implicit val arbitraryObligationGuaranteeForm: Arbitrary[ObligationGuaranteeForm] = Arbitrary {
    for {
      guarantees <- Gen.listOfN(1, arbitraryObligationGuarantee.arbitrary)
    } yield ObligationGuaranteeForm(guarantees)
  }

  implicit val arbitraryAdditionalInfo: Arbitrary[AdditionalInformation] = Arbitrary {
    for {
      statementCode <- option(arbitrary[String].map(_.take(5)))
      statementDescription <- option(arbitrary[String].map(_.take(512)))
    } yield AdditionalInformation(statementCode, statementDescription)
  }

  implicit val arbitraryAuthorisationHolder: Arbitrary[AuthorisationHolder] = Arbitrary {
    for {
      id <- option(alphaNumStr.map(_.take(17)))
      categoryCode <- option(alphaNumStr.map(_.take(4)))
      if id.exists(_.nonEmpty) || categoryCode.exists(_.nonEmpty)
    } yield AuthorisationHolder(id, categoryCode)
  }

  implicit val arbitraryMeasure: Arbitrary[Measure] = Arbitrary {
    for {
      unitCode <- option(arbitrary[String].map(_.take(5)))
      value <- option(arbitrary[BigDecimal].map(_.max(9999999999999999.99999)))
    } yield Measure(unitCode, value)
  }

  implicit val arbitraryAmount: Arbitrary[Amount] = Arbitrary {
    val amount = for {
      currencyId <- currencyGen
      value <- posDecimal(16, 2)
    } yield Amount(Some(currencyId), Some(value))

    option(amount).map(_.fold(Amount(None, None))(identity))
  }

  implicit val arbitraryWriteOff: Arbitrary[WriteOff] = Arbitrary {
    for {
      quantity <- option(arbitraryMeasure.arbitrary)
      amount <- option(arbitraryAmount.arbitrary)
    } yield WriteOff(quantity, amount)
  }

  implicit val arbitraryPreviousDocument: Arbitrary[PreviousDocument] = Arbitrary {
    for {
      categoryCode <- option(arbitrary[String].map(_.take(1)))
      id <- option(arbitrary[String].map(_.take(35)))
      typeCode <- option(arbitrary[String].map(_.take(3)))
      lineNumeric <- option(intBetweenRange(1, 999))
      if categoryCode.nonEmpty || id.nonEmpty || typeCode.nonEmpty || lineNumeric.nonEmpty
    } yield PreviousDocument(categoryCode, id, typeCode, lineNumeric)
  }

  implicit val arbitraryOrigin: Arbitrary[Origin] = Arbitrary {
    for {
      countryCode <- option(arbitrary[String].map(_.take(4)))
      regionId <- option(arbitrary[String].map(_.take(9)))
      typeCode <- option(arbitrary[String].map(_.take(3)))
    } yield Origin(countryCode, regionId, typeCode)
  }

  implicit val arbitraryRoleBasedParty: Arbitrary[RoleBasedParty] = Arbitrary {
    for {
      id <- option(arbitrary[String].map(_.take(17)))
      roleCode <- option(arbitrary[String].map(_.take(3)))
      if id.exists(_.nonEmpty) || roleCode.exists(_.nonEmpty)
    } yield RoleBasedParty(id, roleCode)
  }

  implicit val arbitraryGovernmentAgencyGoodsItemAdditionalDocumentSubmitter:
    Arbitrary[GovernmentAgencyGoodsItemAdditionalDocumentSubmitter] = Arbitrary {
    for {
      name <- option(arbitrary[String].map(_.take(70)))
      roleCode <- option(arbitrary[String].map(_.take(3)))
    } yield GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(name, roleCode)
  }

  implicit val arbitraryGovernmentProcedure: Arbitrary[GovernmentProcedure] = Arbitrary {
    for {
      currentCode <- option(arbitrary[String].map(_.take(2)))
      previousCode <- option(arbitrary[String].map(_.take(2)))
      if currentCode.exists(_.nonEmpty) || previousCode.exists(_.nonEmpty)
    } yield GovernmentProcedure(currentCode, previousCode)
  }

  implicit val arbitraryGovernmentAgencyGoodsItemAdditionalDocument: Arbitrary[GovernmentAgencyGoodsItemAdditionalDocument] = Arbitrary {
    for {
      categoryCode <- option(arbitrary[String].map(_.take(3)))
      effectiveDateTime <- option(arbitraryDateTimeElement.arbitrary)
      id <- option(arbitrary[String].map(_.take(70)))
      name <- option(arbitrary[String].map(_.take(35)))
      typeCode <- option(arbitrary[String].map(_.take(3)))
      lpcoExemptionCode <- option(arbitrary[String].map(_.take(3)))
      submitter <- option(arbitraryGovernmentAgencyGoodsItemAdditionalDocumentSubmitter.arbitrary)
      writeOff <- option(arbitraryWriteOff.arbitrary)
    } yield GovernmentAgencyGoodsItemAdditionalDocument(categoryCode, effectiveDateTime, id, name, typeCode, lpcoExemptionCode, submitter, writeOff)
  }


  implicit val arbitraryAddress: Arbitrary[Address] = Arbitrary {
    for {
      cityName <- option(arbitrary[String].map(_.take(35))) // max length 35
      countryCode <- option(arbitrary[String].map(_.take(2))) // 2 chars [a-zA-Z] ISO 3166-1 2-alpha
      countrySubDivisionCode <- option(arbitrary[String].map(_.take(9))) // max 9 chars
      countrySubDivisionName <- option(arbitrary[String].map(_.take(35))) // max 35 chars
      line <- option(arbitrary[String].map(_.take(70))) // max 70 chars
      postcodeId <- option(arbitrary[String].map(_.take(9))) // max 9 chars
    } yield Address(cityName, countryCode, countrySubDivisionCode, countrySubDivisionName, line, postcodeId)
  }

  implicit val arbitraryNamedEntityWithAddress: Arbitrary[NamedEntityWithAddress] = Arbitrary {
    for {
      name <- option(arbitrary[String].map(_.take(70)))
      id <- option(arbitrary[String].map(_.take(17)))
      address <- option(arbitraryAddress.arbitrary)
    } yield NamedEntityWithAddress(name, id, address)
  }

  implicit val arbitraryPackaging: Arbitrary[Packaging] = Arbitrary {
    for {
      sequenceNumeric <- option(arbitrary[Int].map(_.max(99999)))
      marksNumbersId <- option(arbitrary[String].map(_.take(512)))
      quantity <- option(arbitrary[Int].map(_.max(99999)))
      typeCode <- option(arbitrary[String].map(_.take(2)))
      packingCode <- option(arbitrary[String].map(_.take(256)))
      lengthMeasure <- option(arbitrary[Long].map(_.max(999999999)))
      widthMeasure <- option(arbitrary[Long].map(_.max(999999999)))
      heightMeasure <- option(arbitrary[Long].map(_.max(999999999)))
      volumeMeasure <- option(arbitraryMeasure.arbitrary)
    } yield Packaging(sequenceNumeric, marksNumbersId, quantity, typeCode, packingCode,
      lengthMeasure, widthMeasure, heightMeasure, volumeMeasure)
  }

  implicit val arbitraryDestination: Arbitrary[Destination] = Arbitrary {
    for {
      countryCode <- option(arbitrary[String].map(_.take(3)))
      regionId <- option(arbitrary[String].map(_.take(9)))
    } yield Destination(countryCode, regionId)
  }

  implicit val arbitraryUcr: Arbitrary[Ucr] = Arbitrary {
    for {
      id <- option(arbitrary[String].map(_.take(35)))
      traderAssignedReferenceId <- option(arbitrary[String].map(_.take(35)))
    } yield Ucr(id, traderAssignedReferenceId)
  }

  implicit val arbitraryExportCountry: Arbitrary[ExportCountry] = Arbitrary {
    for {
      id <- arbitrary[String].map(_.take(2)) // either "102" or "304"
    } yield ExportCountry(id)
  }

  implicit val arbitraryDateTimeString: Arbitrary[DateTimeString] = Arbitrary {
    for {
      formatCode <- arbitrary[String].map(_.take(2))
      value <- arbitrary[String].map(_.take(35))
    } yield DateTimeString(formatCode, value)
  }

  implicit val arbitraryDateTimeElement: Arbitrary[DateTimeElement] = Arbitrary {
    for {
      dateTimeString <- arbitraryDateTimeString.arbitrary
    } yield DateTimeElement(dateTimeString)
  }

  implicit val arbitraryValuationAdjustment: Arbitrary[ValuationAdjustment] = Arbitrary {
    for {
      additionCode <- option(arbitrary[String].map(_.take(4))) // TODO : Set specfic values i.e. one of 145, 146, 147, 148, 149
    } yield ValuationAdjustment(additionCode)
  }

  implicit val arbitraryGoodsItemValueInformation: Arbitrary[GoodsItemValueInformation] = Arbitrary {
    for {
      customsValueAmount <- option(arbitrary[BigDecimal].map(_.max(9999999999999999.99999)))
      sequenceNumeric <- arbitrary[Int].map(_.max(99999))
      statisticalValueAmount <- option(arbitraryAmount.arbitrary)
      transactionNatureCode <- option(arbitrary[Int].map(_.max(99)))
      destination <- option(arbitraryDestination.arbitrary)
      ucr <- option(arbitraryUcr.arbitrary)
      exportCountry <- option(arbitraryExportCountry.arbitrary)
      valuationAdjustment <- option(arbitraryValuationAdjustment.arbitrary)
    } yield GoodsItemValueInformation(customsValueAmount, sequenceNumeric,
      statisticalValueAmount, transactionNatureCode, destination, ucr, exportCountry, valuationAdjustment)
  }

  implicit val arbitraryGovernmentAgencyGoodsItem: Arbitrary[GovernmentAgencyGoodsItem] = Arbitrary {
    for {
      goodsItemValue <- option(arbitraryGoodsItemValueInformation.arbitrary)
      additionalDocuments <- Gen.listOf(arbitraryGovernmentAgencyGoodsItemAdditionalDocument.arbitrary)
      additionalInformations <- Gen.listOfN(1, arbitraryAdditionalInfo.arbitrary)
      aeoMutualRecognitionParties <- Gen.listOfN(1, arbitraryRoleBasedParty.arbitrary)
      domesticParties <- Gen.listOfN(1, arbitraryRoleBasedParty.arbitrary)
      governmentProcedures <- Gen.listOfN(1, arbitraryGovernmentProcedure.arbitrary)
      manufacturers <- Gen.listOfN(1, arbitraryNamedEntityWithAddress.arbitrary)
      origins <- Gen.listOfN(1, arbitraryOrigin.arbitrary)
      packagings <- Gen.listOfN(1, arbitraryPackaging.arbitrary)
      previousDocuments <- Gen.listOfN(1, arbitraryPreviousDocument.arbitrary)
    } yield GovernmentAgencyGoodsItem(goodsItemValue, additionalDocuments, additionalInformations, aeoMutualRecognitionParties,
      domesticParties, governmentProcedures, manufacturers, origins, packagings, previousDocuments)
  }

  implicit val arbitraryTransportEquipment = Arbitrary {
    stringsWithMaxLength(17)
      .suchThat(_.nonEmpty)
      .map(s => TransportEquipment(0, Some(s)))
  }

  implicit val arbitraryChargeDeduction: Arbitrary[ChargeDeduction] = Arbitrary {
    for {
      typeCode <- option(arbitrary[String].map(_.take(2)))
      amount   <- option(arbitrary[Amount])
      if typeCode.exists(_.nonEmpty) || amount.exists(a => a.value.nonEmpty || a.currencyId.nonEmpty)
    } yield {
      ChargeDeduction(typeCode, amount)
    }
  }

  def intGreaterThan(min: Int): Gen[Int] =
    choose(min + 1, Int.MaxValue)

  def intLessThan(max: Int): Gen[Int] =
    choose(Int.MinValue, max - 1)

  def intBetweenRange(min: Int, max: Int): Gen[Int] =
    choose(min, max)

  def posDecimal(precision: Int, scale: Int): Gen[BigDecimal] =
    decimal(0, precision, scale)

  implicit val chooseBigInt: Choose[BigInt] =
    Choose.xmap[Long, BigInt](BigInt(_), _.toLong)

  def decimal(minSize: Int, maxSize: Int, scale: Int): Gen[BigDecimal] = {
    val min = if (minSize <= 0) BigInt(0) else BigInt("1" + ("0" * (minSize - 1)))
    choose[BigInt](min, BigInt("9" * maxSize)).map(BigDecimal(_, scale))
  }

  def minStringLength(length: Int): Gen[String] =
    for {
      i <- choose(length, length + 500)
      n <- listOfN(i, arbitrary[Char])
    } yield n.mkString

  val nonAlphaNumericChar: Gen[Char] = {
    val a = choose(Char.MinValue, 47.toChar)
    val b = choose(58.toChar, 64.toChar)
    val c = choose(91.toChar, 96.toChar)
    val d = choose(123.toChar, Char.MaxValue)
    oneOf(a, b, c, d)
  }

  val nonAlphaNumString: Gen[String] = listOf(nonAlphaNumericChar).map(_.mkString)
}
