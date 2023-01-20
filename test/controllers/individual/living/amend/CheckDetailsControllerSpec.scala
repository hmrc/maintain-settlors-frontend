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

package controllers.individual.living.amend

import base.SpecBase
import connectors.TrustConnector
import models.Name
import models.settlors.IndividualSettlor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.individual.living._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.HttpResponse
import utils.print.IndividualSettlorPrintHelper
import views.html.individual.living.amend.CheckDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class CheckDetailsControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private val index: Int = 0
  private lazy val getFromBackendRoute: String = routes.CheckDetailsController.extractAndRender(index).url
  private lazy val getFromUserAnswersRoute: String = routes.CheckDetailsController.renderFromUserAnswers(index).url
  private lazy val submitRoute: String = routes.CheckDetailsController.onSubmit(index).url
  private lazy val onwardRoute: String = controllers.routes.AddASettlorController.onPageLoad().url

  private val name: Name = Name("First", None, "Last")
  private val date: LocalDate = LocalDate.parse("1996-02-03")

  private val userAnswers = emptyUserAnswers
    .set(NamePage, name).success.value
    .set(DateOfBirthYesNoPage, false).success.value
    .set(NationalInsuranceNumberYesNoPage, false).success.value
    .set(AddressYesNoPage, false).success.value
    .set(StartDatePage, date).success.value

  private val provisional: Boolean = false

  "CheckDetails Controller" must {

    "return OK and the correct view for a GET" when {

      ".extractAndRender" in {

        val mockTrustService: TrustService = mock[TrustService]

        when(mockTrustService.getIndividualSettlor(any(), any())(any(), any()))
          .thenReturn(Future.successful(IndividualSettlor(name, None, None, None, None, None, None, date, provisional = provisional)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[TrustService].toInstance(mockTrustService))
          .build()

        val request = FakeRequest(GET, getFromBackendRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckDetailsView]
        val printHelper = application.injector.instanceOf[IndividualSettlorPrintHelper]
        val answerSection = printHelper(userAnswers, provisional, name.displayName)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(answerSection, index)(request, messages).toString
      }

      ".renderFromUserAnswers" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, getFromUserAnswersRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckDetailsView]
        val printHelper = application.injector.instanceOf[IndividualSettlorPrintHelper]
        val answerSection = printHelper(userAnswers, adding = false, name.displayName)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(answerSection, index)(request, messages).toString
      }
    }

    "redirect to the 'add a settlor' page when submitted" in {

      val mockTrustConnector: TrustConnector = mock[TrustConnector]

      when(mockTrustConnector.amendIndividualSettlor(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
        .build()

      val request = FakeRequest(POST, submitRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute

      application.stop()
    }

    "handle error if mapper fails" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(POST, submitRoute)

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }
  }
}
