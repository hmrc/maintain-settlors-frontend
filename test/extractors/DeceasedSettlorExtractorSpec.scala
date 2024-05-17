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

package extractors

import base.SpecBase
import models.BpMatchStatus.FullyMatched
import models.settlors.DeceasedSettlor
import models.{Name, NationalInsuranceNumber, UkAddress}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.AdditionalSettlorsYesNoPage
import pages.individual.deceased._

import java.time.LocalDate

class DeceasedSettlorExtractorSpec extends SpecBase with ScalaCheckPropertyChecks {

  val name: Name = Name("First", None, "Last")
  val date: LocalDate = LocalDate.parse("1967-02-03")
  val dateOfDeath: LocalDate = LocalDate.parse("1957-02-03")
  val address: UkAddress = UkAddress("Line 1", "Line 2", None, None, "postcode")

  val extractor = new DeceasedSettlorExtractor()

  "should populate user answers when a deceased individual has a NINO" in {

    val nino = NationalInsuranceNumber("nino")

    val individual = DeceasedSettlor(
      bpMatchStatus = None,
      name = name,
      dateOfDeath = Some(dateOfDeath),
      dateOfBirth = Some(date),
      nationality = None,
      countryOfResidence = None,
      identification = Some(nino),
      address = None
    )

    val result = extractor(emptyUserAnswers, individual, None, hasAdditionalSettlors = Some(false)).get

    result.get(BpMatchStatusPage) mustNot be(defined)
    result.get(NamePage).get mustBe name
    result.get(DateOfDeathYesNoPage).get mustBe true
    result.get(DateOfDeathPage).get mustBe dateOfDeath
    result.get(DateOfBirthYesNoPage).get mustBe true
    result.get(DateOfBirthPage).get mustBe date
    result.get(NationalInsuranceNumberYesNoPage).get mustBe true
    result.get(NationalInsuranceNumberPage).get mustBe "nino"
    result.get(AddressYesNoPage) mustNot be(defined)
    result.get(LivedInTheUkYesNoPage) mustNot be(defined)
    result.get(UkAddressPage) mustNot be(defined)
    result.get(NonUkAddressPage) mustNot be(defined)
  }

  "should populate user answers when a deceased individual has an address but no NINO" in {

    val individual = DeceasedSettlor(
      bpMatchStatus = None,
      name = name,
      dateOfDeath = Some(dateOfDeath),
      dateOfBirth = Some(date),
      nationality = None,
      countryOfResidence = None,
      identification = None,
      address = Some(address)
    )

    val result = extractor(emptyUserAnswers, individual, None, hasAdditionalSettlors = Some(false)).get

    result.get(BpMatchStatusPage) mustNot be(defined)
    result.get(NamePage).get mustBe name
    result.get(DateOfDeathYesNoPage).get mustBe true
    result.get(DateOfDeathPage).get mustBe dateOfDeath
    result.get(DateOfBirthYesNoPage).get mustBe true
    result.get(DateOfBirthPage).get mustBe date
    result.get(NationalInsuranceNumberYesNoPage).get mustBe false
    result.get(NationalInsuranceNumberPage) mustNot be(defined)
    result.get(AddressYesNoPage).get mustBe true
    result.get(LivedInTheUkYesNoPage).get mustBe true
    result.get(UkAddressPage).get mustBe address
    result.get(NonUkAddressPage) mustNot be(defined)
  }

