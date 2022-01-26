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

package models

import java.time.LocalDate

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

case class RemoveSettlor(`type`: SettlorType, index : Int, endDate: LocalDate)

object RemoveSettlor {

  implicit val writes : Writes[RemoveSettlor] =
    (
      (JsPath \ "type").write[SettlorType](SettlorType.writesToTrusts) and
      (JsPath \ "index").write[Int] and
      (JsPath \ "endDate").write[LocalDate]
    ).apply(unlift(RemoveSettlor.unapply))

  def apply(`type`: SettlorType, index: Int): RemoveSettlor =  RemoveSettlor(`type`, index, LocalDate.now)

}
