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

package models.settlors

import models.TypeOfTrust.HeritageTrust
import models.{CombinedPassportOrIdCard, DetailsType, Name, UkAddress, YesNoDontKnow}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDate

class IndividualSettlorSpec extends AnyWordSpec with Matchers {

  val jsonWithUndefinedOptionalFields: JsValue =
    Json.parse(
      """
        |{
        |  "name": {
        |     "firstName": "Transit",
        |     "lastName": "Morrison"
        |  },
        |  "entityStart": "2021-04-21",
        |  "provisional": true
        |}
        |""".stripMargin
    )

  "IndividualSettlor" must {

    "read and write JSON" when {

      "JSON has defined optional fields" in {
        val json = Json.parse(
          """
            |{
            |  "name": {
            |     "firstName": "Transit",
            |     "lastName": "Morrison"
            |  },
            |  "dateOfBirth": "1945-08-31",
            |  "nationality": "GB",
            |  "countryOfResidence": "GB",
            |  "identification": {
            |    "address": {
            |      "line1": "125 Hyndford Street",
            |      "line2": "Belfast",
            |      "postCode": "BT5 5EN",
            |      "country": "GB"
            |    },
            |    "passport": {
            |      "number": "987345987398457",
            |      "expirationDate": "2025-05-21",
            |      "countryOfIssue": "GB"
            |    }
            |  },
            |  "legallyIncapable": false,
            |  "entityStart": "2021-04-21",
            |  "provisional": true
            |}
            |""".stripMargin
        )

        val expectedIndividualSettlor = IndividualSettlor(
          name = Name("Transit", None, "Morrison"),
          dateOfBirth = Some(LocalDate.of(1945, 8, 31)),
          countryOfNationality = Some("GB"),
          countryOfResidence = Some("GB"),
          identification =
            Some(CombinedPassportOrIdCard("GB", "987345987398457", LocalDate.of(2025, 5, 21), DetailsType.Combined)),
          address = Some(UkAddress("125 Hyndford Street", "Belfast", None, None, "BT5 5EN")),
          mentalCapacityYesNo = Some(YesNoDontKnow.Yes),
          entityStart = LocalDate.of(2021, 4, 21),
          provisional = true
        )

        val result = json.as[IndividualSettlor]

        result mustBe expectedIndividualSettlor
      }

      "JSON has undefined optional fields" in {

        val expectedIndividualSettlor = IndividualSettlor(
          name = Name("Transit", None, "Morrison"),
          dateOfBirth = None,
          countryOfNationality = None,
          countryOfResidence = None,
          identification = None,
          address = None,
          mentalCapacityYesNo = Some(YesNoDontKnow.DontKnow),
          entityStart = LocalDate.of(2021, 4, 21),
          provisional = true
        )

        val result = jsonWithUndefinedOptionalFields.as[IndividualSettlor]

        result mustBe expectedIndividualSettlor
      }
    }

    "return expected startDate and results from hasRequiredData" when {

      "JSON has undefined optional fields" in {
        val result = jsonWithUndefinedOptionalFields.as[IndividualSettlor]

        result.startDate                                                                                  mustBe Some(LocalDate.of(2021, 4, 21))
        result.hasRequiredData(migratingFromNonTaxableToTaxable = true, trustType = None)                 mustBe true
        result.hasRequiredData(migratingFromNonTaxableToTaxable = false, trustType = None)                mustBe true
        result.hasRequiredData(migratingFromNonTaxableToTaxable = true, trustType = Some(HeritageTrust))  mustBe true
        result.hasRequiredData(migratingFromNonTaxableToTaxable = false, trustType = Some(HeritageTrust)) mustBe true
      }
    }

  }

}
