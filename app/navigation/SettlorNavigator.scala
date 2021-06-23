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

import models.Constant.MAX
import models.NormalMode
import models.settlors.Settlors
import play.api.mvc.Call

class SettlorNavigator {

  def addSettlorRoute(settlors: Settlors): Call = {
    settlors match {
      case Settlors(individuals, _, _) if individuals.size >= MAX =>
        controllers.business.routes.NameController.onPageLoad(NormalMode)
      case Settlors(_, businesses, _) if businesses.size >= MAX =>
        controllers.individual.living.routes.NameController.onPageLoad(NormalMode)
      case _ =>
        controllers.routes.AddNowController.onPageLoad()
    }
  }
}
