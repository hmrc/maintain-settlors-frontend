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

package controllers.business.remove

import base.SpecBase
import connectors.TrustConnector
import forms.RemoveIndexFormProvider
import models.settlors.{BusinessSettlor, Settlors}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.business.remove.RemoveBusinessSettlorView

import java.time.LocalDate
import scala.concurrent.Future

class RemoveBusinessSettlorControllerSpec extends SpecBase with ScalaCheckPropertyChecks with ScalaFutures {

  val messagesPrefix = "removeBusinessSettlorYesNo"

  lazy val formProvider        = new RemoveIndexFormProvider()
  lazy val form: Form[Boolean] = formProvider(messagesPrefix)

  lazy val name: String = "Name"

  val mockConnector: TrustConnector = mock[TrustConnector]

  def businessSettlor(id: Int, provisional: Boolean): BusinessSettlor = BusinessSettlor(
    name = name,
    companyType = None,
    companyTime = None,
    utr = None,
    address = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = provisional
  )

  val settlors: List[BusinessSettlor] = List(
    businessSettlor(1, provisional = false),
    businessSettlor(2, provisional = true),
    businessSettlor(3, provisional = true)
  )

  "RemoveBusinessSettlor Controller" when {

    "return OK and the correct view for a GET" in {

      val index = 1

      when(mockConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.successful(Settlors(Nil, settlors, None)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, routes.RemoveBusinessSettlorController.onPageLoad(index).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RemoveBusinessSettlorView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, index, name)(request, messages).toString

      application.stop()
    }

    "redirect to the add to page when settlor is not provisional" in {

      val index = 0

      when(mockConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.successful(Settlors(Nil, settlors, None)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, routes.RemoveBusinessSettlorController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.AddASettlorController.onPageLoad().url

      application.stop()
    }

    "not removing the settlor" must {

      "redirect to the add to page when valid data is submitted" in {

        val index = 0

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustConnector].toInstance(mockConnector))
          .build()

        val request =
          FakeRequest(POST, routes.RemoveBusinessSettlorController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddASettlorController.onPageLoad().url

        application.stop()
      }
    }

    "removing a new settlor" must {

      "redirect to the add to page, removing the settlor" in {

        val index = 2

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustConnector].toInstance(mockConnector))
          .build()

        when(mockConnector.getSettlors(any())(any(), any()))
          .thenReturn(Future.successful(Settlors(Nil, settlors, None)))

        when(mockConnector.removeSettlor(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200, "")))

        val request =
          FakeRequest(POST, routes.RemoveBusinessSettlorController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddASettlorController.onPageLoad().url

        application.stop()
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val index = 0

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustConnector].toInstance(mockConnector))
        .build()

      val request =
        FakeRequest(POST, routes.RemoveBusinessSettlorController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[RemoveBusinessSettlorView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, name)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val index = 0

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.RemoveBusinessSettlorController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val index = 0

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, routes.RemoveBusinessSettlorController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to the add settlors page if we get an Index Not Found Exception" in {

      val index = 1

      when(mockConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.failed(new IndexOutOfBoundsException("")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, routes.RemoveBusinessSettlorController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.AddASettlorController.onPageLoad().url

      application.stop()
    }

    "redirect to the Service down page if we get a RunTimeException" in {

      val index = 1

      when(mockConnector.getSettlors(any())(any(), any()))
        .thenReturn(Future.failed(new RuntimeException("")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, routes.RemoveBusinessSettlorController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }
  }

}
