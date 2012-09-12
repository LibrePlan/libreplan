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

    private Boolean root = false;

    public BudgetElementDTO(OrderElementTemplate orderElementTemplate) {
        code = orderElementTemplate.getCode();
        name = Util.getPrefixSpacesDependingOnDepth(orderElementTemplate)
                + orderElementTemplate.getName();

        budgetIntegerPart = Util.getIntegerPart(orderElementTemplate
                .getBudget());
        budgetFractionalPart = Util.getFractionalPart(orderElementTemplate
                .getBudget());

        if (orderElementTemplate.isLeaf()) {
            BudgetLineTemplate budgetLineTemplate = (BudgetLineTemplate) orderElementTemplate;

            costOrSalaryIntegerPart = Util.getIntegerPart(budgetLineTemplate
                    .getCostOrSalary());
            costOrSalaryFractionalPart = Util
                    .getFractionalPart(budgetLineTemplate.getCostOrSalary());

            type = budgetLineTemplate.getBudgetLineType().toString();
            duration = budgetLineTemplate.getDuration();
            quantity = budgetLineTemplate.getQuantity();

            indemnizationSalaryIntegerPart = Util
                    .getIntegerPart(budgetLineTemplate.getIndemnizationSalary());
            indemnizationSalaryFractionalPart = Util
                    .getFractionalPart(budgetLineTemplate
                            .getIndemnizationSalary());

            holidaySalaryIntegerPart = Util.getIntegerPart(budgetLineTemplate
                    .getHolidaySalary());
            holidaySalaryFractionalPart = Util
                    .getFractionalPart(budgetLineTemplate.getHolidaySalary());

            LocalDate startDate = budgetLineTemplate.getStartDate();
            if (startDate != null) {
                this.startDate = startDate.toDateTimeAtStartOfDay().toDate();
            }
            LocalDate endDate = budgetLineTemplate.getEndDate();
            if (endDate != null) {
                this.endDate = endDate.toDateTimeAtStartOfDay().toDate();
            }
        }

        root = Util.isRoot(orderElementTemplate);
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

    public Boolean getRoot() {
        return root;
    }

    public void setType(String type) {
        this.type = type;
    }

}
