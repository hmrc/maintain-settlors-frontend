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

package utils.print

import com.google.inject.Inject
import controllers.individual.living.add.{routes => addRts}
import controllers.individual.living.{routes => rts}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.individual.living._
import play.api.i18n.Messages
import viewmodels.{AnswerRow, AnswerSection}

class IndividualSettlorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) {

  def apply(userAnswers: UserAnswers, adding: Boolean, settlorName: String)
           (implicit messages: Messages): AnswerSection = {

    val bound = answerRowConverter.bind(userAnswers, settlorName)

    def answerRows: Seq[AnswerRow] = {
      val mode: Mode = if (adding) NormalMode else CheckMode
      Seq(
        bound.nameQuestion(NamePage, "livingSettlor.name", Some(rts.NameController.onPageLoad(mode).url)),
        bound.yesNoQuestion(DateOfBirthYesNoPage, "livingSettlor.dateOfBirthYesNo", Some(rts.DateOfBirthYesNoController.onPageLoad(mode).url)),
        bound.dateQuestion(DateOfBirthPage, "livingSettlor.dateOfBirth", Some(rts.DateOfBirthController.onPageLoad(mode).url)),
        bound.yesNoQuestion(CountryOfNationalityYesNoPage, "livingSettlor.countryOfNationalityYesNo", Some(rts.CountryOfNationalityYesNoController.onPageLoad(mode).url)),
        bound.yesNoQuestion(CountryOfNationalityUkYesNoPage, "livingSettlor.countryOfNationalityUkYesNo", Some(rts.CountryOfNationalityUkYesNoController.onPageLoad(mode).url)),
        bound.countryQuestion(CountryOfNationalityUkYesNoPage, CountryOfNationalityPage, "livingSettlor.countryOfNationality", Some(rts.CountryOfNationalityController.onPageLoad(mode).url)),
        bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "livingSettlor.nationalInsuranceNumberYesNo", Some(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode).url)),
        bound.ninoQuestion(NationalInsuranceNumberPage, "livingSettlor.nationalInsuranceNumber", Some(rts.NationalInsuranceNumberController.onPageLoad(mode).url)),
        bound.yesNoQuestion(CountryOfResidenceYesNoPage, "livingSettlor.countryOfResidenceYesNo", Some(rts.CountryOfResidenceYesNoController.onPageLoad(mode).url)),
        bound.yesNoQuestion(CountryOfResidenceUkYesNoPage, "livingSettlor.countryOfResidenceUkYesNo", Some(rts.CountryOfResidenceUkYesNoController.onPageLoad(mode).url)),
        bound.countryQuestion(CountryOfResidenceUkYesNoPage, CountryOfResidencePage, "livingSettlor.countryOfResidence", Some(rts.CountryOfResidenceController.onPageLoad(mode).url)),
        bound.yesNoQuestion(AddressYesNoPage, "livingSettlor.addressYesNo", Some(rts.AddressYesNoController.onPageLoad(mode).url)),
        bound.yesNoQuestion(LiveInTheUkYesNoPage, "livingSettlor.liveInTheUkYesNo", Some(rts.LiveInTheUkYesNoController.onPageLoad(mode).url)),
        bound.addressQuestion(UkAddressPage, "livingSettlor.ukAddress", Some(rts.UkAddressController.onPageLoad(mode).url)),
        bound.addressQuestion(NonUkAddressPage, "livingSettlor.nonUkAddress", Some(rts.NonUkAddressController.onPageLoad(mode).url)),
        bound.yesNoQuestion(PassportDetailsYesNoPage, "livingSettlor.passportDetailsYesNo", Some(rts.PassportDetailsYesNoController.onPageLoad(mode).url)),
        bound.passportDetailsQuestion(PassportDetailsPage, "livingSettlor.passportDetails", Some(rts.PassportDetailsController.onPageLoad(mode).url)),
        bound.yesNoQuestion(IdCardDetailsYesNoPage, "livingSettlor.idCardDetailsYesNo", Some(rts.IdCardDetailsYesNoController.onPageLoad(mode).url)),
        bound.idCardDetailsQuestion(IdCardDetailsPage, "livingSettlor.idCardDetails", Some(rts.IdCardDetailsController.onPageLoad(mode).url)),
        bound.yesNoQuestion(PassportOrIdCardDetailsYesNoPage, "livingSettlor.passportOrIdCardDetailsYesNo", Some(rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode).url)),
        bound.passportOrIdCardDetailsQuestion(PassportOrIdCardDetailsPage, "livingSettlor.passportOrIdCardDetails", Some(rts.PassportOrIdCardDetailsController.onPageLoad(mode).url)),
        bound.enumQuestion(MentalCapacityYesNoPage, "livingSettlor.mentalCapacityYesNo", rts.MentalCapacityYesNoController.onPageLoad(mode).url, "site"),
        if (adding) bound.dateQuestion(StartDatePage, "livingSettlor.startDate", Some(addRts.StartDateController.onPageLoad().url)) else None
      ).flatten
    }

    AnswerSection(headingKey = None, rows = answerRows)
  }
}
