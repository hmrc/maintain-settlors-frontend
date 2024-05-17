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

package controllers

import base.SpecBase
import connectors.TrustConnector
import models.TaskStatus.InProgress
import models.settlors.{DeceasedSettlor, IndividualSettlor, Settlors}
import models.{Name, TaxableMigrationFlag, TrustDetails, TypeOfTrust, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import pages.AdditionalSettlorsYesNoPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustsStoreService
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockTrustConnector: TrustConnector = mock[TrustConnector]
  val mockTrustsStoreService: TrustsStoreService = mock[TrustsStoreService]

  val identifier = "1234567890"
  val startDate = "2019-06-01"
  val typeOfTrust: Option[TypeOfTrust] = Some(TypeOfTrust.WillTrustOrIntestacyTrust)
  val isTaxable = false
  val isUnderlyingData5mld = false
  val migratingFromNonTaxableToTaxable = false

  override def beforeEach(): Unit = {
    reset(mockTrustsStoreService)

    when(mockTrustConnector.isTrust5mld(any())(any(), any()))
      .thenReturn(Future.successful(isUnderlyingData5mld))

    when(mockTrustConnector.getIsDeceasedSettlorDateOfDeathRecorded(any())(any(), any()))
      .thenReturn(Future.successful(true))

    when(mockTrustsStoreService.updateTaskStatus(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK, "")))
  }

  "Index Controller" must {

    "redirect to task list when there are living settlors" in {

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(
          TrustDetails(
            startDate = LocalDate.parse(startDate),
            typeOfTrust = typeOfTrust,
            deedOfVariation = None,
            trustTaxable = Some(isTaxable)
          )
        ))

      when(mockTrustConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.successful(
          Settlors(
            settlor = List(IndividualSettlor(Name("Adam", None, "Test"), None, None, None, None, None, None, LocalDate.now, provisional = false)),
            settlorCompany = Nil,
            deceased = Some(DeceasedSettlor(
              None,
              Name("First", None, "Last"),
              None, None, None, None, None, None
            ))
          )
        ))

      when(mockTrustConnector.getTrustMigrationFlag(any())(any(), any()))
        .thenReturn(Future.successful(TaxableMigrationFlag(Some(migratingFromNonTaxableToTaxable))))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[TrustConnector].toInstance(mockTrustConnector),
          bind[TrustsStoreService].toInstance(mockTrustsStoreService)
        ).build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(identifier).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.AddASettlorController.onPageLoad().url)

      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)

      uaCaptor.getValue.internalId mustBe "id"
      uaCaptor.getValue.identifier mustBe identifier
      uaCaptor.getValue.whenTrustSetup mustBe LocalDate.parse(startDate)
      uaCaptor.getValue.isTaxable mustBe isTaxable
      uaCaptor.getValue.isUnderlyingData5mld mustBe isUnderlyingData5mld
      uaCaptor.getValue.migratingFromNonTaxableToTaxable mustBe migratingFromNonTaxableToTaxable

      verify(mockTrustsStoreService).updateTaskStatus(eqTo(identifier), eqTo(InProgress))(any(), any())

      application.stop()
    }

    "redirect to task list when there are no living settlors but user has previously answered yes to are there additional settlors to add to the trust" in {

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(
          TrustDetails(
            startDate = LocalDate.parse(startDate),
            typeOfTrust = typeOfTrust,
            deedOfVariation = None,
            trustTaxable = Some(isTaxable)
          )
        ))

      when(mockTrustConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.successful(
          Settlors(
            settlor = Nil,
            settlorCompany = Nil,
            deceased = Some(DeceasedSettlor(
              None,
              Name("First", None, "Last"),
              None, None, None, None, None, None
            ))
          )
        ))

      when(mockTrustConnector.getTrustMigrationFlag(any())(any(), any()))
        .thenReturn(Future.successful(TaxableMigrationFlag(None)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(AdditionalSettlorsYesNoPage, true).success.value))
        .overrides(
          bind[TrustConnector].toInstance(mockTrustConnector),
          bind[TrustsStoreService].toInstance(mockTrustsStoreService)
        ).build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(identifier).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.AddASettlorController.onPageLoad().url)

      verify(mockTrustsStoreService).updateTaskStatus(eqTo(identifier), eqTo(InProgress))(any(), any())

      application.stop()
    }

    "redirect to deceased settlor check answers when there are no living settlors" in {

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(
          TrustDetails(
            startDate = LocalDate.parse(startDate),
            typeOfTrust = typeOfTrust,
            deedOfVariation = None,
            trustTaxable = Some(isTaxable)
          )
        ))

      when(mockTrustConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.successful(
          Settlors(
            settlor = Nil,
            settlorCompany = Nil,
            deceased = Some(DeceasedSettlor(
              None, Name("First", None, "Last"),
              None, None, None, None, None, None
            ))
          )
        ))

      when(mockTrustConnector.getTrustMigrationFlag(any())(any(), any()))
        .thenReturn(Future.successful(TaxableMigrationFlag(None)))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[TrustConnector].toInstance(mockTrustConnector),
          bind[TrustsStoreService].toInstance(mockTrustsStoreService)
        ).build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(identifier).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.individual.deceased.routes.CheckDetailsController.extractAndRender().url)

      verify(mockTrustsStoreService).updateTaskStatus(eqTo(identifier), eqTo(InProgress))(any(), any())

      application.stop()
    }
  }
}
