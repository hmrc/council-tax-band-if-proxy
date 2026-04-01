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

package uk.gov.hmrc.vo.ccyctb.ifproxy.controllers

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, getRequestedFor, post, postRequestedFor, urlEqualTo}
import com.github.tomakehurst.wiremock.http.Fault
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.shaded.ahc.org.asynchttpclient.exception.RemotelyClosedException
import uk.gov.hmrc.vo.integration.test.BaseServerSpec

import java.net.SocketException

/**
  * @author Yuriy Tumakha
  */
class ValuationsProxyControllerSpec extends BaseServerSpec:

  private val controller = inject[ValuationsProxyController]
  val getRequest         = FakeRequest()
  val postRequest        = FakeRequest(POST, "/")

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(
        "metrics.enabled"               -> false,
        "microservice.services.if.port" -> wireMockServer.port
      )
      .build()

  "GET /valuations/get-properties/Search" should {
    "return 200" in {
      val searchResults = """{"total":0}"""

      wireMockServer.stubFor(
        get(urlEqualTo("/valuations/get-properties/Search?start=1&size=20"))
          .willReturn(
            aResponse().withStatus(OK)
              .withBody(searchResults)
          )
      )

      val result = controller.valuationsGetPropertiesSearchTypeGet("Search")(
        FakeRequest("GET", "/valuations/get-properties/Search?start=1&size=20").withHeaders("Authorization" -> "Bearer XXX")
      )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.parse(searchResults)

      wireMockServer.verify(getRequestedFor(urlEqualTo("/valuations/get-properties/Search?start=1&size=20")))
    }

    "throw exception" in {
      wireMockServer.stubFor(
        get(urlEqualTo("/valuations/get-properties/Search?start=1&size=20"))
          .willReturn(
            aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)
          )
      )

      val thrown = intercept[SocketException] {
        await(controller.valuationsGetPropertiesSearchTypeGet("Search")(
          FakeRequest("GET", "/valuations/get-properties/Search?start=1&size=20")
            .withHeaders("Authorization" -> "Bearer XXX", "CorrelationId" -> "throwException")
        ))
      }
      thrown.getMessage shouldBe "Connection reset"

      wireMockServer.verify(getRequestedFor(urlEqualTo("/valuations/get-properties/Search?start=1&size=20")))
    }
  }

  "GET /valuations/get-property/7777777" should {
    "return 200" in {
      val propertyJson = """{"property":{}}"""

      wireMockServer.stubFor(
        get(urlEqualTo("/valuations/get-property/7777777"))
          .willReturn(
            aResponse().withStatus(OK)
              .withBody(propertyJson)
          )
      )

      val result = controller.valuationsGetPropertyIdGet("7777777")(getRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.parse(propertyJson)

      wireMockServer.verify(getRequestedFor(urlEqualTo("/valuations/get-property/7777777")))
    }
  }

  "GET /valuations/get-property/UNKNOWN_ID" should {
    "return 404 for UNKNOWN_ID" in {
      wireMockServer.stubFor(
        get(urlEqualTo("/valuations/get-property/UNKNOWN_ID"))
          .willReturn(
            aResponse().withStatus(NOT_FOUND)
          )
      )

      val result = controller.valuationsGetPropertyIdGet("UNKNOWN_ID")(getRequest)

      status(result)          shouldBe NOT_FOUND
      contentAsString(result) shouldBe ""

      wireMockServer.verify(getRequestedFor(urlEqualTo("/valuations/get-property/UNKNOWN_ID")))
    }
  }

  "GET /valuations/get-property/TEST_EXCEPTION" should {
    "throw exception" in {
      wireMockServer.stubFor(
        get(urlEqualTo("/valuations/get-property/TEST_EXCEPTION"))
          .willReturn(
            aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)
          )
      )

      val thrown = intercept[RemotelyClosedException] {
        await(controller.valuationsGetPropertyIdGet("TEST_EXCEPTION")(getRequest.withHeaders("CorrelationId" -> "throwException")))
      }
      thrown.getMessage shouldBe "Remotely closed"

      wireMockServer.verify(getRequestedFor(urlEqualTo("/valuations/get-property/TEST_EXCEPTION")))
    }
  }

  "POST /valuations/council-tax-band-challenge" should {
    "return 201" in {
      val responseJson = """{"status":"Submitted"}"""

      wireMockServer.stubFor(
        post(urlEqualTo("/valuations/council-tax-band-challenge"))
          .willReturn(
            aResponse().withStatus(CREATED)
              .withBody(responseJson)
          )
      )

      val requestWithJsonBody = postRequest.withJsonBody(Json.obj("param1" -> "value1"))
      val result              = controller.valuationsCouncilTaxBandChallengePost()(requestWithJsonBody)

      status(result)        shouldBe CREATED
      contentAsJson(result) shouldBe Json.parse(responseJson)

      wireMockServer.verify(postRequestedFor(urlEqualTo("/valuations/council-tax-band-challenge")))
    }

    "return 400 for empty body in request" in {
      val requestEmptyBody = postRequest
      val expectedJson     = Json.parse("""{"statusCode":400,"message":"JSON body is expected in request"}""")
      val result           = controller.valuationsCouncilTaxBandChallengePost()(requestEmptyBody)

      status(result)        shouldBe BAD_REQUEST
      contentAsJson(result) shouldBe expectedJson
    }
  }
