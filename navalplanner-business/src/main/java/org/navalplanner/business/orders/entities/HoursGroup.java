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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;


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

    private Set<CriterionRequirement> criterionRequirements = new HashSet<CriterionRequirement>();

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

    public void setCriterionRequirements(Set<CriterionRequirement> criterionRequirements) {
        this.criterionRequirements = criterionRequirements;
    }

    public Set<CriterionRequirement> getCriterionRequirements() {
        return criterionRequirements;
    }

    public Set<Criterion> getCriterions() {
        Set<Criterion> criterions = new HashSet<Criterion>();
        for(CriterionRequirement criterionRequirement: criterionRequirements){
            criterions.add(criterionRequirement.getCriterion());
        }
        return Collections.unmodifiableSet(criterions);
    }

    public void addCriterionRequirement(CriterionRequirement requirement) {
        if (canAddCriterionRequirement(requirement)) {
            requirement.setHoursGroup(this);
            criterionRequirements.add(requirement);
        } else {
            throw new IllegalStateException(
                    " The "
                            + requirement.getCriterion().getName()
                            + " can not be assigned to this hoursGroup because it already exist in other hoursGroup");
        }
    }

    private boolean canAddCriterionRequirement(
            CriterionRequirement newRequirement) {
        for (CriterionRequirement requirement : criterionRequirements) {
            if (requirement.getCriterion()
                    .equals(newRequirement.getCriterion())) {
                return false;
            }
        }
        return true;
    }

    /* TO REMOVE */
    public void removeDirectCriterionRequirement(Criterion criterion){
        CriterionRequirement oldCriterionRequirement =
 getDirectCriterionRequirementByCriterion(criterion);
        if (oldCriterionRequirement != null) {
            removeCriterionRequirement(oldCriterionRequirement);
        }
    }

    public void removeCriterionRequirement(CriterionRequirement criterionRequirement) {
        criterionRequirements.remove(criterionRequirement);
    }

    /* TO REMOVE */
    public CriterionRequirement getDirectCriterionRequirementByCriterion(
            Criterion criterion) {
        for (CriterionRequirement requirement : getDirectCriterionRequirement()) {
                Criterion oldCriterion = requirement.getCriterion();
            if ((oldCriterion != null)
                    && (criterion.getId().equals(oldCriterion.getId()))) {
                    return requirement;
                }
        }
        return null;
    }

    /* TO REMOVE */
    public void addDirectRequirementCriterion(Criterion criterion) {
        CriterionRequirement newCriterionRequirement =
                DirectCriterionRequirement.create(criterion);
        addCriterionRequirement(newCriterionRequirement);
    }

    /* TO REMOVE */
    public Criterion getDirectCriterion(Criterion criterion) {
        CriterionRequirement requirement = getDirectCriterionRequirementByCriterion(criterion);
        if (requirement != null) {
            return requirement.getCriterion();
        }
        return null;
    }

    public void setParentOrderLine(OrderLine parentOrderLine) {
        this.parentOrderLine = parentOrderLine;
    }

    public OrderLine getParentOrderLine() {
        return parentOrderLine;
    }

    private List<DirectCriterionRequirement> getDirectCriterionRequirement() {
        List<DirectCriterionRequirement> list = new ArrayList<DirectCriterionRequirement>();
        for(CriterionRequirement criterionRequirement : criterionRequirements ){
            if(criterionRequirement instanceof DirectCriterionRequirement){
                list.add((DirectCriterionRequirement) criterionRequirement);
            }
        }
        return list;
    }
}
