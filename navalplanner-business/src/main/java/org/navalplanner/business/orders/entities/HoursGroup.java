/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.orders.entities;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ICriterionType;

public class HoursGroup extends BaseEntity implements Cloneable {

    public static HoursGroup create(OrderLine parentOrderLine) {
        HoursGroup result = new HoursGroup(parentOrderLine);
        result.setNewObject(true);
        return result;
    }

    @NotNull
    private Integer workingHours = 0;

    private BigDecimal percentage = new BigDecimal(0).setScale(2);

    private Boolean fixedPercentage = false;

    private Set<Criterion> criterions = new HashSet<Criterion>();

    @NotNull
    private OrderLine parentOrderLine;

    /**
     * Constructor for hibernate. Do not use!
     */
    public HoursGroup() {
    }

    private HoursGroup(OrderLine parentOrderLine) {
        this.parentOrderLine = parentOrderLine;
    }

    public void setWorkingHours(Integer workingHours)
            throws IllegalArgumentException {
        if (workingHours < 0) {
            throw new IllegalArgumentException(
                    "Working hours shouldn't be neagtive");
        }

        this.workingHours = workingHours;
    }

    public Integer getWorkingHours() {
        return workingHours;
    }

    /**
     * @param proportion
     *            It's one based, instead of one hundred based
     * @throws IllegalArgumentException
     *             if the new sum of percentages in the parent {@link OrderLine}
     *             surpasses one
     */
    public void setPercentage(BigDecimal proportion)
            throws IllegalArgumentException {
        BigDecimal oldPercentage = this.percentage;

        this.percentage = proportion;

        if (!parentOrderLine.isPercentageValid()) {
            this.percentage = oldPercentage;
            throw new IllegalArgumentException(
                    "Total percentage should be less than 100%");
        }
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setFixedPercentage(Boolean fixedPercentage) {
        this.fixedPercentage = fixedPercentage;
    }

    public Boolean isFixedPercentage() {
        return this.fixedPercentage;
    }

    public void setCriterions(Set<Criterion> criterions) {
        this.criterions = criterions;
    }

    public Set<Criterion> getCriterions() {
        return criterions;
    }

    public void addCriterion(Criterion criterion) {
        Criterion oldCriterion = getCriterionByType(criterion.getType());
        if (oldCriterion != null) {
            removeCriterion(oldCriterion);
        }

        criterions.add(criterion);
    }

    public void removeCriterion(Criterion criterion) {
        criterions.remove(criterion);
    }

    public Criterion getCriterionByType(ICriterionType<?> type) {
        for (Criterion criterion : criterions) {
            if (criterion.getType().equals(type)) {
                return criterion;
            }
        }

        return null;
    }

    public Criterion getCriterionByType(String type) {
        // TODO: Check if hoursgroup has criterions
        for (Criterion criterion : criterions) {
            if (criterion.getType().getName().equals(type)) {
                return criterion;
            }
        }

        return null;
    }

    public void removeCriterionByType(ICriterionType<?> type) {
        Criterion criterion = getCriterionByType(type);
        if (criterion != null) {
            removeCriterion(criterion);
        }
    }

    public void removeCriterionByType(String type) {
        Criterion criterion = getCriterionByType(type);
        if (criterion != null) {
            removeCriterion(criterion);
        }
    }

    public void setParentOrderLine(OrderLine parentOrderLine) {
        this.parentOrderLine = parentOrderLine;
    }

    public OrderLine getParentOrderLine() {
        return parentOrderLine;
    }

}
