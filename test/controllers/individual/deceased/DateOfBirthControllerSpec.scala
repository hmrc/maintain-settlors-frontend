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

package controllers.individual.deceased

import base.SpecBase
import config.annotations.DeceasedSettlor
import forms.DateOfBirthFormProvider
import models.{Name, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.individual.deceased.{DateOfBirthPage, DateOfDeathPage, NamePage}
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import views.html.individual.deceased.DateOfBirthView

import java.time.LocalDate
import scala.concurrent.Future

class DateOfBirthControllerSpec extends SpecBase with MockitoSugar {

  val dateOfDeath: LocalDate = LocalDate.parse("2019-02-03")

  val formProvider = new DateOfBirthFormProvider(frontendAppConfig)
  private def form = formProvider.withConfig("deceasedSettlor.dateOfBirth")

  def onwardRoute: Call = Call("GET", "/foo")

  val validAnswer: LocalDate = LocalDate.parse("2000-02-03")
  val invalidAnswer: LocalDate = LocalDate.parse("2020-02-03")
  val name: Name = Name("FirstName", None, "LastName")
  val index: Int = 0

  lazy val dateOfBirthRoute: String = routes.DateOfBirthController.onPageLoad().url

  val baseAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value
    .set(DateOfDeathPage, dateOfDeath).success.value

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateOfBirthRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, dateOfBirthRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "DateOfBirth Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val result = route(application, getRequest).value

      val view = application.injector.instanceOf[DateOfBirthView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, name.displayName)(getRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(DateOfBirthPage, validAnswer).success.value
        .set(NamePage, name).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val view = application.injector.instanceOf[DateOfBirthView]

      val result = route(application, getRequest).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), name.displayName)(getRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val mockPlaybackRepository = mock[PlaybackRepository]

      when(mockPlaybackRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].qualifiedWith(classOf[DeceasedSettlor]).toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      val result = route(application, postRequest()).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, dateOfBirthRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[DateOfBirthView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, name.displayName)(request, messages).toString

      application.stop()
    }

    "return a Bad Request and errors when submitted date is after date of death" in {

      val form = formProvider.withConfig("deceasedSettlor.dateOfBirth", (dateOfDeath, "afterDateOfDeath"))

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, dateOfBirthRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> invalidAnswer.getDayOfMonth.toString,
            "value.month" -> invalidAnswer.getMonthValue.toString,
            "value.year"  -> invalidAnswer.getYear.toString
          )

      val boundForm = form.bind(Map(
        "value.day"   -> invalidAnswer.getDayOfMonth.toString,
        "value.month" -> invalidAnswer.getMonthValue.toString,
        "value.year"  -> invalidAnswer.getYear.toString
      ))

      val view = application.injector.instanceOf[DateOfBirthView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, name.displayName)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val result = route(application, getRequest).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val result = route(application, postRequest()).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
