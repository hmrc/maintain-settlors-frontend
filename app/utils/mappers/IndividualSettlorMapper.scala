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

package utils.mappers

import models._
import models.settlors.IndividualSettlor
import pages.QuestionPage
import pages.individual.living._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsSuccess, Reads}

import java.time.LocalDate

class IndividualSettlorMapper extends SettlorMapper[IndividualSettlor] {

  override val reads: Reads[IndividualSettlor] = (
    NamePage.path.read[Name] and
      DateOfBirthPage.path.readNullable[LocalDate] and
      readCountryOfNationality and
      readCountryOfResidence and
      readIdentification and
      readAddress and
      readMentalCapacity and
      StartDatePage.path.read[LocalDate] and
      Reads(_ => JsSuccess(true))
    )(IndividualSettlor.apply _)

  private def readIdentification: Reads[Option[IndividualIdentification]] = {
    NationalInsuranceNumberYesNoPage.path.readNullable[Boolean].flatMap[Option[IndividualIdentification]] {
      case Some(true) => NationalInsuranceNumberPage.path.read[String].map(nino => Some(NationalInsuranceNumber(nino)))
      case Some(false) => readPassportOrIdCard
      case _ => Reads(_ => JsSuccess(None))
    }
  }

  private def readPassportOrIdCard: Reads[Option[IndividualIdentification]] = {
    val identification = for {
      hasNino <- NationalInsuranceNumberYesNoPage.path.readWithDefault(false)
      hasAddress <- AddressYesNoPage.path.readWithDefault(false)
      hasPassport <- PassportDetailsYesNoPage.path.readWithDefault(false)
      hasIdCard <- IdCardDetailsYesNoPage.path.readWithDefault(false)
      hasPassportOrIdCard <- PassportOrIdCardDetailsYesNoPage.path.readWithDefault(false)
    } yield (hasNino, hasAddress, hasPassport, hasIdCard, hasPassportOrIdCard)

    identification.flatMap[Option[IndividualIdentification]] {
      case (false, true, true, false, _) => PassportDetailsPage.path.read[Passport].map(x => x.asCombined).map(Some(_))
      case (false, true, false, true, _) => IdCardDetailsPage.path.read[IdCard].map(x => x.asCombined).map(Some(_))
      case (false, true, false, false, true) => PassportOrIdCardDetailsPage.path.read[CombinedPassportOrIdCard].map(Some(_))
      case _ => Reads(_ => JsSuccess(None))
    }
  }

  private def readMentalCapacity: Reads[Option[YesNoDontKnow]] = {
    MentalCapacityYesNoPage.path
      .readNullable[YesNoDontKnow]
      .flatMap[Option[YesNoDontKnow]] {
        case Some(value) => Reads(_ => JsSuccess(Some(value)))
        case _ => Reads(_ => JsSuccess(None))
      }
  }

  override def ukAddressYesNoPage: QuestionPage[Boolean] = LiveInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def countryOfNationalityYesNoPage: QuestionPage[Boolean] = CountryOfNationalityYesNoPage
  override def ukCountryOfNationalityYesNoPage: QuestionPage[Boolean] = CountryOfNationalityUkYesNoPage
  override def countryOfNationalityPage: QuestionPage[String] = CountryOfNationalityPage

  override def countryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceYesNoPage
  override def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage

}
