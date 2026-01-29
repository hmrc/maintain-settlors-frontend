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

package pages.individal.living

import models.CombinedPassportOrIdCard
import pages.behaviours.PageBehaviours
import pages.individual.living.{PassportOrIdCardDetailsPage, PassportOrIdCardDetailsYesNoPage}

import java.time.LocalDate

class PassportOrIdCardDetailsYesNoPageSpec extends PageBehaviours {

  "PassportOrIdCardDetailsYesNoPage" must {

    beRetrievable[Boolean](PassportOrIdCardDetailsYesNoPage)

    beSettable[Boolean](PassportOrIdCardDetailsYesNoPage)

    beRemovable[Boolean](PassportOrIdCardDetailsYesNoPage)

    "implement cleanup logic when NO selected" in {
      val userAnswers = emptyUserAnswers
        .set(PassportOrIdCardDetailsPage, CombinedPassportOrIdCard("FR", "num", LocalDate.parse("2020-01-01")))
        .success
        .value

      val result = userAnswers.set(PassportOrIdCardDetailsYesNoPage, false).success.value

      result.get(PassportOrIdCardDetailsPage) mustNot be(defined)
    }
  }

}
