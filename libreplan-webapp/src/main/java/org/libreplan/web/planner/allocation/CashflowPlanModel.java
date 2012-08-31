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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.libreplan.business.cashflow.entities.CashflowOutput;
import org.libreplan.business.cashflow.entities.CashflowPlan;
import org.libreplan.business.expensesheet.daos.IExpenseSheetLineDAO;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.orders.entities.SumExpenses;
import org.libreplan.business.planner.entities.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for operations in {@link CashflowPlan} popup.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CashflowPlanModel implements ICashflowPlanModel {

    private Task task;

    @Autowired
    private IExpenseSheetLineDAO expenseSheetLineDAO;

    private List<ExpenseSheetLine> expenseSheetLines;

    @Override
    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public CashflowPlan getCashflowPlan() {
        if (task == null) {
            return null;
        }
        return task.getCashflowPlan();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseSheetLine> getExpenseSheetLines() {
        if (task == null) {
            return new ArrayList<ExpenseSheetLine>();
        }

        if (expenseSheetLines == null) {
            expenseSheetLines = expenseSheetLineDAO.findByOrderElement(task
                    .getOrderElement());
        }
        return expenseSheetLines;
    }

    @Override
    public BigDecimal getTotalExpenses() {
        if (task == null) {
            return BigDecimal.ZERO;
        }
        SumExpenses sumExpenses = task.getOrderElement().getSumExpenses();
        if (sumExpenses == null) {
            return BigDecimal.ZERO;
        }
        return sumExpenses.getTotalDirectExpenses();
    }

    @Override
    public List<CashflowOutput> getOutputs() {
        CashflowPlan cashflowPlan = getCashflowPlan();
        if (cashflowPlan == null) {
            return Collections.emptyList();
        }
        if (cashflowPlan.isManual()) {
            return cashflowPlan.getOutputs();
        } else {
            List<CashflowOutput> outputs = new ArrayList<CashflowOutput>();
            int delayDays = cashflowPlan.getDelayDays() == null ? 0
                    : cashflowPlan.getDelayDays();
            for (ExpenseSheetLine line : getExpenseSheetLines()) {
                outputs.add(new CashflowOutput(line.getDate().plusDays(
                        delayDays), line.getValue()));
            }
            return outputs;
        }
    }

    @Override
    public BigDecimal getTotalOutputs() {
        CashflowPlan cashflowPlan = getCashflowPlan();
        if (cashflowPlan == null) {
            return BigDecimal.ZERO;
        }
        if (cashflowPlan.isManual()) {
            return cashflowPlan.calculateTotal();
        } else {
            BigDecimal total = BigDecimal.ZERO;
            for (CashflowOutput output : getOutputs()) {
                total = total.add(output.getAmount());
            }
            return total;
        }
    }

}
