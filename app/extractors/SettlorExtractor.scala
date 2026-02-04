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

package extractors

import models.Constant.GB
import models.settlors.Settlor
import models.{Address, NonUkAddress, UkAddress, UserAnswers}
import pages.{EmptyPage, QuestionPage}
import play.api.libs.json.{JsPath, Writes}

import java.time.LocalDate
import scala.util.{Success, Try}

trait SettlorExtractor[T <: Settlor] {

  def apply(
    answers: UserAnswers,
    settlor: T,
    index: Option[Int] = None,
    hasAdditionalSettlors: Option[Boolean] = None
  ): Try[UserAnswers] =
    answers
      .deleteAtPath(basePath)
      .flatMap(answers => extractIfDefined(settlor.startDate, startDatePage, answers))
      .flatMap(answers => extractIfDefined(index, indexPage, answers))

  def namePage: QuestionPage[String] = new EmptyPage[String]

  def utrPage: QuestionPage[String] = new EmptyPage[String]

  def countryOfNationalityYesNoPage: QuestionPage[Boolean]   = new EmptyPage[Boolean]
  def ukCountryOfNationalityYesNoPage: QuestionPage[Boolean] = new EmptyPage[Boolean]
  def countryOfNationalityPage: QuestionPage[String]         = new EmptyPage[String]

  def countryOfResidenceYesNoPage: QuestionPage[Boolean]   = new EmptyPage[Boolean]
  def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = new EmptyPage[Boolean]
  def countryOfResidencePage: QuestionPage[String]         = new EmptyPage[String]

  def addressYesNoPage: QuestionPage[Boolean]      = new EmptyPage[Boolean]
  def ukAddressYesNoPage: QuestionPage[Boolean]    = new EmptyPage[Boolean]
  def ukAddressPage: QuestionPage[UkAddress]       = new EmptyPage[UkAddress]
  def nonUkAddressPage: QuestionPage[NonUkAddress] = new EmptyPage[NonUkAddress]

  def startDatePage: QuestionPage[LocalDate] = new EmptyPage[LocalDate]

  def indexPage: QuestionPage[Int] = new EmptyPage[Int]

  def basePath: JsPath

  def extractCountryOfNationality(countryOfNationality: Option[String], answers: UserAnswers): Try[UserAnswers] =
    extractCountryOfResidenceOrNationality(
      country = countryOfNationality,
      answers = answers,
      yesNoPage = countryOfNationalityYesNoPage,
      ukYesNoPage = ukCountryOfNationalityYesNoPage,
      page = countryOfNationalityPage
    )

  def extractCountryOfResidence(countryOfResidence: Option[String], answers: UserAnswers): Try[UserAnswers] =
    extractCountryOfResidenceOrNationality(
      country = countryOfResidence,
      answers = answers,
      yesNoPage = countryOfResidenceYesNoPage,
      ukYesNoPage = ukCountryOfResidenceYesNoPage,
      page = countryOfResidencePage
    )

  def extractCountryOfResidenceOrNationality(
    country: Option[String],
    answers: UserAnswers,
    yesNoPage: QuestionPage[Boolean],
    ukYesNoPage: QuestionPage[Boolean],
    page: QuestionPage[String]
  ): Try[UserAnswers] =
    if (answers.isUnderlyingData5mld) {
      country match {
        case Some(GB)      =>
          answers
            .set(yesNoPage, true)
            .flatMap(_.set(ukYesNoPage, true))
            .flatMap(_.set(page, GB))
        case Some(country) =>
          answers
            .set(yesNoPage, true)
            .flatMap(_.set(ukYesNoPage, false))
            .flatMap(_.set(page, country))
        case None          =>
          answers.set(yesNoPage, false)
      }
    } else {
      Success(answers)
    }

  def extractAddress(address: Option[Address], answers: UserAnswers): Try[UserAnswers] =
    if (answers.isTaxable) {
      address match {
        case Some(uk: UkAddress)       =>
          answers
            .set(addressYesNoPage, true)
            .flatMap(_.set(ukAddressYesNoPage, true))
            .flatMap(_.set(ukAddressPage, uk))
        case Some(nonUk: NonUkAddress) =>
          answers
            .set(addressYesNoPage, true)
            .flatMap(_.set(ukAddressYesNoPage, false))
            .flatMap(_.set(nonUkAddressPage, nonUk))
        case _                         =>
          answers
            .set(addressYesNoPage, false)
      }
    } else {
      Success(answers)
    }

  def extractIfDefined[A](value: Option[A], page: QuestionPage[A], answers: UserAnswers)(implicit
    writes: Writes[A]
  ): Try[UserAnswers] =
    value match {
      case Some(v) => answers.set(page, v)
      case None    => Success(answers)
    }

  def extractConditionalAnswer[A](
    optionalValue: Option[A],
    answers: UserAnswers,
    yesNoPage: QuestionPage[Boolean],
    page: QuestionPage[A]
  )(implicit writes: Writes[A]): Try[UserAnswers] =
    optionalValue match {
      case Some(value) =>
        answers
          .set(yesNoPage, true)
          .flatMap(_.set(page, value))
      case None        =>
        answers
          .set(yesNoPage, false)
    }

}
