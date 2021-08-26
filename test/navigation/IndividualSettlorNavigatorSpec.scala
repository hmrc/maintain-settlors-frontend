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

import base.SpecBase
import controllers.individual.living.add.{routes => addRts}
import controllers.individual.living.amend.{routes => amendRts}
import controllers.individual.living.{routes => rts}
import models._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.individual.living._

import java.time.LocalDate

class IndividualSettlorNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks  {

  val navigator = new IndividualSettlorNavigator

  private val passportOrId = CombinedPassportOrIdCard("FR", "num", LocalDate.parse("2020-01-01"))

  "Individual settlor navigator" when {
    
    "adding in 4mld mode" must {
      
      val mode: Mode = NormalMode

      "Name page -> Do you know date of birth page" in {
        navigator.nextPage(NamePage, mode, emptyUserAnswers)
          .mustBe(rts.DateOfBirthYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> Yes -> Date of birth page" in {
        val answers = emptyUserAnswers
          .set(DateOfBirthYesNoPage, true).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.DateOfBirthController.onPageLoad(mode))
      }

      "Date of birth page -> Do you know NINO page" in {
        navigator.nextPage(DateOfBirthPage, mode, emptyUserAnswers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> No -> Do you know NINO page" in {
        val answers = emptyUserAnswers
          .set(DateOfBirthYesNoPage, false).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Do you know NINO page -> Yes -> NINO page" in {
        val answers = emptyUserAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value

        navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberController.onPageLoad(mode))
      }

      "NINO page -> Start Date page" in {
        val answers = emptyUserAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value

        navigator.nextPage(NationalInsuranceNumberPage, mode, answers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Do you know NINO page -> No -> Do you know address page" in {
        val answers = emptyUserAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value

        navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, answers)
          .mustBe(rts.AddressYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> Yes -> Is address in UK page" in {
        val answers = emptyUserAnswers
          .set(AddressYesNoPage, true).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(rts.LiveInTheUkYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> No -> Start Date page" in {
        val answers = emptyUserAnswers
          .set(AddressYesNoPage, false).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Is address in UK page -> Yes -> UK address page" in {
        val answers = emptyUserAnswers
          .set(LiveInTheUkYesNoPage, true).success.value

        navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
          .mustBe(rts.UkAddressController.onPageLoad(mode))
      }

      "UK address page" when {
        "combined passport/id card details not present" must {
          "-> Do you know passport details yes/no page" in {
            navigator.nextPage(UkAddressPage, mode, emptyUserAnswers)
              .mustBe(rts.PassportDetailsYesNoController.onPageLoad(mode))
          }
        }

        "combined passport/id card details present" must {
          "-> Do you know passport or ID card details yes/no page" in {
            val answers = emptyUserAnswers
              .set(PassportOrIdCardDetailsPage, passportOrId).success.value

            navigator.nextPage(UkAddressPage, mode, answers)
              .mustBe(rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode))
          }
        }
      }

      "Is address in UK page -> No -> Non-UK address page" in {
        val answers = emptyUserAnswers
          .set(LiveInTheUkYesNoPage, false).success.value

        navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
          .mustBe(rts.NonUkAddressController.onPageLoad(mode))
      }

      "Non-UK address page" when {
        "combined passport/id card details not present" must {
          "-> Do you know passport details yes/no page" in {
            navigator.nextPage(NonUkAddressPage, mode, emptyUserAnswers)
              .mustBe(rts.PassportDetailsYesNoController.onPageLoad(mode))
          }
        }

        "combined passport/id card details present" must {
          "-> Do you know passport or ID card details yes/no page" in {
            val answers = emptyUserAnswers
              .set(PassportOrIdCardDetailsPage, passportOrId).success.value

            navigator.nextPage(NonUkAddressPage, mode, answers)
              .mustBe(rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode))
          }
        }
      }

      "Do you know passport details page -> Yes -> Passport details page" in {
        val answers = emptyUserAnswers
          .set(PassportDetailsYesNoPage, true).success.value

        navigator.nextPage(PassportDetailsYesNoPage, mode, answers)
          .mustBe(rts.PassportDetailsController.onPageLoad(mode))
      }

      "Passport details page -> Start Date page" in {
        navigator.nextPage(PassportDetailsPage, mode, emptyUserAnswers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Do you know passport details page -> No -> Do you know ID card details page" in {
        val answers = emptyUserAnswers
          .set(PassportDetailsYesNoPage, false).success.value

        navigator.nextPage(PassportDetailsYesNoPage, mode, answers)
          .mustBe(rts.IdCardDetailsYesNoController.onPageLoad(mode))
      }

      "Do you know ID card details page -> Yes -> ID card details page" in {
        val answers = emptyUserAnswers
          .set(IdCardDetailsYesNoPage, true).success.value

        navigator.nextPage(IdCardDetailsYesNoPage, mode, answers)
          .mustBe(rts.IdCardDetailsController.onPageLoad(mode))
      }

      "ID card details page -> Start Date page" in {
        navigator.nextPage(IdCardDetailsPage, mode, emptyUserAnswers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Do you know ID card details page -> No -> Start Date page" in {
        val answers = emptyUserAnswers
          .set(IdCardDetailsYesNoPage, false).success.value

        navigator.nextPage(IdCardDetailsYesNoPage, mode, answers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Start Date page -> Check details" in {
        navigator.nextPage(StartDatePage, mode, emptyUserAnswers)
          .mustBe(addRts.CheckDetailsController.onPageLoad())
      }
    }
    
    "amending in 4mld mode" must {

      val mode: Mode = CheckMode
      val index: Int = 0
      val baseAnswers: UserAnswers = emptyUserAnswers.set(IndexPage, index).success.value

      "Name page -> Do you know date of birth page" in {
        navigator.nextPage(NamePage, mode, baseAnswers)
          .mustBe(rts.DateOfBirthYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> Yes -> Date of birth page" in {
        val answers = baseAnswers
          .set(DateOfBirthYesNoPage, true).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.DateOfBirthController.onPageLoad(mode))
      }

      "Date of birth page -> Do you know NINO page" in {
        navigator.nextPage(DateOfBirthPage, mode, baseAnswers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> No -> Do you know NINO page" in {
        val answers = baseAnswers
          .set(DateOfBirthYesNoPage, false).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Do you know NINO page -> Yes -> NINO page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value

        navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberController.onPageLoad(mode))
      }

      "NINO page -> Check details" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value

        navigator.nextPage(NationalInsuranceNumberPage, mode, answers)
          .mustBe(controllers.individual.living.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
      }

      "Do you know NINO page -> No -> Do you know address page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value

        navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, answers)
          .mustBe(rts.AddressYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> Yes -> Is address in UK page" in {
        val answers = baseAnswers
          .set(AddressYesNoPage, true).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(rts.LiveInTheUkYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> No -> Check details page" in {
        val answers = baseAnswers
          .set(AddressYesNoPage, false).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(amendRts.CheckDetailsController.renderFromUserAnswers(index))
      }

      "Is address in UK page -> Yes -> UK address page" in {
        val answers = baseAnswers
          .set(LiveInTheUkYesNoPage, true).success.value

        navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
          .mustBe(rts.UkAddressController.onPageLoad(mode))
      }

      "UK address page" when {
        "combined passport/id card details not present" must {
          "-> Do you know passport details yes/no page" in {
            navigator.nextPage(UkAddressPage, mode, baseAnswers)
              .mustBe(rts.PassportDetailsYesNoController.onPageLoad(mode))
          }
        }

        "combined passport/id card details present" must {
          "-> Do you know passport or ID card details yes/no page" in {
            val answers = baseAnswers
              .set(PassportOrIdCardDetailsPage, passportOrId).success.value

            navigator.nextPage(UkAddressPage, mode, answers)
              .mustBe(rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode))
          }
        }
      }

      "Is address in UK page -> No -> Non-UK address page" in {
        val answers = baseAnswers
          .set(LiveInTheUkYesNoPage, false).success.value

        navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
          .mustBe(rts.NonUkAddressController.onPageLoad(mode))
      }

      "Non-UK address page" when {
        "combined passport/id card details not present" must {
          "-> Do you know passport details yes/no page" in {
            navigator.nextPage(NonUkAddressPage, mode, baseAnswers)
              .mustBe(rts.PassportDetailsYesNoController.onPageLoad(mode))
          }
        }

        "combined passport/id card details present" must {
          "-> Do you know passport or ID card details yes/no page" in {
            val answers = baseAnswers
              .set(PassportOrIdCardDetailsPage, passportOrId).success.value

            navigator.nextPage(NonUkAddressPage, mode, answers)
              .mustBe(rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode))
          }
        }
      }

      "Do you know passport or ID card details page -> Yes -> Passport or ID card details page" in {
        val answers = baseAnswers
          .set(PassportOrIdCardDetailsYesNoPage, true).success.value

        navigator.nextPage(PassportOrIdCardDetailsYesNoPage, mode, answers)
          .mustBe(rts.PassportOrIdCardDetailsController.onPageLoad(mode))
      }

      "Passport or ID card details page -> Check details" in {
        navigator.nextPage(PassportOrIdCardDetailsPage, mode, baseAnswers)
          .mustBe(amendRts.CheckDetailsController.renderFromUserAnswers(index))
      }

      "Do you know passport or ID card details page -> No -> Check details" in {
        val answers = baseAnswers
          .set(PassportOrIdCardDetailsYesNoPage, false).success.value

        navigator.nextPage(PassportOrIdCardDetailsYesNoPage, mode, answers)
          .mustBe(amendRts.CheckDetailsController.renderFromUserAnswers(index))
      }
    }

    "adding for a taxable trust in 5mld mode" must {

      val mode: Mode = NormalMode

      val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true)

      "Name page -> Do you know date of birth page" in {
        navigator.nextPage(NamePage, mode, baseAnswers)
          .mustBe(rts.DateOfBirthYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> Yes -> Date of birth page" in {
        val answers = baseAnswers
          .set(DateOfBirthYesNoPage, true).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.DateOfBirthController.onPageLoad(mode))
      }

      "Date of birth page -> Do you know country of nationality page" in {
        navigator.nextPage(DateOfBirthPage, mode, baseAnswers)
          .mustBe(rts.CountryOfNationalityYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> No -> Do you know country of nationality page" in {
        val answers = baseAnswers
          .set(DateOfBirthYesNoPage, false).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.CountryOfNationalityYesNoController.onPageLoad(mode))
      }

      "Do you know country of nationality page -> No -> Do you know NINO page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityYesNoPage, false).success.value

        navigator.nextPage(CountryOfNationalityYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Do you know country of nationality page -> Yes -> Is country of nationality in UK page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityYesNoPage, true).success.value

        navigator.nextPage(CountryOfNationalityYesNoPage, mode, answers)
          .mustBe(rts.CountryOfNationalityUkYesNoController.onPageLoad(mode))
      }

      "Is country of nationality in UK page -> Yes -> Do you know NINO page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityUkYesNoPage, true).success.value

        navigator.nextPage(CountryOfNationalityUkYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Is country of nationality in UK page -> No -> Country of nationality page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityUkYesNoPage, false).success.value

        navigator.nextPage(CountryOfNationalityUkYesNoPage, mode, answers)
          .mustBe(rts.CountryOfNationalityController.onPageLoad(mode))
      }

