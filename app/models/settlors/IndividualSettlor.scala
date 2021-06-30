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

import models.{Address, IndividualIdentification, Name, TypeOfTrust}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

final case class IndividualSettlor(name: Name,
                                   dateOfBirth: Option[LocalDate],
                                   countryOfNationality: Option[String],
                                   countryOfResidence: Option[String],
                                   identification: Option[IndividualIdentification],
                                   address: Option[Address],
                                   mentalCapacityYesNo: Option[Boolean],
                                   entityStart: LocalDate,
                                   provisional: Boolean) extends Settlor {

  override val startDate: Option[LocalDate] = Some(entityStart)
  override def hasRequiredData(migratingFromNonTaxableToTaxable: Boolean, trustType: Option[TypeOfTrust]): Boolean = true
}

object IndividualSettlor extends SettlorReads {

  implicit val reads: Reads[IndividualSettlor] = (
    (__ \ 'name).read[Name] and
      (__ \ 'dateOfBirth).readNullable[LocalDate] and
      (__ \ 'nationality).readNullable[String] and
      (__ \ 'countryOfResidence).readNullable[String] and
      __.lazyRead(readNullableAtSubPath[IndividualIdentification](__ \ 'identification)) and
      __.lazyRead(readNullableAtSubPath[Address](__ \ 'identification \ 'address)) and
      (__ \ 'legallyIncapable).readNullable[Boolean].map(_.map(!_)) and
      (__ \ "entityStart").read[LocalDate] and
      (__ \ "provisional").readWithDefault(false))
    .tupled.map{
    case (name, dob, countryOfNationality, countryOfResidence, nino, identification, mentalCapacity, entityStart, provisional) =>
      IndividualSettlor(name, dob, countryOfNationality, countryOfResidence, nino, identification, mentalCapacity, entityStart, provisional)
  }

  implicit val writes: Writes[IndividualSettlor] = (
    (__ \ 'name).write[Name] and
      (__ \ 'dateOfBirth).writeNullable[LocalDate] and
      (__ \ 'nationality).writeNullable[String] and
      (__ \ 'countryOfResidence).writeNullable[String] and
      (__ \ 'identification).writeNullable[IndividualIdentification] and
      (__ \ 'identification \ 'address).writeNullable[Address] and
      (__ \ 'legallyIncapable).writeNullable[Boolean](x => JsBoolean(!x)) and
      (__ \ "entityStart").write[LocalDate] and
      (__ \ "provisional").write[Boolean]
    ).apply(unlift(IndividualSettlor.unapply))

}
