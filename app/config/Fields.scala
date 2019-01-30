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

package config

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

class Fields extends Options {

  // declarant details form fields
  val declarantName: TextInput = TextInput(
    name = "declaration.declarant.name",
    validators = Seq(OptionalAlphaNumericValidator(70))
  )
  val declarantAddressLine: TextInput = TextInput(
    name = "declaration.declarant.address.line",
    labelKey = Some("common.fields.address.line"),
    validators = Seq(OptionalAlphaNumericValidator(70))
  )
  val declarantAddressCityName: TextInput = TextInput(
    name = "declaration.declarant.address.cityName",
    labelKey = Some("common.fields.address.cityName"),
    validators = Seq(OptionalAlphaNumericValidator(30))
  )
  val declarantAddressCountryCode: SelectInput = SelectInput(
    name = "declaration.declarant.address.countryCode",
    options = countryTypes,
    labelKey = Some("common.fields.address.countryCode"),
    validators = Seq(OptionalContainsValidator(countryTypes.map(_._1).toSet))
  )
  val declarantAddressPostcode: TextInput = TextInput(
    name = "declaration.declarant.address.postcodeId",
    labelKey = Some("common.fields.address.postcodeId"),
    validators = Seq(OptionalAlphaNumericValidator(9))
  )
  val declarantEori: TextInput = TextInput(
    name = "declaration.declarant.id",
    labelKey = Some("common.fields.eori"),
    hintKey = Some("common.hints.eori"),
    validators = Seq(RequiredAlphaNumericValidator(17, 17))
  )

  // references form fields
  // TODO governmentAgencyGoodsItems is a seq of elements needs mapping the sequence
  val referenceNumberUcr1: TextInput = TextInput(
    name = "declaration.goodsShipment.ucr.traderAssignedReferenceId",
    validators = Seq(OptionalAlphaNumericValidator(35))
  )
  val declarantFunctionalReferenceId: TextInput = TextInput(
    name = "declaration.functionalReferenceId",
    hintKey = Some("declaration.functionalReferenceId.hint"),
    validators = Seq(RequiredAlphaNumericValidator(22))
  )
  val declarationType: RadioInput = RadioInput(
    name = "declaration.typeCode",
    options = declarationTypes
  )
  val additionalDeclaratonType: RadioInput = RadioInput(
    name = "declaration.typeCode.additional", // FIXME same xml element mapped for declarationType and additionalDeclarationType - have had to use fictional property
    options = additionalDeclarationTypes,
    labelKey = Some("declaration.typeCode.additional")
  )

  // exporter details form fields
  val exporterName: TextInput = TextInput(
    name = "declaration.exporter.name",
    validators = Seq(OptionalAlphaNumericValidator(70))
  )
  val exporterAddressLine: TextInput = TextInput(
    name = "declaration.exporter.address.line",
    labelKey = Some("common.fields.address.line"),
    validators = Seq(OptionalAlphaNumericValidator(70))
  )
  val exporterAddressCityName: TextInput = TextInput(
    name = "declaration.exporter.address.cityName",
    labelKey = Some("common.fields.address.cityName"),
    validators = Seq(OptionalAlphaNumericValidator(30))
  )
  val exporterAddressCountryCode: SelectInput = SelectInput(
    name = "declaration.exporter.address.countryCode",
    options = countryTypes,
    labelKey = Some("common.fields.address.countryCode"),
    validators = Seq(OptionalContainsValidator(countryTypes.map(_._1).toSet))
  )
  val exporterAddressPostcode: TextInput = TextInput(
    name = "declaration.exporter.address.postcodeId",
    labelKey = Some("common.fields.address.postcodeId"),
    validators = Seq(OptionalAlphaNumericValidator(9))
  )
  val exporterEori: TextInput = TextInput(
    name = "declaration.exporter.id",
    labelKey = Some("common.fields.eori"),
    hintKey = Some("common.hints.eori"),
    validators = Seq(OptionalAlphaNumericValidator(17, 17))
  )

