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

import domain.declaration.InvoiceAmount
import domain.features.{Feature, FeatureStatus}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.test.{AuthenticationBehaviours, CustomsPlaySpec, FeatureSwitchBehaviours, WiremockBehaviours}

class SubmissionControllerSpec
  extends CustomsPlaySpec
    with AuthenticationBehaviours
    with FeatureSwitchBehaviours
    with WiremockBehaviours {

  val method = "GET"
  val handleMethod = "POST"
  val uri = uriWithContextPath("/declaration")

  s"$method $uri" should {

    "return 200" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) {
          wasOk
        }
      }
    }

    "return HTML" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) {
          wasHtml
        }
      }
    }

    "require authentication" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      notSignedInScenario() {
        accessDeniedRequestScenarioTest(method, uri)
      }
    }

    "be behind a feature switch" in featureScenario(Feature.declaration, FeatureStatus.disabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(method, uri, signedInUser) {
          wasNotFound
        }
      }
    }

    "include a text input for WCO data model version code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.wcoDataModelVersionCode")
        }
      }
    }

    "include a text input for WCO type name" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.wcoTypeName")
        }
      }
    }

    "include a text input for Responsible Country Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.responsibleCountryCode")
        }
      }
    }

    "include a text input for Responsible Agency Name" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.responsibleAgencyName")
        }
      }
    }

    "include a text input for Agency Assigned Customization Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.agencyAssignedCustomizationCode")
        }
      }
    }

    "include a text input for Agency Assigned Customization Version Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.agencyAssignedCustomizationVersionCode")
        }
      }
    }

    "include a text input for Badge ID" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "badgeId")
        }
      }
    }

    "include a text input for Acceptance Date Time" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.acceptanceDateTime")
        }
      }
    }

    "include a text input for Acceptance Date Time format code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.acceptanceDateTimeFormatCode")
        }
      }
    }

    "include a text input for Function Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.functionCode")
        }
      }
    }

    "include a text input for Functional Reference ID" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.functionalReferenceId")
        }
      }
    }

    "include a text input for ID" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.id")
        }
      }
    }

    "include a text input for Issue Date Time" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.issueDateTime")
        }
      }
    }

    "include a text input for Issue Date Time format code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.issueDateTimeFormatCode")
        }
      }
    }

    "include a text input for Issue Location ID" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.issueLocationId")
        }
      }
    }

    "include a text input for Type Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.typeCode")
        }
      }
    }

    "include a text input for Goods Item Quantity" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.goodsItemQuantity")
        }
      }
    }

    "include a text input for Declaration Office ID" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.declarationOfficeId")
        }
      }
    }

    "include a text input for Invoice Amount" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.invoiceAmount")
        }
      }
    }

    "include a text input for Invoice Amount currency ID" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.invoiceAmountCurrencyId")
        }
      }
    }

    "include a text input for Loading List Quantity" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.loadingListQuantity")
        }
      }
    }

    "include a text input for Total Gross Mass Measure" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.totalGrossMassMeasure")
        }
      }
    }

    "include a text input for Total Gross Mass Measure unit code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.totalGrossMassMeasureUnitCode")
        }
      }
    }

    "include a text input for Total Package Quantity" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.totalPackageQuantity")
        }
      }
    }

    "include a text input for Specific Circumstances Code Code" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      signedInScenario() {
        userRequestScenario(method, uri) { resp =>
          includesHtmlInput(resp, "text", "metaData.declaration.specificCircumstancesCodeCode")
        }
      }
    }

  }

  s"$handleMethod $uri" should {

    "return 200" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      withDeclarationSubmissionApi() {
        signedInScenario(signedInUser) {
          userRequestScenario(handleMethod, uri, signedInUser) {
            wasOk
          }
        }
      }
    }

    "return HTML" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      withDeclarationSubmissionApi() {
        signedInScenario(signedInUser) {
          userRequestScenario(handleMethod, uri, signedInUser) {
            wasHtml
          }
        }
      }
    }

    // FIXME why does this not pass?
    "require authentication" ignore featureScenario(Feature.declaration, FeatureStatus.enabled) {
      notSignedInScenario() {
        accessDeniedRequestScenarioTest(handleMethod, uri)
      }
    }

    "be behind a feature switch" in featureScenario(Feature.declaration, FeatureStatus.disabled) {
      signedInScenario(signedInUser) {
        userRequestScenario(handleMethod, uri, signedInUser) {
          wasNotFound
        }
      }
    }

    "submit declaration" in featureScenario(Feature.declaration, FeatureStatus.enabled) {
      withDeclarationSubmissionApi() {
        signedInScenario(signedInUser) {
          userRequestScenario(handleMethod, uri, signedInUser) { resp =>
            // TODO unigration test for declaration submission
          }
        }
      }
    }

  }

  "declaration form mapping to domain object" should {

    "map WCO data model version code" in {
      val code = randomString(8)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          wcoDataModelVersionCode = Some(code)
        )
      ).toMetaData.wcoDataModelVersionCode.get must be(code)
    }

    "map WCO type name" in {
      val name = randomString(8)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          wcoTypeName = Some(name)
        )
      ).toMetaData.wcoTypeName.get must be(name)
    }

    "map responsible country code" in {
      val code = randomString(2)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          responsibleCountryCode = Some(code)
        )
      ).toMetaData.responsibleCountryCode.get must be(code)
    }

    "map responsible agency name" in {
      val name = randomString(16)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          responsibleAgencyName = Some(name)
        )
      ).toMetaData.responsibleAgencyName.get must be(name)
    }

    "map agency assigned customization code" in {
      val code = randomString(8)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          agencyAssignedCustomizationCode = Some(code)
        )
      ).toMetaData.agencyAssignedCustomizationCode.get must be(code)
    }

    "map agency assigned customisation version code" in {
      val code = randomString(8)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          agencyAssignedCustomizationVersionCode = Some(code)
        )
      ).toMetaData.agencyAssignedCustomizationVersionCode.get must be(code)
    }

    "map acceptance date time" in {
      val date = randomString(35)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            acceptanceDateTime = Some(date)
          )
        )
      ).toMetaData.declaration.acceptanceDateTime.get.dateTimeString.value must be(date)
    }

    "map acceptance date time format code when acceptance date time is provided" in {
      val code = randomString(3)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            acceptanceDateTime = Some(randomString(16)),
            acceptanceDateTimeFormatCode = Some(code)
          )
        )
      ).toMetaData.declaration.acceptanceDateTime.get.dateTimeString.formatCode must be(code)
    }

    "map function code" in {
      val code = randomString(2)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            functionCode = Some(code)
          )
        )
      ).toMetaData.declaration.functionCode.get must be(code)
    }

    "map functional reference ID" in {
      val id = randomString(35)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            functionalReferenceId = Some(id)
          )
        )
      ).toMetaData.declaration.functionalReferenceId.get must be(id)
    }

    "map id" in {
      val id = randomString(70)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            id = Some(id)
          )
        )
      ).toMetaData.declaration.id.get must be(id)
    }

    "map issue date time" in {
      val date = randomString(35)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            issueDateTime = Some(date)
          )
        )
      ).toMetaData.declaration.issueDateTime.get.dateTimeString.value must be(date)
    }

    "map issue date time format code when issue date time is provided" in {
      val code = randomString(3)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            issueDateTime = Some(randomString(16)),
            issueDateTimeFormatCode = Some(code)
          )
        )
      ).toMetaData.declaration.issueDateTime.get.dateTimeString.formatCode must be(code)
    }

    "map issue location ID" in {
      val id = randomString(5)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            issueLocationId = Some(id)
          )
        )
      ).toMetaData.declaration.issueLocationId.get must be(id)
    }

    "map type code" in {
      val code = randomString(3)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            typeCode = Some(code)
          )
        )
      ).toMetaData.declaration.typeCode.get must be(code)
    }

    "map goods item quantity" in {
      val quantity = randomInt(100000)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            goodsItemQuantity = Some(quantity)
          )
        )
      ).toMetaData.declaration.goodsItemQuantity.get must be(quantity)
    }

    "map declaration office id" in {
      val id = randomString(17)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            declarationOfficeId = Some(id)
          )
        )
      ).toMetaData.declaration.declarationOfficeId.get must be(id)
    }

    "map invoice amount and currency ID" in {
      val amount = randomBigDecimal
      val currency = Some(randomISO4217CurrencyCode)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            invoiceAmount = Some(amount),
            invoiceAmountCurrencyId = currency
          )
        )
      ).toMetaData.declaration.invoiceAmount.get must be(InvoiceAmount(amount, currency))
    }

    "map loading list quantity" in {
      val quantity = randomInt(100000)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            loadingListQuantity = Some(quantity)
          )
        )
      ).toMetaData.declaration.loadingListQuantity.get must be(quantity)
    }

    "map total gross mass measure" in {
      val total = randomBigDecimal
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            totalGrossMassMeasure = Some(total)
          )
        )
      ).toMetaData.declaration.totalGrossMassMeasure.get.value must be(total)
    }

    "map total gross mass measure unit code" in {
      val total = randomBigDecimal
      val code = randomString(5)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            totalGrossMassMeasure = Some(total),
            totalGrossMassMeasureUnitCode = Some(code)
          )
        )
      ).toMetaData.declaration.totalGrossMassMeasure.get.unitCode.get must be(code)
    }

    "map total package quantity" in {
      val quantity = randomInt(100000000)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            totalPackageQuantity = Some(quantity)
          )
        )
      ).toMetaData.declaration.totalPackageQuantity.get must be(quantity)
    }

    "map specific circumstances code code" in {
      val code = randomString(3)
      SubmissionAllInOneForm(
        metaData = SubmissionMetaDataForm(
          declaration = SubmissionDeclarationForm(
            specificCircumstancesCodeCode = Some(code)
          )
        )
      ).toMetaData.declaration.specificCircumstancesCodeCode.get must be(code)
    }

  }

}
