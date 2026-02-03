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

package uk.gov.hmrc.ccyctbifproxy.connectors

import org.apache.pekko.stream.Materializer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request}
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.ccyctbifproxy.controllers.MockHttpClient
import uk.gov.hmrc.http.GatewayTimeoutException
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

/**
  * @author Yuriy Tumakha
  */
class IFConnectorSpec extends AnyFlatSpec with should.Matchers with Injecting with GuiceOneAppPerSuite:

  private val fakeRequest = FakeRequest()
  private val ifConnector = inject[IFConnector]
  given mat: Materializer = inject[Materializer]

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false)
      .overrides(bind[DefaultHttpClient].to[MockHttpClient])
      .build()

  "IFConnector.forwardGetRequest" should "return 200" in {
    val headers                        = Seq("Authorization" -> "Bearer XXX")
    given request: Request[AnyContent] = FakeRequest("GET", "/").withHeaders(headers*)

    val result = ifConnector.forwardGetRequest("http://localhost:8887/valuations/get-properties/Search?start=1&size=20", headers)

    status(result)        shouldBe OK
    contentAsJson(result) shouldBe Json.obj("requestUrl" -> "http://localhost:8887/valuations/get-properties/Search?start=1&size=20")
  }

  it should "throw exception" in {
    val headers                        = Seq("Authorization" -> "Bearer XXX", "CorrelationId" -> "throwException")
    given request: Request[AnyContent] = FakeRequest("GET", "/").withHeaders(headers*)

    val thrown = intercept[GatewayTimeoutException] {
      await(ifConnector.forwardGetRequest("http://localhost:8887/valuations/get-properties/Search?start=1&size=20", headers))
    }

    thrown.getMessage shouldBe "Fake timeout exception"
  }

  "IFConnector.forwardPostRequest" should "return 201" in {
    val headers                        = Seq("Authorization" -> "Bearer XXX")
    val expectedJson                   = Json.parse("""{"requestUrl":"http://localhost:8887/valuations/council-tax-band-challenge","requestBody":{"param1":"value1"}}""")
    given request: Request[AnyContent] = fakeRequest.withMethod("POST").withHeaders(headers*).withJsonBody(Json.obj("param1" -> "value1"))

    val result = ifConnector.forwardPostRequest("http://localhost:8887/valuations/council-tax-band-challenge", headers)

    status(result)        shouldBe CREATED
    contentAsJson(result) shouldBe expectedJson
  }

  it should "return 400 for empty body in request" in {
    val headers                        = Seq("Authorization" -> "Bearer XXX", "CorrelationId" -> "throwException")
    given request: Request[AnyContent] = fakeRequest.withMethod("POST").withHeaders(headers*).withJsonBody(Json.obj("param1" -> "value1"))

    val thrown = intercept[GatewayTimeoutException] {
      await(ifConnector.forwardPostRequest("http://localhost:8887/valuations/council-tax-band-challenge", headers))
    }

    thrown.getMessage shouldBe "Fake timeout exception"
  }
