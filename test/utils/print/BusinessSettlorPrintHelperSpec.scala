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

import base.SpecBase
import models.{CheckMode, CompanyType, NonUkAddress, NormalMode, UkAddress}
import pages.business._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class BusinessSettlorPrintHelperSpec extends SpecBase {

  private val name: String               = "Name"
  private val utr: String                = "1234567890"
  private val ukAddress: UkAddress       = UkAddress("value 1", "value 2", None, None, "AB1 1AB")
  private val nonUkAddress: NonUkAddress = NonUkAddress("value 1", "value 2", None, "DE")

  "BusinessSettlorPrintHelper" must {

    val userAnswers = emptyUserAnswers
      .set(NamePage, name)
      .success
      .value
      .set(UtrYesNoPage, true)
      .success
      .value
      .set(UtrPage, utr)
      .success
      .value
      .set(AddressYesNoPage, true)
      .success
      .value
      .set(LiveInTheUkYesNoPage, true)
      .success
      .value
      .set(UkAddressPage, ukAddress)
      .success
      .value
      .set(NonUkAddressPage, nonUkAddress)
      .success
      .value
      .set(CountryOfResidenceYesNoPage, true)
      .success
      .value
      .set(CountryOfResidenceInTheUkYesNoPage, false)
      .success
      .value
      .set(CountryOfResidencePage, "ES")
      .success
      .value
      .set(CompanyTypePage, CompanyType.Investment)
      .success
      .value
      .set(CompanyTimePage, true)
      .success
      .value
      .set(StartDatePage, LocalDate.of(2020, 1, 1))
      .success
      .value

    val helper = injector.instanceOf[BusinessSettlorPrintHelper]

    "generate add business settlor section for all possible data" in {

      val mode = NormalMode

      val result = helper(userAnswers, adding = true, name)

      result mustBe AnswerSection(
        headingKey = None,
        rows = Seq(
          AnswerRow(
            label = messages("businessSettlor.name.checkYourAnswersLabel"),
            answer = Html("Name"),
            changeUrl = Some(controllers.business.routes.NameController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.utrYesNo.checkYourAnswersLabel", name),
            answer = Html("Yes"),
            changeUrl = Some(controllers.business.routes.UtrYesNoController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.utr.checkYourAnswersLabel", name),
            answer = Html("1234567890"),
            changeUrl = Some(controllers.business.routes.UtrController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.countryOfResidenceYesNo.checkYourAnswersLabel", name),
            answer = Html("Yes"),
            changeUrl = Some(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.countryOfResidenceInTheUkYesNo.checkYourAnswersLabel", name),
            answer = Html("No"),
            changeUrl = Some(controllers.business.routes.CountryOfResidenceInTheUkYesNoController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.countryOfResidence.checkYourAnswersLabel", name),
            answer = Html("Spain"),
            changeUrl = Some(controllers.business.routes.CountryOfResidenceController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.addressYesNo.checkYourAnswersLabel", name),
            answer = Html("Yes"),
            changeUrl = Some(controllers.business.routes.AddressYesNoController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.liveInTheUkYesNo.checkYourAnswersLabel", name),
            answer = Html("Yes"),
            changeUrl = Some(controllers.business.routes.LiveInTheUkYesNoController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.ukAddress.checkYourAnswersLabel", name),
            answer = Html("value 1<br />value 2<br />AB1 1AB"),
            changeUrl = Some(controllers.business.routes.UkAddressController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.nonUkAddress.checkYourAnswersLabel", name),
            answer = Html("value 1<br />value 2<br />Germany"),
            changeUrl = Some(controllers.business.routes.NonUkAddressController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.companyType.checkYourAnswersLabel", name),
            answer = Html("Investment"),
            changeUrl = Some(controllers.business.routes.CompanyTypeController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.companyTimeYesNo.checkYourAnswersLabel"),
            answer = Html("Yes"),
            changeUrl = Some(controllers.business.routes.CompanyTimeController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.startDate.checkYourAnswersLabel", name),
            answer = Html("1 January 2020"),
            changeUrl = Some(controllers.business.routes.StartDateController.onPageLoad().url)
          )
        )
      )
    }

    "generate amend business settlor section for all possible data" in {

      val mode = CheckMode

      val result = helper(userAnswers, adding = false, name)

      result mustBe AnswerSection(
        headingKey = None,
        rows = Seq(
          AnswerRow(
            label = messages("businessSettlor.name.checkYourAnswersLabel"),
            answer = Html("Name"),
            changeUrl = Some(controllers.business.routes.NameController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.utrYesNo.checkYourAnswersLabel", name),
            answer = Html("Yes"),
            changeUrl = Some(controllers.business.routes.UtrYesNoController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.utr.checkYourAnswersLabel", name),
            answer = Html("1234567890"),
            changeUrl = Some(controllers.business.routes.UtrController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.countryOfResidenceYesNo.checkYourAnswersLabel", name),
            answer = Html("Yes"),
            changeUrl = Some(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.countryOfResidenceInTheUkYesNo.checkYourAnswersLabel", name),
            answer = Html("No"),
            changeUrl = Some(controllers.business.routes.CountryOfResidenceInTheUkYesNoController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.countryOfResidence.checkYourAnswersLabel", name),
            answer = Html("Spain"),
            changeUrl = Some(controllers.business.routes.CountryOfResidenceController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.addressYesNo.checkYourAnswersLabel", name),
            answer = Html("Yes"),
            changeUrl = Some(controllers.business.routes.AddressYesNoController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.liveInTheUkYesNo.checkYourAnswersLabel", name),
            answer = Html("Yes"),
            changeUrl = Some(controllers.business.routes.LiveInTheUkYesNoController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.ukAddress.checkYourAnswersLabel", name),
            answer = Html("value 1<br />value 2<br />AB1 1AB"),
            changeUrl = Some(controllers.business.routes.UkAddressController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.nonUkAddress.checkYourAnswersLabel", name),
            answer = Html("value 1<br />value 2<br />Germany"),
            changeUrl = Some(controllers.business.routes.NonUkAddressController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.companyType.checkYourAnswersLabel", name),
            answer = Html("Investment"),
            changeUrl = Some(controllers.business.routes.CompanyTypeController.onPageLoad(mode).url)
          ),
          AnswerRow(
            label = messages("businessSettlor.companyTimeYesNo.checkYourAnswersLabel"),
            answer = Html("Yes"),
            changeUrl = Some(controllers.business.routes.CompanyTimeController.onPageLoad(mode).url)
          )
        )
      )
    }
  }

}
