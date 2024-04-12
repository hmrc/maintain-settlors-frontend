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

import models.BpMatchStatus.FailedToMatch
import models.{Name, UkAddress}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

import java.time.LocalDate

class DeceasedSettlorSpec extends AnyWordSpec with Matchers {

  "DeceasedSettlor" must {
    "deserialise from JSON" when {

      "" in {
        val json = Json.parse(
          """
            |{
            |  "bpMatchStatus": "99",
            |  "name": {
            |     "firstName": "Jim",
            |     "lastName": "Bowen"
            |  },
            |  "dateOfBirth": "1937-08-20",
            |  "dateOfDeath": "2018-03-14",
            |  "nationality": "GB",
            |  "countryOfResidence": "GB",
            |  "identification": {
            |    "address": {
            |      "line1": "8 Gillison Close",
            |      "line2": "Melling",
            |      "line3": "Carnforth",
            |      "line4": "Lancashire",
            |      "postCode": "LA6 2RD",
            |      "country": "GB"
            |    }
            |  }
            |}
            |""".stripMargin)


        val expectedDeceasedSettlor = DeceasedSettlor(
          bpMatchStatus = Some(FailedToMatch),
          name = Name("Jim", None, "Bowen"),
          dateOfBirth = Some(LocalDate.of(1937, 8, 20)),
          dateOfDeath = Some(LocalDate.of(2018, 3, 14)),
          nationality = Some("GB"),
          countryOfResidence = Some("GB"),
          identification = None,
          address = Some(UkAddress("8 Gillison Close", "Melling", Some("Carnforth"), Some("Lancashire"), "LA6 2RD"))
        )

        val result = json.as[DeceasedSettlor]

        result mustBe expectedDeceasedSettlor
      }
    }
  }
}
