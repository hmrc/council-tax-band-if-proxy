/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.Logging
import play.api.http.Status.{BAD_REQUEST, CREATED, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.*
import play.api.mvc.Results.{BadRequest, Status}
import uk.gov.hmrc.http.HttpVerbs.{GET, POST}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendHeaderCarrierProvider
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
  * Integration Framework connector.
  *
  * @author Yuriy Tumakha
  */
class IFConnector @Inject() (
  httpClient: DefaultHttpClient
)(implicit ec: ExecutionContext
) extends BackendHeaderCarrierProvider
  with HeadersHelpers
  with Logging:

  private val skipResponseHeaders = Set("Content-Type", "Content-Length", "Transfer-Encoding")

  def forwardGetRequest(url: String, headers: Seq[(String, String)])(using request: Request[AnyContent]): Future[Result] =
    forwardRequest(GET, url, headers)

  def forwardPostRequest(url: String, headers: Seq[(String, String)])(using request: Request[AnyContent]): Future[Result] =
    forwardRequest(POST, url, headers)

  private def requestQueryString(using request: Request[AnyContent]): String =
    Option(request.target.queryString).filter(_.nonEmpty).map(s => s"?$s").getOrElse("")

  private def forwardRequest(
    httpVerb: String,
    url: String,
    headers: Seq[(String, String)]
  )(using request: Request[AnyContent]
  ): Future[Result] =
    val correlationId = request.headers.get("CorrelationId").getOrElse("none")

    logger.info(s"$httpVerb $url \nCorrelationId: $correlationId")

    // The default HttpReads will wrap the response in an exception and make the body inaccessible
    given responseReads: HttpReads[HttpResponse] = (_, _, response: HttpResponse) => response

    val result =
      if httpVerb == GET then
        httpClient.GET[HttpResponse](url + requestQueryString, Seq.empty, headers)
      else
        request.body.asJson match {
          case Some(json) => httpClient.POST[JsValue, HttpResponse](url, json, headers)
          case None       => Future.failed(NonJsonBodyException())
        }

    result.map { response =>
      val body       = response.body
      val logMessage = s"BST response ${response.status} $url \nCorrelationId: $correlationId \nHEADERS: ${toPrintableResponseHeaders(response)}"

      if response.status == OK || response.status == CREATED then
        logger.info(logMessage)
      else
        logger.warn(logMessage)

      val responseHeaders = headersMapToSeq(response.headers).filter(h => !skipResponseHeaders.exists(_.equalsIgnoreCase(h._1))) :+ "API_URL" -> url

      Status(response.status)(body)
        .withHeaders(responseHeaders*)
    }.recoverWith { exception =>
      logger.warn(s"Failed $httpVerb $url \nCorrelationId: $correlationId \nException: ${exception.getClass.getName} ${exception.getMessage}")
      Future.failed(exception)
    }.recover {
      case _: NonJsonBodyException => BadRequest(Json.obj("statusCode" -> BAD_REQUEST, "message" -> "JSON body is expected in request"))
    }
