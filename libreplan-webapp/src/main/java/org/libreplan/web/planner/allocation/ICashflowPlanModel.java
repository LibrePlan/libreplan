/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.web.planner.allocation;

import java.math.BigDecimal;
import java.util.List;

import org.libreplan.business.cashflow.entities.CashflowOutput;
import org.libreplan.business.cashflow.entities.CashflowPlan;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.planner.entities.Task;

/**
 * Contract for {@link CashflowPlanModel}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public interface ICashflowPlanModel {

    void setTask(Task task);

    Task getTask();

    CashflowPlan getCashflowPlan();

    List<ExpenseSheetLine> getExpenseSheetLines();

    BigDecimal getTotalExpenses();

    List<CashflowOutput> getOutputs();

    BigDecimal getTotalOutputs();

}
