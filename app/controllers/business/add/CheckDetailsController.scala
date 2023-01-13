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

package controllers.business.add

import config.FrontendAppConfig
import connectors.TrustConnector
import controllers.actions._
import controllers.actions.business.NameRequiredAction
import handlers.ErrorHandler
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.mappers.BusinessSettlorMapper
import utils.print.BusinessSettlorPrintHelper
import viewmodels.AnswerSection
import views.html.business.add.CheckDetailsView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CheckDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetailsView,
                                        connector: TrustConnector,
                                        val appConfig: FrontendAppConfig,
                                        printHelper: BusinessSettlorPrintHelper,
                                        mapper: BusinessSettlorMapper,
                                        nameAction: NameRequiredAction,
                                        errorHandler: ErrorHandler
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>

      val section: AnswerSection = printHelper(request.userAnswers, adding = true, request.settlorName)
      Ok(view(section))
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      mapper(request.userAnswers) match {
        case Failure(exception) =>
          logger.error(exception.getMessage)
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
        case Success(settlor) =>
          connector.addBusinessSettlor(request.userAnswers.identifier, settlor).map(_ =>
            Redirect(controllers.routes.AddASettlorController.onPageLoad())
          )
      }
  }
}
