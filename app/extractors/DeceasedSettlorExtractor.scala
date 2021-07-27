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
                     hasAdditionalSettlors: Option[Boolean]): Try[UserAnswers] =
    super.apply(answers, settlor, index, hasAdditionalSettlors)
      .flatMap(answers => extractBpMatchStatus(settlor.bpMatchStatus, answers))
      .flatMap(_.set(NamePage, settlor.name))
      .flatMap(answers => extractDateOfBirth(settlor, answers))
      .flatMap(answers => extractDateOfDeath(settlor, answers))
      .flatMap(answers => extractAddress(settlor.address, answers))
      .flatMap(answers => extractIdentification(settlor, answers))
      .flatMap(answers => extractAdditionalSettlorsYesNo(hasAdditionalSettlors, answers))

  private def extractBpMatchStatus(bpMatchStatus: Option[BpMatchStatus], answers: UserAnswers): Try[UserAnswers] = {
    extractIfDefined(bpMatchStatus, BpMatchStatusPage, answers)
  }

  private def extractDateOfBirth(individual: DeceasedSettlor, answers: UserAnswers): Try[UserAnswers] = {
    extractConditionalAnswer(individual.dateOfBirth, answers, DateOfBirthYesNoPage, DateOfBirthPage)
  }

  private def extractDateOfDeath(individual: DeceasedSettlor, answers: UserAnswers): Try[UserAnswers] = {
    extractConditionalAnswer(individual.dateOfDeath, answers, DateOfDeathYesNoPage, DateOfDeathPage)
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

  override def addressYesNoPage: QuestionPage[Boolean] = AddressYesNoPage
  override def ukAddressYesNoPage: QuestionPage[Boolean] = LivedInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def basePath: JsPath = pages.individual.deceased.basePath

}
