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

    private BigDecimal budget;

    private BigDecimal costOrSalary;

    private String type;

    private Integer duration;

    private Integer quantity;

    private BigDecimal indemnizationSalary;

    private BigDecimal holidaySalary;

    public BudgetElementDTO(OrderElementTemplate orderElementTemplate) {
        code = orderElementTemplate.getCode();
        name = orderElementTemplate.getName();
        budget = orderElementTemplate.getBudget();

        if (orderElementTemplate.isLeaf()) {
            BudgetLineTemplate budgetLineTemplate = (BudgetLineTemplate) orderElementTemplate;
            costOrSalary = budgetLineTemplate.getCostOrSalary();
            type = budgetLineTemplate.getBudgetLineType().toString();
            duration = budgetLineTemplate.getDuration();
            quantity = budgetLineTemplate.getQuantity();
            indemnizationSalary = budgetLineTemplate.getIndemnizationSalary();
            holidaySalary = budgetLineTemplate.getHolidaySalary();
        }
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public BigDecimal getCostOrSalary() {
        return costOrSalary;
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

    public BigDecimal getIndemnizationSalary() {
        return indemnizationSalary;
    }

    public BigDecimal getHolidaySalary() {
        return holidaySalary;
    }

    public void setType(String type) {
        this.type = type;
    }

}
