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

package views.behaviours

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.ViewSpecBase

trait ViewBehaviours extends ViewSpecBase {

  def normalPage(view: HtmlFormat.Appendable,
                 messageKeyPrefix: String,
                 expectedGuidanceKeys: String*): Unit = {

    "have the correct banner title" in {

      val doc = asDocument(view)
      val bannerTitle = doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked")
      bannerTitle.html() mustBe messages("service.name")
    }

    "display the correct browser title" in {

      val doc = asDocument(view)
      assertEqualsMessage(doc, "title", s"$messageKeyPrefix.title")
    }

    "display the correct page title" in {

      val doc = asDocument(view)
      assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading")
    }

    "display the correct guidance" in {

      val doc = asDocument(view)
      for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
    }

    "display language toggles" in {

      val doc = asDocument(view)
      assertRenderedByCssSelector(doc, "a[lang=cy]")
    }
  }

  def dynamicTitlePage(view: HtmlFormat.Appendable,
                       messageKeyPrefix: String,
                       messageKeyParam: String,
                       expectedGuidanceKeys: String*): Unit = {

    "behave like a dynamic title page" when {

      "rendered" must {

        "have the correct banner title" in {

          val doc = asDocument(view)
          val bannerTitle = doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked")
          bannerTitle.html() mustBe messages("service.name")
        }

        "display the correct browser title" in {

          val doc = asDocument(view)
          assertEqualsMessage(doc, "title", s"$messageKeyPrefix.title", messageKeyParam)
        }

        "display the correct page title" in {

          val doc = asDocument(view)
          assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading", messageKeyParam)
        }

        "display the correct guidance" in {

          val doc = asDocument(view)
          for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
        }

        "display language toggles" in {

          val doc = asDocument(view)
          assertRenderedByCssSelector(doc, "a[lang=cy]")
        }
      }
    }
  }

  def pageWithTitle(view: HtmlFormat.Appendable, messageKeyPrefix: String, args: Any*) : Unit = {
    "display the correct page title" in {

      val doc = asDocument(view)
      assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading", args: _*)
    }
  }

  def pageWithTitleAndSectionSubheading(view: HtmlFormat.Appendable, messageKeyPrefix: String) : Unit = {
    "display the correct page title with section" in {

      val doc = asDocument(view)
      assertPageTitleWithSectionSubheading(doc, s"$messageKeyPrefix", captionParam = "")
    }
  }

  def pageWithTitleAndCaption(view: HtmlFormat.Appendable, messageKeyPrefix: String, captionParam: String, args: Any*) : Unit = {
    "display the correct page title with caption" in {

      val doc = asDocument(view)
      assertPageTitleWithCaptionEqualsMessage(doc, messageKeyPrefix,  captionParam, args: _*)
    }
  }

  def pageWithGuidance(view: HtmlFormat.Appendable, messageKeyPrefix: String, expectedGuidanceKeys: String*): Unit = {
    "display the correct guidance" in {

      val doc = asDocument(view)
      for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
    }
  }

  def confirmationPage(view: HtmlFormat.Appendable,
                       messageKeyPrefix: String,
                       messageKeyParam: String,
                       accessibleKeyParam: String): Unit = {

    "behave like a confirmation page" when {

      "rendered" must {

        "have the correct banner title" in {

          val doc = asDocument(view)
          val bannerTitle = doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked")
          bannerTitle.html() mustBe messages("service.name")
        }

        "display the correct browser title" in {

          val doc = asDocument(view)
          assertEqualsMessage(doc, "title", s"$messageKeyPrefix.title", accessibleKeyParam)
        }

        "display the correct page title" in {

          val doc = asDocument(view)
          assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading", messageKeyParam + " " + accessibleKeyParam)
        }

        "display language toggles" in {

          val doc = asDocument(view)
          assertRenderedByCssSelector(doc, "a[lang=cy]")
        }

      }
    }
  }

  def pageWithBackLink(view: HtmlFormat.Appendable): Unit = {

    "behave like a page with a back link" must {

      "have a back link" in {

        val doc = asDocument(view)
        assertRenderedById(doc, "back-link")
      }
    }
  }

  def pageWithLink(view: HtmlFormat.Appendable, id: String, url : String): Unit = {

    "behave like a page with a link" must {

      "have a link" in {

        val doc = asDocument(view)
        val element = doc.getElementById(id)

        assertRenderedById(doc, id)
        assertAttributeValueForElement(element, "href", url)
      }
    }
  }

  def pageWithASubmitButton(view: HtmlFormat.Appendable): Unit = {

    "behave like a page with a submit button" must {
      "have a submit button" in {
        val doc = asDocument(view)
        assertRenderedById(doc, "submit")
      }
    }
  }

  def pageWithContinueButton(view: HtmlFormat.Appendable, url : String): Unit = {

    "behave like a page with a Continue button" must {
      "have a continue button" in {
        val doc = asDocument(view)
        assertContainsTextForId(doc,"button", "Continue")
        assertAttributeValueForElement(
          doc.getElementById("button"),
          "href",
          url
        )
      }
    }
  }

  def pageWithHint[A](form: Form[A],
                      createView: Form[A] => HtmlFormat.Appendable,
                      expectedHintKey: String): Unit = {

    "behave like a page with hint text" in {

      val doc = asDocument(createView(form))
      assertContainsHint(doc, "value", Some(messages(expectedHintKey)))
    }
  }

  def pageWithWarning(view: HtmlFormat.Appendable): Unit = {

    "behave like a page with warning text" in {

      val doc = asDocument(view)

      assertContainsClass(doc, "govuk-warning-text")
      assertContainsClass(doc, "govuk-warning-text__icon")
      assertContainsClass(doc, "govuk-warning-text__text")
      assertContainsClass(doc, "govuk-warning-text__assistive")
    }
  }

  def pageWithReadOnlyInput(view: HtmlFormat.Appendable): Unit = {
    "behave like a page with a read-only input" must {

      "have a read-only input" in {

        val doc = asDocument(view)
        val inputs = doc.getElementsByTag("input")
        inputs.forEach(input => assertElementHasAttribute(input, "readonly"))
      }
    }
  }

  def pageWithoutReadOnlyInput(view: HtmlFormat.Appendable): Unit = {
    "behave like a page without a read-only input" must {

      "not have a read-only input" in {

        val doc = asDocument(view)
        val inputs = doc.getElementsByTag("input")
        inputs.forEach(input => assertElementDoesNotHaveAttribute(input, "readonly"))
      }
    }
  }
  def pageWithoutDisabledInput(view: HtmlFormat.Appendable): Unit = {
    "behave like a page without a disabled input" must {

      "not have a disabled input" in {

        val doc = asDocument(view)
        val inputs = doc.getElementsByTag("input")
        inputs.forEach(input => assertElementDoesNotHaveAttribute(input, "disabled"))
      }
    }
  }

  def pageWithDisabledInput(view: HtmlFormat.Appendable): Unit = {
    "behave like a page with a disabled input" must {

      "have a disabled input" in {

        val doc = asDocument(view)
        val inputs = doc.getElementsByTag("input")
        inputs.forEach(input => assertElementHasAttribute(input, "disabled"))
      }
    }
  }
}