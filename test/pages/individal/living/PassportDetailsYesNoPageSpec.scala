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

package pages.individal.living

import models.{IdCard, Passport}
import pages.behaviours.PageBehaviours
import pages.individual.living._

import java.time.LocalDate

class PassportDetailsYesNoPageSpec extends PageBehaviours {

  "PassportDetailsYesNoPage" must {

    beRetrievable[Boolean](PassportDetailsYesNoPage)

    beSettable[Boolean](PassportDetailsYesNoPage)

    beRemovable[Boolean](PassportDetailsYesNoPage)

    "implement cleanup logic when YES selected" in {
      val userAnswers = emptyUserAnswers
        .set(IdCardDetailsYesNoPage, true).success.value
        .set(IdCardDetailsPage, IdCard("FR", "num", LocalDate.parse("2020-01-01"))).success.value

      val result = userAnswers.set(PassportDetailsYesNoPage, true).success.value

      result.get(IdCardDetailsYesNoPage) mustNot be(defined)
      result.get(IdCardDetailsPage) mustNot be(defined)
    }

    "implement cleanup logic when NO selected" in {
      val userAnswers = emptyUserAnswers
        .set(PassportDetailsPage, Passport("FR", "num", LocalDate.parse("2020-01-01"))).success.value

      val result = userAnswers.set(PassportDetailsYesNoPage, false).success.value

      result.get(PassportDetailsPage) mustNot be(defined)
    }
  }
}
