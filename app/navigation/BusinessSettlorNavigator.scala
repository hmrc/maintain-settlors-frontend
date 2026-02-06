/*
 * Copyright 2026 HM Revenue & Customs
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

package navigation

import controllers.business.{routes => rts}
import models.TypeOfTrust.EmployeeRelated
import models.{Mode, NormalMode, TypeOfTrust, UserAnswers}
import pages.Page
import pages.business._
import play.api.mvc.Call

import javax.inject.Inject

class BusinessSettlorNavigator @Inject() () extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode, userAnswers.trustType)(page)(userAnswers)

  override def nextPage(page: Page, userAnswers: UserAnswers): Call =
    nextPage(page, NormalMode, userAnswers)

  private def simpleNavigation(mode: Mode): PartialFunction[Page, Call] = {
    case CompanyTypePage => rts.CompanyTimeController.onPageLoad(mode)
    case StartDatePage   => controllers.business.add.routes.CheckDetailsController.onPageLoad()
  }

  private def conditionalNavigation(
    mode: Mode,
    trustType: Option[TypeOfTrust]
  ): PartialFunction[Page, UserAnswers => Call] = {
    case NamePage                           => ua => navigateAwayFromNamePage(mode, ua)
    case UtrYesNoPage                       =>
      ua =>
        yesNoNav(
          ua,
          UtrYesNoPage,
          rts.UtrController.onPageLoad(mode),
          rts.CountryOfResidenceYesNoController.onPageLoad(mode)
        )
    case UtrPage                            => _ => rts.CountryOfResidenceYesNoController.onPageLoad(mode)
    case CountryOfResidenceYesNoPage        =>
      ua =>
        yesNoNav(
          ua,
          CountryOfResidenceYesNoPage,
          rts.CountryOfResidenceInTheUkYesNoController.onPageLoad(mode),
          navigateAwayFromResidencePages(mode, trustType, ua)
        )
    case CountryOfResidenceInTheUkYesNoPage =>
      ua =>
        yesNoNav(
          ua,
          CountryOfResidenceInTheUkYesNoPage,
          navigateAwayFromResidencePages(mode, trustType, ua),
          rts.CountryOfResidenceController.onPageLoad(mode)
        )
    case CountryOfResidencePage             => ua => navigateAwayFromResidencePages(mode, trustType, ua)
    case AddressYesNoPage                   =>
      ua =>
        yesNoNav(
          ua,
          AddressYesNoPage,
          rts.LiveInTheUkYesNoController.onPageLoad(mode),
          navigateToEndPages(mode, trustType, ua)
        )
    case LiveInTheUkYesNoPage               =>
      ua =>
        yesNoNav(
          ua,
          LiveInTheUkYesNoPage,
          rts.UkAddressController.onPageLoad(mode),
          rts.NonUkAddressController.onPageLoad(mode)
        )
    case UkAddressPage | NonUkAddressPage   => ua => navigateToEndPages(mode, trustType, ua)
    case CompanyTimePage                    => ua => navigateToStartDateOrCheckDetails(mode, ua)
  }

  private def navigateAwayFromNamePage(mode: Mode, answers: UserAnswers): Call =
    if (answers.isTaxable) {
      rts.UtrYesNoController.onPageLoad(mode)
    } else {
      rts.CountryOfResidenceYesNoController.onPageLoad(mode)
    }

  private def navigateAwayFromResidencePages(mode: Mode, trustType: Option[TypeOfTrust], answers: UserAnswers): Call = {
    val isNonTaxable = !answers.isTaxable

    if (isNonTaxable || isUtrDefined(answers)) {
      navigateToEndPages(mode, trustType, answers)
    } else {
      rts.AddressYesNoController.onPageLoad(mode)
    }
  }

  private def navigateToEndPages(mode: Mode, trustType: Option[TypeOfTrust], answers: UserAnswers): Call = {
    val isNonTaxable         = !answers.isTaxable
    val isNotEmployeeRelated = !trustType.contains(EmployeeRelated)

    if (isNonTaxable || isNotEmployeeRelated) {
      navigateToStartDateOrCheckDetails(mode, answers)
    } else {
      rts.CompanyTypeController.onPageLoad(mode)
    }
  }

  private def navigateToStartDateOrCheckDetails(mode: Mode, answers: UserAnswers): Call =
    if (mode == NormalMode) {
      rts.StartDateController.onPageLoad()
    } else {
      checkDetailsRoute(answers)
    }

  private def checkDetailsRoute(answers: UserAnswers): Call =
    answers.get(IndexPage) match {
      case None    => controllers.routes.SessionExpiredController.onPageLoad()
      case Some(x) =>
        controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(x)
    }

  private def routes(mode: Mode, trustType: Option[TypeOfTrust]): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) andThen (c => (_: UserAnswers) => c) orElse
      conditionalNavigation(mode, trustType)

  private def isUtrDefined(answers: UserAnswers): Boolean = answers.get(UtrYesNoPage).getOrElse(false)
}
