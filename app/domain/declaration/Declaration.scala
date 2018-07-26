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

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonIgnoreProperties, JsonProperty}
import com.fasterxml.jackson.dataformat.xml.annotation.{JacksonXmlElementWrapper, JacksonXmlProperty, JacksonXmlRootElement, JacksonXmlText}

// declaration consists of xsd:sequence the order of which is reflected in the field order
@JacksonXmlRootElement(namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2", localName = "MetaData")
case class MetaData(@JacksonXmlProperty(localName = "WCODataModelVersionCode", namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2")
                    wcoDataModelVersionCode: Option[String] = None,

                    @JacksonXmlProperty(localName = "WCOTypeName", namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2")
                    wcoTypeName: Option[String] = None,

                    @JacksonXmlProperty(localName = "ResponsibleCountryCode", namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2")
                    responsibleCountryCode: Option[String] = None,

                    @JacksonXmlProperty(localName = "ResponsibleAgencyName", namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2")
                    responsibleAgencyName: Option[String] = None,

                    @JacksonXmlProperty(localName = "AgencyAssignedCustomizationCode", namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2")
                    agencyAssignedCustomizationCode: Option[String] = None,

                    @JacksonXmlProperty(localName = "AgencyAssignedCustomizationVersionCode", namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2")
                    agencyAssignedCustomizationVersionCode: Option[String] = None,

                    @JacksonXmlProperty(localName = "Declaration", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                    declaration: Declaration)

// declaration consists of xsd:sequence the order of which is reflected in the field order
@JsonIgnoreProperties(Array(
  "amendments",
  "declarant",
  "exitOffice",
  "exporter",
  "goodsShipment",
  "obligationGuarantees",
  "presentationOffice",
  "supervisingOffice"
))
case class Declaration(@JacksonXmlProperty(localName = "AcceptanceDateTime", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       acceptanceDateTime: Option[AcceptanceDateTime] = None, // name="AcceptanceDateTime" type="ds:DeclarationAcceptanceDateTimeType" minOccurs="0"

                       @JacksonXmlProperty(localName = "FunctionCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       functionCode: Option[String] = None, // name="FunctionCode" type="ds:DeclarationFunctionCodeType" minOccurs="0"

                       @JacksonXmlProperty(localName = "FunctionalReferenceID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       functionalReferenceId: Option[String] = None, // name="FunctionalReferenceID" type="ds:DeclarationFunctionalReferenceIDType" minOccurs="0"

                       @JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       id: Option[String] = None, // name="ID" type="ds:DeclarationIdentificationIDType" minOccurs="0"

                       @JacksonXmlProperty(localName = "IssueDateTime", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       issueDateTime: Option[IssueDateTime] = None, // name="IssueDateTime" type="ds:DeclarationIssueDateTimeType" minOccurs="0"

                       @JacksonXmlProperty(localName = "IssueLocationID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       issueLocationId: Option[String] = None, // name="IssueLocationID" type="ds:DeclarationIssueLocationIdentificationIDType" minOccurs="0"

                       @JacksonXmlProperty(localName = "TypeCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       typeCode: Option[String] = None, // name="TypeCode" type="ds:DeclarationTypeCodeType" minOccurs="0"

                       @JacksonXmlProperty(localName = "GoodsItemQuantity", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       goodsItemQuantity: Option[Int] = None, // name="GoodsItemQuantity" type="ds:DeclarationGoodsItemQuantityType" minOccurs="0"

                       @JacksonXmlProperty(localName = "DeclarationOfficeID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       declarationOfficeId: Option[String] = None, // name="DeclarationOfficeID" type="ds:DeclarationDeclarationOfficeIDType" minOccurs="0"

                       @JacksonXmlProperty(localName = "InvoiceAmount", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       invoiceAmount: Option[InvoiceAmount] = None, // name="InvoiceAmount" type="ds:DeclarationInvoiceAmountType" minOccurs="0"

                       @JacksonXmlProperty(localName = "LoadingListQuantity", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       loadingListQuantity: Option[Int] = None, // name="LoadingListQuantity" type="ds:DeclarationLoadingListQuantityType" minOccurs="0"

                       @JacksonXmlProperty(localName = "TotalGrossMassMeasure", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       totalGrossMassMeasure: Option[MassMeasure] = None, // name="TotalGrossMassMeasure" type="ds:DeclarationTotalGrossMassMeasureType" minOccurs="0"

                       @JacksonXmlProperty(localName = "TotalPackageQuantity", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       totalPackageQuantity: Option[Int] = None, // name="TotalPackageQuantity" type="ds:DeclarationTotalPackageQuantityType" minOccurs="0"

                       @JacksonXmlProperty(localName = "SpecificCircumstancesCodeCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       specificCircumstancesCodeCode: Option[String] = None, // name="SpecificCircumstancesCodeCode" type="ds:DeclarationSpecificCircumstancesCodeCodeType" minOccurs="0"

                       @JacksonXmlProperty(localName = "Authentication", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       authentication: Option[Authentication] = None, // name="Authentication" minOccurs="0" maxOccurs="1"

                       @JacksonXmlProperty(localName = "Submitter", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       submitter: Option[Submitter] = None, // name="Submitter" minOccurs="0" maxOccurs="1"

                       @JacksonXmlElementWrapper(useWrapping = false)
                       @JacksonXmlProperty(localName = "AdditionalDocument", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       additionalDocuments: Seq[AdditionalDocument] = Seq.empty, // name="AdditionalDocument" minOccurs="0" maxOccurs="unbounded"

                       @JacksonXmlElementWrapper(useWrapping = false)
                       @JacksonXmlProperty(localName = "AdditionalInformation", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       additionalInformations: Seq[AdditionalInformation] = Seq.empty, // name="AdditionalInformation" minOccurs="0" maxOccurs="unbounded"

                       @JacksonXmlProperty(localName = "Agent", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       agent: Option[Agent] = None, // name="Agent" minOccurs="0" maxOccurs="1"
                       // TODO model amendment XML
                       amendments: Seq[String] = Seq.empty, //  name="Amendment" minOccurs="0" maxOccurs="unbounded"

                       @JacksonXmlElementWrapper(useWrapping = false)
                       @JacksonXmlProperty(localName = "AuthorisationHolder", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       authorisationHolders: Seq[AuthorisationHolder] = Seq.empty, // name="AuthorisationHolder" minOccurs="0" maxOccurs="unbounded"

                       @JacksonXmlProperty(localName = "BorderTransportMeans", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       borderTransportMeans: Option[BorderTransportMeans] = None, // name="BorderTransportMeans" minOccurs="0" maxOccurs="1"

                       @JacksonXmlElementWrapper(useWrapping = false)
                       @JacksonXmlProperty(localName = "CurrencyExchange", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       currencyExchanges: Seq[CurrencyExchange] = Seq.empty, // name="CurrencyExchange" minOccurs="0" maxOccurs="unbounded"
                       declarant: Option[Declarant] = None, // name="Declarant" minOccurs="0" maxOccurs="1"
                       exitOffice: Option[String] = None, // name="ExitOffice" minOccurs="0" maxOccurs="1"
                       exporter: Option[Exporter] = None, // name="Exporter" minOccurs="0" maxOccurs="1"
                       goodsShipment: Option[GoodsShipment] = None, // name="GoodsShipment" minOccurs="0" maxOccurs="1"
                       obligationGuarantees: Seq[ObligationGuarantee] = Seq.empty, // name="ObligationGuarantee" minOccurs="0" maxOccurs="unbounded"
                       presentationOffice: Option[String] = None, // name="PresentationOffice" minOccurs="0" maxOccurs="1"
                       supervisingOffice: Option[SupervisingOffice] = None) // name="SupervisingOffice" minOccurs="0" maxOccurs="1"

case class CurrencyExchange(@JacksonXmlProperty(localName = "CurrencyTypeCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                            currencyTypeCode: Option[String] = None, // max 3 chars [a-zA-Z] ISO 4217 3-alpha code

                            @JacksonXmlProperty(localName = "RateNumeric", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                            rateNumeric: Option[BigDecimal] = None) // decimal with scale of 12 and precision of 5

case class Agent(@JacksonXmlProperty(localName = "Name", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                 name: Option[String] = None, // max 70 chars

                 @JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                 id: Option[String] = None, // max 17 chars

                 @JacksonXmlProperty(localName = "FunctionCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                 functionCode: Option[String] = None, // max 3 chars

                 @JacksonXmlProperty(localName = "Address", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                 address: Option[Address] = None)

case class Submitter(@JacksonXmlProperty(localName = "Name", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                     name: Option[String] = None, // max length 70

                     @JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                     id: Option[String] = None, // max length 17

                     @JacksonXmlProperty(localName = "Address", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                     address: Option[Address] = None)

case class Authentication(@JacksonXmlProperty(localName = "Authentication", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                          authentication: Option[String] = None,

                          @JacksonXmlProperty(localName = "Authenticator", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                          authenticator: Option[Authenticator] = None)

case class Authenticator(@JacksonXmlProperty(localName = "Name", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                         name: Option[String] = None)

case class AcceptanceDateTime(@JacksonXmlProperty(localName = "DateTimeString", namespace = "urn:wco:datamodel:WCO:Declaration_DS:DMS:2")
                              dateTimeString: DateTimeString)

case class IssueDateTime(@JacksonXmlProperty(localName = "DateTimeString", namespace = "urn:wco:datamodel:WCO:Declaration_DS:DMS:2")
                         dateTimeString: DateTimeString)

case class DateTimeString(@JacksonXmlProperty(localName = "formatCode", isAttribute = true)
                          formatCode: String,

                          @JacksonXmlText
                          value: String)

case class InvoiceAmount(@JacksonXmlProperty(localName = "currencyID", isAttribute = true)
                         currencyId: Option[String] = None,

                         @JacksonXmlText
                         value: Option[BigDecimal] = None)

case class MassMeasure(@JacksonXmlProperty(localName = "unitCode", isAttribute = true)
                       unitCode: Option[String] = None,

                       @JacksonXmlText
                       value: Option[BigDecimal] = None)

case class AdditionalDocument(@JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                              id: Option[String] = None, // max 70 chars

                              @JacksonXmlProperty(localName = "CategoryCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                              categoryCode: Option[String] = None, // max 3 chars

                              @JacksonXmlProperty(localName = "TypeCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                              typeCode: Option[String] = None, // max 3 chars

                              @JacksonXmlProperty(localName = "Name", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                              name: Option[String] = None,

                              @JacksonXmlProperty(localName = "LPCOExemptionCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                              lpcoExemptionCode: Option[String] = None)

case class AuthorisationHolder(@JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                               id: Option[String] = None, // max 17 chars

                               @JacksonXmlProperty(localName = "CategoryCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                               categoryCode: Option[String] = None) // max 4 chars

case class BorderTransportMeans(@JacksonXmlProperty(localName = "Name", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                                name: Option[String] = None, // max 35 chars,

                                @JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                                id: Option[String] = None, // max 35 chars,

                                @JacksonXmlProperty(localName = "IdentificationTypeCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                                identificationTypeCode: Option[String] = None, // max 17 chars

                                @JacksonXmlProperty(localName = "TypeCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                                typeCode: Option[String] = None, // max 4 chars

                                @JacksonXmlProperty(localName = "RegistrationNationalityCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                                registrationNationalityCode: Option[String] = None, // 2 chars [a-zA-Z] when present; presumably ISO 3166-1 alpha2

                                @JacksonXmlProperty(localName = "ModeCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                                modeCode: Option[Int] = None) // 0-9

case class Declarant(id: String)

case class Exporter(name: String,
                    address: Address)

case class Address(@JacksonXmlProperty(localName = "CityName", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                   cityName: Option[String] = None, // max length 35

                   @JacksonXmlProperty(localName = "CountryCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                   countryCode: Option[String] = None, // 2 chars [a-zA-Z] ISO 3166-1 2-alpha

                   @JacksonXmlProperty(localName = "CountrySubDivisionCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                   countrySubDivisionCode: Option[String] = None, // max 9 chars

                   @JacksonXmlProperty(localName = "CountrySubDivisionName", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                   countrySubDivisionName: Option[String] = None, // max 35 chars

                   @JacksonXmlProperty(localName = "Line", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                   line: Option[String] = None, // max 70 chars

                   @JacksonXmlProperty(localName = "PostcodeID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
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

case class AdditionalInformation(@JacksonXmlProperty(localName = "StatementCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                                 statementCode: Option[String] = None, // max 17 chars

                                 @JacksonXmlProperty(localName = "StatementDescription", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                                 statementDescription: Option[String] = None, // max 512 chars

                                 @JacksonXmlProperty(localName = "StatementTypeCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                                 statementTypeCode: Option[String] = None, // max 3 chars

                                 @JacksonXmlElementWrapper(useWrapping = false)
                                 @JacksonXmlProperty(localName = "Pointer", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                                 pointers: Seq[Pointer] = Seq.empty)

case class Pointer(@JacksonXmlProperty(localName = "SequenceNumeric", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                   sequenceNumeric: Option[Int] = None, // min 0 max 99999

                   @JacksonXmlProperty(localName = "DocumentSectionCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                   documentSectionCode: Option[String] = None, // max 3 chars

                   @JacksonXmlProperty(localName = "TagID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
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
