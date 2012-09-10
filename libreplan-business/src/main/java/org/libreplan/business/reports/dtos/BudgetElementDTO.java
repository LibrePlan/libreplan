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
import java.math.RoundingMode;
import java.util.Date;

import org.joda.time.LocalDate;
import org.libreplan.business.templates.entities.BudgetLineTemplate;
import org.libreplan.business.templates.entities.OrderElementTemplate;

/**
 * DTO for elements in the budget report.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class BudgetElementDTO {

    private String code;

    private String name;

    private BigDecimal budgetIntegerPart;
    private BigDecimal budgetFractionalPart;

    private BigDecimal costOrSalaryIntegerPart;
    private BigDecimal costOrSalaryFractionalPart;

    private String type;

    private Integer duration;

    private Integer quantity;

    private BigDecimal indemnizationSalaryIntegerPart;
    private BigDecimal indemnizationSalaryFractionalPart;

    private BigDecimal holidaySalaryIntegerPart;
    private BigDecimal holidaySalaryFractionalPart;

    private Date startDate;

    private Date endDate;

    public BudgetElementDTO(OrderElementTemplate orderElementTemplate) {
        code = orderElementTemplate.getCode();
        name = orderElementTemplate.getName();

        budgetIntegerPart = getIntegerPart(orderElementTemplate.getBudget());
        budgetFractionalPart = getFractionalPart(orderElementTemplate
                .getBudget());

        if (orderElementTemplate.isLeaf()) {
            BudgetLineTemplate budgetLineTemplate = (BudgetLineTemplate) orderElementTemplate;

            costOrSalaryIntegerPart = getIntegerPart(budgetLineTemplate
                    .getCostOrSalary());
            costOrSalaryFractionalPart = getFractionalPart(budgetLineTemplate
                    .getCostOrSalary());

            type = budgetLineTemplate.getBudgetLineType().toString();
            duration = budgetLineTemplate.getDuration();
            quantity = budgetLineTemplate.getQuantity();

            indemnizationSalaryIntegerPart = getIntegerPart(budgetLineTemplate
                    .getIndemnizationSalary());
            indemnizationSalaryFractionalPart = getFractionalPart(budgetLineTemplate
                    .getIndemnizationSalary());

            holidaySalaryIntegerPart = getIntegerPart(budgetLineTemplate
                    .getHolidaySalary());
            holidaySalaryFractionalPart = getFractionalPart(budgetLineTemplate
                    .getHolidaySalary());

            LocalDate startDate = budgetLineTemplate.getStartDate();
            if (startDate != null) {
                this.startDate = startDate.toDateTimeAtStartOfDay().toDate();
            }
            LocalDate endDate = budgetLineTemplate.getEndDate();
            if (endDate != null) {
                this.endDate = endDate.toDateTimeAtStartOfDay().toDate();
            }
        }
    }

    private BigDecimal getIntegerPart(BigDecimal value) {
        if (value == null) {
            return value;
        }
        return value.setScale(2, RoundingMode.DOWN);
    }

    private BigDecimal getFractionalPart(BigDecimal value) {
        if (value == null) {
            return value;
        }
        BigDecimal fractionalPart = value.subtract(value.setScale(0,
                RoundingMode.FLOOR));
        return (fractionalPart.compareTo(BigDecimal.ZERO) != 0) ? fractionalPart
                : null;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBudgetIntegerPart() {
        return budgetIntegerPart;
    }

    public BigDecimal getBudgetFractionalPart() {
        return budgetFractionalPart;
    }

    public BigDecimal getCostOrSalaryIntegerPart() {
        return costOrSalaryIntegerPart;
    }

    public BigDecimal getCostOrSalaryFractionalPart() {
        return costOrSalaryFractionalPart;
    }

    public String getType() {
        return type;
    }

    public Integer getDuration() {
        return duration;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getIndemnizationSalaryIntegerPart() {
        return indemnizationSalaryIntegerPart;
    }

    public BigDecimal getIndemnizationSalaryFractionalPart() {
        return indemnizationSalaryFractionalPart;
    }

    public BigDecimal getHolidaySalaryIntegerPart() {
        return holidaySalaryIntegerPart;
    }

    public BigDecimal getHolidaySalaryFractionalPart() {
        return holidaySalaryFractionalPart;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setType(String type) {
        this.type = type;
    }

}
