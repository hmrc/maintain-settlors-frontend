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

import models.Constant.GB
import models.settlors.Settlor
import models.{Address, NonUkAddress, UkAddress, UserAnswers}
import pages.{EmptyPage, QuestionPage}
import play.api.libs.json.{JsError, JsSuccess, Reads}

import scala.reflect.{ClassTag, classTag}
import scala.util.{Failure, Success, Try}

abstract class SettlorMapper[T <: Settlor : ClassTag] {

  def apply(answers: UserAnswers): Try[T] = {
    answers.data.validate[T](reads) match {
      case JsSuccess(value, _) =>
        Success(value)
      case JsError(errors) =>
        Failure(new Throwable(s"[UTR: ${answers.identifier}] Failed to rehydrate ${classTag[T].runtimeClass.getSimpleName} from UserAnswers due to $errors"))
    }
  }

  val reads: Reads[T]

  def ukAddressYesNoPage: QuestionPage[Boolean]
  def ukAddressPage: QuestionPage[UkAddress]
  def nonUkAddressPage: QuestionPage[NonUkAddress]

  def readAddress: Reads[Option[Address]] = {
    ukAddressYesNoPage.path.readNullable[Boolean].flatMap {
      case Some(true) => ukAddressPage.path.readNullable[UkAddress].widen[Option[Address]]
      case Some(false) => nonUkAddressPage.path.readNullable[NonUkAddress].widen[Option[Address]]
      case _ => Reads(_ => JsSuccess(None))
    }
  }

  def countryOfNationalityYesNoPage: QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ukCountryOfNationalityYesNoPage: QuestionPage[Boolean] = new EmptyPage[Boolean]
  def countryOfNationalityPage: QuestionPage[String] = new EmptyPage[String]

  def readCountryOfNationality: Reads[Option[String]] = {
    readCountryOfResidenceOrNationality(countryOfNationalityYesNoPage, ukCountryOfNationalityYesNoPage, countryOfNationalityPage)
  }

  def countryOfResidenceYesNoPage: QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = new EmptyPage[Boolean]
  def countryOfResidencePage: QuestionPage[String] = new EmptyPage[String]

  def readCountryOfResidence: Reads[Option[String]] = {
    readCountryOfResidenceOrNationality(countryOfResidenceYesNoPage, ukCountryOfResidenceYesNoPage, countryOfResidencePage)
  }

  def readCountryOfResidenceOrNationality(yesNoPage: QuestionPage[Boolean],
                                          ukYesNoPage: QuestionPage[Boolean],
                                          page: QuestionPage[String]): Reads[Option[String]] = {
    yesNoPage.path.readNullable[Boolean].flatMap[Option[String]] {
      case Some(true) => ukYesNoPage.path.read[Boolean].flatMap {
        case true => Reads(_ => JsSuccess(Some(GB)))
        case false => page.path.read[String].map(Some(_))
      }
      case _ => Reads(_ => JsSuccess(None))
    }
  }

}
