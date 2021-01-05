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

package controllers.individual.living.add

import config.annotations.LivingSettlor
import controllers.actions._
import controllers.actions.individual.living.NameRequiredAction
import forms.YesNoFormProvider
import javax.inject.Inject
import models.NormalMode
import navigation.Navigator
import pages.individual.living.IdCardDetailsYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.individual.living.add.IdCardDetailsYesNoView

import scala.concurrent.{ExecutionContext, Future}

class IdCardDetailsYesNoController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              sessionRepository: PlaybackRepository,
                                              @LivingSettlor navigator: Navigator,
                                              standardActionSets: StandardActionSets,
                                              nameAction: NameRequiredAction,
                                              formProvider: YesNoFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: IdCardDetailsYesNoView
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[Boolean] = formProvider.withPrefix("livingSettlor.idCardDetailsYesNo")

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(IdCardDetailsYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.settlorName))
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, request.settlorName))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(IdCardDetailsYesNoPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(IdCardDetailsYesNoPage, NormalMode, updatedAnswers))
      )
  }
}