  // representative details form fields
  val representativeName: TextInput = TextInput(
    name = "declaration.agent.name",
    validators = Seq(OptionalAlphaNumericValidator(70))
  )
  val representativeAddressLine: TextInput = TextInput(
    name = "declaration.agent.address.line",
    labelKey = Some("common.fields.address.line"),
    validators = Seq(OptionalAlphaNumericValidator(70))
  )
  val representativeAddressCityName: TextInput = TextInput(
    name = "declaration.agent.address.cityName",
    labelKey = Some("common.fields.address.cityName"),
    validators = Seq(OptionalAlphaNumericValidator(30))
  )
  val representativeAddressCountryCode: SelectInput = SelectInput(
    name = "declaration.agent.address.countryCode",
    options = countryTypes,
    labelKey = Some("common.fields.address.countryCode"),
    validators = Seq(OptionalContainsValidator(countryTypes.map(_._1).toSet))
  )
  val representativeAddressPostcode: TextInput = TextInput(
    name = "declaration.agent.address.postcodeId",
    labelKey = Some("common.fields.address.postcodeId"),
    validators = Seq(OptionalAlphaNumericValidator(9))
  )
  val representativeEori: TextInput = TextInput(
    name = "declaration.agent.id",
    labelKey = Some("common.fields.eori"),
    hintKey = Some("common.hints.eori"),
    validators = Seq(OptionalAlphaNumericValidator(17, 17))
  )
  val representativeFunctionCode: RadioInput = RadioInput(
    name = "declaration.agent.functionCode",
    options = agentFunctionCodes,
    inline = true,
    hintKey = Some("declaration.agent.functionCode.hint")
  )

  // importer details form fields
  val importerName: TextInput = TextInput(
    name = "declaration.goodsShipment.importer.name",
    validators = Seq(OptionalAlphaNumericValidator(70))
  )
  val importerAddressLine: TextInput = TextInput(
    name = "declaration.goodsShipment.importer.address.line",
    labelKey = Some("common.fields.address.line"),
    validators = Seq(OptionalAlphaNumericValidator(70))
  )
  val importerAddressCityName: TextInput = TextInput(
    name = "declaration.goodsShipment.importer.address.cityName",
    labelKey = Some("common.fields.address.cityName"),
    validators = Seq(OptionalAlphaNumericValidator(30))
  )
  val importerAddressCountryCode: SelectInput = SelectInput(
    name = "declaration.goodsShipment.importer.address.countryCode",
    options = countryTypes,
    labelKey = Some("common.fields.address.countryCode"),
    validators = Seq(OptionalContainsValidator(countryTypes.map(_._1).toSet))
  )
  val importerAddressPostcode: TextInput = TextInput(
    name = "declaration.goodsShipment.importer.address.postcodeId",
    labelKey = Some("common.fields.address.postcodeId"),
    validators = Seq(OptionalAlphaNumericValidator(9))
  )
  val importerEori: TextInput = TextInput(
    name = "declaration.goodsShipment.importer.id",
    labelKey = Some("common.fields.eori"),
    hintKey = Some("common.hints.eori"),
    validators = Seq(OptionalAlphaNumericValidator(17, 17))
  )

  // seller details form fields
  val sellerName: TextInput = TextInput(
    name = "declaration.goodsShipment.seller.name",
    validators = Seq(OptionalAlphaNumericValidator(70))
  )
  val sellerAddressLine: TextInput = TextInput(
    name = "declaration.goodsShipment.seller.address.line",
    labelKey = Some("common.fields.address.line"),
    validators = Seq(OptionalAlphaNumericValidator(70))
  )
  val sellerAddressCityName: TextInput = TextInput(
    name = "declaration.goodsShipment.seller.address.cityName",
    labelKey = Some("common.fields.address.cityName"),
    validators = Seq(OptionalAlphaNumericValidator(30))
  )
  val sellerAddressCountryCode: SelectInput = SelectInput(
    name = "declaration.goodsShipment.seller.address.countryCode",
    options = countryTypes,
    labelKey = Some("common.fields.address.countryCode"),
    validators = Seq(OptionalContainsValidator(countryTypes.map(_._1).toSet))
  )
  val sellerAddressPostcode: TextInput = TextInput(
    name = "declaration.goodsShipment.seller.address.postcodeId",
    labelKey = Some("common.fields.address.postcodeId"),
    validators = Seq(OptionalAlphaNumericValidator(9))
  )
  val sellerCommunicationsId: TextInput = TextInput(
    name = "declaration.goodsShipment.seller.communications[0].id",
    labelKey = Some("common.fields.communications.id"),
    validators = Seq(OptionalAlphaNumericValidator(50))
  )
  val sellerEori: TextInput = TextInput(
    name = "declaration.goodsShipment.seller.id",
    labelKey = Some("common.fields.eori"),
    hintKey = Some("common.hints.eori"),
    validators = Seq(OptionalAlphaNumericValidator(17, 17))
  )

