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

package controllers.business.remove

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
import views.html.business.remove.RemoveBusinessSettlorView

import scala.concurrent.{ExecutionContext, Future}

class RemoveBusinessSettlorController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    standardActionSets: StandardActionSets,
                                                    trustService: TrustService,
                                                    formProvider: RemoveIndexFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: RemoveBusinessSettlorView,
                                                    errorHandler: ErrorHandler
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val messagesPrefix: String = "removeBusinessSettlorYesNo"

  private val form = formProvider.apply(messagesPrefix)

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      trustService.getBusinessSettlor(request.userAnswers.identifier, index).map {
        settlor =>
          if (settlor.provisional) {
            Ok(view(form, index, settlor.name))
          } else {
            Redirect(controllers.routes.AddASettlorController.onPageLoad().url)
          }
      } recoverWith {
        case iobe: IndexOutOfBoundsException =>
          logger.warn(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" error getting business settlor $index from trusts service ${iobe.getMessage}: IndexOutOfBoundsException")

          Future.successful(Redirect(controllers.routes.AddASettlorController.onPageLoad().url))
        case e =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" error getting business settlor $index from trusts service ${e.getMessage}")

          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
      }

  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          trustService.getBusinessSettlor(request.userAnswers.identifier, index).map {
            settlor =>
              BadRequest(view(formWithErrors, index, settlor.name))
          }
        },
        value => {

          if (value) {

            trustService.removeSettlor(request.userAnswers.identifier, RemoveSettlor(SettlorType.BusinessSettlor, index)).map(_ =>
              Redirect(controllers.routes.AddASettlorController.onPageLoad())
            )
          } else {
            Future.successful(Redirect(controllers.routes.AddASettlorController.onPageLoad().url))
          }
        }
      )
  }
}
