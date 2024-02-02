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

import java.time.LocalDate
import config.FrontendAppConfig
import config.annotations.DeceasedSettlor
import connectors.TrustConnector
import controllers.actions.{SettlorNameRequest, StandardActionSets}
import controllers.actions.individual.deceased.NameRequiredAction
import forms.DateOfDeathFormProvider

import javax.inject.Inject
import navigation.Navigator
import pages.individual.deceased.{DateOfBirthPage, DateOfDeathPage, DateOfDeathYesNoPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.individual.deceased.DateOfDeathView

import scala.concurrent.{ExecutionContext, Future}

class DateOfDeathController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: PlaybackRepository,
                                       @DeceasedSettlor navigator: Navigator,
                                       standardActionSets: StandardActionSets,
                                       nameAction: NameRequiredAction,
                                       formProvider: DateOfDeathFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: DateOfDeathView,
                                       trustsConnector : TrustConnector,
                                       appConfig: FrontendAppConfig
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(trustStartDate: LocalDate, minDate: (LocalDate, String)) =
    formProvider.withConfig("deceasedSettlor.dateOfDeath", trustStartDate, minDate)

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction).async {
    implicit request =>

      trustsConnector.getTrustDetails(request.userAnswers.identifier) map { details =>
          val preparedForm = request.userAnswers.get(DateOfDeathPage) match {
            case None => form(details.startDate, minDate)
            case Some(value) => form(details.startDate, minDate).fill(value)
          }

          Ok(view(preparedForm, request.settlorName))
      }
  }

  def onSubmit(): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction).async {
    implicit request =>

      trustsConnector.getTrustDetails(request.userAnswers.identifier) flatMap { details =>
        form(details.startDate, minDate).bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, request.settlorName))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(
                request.userAnswers.set(DateOfDeathPage, value)
                  .flatMap(_.set(DateOfDeathYesNoPage, true))
              )
              _ <- sessionRepository.set(updatedAnswers)
            } yield {
              Redirect(navigator.nextPage(DateOfDeathPage, updatedAnswers))
            }
        )
      }
  }

  private def minDate(implicit request: SettlorNameRequest[AnyContent]): (LocalDate, String) = {
    request.userAnswers.get(DateOfBirthPage) match {
      case Some(dateOfBirth) =>
        (dateOfBirth, "beforeDateOfBirth")
      case None =>
        (appConfig.minDate, "past")
    }
  }
}
