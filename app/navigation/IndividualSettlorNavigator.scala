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

package navigation

import controllers.individual.living.add.{routes => addRts}
import controllers.individual.living.amend.{routes => amendRts}
import controllers.individual.living.{routes => rts}
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.individual.living._
import play.api.mvc.Call

import javax.inject.Inject

class IndividualSettlorNavigator @Inject() () extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  override def nextPage(page: Page, userAnswers: UserAnswers): Call =
    nextPage(page, NormalMode, userAnswers)

  private def simpleNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case NamePage                                => _ => rts.DateOfBirthYesNoController.onPageLoad(mode)
    case DateOfBirthPage                         => _ => rts.CountryOfNationalityYesNoController.onPageLoad(mode)
    case CountryOfNationalityPage                => ua => navigateAwayFromCountryOfNationalityQuestions(mode, ua.isTaxable)
    case NationalInsuranceNumberPage             => _ => rts.CountryOfResidenceYesNoController.onPageLoad(mode)
    case CountryOfResidencePage                  => ua => navigateAwayFromCountryOfResidenceQuestions(mode, ua)
    case UkAddressPage | NonUkAddressPage        => ua => navigateToPassportDetails(mode, ua)
    case PassportDetailsPage | IdCardDetailsPage => ua => navigateAwayFromPassportIdCardCombined(mode)
    case PassportOrIdCardDetailsPage             => ua => navigateAwayFromPassportIdCardCombined(mode)
    case StartDatePage                           => _ => addRts.CheckDetailsController.onPageLoad()
    case MentalCapacityYesNoPage                 => ua => navigateToStartDateOrCheckDetails(mode, ua)
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case DateOfBirthYesNoPage             =>
      ua =>
        yesNoNav(
          ua,
          DateOfBirthYesNoPage,
          rts.DateOfBirthController.onPageLoad(mode),
          rts.CountryOfNationalityYesNoController.onPageLoad(mode)
        )
    case CountryOfNationalityYesNoPage    =>
      ua =>
        yesNoNav(
          ua,
          CountryOfNationalityYesNoPage,
          rts.CountryOfNationalityUkYesNoController.onPageLoad(mode),
          navigateAwayFromCountryOfNationalityQuestions(mode, ua.isTaxable)
        )
    case CountryOfNationalityUkYesNoPage  =>
      ua =>
        yesNoNav(
          ua,
          CountryOfNationalityUkYesNoPage,
          navigateAwayFromCountryOfNationalityQuestions(mode, ua.isTaxable),
          rts.CountryOfNationalityController.onPageLoad(mode)
        )
    case NationalInsuranceNumberYesNoPage =>
      ua =>
        yesNoNav(
          ua,
          NationalInsuranceNumberYesNoPage,
          rts.NationalInsuranceNumberController.onPageLoad(mode),
          rts.CountryOfResidenceYesNoController.onPageLoad(mode)
        )
    case CountryOfResidenceYesNoPage      =>
      ua =>
        yesNoNav(
          ua,
          CountryOfResidenceYesNoPage,
          rts.CountryOfResidenceUkYesNoController.onPageLoad(mode),
          navigateAwayFromCountryOfResidenceQuestions(mode, ua)
        )
    case CountryOfResidenceUkYesNoPage    =>
      ua =>
        yesNoNav(
          ua,
          CountryOfResidenceUkYesNoPage,
          navigateAwayFromCountryOfResidenceQuestions(mode, ua),
          rts.CountryOfResidenceController.onPageLoad(mode)
        )
    case LiveInTheUkYesNoPage             =>
      ua =>
        yesNoNav(
          ua,
          LiveInTheUkYesNoPage,
          rts.UkAddressController.onPageLoad(mode),
          rts.NonUkAddressController.onPageLoad(mode)
        )
    case AddressYesNoPage                 =>
      ua =>
        yesNoNav(
          ua,
          AddressYesNoPage,
          rts.LiveInTheUkYesNoController.onPageLoad(mode),
          rts.MentalCapacityYesNoController.onPageLoad(mode)
        )
    case PassportDetailsYesNoPage         =>
      ua =>
        yesNoNav(
          ua,
          PassportDetailsYesNoPage,
          rts.PassportDetailsController.onPageLoad(mode),
          rts.IdCardDetailsYesNoController.onPageLoad(mode)
        )
    case IdCardDetailsYesNoPage           =>
      ua =>
        yesNoNav(
          ua,
          IdCardDetailsYesNoPage,
          rts.IdCardDetailsController.onPageLoad(mode),
          rts.MentalCapacityYesNoController.onPageLoad(mode)
        )
    case PassportOrIdCardDetailsYesNoPage =>
      ua =>
        if (mode == NormalMode) {
          yesNoNav(
            ua,
            PassportOrIdCardDetailsYesNoPage,
            rts.PassportOrIdCardDetailsController.onPageLoad(mode),
            rts.MentalCapacityYesNoController.onPageLoad(mode)
          )
        } else {
          rts.MentalCapacityYesNoController.onPageLoad(mode)
        }
  }

  private def navigateToPassportDetails(mode: Mode, answers: UserAnswers): Call =
    if (answers.get(PassportOrIdCardDetailsYesNoPage).isDefined || answers.get(PassportOrIdCardDetailsPage).isDefined) {
      if (mode == NormalMode) {
        rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode)
      } else {
        rts.MentalCapacityYesNoController.onPageLoad(mode)
      }
    } else {
      rts.PassportDetailsYesNoController.onPageLoad(mode)
    }

  private def navigateAwayFromPassportIdCardCombined(mode: Mode): Call =
    rts.MentalCapacityYesNoController.onPageLoad(mode)

  private def navigateAwayFromCountryOfNationalityQuestions(mode: Mode, isTaxable: Boolean): Call =
    if (isTaxable) {
      rts.NationalInsuranceNumberYesNoController.onPageLoad(mode)
    } else {
      rts.CountryOfResidenceYesNoController.onPageLoad(mode)
    }

  private def navigateAwayFromCountryOfResidenceQuestions(mode: Mode, ua: UserAnswers): Call =
    if (isNinoDefined(ua) || !ua.isTaxable) {
      rts.MentalCapacityYesNoController.onPageLoad(mode)
    } else {
      rts.AddressYesNoController.onPageLoad(mode)
    }

  private def isNinoDefined(ua: UserAnswers): Boolean =
    ua.get(NationalInsuranceNumberYesNoPage).getOrElse(false)

  private def navigateToStartDateOrCheckDetails(mode: Mode, answers: UserAnswers): Call =
    if (mode == NormalMode) {
      addRts.StartDateController.onPageLoad()
    } else {
      checkDetailsRoute(answers)
    }

  private def checkDetailsRoute(answers: UserAnswers): Call =
    answers.get(IndexPage) match {
      case None    =>
        controllers.routes.SessionExpiredController.onPageLoad()
      case Some(x) =>
        amendRts.CheckDetailsController.renderFromUserAnswers(x)
    }

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) orElse
      yesNoNavigation(mode)

}
