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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.{JacksonXmlElementWrapper, JacksonXmlProperty, JacksonXmlRootElement, JacksonXmlText}

/*
MetaData and Declaration schema generally consists of xsd:sequence definitions the order of which is reflected in the
field order of the case classes.

DO NOT CHANGE the field order on any of the case classes unless the XSD requires it.
 */

@JacksonXmlRootElement(namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2", localName = "MetaData")
case class MetaData(@JacksonXmlProperty(localName = "WCODataModelVersionCode", namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2")
                    wcoDataModelVersionCode: Option[String] = None, // max 6 chars

                    @JacksonXmlProperty(localName = "WCOTypeName", namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2")
                    wcoTypeName: Option[String] = None, // no constraint

                    @JacksonXmlProperty(localName = "ResponsibleCountryCode", namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2")
                    responsibleCountryCode: Option[String] = None, // max 2 chars - ISO 3166-1 alpha2 code

                    @JacksonXmlProperty(localName = "ResponsibleAgencyName", namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2")
                    responsibleAgencyName: Option[String] = None, // max 70 chars

                    @JacksonXmlProperty(localName = "AgencyAssignedCustomizationCode", namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2")
                    agencyAssignedCustomizationCode: Option[String] = None, // max 6 chars

                    @JacksonXmlProperty(localName = "AgencyAssignedCustomizationVersionCode", namespace = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2")
                    agencyAssignedCustomizationVersionCode: Option[String] = None, // max 3 chars

                    @JacksonXmlProperty(localName = "Declaration", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                    declaration: Declaration = Declaration())

@JsonIgnoreProperties(Array(
  "amendments",
  "goodsShipment"
))
case class Declaration(@JacksonXmlProperty(localName = "AcceptanceDateTime", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       acceptanceDateTime: Option[AcceptanceDateTime] = None,

                       @JacksonXmlProperty(localName = "FunctionCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       functionCode: Option[Int] = None, // unsigned int in enumeration of [9, 13, 14]

                       @JacksonXmlProperty(localName = "FunctionalReferenceID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       functionalReferenceId: Option[String] = None, // max 35 chars

                       @JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       id: Option[String] = None, // max 70 chars

                       @JacksonXmlProperty(localName = "IssueDateTime", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       issueDateTime: Option[IssueDateTime] = None,

                       @JacksonXmlProperty(localName = "IssueLocationID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       issueLocationId: Option[String] = None, // max 5 chars

                       @JacksonXmlProperty(localName = "TypeCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       typeCode: Option[String] = None, // max 3 chars

                       @JacksonXmlProperty(localName = "GoodsItemQuantity", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       goodsItemQuantity: Option[Int] = None, // unsigned int max 99999

                       @JacksonXmlProperty(localName = "DeclarationOfficeID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       declarationOfficeId: Option[String] = None, // max 17 chars

                       @JacksonXmlProperty(localName = "InvoiceAmount", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       invoiceAmount: Option[InvoiceAmount] = None,

                       @JacksonXmlProperty(localName = "LoadingListQuantity", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       loadingListQuantity: Option[Int] = None, // unsigned int max 99999

                       @JacksonXmlProperty(localName = "TotalGrossMassMeasure", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       totalGrossMassMeasure: Option[MassMeasure] = None,

                       @JacksonXmlProperty(localName = "TotalPackageQuantity", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       totalPackageQuantity: Option[Int] = None, // unsigned int max 99999999

                       @JacksonXmlProperty(localName = "SpecificCircumstancesCodeCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       specificCircumstancesCode: Option[String] = None, // max 3 chars

                       @JacksonXmlProperty(localName = "Authentication", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       authentication: Option[Authentication] = None,

                       @JacksonXmlProperty(localName = "Submitter", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       submitter: Option[Submitter] = None,

                       @JacksonXmlElementWrapper(useWrapping = false)
                       @JacksonXmlProperty(localName = "AdditionalDocument", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       additionalDocuments: Seq[AdditionalDocument] = Seq.empty,

                       @JacksonXmlElementWrapper(useWrapping = false)
                       @JacksonXmlProperty(localName = "AdditionalInformation", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       additionalInformations: Seq[AdditionalInformation] = Seq.empty,

                       @JacksonXmlProperty(localName = "Agent", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       agent: Option[Agent] = None,

                       // TODO model amendment XML
                       amendments: Seq[String] = Seq.empty, //  name="Amendment" minOccurs="0" maxOccurs="unbounded"

                       @JacksonXmlElementWrapper(useWrapping = false)
                       @JacksonXmlProperty(localName = "AuthorisationHolder", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       authorisationHolders: Seq[AuthorisationHolder] = Seq.empty,

                       @JacksonXmlProperty(localName = "BorderTransportMeans", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       borderTransportMeans: Option[BorderTransportMeans] = None,

                       @JacksonXmlElementWrapper(useWrapping = false)
                       @JacksonXmlProperty(localName = "CurrencyExchange", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       currencyExchanges: Seq[CurrencyExchange] = Seq.empty,

                       @JacksonXmlProperty(localName = "Declarant", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       declarant: Option[Declarant] = None,

                       @JacksonXmlProperty(localName = "ExitOffice", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       exitOffice: Option[ExitOffice] = None,

                       @JacksonXmlProperty(localName = "Exporter", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       exporter: Option[Exporter] = None,

                       // TODO model goods shipment
                       goodsShipment: Option[GoodsShipment] = None, // name="GoodsShipment" minOccurs="0" maxOccurs="1"

                       @JacksonXmlElementWrapper(useWrapping = false)
                       @JacksonXmlProperty(localName = "ObligationGuarantee", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       obligationGuarantees: Seq[ObligationGuarantee] = Seq.empty,

                       @JacksonXmlProperty(localName = "PresentationOffice", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       presentationOffice: Option[PresentationOffice] = None,

                       @JacksonXmlProperty(localName = "SupervisingOffice", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                       supervisingOffice: Option[SupervisingOffice] = None)

case class PresentationOffice(@JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                              id: Option[String]) // max 17 chars

case class ExitOffice(@JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                      id: Option[String] = None) // max 17 chars

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
                          authentication: Option[String] = None, // max 256 chars

                          @JacksonXmlProperty(localName = "Authenticator", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                          authenticator: Option[Authenticator] = None)

case class Authenticator(@JacksonXmlProperty(localName = "Name", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                         name: Option[String] = None) // max 70 chars

case class AcceptanceDateTime(@JacksonXmlProperty(localName = "DateTimeString", namespace = "urn:wco:datamodel:WCO:Declaration_DS:DMS:2")
                              dateTimeString: DateTimeString)

case class IssueDateTime(@JacksonXmlProperty(localName = "DateTimeString", namespace = "urn:wco:datamodel:WCO:Declaration_DS:DMS:2")
                         dateTimeString: DateTimeString)

case class DateTimeString(@JacksonXmlProperty(localName = "formatCode", isAttribute = true)
                          formatCode: String, // either "102" or "304"

                          @JacksonXmlText
                          value: String) // max 35 chars

case class InvoiceAmount(@JacksonXmlProperty(localName = "currencyID", isAttribute = true)
                         currencyId: Option[String] = None, // and ISO 4217 3 char currency code (i.e. "GBP")

                         @JacksonXmlText
                         value: Option[BigDecimal] = None) // scale of 16 and precision of 3

case class MassMeasure(@JacksonXmlProperty(localName = "unitCode", isAttribute = true)
                       unitCode: Option[String] = None, // min 1 max 5 chars when specified

                       @JacksonXmlText
                       value: Option[BigDecimal] = None) // scale 16 precision 6

case class AdditionalDocument(@JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                              id: Option[String] = None, // max 70 chars

                              @JacksonXmlProperty(localName = "CategoryCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                              categoryCode: Option[String] = None, // max 3 chars

                              @JacksonXmlProperty(localName = "TypeCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                              typeCode: Option[String] = None) // max 3 chars

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

case class Declarant(@JacksonXmlProperty(localName = "Name", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                     name: Option[String] = None, // max 70 chars

                     @JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                     id: Option[String] = None, // max 17 chars

                     @JacksonXmlProperty(localName = "Address", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                     address: Option[Address] = None,

                     @JacksonXmlProperty(localName = "Contact", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                     contact: Option[Contact] = None,

                     @JacksonXmlElementWrapper(useWrapping = false)
                     @JacksonXmlProperty(localName = "Communication", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                     communications: Seq[Communication] = Seq.empty)

case class Contact(@JacksonXmlProperty(localName = "Name", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                   name: Option[String] = None) // max 70 chars

case class Communication(@JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                         id: Option[String] = None, // max 50 chars

                         @JacksonXmlProperty(localName = "TypeCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                         typeCode: Option[String] = None) // max 3 chars

case class Exporter(@JacksonXmlProperty(localName = "Name", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                    name: Option[String] = None, // max 70 chars

                    @JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                    id: Option[String] = None, // max 17 chars

                    @JacksonXmlProperty(localName = "Address", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                    address: Option[Address] = None,

                    @JacksonXmlElementWrapper(useWrapping = false)
                    @JacksonXmlProperty(localName = "Contact", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                    contacts: Seq[Contact] = Seq.empty,

                    @JacksonXmlElementWrapper(useWrapping = false)
                    @JacksonXmlProperty(localName = "Communication", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                    communications: Seq[Communication] = Seq.empty)

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

case class ObligationGuarantee(@JacksonXmlProperty(localName = "AmountAmount", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                               amount: Option[BigDecimal] = None, // scale 16 precision 3

                               @JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                               id: Option[String] = None, // max 70 chars

                               @JacksonXmlProperty(localName = "ReferenceID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                               referenceId: Option[String] = None, // max 35 chars

                               @JacksonXmlProperty(localName = "SecurityDetailsCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                               securityDetailsCode: Option[String] = None, // max 3 chars

                               @JacksonXmlProperty(localName = "AccessCode", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                               accessCode: Option[String] = None, // max 4 chars

                               @JacksonXmlProperty(localName = "GuaranteeOffice", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                               guaranteeOffice: Option[GuaranteeOffice] = None)

case class GuaranteeOffice(@JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                           id: Option[String] = None) // max 17 chars

case class SupervisingOffice(@JacksonXmlProperty(localName = "ID", namespace = "urn:wco:datamodel:WCO:DEC-DMS:2")
                             id: Option[String] = None) // max 17 chars
