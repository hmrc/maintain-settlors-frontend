/*
 * Copyright 2021 HM Revenue & Customs
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

package utils

import base.SpecBase
import models.CompanyType.Trading
import models.Name
import models.TypeOfTrust.EmployeeRelated
import models.settlors.{BusinessSettlor, DeceasedSettlor, IndividualSettlor, Settlors}
import viewmodels.addAnother.{AddRow, AddToRows}

import java.time.LocalDate

class AddASettlorViewHelperSpec extends SpecBase {

  private val viewHelper: AddASettlorViewHelper = injector.instanceOf[AddASettlorViewHelper]

  private val index = 0

  private val name = Name(firstName = "Joe", middleName = None, lastName = "Bloggs")
  private val companyName = "Name"

  private val deceasedSettlor = DeceasedSettlor(
    bpMatchStatus = None,
    name = name,
    dateOfDeath = None,
    dateOfBirth = None,
    identification = None,
    address = None
  )

  private def individualSettlor(provisional: Boolean) = IndividualSettlor(
    name = name,
    dateOfBirth = None,
    countryOfNationality = None,
    countryOfResidence = None,
    identification = None,
    address = None,
    mentalCapacityYesNo = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = provisional
  )

  private def businessSettlor(provisional: Boolean) = BusinessSettlor(
    name = companyName,
    companyType = None,
    companyTime = None,
    utr = None,
    address = None,
    entityStart = LocalDate.parse("2012-03-14"),
    provisional = provisional
  )

  "AddASettlorViewHelper" when {

    "not migrating from non-taxable to taxable" must {

      val migratingFromNonTaxableToTaxable = false

      "render complete row" when {

        "individual" when {

          "provisional" in {

            val result = viewHelper.rows(
              settlors = Settlors(settlor = List(individualSettlor(true))),
              migratingFromNonTaxableToTaxable = migratingFromNonTaxableToTaxable
            )

            result mustBe AddToRows(
              inProgress = Nil,
              complete = List(
                AddRow(
                  name = name.displayName,
                  typeLabel = "Individual settlor",
                  changeUrl = controllers.individual.living.amend.routes.CheckDetailsController.extractAndRender(index).url,
                  removeUrl = Some(controllers.individual.living.remove.routes.RemoveIndividualSettlorController.onPageLoad(index).url)
                )
              )
            )
          }

          "not provisional" in {

            val result = viewHelper.rows(
              settlors = Settlors(settlor = List(individualSettlor(false))),
              migratingFromNonTaxableToTaxable = migratingFromNonTaxableToTaxable
            )

            result mustBe AddToRows(
              inProgress = Nil,
              complete = List(
                AddRow(
                  name = name.displayName,
                  typeLabel = "Individual settlor",
                  changeUrl = controllers.individual.living.amend.routes.CheckDetailsController.extractAndRender(index).url,
                  removeUrl = None
                )
              )
            )
          }
        }

        "business" when {

          "provisional" in {

            val result = viewHelper.rows(
              settlors = Settlors(settlorCompany = List(businessSettlor(true))),
              migratingFromNonTaxableToTaxable = migratingFromNonTaxableToTaxable
            )

            result mustBe AddToRows(
              inProgress = Nil,
              complete = List(
                AddRow(
                  name = companyName,
                  typeLabel = "Business settlor",
                  changeUrl = controllers.business.amend.routes.CheckDetailsController.extractAndRender(index).url,
                  removeUrl = Some(controllers.business.remove.routes.RemoveBusinessSettlorController.onPageLoad(index).url)
                )
              )
            )
          }

          "not provisional" in {

            val result = viewHelper.rows(
              settlors = Settlors(settlorCompany = List(businessSettlor(false))),
              migratingFromNonTaxableToTaxable = migratingFromNonTaxableToTaxable
            )

            result mustBe AddToRows(
              inProgress = Nil,
              complete = List(
                AddRow(
                  name = companyName,
                  typeLabel = "Business settlor",
                  changeUrl = controllers.business.amend.routes.CheckDetailsController.extractAndRender(index).url,
                  removeUrl = None
                )
              )
            )
          }
        }

        "deceased" in {

          val result = viewHelper.rows(
            settlors = Settlors(deceased = Some(deceasedSettlor)),
            migratingFromNonTaxableToTaxable = migratingFromNonTaxableToTaxable
          )

          result mustBe AddToRows(
            inProgress = Nil,
            complete = List(
              AddRow(
                name = name.displayName,
                typeLabel = "Will settlor",
                changeUrl = controllers.individual.deceased.routes.CheckDetailsController.extractAndRender().url,
                removeUrl = None
              )
            )
          )
        }
      }
    }

    "migrating from non-taxable to taxable" must {

      val migratingFromNonTaxableToTaxable = true

      "render in-progress row" when {
        "business" when {
          "company type and company time not answered for employee-related trust" when {

            "provisional" in {

              val result = viewHelper.rows(
                settlors = Settlors(settlorCompany = List(businessSettlor(true))),
                migratingFromNonTaxableToTaxable = migratingFromNonTaxableToTaxable,
                trustType = Some(EmployeeRelated)
              )

              result mustBe AddToRows(
                inProgress = List(
                  AddRow(
                    name = companyName,
                    typeLabel = "Business settlor",
                    changeUrl = controllers.business.amend.routes.CheckDetailsController.extractAndRedirect(index).url,
                    removeUrl = Some(controllers.business.remove.routes.RemoveBusinessSettlorController.onPageLoad(index).url)
                  )
                ),
                complete = Nil
              )
            }

            "not provisional" in {

              val result = viewHelper.rows(
                settlors = Settlors(settlorCompany = List(businessSettlor(false))),
                migratingFromNonTaxableToTaxable = migratingFromNonTaxableToTaxable,
                trustType = Some(EmployeeRelated)
              )

              result mustBe AddToRows(
                inProgress = List(
                  AddRow(
                    name = companyName,
                    typeLabel = "Business settlor",
                    changeUrl = controllers.business.amend.routes.CheckDetailsController.extractAndRedirect(index).url,
                    removeUrl = None
                  )
                ),
                complete = Nil
              )
            }
          }
        }
      }

      "render complete row" when {

        "individual" when {

          "provisional" in {

            val result = viewHelper.rows(
              settlors = Settlors(settlor = List(individualSettlor(true))),
              migratingFromNonTaxableToTaxable = migratingFromNonTaxableToTaxable
            )

            result mustBe AddToRows(
              inProgress = Nil,
              complete = List(
                AddRow(
                  name = name.displayName,
                  typeLabel = "Individual settlor",
                  changeUrl = controllers.individual.living.amend.routes.CheckDetailsController.extractAndRender(index).url,
                  removeUrl = Some(controllers.individual.living.remove.routes.RemoveIndividualSettlorController.onPageLoad(index).url)
                )
              )
            )
          }

          "not provisional" in {

            val result = viewHelper.rows(
              settlors = Settlors(settlor = List(individualSettlor(false))),
              migratingFromNonTaxableToTaxable = migratingFromNonTaxableToTaxable
            )

            result mustBe AddToRows(
              inProgress = Nil,
              complete = List(
                AddRow(
                  name = name.displayName,
                  typeLabel = "Individual settlor",
                  changeUrl = controllers.individual.living.amend.routes.CheckDetailsController.extractAndRender(index).url,
                  removeUrl = None
                )
              )
            )
          }
        }

        "business" when {
          "company type and company time answered for employee-related trust" when {

            "provisional" in {

              val result = viewHelper.rows(
                settlors = Settlors(settlorCompany = List(businessSettlor(true).copy(companyType = Some(Trading), companyTime = Some(true)))),
                migratingFromNonTaxableToTaxable = migratingFromNonTaxableToTaxable,
                trustType = Some(EmployeeRelated)
              )

              result mustBe AddToRows(
                inProgress = Nil,
                complete = List(
                  AddRow(
                    name = companyName,
                    typeLabel = "Business settlor",
                    changeUrl = controllers.business.amend.routes.CheckDetailsController.extractAndRender(index).url,
                    removeUrl = Some(controllers.business.remove.routes.RemoveBusinessSettlorController.onPageLoad(index).url)
                  )
                )
              )
            }

            "not provisional" in {

              val result = viewHelper.rows(
                settlors = Settlors(settlorCompany = List(businessSettlor(false).copy(companyType = Some(Trading), companyTime = Some(true)))),
                migratingFromNonTaxableToTaxable = migratingFromNonTaxableToTaxable,
                trustType = Some(EmployeeRelated)
              )

              result mustBe AddToRows(
                inProgress = Nil,
                complete = List(
                  AddRow(
                    name = companyName,
                    typeLabel = "Business settlor",
                    changeUrl = controllers.business.amend.routes.CheckDetailsController.extractAndRender(index).url,
                    removeUrl = None
                  )
                )
              )
            }
          }
        }

        "deceased" in {

          val result = viewHelper.rows(
            settlors = Settlors(deceased = Some(deceasedSettlor)),
            migratingFromNonTaxableToTaxable = migratingFromNonTaxableToTaxable
          )

          result mustBe AddToRows(
            inProgress = Nil,
            complete = List(
              AddRow(
                name = name.displayName,
                typeLabel = "Will settlor",
                changeUrl = controllers.individual.deceased.routes.CheckDetailsController.extractAndRender().url,
                removeUrl = None
              )
            )
          )
        }
      }
    }
  }
}
