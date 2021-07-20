/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.TrustConnector
import models.settlors._
import models.{CombinedPassportOrIdCard, Name, NationalInsuranceNumber, RemoveSettlor, SettlorType}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class TrustServiceSpec extends FreeSpec with MockitoSugar with MustMatchers with ScalaFutures {

  val mockConnector: TrustConnector = mock[TrustConnector]

  val service = new TrustServiceImpl(mockConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val individualSettlor: IndividualSettlor = IndividualSettlor(
    name = Name(firstName = "1234567890 QwErTyUiOp ,.(/)&'- name", middleName = None, lastName = "1234567890 QwErTyUiOp ,.(/)&'- name"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    countryOfNationality = None,
    countryOfResidence = None,
    identification = None,
    address = None,
    mentalCapacityYesNo = None,
    entityStart = LocalDate.of(2012, 4, 15),
    provisional = false
  )

  val deceasedSettlor: DeceasedSettlor = DeceasedSettlor(
    bpMatchStatus = None,
    name = Name(firstName = "first", middleName = None, lastName = "last"),
    dateOfDeath = Some(LocalDate.parse("1993-09-24")),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    identification = None,
    address = None
  )

  val businessSettlor: BusinessSettlor = BusinessSettlor(
    name = "Company Settlor Name",
    companyType = None,
    companyTime = None,
    utr = None,
    address = None,
    entityStart = LocalDate.of(2017, 2, 28),
    provisional = false
  )
  
  val identifier: String = "1234567890"

  "Trust service" - {

    "get settlors" in {

      when(mockConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.successful(
          Settlors(List(individualSettlor), List(businessSettlor), Some(deceasedSettlor))
        ))

      val result = service.getSettlors(identifier)

      whenReady(result) {
        _ mustBe Settlors(List(individualSettlor), List(businessSettlor), Some(deceasedSettlor))
      }
    }

    "get settlor" in {

      val index = 0

      when(mockConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.successful(Settlors(List(individualSettlor), List(businessSettlor), Some(deceasedSettlor))))

      whenReady(service.getIndividualSettlor(identifier, index)) {
        _ mustBe individualSettlor
      }

      whenReady(service.getBusinessSettlor(identifier, index)) {
        _ mustBe businessSettlor
      }

      whenReady(service.getDeceasedSettlor(identifier)) {
        _ mustBe Some(deceasedSettlor)
      }
    }

    "remove settlor" in {

      when(mockConnector.removeSettlor(any(),any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val individualSettlor: RemoveSettlor = RemoveSettlor(SettlorType.IndividualSettlor,
        index = 0,
        endDate = LocalDate.now()
      )

      val businessSettlor: RemoveSettlor = RemoveSettlor(SettlorType.BusinessSettlor,
        index = 0,
        endDate = LocalDate.now()
      )

      whenReady(service.removeSettlor(identifier, individualSettlor)) { r =>
        r.status mustBe 200
      }

      whenReady(service.removeSettlor(identifier, businessSettlor)) { r =>
        r.status mustBe 200
      }

    }

    ".getBusinessUtrs" - {

      "must return empty list" - {

        "when no businesses" in {

          when(mockConnector.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(Nil, Nil, None)))

          val result = Await.result(service.getBusinessUtrs(identifier, None), Duration.Inf)

          result mustBe Nil
        }

        "when there are businesses but they don't have a UTR" in {

          val businesses = List(
            businessSettlor.copy(utr = None)
          )

          when(mockConnector.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(Nil, businesses, None)))

          val result = Await.result(service.getBusinessUtrs(identifier, None), Duration.Inf)

          result mustBe Nil
        }

        "when there is a business with a UTR but it's the same index as the one we're amending" in {

          val businesses = List(
            businessSettlor.copy(utr = Some("utr"))
          )

          when(mockConnector.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(Nil, businesses, None)))

          val result = Await.result(service.getBusinessUtrs(identifier, Some(0)), Duration.Inf)

          result mustBe Nil
        }
      }

      "must return UTRs" - {

        "when businesses have UTRs and we're adding (i.e. no index)" in {

          val businesses = List(
            businessSettlor.copy(utr = Some("utr1")),
            businessSettlor.copy(utr = Some("utr2"))
          )

          when(mockConnector.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(Nil, businesses, None)))

          val result = Await.result(service.getBusinessUtrs(identifier, None), Duration.Inf)

          result mustBe List("utr1", "utr2")
        }

        "when businesses have UTRs and we're amending" in {

          val businesses = List(
            businessSettlor.copy(utr = Some("utr1")),
            businessSettlor.copy(utr = Some("utr2"))
          )

          when(mockConnector.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(Nil, businesses, None)))

          val result = Await.result(service.getBusinessUtrs(identifier, Some(0)), Duration.Inf)

          result mustBe List("utr2")
        }
      }
    }

    ".getIndividualNinos" - {

      "must return empty list" - {

        "when no individuals" in {

          when(mockConnector.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(Nil, Nil, None)))

          val result = Await.result(service.getIndividualNinos(identifier, None), Duration.Inf)

          result mustBe Nil
        }

        "when there are individuals but they don't have a NINo" in {

          val individuals = List(
            individualSettlor.copy(identification = None)
          )

          when(mockConnector.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(individuals, Nil, None)))

          val result = Await.result(service.getIndividualNinos(identifier, None), Duration.Inf)

          result mustBe Nil
        }

        "when there are individuals but they have another form of identification" in {

          val individuals = List(
            individualSettlor.copy(identification = Some(CombinedPassportOrIdCard("FR", "num", LocalDate.parse("2000-01-01"))))
          )

          when(mockConnector.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(individuals, Nil, None)))

          val result = Await.result(service.getIndividualNinos(identifier, None), Duration.Inf)

          result mustBe Nil
        }

        "when there is a individual with a NINo but it's the same index as the one we're amending" in {

          val individuals = List(
            individualSettlor.copy(identification = Some(NationalInsuranceNumber("nino")))
          )

          when(mockConnector.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(individuals, Nil, None)))

          val result = Await.result(service.getIndividualNinos(identifier, Some(0)), Duration.Inf)

          result mustBe Nil
        }
      }

      "must return UTRs" - {

        "when individuals have NINos and we're adding (i.e. no index)" in {

          val individuals = List(
            individualSettlor.copy(identification = Some(NationalInsuranceNumber("nino1"))),
            individualSettlor.copy(identification = Some(NationalInsuranceNumber("nino2")))
          )

          when(mockConnector.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(individuals, Nil, None)))

          val result = Await.result(service.getIndividualNinos(identifier, None), Duration.Inf)

          result mustBe List("nino1", "nino2")
        }

        "when individuals have NINos and we're amending" in {

          val individuals = List(
            individualSettlor.copy(identification = Some(NationalInsuranceNumber("nino1"))),
            individualSettlor.copy(identification = Some(NationalInsuranceNumber("nino2")))
          )

          when(mockConnector.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(individuals, Nil, None)))

          val result = Await.result(service.getIndividualNinos(identifier, Some(0)), Duration.Inf)

          result mustBe List("nino2")
        }
      }
    }

  }

}
