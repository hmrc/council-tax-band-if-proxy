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

package uk.gov.hmrc.ccyctbifproxy.controllers

import play.api.mvc.*
import uk.gov.hmrc.ccyctbifproxy.config.AppConfig
import uk.gov.hmrc.ccyctbifproxy.connectors.{HeadersHelpers, IFConnector}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}

/**
  * @author Yuriy Tumakha
  */
@Singleton()
class ValuationsProxyController @Inject() (
  appConfig: AppConfig,
  ifConnector: IFConnector,
  cc: ControllerComponents
) extends BackendController(cc)
  with HeadersHelpers:

  private val baseUrl                 = s"${appConfig.ifBaseUrl}/valuations"
  private val searchEndpoint          = s"$baseUrl/get-properties/"
  private val getPropertyEndpoint     = s"$baseUrl/get-property/"
  private val submitChallengeEndpoint = s"$baseUrl/council-tax-band-challenge"

  private val staticHeaders: Seq[(String, String)] = Seq(
    AUTHORIZATION -> s"Bearer ${appConfig.ifToken}",
    ACCEPT        -> "application/json;charset=UTF-8",
    "Environment" -> appConfig.ifEnvironment
  )

  private val forwardHeaders =
    Set(
      "CorrelationId",
      "Content-Type"
    )

  private def requestHeaders(using request: Request[?]) = staticHeaders ++ extractHeaders(forwardHeaders)

  def valuationsGetPropertiesSearchTypeGet(searchType: String): Action[AnyContent] = Action.async { implicit request =>
    ifConnector.forwardGetRequest(searchEndpoint + searchType, requestHeaders)
  }

  def valuationsGetPropertyIdGet(id: String): Action[AnyContent] = Action.async { implicit request =>
    ifConnector.forwardGetRequest(getPropertyEndpoint + id, requestHeaders)
  }

  def valuationsCouncilTaxBandChallengePost(): Action[AnyContent] = Action.async { implicit request =>
    ifConnector.forwardPostRequest(submitChallengeEndpoint, requestHeaders)
  }
