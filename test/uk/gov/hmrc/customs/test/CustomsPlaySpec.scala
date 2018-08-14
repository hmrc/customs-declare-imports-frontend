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

package uk.gov.hmrc.customs.test

import akka.stream.Materializer
import akka.util.Timeout
import com.gu.scalatest.JsoupShouldMatchers
import config.AppConfig
import domain.auth.SignedInUser
import domain.wco._
import org.jsoup.nodes.Element
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.http.Status
import play.api.libs.concurrent.Execution.Implicits
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, EnrolmentIdentifier, Enrolments}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.Random

// TODO override configured ports from application.conf with random available ports
trait CustomsPlaySpec extends PlaySpec with OneAppPerSuite with JsoupShouldMatchers with MockitoSugar with ScalaFutures {

  implicit val mat: Materializer = app.materializer
  implicit val ec: ExecutionContext = Implicits.defaultContext
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val patience: PatienceConfig = PatienceConfig(timeout = 5.seconds, interval = 50.milliseconds) // be more patient than the default

  protected val contextPath: String = "/customs-declare-imports"

  class RequestScenario(method: String = "GET", uri: String = s"/$contextPath/", headers: Map[String, String] = Map.empty) {
    val req: FakeRequest[AnyContentAsEmpty.type] = basicRequest(method, uri, headers)
  }

  protected def component[T: ClassTag]: T = app.injector.instanceOf[T]

  protected def basicRequest(method: String = "GET", uri: String = "/", headers: Map[String, String] = Map.empty): FakeRequest[AnyContentAsEmpty.type] = FakeRequest(method, uri).withHeaders(headers.toSeq: _*)

  protected def contentAsHtml(of: Future[Result])(implicit timeout: Timeout): Element = contentAsString(of)(timeout, mat).asBodyFragment

  protected def includesHtmlInput(in: Future[Result], `type`: String, name: String)(implicit timeout: Timeout): Unit = contentAsHtml(in)(timeout) should include element withName("input").withAttrValue("type", `type`).withAttrValue("name", name)

  protected def includesHtmlField(in: Future[Result], `type`: String, name: String)(implicit timeout: Timeout): Unit = contentAsHtml(in)(timeout) should include element withName(`type`).withAttrValue("name", name)

  protected def includesHtmlLink(in: Future[Result], hrefValue: String)(implicit timeout: Timeout): Unit = contentAsHtml(in)(timeout) should include element withName("a").withAttrValue("href", hrefValue)

  protected def requestScenario(method: String = "GET",
                                uri: String = s"/$contextPath/",
                                headers: Map[String, String] = Map.empty)(test: Future[Result] => Unit): Unit = {
    new RequestScenario(method, uri, headers) {
      test(route(app, req).get)
    }
  }

  protected def wasOk(resp: Future[Result]): Unit = status(resp) must be (Status.OK)

  protected def wasRedirected(resp: Future[Result]): Unit = status(resp) must be (Status.SEE_OTHER)

  protected def wasNotFound(resp: Future[Result]): Unit = status(resp) must be (Status.NOT_FOUND)

  protected def wasHtml(resp: Future[Result]): Unit = {
    contentType(resp) must be (Some("text/html"))
    charset(resp) must be (Some("utf-8"))
  }

  protected def uriWithContextPath(path: String): String = s"$contextPath$path"

  protected def userFixture(lastName: String = randomLastName,
                            firstName: Option[String] = Some(randomFirstName),
                            email: Option[String] = Some(randomEmail),
                            eori: Option[String] = Some(randomString(8)),
                            affinityGroup: Option[AffinityGroup] = Some(Individual),
                            internalId: Option[String] = Some(randomString(16))): SignedInUser = SignedInUser(
    Credentials(randomString(8), "GovernmentGateway"),
    Name(firstName, Some(lastName)),
    email,
    affinityGroup,
    internalId,
    Enrolments(
      if (eori.isDefined) Set(Enrolment(SignedInUser.cdsEnrolmentName, Seq(EnrolmentIdentifier(SignedInUser.eoriIdentifierKey, eori.get)), "activated"))
      else Set.empty
    )
  )