  "should populate user answers when a deceased individual has only a name" in {

    val individual = DeceasedSettlor(
      bpMatchStatus = Some(FullyMatched),
      name = name,
      dateOfDeath = None,
      dateOfBirth = None,
      nationality = None,
      countryOfResidence = None,
      identification = None,
      address = None
    )

    val result = extractor(emptyUserAnswers, individual, None, hasAdditionalSettlors = Some(false)).get

    result.get(BpMatchStatusPage).get mustBe FullyMatched
    result.get(NamePage).get mustBe name
    result.get(DateOfBirthYesNoPage).get mustBe false
    result.get(DateOfBirthPage) mustNot be(defined)
    result.get(NationalInsuranceNumberYesNoPage).get mustBe false
    result.get(NationalInsuranceNumberPage) mustNot be(defined)
    result.get(AddressYesNoPage).get mustBe false
    result.get(LivedInTheUkYesNoPage) mustNot be(defined)
    result.get(UkAddressPage) mustNot be(defined)
    result.get(NonUkAddressPage) mustNot be(defined)
  }

  val settlor: DeceasedSettlor = DeceasedSettlor(
    bpMatchStatus = None,
    name = name,
    dateOfDeath = None,
    dateOfBirth = None,
    nationality = None,
    countryOfResidence = None,
    identification = None,
    address = None
  )

  "there are no other settlors" in {

    val result = extractor(emptyUserAnswers, settlor, None, hasAdditionalSettlors = Some(false)).get

    result.get(AdditionalSettlorsYesNoPage).get mustBe false
  }

  "there are other settlors" in {

    val result = extractor(emptyUserAnswers, settlor, None, hasAdditionalSettlors = Some(true)).get

    result.get(AdditionalSettlorsYesNoPage) mustNot be(defined)
  }

  "there are no other settlors but the additional settlors question has been previously answered true" in {

    val result = extractor(
      emptyUserAnswers.set(AdditionalSettlorsYesNoPage, true).success.value,
      settlor,
      None,
      hasAdditionalSettlors = Some(false)
    ).get

    result.get(AdditionalSettlorsYesNoPage).get mustBe true
  }

  "there are no other settlors but the additional settlors question has been previously answered false" in {

    val result = extractor(
      emptyUserAnswers.set(AdditionalSettlorsYesNoPage, false).success.value,
      settlor,
      None,
      hasAdditionalSettlors = Some(false)
    ).get

    result.get(AdditionalSettlorsYesNoPage).get mustBe false
  }

  "there are other settlors but the additional settlors question has been previously answered true" in {

    val result = extractor(
      emptyUserAnswers.set(AdditionalSettlorsYesNoPage, true).success.value,
      settlor,
      None,
      hasAdditionalSettlors = Some(true)
    ).get

    result.get(AdditionalSettlorsYesNoPage).get mustBe true
  }

  "there are other settlors but the additional settlors question has been previously answered false" in {

    val result = extractor(
      emptyUserAnswers.set(AdditionalSettlorsYesNoPage, false).success.value,
      settlor,
      None,
      hasAdditionalSettlors = Some(true)
    ).get

    result.get(AdditionalSettlorsYesNoPage).get mustBe false
  }

  "should populate user answers when non-taxable" in {

    val individual = DeceasedSettlor(
      bpMatchStatus = None,
      name = name,
      dateOfDeath = None,
      dateOfBirth = None,
      nationality = None,
      countryOfResidence = None,
      identification = None,
      address = None
    )

    val result = extractor(emptyUserAnswers.copy(isTaxable = false), individual, None, hasAdditionalSettlors = Some(false)).get

    result.get(BpMatchStatusPage) mustNot be(defined)
    result.get(NamePage).get mustBe name
    result.get(DateOfDeathYesNoPage).get mustBe false
    result.get(DateOfDeathPage) mustNot be(defined)
    result.get(DateOfBirthYesNoPage).get mustBe false
    result.get(DateOfBirthPage) mustNot be(defined)
    result.get(NationalInsuranceNumberYesNoPage) mustNot be(defined)
    result.get(NationalInsuranceNumberPage) mustNot be(defined)
    result.get(AddressYesNoPage) mustNot be(defined)
    result.get(LivedInTheUkYesNoPage) mustNot be(defined)
    result.get(UkAddressPage) mustNot be(defined)
    result.get(NonUkAddressPage) mustNot be(defined)
  }

}
