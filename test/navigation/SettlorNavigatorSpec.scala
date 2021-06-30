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
import models.Constant.MAX
import models.settlors.{BusinessSettlor, IndividualSettlor, Settlors}
import models.{Name, NormalMode, SettlorType}

import java.time.LocalDate

class SettlorNavigatorSpec extends SpecBase {

  private val navigator: SettlorNavigator = injector.instanceOf[SettlorNavigator]

  "SettlorNavigator" when {

    ".addSettlorRoute" when {

      "individuals maxed out" must {
        "redirect to business name page" in {

          val settlor = IndividualSettlor(
            name = Name(firstName = "Joe", middleName = None, lastName = "Bloggs"),
            dateOfBirth = None,
            countryOfNationality = None,
            countryOfResidence = None,
            identification = None,
            address = None,
            mentalCapacityYesNo = None,
            entityStart = LocalDate.parse("2020-01-01"),
            provisional = false
          )

          val settlors = Settlors(List.fill(MAX)(settlor), Nil, None)

          navigator.addSettlorRoute(settlors).url mustBe
            controllers.business.routes.NameController.onPageLoad(NormalMode).url
        }
      }

      "businesses maxed out" must {
        "redirect to individual name page" in {

          val settlor = BusinessSettlor(
            name = "Amazon",
            companyType = None,
            companyTime = None,
            utr = None,
            address = None,
            entityStart = LocalDate.parse("2020-01-01"),
            provisional = false
          )

          val settlors = Settlors(Nil, List.fill(MAX)(settlor), None)

          navigator.addSettlorRoute(settlors).url mustBe
            controllers.individual.living.routes.NameController.onPageLoad(NormalMode).url
        }
      }

      "neither maxed out" must {
        "redirect to add now page" in {

          val settlors = Settlors(Nil, Nil, None)

          navigator.addSettlorRoute(settlors).url mustBe
            controllers.routes.AddNowController.onPageLoad().url
        }
      }
    }

    ".addSettlorNowRoute" when {

      "individual" must {
        "redirect to individual name page" in {

          navigator.addSettlorNowRoute(SettlorType.IndividualSettlor).url mustBe
            controllers.individual.living.routes.NameController.onPageLoad(NormalMode).url
        }
      }

      "business" must {
        "redirect to business name page" in {

          navigator.addSettlorNowRoute(SettlorType.BusinessSettlor).url mustBe
            controllers.business.routes.NameController.onPageLoad(NormalMode).url
        }
      }
    }
  }

}
