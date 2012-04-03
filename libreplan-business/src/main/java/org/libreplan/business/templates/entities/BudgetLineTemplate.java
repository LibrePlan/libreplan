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
package org.libreplan.business.templates.entities;

import java.math.BigDecimal;

/**
 * Subclass of OrderLineTemplate with specific fields for LibrePlan audiovisual
 * templates.
 *
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
public class BudgetLineTemplate extends OrderLineTemplate {

    private BudgetLineTypeEnum budgetLineType;

    /**
     * Price or salary per unit and time period
     */
    private BigDecimal costOrSalary = BigDecimal.ZERO.setScale(2);

    /**
     * Length of the time period
     */
    private Integer duration;

    /**
     * Number of units
     */
    private Integer quantity;

    private BigDecimal indemnizationSalary = BigDecimal.ZERO.setScale(2);

    private BigDecimal holidaySalary = BigDecimal.ZERO.setScale(2);

    public BigDecimal getCostOrSalary() {
        return costOrSalary;
    }

    public void setCostOrSalary(BigDecimal costOrSalary) {
        this.costOrSalary = costOrSalary;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getIndemnizationSalary() {
        return indemnizationSalary;
    }

    public void setIndemnizationSalary(BigDecimal indemnizationSalary) {
        this.indemnizationSalary = indemnizationSalary;
    }

    public BigDecimal getHolidaySalary() {
        return holidaySalary;
    }

    public void setHolidaySalary(BigDecimal holidaySalary) {
        this.holidaySalary = holidaySalary;
    }

    public BudgetLineTypeEnum getBudgetLineType() {
        return budgetLineType;
    }

    public void setBudgetLineType(BudgetLineTypeEnum budgetLineType) {
        this.budgetLineType = budgetLineType;
    }
}
