/*
 * Copyright 2020 HM Revenue & Customs
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

package utils.print

import com.google.inject.Inject
import models.UserAnswers
import pages.individual.deceased._
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels.AnswerSection

class DeceasedSettlor01PrintHelper @Inject()(answerRowConverter: AnswerRowConverter,
                                             countryOptions: CountryOptions
                                            ) {

  def apply(userAnswers: UserAnswers, settlorName: String, isDateOfDeathRecorded: Boolean)(implicit messages: Messages) = {

    val bound = answerRowConverter.bind(userAnswers, settlorName, countryOptions)

    AnswerSection(
      None,
      Seq(
        bound.nameQuestion(NamePage, "deceasedSettlor.name", None),
        bound.yesNoQuestion(DateOfDeathYesNoPage, "deceasedSettlor.dateOfDeathYesNo", if (isDateOfDeathRecorded) None else Some(controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad().url)),
        bound.dateQuestion(DateOfDeathPage, "deceasedSettlor.dateOfDeath", if (isDateOfDeathRecorded) None else Some(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad().url)),
        bound.yesNoQuestion(DateOfBirthYesNoPage, "deceasedSettlor.dateOfBirthYesNo", None),
        bound.dateQuestion(DateOfBirthPage, "deceasedSettlor.dateOfBirth", None),
        bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "deceasedSettlor.nationalInsuranceNumberYesNo", None),
        bound.ninoQuestion(NationalInsuranceNumberPage, "deceasedSettlor.nationalInsuranceNumber", None),
        bound.yesNoQuestion(AddressYesNoPage, "deceasedSettlor.addressYesNo", None),
        bound.yesNoQuestion(LivedInTheUkYesNoPage, "deceasedSettlor.liveInTheUkYesNo", None),
        bound.addressQuestion(UkAddressPage, "deceasedSettlor.ukAddress", None),
        bound.addressQuestion(NonUkAddressPage, "deceasedSettlor.nonUkAddress", None)
      ).flatten
    )
  }
}
