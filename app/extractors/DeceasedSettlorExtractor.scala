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

package extractors

import models.settlors.DeceasedSettlor
import models.{BpMatchStatus, NationalInsuranceNumber, NonUkAddress, UkAddress, UserAnswers}
import pages.individual.deceased._
import pages.{AdditionalSettlorsYesNoPage, QuestionPage}
import play.api.libs.json.JsPath

import scala.util.{Success, Try}

class DeceasedSettlorExtractor extends SettlorExtractor[DeceasedSettlor] {

  override def apply(answers: UserAnswers,
                     settlor: DeceasedSettlor,
                     index: Option[Int],
                     hasAdditionalSettlors: Option[Boolean]): Try[UserAnswers] = {
    /*
      TODO after the super.apply call, answers are deleted at pages.individual.deceased.basePath
     */
    super.apply(answers, settlor, index, hasAdditionalSettlors)
      .flatMap(answers => extractBpMatchStatus(settlor.bpMatchStatus, answers))
      .flatMap(answers => extractName(settlor, answers))
      .flatMap(answers => extractDateOfBirth(settlor, answers))
      .flatMap(answers => extractDateOfDeath(settlor, answers))
      .flatMap(answers => extractNationality(settlor, answers))
      .flatMap(answers => extractCountryOfResidence(settlor, answers))
      .flatMap(answers => extractAddress(settlor.address, answers))
      .flatMap(answers => extractIdentification(settlor, answers))
      .flatMap(answers => extractAdditionalSettlorsYesNo(hasAdditionalSettlors, answers))
  }

  private def extractBpMatchStatus(bpMatchStatus: Option[BpMatchStatus], answers: UserAnswers): Try[UserAnswers] = {
    extractIfDefined(bpMatchStatus, BpMatchStatusPage, answers)
  }

  private def extractName(individual: DeceasedSettlor, answers: UserAnswers): Try[UserAnswers] = {
    answers.get(NamePage) match {
      case Some(value) =>
        answers.set(NamePage, value)
      case None =>
        answers.set(NamePage, individual.name)
    }
  }

  private def extractDateOfBirth(individual: DeceasedSettlor, answers: UserAnswers): Try[UserAnswers] = {
    val maybeDateOfBirth = Some(answers.get(DateOfBirthPage)).getOrElse(individual.dateOfBirth)
    extractConditionalAnswer(maybeDateOfBirth, answers, DateOfBirthYesNoPage, DateOfBirthPage)
  }

  private def extractDateOfDeath(individual: DeceasedSettlor, answers: UserAnswers): Try[UserAnswers] = {
    val maybeDateOfDeath = Some(answers.get(DateOfDeathPage)).getOrElse(individual.dateOfDeath)
    extractConditionalAnswer(maybeDateOfDeath, answers, DateOfDeathYesNoPage, DateOfDeathPage)
  }

  def extractNationality(individual: DeceasedSettlor, answers: UserAnswers): Try[UserAnswers] = {
    extractCountryOfResidenceOrNationality(
      country = individual.nationality,
      answers = answers,
      yesNoPage = countryOfNationalityYesNoPage,
      ukYesNoPage = ukCountryOfNationalityYesNoPage,
      page = countryOfNationalityPage
    )
  }

  def extractCountryOfResidence(individual: DeceasedSettlor, answers: UserAnswers): Try[UserAnswers] = {
    extractCountryOfResidenceOrNationality(
      country = individual.countryOfResidence,
      answers = answers,
      yesNoPage = countryOfResidenceYesNoPage,
      ukYesNoPage = ukCountryOfResidenceYesNoPage,
      page = countryOfResidencePage
    )
  }

  private def extractIdentification(individual: DeceasedSettlor, answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTaxable) {
      individual.identification match {
        case Some(NationalInsuranceNumber(nino)) =>
          answers.set(NationalInsuranceNumberYesNoPage, true)
            .flatMap(_.set(NationalInsuranceNumberPage, nino))
        case _ =>
          answers.set(NationalInsuranceNumberYesNoPage, false)
      }
    } else {
      Success(answers)
    }
  }

  private def extractAdditionalSettlorsYesNo(hasAdditionalSettlors: Option[Boolean], answers: UserAnswers): Try[UserAnswers] = {
    (hasAdditionalSettlors, answers.get(AdditionalSettlorsYesNoPage)) match {
      case (Some(false), None) =>
        answers.set(AdditionalSettlorsYesNoPage, false)
      case _ =>
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

  override def ukAddressYesNoPage: QuestionPage[Boolean] = LivedInTheUkYesNoPage

  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage

  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def basePath: JsPath = pages.individual.deceased.basePath

}
