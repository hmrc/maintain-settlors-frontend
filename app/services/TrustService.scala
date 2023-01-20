/*
 * Copyright 2023 HM Revenue & Customs
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

package services

import com.google.inject.ImplementedBy
import connectors.TrustConnector
import models.settlors.{BusinessSettlor, DeceasedSettlor, IndividualSettlor, Settlors}
import models.{NationalInsuranceNumber, RemoveSettlor}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustServiceImpl @Inject()(connector: TrustConnector) extends TrustService {

  override def getSettlors(utr: String)(implicit hc:HeaderCarrier, ec:ExecutionContext): Future[Settlors] =
    connector.getSettlors(utr)

  override def getIndividualSettlor(utr: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[IndividualSettlor] =
    getSettlors(utr).map(_.settlor(index))

  override def getDeceasedSettlor(utr: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Option[DeceasedSettlor]] =
    connector.getSettlors(utr).map(_.deceased)

  override def getBusinessSettlor(utr: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[BusinessSettlor] =
    getSettlors(utr).map(_.settlorCompany(index))

  override def removeSettlor(utr: String, settlor: RemoveSettlor)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    connector.removeSettlor(utr, settlor)

  override def getBusinessUtrs(identifier: String, index: Option[Int])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[String]] =
    getSettlors(identifier).map(_.settlorCompany
      .zipWithIndex
      .filterNot(x => index.contains(x._2))
      .flatMap(_._1.utr)
    )

  override def getIndividualNinos(identifier: String, index: Option[Int], adding: Boolean)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[String]] = {
    getSettlors(identifier) map { all =>

      val deceasedSettlorNino = if (index.isDefined || adding) {
        all.deceased
          .flatMap(_.identification)
          .collect {
            case NationalInsuranceNumber(nino) => nino
          }
      } else {
        None
      }

      val livingSettlorNinos = all.settlor
        .zipWithIndex
        .filterNot(x => index.contains(x._2))
        .flatMap(_._1.identification)
        .collect {
          case NationalInsuranceNumber(nino) => nino
        }

      deceasedSettlorNino.toList ++ livingSettlorNinos
    }
  }

}

@ImplementedBy(classOf[TrustServiceImpl])
trait TrustService {

  def getSettlors(utr: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Settlors]

  def getIndividualSettlor(utr: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[IndividualSettlor]

  def getDeceasedSettlor(utr: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Option[DeceasedSettlor]]

  def getBusinessSettlor(utr: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[BusinessSettlor]

  def removeSettlor(utr: String, settlor: RemoveSettlor)(implicit hc:HeaderCarrier, ec:ExecutionContext): Future[HttpResponse]

  def getBusinessUtrs(identifier: String, index: Option[Int])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[String]]

  def getIndividualNinos(identifier: String, index: Option[Int], adding: Boolean)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[String]]
}
