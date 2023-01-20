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

package controllers.individual.living.amend

import config.{ErrorHandler, FrontendAppConfig}
import connectors.TrustConnector
import controllers.actions._
import controllers.actions.individual.living.NameRequiredAction
import extractors.IndividualSettlorExtractor
import models.UserAnswers
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.mappers.IndividualSettlorMapper
import utils.print.IndividualSettlorPrintHelper
import viewmodels.AnswerSection
import views.html.individual.living.amend.CheckDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetailsView,
                                        service: TrustService,
                                        connector: TrustConnector,
                                        val appConfig: FrontendAppConfig,
                                        playbackRepository: PlaybackRepository,
                                        printHelper: IndividualSettlorPrintHelper,
                                        mapper: IndividualSettlorMapper,
                                        nameAction: NameRequiredAction,
                                        extractor: IndividualSettlorExtractor,
                                        errorHandler: ErrorHandler
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private def render(userAnswers: UserAnswers,
                     index: Int,
                     name: String)
                    (implicit request: Request[AnyContent]): Result = {
    val section: AnswerSection = printHelper(userAnswers, adding = false, name)
    Ok(view(section, index))
  }

  def extractAndRender(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      service.getIndividualSettlor(request.userAnswers.identifier, index) flatMap {
        settlor =>
          for {
            extractedF <- Future.fromTry(extractor(request.userAnswers, settlor, Some(index)))
            _ <- playbackRepository.set(extractedF)
          } yield {
            render(extractedF, index, settlor.name.displayName)
          }
      }
  }

  def renderFromUserAnswers(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>
      render(request.userAnswers, index, request.settlorName)
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      (for {
        individual <- Future.fromTry(mapper(request.userAnswers))
        _ <- connector.amendIndividualSettlor(request.userAnswers.identifier, index, individual)
      } yield {
        Redirect(controllers.routes.AddASettlorController.onPageLoad())
      }).recover {
        case e =>
          logger.error(e.getMessage)
          InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }
}
