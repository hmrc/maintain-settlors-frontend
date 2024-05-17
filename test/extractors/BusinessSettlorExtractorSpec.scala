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
import models.Constant.GB
import models.settlors.BusinessSettlor
import models.{UkAddress, UserAnswers}
import pages.business._

import java.time.LocalDate

class BusinessSettlorExtractorSpec extends SpecBase {

  private val index = 0

  private val name = "Name"
  private val utr = "1234567890"
  private val date = LocalDate.parse("1996-02-03")
  private val address = UkAddress("Line 1", "Line 2", None, None, "postcode")

  private val extractor = new BusinessSettlorExtractor()

  "BusinessSettlorExtractor" must {

    "Populate user answers" when {

        "taxable" when {
          "underlying trust data is 4mld" when {
            val baseAnswers: UserAnswers = emptyUserAnswers.copy(isTaxable = true, isUnderlyingData5mld = false)
            "has no country of residence and no address" in {

              val business = BusinessSettlor(
                name = name,
                companyType = None,
                companyTime = None,
                utr = None,
                address = None,
                entityStart = date,
                provisional = true
              )

              val result = extractor.apply(baseAnswers, business, Some(index)).get

              result.get(IndexPage).get mustBe index
              result.get(NamePage).get mustBe name
              result.get(UtrYesNoPage).get mustBe false
              result.get(UtrPage) mustBe None
              result.get(CountryOfResidenceYesNoPage) mustBe None
              result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
              result.get(CountryOfResidencePage) mustBe None
              result.get(AddressYesNoPage).get mustBe false
              result.get(LiveInTheUkYesNoPage) mustBe None
              result.get(UkAddressPage) mustBe None
              result.get(NonUkAddressPage) mustBe None
              result.get(StartDatePage).get mustBe date
            }

            "has no country of residence but does have an address" in {
              val business = BusinessSettlor(
                name = name,
                companyType = None,
                companyTime = None,
                utr = None,
                countryOfResidence = None,
                address = Some(address),
                entityStart = date,
                provisional = true
              )

              val result = extractor.apply(baseAnswers, business, Some(index)).get

              result.get(IndexPage).get mustBe index
              result.get(NamePage).get mustBe name
              result.get(UtrYesNoPage).get mustBe false
              result.get(UtrPage) mustBe None
              result.get(CountryOfResidenceYesNoPage) mustBe None
              result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
              result.get(CountryOfResidencePage) mustBe None
              result.get(AddressYesNoPage).get mustBe true
              result.get(LiveInTheUkYesNoPage).get mustBe true
              result.get(UkAddressPage).get mustBe address
              result.get(NonUkAddressPage) mustBe None
              result.get(StartDatePage).get mustBe date
            }
          }

          "underlying trust data is 5mld" when {
            val baseAnswers: UserAnswers = emptyUserAnswers.copy(isTaxable = true, isUnderlyingData5mld = true)

            "has a UTR" in {

              val business = BusinessSettlor(
                name = name,
                companyType = None,
                companyTime = None,
                utr = Some(utr),
                countryOfResidence = None,
                address = None,
                entityStart = date,
                provisional = true
              )

              val result = extractor(baseAnswers, business, Some(index)).get

              result.get(IndexPage).get mustBe index
              result.get(NamePage).get mustBe name
              result.get(UtrYesNoPage).get mustBe true
              result.get(UtrPage).get mustBe utr
              result.get(CountryOfResidenceYesNoPage).get mustBe false
              result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
              result.get(CountryOfResidencePage) mustBe None
              result.get(AddressYesNoPage) mustBe None
              result.get(LiveInTheUkYesNoPage) mustBe None
              result.get(UkAddressPage) mustBe None
              result.get(NonUkAddressPage) mustBe None
              result.get(StartDatePage).get mustBe date
            }

            "has no country of residence and no address" in {
              val business = BusinessSettlor(
                name = name,
                companyType = None,
                companyTime = None,
                utr = None,
                countryOfResidence = None,
                address = None,
                entityStart = date,
                provisional = true
              )

              val result = extractor.apply(baseAnswers, business, Some(index)).get

              result.get(IndexPage).get mustBe index
              result.get(NamePage).get mustBe name
              result.get(UtrYesNoPage).get mustBe false
              result.get(UtrPage) mustBe None
              result.get(CountryOfResidenceYesNoPage).get mustBe false
              result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
              result.get(CountryOfResidencePage) mustBe None
              result.get(AddressYesNoPage).get mustBe false
              result.get(LiveInTheUkYesNoPage) mustBe None
              result.get(UkAddressPage) mustBe None
              result.get(NonUkAddressPage) mustBe None
              result.get(StartDatePage).get mustBe date
            }

            "has no country of residence but does have an address" in {
              val business = BusinessSettlor(
                name = name,
                companyType = None,
                companyTime = None,
                utr = None,
                countryOfResidence = None,
                address = Some(address),
                entityStart = date,
                provisional = true
              )

              val result = extractor.apply(baseAnswers, business, Some(index)).get

              result.get(IndexPage).get mustBe index
              result.get(NamePage).get mustBe name
              result.get(UtrYesNoPage).get mustBe false
              result.get(UtrPage) mustBe None
              result.get(CountryOfResidenceYesNoPage).get mustBe false
              result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
              result.get(CountryOfResidencePage) mustBe None
              result.get(AddressYesNoPage).get mustBe true
              result.get(LiveInTheUkYesNoPage).get mustBe true
              result.get(UkAddressPage).get mustBe address
              result.get(NonUkAddressPage) mustBe None
              result.get(StartDatePage).get mustBe date
            }

            "has a country of residence in GB" in {
              val business = BusinessSettlor(
                name = name,
                companyType = None,
                companyTime = None,
                utr = None,
                countryOfResidence = Some(GB),
                address = None,
                entityStart = date,
                provisional = true
              )

              val result = extractor.apply(baseAnswers, business, Some(index)).get

              result.get(IndexPage).get mustBe index
              result.get(NamePage).get mustBe name
              result.get(UtrYesNoPage).get mustBe false
              result.get(UtrPage) mustBe None
              result.get(CountryOfResidenceYesNoPage).get mustBe true
              result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe true
              result.get(CountryOfResidencePage).get mustBe GB
              result.get(AddressYesNoPage).get mustBe false
              result.get(LiveInTheUkYesNoPage) mustBe None
              result.get(UkAddressPage) mustBe None
              result.get(NonUkAddressPage) mustBe None
              result.get(StartDatePage).get mustBe date
            }

            "has a country of residence in Spain" in {
              val business = BusinessSettlor(
                name = name,
                companyType = None,
                companyTime = None,
                utr = None,
                countryOfResidence = Some("Spain"),
                address = None,
                entityStart = date,
                provisional = true
              )

              val result = extractor.apply(baseAnswers, business, Some(index)).get

              result.get(IndexPage).get mustBe index
              result.get(NamePage).get mustBe name
              result.get(UtrYesNoPage).get mustBe false
              result.get(UtrPage) mustBe None
              result.get(CountryOfResidenceYesNoPage).get mustBe true
              result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe false
              result.get(CountryOfResidencePage).get mustBe "Spain"
              result.get(AddressYesNoPage).get mustBe false
              result.get(LiveInTheUkYesNoPage) mustBe None
              result.get(UkAddressPage) mustBe None
              result.get(NonUkAddressPage) mustBe None
              result.get(StartDatePage).get mustBe date
            }
          }
        }

        "non taxable" when {
          val baseAnswers: UserAnswers = emptyUserAnswers.copy(isTaxable = false, isUnderlyingData5mld = true)

          "has a UTR" in {

            val business = BusinessSettlor(
              name = name,
              companyType = None,
              companyTime = None,
              utr = Some(utr),
              countryOfResidence = None,
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor(baseAnswers, business, Some(index)).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(UtrYesNoPage) mustBe None
            result.get(UtrPage) mustBe None
            result.get(CountryOfResidenceYesNoPage).get mustBe false
            result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
            result.get(CountryOfResidencePage) mustBe None
            result.get(AddressYesNoPage) mustBe None
            result.get(LiveInTheUkYesNoPage) mustBe None
            result.get(UkAddressPage) mustBe None
            result.get(NonUkAddressPage) mustBe None
            result.get(StartDatePage).get mustBe date
          }

          "has no country of residence" in {
            val business = BusinessSettlor(
              name = name,
              companyType = None,
              companyTime = None,
              utr = None,
              countryOfResidence = None,
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.apply(baseAnswers, business, Some(index)).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(UtrYesNoPage) mustBe None
            result.get(UtrPage) mustBe None
            result.get(CountryOfResidenceYesNoPage).get mustBe false
            result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
            result.get(CountryOfResidencePage) mustBe None
            result.get(AddressYesNoPage) mustBe None
            result.get(LiveInTheUkYesNoPage) mustBe None
            result.get(UkAddressPage) mustBe None
            result.get(NonUkAddressPage) mustBe None
            result.get(StartDatePage).get mustBe date
          }

          "has a country of residence in GB" in {
            val business = BusinessSettlor(
              name = name,
              companyType = None,
              companyTime = None,
              utr = None,
              countryOfResidence = Some(GB),
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.apply(baseAnswers, business, Some(index)).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(UtrYesNoPage) mustBe None
            result.get(UtrPage) mustBe None
            result.get(CountryOfResidenceYesNoPage).get mustBe true
            result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe true
            result.get(CountryOfResidencePage).get mustBe GB
            result.get(AddressYesNoPage) mustBe None
            result.get(LiveInTheUkYesNoPage) mustBe None
            result.get(UkAddressPage) mustBe None
            result.get(NonUkAddressPage) mustBe None
            result.get(StartDatePage).get mustBe date
          }
        }
    }
  }

}
