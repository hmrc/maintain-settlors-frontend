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

package models.settlors

import models.{Address, IndividualIdentification, Name, TypeOfTrust, YesNoDontKnow}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

final case class IndividualSettlor(
  name: Name,
  dateOfBirth: Option[LocalDate],
  countryOfNationality: Option[String],
  countryOfResidence: Option[String],
  identification: Option[IndividualIdentification],
  address: Option[Address],
  mentalCapacityYesNo: Option[YesNoDontKnow],
  entityStart: LocalDate,
  provisional: Boolean
) extends Settlor {

  override val startDate: Option[LocalDate] = Some(entityStart)

  override def hasRequiredData(migratingFromNonTaxableToTaxable: Boolean, trustType: Option[TypeOfTrust]): Boolean =
    true

}

object IndividualSettlor extends SettlorReads {

  implicit val reads: Reads[IndividualSettlor] = ((__ \ Symbol("name")).read[Name] and
    (__ \ Symbol("dateOfBirth")).readNullable[LocalDate] and
    (__ \ Symbol("nationality")).readNullable[String] and
    (__ \ Symbol("countryOfResidence")).readNullable[String] and
    __.lazyRead(readNullableAtSubPath[IndividualIdentification](__ \ Symbol("identification"))) and
    __.lazyRead(readNullableAtSubPath[Address](__ \ Symbol("identification") \ Symbol("address"))) and
    readMentalCapacity and
    (__ \ "entityStart").read[LocalDate] and
    (__ \ "provisional").readWithDefault(false)).tupled.map {
    case (
          name,
          dob,
          countryOfNationality,
          countryOfResidence,
          nino,
          identification,
          mentalCapacity,
          entityStart,
          provisional
        ) =>
      IndividualSettlor(
        name,
        dob,
        countryOfNationality,
        countryOfResidence,
        nino,
        identification,
        mentalCapacity,
        entityStart,
        provisional
      )
  }

  implicit val writes: Writes[IndividualSettlor] = (
    (__ \ Symbol("name")).write[Name] and
      (__ \ Symbol("dateOfBirth")).writeNullable[LocalDate] and
      (__ \ Symbol("nationality")).writeNullable[String] and
      (__ \ Symbol("countryOfResidence")).writeNullable[String] and
      (__ \ Symbol("identification")).writeNullable[IndividualIdentification] and
      (__ \ Symbol("identification") \ Symbol("address")).writeNullable[Address] and
      (__ \ Symbol("legallyIncapable")).writeNullable[YesNoDontKnow](writeMentalCapacity) and
      (__ \ "entityStart").write[LocalDate] and
      (__ \ "provisional").write[Boolean]
  ).apply(unlift(IndividualSettlor.unapply))

  def readMentalCapacity: Reads[Option[YesNoDontKnow]] =
    (__ \ Symbol("legallyIncapable")).readNullable[Boolean].flatMap[Option[YesNoDontKnow]] { x: Option[Boolean] =>
      Reads(_ => JsSuccess(YesNoDontKnow.fromBoolean(x)))
    }

  private def writeMentalCapacity: Writes[YesNoDontKnow] = {
    case YesNoDontKnow.Yes => JsBoolean(false)
    case YesNoDontKnow.No  => JsBoolean(true)
    case _                 => JsNull
  }

}