  // buyer details form fields
  val buyerName: TextInput = TextInput(
    name = "declaration.goodsShipment.buyer.name",
    validators = Seq(OptionalAlphaNumericValidator(70))
  )
  val buyerAddressLine: TextInput = TextInput(
    name = "declaration.goodsShipment.buyer.address.line",
    labelKey = Some("common.fields.address.line"),
    validators = Seq(OptionalAlphaNumericValidator(70))
  )
  val buyerAddressCityName: TextInput = TextInput(
    name = "declaration.goodsShipment.buyer.address.cityName",
    labelKey = Some("common.fields.address.cityName"),
    validators = Seq(OptionalAlphaNumericValidator(30))
  )
  val buyerAddressCountryCode: SelectInput = SelectInput(
    name = "declaration.goodsShipment.buyer.address.countryCode",
    options = countryTypes,
    labelKey = Some("common.fields.address.countryCode"),
    validators = Seq(OptionalContainsValidator(countryTypes.map(_._1).toSet))
  )
  val buyerAddressPostcode: TextInput = TextInput(
    name = "declaration.goodsShipment.buyer.address.postcodeId",
    labelKey = Some("common.fields.address.postcodeId"),
    validators = Seq(OptionalAlphaNumericValidator(9))
  )
  val buyerCommunicationsId: TextInput = TextInput(
    name = "declaration.goodsShipment.buyer.communications[0].id",
    labelKey = Some("common.fields.communications.id"),
    validators = Seq(OptionalAlphaNumericValidator(50))
  )
  val buyerEori: TextInput = TextInput(
    name = "declaration.goodsShipment.buyer.id",
    labelKey = Some("common.fields.eori"),
    hintKey = Some("common.hints.eori"),
    validators = Seq(OptionalAlphaNumericValidator(17, 17))
  )

  // summary of goods form fields
  val declarationTotalPackageQuantity: TextInput = TextInput(
    name = "declaration.totalPackageQuantity",
    labelKey = Some("declaration.totalPackageQuantity"),
    validators = Seq(RequiredNumericValidator(8))
  )

  val goodsMeasureGrossMassMeasure: TextInput = TextInput(
    name = "declaration.totalGrossMassMeasure.value",
    labelKey = Some("declaration.totalGrossMassMeasure.value"),
    validators = Seq(RequiredNumericValidator(16, 6))
  )

  // place of despatch form fields
  val exportCountry: SelectInput = SelectInput(
    name = "declaration.goodsShipment.governmentAgencyGoodsItems[0].exportCountry.id",
    options = thirdSpecialTerritoriesCategories,
    labelKey = Some("declaration.goodsShipment.governmentAgencyGoodsItems.exportCountry.id"),
    validators = Seq(RequiredContainsValidator(thirdSpecialTerritoriesCategories.map(_._1).toSet))
  )
  val loadingLocation: SelectInput = SelectInput(
    name = "declaration.goodsShipment.consignment.loadingLocation.id",
    options = airportCodes,
    labelKey = Some("declaration.goodsShipment.consignment.loadingLocation.id"),
    validators = Seq(RequiredContainsValidator(airportCodes.map(_._1).toSet))
  )
  val destinationCountry: SelectInput = SelectInput(
    name = "declaration.goodsShipment.governmentAgencyGoodsItems[0].destination.countryCode",
    options = euCountries,
    labelKey = Some("declaration.goodsShipment.governmentAgencyGoodsItems.destination.countryCode"),
    validators = Seq(RequiredContainsValidator(euCountries.map(_._1).toSet))
  )
// TODO - Set to "euCountries" assuming this is an H1 type, but for I1, needs to be "NonThirdCountries"

