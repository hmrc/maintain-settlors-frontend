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

import controllers.individual.deceased.{routes => rts}
import javax.inject.Inject
import models.BpMatchStatus.FullyMatched
import models.{Mode, TypeOfTrust, UserAnswers}
import pages.individual.deceased._
import pages.{AdditionalSettlorsYesNoPage, Page}
import play.api.mvc.Call

class DeceasedSettlorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, userAnswers: UserAnswers): Call =
    routes(page)(userAnswers)

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(page)(userAnswers)

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, trustType: Option[TypeOfTrust]): Call = {
    routes(page)(userAnswers)
  }

  private val simpleNavigation: PartialFunction[Page, UserAnswers => Call] = {
    case NamePage => _ => rts.DateOfDeathYesNoController.onPageLoad()
    case DateOfDeathPage => ua => matchStatusNav(ua, rts.DateOfBirthYesNoController.onPageLoad())
    case DateOfBirthPage => ua => redirectToNinoQuestionsIfTaxable(ua)
    case NationalInsuranceNumberPage | UkAddressPage | NonUkAddressPage => ua => additionalSettlorsNav(ua)
    case AdditionalSettlorsYesNoPage => _ => rts.CheckDetailsController.renderFromUserAnswers()
  }

  private val yesNoNavigation: PartialFunction[Page, UserAnswers => Call] = {
    case DateOfDeathYesNoPage => ua =>
      yesNoNav(ua, DateOfDeathYesNoPage, rts.DateOfDeathController.onPageLoad(), matchStatusNav(ua, rts.DateOfBirthYesNoController.onPageLoad()))
    case DateOfBirthYesNoPage => ua =>
      yesNoNav(ua, DateOfBirthYesNoPage, rts.DateOfBirthController.onPageLoad(), redirectToNinoQuestionsIfTaxable(ua))
    case NationalInsuranceNumberYesNoPage => ua =>
      yesNoNav(ua, NationalInsuranceNumberYesNoPage, rts.NationalInsuranceNumberController.onPageLoad(), rts.AddressYesNoController.onPageLoad())
    case AddressYesNoPage => ua =>
      yesNoNav(ua, AddressYesNoPage, rts.LivedInTheUkYesNoController.onPageLoad(), additionalSettlorsNav(ua))
    case LivedInTheUkYesNoPage => ua =>
      yesNoNav(ua, LivedInTheUkYesNoPage, rts.UkAddressController.onPageLoad(), rts.NonUkAddressController.onPageLoad())
  }

  private def redirectToNinoQuestionsIfTaxable(ua: UserAnswers): Call = {
    if (ua.isTaxable) {
      rts.NationalInsuranceNumberYesNoController.onPageLoad()
    } else {
      additionalSettlorsNav(ua)
    }
  }

  private def matchStatusNav(ua: UserAnswers, nextCall: Call): Call = {
    ua.get(BpMatchStatusPage) match {
      case Some(FullyMatched) =>
        additionalSettlorsNav(ua)
      case Some(_) =>
        nextCall
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def additionalSettlorsNav(ua: UserAnswers): Call = {
    ua.get(AdditionalSettlorsYesNoPage) match {
      case Some(_) =>
        rts.AdditionalSettlorsYesNoController.onPageLoad()
      case _ =>
        rts.CheckDetailsController.renderFromUserAnswers()
    }
  }

  val routes: PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation orElse
      yesNoNavigation

}

