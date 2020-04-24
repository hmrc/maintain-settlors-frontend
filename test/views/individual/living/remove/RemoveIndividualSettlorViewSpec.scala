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

package views.individual.living.remove

import controllers.individual.living.remove.routes
import forms.YesNoFormProvider
import models.Name
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.individual.living.remove.RemoveIndividualSettlorView

class RemoveIndividualSettlorViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "removeIndividualSettlor"
  val form = (new YesNoFormProvider).withPrefix(messageKeyPrefix)
  val name: Name = Name("First", None, "Last")
  val index = 0

  "RemoveIndividualSettlor view" must {

    val view = viewFor[RemoveIndividualSettlorView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, index, name.displayName)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name.displayName)

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, messageKeyPrefix, Some(name.displayName), routes.RemoveIndividualSettlorController.onSubmit(index).url)
  }
}
