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

package controllers

import play.api.data.validation.ValidationError
import play.api.i18n.Messages

import scala.collection.mutable


object ViewUtils {

  //Declarant fields
  val declarantName = "MetaData_declaration_declarant_name"
  val declarantAddressLine = "MetaData_declaration_declarant_address_line"
  val declarantAddressCityName = "MetaData_declaration_declarant_address_cityName"
  val declarantAddressCountryCode = "MetaData_declaration_declarant_address_countryCode"
  val declarantAddressPostcode = "MetaData_declaration_declarant_address_postcodeId"
  val declarantEori = "MetaData_declaration_declarant_id"

  //Exporter fields
  val exporterName = "MetaData_declaration_exporter_name"
  val exporterAddressLine = "MetaData_declaration_exporter_address_line"
  val exporterAddressCityName = "MetaData_declaration_exporter_address_cityName"
  val exporterAddressCountryCode = "MetaData_declaration_exporter_address_countryCode"
  val exporterAddressPostcode = "MetaData_declaration_exporter_address_postcodeId"
  val exporterEori = "MetaData_declaration_exporter_id"

  //Represetative fields
  val agentName = "MetaData_declaration_agent_name"
  val agentAddressLine = "MetaData_declaration_agent_address_line"
  val agentAddressCityName = "MetaData_declaration_agent_address_cityName"
  val agentAddressCountryCode = "MetaData_declaration_agent_address_countryCode"
  val agentAddressPostcode = "MetaData_declaration_agent_address_postcodeId"
  val agentEori = "MetaData_declaration_agent_id"
  val agentFunctionCode = "MetaData_declaration_agent_functionCode"

  //Importer fields
  val importerName = "MetaData_declaration_importer_name"
  val importerAddressLine = "MetaData_declaration_importer_address_line"
  val importerAddressCityName = "MetaData_declaration_importer_address_cityName"
  val importerAddressCountryCode = "MetaData_declaration_importer_address_countryCode"
  val importerAddressPostcode = "MetaData_declaration_importer_address_postcodeId"
  val importerEori = "MetaData_declaration_importer_id"

  //Seller fields
  val sellerName = "MetaData_declaration_seller_name"
  val sellerAddressLine = "MetaData_declaration_seller_address_line"
  val sellerAddressCityName = "MetaData_declaration_seller_address_cityName"
  val sellerAddressCountryCode = "MetaData_declaration_seller_address_countryCode"
  val sellerAddressPostcode = "MetaData_declaration_seller_address_postcodeId"
  val sellerCommunicationID = "MetaData_declaration_seller_communications_id"
  val sellerEori = "MetaData_declaration_seller_id"

  //Buyer fields
  val buyerName = "MetaData_declaration_buyer_name"
  val buyerAddressLine = "MetaData_declaration_buyer_address_line"
  val buyerAddressCityName = "MetaData_declaration_buyer_address_cityName"
  val buyerAddressCountryCode = "MetaData_declaration_buyer_address_countryCode"
  val buyerAddressPostcode = "MetaData_declaration_buyer_address_postcodeId"
  val buyerCommunicationID = "MetaData_declaration_buyer_communications_id"
  val buyerEori = "MetaData_declaration_buyer_id"

  //Additional supply chain actors fields
  val aeoMutualRecognitionPartiesID = "MetaData_declaration_aeoMutualRecognitionParties_id"
  val aeoMutualRecognitionPartyRoleCode = "MetaData_declaration_aeoMutualRecognitionParties_roleCode"
  val authorisationHolderID = "MetaData_declaration_authorisationHolders_id"
  val authorisationHolderCategoryCode = "MetaData_declaration_authorisationHolders_categoryCode"

  //references screen fields
 //functionalReferenceId: Option[String] = None, // max 35 chars
  val declarantFunctionalReferenceID = "MetaData_declaration_functionalReferenceID"
  //traderAssignedReferenceId: Option[String] = None) // max 35 chars
  val referenceNumberUCR1 = "MetaData_declaration_goodsShipment_ucr_traderAssignedReferenceId"
  //TODO:governmentAgencyGoodsItems is a seq of elements needs mapping the sequence
  //traderAssignedReferenceId: Option[String] = None) // max 35 chars
  val referenceNumberUCR2 = "MetaData_declaration_goodsShipment_governmentAgencyGoodsItems_ucr_traderAssignedReferenceID"
  //typeCode: Option[String] = None, // max 3 chars; MUST be "INV" in cancellation use case
  //TODO: Same xml element mapped for declarationType and additionalDeclarationType
  val declarationType = "MetaData_declaration_typeCode"
  //typeCode: Option[String] = None, // max 3 chars; MUST be "INV" in cancellation use case
  val additionalDeclarationType = "MetaData_declaration_typeCode"


