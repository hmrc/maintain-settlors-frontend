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
import models.Constant.MAX
import models.TaskStatus.Completed
import models.settlors.{BusinessSettlor, DeceasedSettlor, IndividualSettlor, Settlors}
import models.{AddASettlor, Name, RemoveSettlor, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
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

class AddASettlorControllerSpec extends SpecBase with ScalaFutures with BeforeAndAfterEach {

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
    changeUrl = "change-url",
    removeUrl = Some("remove-url")
  )

  class FakeService(data: Settlors) extends TrustService {

    override def getSettlors(utr: String)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Settlors] = Future.successful(data)

    override def getIndividualSettlor(utr: String, index: Int)
                                     (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[IndividualSettlor] = ???

    override def getBusinessSettlor(utr: String, index: Int)
                                   (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[BusinessSettlor] = ???

    override def removeSettlor(utr: String, settlor: RemoveSettlor)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = ???

    override def getDeceasedSettlor(utr: String)
                                   (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Option[DeceasedSettlor]] = ???

    override def getBusinessUtrs(identifier: String, index: Option[Int])
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[String]] = ???

    override def getIndividualNinos(identifier: String, index: Option[Int], adding: Boolean)
                                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[String]] = ???
  }

  override def beforeEach(): Unit = {
    reset(playbackRepository, mockStoreConnector)

    when(playbackRepository.set(any())).thenReturn(Future.successful(true))

    when(mockStoreConnector.updateTaskStatus(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse.apply(OK, "")))
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

      "return OK and the correct view for a GET" when {

        def runTest(migrating: Boolean) = {
          val fakeService = new FakeService(settlors)

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(migratingFromNonTaxableToTaxable = migrating)))
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
              Nil,
              migrating
            )(request, messages).toString

          application.stop()
        }

        "migrating" in {
          runTest(true)
        }

        "not migrating" in {
          runTest(false)
        }
      }

      // need to persist deceased answers as user could click back from add-to page to deceased CYA updated-details page
      // this doesn't affect them wanting to add a new business or living individual
      "cleanup individual and business date in user answers but persist deceased data" when {

        "GET" in {

          val fakeService = new FakeService(settlors)

          val userAnswers = emptyUserAnswers
            .set(pages.individual.living.NamePage, Name("Joe", None, "Bloggs")).success.value
            .set(pages.business.NamePage, "Amazon").success.value
            .set(pages.individual.deceased.NamePage, Name("Joe", None, "Bloggs")).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind(classOf[TrustService]).toInstance(fakeService))
            .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
            .build()

          val request = FakeRequest(GET, getRoute)

          val result = route(application, request).value

          status(result) mustEqual OK

          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(playbackRepository).set(uaCaptor.capture)
          uaCaptor.getValue.get(pages.individual.living.NamePage) mustNot be(defined)
          uaCaptor.getValue.get(pages.business.NamePage) mustNot be(defined)
          uaCaptor.getValue.get(pages.individual.deceased.NamePage) must be(defined)

          application.stop()
        }

        "POST" in {

          val fakeService = new FakeService(settlors)

          val userAnswers = emptyUserAnswers
            .set(pages.individual.living.NamePage, Name("Joe", None, "Bloggs")).success.value
            .set(pages.business.NamePage, "Amazon").success.value
            .set(pages.individual.deceased.NamePage, Name("Joe", None, "Bloggs")).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind(classOf[TrustService]).toInstance(fakeService),
              bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector),
              bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper)
            ).build()

          val request = FakeRequest(POST, submitRoute)
            .withFormUrlEncodedBody(("value", AddASettlor.YesNow.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(playbackRepository).set(uaCaptor.capture)
          uaCaptor.getValue.get(pages.individual.living.NamePage) mustNot be(defined)
          uaCaptor.getValue.get(pages.business.NamePage) mustNot be(defined)
          uaCaptor.getValue.get(pages.individual.deceased.NamePage) must be(defined)

          application.stop()
        }
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

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        verify(mockStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

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

        verify(mockStoreConnector, never()).updateTaskStatus(any(), eqTo(Completed))(any(), any())

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" when {

        def runTest(migrating: Boolean) = {
          val fakeService = new FakeService(settlors)

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(migratingFromNonTaxableToTaxable = migrating)))
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
              Nil,
              migrating
            )(request, messages).toString

          application.stop()
        }

        "migrating" in {
          runTest(true)
        }

        "not migrating" in {
          runTest(false)
        }
      }
    }

    "maxed out settlors" when {

      "counting max as combined" must {

        val completedRows = List.fill(MAX)(fakeAddRow)

        "return OK and the correct view for a GET" when {

          def runTest(migrating: Boolean) = {
            val settlors = Settlors(
              List.fill((MAX / 2F).ceil.toInt)(individualSettlor),
              List.fill((MAX / 2F).floor.toInt)(businessSettlor),
              None
            )

            val fakeService = new FakeService(settlors)

            when(mockViewHelper.rows(any(), any(), any())(any())).thenReturn(AddToRows(Nil, completedRows))

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(migratingFromNonTaxableToTaxable = migrating)))
              .overrides(bind(classOf[TrustService]).toInstance(fakeService))
              .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
              .configure("microservice.services.features.count-max-as-combined" -> true)
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
                MAX,
                migrating
              )(request, messages).toString

            content must include("You cannot enter another settlor as you have entered a maximum of 25.")
            content must include("If you have further settlors to add, write to HMRC with their details.")

            application.stop()
          }

          "migrating" in {
            runTest(true)
          }

          "not migrating" in {
            runTest(false)
          }
        }

        "return correct view when individuals are maxed out" when {

          def runTest(migrating: Boolean) = {
            val settlors = Settlors(
              List.fill(MAX)(individualSettlor),
              Nil,
              None
            )

            val fakeService = new FakeService(settlors)

            when(mockViewHelper.rows(any(), any(), any())(any())).thenReturn(AddToRows(Nil, completedRows))

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(migratingFromNonTaxableToTaxable = migrating)))
              .overrides(bind(classOf[TrustService]).toInstance(fakeService))
              .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
              .configure("microservice.services.features.count-max-as-combined" -> true)
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
                MAX,
                migrating
              )(request, messages).toString

            content must include("You cannot enter another settlor as you have entered a maximum of 25.")
            content must include("If you have further settlors to add, write to HMRC with their details.")

            application.stop()
          }

          "migrating" in {
            runTest(true)
          }

          "not migrating" in {
            runTest(false)
          }
        }

        "return correct view when businesses are maxed out" when {

          def runTest(migrating: Boolean) = {
            val settlors = Settlors(
              Nil,
              List.fill(MAX)(businessSettlor),
              None
            )

            val fakeService = new FakeService(settlors)

            when(mockViewHelper.rows(any(), any(), any())(any())).thenReturn(AddToRows(Nil, completedRows))

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(migratingFromNonTaxableToTaxable = migrating)))
              .overrides(bind(classOf[TrustService]).toInstance(fakeService))
              .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
              .configure("microservice.services.features.count-max-as-combined" -> true)
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
                MAX,
                migrating
              )(request, messages).toString

            content must include("You cannot enter another settlor as you have entered a maximum of 25.")
            content must include("If you have further settlors to add, write to HMRC with their details.")

            application.stop()
          }

          "migrating" in {
            runTest(true)
          }

          "not migrating" in {
            runTest(false)
          }
        }

        "return OK and the correct view for a GET when there is also a will settlor" when {

          def runTest(migrating: Boolean) = {
            val settlors = Settlors(
              List.fill((MAX / 2F).ceil.toInt)(individualSettlor),
              List.fill((MAX / 2F).floor.toInt)(businessSettlor),
              Some(deceasedSettlor)
            )

            val fakeService = new FakeService(settlors)

            val completedRows = List.fill(MAX + 1)(fakeAddRow)

            when(mockViewHelper.rows(any(), any(), any())(any())).thenReturn(AddToRows(Nil, completedRows))

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(migratingFromNonTaxableToTaxable = migrating)))
              .overrides(bind(classOf[TrustService]).toInstance(fakeService))
              .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
              .configure("microservice.services.features.count-max-as-combined" -> true)
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
                MAX + 1,
                migrating
              )(request, messages).toString

            content must include("You cannot enter another settlor as you have entered a maximum of 26.")
            content must include("If you have further settlors to add, write to HMRC with their details.")

            application.stop()
          }

          "migrating" in {
            runTest(true)
          }

          "not migrating" in {
            runTest(false)
          }
        }

        "redirect to add to page and set settlors to complete when user clicks continue" in {

          val fakeService = new FakeService(settlors)

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind(classOf[TrustService]).toInstance(fakeService),
              bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector),
              bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper)
            )
            .configure("microservice.services.features.count-max-as-combined" -> true)
            .build()

          val request = FakeRequest(POST, submitCompleteRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

          verify(mockStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

          application.stop()
        }
      }

      "not counting max as combined" must {

        "return OK and the correct view for a GET" when {

          def runTest(migrating: Boolean) = {
            val settlors = Settlors(
              List.fill(MAX)(individualSettlor),
              List.fill(MAX)(businessSettlor),
              None
            )

            val fakeService = new FakeService(settlors)

            val completedRows = List.fill(MAX * 2)(fakeAddRow)

            when(mockViewHelper.rows(any(), any(), any())(any())).thenReturn(AddToRows(Nil, completedRows))

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(migratingFromNonTaxableToTaxable = migrating)))
              .overrides(bind(classOf[TrustService]).toInstance(fakeService))
              .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
              .configure("microservice.services.features.count-max-as-combined" -> false)
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
                MAX * 2,
                migrating
              )(request, messages).toString

            content must include("You cannot enter another settlor as you have entered a maximum of 50.")
            content must include("If you have further settlors to add, write to HMRC with their details.")

            application.stop()
          }

          "migrating" in {
            runTest(true)
          }

          "not migrating" in {
            runTest(false)
          }
        }

        "return correct view when individuals are maxed out" when {

          def runTest(migrating: Boolean) = {
            val settlors = Settlors(
              List.fill(MAX)(individualSettlor),
              Nil,
              None
            )

            val fakeService = new FakeService(settlors)

            val completedRows = List.fill(MAX * 2)(fakeAddRow)

            when(mockViewHelper.rows(any(), any(), any())(any())).thenReturn(AddToRows(Nil, completedRows))

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(migratingFromNonTaxableToTaxable = migrating)))
              .overrides(bind(classOf[TrustService]).toInstance(fakeService))
              .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
              .configure("microservice.services.features.count-max-as-combined" -> false)
              .build()

            val request = FakeRequest(GET, getRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[AddASettlorView]

            status(result) mustEqual OK

            val content = contentAsString(result)

            content mustEqual
              view(
                addTrusteeForm,
                Some("This is a will trust. If the trust does not have a will settlor, you will need to change your answers."),
                Nil,
                completedRows,
                "The trust has 25 settlors",
                List("whatTypeOfSettlor.individual"),
                migrating
              )(request, messages).toString

            content must include("You cannot add another individual as you have entered a maximum of 25.")
            content must include("If you have further settlors to add within this type, write to HMRC with their details.")

            application.stop()
          }

          "migrating" in {
            runTest(true)
          }

          "not migrating" in {
            runTest(false)
          }
        }

        "return correct view when businesses are maxed out" when {

          def runTest(migrating: Boolean) = {
            val settlors = Settlors(
              Nil,
              List.fill(MAX)(businessSettlor),
              None
            )

            val fakeService = new FakeService(settlors)

            val completedRows = List.fill(MAX * 2)(fakeAddRow)

            when(mockViewHelper.rows(any(), any(), any())(any())).thenReturn(AddToRows(Nil, completedRows))

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(migratingFromNonTaxableToTaxable = migrating)))
              .overrides(bind(classOf[TrustService]).toInstance(fakeService))
              .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
              .configure("microservice.services.features.count-max-as-combined" -> false)
              .build()

            val request = FakeRequest(GET, getRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[AddASettlorView]

            status(result) mustEqual OK

            val content = contentAsString(result)

            content mustEqual
              view(
                addTrusteeForm,
                Some("This is a will trust. If the trust does not have a will settlor, you will need to change your answers."),
                Nil,
                completedRows,
                "The trust has 25 settlors",
                List("whatTypeOfSettlor.business"),
                migrating
              )(request, messages).toString

            content must include("You cannot add another business as you have entered a maximum of 25.")
            content must include("If you have further settlors to add within this type, write to HMRC with their details.")

            application.stop()
          }

          "migrating" in {
            runTest(true)
          }

          "not migrating" in {
            runTest(false)
          }
        }

        "return OK and the correct view for a GET when there is also a will settlor" when {

          def runTest(migrating: Boolean) = {
            val settlors = Settlors(
              List.fill(MAX)(individualSettlor),
              List.fill(MAX)(businessSettlor),
              Some(deceasedSettlor)
            )

            val fakeService = new FakeService(settlors)

            val completedRows = List.fill((MAX * 2) + 1)(fakeAddRow)

            when(mockViewHelper.rows(any(), any(), any())(any())).thenReturn(AddToRows(Nil, completedRows))

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(migratingFromNonTaxableToTaxable = migrating)))
              .overrides(bind(classOf[TrustService]).toInstance(fakeService))
              .overrides(bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper))
              .configure("microservice.services.features.count-max-as-combined" -> false)
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
                (MAX * 2) + 1,
                migrating
              )(request, messages).toString

            content must include("You cannot enter another settlor as you have entered a maximum of 51.")
            content must include("If you have further settlors to add, write to HMRC with their details.")

            application.stop()
          }

          "migrating" in {
            runTest(true)
          }

          "not migrating" in {
            runTest(false)
          }
        }

        "redirect to add to page and set settlors to complete when user clicks continue" in {

          val fakeService = new FakeService(settlors)

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind(classOf[TrustService]).toInstance(fakeService),
              bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector),
              bind(classOf[AddASettlorViewHelper]).toInstance(mockViewHelper)
            )
            .configure("microservice.services.features.count-max-as-combined" -> false)
            .build()

          val request = FakeRequest(POST, submitCompleteRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

          verify(mockStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

          application.stop()
        }
      }
    }
  }
}
