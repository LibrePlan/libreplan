/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.orders.daos;

import java.util.Set;

import org.libreplan.business.common.daos.IGenericDAO;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.SumExpenses;

/**
 * Contract for {@link SumExpensesDAO}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface ISumExpensesDAO extends IGenericDAO<SumExpenses, Long> {

    /**
     * Update the {@link SumExpenses} objects with the changes in the
     * {@link ExpenseSheetLine} set passed as argument. <br />
     *
     * If the {@link ExpenseSheetLine} is new, the value is added to the
     * corresponding {@link SumExpenses}. Otherwise, the difference of
     * value is added or subtracted as required. <br />
     *
     * If there is not {@link SumExpenses} associated to the
     * {@link OrderElement} yet, it is created on demand.
     *
     * @param expenseSheetLineSet
     */
    void updateRelatedSumExpensesWithExpenseSheetLineSet(Set<ExpenseSheetLine> expenseSheetLineSet);

    /**
     * Update the {@link SumExpenses} objects removing the values from the
     * {@link ExpenseSheetLine} set passed as argument. <br />
     *
     * If the {@link ExpenseSheetLine} is new, nothing is substracted. Otherwise,
     * the actual value saved in the database is substracted and not the one
     * coming in the objects passed.
     *
     * @param expenseSheetLineSet
     */

    void updateRelatedSumExpensesWithDeletedExpenseSheetLineSet(
            Set<ExpenseSheetLine> expenseSheetLineSet);

    SumExpenses findByOrderElement(OrderElement orderElement);

}