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

package controllers

import domain.{GoodsItemValueInformation, GovernmentAgencyGoodsItem}
import org.joda.time.DateTime
import uk.gov.hmrc.wco.dec._

trait TestData {

  val goodsItemValueInformation:GoodsItemValueInformation = GoodsItemValueInformation(Some(30.00), 123,
    Some(Amount(Some("GBP"),Some(123))), Some(333))

  val measure = Measure(Some("12345"), Some(30.30))
  val address = Address(Some("CityName"), Some("UK"))

  val additionalDocuments =Seq(GovernmentAgencyGoodsItemAdditionalDocument(Some("123"),
    Some(DateTimeElement(DateTimeString("102",DateTime.now.toString))),Some("id1"),Some("name12"),Some("123"),Some("321"),
    Some(GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(Some("SubmitterName 1"),Some("678")))))

  val goodsItemAdditionalInfo = Seq(AdditionalInformation(Some("StatementCode 1"),Some("StatementDescription-2"),Some("567")))

  val roleBasedParties = Seq(RoleBasedParty(Some("id1234"), Some("444")))

  val goodsItemGovProcedures = Seq(GovernmentProcedure(Some("current"), Some("prev")))
  val namedEntityWithAddress = Seq(NamedEntityWithAddress(Some("name"), Some("id1"), Some(address)))

  val packagings = Seq(Packaging(Some(1234), Some("marksNumbersId1"), Some(344), Some("22"),
    Some("packingMaterialDescription"), Some(333),Some(333),Some(333), Some(measure)))

  val goodsItemOrigins = Seq(Origin(Some("UK"), Some("NE44 9PF"), Some("333")))

  val previousDocuments = Seq(PreviousDocument(Some("444"), Some("Id1"), Some("555"),Some(1)))

  val goodsItem = GovernmentAgencyGoodsItem(additionalDocuments = additionalDocuments,
    additionalInformations = goodsItemAdditionalInfo,
    aeoMutualRecognitionParties = roleBasedParties,
    governmentProcedures = goodsItemGovProcedures,
    origins = goodsItemOrigins,
    packagings = packagings,
    manufacturers = namedEntityWithAddress,
      previousDocuments = previousDocuments)
}