  // transport form fields
  val containerCode: TextInput = TextInput(
    name = "declaration.goodsShipment.consignment.containerCode",
    labelKey = Some("declaration.goodsShipment.consignment.containerCode")
  )

  val borderTransportMeans: SelectInput = SelectInput(
    name = "declaration.borderTransportMeans.modeCode",
    options = transportModeTypes,
    labelKey = Some("declaration.borderTransportMeans.modeCode"),
    validators = Seq(RequiredContainsValidator(transportModeTypes.map(_._1).toSet))
  )

  val arrivalTransportMeans: SelectInput = SelectInput(
    name = "declaration.goodsShipment.consignment.arrivalTransportMeans.modeCode",
    options = transportModeTypes,
    labelKey = Some("declaration.goodsShipment.consignment.arrivalTransportMeans.modeCode")
  )

  val arrivalTransportMeansIdentificationTypeCode: SelectInput = SelectInput(
    name = "declaration.goodsShipment.consignment.arrivalTransportMeans.identificationTypeCode",
    options = transportMeansIdentificationTypes,
    labelKey = Some("declaration.goodsShipment.consignment.arrivalTransportMeans.identificationTypeCode")
  )

  val arrivalTransportMeansIdentificationId: TextInput = TextInput(
    name = "declaration.goodsShipment.consignment.arrivalTransportMeans.id",
    labelKey = Some("declaration.goodsShipment.consignment.arrivalTransportMeans.id")
  )

  val transportEquipmentId: TextInput = TextInput(
    name = "declaration.goodsShipment.consignment.transportEquipments[0].id",
    labelKey = Some("declaration.goodsShipment.consignment.transportEquipments.id")
  )

  val borderTransportMeansRegistrationNationalityCode: SelectInput = SelectInput(
    name = "declaration.borderTransportMeans.registrationNationalityCode",
    options = countryTypes,
    labelKey = Some("declaration.borderTransportMeans.registrationNationalityCode")
  )

  // Location of goods form fields
  val locationOfGoodsIdentificationOfLocation: TextInput = TextInput(
    name = "declaration.goodsShipment.consignment.goodsLocation.name",
    labelKey = Some("declaration.goodsShipment.consignment.goodsLocation.name"),
    validators = Seq(OptionalAlphaNumericValidator(35))
  )

  val locationOfGoodsAdditionalIdentifier: TextInput = TextInput(
    name = "declaration.goodsShipment.consignment.goodsLocation.id",
    labelKey = Some("declaration.goodsShipment.consignment.goodsLocation.id"),
    validators = Seq(OptionalNumericValidator(3))
  )

  val locationOfGoodsTypeOfLocation: SelectInput = SelectInput(
    name = "declaration.goodsShipment.consignment.goodsLocation.typeCode",
    options = goodsLocationTypeCode,
    labelKey = Some("declaration.goodsShipment.consignment.goodsLocation.typeCode"),
    validators = Seq(RequiredContainsValidator(goodsLocationTypeCode.map(_._1).toSet))
  )

  val locationOfGoodsStreetAndNumber: TextInput = TextInput(
    name = "declaration.goodsShipment.consignment.goodsLocation.address.line",
    labelKey = Some("declaration.goodsShipment.consignment.goodsLocation.address.line"),
    validators = Seq(OptionalAlphaNumericValidator(70))
  )

  val locationOfGoodsPostcode: TextInput = TextInput(
    name = "declaration.goodsShipment.consignment.goodsLocation.address.postcodeId",
    labelKey = Some("common.fields.address.postcodeId"),
    validators = Seq(OptionalAlphaNumericValidator(9))
  )

  val locationOfGoodsCity: TextInput = TextInput(
    name = "declaration.goodsShipment.consignment.goodsLocation.address.cityName",
    labelKey = Some("common.fields.address.cityName"),
    validators = Seq(OptionalAlphaNumericValidator(35))
  )

  val locationOfGoodsCountry: SelectInput = SelectInput(
    name = "declaration.goodsShipment.consignment.goodsLocation.address.countryCode",
    options = countryOptions,
    labelKey = Some("common.fields.address.countryCode"),
    validators = Seq(OptionalContainsValidator(countryOptions.map(_._1).toSet))
  )

