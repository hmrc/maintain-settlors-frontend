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

package models

import viewmodels.RadioOption

sealed trait CompanyType

object CompanyType extends Enumerable.Implicits {

  case object Trading extends WithName("Trading") with CompanyType
  case object Investment extends WithName("Investment") with CompanyType

  val values: List[CompanyType] = List(
    Trading, Investment
  )

  val options: List[RadioOption] = values.map {
    value =>
      RadioOption("companyType", value.toString)
  }

  implicit val enumerable: Enumerable[CompanyType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
