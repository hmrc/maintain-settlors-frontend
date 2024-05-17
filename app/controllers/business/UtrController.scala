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

package controllers.business

import config.annotations.BusinessSettlor
import controllers.actions.business.NameRequiredAction
import controllers.actions.{SettlorNameRequest, StandardActionSets}
import forms.UtrFormProvider
import models.Mode
import navigation.Navigator
import pages.business.{IndexPage, UtrPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.business.UtrView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UtrController @Inject()(
                               val controllerComponents: MessagesControllerComponents,
                               standardActionSets: StandardActionSets,
                               nameAction: NameRequiredAction,
                               formProvider: UtrFormProvider,
                               playbackRepository: PlaybackRepository,
                               view: UtrView,
                               @BusinessSettlor navigator: Navigator,
                               trustsService: TrustService
                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(utrs: List[String])(implicit request: SettlorNameRequest[AnyContent]): Form[String] =
    formProvider.apply("businessSettlor.utr", request.userAnswers.identifier, utrs)

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction).async {
    implicit request =>

      trustsService.getBusinessUtrs(request.userAnswers.identifier, request.userAnswers.get(IndexPage)) map { utrs =>
        val preparedForm = request.userAnswers.get(UtrPage) match {
          case None => form(utrs)
          case Some(value) => form(utrs).fill(value)
        }

        Ok(view(preparedForm, request.settlorName, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction).async {
    implicit request =>

      trustsService.getBusinessUtrs(request.userAnswers.identifier, request.userAnswers.get(IndexPage)) flatMap { utrs =>
        form(utrs).bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(view(formWithErrors, request.settlorName, mode))),
          value => {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(UtrPage, value))
              _ <- playbackRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(UtrPage, mode, updatedAnswers))
          }
        )
      }
  }
}
