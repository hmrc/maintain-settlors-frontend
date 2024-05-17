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

package utils.mappers

import models.settlors.BusinessSettlor
import models.{CompanyType, NonUkAddress, UkAddress}
import pages.QuestionPage
import pages.business._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsSuccess, Reads}

import java.time.LocalDate

class BusinessSettlorMapper extends SettlorMapper[BusinessSettlor] {

  override val reads: Reads[BusinessSettlor] = (
    NamePage.path.read[String] and
      CompanyTypePage.path.readNullable[CompanyType] and
      CompanyTimePage.path.readNullable[Boolean] and
      UtrPage.path.readNullable[String] and
      readCountryOfResidence and
      readAddress and
      StartDatePage.path.read[LocalDate] and
      Reads(_ => JsSuccess(true))
    )(BusinessSettlor.apply _)

  override def ukAddressYesNoPage: QuestionPage[Boolean] = LiveInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def countryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceYesNoPage
  override def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceInTheUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage

}
