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

import config.Options
import domain._
import forms.DeclarationFormMapping.Date
import forms.ObligationGuaranteeForm
import models.ChangeReasonCode
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.{Arbitrary, Gen, Shrink}
import play.api.libs.json.{JsString, JsValue}
import typeclasses.Monoid
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec._

trait Generators extends SignedInUserGen with ViewModelGenerators with Lenses {

  implicit val dontShrinkStrings: Shrink[String] = Shrink.shrinkAny
  implicit val dontShrinkDecimals: Shrink[BigDecimal] = Shrink.shrinkAny
  implicit val dontShrinkInts: Shrink[Int] = Shrink.shrinkAny

  case class GuaranteeType(value: ObligationGuarantee)

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
    nonEmptyString
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

  def alphaLongerThan(minLength: Int): Gen[String] =
    alphaStr suchThat (_.length > minLength)

  def alphaLessThan(maxLength: Int): Gen[String] =
    alphaStr suchThat (_.nonEmpty) map (_.take(maxLength))

  def numStrLongerThan(minLength: Int): Gen[String] =
    numStr suchThat (_.length > minLength)

  def varListOf[A](max: Int)(gen: Gen[A]): Gen[List[A]] =
    for {
      i <- choose(0, max)
      xs <- listOfN(i, gen)
    } yield xs

  def currencyGen: Gen[String] = oneOf(config.Options.currencyTypes.map(_._2))

  def countryGen: Gen[String] = oneOf(config.Options.countryOptions.map(_._1))

  private def zip[A, B](fa: Option[A], fb: Option[B]): Option[(A, B)] =
    fa.flatMap(a => fb.map(b => (a, b)))

  implicit val arbitraryOffice: Arbitrary[Office] = Arbitrary {
    for {
      id <- option(nonEmptyString.map(_.take(8)))
    } yield Office(id)
  }

  implicit val arbitraryWarehouse: Arbitrary[Warehouse] = Arbitrary {
    for {
      id <- option(nonEmptyString.map(_.take(35)))
      typeCode <- oneOf(config.Options.customsWareHouseTypes.map(_._1))
    } yield Warehouse(id, typeCode)
  }

  implicit val arbitraryWarehouseAndCustoms: Arbitrary[WarehouseAndCustoms] = Arbitrary {
    for {
      warehouse <- option(arbitrary[Warehouse])
      presentationOffice <- arbitrary[Office]
      supervisingOffice <- arbitrary[Office]
    } yield WarehouseAndCustoms(
      warehouse,
      presentationOffice.id.map(_ => presentationOffice),
      supervisingOffice.id.map(_ => supervisingOffice)
    )
  }

  implicit val arbitraryTradeTerms: Arbitrary[TradeTerms] = Arbitrary {
    for {
      conditionCode <- oneOf(config.Options.incoTermCodes).map(_._1)
      locationId <- option(nonEmptyString.map(_.take(17)))
      locationName <- option(nonEmptyString.map(_.take(37)))
    } yield TradeTerms(Some(conditionCode), None, None, locationId, locationName)
  }

  implicit val arbitraryCurrencyExchange: Arbitrary[CurrencyExchange] = Arbitrary {
    for {
      currencyTypeCode <- currencyGen
      rateNumeric <- posDecimal(12, 5)
    } yield CurrencyExchange(Some(currencyTypeCode), Some(rateNumeric))
  }

  implicit val arbitraryObligationGuarantee: Arbitrary[ObligationGuarantee] = Arbitrary {
    for {
      amount <- option(posDecimal(16, 2))
      id <- option(arbitrary[String].map(_.take(35)))
      referenceId <- option(arbitrary[String].map(_.take(35)))
      securityDetailsCode <- option(arbitrary[String].map(_.take(3)))
      accessCode <- option(arbitrary[String].map(_.take(4)))
      office <- option(arbitrary[Office])
    } yield ObligationGuarantee(amount, id, referenceId, securityDetailsCode, accessCode, office)
  }

  implicit val arbitraryObligationGuaranteeForm: Arbitrary[ObligationGuaranteeForm] = Arbitrary {
    for {
      guarantees <- Gen.listOfN(1, arbitrary[ObligationGuarantee])
    } yield ObligationGuaranteeForm(guarantees)
  }

