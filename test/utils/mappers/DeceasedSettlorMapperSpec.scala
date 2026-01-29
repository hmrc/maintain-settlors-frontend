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

package utils.mappers

import base.SpecBase
import models.BpMatchStatus.FullyMatched
import models.{Name, NationalInsuranceNumber, NonUkAddress, UkAddress}
import pages.individual.deceased._

import java.time.LocalDate

class DeceasedSettlorMapperSpec extends SpecBase {

  private val bpMatchStatus = FullyMatched
  private val name          = Name("First", None, "Last")
  private val dateOfDeath   = LocalDate.parse("2011-02-03")
  private val dateOfBirth   = LocalDate.parse("2010-02-03")
  private val ukAddress     = UkAddress("line1", "line2", Some("line3"), Some("line4"), "POSTCODE")
  private val nonUkAddress  = NonUkAddress("line1", "line2", Some("line3"), "country")

  "DeceasedSettlorMapper" must {

    "generate deceased settlor model with nino, date of death and date of birth" in {

      val mapper = injector.instanceOf[DeceasedSettlorMapper]

      val nino = "AA123456A"

      val userAnswers = emptyUserAnswers
        .set(BpMatchStatusPage, bpMatchStatus)
        .success
        .value
        .set(NamePage, name)
        .success
        .value
        .set(DateOfDeathYesNoPage, true)
        .success
        .value
        .set(DateOfDeathPage, dateOfDeath)
        .success
        .value
        .set(DateOfBirthYesNoPage, true)
        .success
        .value
        .set(DateOfBirthPage, dateOfBirth)
        .success
        .value
        .set(NationalInsuranceNumberYesNoPage, true)
        .success
        .value
        .set(NationalInsuranceNumberPage, nino)
        .success
        .value

      val result = mapper(userAnswers).get

      result.bpMatchStatus.get mustBe bpMatchStatus
      result.name              mustBe name
      result.dateOfDeath       mustBe Some(dateOfDeath)
      result.dateOfBirth       mustBe Some(dateOfBirth)
      result.identification    mustBe Some(NationalInsuranceNumber(nino))
      result.address           mustBe None
    }

    "generate deceased settlor model with UK address" in {

      val mapper = injector.instanceOf[DeceasedSettlorMapper]

      val userAnswers = emptyUserAnswers
        .set(BpMatchStatusPage, bpMatchStatus)
        .success
        .value
        .set(NamePage, name)
        .success
        .value
        .set(DateOfDeathYesNoPage, true)
        .success
        .value
        .set(DateOfDeathPage, dateOfDeath)
        .success
        .value
        .set(DateOfBirthYesNoPage, true)
        .success
        .value
        .set(DateOfBirthPage, dateOfBirth)
        .success
        .value
        .set(NationalInsuranceNumberYesNoPage, false)
        .success
        .value
        .set(AddressYesNoPage, true)
        .success
        .value
        .set(LivedInTheUkYesNoPage, true)
        .success
        .value
        .set(UkAddressPage, ukAddress)
        .success
        .value

      val result = mapper(userAnswers).get

      result.bpMatchStatus.get mustBe bpMatchStatus
      result.name              mustBe name
      result.dateOfDeath       mustBe Some(dateOfDeath)
      result.dateOfBirth       mustBe Some(dateOfBirth)
      result.identification    mustBe None
      result.address           mustBe Some(ukAddress)
    }

    "generate deceased settlor model with non-UK address" in {

      val mapper = injector.instanceOf[DeceasedSettlorMapper]

      val userAnswers = emptyUserAnswers
        .set(BpMatchStatusPage, bpMatchStatus)
        .success
        .value
        .set(NamePage, name)
        .success
        .value
        .set(DateOfDeathYesNoPage, true)
        .success
        .value
        .set(DateOfDeathPage, dateOfDeath)
        .success
        .value
        .set(DateOfBirthYesNoPage, true)
        .success
        .value
        .set(DateOfBirthPage, dateOfBirth)
        .success
        .value
        .set(NationalInsuranceNumberYesNoPage, false)
        .success
        .value
        .set(AddressYesNoPage, true)
        .success
        .value
        .set(LivedInTheUkYesNoPage, false)
        .success
        .value
        .set(NonUkAddressPage, nonUkAddress)
        .success
        .value

      val result = mapper(userAnswers).get

      result.bpMatchStatus.get mustBe bpMatchStatus
      result.name              mustBe name
      result.dateOfDeath       mustBe Some(dateOfDeath)
      result.dateOfBirth       mustBe Some(dateOfBirth)
      result.identification    mustBe None
      result.address           mustBe Some(nonUkAddress)
    }

    "generate deceased settlor model with neither nino nor address" in {

      val mapper = injector.instanceOf[DeceasedSettlorMapper]

      val userAnswers = emptyUserAnswers
        .set(BpMatchStatusPage, bpMatchStatus)
        .success
        .value
        .set(NamePage, name)
        .success
        .value
        .set(DateOfDeathYesNoPage, true)
        .success
        .value
        .set(DateOfDeathPage, dateOfDeath)
        .success
        .value
        .set(DateOfBirthYesNoPage, true)
        .success
        .value
        .set(DateOfBirthPage, dateOfBirth)
        .success
        .value
        .set(NationalInsuranceNumberYesNoPage, false)
        .success
        .value
        .set(AddressYesNoPage, false)
        .success
        .value

      val result = mapper(userAnswers).get

      result.bpMatchStatus.get mustBe bpMatchStatus
      result.name              mustBe name
      result.dateOfDeath       mustBe Some(dateOfDeath)
      result.dateOfBirth       mustBe Some(dateOfBirth)
      result.identification    mustBe None
      result.address           mustBe None
    }

    "generate deceased settlor model when non-taxable" in {

      val mapper = injector.instanceOf[DeceasedSettlorMapper]

      val userAnswers = emptyUserAnswers
        .copy(isTaxable = false)
        .set(BpMatchStatusPage, bpMatchStatus)
        .success
        .value
        .set(NamePage, name)
        .success
        .value
        .set(DateOfDeathYesNoPage, false)
        .success
        .value
        .set(DateOfBirthYesNoPage, false)
        .success
        .value

      val result = mapper(userAnswers).get

      result.bpMatchStatus.get mustBe bpMatchStatus
      result.name              mustBe name
      result.dateOfDeath       mustBe None
      result.dateOfBirth       mustBe None
      result.identification    mustBe None
      result.address           mustBe None
    }
  }

}
