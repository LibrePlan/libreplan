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

package org.libreplan.business.cashflow.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;

/**
 * Represents the cashflow plan for a {@link TaskElement}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class CashflowPlan extends BaseEntity {

    private CashflowType type = CashflowType.MANUAL;

    private List<CashflowOutput> outputs = new ArrayList<CashflowOutput>();

    private Integer delayDays = 0;

    private Task task;

    public static CashflowPlan create(Task task) {
        return create(new CashflowPlan(task));
    }

    public static CashflowPlan create(Task task, CashflowType type,
            Integer delayDays) {
        CashflowPlan cashflowPlan = new CashflowPlan(task);
        if (type != null) {
            cashflowPlan.type = type;
            cashflowPlan.delayDays = delayDays;
        }
        return create(cashflowPlan);
    }

    /**
     * Default constructor for Hibernate. Do not use!
     */
    protected CashflowPlan() {
    }

    public CashflowPlan(Task task) {
        this.task = task;
    }

    @NotNull(message = "type not specified")
    public CashflowType getType() {
        return type;
    }

    public void setType(CashflowType type) {
        this.type = type;
        if (!isManual()) {
            outputs.clear();
            if (delayDays == null) {
                delayDays = 0;
            }
        }
    }

    @Valid
    public List<CashflowOutput> getOutputs() {
        if (isManual()) {
            return Collections.unmodifiableList(outputs);
        } else {
            List<CashflowOutput> outputs = new ArrayList<CashflowOutput>();
            int delayDays = getDelayDays() == null ? 0 : getDelayDays();
            for (ExpenseSheetLine line : getExpenseSheetLines()) {
                outputs.add(new CashflowOutput(line.getDate().plusDays(
                        delayDays), line.getValue()));
            }
            return outputs;
        }
    }

    private List<ExpenseSheetLine> getExpenseSheetLines() {
        if (task == null) {
            return new ArrayList<ExpenseSheetLine>();
        }

        return Registry.getExpenseSheetLineDAO().findByOrderElement(
                task.getOrderElement());
    }

    public void addOutput(LocalDate date, BigDecimal amount) {
        outputs.add(new CashflowOutput(date, amount));
    }

    public void removeOutput(LocalDate date, BigDecimal amount) {
        int index = indexOfOutput(date, amount);
        if (index >= 0) {
            outputs.remove(index);
        }
    }

    private int indexOfOutput(LocalDate date, BigDecimal amount) {
        int i = 0;
        for (CashflowOutput output : outputs) {
            if (output.getDate().equals(date)
                    && output.getAmount().equals(amount)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CashflowOutput output : getOutputs()) {
            total = total.add(output.getAmount());
        }
        return total;
    }

    public boolean isManual() {
        return type.equals(CashflowType.MANUAL);
    }

    public Integer getDelayDays() {
        return delayDays;
    }

    public void setDelayDays(Integer delayDays) {
        if (isManual()) {
            throw new IllegalArgumentException(
                    "You cannot set delay days for chasflow plan with type manual");
        }
        if (delayDays == null || delayDays < 0) {
            throw new IllegalArgumentException(
                    "Delay days cannot be null or negative");
        }
        this.delayDays = delayDays;
    }

    public CashflowPlan copy() {
        CashflowPlan copy = create(task, type, delayDays);
        for (CashflowOutput output : outputs) {
            copy.addOutput(output.getDate(), output.getAmount());
        }
        return copy;
    }

    public Task getTask() {
        return task;
    }

}