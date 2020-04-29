/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.actions._
import forms.AddSettlorTypeFormProvider
import javax.inject.Inject
import models.NormalMode
import models.settlors.TypeOfSettlorToAdd
import models.settlors.TypeOfSettlorToAdd.Individual
import pages.AddNowPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.AddNowView

import scala.concurrent.{ExecutionContext, Future}

class AddNowController @Inject()(
                                  override val messagesApi: MessagesApi,
                                  standardActionSets: StandardActionSets,
                                  val controllerComponents: MessagesControllerComponents,
                                  view: AddNowView,
                                  formProvider: AddSettlorTypeFormProvider,
                                  repository: PlaybackRepository,
                                  trustService: TrustService
                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[TypeOfSettlorToAdd] = formProvider()

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trustService.getSettlors(request.userAnswers.utr).map {
        settlors =>
          val preparedForm = request.userAnswers.get(AddNowPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, settlors.nonMaxedOutOptions))

      }
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trustService.getSettlors(request.userAnswers.utr).flatMap {
        settlors =>

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, settlors.nonMaxedOutOptions))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddNowPage, value))
                _ <- repository.set(updatedAnswers)
              } yield {
                value match {
                  case Individual => Redirect(controllers.individual.living.routes.NameController.onPageLoad(NormalMode))
                  case _ => Redirect(controllers.routes.FeatureNotAvailableController.onPageLoad())
                }
              }
          )
      }
  }
}