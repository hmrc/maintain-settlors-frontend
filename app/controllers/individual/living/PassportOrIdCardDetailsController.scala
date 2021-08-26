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

import config.annotations.LivingSettlor
import controllers.actions._
import controllers.actions.individual.living.NameRequiredAction
import forms.CombinedPassportOrIdCardDetailsFormProvider
import models.DetailsType._
import models.{CombinedPassportOrIdCard, Mode}
import navigation.Navigator
import pages.individual.living.PassportOrIdCardDetailsPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import views.html.individual.living.PassportOrIdCardDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PassportOrIdCardDetailsController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   playbackRepository: PlaybackRepository,
                                                   @LivingSettlor navigator: Navigator,
                                                   standardActionSets: StandardActionSets,
                                                   nameAction: NameRequiredAction,
                                                   formProvider: CombinedPassportOrIdCardDetailsFormProvider,
                                                   countryOptions: CountryOptions,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: PassportOrIdCardDetailsView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[CombinedPassportOrIdCard] = formProvider.withPrefix("livingSettlor.passportOrIdCardDetails")

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PassportOrIdCardDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, request.settlorName, countryOptions.options))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, request.settlorName, countryOptions.options))),

        newAnswer =>
          for {
            oldAnswer <- Future.successful(request.userAnswers.get(PassportOrIdCardDetailsPage))
            detailsType = {
              oldAnswer match {
                case Some(value) if value == newAnswer && !value.detailsType.isProvisional => Combined
                case _ => CombinedProvisional
              }
            }
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PassportOrIdCardDetailsPage, newAnswer.copy(detailsType = detailsType)))
            _ <- playbackRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PassportOrIdCardDetailsPage, mode, updatedAnswers))
      )
  }
}