  protected def randomCancelDeclaration: MetaData = MetaData(
    wcoDataModelVersionCode = Some(randomString(6)),
    wcoTypeName = Some(randomString(712)),
    responsibleCountryCode = Some(randomISO3166Alpha2CountryCode),
    responsibleAgencyName = Some(randomString(70)),
    agencyAssignedCustomizationVersionCode = Some(randomString(3)),
    declaration = Declaration(
      typeCode = Some("INV"), // ONLY acceptable value for a cancellation
      functionCode = Some(13), // ONLY acceptable value for a cancellation
      functionalReferenceId = Some(randomString(35)),
      id = Some(randomString(70)),
      additionalInformations = Seq(AdditionalInformation(
        statementDescription = Some(randomString(512))
      )),
      amendments = Seq(Amendment(
        changeReasonCode = Some(randomString(3))
      ))
    )
  )

  protected def randomDomainName: String = randomString(8) + tlds(randomInt(tlds.length))

  protected def randomEmail: String = randomEmail(randomFirstName, randomLastName)

  protected def randomEmail(firstName: String, lastName: String): String = s"${firstName.toLowerCase}.${lastName.toLowerCase}@$randomDomainName"

  protected def randomFirstName: String = firstNames(randomInt(firstNames.length))

  protected def randomLastName: String = lastNames(randomInt(lastNames.length))

  protected def randomInt(limit: Int): Int = Random.nextInt(limit)

  protected def randomBigDecimal: BigDecimal = randomBigDecimal(Int.MaxValue)

  protected def randomBigDecimal(limit: Int): BigDecimal = BigDecimal(randomInt(limit))

  protected def random0To9: Int = randomInt(10)

  protected def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  protected def randomValidDeclaration: Declaration = Declaration()

  protected def randomBoolean: Boolean = if(Random.nextInt() % 2 == 0) true else false

  protected def randomDeclarationFunctionCode: Int = declarationFunctionCodes(randomInt(declarationFunctionCodes.length))

  protected def randomDateTimeFormatCode: String = dateTimeFormatCodes(randomInt(dateTimeFormatCodes.length))

  protected def randomDateTimeString: String = s"20$random0To9$random0To9$random0To9$random0To9$random0To9$random0To9$random0To9$random0To9$random0To9$random0To9$random0To9$random0To9$randomZ$random0To9$random0To9"

  protected def randomISO4217CurrencyCode: String = iso4217(randomInt(iso4217.length))

  protected def randomISO3166Alpha2CountryCode: String = iso3166(randomInt(iso3166.length))

  private val randomZ: String = z(randomInt(z.length))

  private lazy val z: Seq[String] = Seq("+", "-")

  private lazy val declarationFunctionCodes: Seq[Int] = Seq(9, 13, 14)

  private lazy val dateTimeFormatCodes: Seq[String] = Seq("102", "304")

  private lazy val firstNames: Seq[String] = Seq("Oliver", "Jack", "Harry", "Jacob", "Charlie", "Thomas", "George", "Oscar", "James", "William", "Amelia", "Olivia", "Isla", "Emily", "Poppy", "Ava", "Isabella", "Jessica", "Lily", "Sophie")

  private lazy val lastNames: Seq[String] = Seq("Smith", "Jones", "Williams", "Brown", "Taylor", "Davies", "Wilson", "Evans", "Thomas", "Roberts")

  private lazy val tlds: Seq[String] = Seq(".com", ".org", ".net", ".co.uk", ".org.uk")