  val locationOfGoodsQualifierOfTheIdentification: SelectInput = SelectInput(
    name = "declaration.goodsShipment.consignment.goodsLocation.address.typeCode",
    options = goodsLocationTypeCode,
    labelKey = Some("declaration.goodsShipment.consignment.goodsLocation.address.typeCode"),
    validators = Seq(RequiredContainsValidator(goodsLocationTypeCode.map(_._1).toSet))
  )

  // Warehouse and customs offices form fields
  val identificationOfWarehouseWarehouseType: SelectInput = SelectInput(
    name = "declaration.goodsShipment.warehouse.typeCode",
    options = customsWareHouseTypes,
    labelKey = Some("declaration.goodsShipment.warehouse.typeCode"),
    validators = Seq(OptionalContainsValidator(customsWareHouseTypes.map(_._1).toSet))
  )

  val identificationOfWarehouseWarehouseIdentifier: TextInput = TextInput(
    name = "declaration.goodsShipment.warehouse.id",
    labelKey = Some("declaration.goodsShipment.warehouse.id"),
    validators = Seq(OptionalAlphaNumericValidator(35))
  )

  val customsOfficeOfPresentation: SelectInput = SelectInput(
    name = "declaration.presentationOffice.id",
    options = supervisingCustomsOffices,
    labelKey = Some("declaration.presentationOffice.id"),
    validators = Seq(OptionalContainsValidator(supervisingCustomsOffices.map(_._1).toSet))
  )

  val superivisingCustomsOffice: SelectInput = SelectInput(
    name = "declaration.supervisingOffice.id",
    options = supervisingCustomsOffices,
    labelKey = Some("declaration.supervisingOffice.id"),
    validators = Seq(OptionalContainsValidator(supervisingCustomsOffices.map(_._1).toSet))
  )

  // Delivery term form fields
  val deliveryTermsIncotermCode: SelectInput = SelectInput(
    name = "declaration.goodsShipment.tradeTerms.conditionCode",
    options = incoTermCodes,
    labelKey = Some("declaration.goodsShipment.tradeTerms.conditionCode"),
    hintKey = Some("declaration.goodsShipment.tradeTerms.conditionCode.hint"),
    validators = Seq(OptionalContainsValidator(incoTermCodes.map(_._1).toSet))
  )

  val deliveryTermsUnlocodeCode: TextInput = TextInput(
    name = "declaration.goodsShipment.tradeTerms.locationId",
    labelKey = Some("declaration.goodsShipment.tradeTerms.locationId"),
    validators = Seq(OptionalAlphaNumericValidator(17))
  )

  val deliveryTermsCountryCode: SelectInput = SelectInput(
    name = "declaration.goodsShipment.tradeTerms.locationName",
    options = countryTypes,
    labelKey = Some("common.fields.address.countryCode"),
    validators = Seq(OptionalContainsValidator(countryTypes.map(_._1).toSet))
  )


  val invoiceAmountValue: TextInput = TextInput(
    name = "declaration.invoiceAmount.value",
    labelKey = Some("declaration.invoiceAmount.value"),
    validators = Seq(OptionalNumericValidator(16,2))
  )

  val invoiceAmountCurrency: SelectInput = SelectInput(
    name = "declaration.invoiceAmount.currencyId",
    options = currencyTypes,
    labelKey = Some("declaration.invoiceAmount.currencyId")
  )

}

object Fields extends Fields {

  private val typeMirror = runtimeMirror(this.getClass.getClassLoader)
  private val instanceMirror = typeMirror.reflect(this)
  private val members = instanceMirror.symbol.typeSignature.members

  val definitions: Map[String, FieldDefinition] = fields.map { f =>
    val t = fieldMirror(f).get.asInstanceOf[FieldDefinition]
    t.name -> t
  }.toMap

  private def fields: Iterable[universe.Symbol] = members.filter(_.typeSignature.baseClasses.contains(typeOf[FieldDefinition].typeSymbol))

  private def fieldMirror(symbol: Symbol): universe.FieldMirror = instanceMirror.reflectField(symbol.asTerm)
}
