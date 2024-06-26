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

package controllers.individual.deceased

import java.time.LocalDate

import config.annotations.DeceasedSettlor
import controllers.actions.individual.deceased.NameRequiredAction
import controllers.actions.{SettlorNameRequest, StandardActionSets}
import forms.DateOfBirthFormProvider
import javax.inject.Inject
import navigation.Navigator
import pages.individual.deceased.{DateOfBirthPage, DateOfDeathPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.individual.deceased.DateOfBirthView

import scala.concurrent.{ExecutionContext, Future}

class DateOfBirthController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: PlaybackRepository,
                                       @DeceasedSettlor navigator: Navigator,
                                       standardActionSets: StandardActionSets,
                                       nameAction: NameRequiredAction,
                                       formProvider: DateOfBirthFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: DateOfBirthView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(maxDate: (LocalDate, String)): Form[LocalDate] =
    formProvider.withConfig("deceasedSettlor.dateOfBirth", maxDate)

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(DateOfBirthPage) match {
        case None => form(maxDate)
        case Some(value) => form(maxDate).fill(value)
      }

      Ok(view(preparedForm, request.settlorName))
  }

  def onSubmit(): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction).async {
    implicit request =>

      form(maxDate).bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, request.settlorName))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(DateOfBirthPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(DateOfBirthPage, updatedAnswers))
      )
  }

  private def maxDate(implicit request: SettlorNameRequest[AnyContent]): (LocalDate, String) = {
    request.userAnswers.get(DateOfDeathPage) match {
      case Some(dateOfDeath) =>
        (dateOfDeath, "afterDateOfDeath")
      case None =>
        (LocalDate.now, "future")
    }
  }
}