  // the source I took this from listed by country so included duplicate codes:
  // put into set to remove dupes then transform into seq for convenient pseudo-random access
  private lazy val iso4217: Seq[String] = Set("AFN","EUR","ALL","DZD","USD","EUR","AOA","XCD","ARS","AMD","AWG","AUD","EUR","AZN","BSD","BHD","BDT","BBD","BYN","EUR","BZD","XOF","BMD","INR","BTN","BOB","BOV","USD","BAM","BWP","NOK","BRL","USD","BND","BGN","XOF","BIF","CVE","KHR","XAF","CAD","KYD","XAF","XAF","CLP","CLF","CNY","AUD","AUD","COP","COU","KMF","CDF","XAF","NZD","CRC","XOF","HRK","CUP","CUC","ANG","EUR","CZK","DKK","DJF","XCD","DOP","USD","EGP","SVC","USD","XAF","ERN","EUR","ETB","EUR","FKP","DKK","FJD","EUR","EUR","EUR","XPF","EUR","XAF","GMD","GEL","EUR","GHS","GIP","EUR","DKK","XCD","EUR","USD","GTQ","GBP","GNF","XOF","GYD","HTG","USD","AUD","EUR","HNL","HKD","HUF","ISK","INR","IDR","XDR","IRR","IQD","EUR","GBP","ILS","EUR","JMD","JPY","GBP","JOD","KZT","KES","AUD","KPW","KRW","KWD","KGS","LAK","EUR","LBP","LSL","ZAR","LRD","LYD","CHF","EUR","EUR","MOP","MKD","MGA","MWK","MYR","MVR","XOF","EUR","USD","EUR","MRU","MUR","EUR","XUA","MXN","MXV","USD","MDL","EUR","MNT","EUR","XCD","MAD","MZN","MMK","NAD","ZAR","AUD","NPR","EUR","XPF","NZD","NIO","XOF","NGN","NZD","AUD","USD","NOK","OMR","PKR","USD","PAB","USD","PGK","PYG","PEN","PHP","NZD","PLN","EUR","USD","QAR","EUR","RON","RUB","RWF","EUR","SHP","XCD","XCD","EUR","EUR","XCD","WST","EUR","STN","SAR","XOF","RSD","SCR","SLL","SGD","ANG","XSU","EUR","EUR","SBD","SOS","ZAR","SSP","EUR","LKR","SDG","SRD","NOK","SZL","SEK","CHF","CHE","CHW","SYP","TWD","TJS","TZS","THB","USD","XOF","NZD","TOP","TTD","TND","TRY","TMT","USD","AUD","UGX","UAH","AED","GBP","USD","USD","USN","UYU","UYI","UZS","VUV","VEF","VND","USD","USD","XPF","MAD","YER","ZMW","ZWL","XBA","XBB","XBC","XBD","XTS","XXX","XAU","XPD","XPT","XAG").toSeq

  private lazy val iso3166: Seq[String] = Seq("AF","AX","AL","DZ","AS","AD","AO","AI","AQ","AG","AR","AM","AW","AU","AT","AZ","BS","BH","BD","BB","BY","BE","BZ","BJ","BM","BT","BO","BA","BW","BV","BR","IO","BN","BG","BF","BI","KH","CM","CA","CV","KY","CF","TD","CL","CN","CX","CC","CO","KM","CG","CD","CK","CR","CI","HR","CU","CY","CZ","DK","DJ","DM","DO","EC","EG","SV","GQ","ER","EE","ET","FK","FO","FJ","FI","FR","GF","PF","TF","GA","GM","GE","DE","GH","GI","GR","GL","GD","GP","GU","GT","GG","GN","GW","GY","HT","HM","VA","HN","HK","HU","IS","IN","ID","IR","IQ","IE","IM","IL","IT","JM","JP","JE","JO","KZ","KE","KI","KR","KW","KG","LA","LV","LB","LS","LR","LY","LI","LT","LU","MO","MK","MG","MW","MY","MV","ML","MT","MH","MQ","MR","MU","YT","MX","FM","MD","MC","MN","ME","MS","MA","MZ","MM","NA","NR","NP","NL","AN","NC","NZ","NI","NE","NG","NU","NF","MP","NO","OM","PK","PW","PS","PA","PG","PY","PE","PH","PN","PL","PT","PR","QA","RE","RO","RU","RW","BL","SH","KN","LC","MF","PM","VC","WS","SM","ST","SA","SN","RS","SC","SL","SG","SK","SI","SB","SO","ZA","GS","ES","LK","SD","SR","SJ","SZ","SE","CH","SY","TW","TJ","TZ","TH","TL","TG","TK","TO","TT","TN","TR","TM","TC","TV","UG","UA","AE","GB","US","UM","UY","UZ","VU","VE","VN","VG","VI","WF","EH","YE","ZM","ZW")

}
