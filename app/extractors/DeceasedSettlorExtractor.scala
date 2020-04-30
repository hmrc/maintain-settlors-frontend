/*
 * Copyright 2020 HM Revenue & Customs
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

import com.google.inject.Inject
import models.settlors.DeceasedSettlor
import models.{Address, NationalInsuranceNumber, NonUkAddress, UkAddress, UserAnswers}
import pages.individual.deceased._

import scala.util.Try

class DeceasedSettlorExtractor @Inject()() {

  def apply(answers: UserAnswers, individual : DeceasedSettlor, index: Int): Try[UserAnswers] =
    answers.deleteAtPath(pages.individual.deceased.basePath)
      .flatMap(_.set(NamePage, individual.name))
      .flatMap(answers => extractDateOfBirth(individual, answers))
      .flatMap(answers => extractDateOfDeath(individual, answers))
      .flatMap(answers => extractAddress(individual.address, answers))
      .flatMap(answers => extractIdentification(individual, answers))
      .flatMap(_.set(IndexPage, index))

  private def extractAddress(address: Option[Address], answers: UserAnswers) : Try[UserAnswers] = {
    address match {
      case Some(uk: UkAddress) =>
        answers.set(AddressYesNoPage, true)
          .flatMap(_.set(LivedInTheUkYesNoPage, true))
          .flatMap(_.set(UkAddressPage, uk))
      case Some(nonUk: NonUkAddress) =>
        answers.set(AddressYesNoPage, true)
          .flatMap(_.set(LivedInTheUkYesNoPage, false))
          .flatMap(_.set(NonUkAddressPage, nonUk))
      case _ =>
        answers.set(AddressYesNoPage, false)
    }
  }

  private def extractDateOfBirth(individual: DeceasedSettlor, answers: UserAnswers) : Try[UserAnswers] =
  {
    individual.dateOfBirth match {
      case Some(dob) =>
        answers.set(DateOfBirthYesNoPage, true)
          .flatMap(_.set(DateOfBirthPage, dob))
      case None =>
        // Assumption that user answered no as dob is not provided
        answers.set(DateOfBirthYesNoPage, false)
    }
  }

  private def extractDateOfDeath(individual: DeceasedSettlor, answers: UserAnswers) : Try[UserAnswers] =
  {
    individual.dateOfDeath match {
      case Some(dob) =>
        answers.set(DateOfDeathYesNoPage, true)
          .flatMap(_.set(DateOfDeathPage, dob))
      case None =>
        // Assumption that user answered no as dob is not provided
        answers.set(DateOfDeathYesNoPage, false)
    }
  }

  private def extractIdentification(individual: DeceasedSettlor,
                                    answers: UserAnswers) : Try[UserAnswers] =
  {
    individual.identification match {
      case Some(NationalInsuranceNumber(nino)) =>
        answers.set(NationalInsuranceNumberYesNoPage, true)
          .flatMap(_.set(NationalInsuranceNumberPage, nino))
      case _ =>
        answers.set(NationalInsuranceNumberYesNoPage, false)
    }
  }
}