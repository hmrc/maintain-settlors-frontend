/*
 * Copyright 2026 HM Revenue & Customs
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

package models

import pages.AddNowPage
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import queries.{Gettable, Settable}

import java.time.{LocalDate, LocalDateTime}
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
  internalId: String,
  identifier: String,
  sessionId: String,
  newId: String,
  whenTrustSetup: LocalDate,
  trustType: Option[TypeOfTrust],
  deedOfVariation: Option[DeedOfVariation],
  isDateOfDeathRecorded: Boolean,
  data: JsObject = Json.obj(),
  updatedAt: LocalDateTime = LocalDateTime.now,
  isTaxable: Boolean,
  isUnderlyingData5mld: Boolean,
  migratingFromNonTaxableToTaxable: Boolean
) {

  private val logger: Logger = Logger(getClass)

  def cleanup: Try[UserAnswers] =
    this
      .deleteAtPath(pages.individual.living.basePath)
      .flatMap(_.deleteAtPath(pages.business.basePath))
      .flatMap(_.remove(AddNowPage))

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.at(page.path).reads(data) match {
      case JsSuccess(value, _) => Some(value)
      case JsError(_)          => None
    }

  def set[A](page: Settable[A], value: Option[A])(implicit writes: Writes[A]): Try[UserAnswers] =
    value match {
      case Some(v) => setValue(page, v)
      case None    => page.cleanup(value, this)
    }

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = setValue(page, value)

  private def setValue[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {
    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors)       =>
        val errorPaths = errors.collectFirst { case (path, e) => s"$path $e" }
        logger.warn(s"Unable to set path ${page.path} due to errors $errorPaths")
        Failure(JsResultException(errors))
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](query: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(query.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_)            =>
        Success(data)
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      query.cleanup(None, updatedAnswers)
    }
  }

  def deleteAtPath(path: JsPath): Try[UserAnswers] =
    data
      .removeObject(path)
      .map(obj => copy(data = obj))
      .fold(
        _ => Success(this),
        result => Success(result)
      )

}

object UserAnswers {

  implicit lazy val reads: Reads[UserAnswers] = (
    (__ \ "internalId").read[String] and
      ((__ \ "utr").read[String] or (__ \ "identifier").read[String]) and
      (__ \ "sessionId").read[String] and
      (__ \ "newId").read[String] and
      (__ \ "whenTrustSetup").read[LocalDate] and
      (__ \ "trustType").readNullable[TypeOfTrust] and
      (__ \ "deedOfVariation").readNullable[DeedOfVariation] and
      (__ \ "isDateOfDeathRecorded").read[Boolean] and
      (__ \ "data").read[JsObject] and
      (__ \ "updatedAt").read(MongoDateTimeFormats.localDateTimeRead) and
      (__ \ "isTaxable").readWithDefault[Boolean](true) and
      (__ \ "isUnderlyingData5mld").readWithDefault[Boolean](false) and
      (__ \ "migratingFromNonTaxableToTaxable").readWithDefault[Boolean](false)
  )(UserAnswers.apply _)

  implicit lazy val writes: Writes[UserAnswers] = (
    (__ \ "internalId").write[String] and
      (__ \ "identifier").write[String] and
      (__ \ "sessionId").write[String] and
      (__ \ "newId").write[String] and
      (__ \ "whenTrustSetup").write[LocalDate] and
      (__ \ "trustType").writeNullable[TypeOfTrust] and
      (__ \ "deedOfVariation").writeNullable[DeedOfVariation] and
      (__ \ "isDateOfDeathRecorded").write[Boolean] and
      (__ \ "data").write[JsObject] and
      (__ \ "updatedAt").write(MongoDateTimeFormats.localDateTimeWrite) and
      (__ \ "isTaxable").write[Boolean] and
      (__ \ "isUnderlyingData5mld").write[Boolean] and
      (__ \ "migratingFromNonTaxableToTaxable").write[Boolean]
  )(unlift(UserAnswers.unapply))

}
