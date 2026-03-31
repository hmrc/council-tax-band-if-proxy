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

package uk.gov.hmrc.vo.ccyctb.ifproxy.connectors

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier}
import uk.gov.hmrc.vo.unit.test.BaseAppSpec

import java.net.URL

/**
  * @author Yuriy Tumakha
  */
class IFConnectorSpec extends BaseAppSpec:

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false)
      .build()

  "IFConnector.forwardGetRequest" should {
    "return 200" in {
      val headers = Seq("Authorization" -> "Bearer XXX")

      given request: Request[AnyContent] = getRequest.withHeaders(headers*)

      val response    = Json.obj("status" -> "OK")
      val httpClient  = httpClientMock(responseBody = response)
      val ifConnector = IFConnector(httpClient)

      val result = ifConnector.forwardGetRequest("http://localhost:8887/valuations/get-properties/Search?start=1&size=20", headers)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe response

      verify(httpClient)
        .get(any[URL])(using any[HeaderCarrier])
    }

    "throw exception" in {
      val headers = Seq("Authorization" -> "Bearer XXX", "CorrelationId" -> "825b6aec-e592-4b52-92a9-940cc54cc66d")

      given request: Request[AnyContent] = getRequest.withHeaders(headers*)

      val httpClient  = httpClientFailedMock(returnFailure = GatewayTimeoutException("Fake timeout exception"))
      val ifConnector = IFConnector(httpClient)

      val thrown = intercept[GatewayTimeoutException] {
        await(ifConnector.forwardGetRequest("http://localhost:8887/valuations/get-properties/Search?start=1&size=20", headers))
      }

      thrown.getMessage shouldBe "Fake timeout exception"

      verify(httpClient)
        .get(any[URL])(using any[HeaderCarrier])
    }
  }

  "IFConnector.forwardPostRequest" should {
    "return 201" in {
      val headers      = Seq("Authorization" -> "Bearer XXX")
      val expectedJson = Json.obj("status" -> "CREATED")

      given request: Request[AnyContent] = postRequest.withHeaders(headers*).withJsonBody(Json.obj("param1" -> "value1"))

      val httpClient  = httpClientMock(POST, responseBody = expectedJson, responseStatus = CREATED)
      val ifConnector = IFConnector(httpClient)

      val result = ifConnector.forwardPostRequest("http://localhost:8887/valuations/council-tax-band-challenge", headers)

      status(result)        shouldBe CREATED
      contentAsJson(result) shouldBe expectedJson

      verify(httpClient)
        .post(any[URL])(using any[HeaderCarrier])
    }

    "return 400 for empty body in request" in {
      val headers = Seq("Authorization" -> "Bearer XXX")

      given request: Request[AnyContent] = postRequest.withHeaders(headers*).withJsonBody(Json.obj("param1" -> "value1"))

      val httpClient  = httpClientFailedMock(POST, returnFailure = GatewayTimeoutException("Fake timeout exception"))
      val ifConnector = IFConnector(httpClient)

      val thrown = intercept[GatewayTimeoutException] {
        await(ifConnector.forwardPostRequest("http://localhost:8887/valuations/council-tax-band-challenge", headers))
      }

      thrown.getMessage shouldBe "Fake timeout exception"

      verify(httpClient)
        .post(any[URL])(using any[HeaderCarrier])
    }
  }
