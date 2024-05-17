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

package controllers

import com.google.inject.Inject
import config.FrontendAppConfig
import play.api.Configuration
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.language.{LanguageController, LanguageUtils}

class LanguageSwitchController @Inject()(
                                          configuration: Configuration,
                                          appConfig: FrontendAppConfig,
                                          override implicit val messagesApi: MessagesApi,
                                          languageUtils: LanguageUtils,
                                          cc: MessagesControllerComponents
                                        ) extends LanguageController(languageUtils, cc) {

  override def fallbackURL: String = appConfig.loginContinueUrl

  override def languageMap: Map[String, Lang] = appConfig.languageMap
}
