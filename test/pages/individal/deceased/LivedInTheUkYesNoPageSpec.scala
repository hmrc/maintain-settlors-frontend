/*
 * Copyright 2026 HM Revenue & Customs
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

package pages.individal.deceased

import models.UkAddress
import pages.behaviours.PageBehaviours
import pages.individual.deceased.{AddressYesNoPage, LivedInTheUkYesNoPage, UkAddressPage}

class LivedInTheUkYesNoPageSpec extends PageBehaviours {

  "LivedInTheUkYesNoPage" must {

    beRetrievable[Boolean](LivedInTheUkYesNoPage)

    beSettable[Boolean](LivedInTheUkYesNoPage)

    beRemovable[Boolean](LivedInTheUkYesNoPage)

    "implement cleanup logic when NO selected" in {
      val userAnswers = emptyUserAnswers
        .set(UkAddressPage, UkAddress("line1", "line2", None, None, "postcode"))
        .success
        .value
        .set(LivedInTheUkYesNoPage, true)
        .flatMap(_.set(AddressYesNoPage, false))

      userAnswers.get.get(UkAddressPage) mustNot be(defined)
      userAnswers.get.get(LivedInTheUkYesNoPage) mustNot be(defined)
    }
  }

}
