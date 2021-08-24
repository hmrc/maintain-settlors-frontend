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

import base.SpecBase
import controllers.individual.living.add.{routes => addRts}
import controllers.individual.living.{routes => rts}
import models._
import pages.individual.living._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class IndividualSettlorPrintHelperSpec extends SpecBase {

  private val name: Name = Name("First", Some("Middle"), "Last")
  private val ukAddress: UkAddress = UkAddress("value 1", "value 2", None, None, "AB1 1AB")
  private val nonUkAddress: NonUkAddress = NonUkAddress("value 1", "value 2", None, "DE")

  private val helper: IndividualSettlorPrintHelper = injector.instanceOf[IndividualSettlorPrintHelper]

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value
    .set(DateOfBirthYesNoPage, true).success.value
    .set(DateOfBirthPage, LocalDate.of(2010, 10, 10)).success.value
    .set(NationalInsuranceNumberYesNoPage, true).success.value
    .set(NationalInsuranceNumberPage, "AA000000A").success.value
    .set(AddressYesNoPage, true).success.value
    .set(LiveInTheUkYesNoPage, true).success.value
    .set(UkAddressPage, ukAddress).success.value
    .set(NonUkAddressPage, nonUkAddress).success.value
    .set(StartDatePage, LocalDate.of(2020, 1, 1)).success.value

  "IndividualSettlorPrintHelper" when {

    "adding" must {
      "generate individual settlor section for all possible data" in {
        
        val mode: Mode = NormalMode

        val userAnswers: UserAnswers = baseAnswers
          .set(PassportDetailsYesNoPage, true).success.value
          .set(PassportDetailsPage, Passport("GB", "1234567890", LocalDate.of(2030, 10, 10))).success.value
          .set(IdCardDetailsYesNoPage, true).success.value
          .set(IdCardDetailsPage, IdCard("GB", "1234567890", LocalDate.of(2030, 10, 10))).success.value

        val result = helper(userAnswers, adding = true, name.displayName)
        result mustBe AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(label = messages("livingSettlor.name.checkYourAnswersLabel"), answer = Html("First Middle Last"), changeUrl = Some(rts.NameController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.DateOfBirthYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.dateOfBirth.checkYourAnswersLabel", name.displayName), answer = Html("10 October 2010"), changeUrl = Some(rts.DateOfBirthController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName), answer = Html("AA 00 00 00 A"), changeUrl = Some(rts.NationalInsuranceNumberController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.addressYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.AddressYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.liveInTheUkYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.LiveInTheUkYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.ukAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = Some(rts.UkAddressController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.nonUkAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = Some(rts.NonUkAddressController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.passportDetailsYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.PassportDetailsYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.passportDetails.checkYourAnswersLabel", name.displayName), answer = Html("United Kingdom<br />1234567890<br />10 October 2030"), changeUrl = Some(rts.PassportDetailsController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.idCardDetailsYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.IdCardDetailsYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.idCardDetails.checkYourAnswersLabel", name.displayName), answer = Html("United Kingdom<br />1234567890<br />10 October 2030"), changeUrl = Some(rts.IdCardDetailsController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.startDate.checkYourAnswersLabel", name.displayName), answer = Html("1 January 2020"), changeUrl = Some(addRts.StartDateController.onPageLoad().url))
          )
        )
      }
    }

    "amending" must {
      "generate individual settlor section for all possible data" in {

        val mode: Mode = CheckMode

        val userAnswers = baseAnswers
          .set(PassportOrIdCardDetailsYesNoPage, true).success.value
          .set(PassportOrIdCardDetailsPage, CombinedPassportOrIdCard("GB", "1234567890", LocalDate.of(2030, 10, 10))).success.value
          .set(ProvisionalIdDetailsPage, false).success.value

        val result = helper(userAnswers, adding = false, name.displayName)
        result mustBe AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(label = messages("livingSettlor.name.checkYourAnswersLabel"), answer = Html("First Middle Last"), changeUrl = Some(rts.NameController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.DateOfBirthYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.dateOfBirth.checkYourAnswersLabel", name.displayName), answer = Html("10 October 2010"), changeUrl = Some(rts.DateOfBirthController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName), answer = Html("AA 00 00 00 A"), changeUrl = Some(rts.NationalInsuranceNumberController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.addressYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.AddressYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.liveInTheUkYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.LiveInTheUkYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.ukAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = Some(rts.UkAddressController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.nonUkAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = Some(rts.NonUkAddressController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.passportOrIdCardDetailsYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("livingSettlor.passportOrIdCardDetails.checkYourAnswersLabel", name.displayName), answer = Html("United Kingdom<br />Number ending 7890<br />10 October 2030"), changeUrl = Some(rts.PassportOrIdCardDetailsController.onPageLoad(mode).url))
          )
        )
      }
    }
  }
}
