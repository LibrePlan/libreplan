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

package org.libreplan.business.reports.dtos;

import java.math.BigDecimal;
import java.util.Date;

import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.SumExpenses;
import org.libreplan.business.orders.entities.TaskSource;

/**
 * Utilities methods for report DTOs.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class ProjectStatusReportDTO {

    private String code;

    private String name;

    private BigDecimal budgetedIntegerPart;
    private BigDecimal budgetedFractionalPart;

    private BigDecimal plannedIntegerPart;
    private BigDecimal plannedFractionalPart;

    private BigDecimal spentIntegerPart;
    private BigDecimal spentFractionalPart;

    private Date startDate;

    private Date endDate;

    public ProjectStatusReportDTO(OrderElement orderElement) {
        code = orderElement.getCodeWithoutOrderPrefix();
        name = Util.getPrefixSpacesDependingOnDepth(orderElement)
                + orderElement.getName();

        BigDecimal budgeted = orderElement.getBudget();
        budgetedIntegerPart = Util.getIntegerPart(budgeted);
        budgetedFractionalPart = Util.getFractionalPart(budgeted);

        TaskSource taskSource = orderElement.getTaskSource();
        if (taskSource != null) {
            BigDecimal planned = taskSource.getTask().getSumOfAssignedEffort()
                    .toEurosAsDecimal();
            plannedIntegerPart = Util.getIntegerPart(planned);
            plannedFractionalPart = Util.getFractionalPart(planned);

            startDate = taskSource.getTask().getStartDate();
            endDate = taskSource.getTask().getEndDate();
        }

        SumExpenses sumExpenses = orderElement.getSumExpenses();
        if (sumExpenses != null) {
            BigDecimal spent = sumExpenses.getTotalExpenses();
            spentIntegerPart = Util.getIntegerPart(spent);
            spentFractionalPart = Util.getFractionalPart(spent);
        }
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBudgetedIntegerPart() {
        return budgetedIntegerPart;
    }

    public BigDecimal getBudgetedFractionalPart() {
        return budgetedFractionalPart;
    }

    public BigDecimal getPlannedIntegerPart() {
        return plannedIntegerPart;
    }

    public BigDecimal getPlannedFractionalPart() {
        return plannedFractionalPart;
    }

    public BigDecimal getSpentIntegerPart() {
        return spentIntegerPart;
    }

    public BigDecimal getSpentFractionalPart() {
        return spentFractionalPart;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

}