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

package uk.gov.hmrc.customs.test.behaviours

import uk.gov.hmrc.customs.test.IntegrationTest
import play.api.test.Helpers._

trait EndpointBehaviours extends CustomsSpec {

  def authenticatedEndpoint(uri: String, method: String = GET): Unit = {

    "redirect user" when {
      s"$uri endpoint is called with $method and without signed in user" in new IntegrationTest {

        withoutSignedInUser() { (headers, tags) =>
          withRequest(method, uriWithContextPath(uri), headers, tags = tags) { resp =>
            wasRedirected(ggLoginRedirectUri(uriWithContextPath(uri)), resp)
          }
        }
      }
    }
  }

  def okEndpoint(uri: String, method: String = GET): Unit = {

    "return 200" when {

      s"$uri endpoint is called with $method and with signed in user" in new IntegrationTest {

        withCaching(None)
        withSignedInUser() { (headers, session, tags) =>
          withRequest(method, uriWithContextPath(uri), headers, session, tags) {
            wasOk
          }
        }
      }
    }
  }

  def redirectedEndpoint(uri: String, redirectTo: String, method: String = GET): Unit = {

    "return 303" when {

      s"$uri endpoint is called with $method and with signed in user" in new IntegrationTest {

        withCaching(None)
        withSignedInUser() { (headers, session, tags) =>
          withRequest(method, uriWithContextPath(uri), headers, session, tags) { resp =>
            wasRedirected(uriWithContextPath(redirectTo), resp)
          }
        }
      }
    }
  }
}