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

package models.settlors

import models.Constant.MAX
import models.{SettlorType, TypeOfTrust}
import play.api.i18n.{Messages, MessagesProvider}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsSuccess, Reads, __}
import viewmodels.RadioOption

import java.time.LocalDate

trait Settlor {
  val startDate: Option[LocalDate]
  def hasRequiredData(migratingFromNonTaxableToTaxable: Boolean, trustType: Option[TypeOfTrust]): Boolean
}

case class Settlors(settlor: List[IndividualSettlor] = Nil,
                    settlorCompany: List[BusinessSettlor] = Nil,
                    deceased: Option[DeceasedSettlor] = None) {

  val size: Int = (settlor ++ settlorCompany ++ deceased).size

  val hasLivingSettlors: Boolean = settlor.nonEmpty || settlorCompany.nonEmpty

  def addToHeading()(implicit mp: MessagesProvider): String = {

    size match {
      case c if c > 1 => Messages("addASettlor.count.heading", c)
      case _ => Messages("addASettlor.heading")
    }
  }

  private val options: List[(Int, SettlorType)] = {
    (settlor.size, SettlorType.IndividualSettlor) ::
      (settlorCompany.size, SettlorType.BusinessSettlor) ::
      Nil
  }

  val nonMaxedOutOptions: List[RadioOption] = {
    options.filter(x => x._1 < MAX).map {
      x => RadioOption(SettlorType.prefix, x._2.toString)
    }
  }

  val maxedOutOptions: List[RadioOption] = {
    options.filter(x => x._1 >= MAX).map {
      x => RadioOption(SettlorType.prefix, x._2.toString)
    }
  }

}

object Settlors {
  implicit val reads: Reads[Settlors] = (
    (__ \ "settlors" \ "settlor").readWithDefault[List[IndividualSettlor]](Nil)
      and (__ \ "settlors" \ "settlorCompany").readWithDefault[List[BusinessSettlor]](Nil)
      and (__ \ "settlors" \ "deceased").readNullable[DeceasedSettlor]
    ).apply(Settlors.apply _)
}

trait SettlorReads {
  def readNullableAtSubPath[T: Reads](subPath: JsPath): Reads[Option[T]] = Reads (
    _.transform(subPath.json.pick)
      .flatMap(_.validate[T])
      .map(Some(_))
      .recoverWith(_ => JsSuccess(None))
  )
}
