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

package navigation

import controllers.individual.living.add.{routes => addRts}
import controllers.individual.living.amend.{routes => amendRts}
import controllers.individual.living.{routes => rts}
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.individual.living._
import play.api.mvc.Call

import javax.inject.Inject

class IndividualSettlorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  override def nextPage(page: Page, userAnswers: UserAnswers): Call =
    nextPage(page, NormalMode, userAnswers)

  private def simpleNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case NamePage => _ => rts.DateOfBirthYesNoController.onPageLoad(mode)
    case DateOfBirthPage => ua => navigateAwayFromDateOfBirthQuestions(ua.is5mldEnabled, mode)
    case CountryOfNationalityPage => ua => navigateAwayFromCountryOfNationalityQuestions(mode, ua.isTaxable)
    case NationalInsuranceNumberPage => ua => navigateAwayFromNinoPages(mode, ua)
    case CountryOfResidencePage => ua => navigateAwayFromCountryOfResidenceQuestions(mode, ua)
    case UkAddressPage | NonUkAddressPage => ua => navigateToPassportDetails(mode, ua)
    case PassportDetailsPage | IdCardDetailsPage => ua => navigateToMentalCapacity(mode, ua)
    case PassportOrIdCardDetailsPage => ua => navigateToMentalCapacity(mode, ua)
    case StartDatePage => _ => addRts.CheckDetailsController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case DateOfBirthYesNoPage => ua =>
      yesNoNav(ua, DateOfBirthYesNoPage, rts.DateOfBirthController.onPageLoad(mode), navigateAwayFromDateOfBirthQuestions(ua.is5mldEnabled, mode))
    case CountryOfNationalityYesNoPage => ua =>
      yesNoNav(ua, CountryOfNationalityYesNoPage, rts.CountryOfNationalityUkYesNoController.onPageLoad(mode), navigateAwayFromCountryOfNationalityQuestions(mode, ua.isTaxable))
    case CountryOfNationalityUkYesNoPage => ua =>
      yesNoNav(ua, CountryOfNationalityUkYesNoPage, navigateAwayFromCountryOfNationalityQuestions(mode, ua.isTaxable), rts.CountryOfNationalityController.onPageLoad(mode))
    case NationalInsuranceNumberYesNoPage => ua =>
      yesNoNav(ua, NationalInsuranceNumberYesNoPage, rts.NationalInsuranceNumberController.onPageLoad(mode), navigateAwayFromNinoPages(mode, ua))
    case CountryOfResidenceYesNoPage => ua =>
      yesNoNav(ua, CountryOfResidenceYesNoPage, rts.CountryOfResidenceUkYesNoController.onPageLoad(mode), navigateAwayFromCountryOfResidenceQuestions(mode, ua))
    case CountryOfResidenceUkYesNoPage => ua =>
      yesNoNav(ua, CountryOfResidenceUkYesNoPage, navigateAwayFromCountryOfResidenceQuestions(mode, ua), rts.CountryOfResidenceController.onPageLoad(mode))
    case LiveInTheUkYesNoPage => ua =>
      yesNoNav(ua, LiveInTheUkYesNoPage, rts.UkAddressController.onPageLoad(mode), rts.NonUkAddressController.onPageLoad(mode))
    case AddressYesNoPage => ua =>
      yesNoNav(ua, AddressYesNoPage, rts.LiveInTheUkYesNoController.onPageLoad(mode), navigateToMentalCapacity(mode, ua))
    case PassportDetailsYesNoPage => ua =>
      yesNoNav(ua, PassportDetailsYesNoPage, rts.PassportDetailsController.onPageLoad(mode), rts.IdCardDetailsYesNoController.onPageLoad(mode))
    case IdCardDetailsYesNoPage => ua =>
      yesNoNav(ua, IdCardDetailsYesNoPage, rts.IdCardDetailsController.onPageLoad(mode), navigateToMentalCapacity(mode, ua))
    case PassportOrIdCardDetailsYesNoPage => ua =>
      yesNoNav(ua, PassportOrIdCardDetailsYesNoPage, rts.PassportOrIdCardDetailsController.onPageLoad(mode), navigateToMentalCapacity(mode, ua))
    case MentalCapacityYesNoPage => ua =>
      yesNoNav(ua, MentalCapacityYesNoPage, navigateToStartDateOrCheckDetails(mode, ua), navigateToStartDateOrCheckDetails(mode, ua))
  }

  private def navigateToPassportDetails(mode: Mode, answers: UserAnswers): Call = {
    if (answers.get(PassportOrIdCardDetailsPage).isDefined) {
      rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode)
    } else {
      rts.PassportDetailsYesNoController.onPageLoad(mode)
    }
  }

  private def navigateAwayFromDateOfBirthQuestions(is5mldEnabled: Boolean, mode: Mode): Call = {
    if (is5mldEnabled) {
      rts.CountryOfNationalityYesNoController.onPageLoad(mode)
    } else {
      rts.NationalInsuranceNumberYesNoController.onPageLoad(mode)
    }
  }

  private def navigateAwayFromNinoPages(mode: Mode, answers: UserAnswers): Call = {
    (answers.is5mldEnabled, isNinoDefined(answers)) match {
      case (true, _) => rts.CountryOfResidenceYesNoController.onPageLoad(mode)
      case (false, true) => navigateToStartDateOrCheckDetails(mode, answers)
      case (false, _) => rts.AddressYesNoController.onPageLoad(mode)
    }
  }

  private def navigateAwayFromCountryOfNationalityQuestions(mode: Mode, isTaxable: Boolean): Call = {
    if (isTaxable) {
      rts.NationalInsuranceNumberYesNoController.onPageLoad(mode)
    } else {
      rts.CountryOfResidenceYesNoController.onPageLoad(mode)
    }
  }

  private def navigateAwayFromCountryOfResidenceQuestions(mode: Mode, ua: UserAnswers): Call = {
    if (isNinoDefined(ua) || !ua.isTaxable) {
      rts.MentalCapacityYesNoController.onPageLoad(mode)
    } else {
      rts.AddressYesNoController.onPageLoad(mode)
    }
  }

  private def navigateToMentalCapacity(mode: Mode, ua: UserAnswers): Call = {
    if (ua.is5mldEnabled) {
      rts.MentalCapacityYesNoController.onPageLoad(mode)
    } else {
      navigateToStartDateOrCheckDetails(mode, ua)
    }
  }

  private def isNinoDefined(ua: UserAnswers): Boolean = {
    ua.get(NationalInsuranceNumberYesNoPage).getOrElse(false)
  }

  private def navigateToStartDateOrCheckDetails(mode: Mode, answers: UserAnswers): Call = {
    if (mode == NormalMode) {
      addRts.StartDateController.onPageLoad()
    } else {
      checkDetailsRoute(answers)
    }
  }

  private def checkDetailsRoute(answers: UserAnswers): Call = {
    answers.get(IndexPage) match {
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
      case Some(x) =>
        amendRts.CheckDetailsController.renderFromUserAnswers(x)
    }
  }

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) orElse
      yesNoNavigation(mode)

}
