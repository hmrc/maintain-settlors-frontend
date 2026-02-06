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

package utils.print

import com.google.inject.Inject
import models.BpMatchStatus.FullyMatched
import models.UserAnswers
import pages.AdditionalSettlorsYesNoPage
import pages.individual.deceased._
import play.api.i18n.Messages
import viewmodels.{AnswerRow, AnswerSection}

class DeceasedSettlorPrintHelper @Inject() (answerRowConverter: AnswerRowConverter) {

  def apply(userAnswers: UserAnswers, settlorName: String, hasAdditionalSettlors: Boolean)(implicit
    messages: Messages
  ): AnswerSection = {

    val bound = answerRowConverter.bind(userAnswers, settlorName)

    def answerRows: Seq[AnswerRow] = {
      val fullyMatched                       = userAnswers.get(BpMatchStatusPage).contains(FullyMatched)
      val fullyMatchedAndDateOfDeathRecorded = fullyMatched && userAnswers.isDateOfDeathRecorded
      Seq(
        bound.nameQuestion(
          NamePage,
          "deceasedSettlor.name",
          if (fullyMatched) None else Some(controllers.individual.deceased.routes.NameController.onPageLoad().url)
        ),
        bound.yesNoQuestion(
          DateOfDeathYesNoPage,
          "deceasedSettlor.dateOfDeathYesNo",
          if (fullyMatchedAndDateOfDeathRecorded) None
          else Some(controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad().url)
        ),
        bound.dateQuestion(
          DateOfDeathPage,
          "deceasedSettlor.dateOfDeath",
          if (fullyMatchedAndDateOfDeathRecorded) None
          else Some(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad().url)
        ),
        bound.yesNoQuestion(
          DateOfBirthYesNoPage,
          "deceasedSettlor.dateOfBirthYesNo",
          if (fullyMatched) None
          else Some(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad().url)
        ),
        bound.dateQuestion(
          DateOfBirthPage,
          "deceasedSettlor.dateOfBirth",
          if (fullyMatched) None
          else Some(controllers.individual.deceased.routes.DateOfBirthController.onPageLoad().url)
        ),
        bound.yesNoQuestion(CountryOfNationalityYesNoPage, "deceasedSettlor.nationalityYesNo", None),
        bound.yesNoQuestion(CountryOfNationalityUkYesNoPage, "deceasedSettlor.nationalityInTheUKYesNo", None),
        bound.countryQuestion(
          CountryOfNationalityUkYesNoPage,
          CountryOfNationalityPage,
          "deceasedSettlor.nationality",
          None
        ),
        bound.yesNoQuestion(
          NationalInsuranceNumberYesNoPage,
          "deceasedSettlor.nationalInsuranceNumberYesNo",
          if (fullyMatched) None
          else Some(controllers.individual.deceased.routes.NationalInsuranceNumberYesNoController.onPageLoad().url)
        ),
        bound.ninoQuestion(
          NationalInsuranceNumberPage,
          "deceasedSettlor.nationalInsuranceNumber",
          if (fullyMatched) None
          else Some(controllers.individual.deceased.routes.NationalInsuranceNumberController.onPageLoad().url)
        ),
        bound.yesNoQuestion(CountryOfResidenceYesNoPage, "deceasedSettlor.countryOfResidenceYesNo", None),
        bound.yesNoQuestion(CountryOfResidenceUkYesNoPage, "deceasedSettlor.countryOfResidenceInTheUkYesNo", None),
        bound.countryQuestion(
          CountryOfResidenceUkYesNoPage,
          CountryOfResidencePage,
          "deceasedSettlor.countryOfResidence",
          None
        ),
        bound.yesNoQuestion(
          AddressYesNoPage,
          "deceasedSettlor.addressYesNo",
          if (fullyMatched) None
          else Some(controllers.individual.deceased.routes.AddressYesNoController.onPageLoad().url)
        ),
        bound.yesNoQuestion(
          LivedInTheUkYesNoPage,
          "deceasedSettlor.livedInTheUkYesNo",
          if (fullyMatched) None
          else Some(controllers.individual.deceased.routes.LivedInTheUkYesNoController.onPageLoad().url)
        ),
        bound.addressQuestion(
          UkAddressPage,
          "deceasedSettlor.ukAddress",
          if (fullyMatched) None else Some(controllers.individual.deceased.routes.UkAddressController.onPageLoad().url)
        ),
        bound.addressQuestion(
          NonUkAddressPage,
          "deceasedSettlor.nonUkAddress",
          if (fullyMatched) None
          else Some(controllers.individual.deceased.routes.NonUkAddressController.onPageLoad().url)
        ),
        bound.additionalSettlorsQuestion(
          AdditionalSettlorsYesNoPage,
          "deceasedSettlor.additionalSettlorsYesNo",
          Some(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad().url),
          hasAdditionalSettlors
        )
      ).flatten
    }

    AnswerSection(headingKey = None, rows = answerRows)
  }

}
