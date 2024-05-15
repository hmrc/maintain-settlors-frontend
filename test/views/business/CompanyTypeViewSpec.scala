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

package views.business

import forms.CompanyTypeFormProvider
import models.{CompanyType, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.OptionsViewBehaviours
import views.html.business.CompanyTypeView

class CompanyTypeViewSpec extends OptionsViewBehaviours {

  val messageKeyPrefix = "businessSettlor.companyType"
  val name = "Name"
  val form: Form[CompanyType] = new CompanyTypeFormProvider()()
  val view: CompanyTypeView = viewFor[CompanyTypeView](Some(emptyUserAnswers))

  "CompanyType view" must {

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, name, NormalMode)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithOptions(form, applyView, CompanyType.options)

    behave like pageWithASubmitButton(applyView(form))
  }

}
