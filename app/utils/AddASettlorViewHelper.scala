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

package utils

import models.TypeOfTrust
import models.settlors._
import play.api.i18n.Messages
import viewmodels.addAnother.{AddRow, AddToRows}

class AddASettlorViewHelper {

  def rows(settlors: Settlors, migratingFromNonTaxableToTaxable: Boolean, trustType: Option[TypeOfTrust] = None)(
    implicit messages: Messages
  ): AddToRows = {

    implicit class SettlorRows[T <: Settlor](settlors: List[T]) {
      def zipThenGroupThenMap(row: (T, Int) => AddRow, isComplete: Boolean): List[AddRow] = settlors.zipWithIndex
        .groupBy(_._1.hasRequiredData(migratingFromNonTaxableToTaxable, trustType))
        .getOrElse(isComplete, Nil)
        .map(x => row(x._1, x._2))
    }

    def deceasedSettlorRow(settlor: DeceasedSettlor): AddRow =
      AddRow(
        name = settlor.name.displayName,
        typeLabel = messages("entities.settlor.deceased"),
        changeUrl = controllers.individual.deceased.routes.CheckDetailsController.extractAndRender().url,
        removeUrl = None
      )

    def individualSettlorRow(settlor: IndividualSettlor, index: Int): AddRow =
      AddRow(
        name = settlor.name.displayName,
        typeLabel = messages("entities.settlor.individual"),
        changeUrl = controllers.individual.living.amend.routes.CheckDetailsController.extractAndRender(index).url,
        removeUrl = if (settlor.provisional) {
          Some(controllers.individual.living.remove.routes.RemoveIndividualSettlorController.onPageLoad(index).url)
        } else {
          None
        }
      )

    def businessSettlorRow(settlor: BusinessSettlor, index: Int): AddRow =
      AddRow(
        name = settlor.name,
        typeLabel = messages("entities.settlor.business"),
        changeUrl = if (settlor.hasRequiredData(migratingFromNonTaxableToTaxable, trustType)) {
          controllers.business.amend.routes.CheckDetailsController.extractAndRender(index).url
        } else {
          controllers.business.amend.routes.CheckDetailsController.extractAndRedirect(index).url
        },
        removeUrl = if (settlor.provisional) {
          Some(controllers.business.remove.routes.RemoveBusinessSettlorController.onPageLoad(index).url)
        } else {
          None
        }
      )

    def livingSettlorRows(isComplete: Boolean): List[AddRow] =
      settlors.settlor.zipThenGroupThenMap(individualSettlorRow, isComplete) ++
        settlors.settlorCompany.zipThenGroupThenMap(businessSettlorRow, isComplete)

    val inProgressRows: List[AddRow] = if (migratingFromNonTaxableToTaxable) {
      livingSettlorRows(isComplete = false)
    } else {
      Nil
    }

    val completedRows: List[AddRow] =
      settlors.deceased.map(deceasedSettlorRow).toList ++ livingSettlorRows(isComplete = true)

    AddToRows(inProgressRows, completedRows)
  }

}
