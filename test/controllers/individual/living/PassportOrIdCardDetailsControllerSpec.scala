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

package controllers.individual.living

import base.SpecBase
import config.annotations.LivingSettlor
import forms.CombinedPassportOrIdCardDetailsFormProvider
import models.{CombinedPassportOrIdCard, DetailsType, Mode, Name, NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.individual.living.{NamePage, PassportOrIdCardDetailsPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import views.html.individual.living.PassportOrIdCardDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class PassportOrIdCardDetailsControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val formProvider: CombinedPassportOrIdCardDetailsFormProvider = new CombinedPassportOrIdCardDetailsFormProvider(frontendAppConfig)
  private val form: Form[CombinedPassportOrIdCard] = formProvider.withPrefix("livingSettlor.passportOrIdCardDetails")
  private val name: Name = Name("Joe", None, "Bloggs")

  private val countryOptions: CountryOptions = injector.instanceOf[CountryOptions]

  val mode: Mode = NormalMode

  override val emptyUserAnswers: UserAnswers = super.emptyUserAnswers
    .set(NamePage, name).success.value

  private lazy val passportOrIdCardDetailsRoute: String = routes.PassportOrIdCardDetailsController.onPageLoad(mode).url

  private val validData: CombinedPassportOrIdCard = CombinedPassportOrIdCard("country", "number", LocalDate.parse("2020-02-03"))

  override def beforeEach(): Unit = {
    reset(playbackRepository)
    when(playbackRepository.set(any())) thenReturn Future.successful(true)
  }

  "PassportOrIdCardDetails Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, passportOrIdCardDetailsRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[PassportOrIdCardDetailsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mode, name.displayName, countryOptions.options)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(PassportOrIdCardDetailsPage, validData).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, passportOrIdCardDetailsRoute)

      val view = application.injector.instanceOf[PassportOrIdCardDetailsView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validData), mode, name.displayName, countryOptions.options)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" when {

      "answer has changed" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].qualifiedWith(classOf[LivingSettlor]).toInstance(fakeNavigator))
          .build()

        val request = FakeRequest(POST, passportOrIdCardDetailsRoute)
          .withFormUrlEncodedBody(
            "country" -> validData.countryOfIssue,
            "number" -> validData.number,
            "expiryDate.day" -> validData.expirationDate.getDayOfMonth.toString,
            "expiryDate.month" -> validData.expirationDate.getMonthValue.toString,
            "expiryDate.year" -> validData.expirationDate.getYear.toString,
            "detailsType" -> validData.detailsType.toString
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

        val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(playbackRepository).set(uaCaptor.capture)
        uaCaptor.getValue.get(PassportOrIdCardDetailsPage).get.detailsType mustBe DetailsType.CombinedProvisional

        application.stop()
      }

      "answer has not changed" in {

        val userAnswers = emptyUserAnswers.set(PassportOrIdCardDetailsPage, validData).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[Navigator].qualifiedWith(classOf[LivingSettlor]).toInstance(fakeNavigator))
          .build()

        val request = FakeRequest(POST, passportOrIdCardDetailsRoute)
          .withFormUrlEncodedBody(
            "country" -> validData.countryOfIssue,
            "number" -> validData.number,
            "expiryDate.day" -> validData.expirationDate.getDayOfMonth.toString,
            "expiryDate.month" -> validData.expirationDate.getMonthValue.toString,
            "expiryDate.year" -> validData.expirationDate.getYear.toString,
            "detailsType" -> validData.detailsType.toString
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

        val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(playbackRepository).set(uaCaptor.capture)
        uaCaptor.getValue.get(PassportOrIdCardDetailsPage).get.detailsType mustBe DetailsType.Combined

        application.stop()
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, passportOrIdCardDetailsRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[PassportOrIdCardDetailsView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, mode, name.displayName, countryOptions.options)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, passportOrIdCardDetailsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, passportOrIdCardDetailsRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
