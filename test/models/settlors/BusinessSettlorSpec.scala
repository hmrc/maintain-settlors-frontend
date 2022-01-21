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

package models.settlors

import java.time.LocalDate

import models.CompanyType.Investment
import models.{NonUkAddress, UkAddress}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json

class BusinessSettlorSpec extends WordSpec with MustMatchers {

  "BusinessSettlor" must {
    "deserialise from backend JSON" when {

      "there is a UK address" in {
        val json = Json.parse(
          """
            |{
            |  "lineNo": "1",
            |  "name": "Nelson Ltd ",
            |  "identification": {
            |    "safeId": "2222200000000",
            |    "utr": "12345678",
            |    "address": {
            |      "line1": "Suite 10",
            |      "line2": "Wealthy Arena",
            |      "line3": "Trafagar Square",
            |      "line4": "London",
            |      "postCode": "SE2 2HB",
            |      "country": "GB"
            |    }
            |  },
            |  "entityStart": "2017-02-28",
            |  "provisional": false
            |}
            |""".stripMargin)

        val businessSettlor = json.as[BusinessSettlor]
        businessSettlor mustBe BusinessSettlor(
          name = "Nelson Ltd ",
          companyType = None,
          companyTime = None,
          utr = Some("12345678"),
          countryOfResidence = None,
          address = Some(UkAddress(
            line1 = "Suite 10",
            line2 = "Wealthy Arena",
            line3 = Some("Trafagar Square"),
            line4 = Some("London"),
            postcode = "SE2 2HB"
          )),
          entityStart = LocalDate.of(2017, 2, 28),
          provisional = false
        )
      }

      "there is a non-UK address" in {
        val json = Json.parse(
          """
            |{
            |  "lineNo": "1",
            |  "name": "Nelson Ltd ",
            |  "identification": {
            |    "safeId": "2222200000000",
            |    "address": {
            |      "line1": "Suite 10",
            |      "line2": "Wealthy Arena",
            |      "line3": "Paris",
            |      "country": "FR"
            |    }
            |  },
            |  "entityStart": "2017-02-28",
            |  "provisional": false
            |}
            |""".stripMargin)

        val businessSettlor = json.as[BusinessSettlor]
        businessSettlor mustBe BusinessSettlor(
          name = "Nelson Ltd ",
          companyType = None,
          companyTime = None,
          utr = None,
          countryOfResidence = None,
          address = Some(NonUkAddress(
            line1 = "Suite 10",
            line2 = "Wealthy Arena",
            line3 = Some("Paris"),
            country = "FR"
          )),
          entityStart = LocalDate.of(2017, 2, 28),
          provisional = false
        )
      }

      "there is a companyTime and Type" in {
        val json = Json.parse(
          """
            |{
            |  "lineNo": "1",
            |  "name": "Nelson Ltd ",
            |  "companyType" : "Investment",
            |  "companyTime" : false,
            |  "identification": {
            |    "safeId": "2222200000000"
            |  },
            |  "entityStart": "2017-02-28",
            |  "provisional": false
            |}
            |""".stripMargin)

        val businessSettlor = json.as[BusinessSettlor]
        businessSettlor mustBe BusinessSettlor(
          name = "Nelson Ltd ",
          companyType = Some(Investment),
          companyTime = Some(false),
          utr = None,
          countryOfResidence = None,
          address = None,
          entityStart = LocalDate.of(2017, 2, 28),
          provisional = false
        )
      }

      "there is a country of residence" in {
        val json = Json.parse(
          """
            |{
            |  "lineNo": "1",
            |  "name": "Nelson Ltd ",
            |  "beneficiaryDiscretion": true,
            |  "identification": {
            |    "safeId": "2222200000000"
            |  },
            |  "countryOfResidence": "GB",
            |  "entityStart": "2017-02-28",
            |  "provisional": false
            |}
            |""".stripMargin)

        val businessSettlor = json.as[BusinessSettlor]
        businessSettlor mustBe BusinessSettlor(
          name = "Nelson Ltd ",
          companyType = None,
          companyTime = None,
          utr = None,
          countryOfResidence = Some("GB"),
          address = None,
          entityStart = LocalDate.of(2017, 2, 28),
          provisional = false
        )
      }
    }
  }
}
