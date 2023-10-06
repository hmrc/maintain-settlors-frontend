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

package utils.mappers

import models.settlors.DeceasedSettlor
import models.{BpMatchStatus, IndividualIdentification, Name, NationalInsuranceNumber, NonUkAddress, UkAddress}
import pages.QuestionPage
import pages.individual.deceased._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsSuccess, Reads}

import java.time.LocalDate

class DeceasedSettlorMapper extends SettlorMapper[DeceasedSettlor] {

  override val reads: Reads[DeceasedSettlor] = (
    BpMatchStatusPage.path.readNullable[BpMatchStatus] and
      NamePage.path.read[Name] and
      DateOfBirthPage.path.readNullable[LocalDate] and
      DateOfDeathPage.path.readNullable[LocalDate] and
      CountryOfNationalityPage.path.readNullable[String] and
      CountryOfResidencePage.path.readNullable[String] and
      readIdentification and
      readAddress
    )(DeceasedSettlor.apply _)

  private def readIdentification: Reads[Option[IndividualIdentification]] = {
    NationalInsuranceNumberYesNoPage.path.readNullable[Boolean].flatMap[Option[IndividualIdentification]] {
      case Some(true) => NationalInsuranceNumberPage.path.read[String].map(nino => Some(NationalInsuranceNumber(nino)))
      case _ => Reads(_ => JsSuccess(None))
    }
  }

  override def ukAddressYesNoPage: QuestionPage[Boolean] = LivedInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

}
