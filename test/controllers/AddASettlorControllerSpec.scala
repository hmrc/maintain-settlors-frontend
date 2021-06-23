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

package controllers

import base.SpecBase
import connectors.TrustStoreConnector
import forms.AddASettlorFormProvider
import models.settlors.{BusinessSettlor, DeceasedSettlor, IndividualSettlor, Settlors}
import models.{AddASettlor, Name, RemoveSettlor}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.AddASettlorViewHelper
import viewmodels.addAnother.{AddRow, AddToRows}
import views.html.{AddASettlorView, MaxedOutSettlorsView}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class AddASettlorControllerSpec extends SpecBase with ScalaFutures {

  lazy val getRoute: String = controllers.routes.AddASettlorController.onPageLoad().url
  lazy val submitRoute: String = controllers.routes.AddASettlorController.submit().url
  lazy val submitCompleteRoute: String = controllers.routes.AddASettlorController.submitComplete().url

  val mockStoreConnector: TrustStoreConnector = mock[TrustStoreConnector]
  val mockViewHelper: AddASettlorViewHelper = mock[AddASettlorViewHelper]
  when(mockViewHelper.rows(any(), any(), any())(any())).thenReturn(AddToRows(Nil, Nil))

  val addTrusteeForm = new AddASettlorFormProvider()()

  private val deceasedSettlor = DeceasedSettlor(
    bpMatchStatus = None,
    name = Name(firstName = "Some", middleName = None, lastName = "One"),
    dateOfDeath = None,
    dateOfBirth = None,
    identification = None,
    address = None
  )
  private val individualSettlor = IndividualSettlor(
    name = Name(firstName = "First", middleName = None, lastName = "Last"),
    dateOfBirth = None,
    countryOfNationality = None,
    countryOfResidence = None,
    identification = None,
    address = None,
    mentalCapacityYesNo = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = false
  )

  private val businessSettlor = BusinessSettlor(
    name = "Humanitarian Company Ltd",
    companyType = None,
    companyTime = None,
    utr = None,
    address = None,
    entityStart = LocalDate.parse("2012-03-14"),
    provisional = false
  )

  private val settlors = Settlors(
    List(individualSettlor),
    List(businessSettlor),
    Some(deceasedSettlor)
  )

  val fakeAddRow: AddRow = AddRow(
    name = "Name",
    typeLabel = "Type",
    changeUrl = Some("change-url"),
    removeUrl = Some("remove-url")
  )

  class FakeService(data: Settlors) extends TrustService {

    override def getSettlors(utr: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Settlors] = Future.successful(data)

    override def getIndividualSettlor(utr: String, index: Int)
                                         (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[IndividualSettlor] = ???

    override def getBusinessSettlor(utr: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[BusinessSettlor] = ???

    override def removeSettlor(utr: String, settlor: RemoveSettlor)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = ???

    override def getDeceasedSettlor(utr: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Option[DeceasedSettlor]] = ???
  }

  "AddASettlor Controller" when {

    "no data" must {

      "redirect to Session Expired for a GET if no existing data is found" in {

        val fakeService = new FakeService(Settlors(Nil, Nil, None))

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

      "redirect to Session Expired for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("value", AddASettlor.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }
    }

    "there are settlors" must {

      "return OK and the correct view for a GET" in {

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddASettlorView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            addTrusteeForm,
            Some("This is a will trust. If the trust does not have a will settlor, you will need to change your answers."),
            Nil,
            Nil,
            "The trust has 3 settlors",
            Nil
          )(request, messages).toString

        application.stop()
      }

      "redirect to the maintain task list when the user says they are done" in {

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector),
            bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper)
          ).build()

        val request = FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("value", AddASettlor.NoComplete.toString))

        when(mockStoreConnector.setTaskComplete(any())(any(), any())).thenReturn(Future.successful(HttpResponse.apply(200, "")))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()
      }

      "redirect to the maintain task list when the user says they want to add later" ignore {

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
          .build()

        val request = FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("value", AddASettlor.YesLater.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
          .build()

        val request = FakeRequest(POST, submitRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = addTrusteeForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddASettlorView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            Some("This is a will trust. If the trust does not have a will settlor, you will need to change your answers."),
            Nil,
            Nil,
            "The trust has 3 settlors",
            Nil
          )(request, messages).toString

        application.stop()
      }
    }

    "maxed out settlors" must {

      "return OK and the correct view for a GET" in {

        val settlors = Settlors(
          List.fill(25)(individualSettlor),
          List.fill(25)(businessSettlor),
          None
        )

        val fakeService = new FakeService(settlors)

        val completedRows = List.fill(50)(fakeAddRow)

        when(mockViewHelper.rows(any(), any(), any())(any())).thenReturn(AddToRows(Nil, completedRows))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MaxedOutSettlorsView]

        status(result) mustEqual OK

        val content = contentAsString(result)

        content mustEqual
          view(
            Some("This is a will trust. If the trust does not have a will settlor, you will need to change your answers."),
            Nil,
            completedRows,
            50
          )(request, messages).toString
        content must include("You cannot enter another settlor as you have entered a maximum of 50.")
        content must include("If you have further settlors to add, write to HMRC with their details.")

        application.stop()

      }

      "return correct view when individuals are maxed out" in {

        val settlors = Settlors(
          List.fill(25)(individualSettlor),
          Nil,
          None
        )

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        contentAsString(result) must include("You cannot add another individual as you have entered a maximum of 25.")
        contentAsString(result) must include("If you have further settlors to add within this type, write to HMRC with their details.")

        application.stop()

      }

      "return correct view when businesses are maxed out" in {

        val settlors = Settlors(
          Nil,
          List.fill(25)(businessSettlor),
          None
        )

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        contentAsString(result) must include("You cannot add another business as you have entered a maximum of 25.")
        contentAsString(result) must include("If you have further settlors to add within this type, write to HMRC with their details.")

        application.stop()

      }

      "return OK and the correct view for a GET when there is also a will settlor" in {

        val settlors = Settlors(
          List.fill(25)(individualSettlor),
          List.fill(25)(businessSettlor),
          Some(deceasedSettlor)
        )

        val fakeService = new FakeService(settlors)

        val completedRows = List.fill(51)(fakeAddRow)

        when(mockViewHelper.rows(any(), any(), any())(any())).thenReturn(AddToRows(Nil, completedRows))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MaxedOutSettlorsView]

        status(result) mustEqual OK

        val content = contentAsString(result)

        content mustEqual
          view(
            Some("This is a will trust. If the trust does not have a will settlor, you will need to change your answers."),
            Nil,
            completedRows,
            51
          )(request, messages).toString
        content must include("You cannot enter another settlor as you have entered a maximum of 51.")
        content must include("If you have further settlors to add, write to HMRC with their details.")

        application.stop()

      }

      "redirect to add to page and set settlors to complete when user clicks continue" in {

        val fakeService = new FakeService(settlors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector),
            bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper)
          ).build()

        val request = FakeRequest(POST, submitCompleteRoute)

        when(mockStoreConnector.setTaskComplete(any())(any(), any())).thenReturn(Future.successful(HttpResponse.apply(200, "")))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()

      }

    }
  }
}
