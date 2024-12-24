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

package controllers.individual.living.remove

import controllers.actions.StandardActionSets
import forms.RemoveIndexFormProvider
import handlers.ErrorHandler
import javax.inject.Inject
import models.{RemoveSettlor, SettlorType}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.individual.living.remove.RemoveIndividualSettlorView

import scala.concurrent.{ExecutionContext, Future}

class RemoveIndividualSettlorController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    standardActionSets: StandardActionSets,
                                                    trustService: TrustService,
                                                    formProvider: RemoveIndexFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: RemoveIndividualSettlorView,
                                                    errorHandler: ErrorHandler
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val messagesPrefix: String = "removeIndividualSettlorYesNo"

  private val form = formProvider.apply(messagesPrefix)

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      trustService.getIndividualSettlor(request.userAnswers.identifier, index).map {
        settlor =>
          if (settlor.provisional) {
            Ok(view(form, index, settlor.name.displayName))
          } else {
            Redirect(controllers.routes.AddASettlorController.onPageLoad().url)
          }
      } recoverWith {
        case iobe: IndexOutOfBoundsException =>
          logger.warn(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" error getting individual settlor $index from trusts service ${iobe.getMessage}: IndexOutOfBoundsException")

          Future.successful(Redirect(controllers.routes.AddASettlorController.onPageLoad().url))
        case e =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" error getting individual settlor $index from trusts service ${e.getMessage}")

          errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
      }

  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          trustService.getIndividualSettlor(request.userAnswers.identifier, index).map {
            settlor =>
              BadRequest(view(formWithErrors, index, settlor.name.displayName))
          }
        },
        value => {

          if (value) {

            trustService.removeSettlor(request.userAnswers.identifier, RemoveSettlor(SettlorType.IndividualSettlor, index)).map(_ =>
              Redirect(controllers.routes.AddASettlorController.onPageLoad())
            )
          } else {
            Future.successful(Redirect(controllers.routes.AddASettlorController.onPageLoad().url))
          }
        }
      )
  }
}
