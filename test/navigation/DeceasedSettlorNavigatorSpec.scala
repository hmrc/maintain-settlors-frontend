/*
 * Copyright 2023 HM Revenue & Customs
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

import base.SpecBase
import models.BpMatchStatus.{FailedToMatch, FullyMatched}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.AdditionalSettlorsYesNoPage
import pages.individual.deceased._

class DeceasedSettlorNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val navigator = new DeceasedSettlorNavigator

  "deceased settlor navigator" when {

    "taxable" when {

      val coreAnswers = emptyUserAnswers.copy(isTaxable = true)

      "Fully matched" when {

        val baseAnswers = coreAnswers.set(BpMatchStatusPage, FullyMatched).success.value

        "there are no other settlors" must {

          val answers = baseAnswers.set(AdditionalSettlorsYesNoPage, false).success.value

          "Do you know date of death page -> Yes -> Date of death page" in {

            navigator.nextPage(DateOfDeathYesNoPage, answers.set(DateOfDeathYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad())
          }

          "Do you know date of death page -> No -> Are there any additional settlors for the trust page" in {

            navigator.nextPage(DateOfDeathYesNoPage, answers.set(DateOfDeathYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad())
          }

          "Date of death page -> Are there any additional settlors for the trust page" in {

            navigator.nextPage(DateOfDeathPage, answers)
              .mustBe(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad())
          }

          "Are there any additional settlors for the trust page -> Check details page" in {

            navigator.nextPage(AdditionalSettlorsYesNoPage, answers)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }
        }

        "there are other settlors" must {

          "Do you know date of death page -> Yes -> Date of death page" in {

            navigator.nextPage(DateOfDeathYesNoPage, baseAnswers.set(DateOfDeathYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad())
          }

          "Do you know date of death page -> No -> Check details page" in {

            navigator.nextPage(DateOfDeathYesNoPage, baseAnswers.set(DateOfDeathYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }

          "Date of death page -> Check details page" in {

            navigator.nextPage(DateOfDeathPage, baseAnswers)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }
        }
      }

      "Not fully matched" when {

        val baseAnswers = coreAnswers.set(BpMatchStatusPage, FailedToMatch).success.value

        "there are no other settlors" must {

          val answers = baseAnswers.set(AdditionalSettlorsYesNoPage, false).success.value

          "Name page -> Do you know date of death page" in {

            navigator.nextPage(NamePage, answers)
              .mustBe(controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad())
          }

          "Do you know date of death page -> Yes -> Date of death page" in {

            navigator.nextPage(DateOfDeathYesNoPage, answers.set(DateOfDeathYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad())
          }

          "Do you know date of death page -> No -> Do you know date of birth page" in {

            navigator.nextPage(DateOfDeathYesNoPage, answers.set(DateOfDeathYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad())
          }

          "Date of death page -> Do you know date of birth page" in {

            navigator.nextPage(DateOfDeathPage, answers)
              .mustBe(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad())
          }

          "Do you know date of birth page -> Yes -> Date of birth page" in {

            navigator.nextPage(DateOfBirthYesNoPage, answers.set(DateOfBirthYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfBirthController.onPageLoad())
          }

          "Do you know date of birth page -> No -> Do you know NINO page" in {

            navigator.nextPage(DateOfBirthYesNoPage, answers.set(DateOfBirthYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.NationalInsuranceNumberYesNoController.onPageLoad())
          }

          "Date of birth page -> Do you know NINO page" in {

            navigator.nextPage(DateOfBirthPage, answers)
              .mustBe(controllers.individual.deceased.routes.NationalInsuranceNumberYesNoController.onPageLoad())
          }

          "Do you know NINO page -> Yes -> NINO page" in {

            navigator.nextPage(NationalInsuranceNumberYesNoPage, answers.set(NationalInsuranceNumberYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.NationalInsuranceNumberController.onPageLoad())
          }

          "Do you know NINO page -> No -> Do you know address page" in {

            navigator.nextPage(NationalInsuranceNumberYesNoPage, answers.set(NationalInsuranceNumberYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.AddressYesNoController.onPageLoad())
          }

          "NINO page -> Are there any additional settlors for the trust page" in {

            navigator.nextPage(NationalInsuranceNumberPage, answers)
              .mustBe(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad())
          }

          "Do you know address page -> Yes -> Is address in UK page" in {

            navigator.nextPage(AddressYesNoPage, answers.set(AddressYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.LivedInTheUkYesNoController.onPageLoad())
          }

          "Do you know address page -> No -> Are there any additional settlors for the trust page" in {

            navigator.nextPage(AddressYesNoPage, answers.set(AddressYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad())
          }

          "Is address in UK page -> Yes -> UK address page" in {

            navigator.nextPage(LivedInTheUkYesNoPage, answers.set(LivedInTheUkYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.UkAddressController.onPageLoad())
          }

          "Is address in UK page -> No -> Non-UK address page" in {

            navigator.nextPage(LivedInTheUkYesNoPage, answers.set(LivedInTheUkYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.NonUkAddressController.onPageLoad())
          }

          "UK address page -> Are there any additional settlors for the trust page" in {

            navigator.nextPage(UkAddressPage, answers)
              .mustBe(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad())
          }

          "Non-UK address page -> Are there any additional settlors for the trust page" in {

            navigator.nextPage(NonUkAddressPage, answers)
              .mustBe(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad())
          }

          "Are there any additional settlors for the trust page -> Check details page" in {

            navigator.nextPage(AdditionalSettlorsYesNoPage, answers)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }
        }

        "there are other settlors" must {

          "Name page -> Do you know date of death page" in {
            navigator.nextPage(NamePage, baseAnswers)
              .mustBe(controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad())
          }

          "Do you know date of death page -> Yes -> Date of death page" in {

            navigator.nextPage(DateOfDeathYesNoPage, baseAnswers.set(DateOfDeathYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad())
          }

          "Do you know date of death page -> No -> Do you know date of birth page" in {

            navigator.nextPage(DateOfDeathYesNoPage, baseAnswers.set(DateOfDeathYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad())
          }

          "Date of death page -> Do you know date of birth page" in {

            navigator.nextPage(DateOfDeathPage, baseAnswers)
              .mustBe(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad())
          }

          "Do you know date of birth page -> Yes -> Date of birth page" in {

            navigator.nextPage(DateOfBirthYesNoPage, baseAnswers.set(DateOfBirthYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfBirthController.onPageLoad())
          }

          "Do you know date of birth page -> No -> Do you know NINO page" in {

            navigator.nextPage(DateOfBirthYesNoPage, baseAnswers.set(DateOfBirthYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.NationalInsuranceNumberYesNoController.onPageLoad())
          }

          "Date of birth page -> Do you know NINO page" in {

            navigator.nextPage(DateOfBirthPage, baseAnswers)
              .mustBe(controllers.individual.deceased.routes.NationalInsuranceNumberYesNoController.onPageLoad())
          }

          "Do you know NINO page -> Yes -> NINO page" in {

            navigator.nextPage(NationalInsuranceNumberYesNoPage, baseAnswers.set(NationalInsuranceNumberYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.NationalInsuranceNumberController.onPageLoad())
          }

          "Do you know NINO page -> No -> Do you know address page" in {

            navigator.nextPage(NationalInsuranceNumberYesNoPage, baseAnswers.set(NationalInsuranceNumberYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.AddressYesNoController.onPageLoad())
          }

          "NINO page -> Check details page" in {

            navigator.nextPage(NationalInsuranceNumberPage, baseAnswers)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }

          "Do you know address page -> Yes -> Is address in UK page" in {

            navigator.nextPage(AddressYesNoPage, baseAnswers.set(AddressYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.LivedInTheUkYesNoController.onPageLoad())
          }

          "Do you know address page -> No -> Check details page" in {

            navigator.nextPage(AddressYesNoPage, baseAnswers.set(AddressYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }

          "Is address in UK page -> Yes -> UK address page" in {

            navigator.nextPage(LivedInTheUkYesNoPage, baseAnswers.set(LivedInTheUkYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.UkAddressController.onPageLoad())
          }

          "Is address in UK page -> No -> Non-UK address page" in {

            navigator.nextPage(LivedInTheUkYesNoPage, baseAnswers.set(LivedInTheUkYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.NonUkAddressController.onPageLoad())
          }

          "UK address page -> Check details page" in {

            navigator.nextPage(UkAddressPage, baseAnswers)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }

          "Non-UK address page -> Check details page" in {

            navigator.nextPage(NonUkAddressPage, baseAnswers)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }
        }
      }
    }

    "non-taxable" when {

      val coreAnswers = emptyUserAnswers.copy(isTaxable = false)

      "Fully matched" when {

        val baseAnswers = coreAnswers.set(BpMatchStatusPage, FullyMatched).success.value

        "there are no other settlors" must {

          val answers = baseAnswers.set(AdditionalSettlorsYesNoPage, false).success.value

          "Do you know date of death page -> Yes -> Date of death page" in {

            navigator.nextPage(DateOfDeathYesNoPage, answers.set(DateOfDeathYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad())
          }

          "Do you know date of death page -> No -> Are there any additional settlors for the trust page" in {

            navigator.nextPage(DateOfDeathYesNoPage, answers.set(DateOfDeathYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad())
          }

          "Date of death page -> Are there any additional settlors for the trust page" in {

            navigator.nextPage(DateOfDeathPage, answers)
              .mustBe(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad())
          }

          "Are there any additional settlors for the trust page -> Check details page" in {

            navigator.nextPage(AdditionalSettlorsYesNoPage, answers)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }
        }

        "there are other settlors" must {

          "Do you know date of death page -> Yes -> Date of death page" in {

            navigator.nextPage(DateOfDeathYesNoPage, baseAnswers.set(DateOfDeathYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad())
          }

          "Do you know date of death page -> No -> Check details page" in {

            navigator.nextPage(DateOfDeathYesNoPage, baseAnswers.set(DateOfDeathYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }

          "Date of death page -> Check details page" in {

            navigator.nextPage(DateOfDeathPage, baseAnswers)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }
        }
      }

      "Not fully matched" when {

        val baseAnswers = coreAnswers.set(BpMatchStatusPage, FailedToMatch).success.value

        "there are no other settlors" must {

          val answers = baseAnswers.set(AdditionalSettlorsYesNoPage, false).success.value

          "Name page -> Do you know date of death page" in {

            navigator.nextPage(NamePage, answers)
              .mustBe(controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad())
          }

          "Do you know date of death page -> Yes -> Date of death page" in {

            navigator.nextPage(DateOfDeathYesNoPage, answers.set(DateOfDeathYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad())
          }

          "Do you know date of death page -> No -> Do you know date of birth page" in {

            navigator.nextPage(DateOfDeathYesNoPage, answers.set(DateOfDeathYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad())
          }

          "Date of death page -> Do you know date of birth page" in {

            navigator.nextPage(DateOfDeathPage, answers)
              .mustBe(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad())
          }

          "Do you know date of birth page -> Yes -> Date of birth page" in {

            navigator.nextPage(DateOfBirthYesNoPage, answers.set(DateOfBirthYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfBirthController.onPageLoad())
          }

          "Do you know date of birth page -> No -> Are there any additional settlors for the trust page" in {

            navigator.nextPage(DateOfBirthYesNoPage, answers.set(DateOfBirthYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad())
          }

          "Date of birth page -> Are there any additional settlors for the trust page" in {

            navigator.nextPage(DateOfBirthPage, answers)
              .mustBe(controllers.individual.deceased.routes.AdditionalSettlorsYesNoController.onPageLoad())
          }

          "Are there any additional settlors for the trust page -> Check details page" in {

            navigator.nextPage(AdditionalSettlorsYesNoPage, answers)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }
        }

        "there are other settlors" must {

          "Name page -> Do you know date of death page" in {
            navigator.nextPage(NamePage, baseAnswers)
              .mustBe(controllers.individual.deceased.routes.DateOfDeathYesNoController.onPageLoad())
          }

          "Do you know date of death page -> Yes -> Date of death page" in {

            navigator.nextPage(DateOfDeathYesNoPage, baseAnswers.set(DateOfDeathYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfDeathController.onPageLoad())
          }

          "Do you know date of death page -> No -> Do you know date of birth page" in {

            navigator.nextPage(DateOfDeathYesNoPage, baseAnswers.set(DateOfDeathYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad())
          }

          "Date of death page -> Do you know date of birth page" in {

            navigator.nextPage(DateOfDeathPage, baseAnswers)
              .mustBe(controllers.individual.deceased.routes.DateOfBirthYesNoController.onPageLoad())
          }

          "Do you know date of birth page -> Yes -> Date of birth page" in {

            navigator.nextPage(DateOfBirthYesNoPage, baseAnswers.set(DateOfBirthYesNoPage, true).success.value)
              .mustBe(controllers.individual.deceased.routes.DateOfBirthController.onPageLoad())
          }

          "Do you know date of birth page -> No -> Check details page" in {

            navigator.nextPage(DateOfBirthYesNoPage, baseAnswers.set(DateOfBirthYesNoPage, false).success.value)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }

          "Date of birth page -> Check details page" in {

            navigator.nextPage(DateOfBirthPage, baseAnswers)
              .mustBe(controllers.individual.deceased.routes.CheckDetailsController.renderFromUserAnswers())
          }
        }
      }
    }
  }
}
