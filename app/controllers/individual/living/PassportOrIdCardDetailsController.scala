/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.actions._
import controllers.actions.individual.living.NameRequiredAction
import models.Mode
import pages.individual.living.IndexPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PassportOrIdCardDetailsController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   standardActionSets: StandardActionSets,
                                                   nameAction: NameRequiredAction,
                                                   val controllerComponents: MessagesControllerComponents
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def route()(implicit request: SettlorNameRequest[AnyContent]) =
    request.userAnswers.get(IndexPage) match {
      case Some(index) =>
        Redirect(amend.routes.CheckDetailsController.renderFromUserAnswers(index))
      case None =>
        Redirect(controllers.routes.SessionExpiredController.onPageLoad())
    }

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction) {
    implicit request =>
      route()
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction) {
    implicit request =>
      route()
  }
}
