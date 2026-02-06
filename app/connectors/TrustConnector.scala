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

package connectors

import config.FrontendAppConfig
import models.settlors.{BusinessSettlor, DeceasedSettlor, IndividualSettlor, Settlors}
import models.{RemoveSettlor, TaxableMigrationFlag, TrustDetails}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.{readFromJson, readRaw}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject() (http: HttpClientV2, config: FrontendAppConfig) {

  private val trustsUrl: String   = s"${config.trustsUrl}/trusts"
  private val settlorsUrl: String = s"$trustsUrl/settlors"

  def getTrustDetails(identifier: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] = {
    val url: String = s"$trustsUrl/trust-details/$identifier/transformed"
    http.get(url"$url").execute[TrustDetails]
  }

  def getSettlors(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Settlors] = {
    val url: String = s"$settlorsUrl/$identifier/transformed"
    http.get(url"$url").execute[Settlors]

  }

  def getIsDeceasedSettlorDateOfDeathRecorded(
    identifier: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val url: String = s"$settlorsUrl/$identifier/transformed/deceased-settlor-death-recorded"
    http.get(url"$url").execute[Boolean]

  }

  def addIndividualSettlor(identifier: String, settlor: IndividualSettlor)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {
    val url: String = s"$settlorsUrl/add-individual/$identifier"
    http.post(url"$url").withBody(Json.toJson(settlor)).execute[HttpResponse]

  }

  def amendIndividualSettlor(identifier: String, index: Int, individual: IndividualSettlor)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {
    val url: String = s"$settlorsUrl/amend-individual/$identifier/$index"
    http.post(url"$url").withBody(Json.toJson(individual)).execute[HttpResponse]

  }

  def addBusinessSettlor(identifier: String, settlor: BusinessSettlor)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {
    val url: String = s"$settlorsUrl/add-business/$identifier"
    http.post(url"$url").withBody(Json.toJson(settlor)).execute[HttpResponse]

  }

  def amendBusinessSettlor(identifier: String, index: Int, business: BusinessSettlor)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {
    val url: String = s"$settlorsUrl/amend-business/$identifier/$index"
    http.post(url"$url").withBody(Json.toJson(business)).execute[HttpResponse]

  }

  def amendDeceasedSettlor(identifier: String, deceasedSettlor: DeceasedSettlor)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {
    val url: String = s"$settlorsUrl/amend-deceased/$identifier"
    http.post(url"$url").withBody(Json.toJson(deceasedSettlor)).execute[HttpResponse]

  }

  def removeSettlor(identifier: String, settlor: RemoveSettlor)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {
    val url: String = s"$settlorsUrl/$identifier/remove"
    http.put(url"$url").withBody(Json.toJson(settlor)).execute[HttpResponse]

  }

  def isTrust5mld(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val url: String = s"$trustsUrl/$identifier/is-trust-5mld"
    http.get(url"$url").execute[Boolean]
  }

  def getTrustMigrationFlag(
    identifier: String
  )(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TaxableMigrationFlag] = {
    val url = s"$trustsUrl/$identifier/taxable-migration/migrating-to-taxable"
    http.get(url"$url").execute[TaxableMigrationFlag]
  }

}
