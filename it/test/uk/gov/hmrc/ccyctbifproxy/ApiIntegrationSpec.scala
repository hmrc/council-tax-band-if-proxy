/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.ccyctbifproxy

import com.github.tomakehurst.wiremock.client.WireMock.*
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSBodyReadables.readableAsString
import uk.gov.hmrc.vo.integration.test.BaseServerSpec

class ApiIntegrationSpec extends BaseServerSpec:

  private val searchPath      = "/valuations/get-properties/Search?postCodeStandardSearch=M11%201AE"
  private val getPropertyPath = "/valuations/get-property/123"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(
        "metrics.enabled"               -> false,
        "microservice.services.if.port" -> wireMockServer.port
      )
      .build()

  "GET /valuations/get-properties/Search" should {
    "return OK" in {
      val searchResults = "{search results}"

      wireMockServer.stubFor(
        get(urlEqualTo(searchPath))
          .willReturn(
            aResponse().withStatus(OK)
              .withBody(searchResults)
          )
      )

      val response =
        wsUrl(searchPath)
          .get()
          .futureValue

      response.body   shouldBe searchResults
      response.status shouldBe OK

      wireMockServer.verify(getRequestedFor(urlEqualTo(searchPath)))
    }
  }

  "GET /valuations/get-property/123" should {
    "return OK" in {
      val propertyDetails = "{property details}"

      wireMockServer.stubFor(
        get(urlEqualTo(getPropertyPath))
          .willReturn(
            aResponse().withStatus(OK)
              .withBody(propertyDetails)
          )
      )

      val response =
        wsUrl(getPropertyPath)
          .get()
          .futureValue

      response.body   shouldBe propertyDetails
      response.status shouldBe OK

      wireMockServer.verify(getRequestedFor(urlEqualTo(getPropertyPath)))
    }
  }
