/*
 * Copyright 2022 HM Revenue & Customs
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

package views

import forms.AddASettlorFormProvider
import models.AddASettlor
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.addAnother.AddRow
import views.behaviours.{OptionsViewBehaviours, TabularDataViewBehaviours}
import views.html.AddASettlorView

class AddASettlorViewSpec extends OptionsViewBehaviours with TabularDataViewBehaviours {

  val completeSettlors = Seq(
    AddRow("Joe Bloggs", "Individual", "#", None),
    AddRow("Tom Jones", "Individual", "#", None)
  )

  val inProgressSettlors = Seq(
    AddRow("John Doe", "Individual", "#", None)
  )

  val messageKeyPrefix = "addASettlor"

  val form = new AddASettlorFormProvider()()

  val view: AddASettlorView = viewFor[AddASettlorView](Some(emptyUserAnswers))

  def applyConfiguration(form: Form[_],
                         inProgressRows: Seq[AddRow],
                         completeRows: Seq[AddRow],
                         count: Int,
                         maxedOut: List[String],
                         migrating: Boolean): HtmlFormat.Appendable = {
    val title = if (count > 1) s"The trust has $count settlors" else "Add a settlor"
    view.apply(form, None, inProgressRows, completeRows, title, maxedOut, migrating)(fakeRequest, messages)
  }

  "AddASettlorView" when {

    "migrating from non-taxable to taxable" when {

      val migrating: Boolean = true

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, None, Nil, Nil, "Add a settlor", Nil, migrating)(fakeRequest, messages)

      "there are no settlors" must {

        behave like normalPage(applyView(form), messageKeyPrefix)

        behave like pageWithNoTabularData(applyView(form), migrating)

        behave like pageWithBackLink(applyView(form))

        behave like pageWithOptions(form, applyView, AddASettlor.options)

        behave like pageWithWarning(applyView(form))

        "render additional content" in {

          val doc = asDocument(applyView(form))
          assertContainsText(doc, messages("addASettlor.transition.subheading"))
          assertContainsText(doc, messages("addASettlor.transition.warning"))
          assertContainsText(doc, messages("addASettlor.transition.p1"))
        }
      }

      "there is data in progress" must {

        val viewWithData = applyConfiguration(form, inProgressSettlors, Nil, 1, Nil, migrating)

        behave like dynamicTitlePage(viewWithData, "addASettlor", "1")

        behave like pageWithBackLink(viewWithData)

        behave like pageWithInProgressTabularData(viewWithData, inProgressSettlors, migrating)

        behave like pageWithOptions(form, applyView, AddASettlor.options)

        behave like pageWithWarning(applyView(form))

        "render additional content" in {

          val doc = asDocument(applyView(form))
          assertContainsText(doc, messages("addASettlor.transition.subheading"))
          assertContainsText(doc, messages("addASettlor.transition.warning"))
          assertContainsText(doc, messages("addASettlor.transition.p1"))
        }
      }

      "there is complete data" must {

        val viewWithData = applyConfiguration(form, Nil, completeSettlors, 2, Nil, migrating)

        behave like dynamicTitlePage(viewWithData, "addASettlor.count", "2")

        behave like pageWithBackLink(viewWithData)

        behave like pageWithCompleteTabularData(viewWithData, completeSettlors, migrating)

        behave like pageWithOptions(form, applyView, AddASettlor.options)

        behave like pageWithWarning(applyView(form))

        "render additional content" in {

          val doc = asDocument(applyView(form))
          assertContainsText(doc, messages("addASettlor.transition.subheading"))
          assertContainsText(doc, messages("addASettlor.transition.warning"))
          assertContainsText(doc, messages("addASettlor.transition.p1"))
        }
      }

      "there is one maxed out type" must {

        val viewWithData = applyConfiguration(form, Nil, completeSettlors, 25, List("whatTypeOfSettlor.individual"), migrating)

        behave like dynamicTitlePage(viewWithData, "addASettlor.count", "25")

        behave like pageWithBackLink(viewWithData)

        behave like pageWithCompleteTabularData(viewWithData, completeSettlors, migrating)

        behave like pageWithOptions(form, applyView, AddASettlor.options)

        "render content" in {
          val doc = asDocument(viewWithData)
          assertContainsText(doc, "You cannot add another individual as you have entered a maximum of 25.")
          assertContainsText(doc, "Check the settlors you have added. If you have further settlors to add within this type, write to HMRC with their details.")
        }

        behave like pageWithWarning(applyView(form))

        "render additional content" in {

          val doc = asDocument(applyView(form))
          assertContainsText(doc, messages("addASettlor.transition.subheading"))
          assertContainsText(doc, messages("addASettlor.transition.warning"))
          assertContainsText(doc, messages("addASettlor.transition.p1"))
        }
      }
    }

    "not migrating from non-taxable to taxable" when {

      val migrating: Boolean = false

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, None, Nil, Nil, "Add a settlor", Nil, migrating)(fakeRequest, messages)

      "there are no settlors" must {

        behave like normalPage(applyView(form), messageKeyPrefix)

        behave like pageWithNoTabularData(applyView(form), migrating)

        behave like pageWithBackLink(applyView(form))

        behave like pageWithOptions(form, applyView, AddASettlor.options)
      }

      "there is data in progress" must {

        val viewWithData = applyConfiguration(form, inProgressSettlors, Nil, 1, Nil, migrating)

        behave like dynamicTitlePage(viewWithData, "addASettlor", "1")

        behave like pageWithBackLink(viewWithData)

        behave like pageWithInProgressTabularData(viewWithData, inProgressSettlors, migrating)

        behave like pageWithOptions(form, applyView, AddASettlor.options)

      }

      "there is complete data" must {

        val viewWithData = applyConfiguration(form, Nil, completeSettlors, 2, Nil, migrating)

        behave like dynamicTitlePage(viewWithData, "addASettlor.count", "2")

        behave like pageWithBackLink(viewWithData)

        behave like pageWithCompleteTabularData(viewWithData, completeSettlors, migrating)

        behave like pageWithOptions(form, applyView, AddASettlor.options)
      }

      "there is one maxed out type" must {

        val viewWithData = applyConfiguration(form, Nil, completeSettlors, 25, List("whatTypeOfSettlor.individual"), migrating)

        behave like dynamicTitlePage(viewWithData, "addASettlor.count", "25")

        behave like pageWithBackLink(viewWithData)

        behave like pageWithCompleteTabularData(viewWithData, completeSettlors, migrating)

        behave like pageWithOptions(form, applyView, AddASettlor.options)

        "render content" in {
          val doc = asDocument(viewWithData)
          assertContainsText(doc, "You cannot add another individual as you have entered a maximum of 25.")
          assertContainsText(doc, "Check the settlors you have added. If you have further settlors to add within this type, write to HMRC with their details.")
        }
      }
    }
  }

}
