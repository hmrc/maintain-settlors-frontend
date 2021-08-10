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

package controllers.individual.deceased

import config.{ErrorHandler, FrontendAppConfig}
import connectors.{TrustConnector, TrustStoreConnector}
import controllers.actions._
import controllers.actions.individual.deceased.NameRequiredAction
import extractors.DeceasedSettlorExtractor
import models.BpMatchStatus.FullyMatched
import models.UserAnswers
import models.settlors.Settlors
import pages.AdditionalSettlorsYesNoPage
import pages.individual.deceased.BpMatchStatusPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.mappers.DeceasedSettlorMapper
import utils.print.DeceasedSettlorPrintHelper
import viewmodels.AnswerSection
import views.html.individual.deceased.CheckDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetailsView,
                                        service: TrustService,
                                        connector: TrustConnector,
                                        trustStoreConnector: TrustStoreConnector,
                                        val appConfig: FrontendAppConfig,
                                        playbackRepository: PlaybackRepository,
                                        printHelper: DeceasedSettlorPrintHelper,
                                        mapper: DeceasedSettlorMapper,
                                        nameAction: NameRequiredAction,
                                        extractor: DeceasedSettlorExtractor,
                                        errorHandler: ErrorHandler
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private def render(userAnswers: UserAnswers,
                     name: String,
                     hasAdditionalSettlors: Boolean)(implicit request: Request[AnyContent]): Result = {
    val section: AnswerSection = printHelper(userAnswers, name, hasAdditionalSettlors)
    Ok(view(
      section,
      name,
      userAnswers.get(BpMatchStatusPage) match {
        case Some(FullyMatched) => true
        case _ => false
      },
      userAnswers.isDateOfDeathRecorded
    ))
  }

  def extractAndRender(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      service.getSettlors(request.userAnswers.identifier) flatMap {
        case Settlors(individuals, businesses, Some(deceased)) =>
          for {
            hasAdditionalSettlors <- Future.successful(individuals.nonEmpty || businesses.nonEmpty)
            extractedF <- Future.fromTry(extractor(request.userAnswers, deceased, None, Some(hasAdditionalSettlors)))
            _ <- playbackRepository.set(extractedF)
          } yield {
            render(extractedF, deceased.name.displayName, hasAdditionalSettlors)
          }
        case Settlors(_, _, None) =>
          throw new Exception("Deceased Settlor Information not found")

      }
  }

  def renderFromUserAnswers(): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction).async {
    implicit request =>
      service.getSettlors(request.userAnswers.identifier).flatMap { settlors =>
        Future.successful(render(
          request.userAnswers,
          request.settlorName,
          settlors.hasLivingSettlors
        ))
      }
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      (for {
        deceasedSettlor <- Future.fromTry(mapper(request.userAnswers))
        _ <- connector.amendDeceasedSettlor(request.userAnswers.identifier, deceasedSettlor)
        settlors <- service.getSettlors(request.userAnswers.identifier)
        isTaskComplete = !settlors.hasLivingSettlors && request.userAnswers.get(AdditionalSettlorsYesNoPage).contains(false)
        _ <- if (isTaskComplete) {
          trustStoreConnector.setTaskComplete(request.userAnswers.identifier).map(_ => ())
        } else {
          Future.successful(())
        }
      } yield {
        if (isTaskComplete) {
          Redirect(appConfig.maintainATrustOverview)
        } else {
          Redirect(controllers.routes.AddASettlorController.onPageLoad())
        }
      }).recover {
        case e =>
          logger.error(e.getMessage)
          InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }
}
