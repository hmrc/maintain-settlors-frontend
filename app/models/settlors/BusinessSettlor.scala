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

import models.TypeOfTrust.EmployeeRelated
import models.{Address, CompanyType, TypeOfTrust}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

final case class BusinessSettlor(name: String,
                                 companyType: Option[CompanyType],
                                 companyTime: Option[Boolean],
                                 utr: Option[String],
                                 countryOfResidence: Option[String] = None,
                                 address: Option[Address],
                                 entityStart: LocalDate,
                                 provisional: Boolean) extends Settlor {

  override val startDate: Option[LocalDate] = Some(entityStart)
  override def hasRequiredData(migratingFromNonTaxableToTaxable: Boolean, trustType: Option[TypeOfTrust]): Boolean = {
    if (migratingFromNonTaxableToTaxable) {
      trustType match {
        case Some(EmployeeRelated) => companyType.isDefined && companyTime.isDefined
        case _ => true
      }
    } else {
      true
    }
  }
}

object BusinessSettlor extends SettlorReads {

  implicit val reads: Reads[BusinessSettlor] = (
    (__ \ Symbol("name")).read[String] and
      (__ \ Symbol("companyType")).readNullable[CompanyType] and
      (__ \ Symbol("companyTime")).readNullable[Boolean] and
      __.lazyRead(readNullableAtSubPath[String](__ \ Symbol("identification") \  Symbol("utr"))) and
      (__ \ Symbol("countryOfResidence")).readNullable[String] and
      __.lazyRead(readNullableAtSubPath[Address](__ \ Symbol("identification") \ Symbol("address"))) and
      (__ \ "entityStart").read[LocalDate] and
      (__ \ "provisional").readWithDefault(false))
    .tupled.map {
    case (name, companyType, companyTime, identifier, countryOfResidence, address, entityStart, provisional) =>
      BusinessSettlor(name, companyType, companyTime, identifier, countryOfResidence, address, entityStart, provisional)
  }

  implicit val writes: Writes[BusinessSettlor] = (
    (__ \ Symbol("name")).write[String] and
      (__ \ Symbol("companyType")).writeNullable[CompanyType] and
      (__ \ Symbol("companyTime")).writeNullable[Boolean] and
      (__ \ Symbol("identification") \ Symbol("utr")).writeNullable[String] and
      (__ \ Symbol("countryOfResidence")).writeNullable[String] and
      (__ \ Symbol("identification") \ Symbol("address")).writeNullable[Address] and
      (__ \ "entityStart").write[LocalDate] and
      (__ \ "provisional").write[Boolean]
    ).apply(unlift(BusinessSettlor.unapply))

}
