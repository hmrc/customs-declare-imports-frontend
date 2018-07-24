/*
 * Copyright 2018 HM Revenue & Customs
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

package domain.declaration

case class MetaData(declaration: Declaration,
                    wcoDataModelVersionCode: Option[String] = None,
                    wcoTypeName: Option[String] = None,
                    responsibleCountryCode: Option[String] = None,
                    responsibleAgencyName: Option[String] = None,
                    agencyAssignedCustomizationCode: Option[String] = None,
                    agencyAssignedCustomizationVersionCode: Option[String] = None)

// declaration consists of xsd:sequence the order of which is reflected in the field order
case class Declaration(acceptanceDateTime: Option[AcceptanceDateTime] = None, // name="AcceptanceDateTime" type="ds:DeclarationAcceptanceDateTimeType" minOccurs="0"
                       functionCode: Option[String] = None, // name="FunctionCode" type="ds:DeclarationFunctionCodeType" minOccurs="0"
                       functionalReferenceId: Option[String] = None, // name="FunctionalReferenceID" type="ds:DeclarationFunctionalReferenceIDType" minOccurs="0"
                       id: Option[String] = None, // name="ID" type="ds:DeclarationIdentificationIDType" minOccurs="0"
                       issueDateTime: Option[IssueDateTime] = None, // name="IssueDateTime" type="ds:DeclarationIssueDateTimeType" minOccurs="0"
                       issueLocationId: Option[String] = None, // name="IssueLocationID" type="ds:DeclarationIssueLocationIdentificationIDType" minOccurs="0"
                       typeCode: Option[String] = None, // name="TypeCode" type="ds:DeclarationTypeCodeType" minOccurs="0"
                       goodsItemQuantity: Option[Int] = None, // name="GoodsItemQuantity" type="ds:DeclarationGoodsItemQuantityType" minOccurs="0"
                       declarationOfficeId: Option[String] = None, // name="DeclarationOfficeID" type="ds:DeclarationDeclarationOfficeIDType" minOccurs="0"
                       invoiceAmount: Option[InvoiceAmount] = None, // name="InvoiceAmount" type="ds:DeclarationInvoiceAmountType" minOccurs="0"
                       loadingListQuantity: Option[Int] = None, // name="LoadingListQuantity" type="ds:DeclarationLoadingListQuantityType" minOccurs="0"
                       totalGrossMassMeasure: Option[MassMeasure] = None, // name="TotalGrossMassMeasure" type="ds:DeclarationTotalGrossMassMeasureType" minOccurs="0"
                       totalPackageQuantity: Option[Int] = None, // name="TotalPackageQuantity" type="ds:DeclarationTotalPackageQuantityType" minOccurs="0"
                       specificCircumstancesCodeCode: Option[String] = None, // name="SpecificCircumstancesCodeCode" type="ds:DeclarationSpecificCircumstancesCodeCodeType" minOccurs="0"
                       authentication: Option[Authentication] = None, // name="Authentication" minOccurs="0" maxOccurs="1"
                       submitter: Option[Submitter] = None, // name="Submitter" minOccurs="0" maxOccurs="1"
                       additionalDocuments: Seq[AdditionalDocument] = Seq.empty, // name="AdditionalDocument" minOccurs="0" maxOccurs="unbounded"
                       additionalInformations: Seq[AdditionalInformation] = Seq.empty, // name="AdditionalInformation" minOccurs="0" maxOccurs="unbounded"
                       agent: Option[Agent] = None, // name="Agent" minOccurs="0" maxOccurs="1"
                       // TODO model amendment XML
                       amendments: Seq[String] = Seq.empty, //  name="Amendment" minOccurs="0" maxOccurs="unbounded"
                       authorisationHolders: Seq[AuthorisationHolder] = Seq.empty, // name="AuthorisationHolder" minOccurs="0" maxOccurs="unbounded"
                       borderTransportMeans: Option[BorderTransportMeans] = None, // name="BorderTransportMeans" minOccurs="0" maxOccurs="1"
                       currencyExchanges: Seq[String] = Seq.empty, // name="CurrencyExchange" minOccurs="0" maxOccurs="unbounded"
                       declarant: Option[Declarant] = None, // name="Declarant" minOccurs="0" maxOccurs="1"
                       exitOffice: Option[String] = None, // name="ExitOffice" minOccurs="0" maxOccurs="1"
                       exporter: Option[Exporter] = None, // name="Exporter" minOccurs="0" maxOccurs="1"
                       goodsShipment: Option[GoodsShipment] = None, // name="GoodsShipment" minOccurs="0" maxOccurs="1"
                       obligationGuarantees: Seq[ObligationGuarantee] = Seq.empty, // name="ObligationGuarantee" minOccurs="0" maxOccurs="unbounded"
                       presentationOffice: Option[String] = None, // name="PresentationOffice" minOccurs="0" maxOccurs="1"
                       supervisingOffice: Option[SupervisingOffice] = None) // name="SupervisingOffice" minOccurs="0" maxOccurs="1"

case class Agent(name: Option[String] = None, // max 70 chars
                 id: Option[String] = None, // max 17 chars
                 functionCode: Option[String] = None, // max 3 chars
                 address: Option[Address] = None)

case class Submitter(name: Option[String] = None, // max length 70
                     id: Option[String] = None, // max length 17
                     address: Option[Address] = None)

case class Authentication(authentication: Option[String] = None, authenticator: Option[Authenticator] = None)

case class Authenticator(name: Option[String] = None)

case class AcceptanceDateTime(dateTimeString: DateTimeString)

case class IssueDateTime(dateTimeString: DateTimeString)

case class DateTimeString(formatCode: String,
                          value: String)

case class InvoiceAmount(value: BigDecimal, currencyId: Option[String] = None)

case class MassMeasure(value: BigDecimal, unitCode: Option[String] = None)

case class AdditionalDocument(id: Option[String] = None, // max 70 chars
                              categoryCode: Option[String] = None, // max 3 chars
                              typeCode: Option[String] = None, // max 3 chars
                              name: Option[String] = None,
                              lpcoExemptionCode: Option[String] = None)

case class AuthorisationHolder(id: Option[String] = None, // max 17 chars
                               categoryCode: Option[String] = None) // max 4 chars

case class BorderTransportMeans(name: Option[String] = None, // max 35 chars,
                                id: Option[String] = None, // max 35 chars,
                                identificationTypeCode: Option[String] = None, // max 17 chars
                                typeCode: Option[String] = None, // max 4 chars
                                registrationNationalityCode: Option[String] = None, // 2 chars [a-zA-Z] when present; presumably ISO 3166-1 alpha2
                                modeCode: Option[Int] = None) // 0-9

case class Declarant(id: String)

case class Exporter(name: String,
                    address: Address)

case class Address(cityName: Option[String] = None, // max length 35
                   countryCode: Option[String] = None, // 2 chars [a-zA-Z] ISO 3166-1 2-alpha
                   countrySubDivisionCode: Option[String] = None, // max 9 chars
                   countrySubDivisionName: Option[String] = None, // max 35 chars
                   line: Option[String] = None, // max 70 chars
                   postcodeId: Option[String] = None) // max 9 chars

case class GoodsShipment(transactionNatureCode: String,
                         consignment: Consignment,
                         exportCountry: ExportCountry,
                         governmentAgencyGoodsItem: GovernmentAgencyGoodsItem,
                         importer: Importer,
                         tradeTerms: TradeTerms,
                         ucr: Ucr,
                         warehouse: Warehouse)

case class Warehouse(id: String,
                     typeCode: String)

case class TradeTerms(conditionCode: String,
                      locationId: String)

case class Ucr(traderAssignedReferenceId: String)

case class Importer(id: String)

case class Consignment(containerCode: String,
                       arrivalTransportMeans: ArrivalTransportMeans,
                       goodsLocation: GoodsLocation,
                       transportEquipment: TransportEquipment)

case class ArrivalTransportMeans(id: String,
                                 identificationTypeCode: String,
                                 modeCode: String)

case class GoodsLocation(id: String,
                         typeCode: String,
                         address: GoodsLocationAddress)

case class GoodsLocationAddress(typeCode: String,
                                countryCode: String)

case class TransportEquipment(sequenceNumeric: Int,
                              id: String)

case class ExportCountry(id: String)

case class GovernmentAgencyGoodsItem(sequenceNumeric: Int,
                                     statisticalValueAmount: StatisticalValueAmount,
                                     additionalDocuments: List[AdditionalDocument],
                                     additionalInformation: AdditionalInformation,
                                     commodity: Commodity,
                                     customsValuation: CustomsValuation,
                                     destination: Destination,
                                     governmentProcedures: List[GovernmentProcedure],
                                     origin: Origin,
                                     packaging: Packaging,
                                     previousDocument: PreviousDocument,
                                     valuationAdjustment: ValuationAdjustment)

case class ValuationAdjustment(additionCode: String)

case class PreviousDocument(categoryCode: String,
                            id: String,
                            typeCode: String,
                            lineNumeric: Int)

case class Packaging(sequenceNumeric: Int,
                     marksNumbersId: String,
                     typeCode: String)

case class StatisticalValueAmount(currencyId: String,
                                  value: BigDecimal)

case class AdditionalInformation(statementCode: Option[String] = None, // max 17 chars
                                 statementDescription: Option[String] = None, // max 512 chars
                                 statementTypeCode: Option[String] = None, // max 3 chars
                                 pointers: Seq[Pointer] = Seq.empty)

case class Pointer(sequenceNumeric: Option[Int] = None, // min 0 max 99999
                   documentSectionCode: Option[String] = None, // max 3 chars
                   tagId: Option[String] = None) // max 4 chars

case class Commodity(description: String,
                     classifications: List[Classification],
                     dutyTaxFee: DutyTaxFee,
                     goodsMeasure: GoodsMeasure,
                     invoiceLine: InvoiceLine)

case class Classification(id: String,
                          identificationTypeCode: String)

case class DutyTaxFee(dutyRegimeCode: String,
                      payment: Payment)

case class Payment(methodCode: String)

case class GoodsMeasure(grossMassMeasure: BigDecimal,
                        netNetWeightMeasure: BigDecimal)

case class InvoiceLine(itemChargeAmount: ItemChargeAmount)

case class ItemChargeAmount(currencyId: String,
                            value: BigDecimal)

case class CustomsValuation(methodCode: String)

case class Destination(countryCode: String)

case class GovernmentProcedure(currentCode: String,
                               previousCode: Option[String] = None)

case class Origin(countryCode: String,
                  typeCode: String)

case class ObligationGuarantee(referenceId: String,
                               securityDetailsCode: String)

case class SupervisingOffice(id: String)
