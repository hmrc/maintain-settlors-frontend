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

package models

import play.api.libs.json.{JsString, Writes}
import viewmodels.RadioOption

sealed trait SettlorType

object SettlorType extends Enumerable.Implicits {

  val prefix = "whatTypeOfSettlor"

  case object IndividualSettlor extends WithName("individual") with SettlorType
  case object BusinessSettlor extends WithName("business") with SettlorType

  val values: List[SettlorType] = List(
    IndividualSettlor, BusinessSettlor
  )

  val options: List[RadioOption] = values.map {
    value =>
      RadioOption(prefix, value.toString)
  }

  implicit val enumerable: Enumerable[SettlorType] =
    Enumerable(values.map(v => v.toString -> v): _*)

  val writesToTrusts: Writes[SettlorType] = Writes {
    case IndividualSettlor => JsString("settlor")
    case BusinessSettlor => JsString("settlorCompany")
  }
}
