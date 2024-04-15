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
                                 nationality: Option[String],
                                 countryOfResidence: Option[String],
                                 identification: Option[IndividualIdentification],
                                 address: Option[Address]) extends Settlor {

  override val startDate: Option[LocalDate] = None
  override def hasRequiredData(migratingFromNonTaxableToTaxable: Boolean, trustType: Option[TypeOfTrust]): Boolean = true
}

object DeceasedSettlor extends SettlorReads {

  implicit val reads: Reads[DeceasedSettlor] = (
    (__ \ Symbol("bpMatchStatus")).readNullable[BpMatchStatus] and
      (__ \ Symbol("name")).read[Name] and
      (__ \ Symbol("dateOfBirth")).readNullable[LocalDate] and
      (__ \ Symbol("dateOfDeath")).readNullable[LocalDate] and
      (__ \ Symbol("nationality")).readNullable[String] and
      (__ \ Symbol("countryOfResidence")).readNullable[String] and
      __.lazyRead(readNullableAtSubPath[IndividualIdentification](__ \ Symbol("identification"))) and
      __.lazyRead(readNullableAtSubPath[Address](__ \ Symbol("identification") \ Symbol("address"))))
    .tupled.map{
    case (bpMatchStatus, name, dob, dod, nationality, countryOfResidence, nino, identification) =>
      DeceasedSettlor(bpMatchStatus, name, dob, dod, nationality, countryOfResidence, nino, identification)
  }

  implicit val writes: Writes[DeceasedSettlor] = (
    (__ \ Symbol("bpMatchStatus")).writeNullable[BpMatchStatus] and
      (__ \ Symbol("name")).write[Name] and
      (__ \ Symbol("dateOfBirth")).writeNullable[LocalDate] and
      (__ \ Symbol("dateOfDeath")).writeNullable[LocalDate] and
      (__ \ Symbol("nationality")).writeNullable[String] and
      (__ \ Symbol("countryOfResidence")).writeNullable[String] and
      (__ \ Symbol("identification")).writeNullable[IndividualIdentification] and
      (__ \ Symbol("identification") \ Symbol("address")).writeNullable[Address]
    ).apply(settlor => (
    None, // bpMatchStatus shouldn't be written to the backend
    settlor.name,
    settlor.dateOfBirth,
    settlor.dateOfDeath,
    settlor.nationality,
    settlor.countryOfResidence,
    settlor.identification,
    settlor.address
  ))

}
