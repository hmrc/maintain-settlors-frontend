/*
 * Copyright 2023 HM Revenue & Customs
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

import models.{Address, BpMatchStatus, IndividualIdentification, Name, TypeOfTrust}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

final case class DeceasedSettlor(bpMatchStatus: Option[BpMatchStatus],
                                 name: Name,
                                 dateOfBirth: Option[LocalDate],
                                 dateOfDeath: Option[LocalDate],
                                 identification: Option[IndividualIdentification],
                                 address: Option[Address]) extends Settlor {

  override val startDate: Option[LocalDate] = None
  override def hasRequiredData(migratingFromNonTaxableToTaxable: Boolean, trustType: Option[TypeOfTrust]): Boolean = true
}

object DeceasedSettlor extends SettlorReads {

  implicit val reads: Reads[DeceasedSettlor] = (
    (__ \ 'bpMatchStatus).readNullable[BpMatchStatus] and
      (__ \ 'name).read[Name] and
      (__ \ 'dateOfBirth).readNullable[LocalDate] and
      (__ \ 'dateOfDeath).readNullable[LocalDate] and
      __.lazyRead(readNullableAtSubPath[IndividualIdentification](__ \ 'identification)) and
      __.lazyRead(readNullableAtSubPath[Address](__ \ 'identification \ 'address)))
    .tupled.map{
    case (bpMatchStatus, name, dob, dod, nino, identification) =>
      DeceasedSettlor(bpMatchStatus, name, dob, dod, nino, identification)
  }

  implicit val writes: Writes[DeceasedSettlor] = (
    (__ \ 'bpMatchStatus).writeNullable[BpMatchStatus] and
      (__ \ 'name).write[Name] and
      (__ \ 'dateOfBirth).writeNullable[LocalDate] and
      (__ \ 'dateOfDeath).writeNullable[LocalDate] and
      (__ \ 'identification).writeNullable[IndividualIdentification] and
      (__ \ 'identification \ 'address).writeNullable[Address]
    ).apply(settlor => (
    None,
    settlor.name,
    settlor.dateOfBirth,
    settlor.dateOfDeath,
    settlor.identification,
    settlor.address
  ))

}
