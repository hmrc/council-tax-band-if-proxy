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
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSBodyReadables.readableAsString
import play.api.libs.ws.WSClient
import play.api.test.Injecting

class ApiIntegrationSpec
  extends AnyFlatSpec
  with should.Matchers
  with ScalaFutures
  with IntegrationPatience
  with WiremockHelper
  with Injecting
  with GuiceOneServerPerSuite:

  private val wsClient        = inject[WSClient]
  private val baseUrl         = s"http://localhost:$port"
  private val searchPath      = "/valuations/get-properties/Search?postCodeStandardSearch=M11%201AE"
  private val searchUrl       = s"$baseUrl$searchPath"
  private val getPropertyPath = "/valuations/get-property/123"
  private val getPropertyUrl  = s"$baseUrl$getPropertyPath"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false, "microservice.services.if.port" -> wireMockServer.port())
      .build()

  "GET /valuations/get-properties/Search" should "return OK" in {
    val searchResults = "{search results}"

    wireMockServer.stubFor(
      get(urlEqualTo(searchPath))
        .willReturn(
          aResponse().withStatus(OK)
            .withBody(searchResults)
        )
    )

    val response =
      wsClient
        .url(searchUrl)
        .get()
        .futureValue

    response.body   shouldBe searchResults
    response.status shouldBe OK

    wireMockServer.verify(getRequestedFor(urlEqualTo(searchPath)))
  }

  "GET /valuations/get-property/123" should "return OK" in {
    val propertyDetails = "{property details}"

    wireMockServer.stubFor(
      get(urlEqualTo(getPropertyPath))
        .willReturn(
          aResponse().withStatus(OK)
            .withBody(propertyDetails)
        )
    )

    val response =
      wsClient
        .url(getPropertyUrl)
        .get()
        .futureValue

    response.body   shouldBe propertyDetails
    response.status shouldBe OK

    wireMockServer.verify(getRequestedFor(urlEqualTo(getPropertyPath)))
  }
