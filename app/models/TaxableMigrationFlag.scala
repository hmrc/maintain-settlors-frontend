package models

import play.api.libs.json.{Format, Json}

case class TaxableMigrationFlag(value: Option[Boolean]) {

  def migratingFromNonTaxableToTaxable: Boolean = value.contains(true)
}

object TaxableMigrationFlag {
  implicit val format: Format[TaxableMigrationFlag] = Json.format[TaxableMigrationFlag]
}
