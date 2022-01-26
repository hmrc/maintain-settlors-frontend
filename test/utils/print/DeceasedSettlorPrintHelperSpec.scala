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

package utils.print

import java.time.LocalDate

import base.SpecBase
import models.BpMatchStatus.{FailedToMatch, FullyMatched}
import models.{BpMatchStatus, Name, NonUkAddress, UkAddress, UserAnswers}
import pages.AdditionalSettlorsYesNoPage
import pages.individual.deceased._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class DeceasedSettlorPrintHelperSpec extends SpecBase {

  private val name: Name = Name("First", Some("Middle"), "Last")
  private val ukAddress = UkAddress("value 1", "value 2", None, None, "AB1 1AB")
  private val nonUkAddress = NonUkAddress("value 1", "value 2", None, "DE")

  private def fillUserAnswers(bpMatchStatus: BpMatchStatus,
                              isDateOfDeathRecorded: Boolean): UserAnswers = {

    emptyUserAnswers.copy(isDateOfDeathRecorded = isDateOfDeathRecorded)
      .set(BpMatchStatusPage, bpMatchStatus).success.value
      .set(NamePage, name).success.value
      .set(DateOfDeathYesNoPage, true).success.value
      .set(DateOfDeathPage, LocalDate.of(2011, 10, 10)).success.value
      .set(DateOfBirthYesNoPage, true).success.value
      .set(DateOfBirthPage, LocalDate.of(2010, 10, 10)).success.value
      .set(NationalInsuranceNumberYesNoPage, true).success.value
      .set(NationalInsuranceNumberPage, "AA000000A").success.value
      .set(AddressYesNoPage, true).success.value
      .set(LivedInTheUkYesNoPage, true).success.value
      .set(UkAddressPage, ukAddress).success.value
      .set(NonUkAddressPage, nonUkAddress).success.value
      .set(AdditionalSettlorsYesNoPage, false).success.value
  }

  "DeceasedSettlorPrintHelper" must {

    "generate deceased settlor section for all possible data" in {

      val helper = injector.instanceOf[DeceasedSettlorPrintHelper]

      val userAnswers = fillUserAnswers(FailedToMatch, isDateOfDeathRecorded = true)

      val result = helper(userAnswers, name.displayName, hasAdditionalSettlors = false)
      result mustBe AnswerSection(
        headingKey = None,
        rows = Seq(
          AnswerRow(label = messages("deceasedSettlor.name.checkYourAnswersLabel"), answer = Html("First Middle Last"), changeUrl = Some(controllers.individual.deceased.routes.NameController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.dateOfDeathYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.dateOfDeath.checkYourAnswersLabel", name.displayName), answer = Html("10 October 2011"), changeUrl = Some(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.dateOfBirth.checkYourAnswersLabel", name.displayName), answer = Html("10 October 2010"), changeUrl = Some(controllers.individual.deceased.routes.DateOfBirthController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(controllers.individual.deceased.routes.NationalInsuranceNumberYesNoController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName), answer = Html("AA 00 00 00 A"), changeUrl = Some(controllers.individual.deceased.routes.NationalInsuranceNumberController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.addressYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(controllers.individual.deceased.routes.AddressYesNoController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.livedInTheUkYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(controllers.individual.deceased.routes.LivedInTheUkYesNoController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.ukAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = Some(controllers.individual.deceased.routes.UkAddressController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.nonUkAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = Some(controllers.individual.deceased.routes.NonUkAddressController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.additionalSettlorsYesNo.checkYourAnswersLabel"), answer = Html("No"), changeUrl = Some(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad().url))
        )
      )
    }

    "generate deceased settlor section for all possible data for 01 match status when date of death is known to ETMP and there are no other settlors" in {

      val helper = injector.instanceOf[DeceasedSettlorPrintHelper]

      val userAnswers = fillUserAnswers(FullyMatched, isDateOfDeathRecorded = true)

      val result = helper(userAnswers, name.displayName, hasAdditionalSettlors = false)
      result mustBe AnswerSection(
        headingKey = None,
        rows = Seq(
          AnswerRow(label = messages("deceasedSettlor.name.checkYourAnswersLabel"), answer = Html("First Middle Last"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.dateOfDeathYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.dateOfDeath.checkYourAnswersLabel", name.displayName), answer = Html("10 October 2011"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.dateOfBirth.checkYourAnswersLabel", name.displayName), answer = Html("10 October 2010"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName), answer = Html("AA 00 00 00 A"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.addressYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.livedInTheUkYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.ukAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.nonUkAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.additionalSettlorsYesNo.checkYourAnswersLabel"), answer = Html("No"), changeUrl = Some(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad().url))
        )
      )
    }

    "generate deceased settlor section for all possible for 01 match status data when date of death is not known to ETMP and there are other settlors" in {

      val helper = injector.instanceOf[DeceasedSettlorPrintHelper]

      val userAnswers = fillUserAnswers(FullyMatched, isDateOfDeathRecorded = false)

      val result = helper(userAnswers, name.displayName, hasAdditionalSettlors = true)
      result mustBe AnswerSection(
        headingKey = None,
        rows = Seq(
          AnswerRow(label = messages("deceasedSettlor.name.checkYourAnswersLabel"), answer = Html("First Middle Last"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.dateOfDeathYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.dateOfDeath.checkYourAnswersLabel", name.displayName), answer = Html("10 October 2011"), changeUrl = Some(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad().url)),
          AnswerRow(label = messages("deceasedSettlor.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.dateOfBirth.checkYourAnswersLabel", name.displayName), answer = Html("10 October 2010"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName), answer = Html("AA 00 00 00 A"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.addressYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.livedInTheUkYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.ukAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = None),
          AnswerRow(label = messages("deceasedSettlor.nonUkAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = None)
        )
      )
    }
  }
}