  val borderTransportMeansModeCodes = mutable.LinkedHashMap("C"-> "C","F"->"F","Z"-> "Z","Y"->"Y")

  val PartySubRoleTypes = mutable.LinkedHashMap("CS"-> "CS",
    "FR1"-> "FR1",
    "FR2"-> "FR2",
    "FR3"-> "FR3",
    "FR4"-> "FR4",
    "FW"-> "FW",
    "MF"-> "MF",
    "WH"-> "WH")

  val PartyRoleAuthorizationTypes = mutable.LinkedHashMap("ACE"-> "ACE",
    "ACP"-> "ACP",
    "ACR"-> "ACR",
    "ACT"-> "ACT",
    "AEOC"-> "AEOC",
    "AEOF"-> "AEOF",
    "AEOS"-> "AEOS",
    "APEX"-> "APEX",
    "AWB"-> "AWB",
    "BOI"-> "BOI",
    "BTI"-> "BTI",
    "CCL"-> "CCL",
    "CGU"-> "CGU",
    "CSDR"-> "CSDR",
    "CSE"-> "CSE",
    "CVA"-> "CVA",
    "CW1"-> "CW1",
    "CW2"-> "CW2",
    "CWP"-> "CWP",
    "DEP"-> "DEP",
    "DPO"-> "DPO",
    "EIR"-> "EIR",
    "EORI"-> "EORI",
    "EPSS"-> "EPSS",
    "ETD"-> "ETD",
    "EUS"-> "EUS",
    "EXEE"-> "EXEE",
    "EXOR"-> "EXOR",
    "EXW"-> "EXW",
    "EXWH"-> "EXWH",
    "FAS"-> "FAS",
    "FZ"-> "FZ",
    "GGA"-> "GGA",
    "GVS"-> "GVS",
    "IPO"-> "IPO",
    "LVBI"-> "LVBI",
    "OPO"-> "OPO",
    "REM"-> "REM",
    "REP"-> "REP",
    "REX"-> "REX",
    "RSS"-> "RSS",
    "SAS"-> "SAS",
    "SASP"-> "SASP",
    "SDE"-> "SDE",
    "SIVA"-> "SIVA",
    "SSE"-> "SSE",
    "TEA"-> "TEA",
    "TEAH"-> "TEAH",
    "TRD"-> "TRD",
    "TST"-> "TST",
    "UKCS"-> "UKCS")


