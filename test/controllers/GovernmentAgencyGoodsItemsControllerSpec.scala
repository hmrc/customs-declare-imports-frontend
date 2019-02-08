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
import domain.features.Feature
import generators.Generators
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.prop.PropertyChecks
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.assertions.{HtmlAssertions, HttpAssertions}
import uk.gov.hmrc.customs.test.behaviours._
import uk.gov.hmrc.wco.dec.{status => _, _}

class GovernmentAgencyGoodsItemsControllerSpec extends CustomsSpec
  with AuthenticationBehaviours
  with FeatureBehaviours
  with RequestHandlerBehaviours
  with HttpAssertions
  with HtmlAssertions
  with Generators
  with PropertyChecks {

  val goodsItemsPageUri = uriWithContextPath("/submit-declaration-goods/goods-item-value")
  val goodsItemsUri = uriWithContextPath("/submit-declaration-goods/add-gov-agency-goods-item")
  val navigateToSelectedGoodsItemPageUri = uriWithContextPath("/submit-declaration-goods/add-gov-agency-goods-items")
  val addOriginsPageUri = uriWithContextPath("/submit-declaration-goods/add-origins")
  val addManufacturersPageUri = uriWithContextPath("/submit-declaration-goods/add-manufacturers")
  val get = "GET"
  val postMethod = "POST"

  "GovernmentAgencyGoodsItemsController" should {

    "display GoodsItems values with single field Page" in withFeatures((enabled(Feature.submit))) {
      withSignedInUser() { (headers, session, tags) =>
        withCaching(None)
        withRequest(get, goodsItemsPageUri, headers, session, tags) { resp =>
          val content = contentAsString(resp)
          content must include("customsValueAmount")
          content must include("statisticalValueAmount.currencyId")
          content must include("statisticalValueAmount.value")
          content must include("transactionNatureCode")
          content must include("destination.countryCode")
          content must include("destination.regionId")
          content must include("ucr.id")
          content must include("ucr.traderAssignedReferenceId")
          content must include("exportCountry.id")
          content must include("valuationAdjustment.additionCode")
        }
      }
    }


    "display GoodsItems values Page with pre-populated data that user has entered before " in withFeatures((enabled(Feature.submit))) {
      withSignedInUser() { (headers, session, tags) =>
        withCaching(None)
        withRequest(get, goodsItemsPageUri, headers, session, tags) { resp =>
          val content = contentAsString(resp)
          content must include("customsValueAmount")
          content must include("statisticalValueAmount.currencyId")
          content must include("statisticalValueAmount.value")
          content must include("transactionNatureCode")
          content must include("destination.countryCode")
          content must include("destination.regionId")
          content must include("ucr.id")
          content must include("ucr.traderAssignedReferenceId")
          content must include("exportCountry.id")
          content must include("valuationAdjustment.additionCode")
        }
      }
    }
    val invalidPayload = Map(
      "customsValueAmount" -> "-0",
      "statisticalValueAmount.currencyId" -> "name1nasdfghlertghoy asdflgothidlglfdleasdflksdf",
      "statisticalValueAmount.value" -> "Address1 Address1 Address1 Address1 Address1 Address1 Address1 Address1",
      "transactionNatureCode" -> "12345678912341234",
      "destination.countryCode" -> "references",
      "destination.regionId" -> "ID1234567",
      "ucr.id" -> "ID1234567",
      "ucr.traderAssignedReferenceId" -> "ID1234567",
      "exportCountry.id" -> "ID1234567",
      "valuationAdjustment.additionCode" -> "ID1234567",
      "submit" -> "Add"
    )

    //TODO random validations tested should revisit
    "validate user input data on click of Add" in withFeatures(enabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>
        withRequestAndFormBody(postMethod, goodsItemsPageUri, headers, session, tags, invalidPayload) { resp =>
          val stringResult = contentAsString(resp)
          stringResult must include("customs Value Amount must not be negative")
          stringResult must include("id=\"error-message-sequenceNumeric-input\">This field is required")
          stringResult must include("Real number value expected")
          stringResult must include("id=\"error-message-statisticalValueAmount_currencyId-input\">Currency ID is not a valid currency")
          stringResult must include("valuationAdjustment should be less than or equal to 4 characters")
          stringResult must include("export Country code should be less than or equal to 2 characters")
          stringResult must include("country code is only 3 characters")
          stringResult must include("id=\"error-message-transactionNatureCode-input\">Numeric value expected")
        }
      }
    }

    val validPayload = Map(
      "customsValueAmount" -> "30.00",
      "sequenceNumeric" -> "3",
      "statisticalValueAmount.currencyId" -> "GBP",
      "statisticalValueAmount.value" -> "3345",
      "transactionNatureCode" -> "123",
      "destination.countryCode" -> "UK",
      "destination.regionId" -> "UK",
      "ucr.id" -> "ID1",
      "ucr.traderAssignedReferenceId" -> "TRADER_REF-1",
      "exportCountry.id" -> "PL",
      "valuationAdjustment.additionCode" -> "DDC",
      "submit" -> "Add"
    )

    "navigate to good items page on click of Continue " in withFeatures(enabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>
        withCaching(None)
        withRequestAndFormBody(postMethod, goodsItemsPageUri, headers, session, tags, validPayload) { resp =>
          val header = resp.futureValue.header
          status(resp) must be(SEE_OTHER)
          header.headers.get("Location") must
            be(Some("/customs-declare-imports/submit-declaration-goods/add-gov-agency-goods-item"))
        }
      }
    }

    "display good items page with pre-populated data on revisiting the page " in withFeatures(enabled(Feature.submit)) {
      withSignedInUser() { (headers, session, tags) =>


        arbitrary[GoodsItemValueInformation].sample.foreach { sampleData =>

          withCaching(Some(sampleData))
          withRequest(get, goodsItemsPageUri, headers, session, tags) { resp =>
            includesHtmlInput(resp, "customsValueAmount", value = sampleData.customsValueAmount.getOrElse("").toString)
            includesHtmlInput(resp, "sequenceNumeric", value = sampleData.sequenceNumeric.toString)
            includesHtmlInput(resp, "statisticalValueAmount.currencyId",
              value = sampleData.statisticalValueAmount.flatMap(_.currencyId).getOrElse("").toString)
            includesHtmlInput(resp, "statisticalValueAmount.value",
              value = sampleData.statisticalValueAmount.flatMap(_.value).getOrElse("").toString)
            includesHtmlInput(resp, "transactionNatureCode", value = sampleData.transactionNatureCode.getOrElse("").toString)
          }
        }
      }
    }
  }

  "display GoodsItems multiple items view Page with all the list fields" in withFeatures((enabled(Feature.submit))) {
    withSignedInUser() { (headers, session, tags) =>
      withCaching(None)
      withRequest(get, goodsItemsUri, headers, session, tags) { resp =>
        val content = contentAsHtml(resp)
        content should include element withAttrValue("id", "AddGAGIAddDoc")
        content should include element withAttrValue("id", "AddAdditionalInformation")
        content should include element withAttrValue("id", "AddMutualRecognitionParties")
        content should include element withAttrValue("id", "AddDomesticDutyTaxParties")
        content should include element withAttrValue("id", "AddGovernmentProcedures")
        content should include element withAttrValue("id", "AddOrigins")
        content should include element withAttrValue("id", "AddManufacturers")
        content should include element withAttrValue("id", "AddPackagings")
        content should include element withAttrValue("id", "AddPreviousDocuments")
        content should include element withAttrValue("id", "AddRefundRecipientParties")
        content should include element withAttrValue("id", "SaveGoodsItem")
      }
    }
  }


  "show addOriginsPage fields on navigating to the screen" in withFeatures((enabled(Feature.submit))) {
    withSignedInUser() { (headers, session, tags) =>
      withCaching(None)
      withRequest(get, addOriginsPageUri, headers, session, tags) { resp =>
        val content = contentAsHtml(resp)
        contentAsString(resp) must include("No Origins available")
        content should include element withAttrValue("name", "countryCode")
        content should include element withAttrValue("name", "regionId")
        content should include element withAttrValue("name", "typeCode")
      }
    }
  }

  "pre-populate addOriginsPage that are added/cached on user navigating to the screen" in withFeatures((enabled(Feature.submit))) {
    withSignedInUser() { (headers, session, tags) =>

      val goodsItemGen = arbitrary[Origin].map { o =>
        GovernmentAgencyGoodsItem(origins = List(o))
      }

      withCaching(goodsItemGen.sample)
      withRequest(get, addOriginsPageUri, headers, session, tags) { resp =>
        val content = contentAsHtml(resp)
        contentAsString(resp) must include("1 Goods Item Origins added")
        content should include element withAttrValue("name", "countryCode")
        content should include element withAttrValue("name", "regionId")
        content should include element withAttrValue("name", "typeCode")
      }
    }
  }

  "show addManufacturersPage fields on navigating to the screen" in withFeatures((enabled(Feature.submit))) {
    withSignedInUser() { (headers, session, tags) =>
      withCaching(None)
      withRequest(get, addManufacturersPageUri, headers, session, tags) { resp =>
        val content = contentAsHtml(resp)
        contentAsString(resp) must include("No Manufacturers available")
        content should include element withAttrValue("name", "id")
        content should include element withAttrValue("name", "name")
        content should include element withAttrValue("name", "address.cityName")
        content should include element withAttrValue("name", "address.countryCode")
        content should include element withAttrValue("name", "address.countrySubDivisionCode")
        content should include element withAttrValue("name", "address.countrySubDivisionName")
        content should include element withAttrValue("name", "address.line")
        content should include element withAttrValue("name", "address.postcodeId")
      }
    }
  }

  "pre-populate addManufacturersPage that are added/cached on user navigating to the screen" in withFeatures((enabled(Feature.submit))) {
    withSignedInUser() { (headers, session, tags) =>

      val goodsItemGen = arbitrary[NamedEntityWithAddress].map { e =>
        GovernmentAgencyGoodsItem(manufacturers = List(e))
      }

      withCaching(goodsItemGen.sample)
      withRequest(get, addManufacturersPageUri, headers, session, tags) { resp =>
        val content = contentAsHtml(resp)
        contentAsString(resp) must include("1 Manufacturers added")
        content should include element withAttrValue("name", "id")
        content should include element withAttrValue("name", "name")
        content should include element withAttrValue("name", "address.cityName")
        content should include element withAttrValue("name", "address.countryCode")
        content should include element withAttrValue("name", "address.countrySubDivisionCode")
        content should include element withAttrValue("name", "address.countrySubDivisionName")
        content should include element withAttrValue("name", "address.line")
        content should include element withAttrValue("name", "address.postcodeId")
      }
    }
  }

}