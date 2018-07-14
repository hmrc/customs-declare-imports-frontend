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

import akka.util.Timeout
import com.gu.scalatest.JsoupShouldMatchers
import config.AppConfig
import domain.auth.SignedInUser
import domain.declaration.Declaration
import org.jsoup.nodes.Element
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.concurrent.Execution.Implicits
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, EnrolmentIdentifier, Enrolments}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

trait CustomsPlaySpec extends PlaySpec with OneAppPerSuite with JsoupShouldMatchers with MockitoSugar {

  implicit val mat = app.materializer
  implicit val ec: ExecutionContext = Implicits.defaultContext
  implicit val appConfig = app.injector.instanceOf[AppConfig]

  protected val contextPath: String = "/customs-declare-imports"

  class RequestScenario(method: String = "GET", uri: String = s"/${contextPath}/", headers: Map[String, String] = Map.empty) {
    val req = basicRequest(method, uri, headers)
  }

  protected def basicRequest(method: String, uri: String, headers: Map[String, String] = Map.empty): FakeRequest[AnyContentAsEmpty.type] = FakeRequest(method, uri).withHeaders(headers.toSeq: _*)

  protected def contentAsHtml(of: Future[Result])(implicit timeout: Timeout): Element = contentAsString(of)(timeout, mat).asBodyFragment

  protected def requestScenario(method: String = "GET",
                                uri: String = s"/${contextPath}/",
                                headers: Map[String, String] = Map.empty)(test: (Future[Result]) => Unit): Unit = {
    new RequestScenario(method, uri, headers) {
      test(route(app, req).get)
    }
  }

  protected def uriWithContextPath(path: String): String = s"${contextPath}${path}"

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
      if (eori.isDefined) Set(Enrolment("HMRC-CUS-ORG", Seq(EnrolmentIdentifier("EORINumber", eori.get)), "activated"))
      else Set.empty
    )
  )

  protected def randomDomainName: String = randomString(8) + tlds(randomInt(tlds.length))

  protected def randomEmail: String = randomEmail(randomFirstName, randomLastName)

  protected def randomEmail(firstName: String, lastName: String): String = s"${firstName.toLowerCase}.${lastName.toLowerCase}@${randomDomainName}"

  protected def randomFirstName: String = firstNames(randomInt(firstNames.length))

  protected def randomLastName: String = lastNames(randomInt(lastNames.length))

  protected def randomInt(limit: Int): Int = Random.nextInt(limit)

  protected def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  protected def randomValidDeclaration: Declaration = Declaration()

  private val firstNames: Seq[String] = Seq("Oliver", "Jack", "Harry", "Jacob", "Charlie", "Thomas", "George", "Oscar", "James", "William", "Amelia", "Olivia", "Isla", "Emily", "Poppy", "Ava", "Isabella", "Jessica", "Lily", "Sophie")

  private val lastNames: Seq[String] = Seq("Smith", "Jones", "Williams", "Brown", "Taylor", "Davies", "Wilson", "Evans", "Thomas", "Roberts")

  private val tlds: Seq[String] = Seq(".com", ".org", ".net", ".co.uk", ".org.uk")

}