  implicit val arbitraryAdditionalInfo: Arbitrary[AdditionalInformation] = Arbitrary {
    for {
      statementCode <- option(nonEmptyString.map(_.take(5)))
      statementDescription <- option(nonEmptyString.map(_.take(512)))
      if statementCode.nonEmpty || statementDescription.nonEmpty
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
      unitCode <- option(nonEmptyString.map(_.take(3)))
      value <- option(posDecimal(10, 2))
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
    } yield WriteOff(quantity)
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

  implicit val arbitraryAdditionalDocument: Arbitrary[AdditionalDocument] = Arbitrary {
    for {
      categoryCode <- option(arbitrary[String].map(_.take(1)))
      id <- option(intBetweenRange(0, 9999999).map(_.toString))
      typeCode <- option(arbitrary[String].map(_.take(3)))
    } yield AdditionalDocument(categoryCode, id, typeCode)
  }

  implicit val arbitraryOrigin: Arbitrary[Origin] = Arbitrary {
    for {
      countryCode <- countryGen
      typeCode <- some(choose[Int](1, 9))
    } yield Origin(Some(countryCode), None)
  }

  implicit val arbitraryRoleBasedParty: Arbitrary[RoleBasedParty] = Arbitrary {
    for {
      id <- option(nonEmptyString.map(_.take(17)))
      roleCode <- option(alphaStr.map(_.take(3))).map(_.find(_.nonEmpty))
      if id.nonEmpty || roleCode.nonEmpty
    } yield RoleBasedParty(id, roleCode)
  }

  implicit val arbitraryGovernmentAgencyGoodsItemAdditionalDocumentSubmitter
  : Arbitrary[GovernmentAgencyGoodsItemAdditionalDocumentSubmitter] = Arbitrary {
    for {
      name <- option(nonEmptyString.map(_.take(20)))
    } yield GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(name)
  }

  implicit val arbitraryGovernmentProcedure: Arbitrary[GovernmentProcedure] = Arbitrary {
    for {
      currentCode <- option(nonEmptyString.map(_.take(2)))
      previousCode <- option(nonEmptyString.map(_.take(2)))
      if currentCode.nonEmpty || previousCode.nonEmpty
    } yield GovernmentProcedure(currentCode, previousCode)
  }

  implicit val arbitraryGovernmentAgencyGoodsItemAdditionalDocument: Arbitrary[GovernmentAgencyGoodsItemAdditionalDocument] = Arbitrary {
    for {
      categoryCode <- option(numChar.map(_.toString))
      effectiveDateTime <- option(arbitrary[DateTimeElement])
      id <- option(nonEmptyString.map(_.take(20)))
      name <- option(nonEmptyString.map(_.take(20)))
      typeCode <- option(listOfN(3, numChar).map(_.mkString))
      lpcoExemptionCode <- option(listOfN(2, alphaChar).map(_.mkString))
      submitter <- arbitrary[GovernmentAgencyGoodsItemAdditionalDocumentSubmitter]
        .flatMap(x => lpcoExemptionCode.fold(option(x))(_ => some(x)))
      writeOff <- arbitrary[WriteOff]
        .flatMap(c => lpcoExemptionCode.fold(option(c))(_ => some(c)))
    } yield {
      GovernmentAgencyGoodsItemAdditionalDocument(categoryCode, effectiveDateTime, id, name, typeCode, lpcoExemptionCode, submitter, writeOff)
    }
  }

  implicit val arbitraryAddress: Arbitrary[Address] = Arbitrary {
    for {
      cityName <- option(nonEmptyString.map(_.take(35))) // max length 35
      countryCode <- option(countryGen) // 2 chars [a-zA-Z] ISO 3166-1 2-alpha
      countrySubDivisionCode <- option(nonEmptyString.map(_.take(9))) // max 9 chars
      countrySubDivisionName <- option(nonEmptyString.map(_.take(35))) // max 35 chars
      line <- option(nonEmptyString.map(_.take(70))) // max 70 chars
      postcodeId <- option(nonEmptyString.map(_.take(9))) // max 9 chars
    } yield Address(cityName, countryCode, countrySubDivisionCode, countrySubDivisionName, line, postcodeId)
  }

  implicit val arbitraryNamedEntityWithAddress: Arbitrary[NamedEntityWithAddress] = Arbitrary {
    for {
      name <- option(nonEmptyString.map(_.take(70)))
      id <- option(nonEmptyString.map(_.take(17)))
      address <- option(arbitrary[Address])
    } yield NamedEntityWithAddress(name, id, address)
  }

  implicit val arbitraryPackaging: Arbitrary[Packaging] = Arbitrary {
    for {
      marksNumbersId <- option(nonEmptyString.map(_.take(500)))
      quantity <- option(choose[Int](1, 999))
      typeCode <- option(listOfN(2, arbitrary[Char]).map(_.mkString))
    } yield Packaging(None, marksNumbersId, quantity, typeCode, None,
      None, None, None, None)
  }

  implicit val arbitraryDestination: Arbitrary[Destination] = Arbitrary {
    for {
      countryCode <- option(oneOf(config.Options.countryOptions.map(_._1)))
    } yield Destination(countryCode, None)
  }

  implicit val arbitraryUcr: Arbitrary[Ucr] = Arbitrary {
    for {
      id <- option(arbitrary[String].map(_.take(35)))
      traderAssignedReferenceId <- option(arbitrary[String].map(_.take(35)))
    } yield Ucr(id, traderAssignedReferenceId)
  }

  implicit val arbitraryExportCountry: Arbitrary[ExportCountry] = Arbitrary {
    for {
      id <- oneOf(config.Options.countryTypes.map(_._1)) // either "102" or "304"
    } yield ExportCountry(id)
  }

  implicit val arbitraryDate: Arbitrary[Date] = Arbitrary {
    for {
      day <- chooseNum(1, 28)
      month <- chooseNum(1, 12)
      year <- chooseNum(1900, 2020)
    } yield Date(day, month, year)
  }

  implicit val arbitraryDateTimeString: Arbitrary[DateTimeString] = Arbitrary {
    for {
      formatCode <- Gen.const("102")
      value <- arbitrary[Date].map(date => s"${date.year}/${date.month}/${date.day}")
    } yield DateTimeString(formatCode, value)
  }

  implicit val arbitraryDateTimeElement: Arbitrary[DateTimeElement] = Arbitrary {
    for {
      dateTimeString <- arbitrary[DateTimeString]
    } yield DateTimeElement(dateTimeString)
  }

  implicit val arbitraryValuationAdjustment: Arbitrary[ValuationAdjustment] = Arbitrary {
    for {
      additionCode <- option(arbitrary[String].map(_.take(4))) // TODO : Set specfic values i.e. one of 145, 146, 147, 148, 149
    } yield ValuationAdjustment(additionCode)
  }

  implicit val arbitraryGovernmentAgencyGoodsItem: Arbitrary[GovernmentAgencyGoodsItem] = Arbitrary {
    for {
      customsValueAmount <- option(arbitrary[BigDecimal].map(_.max(9999999999999999.99999)))
      sequenceNumeric <- arbitrary[Int].map(_.max(99999))
      statisticalValueAmount <- option(arbitraryAmount.arbitrary)
      transactionNatureCode <- option(arbitrary[Int].map(_.max(99)))
      destination <- option(arbitraryDestination.arbitrary)
      ucr <- option(arbitraryUcr.arbitrary)
      exportCountry <- option(arbitraryExportCountry.arbitrary)
      valuationAdjustment <- option(arbitraryValuationAdjustment.arbitrary)
      additionalDocuments <- Gen.listOfN(1, arbitraryGovernmentAgencyGoodsItemAdditionalDocument.arbitrary)
      additionalInformations <- Gen.listOfN(1, arbitraryAdditionalInfo.arbitrary)
      aeoMutualRecognitionParties <- Gen.listOfN(1, arbitraryRoleBasedParty.arbitrary)
      domesticParties <- Gen.listOfN(1, arbitraryRoleBasedParty.arbitrary)
      governmentProcedures <- Gen.listOfN(1, arbitraryGovernmentProcedure.arbitrary)
      manufacturers <- Gen.listOfN(1, arbitraryNamedEntityWithAddress.arbitrary)
      origins <- Gen.listOfN(1, arbitraryOrigin.arbitrary)
      packagings <- Gen.listOfN(1, arbitraryPackaging.arbitrary)
      previousDocuments <- Gen.listOfN(1, arbitraryPreviousDocument.arbitrary)
    } yield {
      GovernmentAgencyGoodsItem(
        customsValueAmount = customsValueAmount,
        sequenceNumeric = sequenceNumeric,
        statisticalValueAmount = statisticalValueAmount,
        transactionNatureCode = transactionNatureCode,
        additionalDocuments = additionalDocuments,
        additionalInformations = additionalInformations,
        aeoMutualRecognitionParties = aeoMutualRecognitionParties,
        destination = destination,
        domesticDutyTaxParties = domesticParties,
        exportCountry = exportCountry,
        governmentProcedures = governmentProcedures,
        manufacturers = manufacturers,
        origins = origins,
        packagings = packagings,
        previousDocuments = previousDocuments,
        ucr = ucr,
        valuationAdjustment = valuationAdjustment)
    }
  }

  implicit val arbitraryTransportEquipment = Arbitrary {
    stringsWithMaxLength(17)
      .suchThat(_.nonEmpty)
      .map(s => TransportEquipment(0, Some(s)))
  }

  implicit val arbitraryChargeDeduction: Arbitrary[ChargeDeduction] = Arbitrary {
    for {
      typeCode <- alphaStr.suchThat(_.nonEmpty).map(_.take(2))
      amount <- arbitrary[Amount]
      if typeCode.length == 2 || amount.currencyId.nonEmpty
    } yield {
      ChargeDeduction(Some(typeCode), amount.currencyId.map(_ => amount))
    }
  }

  implicit val arbitrarySecurityDetailsCode: Arbitrary[GuaranteeType] = Arbitrary {
    arbitrary[Char]
      .map(c => GuaranteeType(ObligationGuarantee(securityDetailsCode = Some(c.toString))))
  }

  implicit val arbitraryImportExportParty: Arbitrary[ImportExportParty] = Arbitrary {
    for {
      name <- option(nonEmptyString.map(_.take(70)))
      id <- option(nonEmptyString.map(_.take(17)))
      address <- option(arbitrary[Address])
      comms <- option(arbitrary[Communication]).map(_.toList)
    } yield {
      ImportExportParty(name, id, address, communications = comms)
    }
  }

  implicit val arbitraryReferences: Arbitrary[References] = Arbitrary {
    for {
      typeCode <- option(alphaStr.suchThat(_.nonEmpty).map(_.take(2)))
      typerCode <- option(alphaStr.suchThat(_.nonEmpty).map(_.take(1)))
      traderId <- option(nonEmptyString.map(_.take(35)))
      funcRefId <- arbitrary[String].map(_.take(22))
      natureCode <- option(choose[Int](-9, 99))
    } yield {
      References(typeCode, typerCode, traderId, funcRefId, natureCode)
    }
  }

  implicit val arbitraryAgent: Arbitrary[Agent] = Arbitrary {
    for {
      party <- arbitrary[ImportExportParty]
      code <- option(oneOf(config.Options.agentFunctionCodes.map(_._1)))
    } yield {
      Agent(party.name, party.id, code, party.address)
    }
  }

  implicit val arbitraryInvoiceAndCurrency: Arbitrary[InvoiceAndCurrency] = Arbitrary {
    for {
      amount <- arbitrary[Amount]
      currency <- arbitrary[CurrencyExchange]
    } yield InvoiceAndCurrency(amount.currencyId.map(_ => amount), currency.currencyTypeCode.map(_ => currency))
  }

  implicit val arbitraryCommunication: Arbitrary[Communication] = Arbitrary {
    for {
      id <- option(nonEmptyString.map(_.take(50)))
      code <- option(nonEmptyString.map(_.take(3)))
    } yield {
      Communication(id, code)
    }
  }

  implicit val arbitraryAboutGoods: Arbitrary[SummaryOfGoods] = Arbitrary {
    for {
      quantity <- choose(0, 99999999)
      measure <- arbitrary[Measure]
      measureOpt = zip(measure.value, measure.unitCode).map(_ => measure)
    } yield {
      SummaryOfGoods(Some(quantity), measureOpt)
    }
  }

  implicit val arbitraryBorderTransportMeans: Arbitrary[BorderTransportMeans] = Arbitrary {
    for {
      modeCode <- option(intBetweenRange(1, 9))
      regCode <- option(countryGen)
    } yield {
      BorderTransportMeans(None, None, None, None, regCode, modeCode)
    }
  }

  implicit val arbitraryTransportMeans: Arbitrary[TransportMeans] = Arbitrary {
    for {
      id <- option(nonEmptyString.map(_.take(35)))
      typeId <- option(oneOf(Options.transportMeansIdentificationTypes.map(_._1)))
      modeCode <- option(intBetweenRange(1, 9))
    } yield {
      TransportMeans(None, id, typeId, None, modeCode)
    }
  }

  implicit val arbitraryTransport: Arbitrary[Transport] = Arbitrary {
    for {
      container <- option(intBetweenRange(0, 9))
      border <- arbitrary[BorderTransportMeans]
      arrival <- option(arbitrary[TransportMeans])
      borderOpt = zip(border.registrationNationalityCode, border.modeCode).map(_ => border)
    } yield {
      Transport(container, borderOpt, arrival)
    }
  }

  implicit val arbitraryGoodsLocationAddress: Arbitrary[GoodsLocationAddress] = Arbitrary {
    for {
      line <- option(nonEmptyString.map(_.take(70)))
      postcodeId <- option(nonEmptyString.map(_.take(9)))
      cityName <- option(nonEmptyString.map(_.take(35)))
      countryCode <- option(oneOf(config.Options.countryOptions.map(_._1)))
      typeCode <- option(oneOf(config.Options.goodsLocationTypeCode.map(_._1)))
    } yield {
      GoodsLocationAddress(typeCode, cityName, countryCode, line, postcodeId)
    }
  }

  implicit val arbitraryGoodsLocation: Arbitrary[GoodsLocation] = Arbitrary {
    for {
      name <- option(nonEmptyString.map(_.take(35)))
      id <- nonEmptyString.map(_.take(3))
      typeCode <- option(oneOf(config.Options.goodsLocationTypeCode.map(_._1)))
      address <- option(arbitrary[GoodsLocationAddress])
    } yield {
      GoodsLocation(name, id, typeCode, address)
    }
  }

  implicit val arbitraryLoadingLocation: Arbitrary[LoadingLocation] = Arbitrary {
    for {
      id <- option(nonEmptyString.map(_.take(17)))
    } yield {
      LoadingLocation(None, id)
    }
  }

  implicit val arbitraryLocationOfGoods: Arbitrary[LocationOfGoods] = Arbitrary {
    for {
      goodsLocation <- option(arbitrary[GoodsLocation])
      goodsLocationAddress <- option(arbitrary[GoodsLocationAddress])
      destination <- arbitrary[Destination]
      exportCountry <- option(arbitrary[ExportCountry])
      loadingLocation <- arbitrary[LoadingLocation]
    } yield {
      LocationOfGoods(goodsLocation, destination.countryCode.map(_ => destination), exportCountry, loadingLocation.id.map(_ => loadingLocation))
    }
  }
  val mapGen: Gen[Map[String, JsValue]] =
    listOf(Gen.zip(arbitrary[String], arbitrary[String]).map {
      case (k, v) => Map[String, JsValue](k -> JsString(v))
    }).map(_.fold(Map())(_ ++ _))

  implicit val arbitraryCacheMap: Arbitrary[CacheMap] = Arbitrary {

    import CacheMapLens._

    def list[A]: Gen[A] => Gen[List[A]] = varListOf[A](5)

    List(
      declarantDetails.set(arbitrary[ImportExportParty]),
      references.set(arbitrary[References]),
      exporter.set(arbitrary[ImportExportParty]),
      representative.set(arbitrary[Agent]),
      importer.set(arbitrary[ImportExportParty]),
      tradeTerms.set(arbitrary[TradeTerms]),
      invoiceAndCurrency.set(arbitrary[InvoiceAndCurrency]),
      seller.set(arbitrary[ImportExportParty]),
      buyer.set(arbitrary[ImportExportParty]),
      summaryOfGoods.set(arbitrary[SummaryOfGoods]),
      transport.set(arbitrary[Transport]),
      authorisationHolders.set(list(arbitrary[AuthorisationHolder])),
      guaranteeReferences.set(list(arbitrary[ObligationGuarantee])),
      previousDocuments.set(list(arbitrary[PreviousDocument])),
      additionalDocuments.set(list(arbitrary[AdditionalDocument])),
      additionalSupplyChainActors.set(list(arbitrary[RoleBasedParty])),
      domesticDutyTaxParty.set(list(arbitrary[RoleBasedParty])),
      additionsAndDeductions.set(list(arbitrary[ChargeDeduction])),
      containerIdNos.set(list(arbitrary[TransportEquipment])),
      guaranteeTypes.set(list(arbitrary[ObligationGuarantee])),
      govAgencyGoodsItemsList.set(list(arbitrary[GovernmentAgencyGoodsItem])),
      warehouseAndCustsoms.set(arbitrary[WarehouseAndCustoms]),
      locationOfGoods.set(arbitrary[LocationOfGoods])
    ).foldLeft(const(Monoid.empty[CacheMap])) { case (acc, f) => f(acc) }
  }

  implicit val arbitraryPayment: Arbitrary[Payment] = Arbitrary {
    for {
      methodCode <- option(alphaStr.suchThat(_.nonEmpty).map(_.take(1)))
      taxableAmount <- arbitrary[Amount]
      paymentAmount <- arbitrary[Amount]
    } yield Payment(methodCode, taxableAmount.currencyId.map(_ => taxableAmount), paymentAmount.currencyId.map(_ => paymentAmount))
  }

  implicit val arbitraryDutyTaxFee: Arbitrary[DutyTaxFee] = Arbitrary {
    for {
      specificTaxBaseQuantity <- arbitrary[Measure]
      taxRateNumeric <- posDecimal(16, 2)
      typeCode <- option(nonEmptyString.map(_.take(3)))
      quataOrderNo <- option(nonEmptyString.map(_.take(6)))
      payment <- arbitraryPayment.arbitrary
      if (typeCode.exists(_.length == 3) && quataOrderNo.exists(_.length == 6))
    } yield DutyTaxFee(None, None, None, Some(specificTaxBaseQuantity), Some(taxRateNumeric), typeCode, quataOrderNo, Some(payment))
  }

  implicit val arbitraryCommodity: Arbitrary[Commodity] = Arbitrary {
    for {
      dutyTaxFees <- Gen.listOfN(1, arbitrary[DutyTaxFee])
    } yield Commodity(dutyTaxFees = dutyTaxFees)
  }

  implicit val arbitraryCustomsValuation: Arbitrary[CustomsValuation] = Arbitrary {
    for {
      chargeDeductions <- Gen.listOfN(1, arbitrary[ChargeDeduction])
    } yield CustomsValuation(chargeDeductions = chargeDeductions)
  }

  implicit val arbitraryChangeReasonCode: Arbitrary[ChangeReasonCode] = Arbitrary(Gen.oneOf(ChangeReasonCode.values.toSeq))

  implicit val arbitraryCancel: Arbitrary[Cancel] = Arbitrary {
    for {
      changeReasonCode <- arbitrary[ChangeReasonCode]
      description <- stringsWithMaxLength(512)
    } yield Cancel(changeReasonCode, description)
  }

  def intGreaterThan(min: Int): Gen[Int] =
    choose(min + 1, Int.MaxValue)

  def intLessThan(max: Int): Gen[Int] =
    choose(Int.MinValue, max - 1)

  def intBetweenRange(min: Int, max: Int): Gen[Int] =
    choose(min, max)

  def intOutsideRange(min: Int, max: Int): Gen[Int] =
    oneOf(intLessThan(min), intGreaterThan(max))

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

  val nonAlphaChar: Gen[Char] = {
    val a = choose(Char.MinValue, 64.toChar)
    val b = choose(91.toChar, 96.toChar)
    val c = choose(123.toChar, Char.MaxValue)

    oneOf(a, b, c)
  }

  val nonAlphaNumString: Gen[String] = listOf(nonAlphaNumericChar).map(_.mkString)

  val nonAlphaString: Gen[String] = listOf(nonAlphaChar).map(_.mkString)
}

object Generators extends Generators