  val countryOptions = mutable.LinkedHashMap("AF"->"Afghanistan",
    "AX"->"Aland Islands",
    "AL"->"Albania",
    "DZ"->"Algeria",
    "AS"->"American Samoa",
    "AD"->"Andorra",
    "AO"->"Angola",
    "AI"->"Anguilla",
    "AQ"->"Antarctica",
    "AG"->"Antigua and Barbuda",
    "AR"->"Argentina",
    "AM"->"Armenia",
    "AW"->"Aruba",
    "AU"->"Australia",
    "AT"->"Austria",
    "AZ"->"Azerbaijan",
    "BS"->"Bahamas",
    "BH"->"Bahrain",
    "BD"->"Bangladesh",
    "BB"->"Barbados",
    "BY"->"Belarus",
    "BE"->"Belgium",
    "BZ"->"Belize",
    "BJ"->"Benin",
    "BM"->"Bermuda",
    "BT"->"Bhutan",
    "BO"->"Bolivia",
    "BA"->"Bosnia and Herzegovina",
    "BW"->"Botswana",
    "BV"->"Bouvet Island",
    "BR"->"Brazil",
    "IO"->"British Indian Ocean Territory"
    ,"BN"->"Brunei Darussalam"
    ,"BG"->"Bulgaria"
    ,"BF"->"Burkina Faso"
    ,"BI"->"Burundi"
    ,"KH"->"Cambodia"
    ,"CM"->"Cameroon"
    ,"CA"->"Canada"
    ,"CV"->"Cape Verde"
    ,"KY"->"Cayman Islands"
    ,"CF"->"Central African Republic"
    ,"TD"->"Chad"
    ,"CL"->"Chile"
    ,"CN"->"China"
    ,"HK"->"Hong Kong, SAR China"
    ,"MO"->"Macao, SAR China"
    ,"CX"->"Christmas Island"
    ,"CC"->"Cocos (Keeling) Islands"
    ,"CO"->"Colombia"
    ,"KM"->"Comoros"
    ,"CG"->"Congo (Brazzaville)"
    ,"CD"->"Congo, (Kinshasa)"
    ,"CK"->"Cook Islands"
    ,"CR"->"Costa Rica"
    ,"CI"->"Côte d'Ivoire"
    ,"HR"->"Croatia"
    ,"CU"->"Cuba"
    ,"CY"->"Cyprus"
    ,"CZ"->"Czech Republic"
    ,"DK"->"Denmark"
    ,"DJ"->"Djibouti"
    ,"DM"->"Dominica"
    ,"DO"->"Dominican Republic"
    ,"EC"->"Ecuador"
    ,"EG"->"Egypt"
    ,"SV"->"El Salvador"
    ,"GQ"->"Equatorial Guinea"
    ,"ER"->"Eritrea"
    ,"EE"->"Estonia"
    ,"ET"->"Ethiopia"
    ,"FK"->"Falkland Islands (Malvinas)"
    ,"FO"->"Faroe Islands"
    ,"FJ"->"Fiji"
    ,"FI"->"Finland"
    ,"FR"->"France"
    ,"GF"->"French Guiana"
    ,"PF"->"French Polynesia"
    ,"TF"->"French Southern Territories"
    ,"GA"->"Gabon"
    ,"GM"->"Gambia"
    ,"GE"->"Georgia"
    ,"DE"->"Germany"
    ,"GH"->"Ghana"
    ,"GI"->"Gibraltar"
    ,"GR"->"Greece"
    ,"GL"->"Greenland"
    ,"GD"->"Grenada"
    ,"GP"->"Guadeloupe"
    ,"GU"->"Guam"
    ,"GT"->"Guatemala"
    ,"GG"->"Guernsey"
    ,"GN"->"Guinea"
    ,"GW"->"Guinea-Bissau"
    ,"GY"->"Guyana"
    ,"HT"->"Haiti"
    ,"HM"->"Heard and Mcdonald Islands"
    ,"VA"->"Holy See (Vatican City State)"
    ,"HN"->"Honduras"
    ,"HU"->"Hungary"
    ,"IS"->"Iceland"
    ,"IN"->"India"
    ,"ID"->"Indonesia"
    ,"IR"->"Iran, Islamic Republic of"
    ,"IQ"->"Iraq"
    ,"IE"->"Ireland"
    ,"IM"->"Isle of Man"
    ,"IL"->"Israel"
    ,"IT"->"Italy"
    ,"JM"->"Jamaica"
    ,"JP"->"Japan"
    ,"JE"->"Jersey"
    ,"JO"->"Jordan"
    ,"KZ"->"Kazakhstan"
    ,"KE"->"Kenya"
    ,"KI"->"Kiribati"
    ,"KP"->"Korea (North)"
    ,"KR"->"Korea (South)"
    ,"KW"->"Kuwait"
    ,"KG"->"Kyrgyzstan"
    ,"LA"->"Lao PDR"
    ,"LV"->"Latvia"
    ,"LB"->"Lebanon"
    ,"LS"->"Lesotho"
    ,"LR"->"Liberia"
    ,"LY"->"Libya"
    ,"LI"->"Liechtenstein"
    ,"LT"->"Lithuania"
    ,"LU"->"Luxembourg"
    ,"MK"->"Macedonia, Republic of"
    ,"MG"->"Madagascar"
    ,"MW"->"Malawi"
    ,"MY"->"Malaysia"
    ,"MV"->"Maldives"
    ,"ML"->"Mali"
    ,"MT"->"Malta"
    ,"MH"->"Marshall Islands"
    ,"MQ"->"Martinique"
    ,"MR"->"Mauritania"
    ,"MU"->"Mauritius"
    ,"YT"->"Mayotte"
    ,"MX"->"Mexico"
    ,"FM"->"Micronesia, Federated States of"
    ,"MD"->"Moldova"
    ,"MC"->"Monaco"
    ,"MN"->"Mongolia"
    ,"ME"->"Montenegro"
    ,"MS"->"Montserrat"
    ,"MA"->"Morocco"
    ,"MZ"->"Mozambique"
    ,"MM"->"Myanmar"
    ,"NA"->"Namibia"
    ,"NR"->"Nauru"
    ,"NP"->"Nepal"
    ,"NL"->"Netherlands"
    ,"AN"->"Netherlands Antilles"
    ,"NC"->"New Caledonia"
    ,"NZ"->"New Zealand"
    ,"NI"->"Nicaragua"
    ,"NE"->"Niger"
    ,"NG"->"Nigeria"
    ,"NU"->"Niue"
    ,"NF"->"Norfolk Island"
    ,"MP"->"Northern Mariana Islands"
    ,"NO"->"Norway"
    ,"OM"->"Oman"
    ,"PK"->"Pakistan"
    ,"PW"->"Palau"
    ,"PS"->"Palestinian Territory"
    ,"PA"->"Panama"
    ,"PG"->"Papua New Guinea"
    ,"PY"->"Paraguay"
    ,"PE"->"Peru"
    ,"PH"->"Philippines"
    ,"PN"->"Pitcairn"
    ,"PL"->"Poland"
    ,"PT"->"Portugal"
    ,"PR"->"Puerto Rico"
    ,"QA"->"Qatar"
    ,"RE"->"Réunion"
    ,"RO"->"Romania"
    ,"RU"->"Russian Federation"
    ,"RW"->"Rwanda"
    ,"BL"->"Saint-Barthélemy"
    ,"SH"->"Saint Helena"
    ,"KN"->"Saint Kitts and Nevis"
    ,"LC"->"Saint Lucia"
    ,"MF"->"Saint-Martin (French part)"
    ,"PM"->"Saint Pierre and Miquelon"
    ,"VC"->"Saint Vincent and Grenadines"
    ,"WS"->"Samoa"
    ,"SM"->"San Marino"
    ,"ST"->"Sao Tome and Principe"
    ,"SA"->"Saudi Arabia"
    ,"SN"->"Senegal"
    ,"RS"->"Serbia"
    ,"SC"->"Seychelles"
    ,"SL"->"Sierra Leone"
    ,"SG"->"Singapore"
    ,"SK"->"Slovakia"
    ,"SI"->"Slovenia"
    ,"SB"->"Solomon Islands"
    ,"SO"->"Somalia"
    ,"ZA"->"South Africa"
    ,"GS"->"South Georgia and the South Sandwich Islands"
    ,"SS"->"South Sudan"
    ,"ES"->"Spain"
    ,"LK"->"Sri Lanka"
    ,"SD"->"Sudan"
    ,"SR"->"Suriname"
    ,"SJ"->"Svalbard and Jan Mayen Islands"
    ,"SZ"->"Swaziland"
    ,"SE"->"Sweden"
    ,"CH"->"Switzerland"
    ,"SY"->"Syrian Arab Republic (Syria)"
    ,"TW"->"Taiwan, Republic of China"
    ,"TJ"->"Tajikistan"
    ,"TZ"->"Tanzania, United Republic of"
    ,"TH"->"Thailand"
    ,"TL"->"Timor-Leste"
    ,"TG"->"Togo"
    ,"TK"->"Tokelau"
    ,"TO"->"Tonga"
    ,"TT"->"Trinidad and Tobago"
    ,"TN"->"Tunisia"
    ,"TR"->"Turkey"
    ,"TM"->"Turkmenistan"
    ,"TC"->"Turks and Caicos Islands"
    ,"TV"->"Tuvalu"
    ,"UG"->"Uganda"
    ,"UA"->"Ukraine"
    ,"AE"->"United Arab Emirates"
    ,"GB"->"United Kingdom"
    ,"US"->"United States of America"
    ,"UM"->"US Minor Outlying Islands"
    ,"UY"->"Uruguay"
    ,"UZ"->"Uzbekistan"
    ,"VU"->"Vanuatu"
    ,"VE"->"Venezuela (Bolivarian Republic)"
    ,"VN"->"Viet Nam"
    ,"VI"->"Virgin Islands, US"
    ,"WF"->"Wallis and Futuna Islands"
    ,"EH"->"Western Sahara"
    ,"YE"->"Yemen"
    ,"ZM"->"Zambia"
    ,"ZW"->"Zimbabwe")

  def getError(key: String, errors: Map[String, ValidationError])(implicit messages: Messages) = {
    if(errors.get(key).isDefined) {
      errors.get(key).get.messages.map(messages(_))}
  }
}

