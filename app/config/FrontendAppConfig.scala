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

package config

import com.google.inject.{Inject, Singleton}
import controllers.routes
import play.api.Configuration
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Call

import java.time.LocalDate
import uk.gov.hmrc.hmrcfrontend.config.ContactFrontendConfig
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (
  configuration: Configuration,
  contactFrontendConfig: ContactFrontendConfig,
  servicesConfig: ServicesConfig
) {

  final val ENGLISH         = "en"
  final val WELSH           = "cy"
  final val UK_COUNTRY_CODE = "GB"

  val betaFeedbackUrl =
    s"${contactFrontendConfig.baseUrl.get}/contact/beta-feedback?service=${contactFrontendConfig.serviceId.get}"

  val appName: String = configuration.get[String]("appName")

  lazy val authUrl: String          = servicesConfig.baseUrl("auth")
  lazy val loginUrl: String         = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")

  lazy val countdownLength: Int = configuration.get[Int]("timeout.countdown")
  lazy val timeoutLength: Int   = configuration.get[Int]("timeout.length")

  lazy val trustsUrl: String = servicesConfig.baseUrl("trusts")

  lazy val trustAuthUrl: String = servicesConfig.baseUrl("trusts-auth")

  lazy val trustsStoreUrl: String = servicesConfig.baseUrl("trusts-store")

  lazy val locationCanonicalList: String   = configuration.get[String]("location.canonical.list.all")
  lazy val locationCanonicalListCY: String = configuration.get[String]("location.canonical.list.allCY")

  lazy val logoutUrl: String = configuration.get[String]("urls.logout")

  lazy val logoutAudit: Boolean =
    configuration.get[Boolean]("microservice.services.features.auditing.logout")

  lazy val maintainATrustOverview: String = configuration.get[String]("urls.maintainATrustOverview")

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang(ENGLISH),
    "cymraeg" -> Lang(WELSH)
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  private val minDay: Int     = configuration.get[Int]("dates.minimum.day")
  private val minMonth: Int   = configuration.get[Int]("dates.minimum.month")
  private val minYear: Int    = configuration.get[Int]("dates.minimum.year")
  lazy val minDate: LocalDate = LocalDate.of(minYear, minMonth, minDay)

  private val maxDay: Int     = configuration.get[Int]("dates.maximum.day")
  private val maxMonth: Int   = configuration.get[Int]("dates.maximum.month")
  private val maxYear: Int    = configuration.get[Int]("dates.maximum.year")
  lazy val maxDate: LocalDate = LocalDate.of(maxYear, maxMonth, maxDay)

  def helplineUrl(implicit messages: Messages): String = {
    val path = messages.lang.code match {
      case WELSH => "urls.welshHelpline"
      case _     => "urls.trustsHelpline"
    }

    configuration.get[String](path)
  }

  /**
   * The schema allows up to 50 individuals AND 50 businesses when submitting a variation.
   * This then equates to 25 of each as an amended settlor counts as 2.
   * However, it is very unlikely that a trust will have this many settlors.
   * Therefore, it has been decided to only allow a maximum of 25 COMBINED settlors (i.e. individuals + businesses <= 25).
   * Toggle count-max-as-combined in application.conf to change this as required.
   * @return either true or false
   */
  def countMaxAsCombined: Boolean = configuration.get[Boolean]("microservice.services.features.count-max-as-combined")

  val cachettlplaybackInSeconds: Long = configuration.get[Long]("mongodb.playback.ttlSeconds")
  val cachettlSessionInSeconds: Long  = configuration.get[Long]("mongodb.session.ttlSeconds")

  val dropIndexes: Boolean =
    configuration.getOptional[Boolean]("microservice.services.features.mongo.dropIndexes").getOrElse(false)

}
