/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.individual.deceased

import base.SpecBase
import config.FrontendAppConfig
import connectors.{TrustConnector, TrustStoreConnector}
import models.BpMatchStatus.{FailedToMatch, FullyMatched}
import models.TaskStatus.Completed
import models.settlors.{DeceasedSettlor, IndividualSettlor, Settlors}
import models.{BpMatchStatus, Name, NationalInsuranceNumber, UserAnswers}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.AdditionalSettlorsYesNoPage
import pages.individual.deceased._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HttpResponse
import utils.print.DeceasedSettlorPrintHelper
import views.html.individual.deceased.CheckDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class CheckDetailsControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private val appConfig = app.injector.instanceOf[FrontendAppConfig]

  private lazy val checkDetailsRoute = routes.CheckDetailsController.extractAndRender().url
  private lazy val submitDetailsRoute = routes.CheckDetailsController.onSubmit().url

  private lazy val onwardRoute = controllers.routes.AddASettlorController.onPageLoad().url
  private lazy val finishedRoute = appConfig.maintainATrustOverview

  private val name = Name("First", None, "Last")
  private val dateOfDeath = LocalDate.parse("2018-02-03")
  private val dateOfBirth = LocalDate.parse("2010-02-03")
  private val nino = "AA123456A"
  private val startDate = LocalDate.parse("2019-03-09")

  private val individualSettlor = IndividualSettlor(
    name = Name(
      firstName = "First",
      middleName = None,
      lastName = "Last"
    ),
    dateOfBirth = Some(LocalDate.parse("2010-02-03")),
    countryOfNationality = None,
    countryOfResidence = None,
    identification = Some(NationalInsuranceNumber("AA123456A")),
    address = None,
    mentalCapacityYesNo = None,
    entityStart = startDate,
    provisional = false
  )

  private def userAnswers(bpMatchStatus: BpMatchStatus = FullyMatched,
                          isDateOfDeathRecorded: Boolean = true,
                          addAdditionalSettlors: Boolean = false): UserAnswers = {

    emptyUserAnswers.copy(isDateOfDeathRecorded = isDateOfDeathRecorded)
      .set(BpMatchStatusPage, bpMatchStatus).success.value
      .set(NamePage, name).success.value
      .set(DateOfDeathYesNoPage, true).success.value
      .set(DateOfDeathPage, dateOfDeath).success.value
      .set(DateOfBirthYesNoPage, true).success.value
      .set(DateOfBirthPage, dateOfBirth).success.value
      .set(NationalInsuranceNumberYesNoPage, true).success.value
      .set(NationalInsuranceNumberPage, nino).success.value
      .set(AdditionalSettlorsYesNoPage, addAdditionalSettlors).success.value
  }

  private def deceasedSettlor(matchStatus: BpMatchStatus = FullyMatched) = DeceasedSettlor(
    bpMatchStatus = Some(matchStatus),
    name = name,
    dateOfDeath = Some(dateOfDeath),
    dateOfBirth = Some(dateOfBirth),
    identification = Some(NationalInsuranceNumber(nino)),
    address = None
  )

  "CheckDetails Controller" must {

    "return OK and the correct view for a GET" when {

      "there are additional settlors" when {

        "01 match status and date of death is known to ETMP" in {

          val matchStatus = FullyMatched

          val ua = userAnswers(matchStatus)

          val mockService : TrustService = mock[TrustService]

          val application = applicationBuilder(userAnswers = Some(ua))
            .overrides(
              bind[TrustService].toInstance(mockService)
            )
            .build()

          when(mockService.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(List(individualSettlor), Nil, Some(deceasedSettlor(matchStatus)))))

          val request = FakeRequest(GET, checkDetailsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckDetailsView]
          val printHelper = application.injector.instanceOf[DeceasedSettlorPrintHelper]
          val answerSection = printHelper(ua, name.displayName, hasAdditionalSettlors = true)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              answerSection,
              name.displayName,
              is01MatchStatus = true,
              isDateOfDeathRecorded = true
            )(request, messages).toString
        }

        "01 match status and date of death is not known to ETMP" in {

          val matchStatus = FullyMatched

          val ua = userAnswers(matchStatus, isDateOfDeathRecorded = false)

          val mockService : TrustService = mock[TrustService]

          val application = applicationBuilder(userAnswers = Some(ua))
            .overrides(
              bind[TrustService].toInstance(mockService)
            )
            .build()

          when(mockService.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(List(individualSettlor), Nil, Some(deceasedSettlor(matchStatus)))))

          val request = FakeRequest(GET, checkDetailsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckDetailsView]
          val printHelper = application.injector.instanceOf[DeceasedSettlorPrintHelper]
          val answerSection = printHelper(ua, name.displayName, hasAdditionalSettlors = true)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              answerSection,
              name.displayName,
              is01MatchStatus = true,
              isDateOfDeathRecorded = false
            )(request, messages).toString
        }

        "not a 01 match status" in {

          val matchStatus = FailedToMatch

          val ua = userAnswers(matchStatus)

          val mockService : TrustService = mock[TrustService]

          val application = applicationBuilder(userAnswers = Some(ua))
            .overrides(
              bind[TrustService].toInstance(mockService)
            )
            .build()

          when(mockService.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(List(individualSettlor), Nil, Some(deceasedSettlor(matchStatus)))))

          val request = FakeRequest(GET, checkDetailsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckDetailsView]
          val printHelper = application.injector.instanceOf[DeceasedSettlorPrintHelper]
          val answerSection = printHelper(ua, name.displayName, hasAdditionalSettlors = true)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              answerSection,
              name.displayName,
              is01MatchStatus = false,
              isDateOfDeathRecorded = true
            )(request, messages).toString
        }
      }

      "there are no additional settlors" when {

        "01 match status and date of death is known to ETMP" in {

          val matchStatus = FullyMatched

          val ua = userAnswers(matchStatus)

          val mockService : TrustService = mock[TrustService]

          val application = applicationBuilder(userAnswers = Some(ua))
            .overrides(
              bind[TrustService].toInstance(mockService)
            )
            .build()

          when(mockService.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(Nil, Nil, Some(deceasedSettlor(matchStatus)))))

          val request = FakeRequest(GET, checkDetailsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckDetailsView]
          val printHelper = application.injector.instanceOf[DeceasedSettlorPrintHelper]
          val answerSection = printHelper(ua, name.displayName, hasAdditionalSettlors = false)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              answerSection,
              name.displayName,
              is01MatchStatus = true,
              isDateOfDeathRecorded = true
            )(request, messages).toString
        }

        "01 match status and date of death is not known to ETMP" in {

          val matchStatus = FullyMatched

          val ua = userAnswers(matchStatus, isDateOfDeathRecorded = false)

          val mockService : TrustService = mock[TrustService]

          val application = applicationBuilder(userAnswers = Some(ua))
            .overrides(
              bind[TrustService].toInstance(mockService)
            )
            .build()

          when(mockService.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(Nil, Nil, Some(deceasedSettlor(matchStatus)))))

          val request = FakeRequest(GET, checkDetailsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckDetailsView]
          val printHelper = application.injector.instanceOf[DeceasedSettlorPrintHelper]
          val answerSection = printHelper(ua, name.displayName, hasAdditionalSettlors = false)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              answerSection,
              name.displayName,
              is01MatchStatus = true,
              isDateOfDeathRecorded = false
            )(request, messages).toString
        }

        "not a 01 match status" in {

          val matchStatus = FailedToMatch

          val ua = userAnswers(matchStatus)

          val mockService : TrustService = mock[TrustService]

          val application = applicationBuilder(userAnswers = Some(ua))
            .overrides(
              bind[TrustService].toInstance(mockService)
            )
            .build()

          when(mockService.getSettlors(any())(any(), any()))
            .thenReturn(Future.successful(Settlors(Nil, Nil, Some(deceasedSettlor(matchStatus)))))

          val request = FakeRequest(GET, checkDetailsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckDetailsView]
          val printHelper = application.injector.instanceOf[DeceasedSettlorPrintHelper]
          val answerSection = printHelper(ua, name.displayName, hasAdditionalSettlors = false)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              answerSection,
              name.displayName,
              is01MatchStatus = false,
              isDateOfDeathRecorded = true
            )(request, messages).toString
        }
      }
    }

    "redirect to the 'add a settlor' page when submitted if there are other settlors" in {

      val mockTrustConnector = mock[TrustConnector]
      val mockTrustService = mock[TrustService]

      val application =
        applicationBuilder(userAnswers = Some(userAnswers()), affinityGroup = Agent)
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .overrides(bind[TrustService].toInstance(mockTrustService))
          .build()

      when(mockTrustConnector.amendDeceasedSettlor(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))
      when(mockTrustService.getSettlors(any())(any(), any())).thenReturn(Future.successful(Settlors(List(individualSettlor), Nil, Some(deceasedSettlor()))))

      val request = FakeRequest(POST, submitDetailsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute

      application.stop()
    }

    "redirect to the 'add a settlor' page when submitted if there are no other settlors but user selected yes to Are there any additional settlors for the trust?" in {

      val mockTrustConnector = mock[TrustConnector]
      val mockTrustService = mock[TrustService]

      val application =
        applicationBuilder(userAnswers = Some(userAnswers(addAdditionalSettlors = true)), affinityGroup = Agent)
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .overrides(bind[TrustService].toInstance(mockTrustService))
          .build()

      when(mockTrustConnector.amendDeceasedSettlor(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))
      when(mockTrustService.getSettlors(any())(any(), any())).thenReturn(Future.successful(Settlors(Nil, Nil, Some(deceasedSettlor()))))

      val request = FakeRequest(POST, submitDetailsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute

      application.stop()
    }

    "redirect to 'maintain a trust overview' when submitted if there are no other settlors" in {

      val mockTrustConnector = mock[TrustConnector]
      val mockTrustService = mock[TrustService]
      val mockTrustStoreConnector = mock[TrustStoreConnector]

      val application =
        applicationBuilder(userAnswers = Some(userAnswers()), affinityGroup = Agent)
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .overrides(bind[TrustService].toInstance(mockTrustService))
          .overrides(bind[TrustStoreConnector].toInstance(mockTrustStoreConnector))
          .build()

      when(mockTrustConnector.amendDeceasedSettlor(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))
      when(mockTrustService.getSettlors(any())(any(), any())).thenReturn(Future.successful(Settlors(Nil, Nil, Some(deceasedSettlor()))))
      when(mockTrustStoreConnector.updateTaskStatus(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, submitDetailsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual finishedRoute

      verify(mockTrustStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

      application.stop()
    }
  }
}