      "Country of nationality page -> Do you know NINO page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityPage, "DE").success.value

        navigator.nextPage(CountryOfNationalityPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Do you know NINO page -> Yes -> NINO page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value

        navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberController.onPageLoad(mode))
      }

      "NINO page -> Do you know country of residence page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value

        navigator.nextPage(NationalInsuranceNumberPage, mode, answers)
          .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
      }

      "Do you know country of residence page (with Nino) -> No -> Mental Capacity Yes/No Page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value
          .set(CountryOfResidenceYesNoPage, false).success.value

        navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Do you know country of residence page (with Nino) -> Yes -> Is country of residence in the UK page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value
          .set(CountryOfResidenceYesNoPage, true).success.value

        navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceUkYesNoController.onPageLoad(mode))
      }

      "Is country of residence in UK page (with Nino) -> Yes -> Mental Capacity Yes/No page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value
          .set(CountryOfResidenceUkYesNoPage, true).success.value

        navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Is country of residence in UK page (with Nino) -> No -> Country of residence page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value
          .set(CountryOfResidenceUkYesNoPage, false).success.value

        navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceController.onPageLoad(mode))
      }

      "Country of residence page (with Nino) -> Mental Capacity Yes/No page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value
          .set(CountryOfResidencePage, "DE").success.value

        navigator.nextPage(CountryOfResidencePage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Mental Capacity Yes/No page -> Yes -> Start Date page" in {
        val answers = baseAnswers
          .set(MentalCapacityYesNoPage, true).success.value

        navigator.nextPage(MentalCapacityYesNoPage, mode, answers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Mental Capacity Yes/No page -> No -> Start Date page" in {
        val answers = baseAnswers
          .set(MentalCapacityYesNoPage, false).success.value

        navigator.nextPage(MentalCapacityYesNoPage, mode, answers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Do you know NINO page -> No -> Do you know country of residence page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value

        navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
      }

      "Do you know country of residence page (without Nino) -> No -> Do you know address page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value
          .set(CountryOfResidenceYesNoPage, false).success.value

        navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
          .mustBe(rts.AddressYesNoController.onPageLoad(mode))
      }

      "Do you know country of residence page (without Nino) -> Yes -> Is country of residence in the UK page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value
          .set(CountryOfResidenceYesNoPage, true).success.value

        navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceUkYesNoController.onPageLoad(mode))
      }

      "Is country of residence in UK page (without Nino) -> Yes -> Do you know address page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value
          .set(CountryOfResidenceUkYesNoPage, true).success.value

        navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
          .mustBe(rts.AddressYesNoController.onPageLoad(mode))
      }

      "Is country of residence in UK page (without Nino) -> No -> Country of residence page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value
          .set(CountryOfResidenceUkYesNoPage, false).success.value

        navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceController.onPageLoad(mode))
      }

      "Country of residence page (without Nino) -> Do you know address page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value
          .set(CountryOfResidencePage, "DE").success.value

        navigator.nextPage(CountryOfResidencePage, mode, answers)
          .mustBe(rts.AddressYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> Yes -> Is address in UK page" in {
        val answers = baseAnswers
          .set(AddressYesNoPage, true).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(rts.LiveInTheUkYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> No -> Mental Capacity Yes/No page" in {
        val answers = baseAnswers
          .set(AddressYesNoPage, false).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Is address in UK page -> Yes -> UK address page" in {
        val answers = baseAnswers
          .set(LiveInTheUkYesNoPage, true).success.value

        navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
          .mustBe(rts.UkAddressController.onPageLoad(mode))
      }

      "UK address page" when {
        "combined passport/id card details not present" must {
          "-> Do you know passport details yes/no page" in {
            navigator.nextPage(UkAddressPage, mode, baseAnswers)
              .mustBe(rts.PassportDetailsYesNoController.onPageLoad(mode))
          }
        }

        "combined passport/id card details present" must {
          "-> Do you know passport or ID card details yes/no page" in {
            val answers = baseAnswers
              .set(PassportOrIdCardDetailsPage, passportOrId).success.value

            navigator.nextPage(UkAddressPage, mode, answers)
              .mustBe(rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode))
          }
        }
      }

      "Is address in UK page -> No -> Non-UK address page" in {
        val answers = baseAnswers
          .set(LiveInTheUkYesNoPage, false).success.value

        navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
          .mustBe(rts.NonUkAddressController.onPageLoad(mode))
      }

      "Non-UK address page" when {
        "combined passport/id card details not present" must {
          "-> Do you know passport details yes/no page" in {
            navigator.nextPage(NonUkAddressPage, mode, baseAnswers)
              .mustBe(rts.PassportDetailsYesNoController.onPageLoad(mode))
          }
        }

        "combined passport/id card details present" must {
          "-> Do you know passport or ID card details yes/no page" in {
            val answers = baseAnswers
              .set(PassportOrIdCardDetailsPage, passportOrId).success.value

            navigator.nextPage(NonUkAddressPage, mode, answers)
              .mustBe(rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode))
          }
        }
      }

      "Do you know passport details page -> Yes -> Passport details page" in {
        val answers = baseAnswers
          .set(PassportDetailsYesNoPage, true).success.value

        navigator.nextPage(PassportDetailsYesNoPage, mode, answers)
          .mustBe(rts.PassportDetailsController.onPageLoad(mode))
      }

      "Passport details page -> Mental Capacity Yes/No page" in {
        navigator.nextPage(PassportDetailsPage, mode, baseAnswers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Do you know passport details page -> No -> Do you know ID card details page" in {
        val answers = baseAnswers
          .set(PassportDetailsYesNoPage, false).success.value

        navigator.nextPage(PassportDetailsYesNoPage, mode, answers)
          .mustBe(rts.IdCardDetailsYesNoController.onPageLoad(mode))
      }

      "Do you know ID card details page -> Yes -> ID card details page" in {
        val answers = baseAnswers
          .set(IdCardDetailsYesNoPage, true).success.value

        navigator.nextPage(IdCardDetailsYesNoPage, mode, answers)
          .mustBe(rts.IdCardDetailsController.onPageLoad(mode))
      }

      "ID card details page -> Mental Capacity Yes/No page" in {
        navigator.nextPage(IdCardDetailsPage, mode, baseAnswers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Do you know ID card details page -> No -> Mental Capacity Yes/No page" in {
        val answers = baseAnswers
          .set(IdCardDetailsYesNoPage, false).success.value

        navigator.nextPage(IdCardDetailsYesNoPage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Start Date page -> Check details" in {
        navigator.nextPage(StartDatePage, mode, baseAnswers)
          .mustBe(addRts.CheckDetailsController.onPageLoad())
      }
    }

    "amending for a taxable trust in 5mld mode" must {

      val mode: Mode = CheckMode
      val index: Int = 0
      val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true).set(IndexPage, index).success.value

      "Name page -> Do you know date of birth page" in {
        navigator.nextPage(NamePage, mode, baseAnswers)
          .mustBe(rts.DateOfBirthYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> Yes -> Date of birth page" in {
        val answers = baseAnswers
          .set(DateOfBirthYesNoPage, true).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.DateOfBirthController.onPageLoad(mode))
      }

      "Date of birth page -> Do you know country of nationality page" in {
        navigator.nextPage(DateOfBirthPage, mode, baseAnswers)
          .mustBe(rts.CountryOfNationalityYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> No -> Do you know country of nationality page" in {
        val answers = baseAnswers
          .set(DateOfBirthYesNoPage, false).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.CountryOfNationalityYesNoController.onPageLoad(mode))
      }

      "Do you know country of nationality page -> No -> Do you know NINO page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityYesNoPage, false).success.value

        navigator.nextPage(CountryOfNationalityYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Do you know country of nationality page -> Yes -> Is country of nationality in UK page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityYesNoPage, true).success.value

        navigator.nextPage(CountryOfNationalityYesNoPage, mode, answers)
          .mustBe(rts.CountryOfNationalityUkYesNoController.onPageLoad(mode))
      }

      "Is country of nationality in UK page -> Yes -> Do you know NINO page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityUkYesNoPage, true).success.value

        navigator.nextPage(CountryOfNationalityUkYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Is country of nationality in UK page -> No -> Country of nationality page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityUkYesNoPage, false).success.value

        navigator.nextPage(CountryOfNationalityUkYesNoPage, mode, answers)
          .mustBe(rts.CountryOfNationalityController.onPageLoad(mode))
      }

      "Country of nationality page -> Do you know NINO page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityPage, "DE").success.value

        navigator.nextPage(CountryOfNationalityPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
      }

      "Do you know NINO page -> Yes -> NINO page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value

        navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, answers)
          .mustBe(rts.NationalInsuranceNumberController.onPageLoad(mode))
      }

      "NINO page -> Do you know country of residence page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value

        navigator.nextPage(NationalInsuranceNumberPage, mode, answers)
          .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
      }

      "Do you know country of residence page (with Nino) -> No -> Mental Capacity Yes/No Page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value
          .set(CountryOfResidenceYesNoPage, false).success.value

        navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Do you know country of residence page (with Nino) -> Yes -> Is country of residence in the UK page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value
          .set(CountryOfResidenceYesNoPage, true).success.value

        navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceUkYesNoController.onPageLoad(mode))
      }

      "Is country of residence in UK page (with Nino) -> Yes -> Mental Capacity Yes/No page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value
          .set(CountryOfResidenceUkYesNoPage, true).success.value

        navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Is country of residence in UK page (with Nino) -> No -> Country of residence page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value
          .set(CountryOfResidenceUkYesNoPage, false).success.value

        navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceController.onPageLoad(mode))
      }

      "Country of residence page (with Nino) -> Mental Capacity Yes/No page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, true).success.value
          .set(CountryOfResidencePage, "DE").success.value

        navigator.nextPage(CountryOfResidencePage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Mental Capacity Yes/No page -> Yes -> Check details" in {
        val answers = baseAnswers
          .set(MentalCapacityYesNoPage, true).success.value

        navigator.nextPage(MentalCapacityYesNoPage, mode, answers)
          .mustBe(amendRts.CheckDetailsController.renderFromUserAnswers(index))
      }

      "Mental Capacity Yes/No page -> No -> Check details" in {
        val answers = baseAnswers
          .set(MentalCapacityYesNoPage, false).success.value

        navigator.nextPage(MentalCapacityYesNoPage, mode, answers)
          .mustBe(amendRts.CheckDetailsController.renderFromUserAnswers(index))
      }

      "Do you know NINO page -> No -> Do you know country of residence page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value

        navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
      }

      "Do you know country of residence page (without Nino) -> No -> Do you know Address page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value
          .set(CountryOfResidenceYesNoPage, false).success.value

        navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
          .mustBe(rts.AddressYesNoController.onPageLoad(mode))
      }

      "Do you know country of residence page (without Nino) -> Yes -> Is country of residence in the UK page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value
          .set(CountryOfResidenceYesNoPage, true).success.value

        navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceUkYesNoController.onPageLoad(mode))
      }

      "Is country of residence in UK page (without Nino) -> Yes -> Do you know address page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value
          .set(CountryOfResidenceUkYesNoPage, true).success.value

        navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
          .mustBe(rts.AddressYesNoController.onPageLoad(mode))
      }

      "Is country of residence in UK page (without Nino) -> No -> Country of residence page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value
          .set(CountryOfResidenceUkYesNoPage, false).success.value

        navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceController.onPageLoad(mode))
      }

      "Country of residence page (without Nino) -> Do you know address page" in {
        val answers = baseAnswers
          .set(NationalInsuranceNumberYesNoPage, false).success.value
          .set(CountryOfResidencePage, "DE").success.value

        navigator.nextPage(CountryOfResidencePage, mode, answers)
          .mustBe(rts.AddressYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> Yes -> Is address in UK page" in {
        val answers = baseAnswers
          .set(AddressYesNoPage, true).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(rts.LiveInTheUkYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> No -> Mental Capacity Yes/No page" in {
        val answers = baseAnswers
          .set(AddressYesNoPage, false).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Is address in UK page -> Yes -> UK address page" in {
        val answers = baseAnswers
          .set(LiveInTheUkYesNoPage, true).success.value

        navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
          .mustBe(rts.UkAddressController.onPageLoad(mode))
      }

      "UK address page" when {
        "combined passport/id card details not present" must {
          "-> Do you know passport details yes/no page" in {
            navigator.nextPage(UkAddressPage, mode, baseAnswers)
              .mustBe(rts.PassportDetailsYesNoController.onPageLoad(mode))
          }
        }

        "combined passport/id card details present" must {
          "-> Do you know passport or ID card details yes/no page" in {
            val answers = baseAnswers
              .set(PassportOrIdCardDetailsPage, passportOrId).success.value

            navigator.nextPage(UkAddressPage, mode, answers)
              .mustBe(rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode))
          }
        }
      }

      "Is address in UK page -> No -> Non-UK address page" in {
        val answers = baseAnswers
          .set(LiveInTheUkYesNoPage, false).success.value

        navigator.nextPage(LiveInTheUkYesNoPage, mode, answers)
          .mustBe(rts.NonUkAddressController.onPageLoad(mode))
      }

      "Non-UK address page" when {
        "combined passport/id card details not present" must {
          "-> Do you know passport details yes/no page" in {
            navigator.nextPage(NonUkAddressPage, mode, baseAnswers)
              .mustBe(rts.PassportDetailsYesNoController.onPageLoad(mode))
          }
        }

        "combined passport/id card details present" must {
          "-> Do you know passport or ID card details yes/no page" in {
            val answers = baseAnswers
              .set(PassportOrIdCardDetailsPage, passportOrId).success.value

            navigator.nextPage(NonUkAddressPage, mode, answers)
              .mustBe(rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode))
          }
        }
      }

      "Do you know passport or ID card details page -> Yes -> Passport or ID card details page" in {
        val answers = baseAnswers
          .set(PassportOrIdCardDetailsYesNoPage, true).success.value

        navigator.nextPage(PassportOrIdCardDetailsYesNoPage, mode, answers)
          .mustBe(rts.PassportOrIdCardDetailsController.onPageLoad(mode))
      }

      "Passport or ID card details page -> Mental Capacity Yes/No page" in {
        navigator.nextPage(PassportOrIdCardDetailsPage, mode, baseAnswers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Do you know passport or ID card details page -> No -> Mental Capacity Yes/No page" in {
        val answers = baseAnswers
          .set(PassportOrIdCardDetailsYesNoPage, false).success.value

        navigator.nextPage(PassportOrIdCardDetailsYesNoPage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }
    }

    "adding for a non taxable trust in 5mld mode" must {

      val mode: Mode = NormalMode

      val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = false)

      "Name page -> Do you know date of birth page" in {
        navigator.nextPage(NamePage, mode, baseAnswers)
          .mustBe(rts.DateOfBirthYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> Yes -> Date of birth page" in {
        val answers = baseAnswers
          .set(DateOfBirthYesNoPage, true).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.DateOfBirthController.onPageLoad(mode))
      }

      "Date of birth page -> Do you know country of nationality page" in {
        navigator.nextPage(DateOfBirthPage, mode, baseAnswers)
          .mustBe(rts.CountryOfNationalityYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> No -> Do you know country of nationality page" in {
        val answers = baseAnswers
          .set(DateOfBirthYesNoPage, false).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.CountryOfNationalityYesNoController.onPageLoad(mode))
      }

      "Do you know country of nationality page -> No -> Do you know country of residence page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityYesNoPage, false).success.value

        navigator.nextPage(CountryOfNationalityYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
      }

      "Do you know country of nationality page -> Yes -> Is country of nationality in UK page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityYesNoPage, true).success.value

        navigator.nextPage(CountryOfNationalityYesNoPage, mode, answers)
          .mustBe(rts.CountryOfNationalityUkYesNoController.onPageLoad(mode))
      }

      "Is country of nationality in UK page -> Yes -> Do you know country of residence page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityUkYesNoPage, true).success.value

        navigator.nextPage(CountryOfNationalityUkYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
      }

      "Is country of nationality in UK page -> No -> Country of nationality page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityUkYesNoPage, false).success.value

        navigator.nextPage(CountryOfNationalityUkYesNoPage, mode, answers)
          .mustBe(rts.CountryOfNationalityController.onPageLoad(mode))
      }

      "Country of nationality page -> Do you know country of residence page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityPage, "DE").success.value

        navigator.nextPage(CountryOfNationalityPage, mode, answers)
          .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
      }

      "Do you know country of residence page -> No -> Mental Capacity Yes/No Page" in {
        val answers = baseAnswers
          .set(CountryOfResidenceYesNoPage, false).success.value

        navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Do you know country of residence page -> Yes -> Is country of residence in the UK page" in {
        val answers = baseAnswers
          .set(CountryOfResidenceYesNoPage, true).success.value

        navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceUkYesNoController.onPageLoad(mode))
      }

      "Is country of residence in UK page -> Yes -> Mental Capacity Yes/No page" in {
        val answers = baseAnswers
          .set(CountryOfResidenceUkYesNoPage, true).success.value

        navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Is country of residence in UK page -> No -> Country of residence page" in {
        val answers = baseAnswers
          .set(CountryOfResidenceUkYesNoPage, false).success.value

        navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceController.onPageLoad(mode))
      }

      "Country of residence page -> Mental Capacity Yes/No page" in {
        val answers = baseAnswers
          .set(CountryOfResidencePage, "DE").success.value

        navigator.nextPage(CountryOfResidencePage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Mental Capacity Yes/No page -> Yes -> Start Date page" in {
        val answers = baseAnswers
          .set(MentalCapacityYesNoPage, true).success.value

        navigator.nextPage(MentalCapacityYesNoPage, mode, answers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Mental Capacity Yes/No page -> No -> Start Date page" in {
        val answers = baseAnswers
          .set(MentalCapacityYesNoPage, false).success.value

        navigator.nextPage(MentalCapacityYesNoPage, mode, answers)
          .mustBe(addRts.StartDateController.onPageLoad())
      }

      "Start Date page -> Check details" in {
        navigator.nextPage(StartDatePage, mode, baseAnswers)
          .mustBe(addRts.CheckDetailsController.onPageLoad())
      }
    }

    "amending for a non taxable trust in 5mld mode" must {

      val mode: Mode = CheckMode
      val index: Int = 0
      val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = false)
        .set(IndexPage, index).success.value

      "Name page -> Do you know date of birth page" in {
        navigator.nextPage(NamePage, mode, baseAnswers)
          .mustBe(rts.DateOfBirthYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> Yes -> Date of birth page" in {
        val answers = baseAnswers
          .set(DateOfBirthYesNoPage, true).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.DateOfBirthController.onPageLoad(mode))
      }

      "Date of birth page -> Do you know country of nationality page" in {
        navigator.nextPage(DateOfBirthPage, mode, baseAnswers)
          .mustBe(rts.CountryOfNationalityYesNoController.onPageLoad(mode))
      }

      "Do you know date of birth page -> No -> Do you know country of nationality page" in {
        val answers = baseAnswers
          .set(DateOfBirthYesNoPage, false).success.value

        navigator.nextPage(DateOfBirthYesNoPage, mode, answers)
          .mustBe(rts.CountryOfNationalityYesNoController.onPageLoad(mode))
      }

      "Do you know country of nationality page -> No -> Do you know country of residence page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityYesNoPage, false).success.value

        navigator.nextPage(CountryOfNationalityYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
      }

      "Do you know country of nationality page -> Yes -> Is country of nationality in UK page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityYesNoPage, true).success.value

        navigator.nextPage(CountryOfNationalityYesNoPage, mode, answers)
          .mustBe(rts.CountryOfNationalityUkYesNoController.onPageLoad(mode))
      }

      "Is country of nationality in UK page -> Yes -> Do you know country of residence page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityUkYesNoPage, true).success.value

        navigator.nextPage(CountryOfNationalityUkYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
      }

      "Is country of nationality in UK page -> No -> Country of nationality page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityUkYesNoPage, false).success.value

        navigator.nextPage(CountryOfNationalityUkYesNoPage, mode, answers)
          .mustBe(rts.CountryOfNationalityController.onPageLoad(mode))
      }

      "Country of nationality page -> Do you know country of residence page" in {
        val answers = baseAnswers
          .set(CountryOfNationalityPage, "DE").success.value

        navigator.nextPage(CountryOfNationalityPage, mode, answers)
          .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
      }

      "Do you know country of residence page -> No -> Mental Capacity Yes/No Page" in {
        val answers = baseAnswers
          .set(CountryOfResidenceYesNoPage, false).success.value

        navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Do you know country of residence page -> Yes -> Is country of residence in the UK page" in {
        val answers = baseAnswers
          .set(CountryOfResidenceYesNoPage, true).success.value

        navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceUkYesNoController.onPageLoad(mode))
      }

      "Is country of residence in UK page -> Yes -> Mental Capacity Yes/No page" in {
        val answers = baseAnswers
          .set(CountryOfResidenceUkYesNoPage, true).success.value

        navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Is country of residence in UK page -> No -> Country of residence page" in {
        val answers = baseAnswers
          .set(CountryOfResidenceUkYesNoPage, false).success.value

        navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
          .mustBe(rts.CountryOfResidenceController.onPageLoad(mode))
      }

      "Country of residence page -> Mental Capacity Yes/No page" in {
        val answers = baseAnswers
          .set(CountryOfResidencePage, "DE").success.value

        navigator.nextPage(CountryOfResidencePage, mode, answers)
          .mustBe(rts.MentalCapacityYesNoController.onPageLoad(mode))
      }

      "Mental Capacity Yes/No page -> Yes -> Check details" in {
        val answers = baseAnswers
          .set(MentalCapacityYesNoPage, true).success.value

        navigator.nextPage(MentalCapacityYesNoPage, mode, answers)
          .mustBe(amendRts.CheckDetailsController.renderFromUserAnswers(index))
      }

      "Mental Capacity Yes/No page -> No -> Check details" in {
        val answers = baseAnswers
          .set(MentalCapacityYesNoPage, false).success.value

        navigator.nextPage(MentalCapacityYesNoPage, mode, answers)
          .mustBe(amendRts.CheckDetailsController.renderFromUserAnswers(index))
      }
    }
  }
}
