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

package utils.print

import com.google.inject.Inject
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.business._
import play.api.i18n.Messages
import viewmodels.{AnswerRow, AnswerSection}

class BusinessSettlorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) {

  def apply(userAnswers: UserAnswers, adding: Boolean, settlorName: String)
           (implicit messages: Messages): AnswerSection = {

    val bound = answerRowConverter.bind(userAnswers, settlorName)

    def answerRows: Seq[AnswerRow] = {
      val mode: Mode = if (adding) NormalMode else CheckMode
      Seq(
        bound.stringQuestion(NamePage, "businessSettlor.name", Some(controllers.business.routes.NameController.onPageLoad(mode).url)),
        bound.yesNoQuestion(UtrYesNoPage, "businessSettlor.utrYesNo", Some(controllers.business.routes.UtrYesNoController.onPageLoad(mode).url)),
        bound.stringQuestion(UtrPage, "businessSettlor.utr", Some(controllers.business.routes.UtrController.onPageLoad(mode).url)),
        bound.yesNoQuestion(CountryOfResidenceYesNoPage, "businessSettlor.countryOfResidenceYesNo", Some(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode).url)),
        bound.yesNoQuestion(CountryOfResidenceInTheUkYesNoPage, "businessSettlor.countryOfResidenceInTheUkYesNo", Some(controllers.business.routes.CountryOfResidenceInTheUkYesNoController.onPageLoad(mode).url)),
        bound.countryQuestion(CountryOfResidenceInTheUkYesNoPage, CountryOfResidencePage, "businessSettlor.countryOfResidence", Some(controllers.business.routes.CountryOfResidenceController.onPageLoad(mode).url)),
        bound.yesNoQuestion(AddressYesNoPage, "businessSettlor.addressYesNo", Some(controllers.business.routes.AddressYesNoController.onPageLoad(mode).url)),
        bound.yesNoQuestion(LiveInTheUkYesNoPage, "businessSettlor.liveInTheUkYesNo", Some(controllers.business.routes.LiveInTheUkYesNoController.onPageLoad(mode).url)),
        bound.addressQuestion(UkAddressPage, "businessSettlor.ukAddress", Some(controllers.business.routes.UkAddressController.onPageLoad(mode).url)),
        bound.addressQuestion(NonUkAddressPage, "businessSettlor.nonUkAddress", Some(controllers.business.routes.NonUkAddressController.onPageLoad(mode).url)),
        bound.companyTypeQuestion(CompanyTypePage, "businessSettlor.companyType", Some(controllers.business.routes.CompanyTypeController.onPageLoad(mode).url)),
        bound.yesNoQuestion(CompanyTimePage, "businessSettlor.companyTimeYesNo", Some(controllers.business.routes.CompanyTimeController.onPageLoad(mode).url)),
        if (adding) bound.dateQuestion(StartDatePage, "businessSettlor.startDate", Some(controllers.business.routes.StartDateController.onPageLoad().url)) else None
      ).flatten
    }

    AnswerSection(headingKey = None, rows = answerRows)
  }
}
