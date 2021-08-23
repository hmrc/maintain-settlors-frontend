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

package extractors

import models.settlors.IndividualSettlor
import models.{Address, CombinedPassportOrIdCard, IdCard, NationalInsuranceNumber, NonUkAddress, Passport, UkAddress, UserAnswers}
import pages.QuestionPage
import pages.individual.living._
import play.api.libs.json.JsPath

import java.time.LocalDate
import scala.util.{Success, Try}

class IndividualSettlorExtractor extends SettlorExtractor[IndividualSettlor] {

  override def apply(answers: UserAnswers,
                     individual: IndividualSettlor,
                     index: Option[Int],
                     hasAdditionalSettlors: Option[Boolean]): Try[UserAnswers] =
    super.apply(answers, individual, index, hasAdditionalSettlors)
      .flatMap(_.set(NamePage, individual.name))
      .flatMap(answers => extractDateOfBirth(individual, answers))
      .flatMap(answers => extractCountryOfNationality(individual.countryOfNationality, answers))
      .flatMap(answers => extractCountryOfResidence(individual.countryOfResidence, answers))
      .flatMap(answers => extractAddress(individual.address, answers))
      .flatMap(answers => extractIdentification(individual, answers))
      .flatMap(_.set(MentalCapacityYesNoPage, individual.mentalCapacityYesNo))
      .flatMap(_.set(ProvisionalPage, individual.provisional))

  private def extractDateOfBirth(individual: IndividualSettlor, answers: UserAnswers): Try[UserAnswers] = {
    extractConditionalAnswer(individual.dateOfBirth, answers, DateOfBirthYesNoPage, DateOfBirthPage)
  }

  private def extractIdentification(individual: IndividualSettlor, answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTaxable) {
      individual.identification match {
        case Some(NationalInsuranceNumber(nino)) => answers
          .set(NationalInsuranceNumberYesNoPage, true)
          .flatMap(_.set(NationalInsuranceNumberPage, nino))
        case Some(p: Passport) => answers
          .set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(_.set(PassportDetailsYesNoPage, true))
          .flatMap(_.set(PassportDetailsPage, p))
        case Some(id: IdCard) => answers
          .set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(_.set(PassportDetailsYesNoPage, false))
          .flatMap(_.set(IdCardDetailsYesNoPage, true))
          .flatMap(_.set(IdCardDetailsPage, id))
        case Some(combined: CombinedPassportOrIdCard) => answers
          .set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(_.set(PassportOrIdCardDetailsYesNoPage, true))
          .flatMap(_.set(PassportOrIdCardDetailsPage, combined))
        case _ => answers
          .set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(answers => extractPassportOrIdCardDetailsYesNo(individual.address, answers))
      }
    } else {
      Success(answers)
    }
  }

  private def extractPassportOrIdCardDetailsYesNo(address: Option[Address], answers: UserAnswers): Try[UserAnswers] = {
    if (address.isDefined) {
      answers.set(PassportOrIdCardDetailsYesNoPage, false)
    } else {
      Success(answers)
    }
  }

  override def countryOfNationalityYesNoPage: QuestionPage[Boolean] = CountryOfNationalityYesNoPage
  override def ukCountryOfNationalityYesNoPage: QuestionPage[Boolean] = CountryOfNationalityUkYesNoPage
  override def countryOfNationalityPage: QuestionPage[String] = CountryOfNationalityPage

  override def countryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceYesNoPage
  override def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage

  override def addressYesNoPage: QuestionPage[Boolean] = AddressYesNoPage
  override def ukAddressYesNoPage: QuestionPage[Boolean] = LiveInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def startDatePage: QuestionPage[LocalDate] = StartDatePage

  override def indexPage: QuestionPage[Int] = IndexPage

  override def basePath: JsPath = pages.individual.living.basePath

}
