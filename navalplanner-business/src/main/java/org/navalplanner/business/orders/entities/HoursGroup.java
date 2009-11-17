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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ResourceEnum;


public class HoursGroup extends BaseEntity implements Cloneable {

    public static HoursGroup create(OrderLine parentOrderLine) {
        HoursGroup result = new HoursGroup(parentOrderLine);
        result.setNewObject(true);
        return result;
    }

    private String name;

    private ResourceEnum resourceType = ResourceEnum.WORKER;

    @NotNull
    private Integer workingHours = 0;

    private BigDecimal percentage = new BigDecimal(0).setScale(2);

    private Boolean fixedPercentage = false;

    private Set<CriterionRequirement> criterionRequirements = new HashSet<CriterionRequirement>();

    @NotNull
    private OrderLine parentOrderLine;

    protected CriterionRequirementHandler criterionRequirementHandler = CriterionRequirementHandler
            .getInstance();

    /**
     * Constructor for hibernate. Do not use!
     */
    public HoursGroup() {
    }

    private HoursGroup(OrderLine parentOrderLine) {
        this.parentOrderLine = parentOrderLine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceEnum getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceEnum resource) {
        if ((resource != null) && (resource.equals(ResourceEnum.getDefault()))) {
            throw new IllegalArgumentException(
                    "the resource type should be Worker or Machine");
        }
        this.resourceType = resource;
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

    @Valid
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
        if (!isValidResourceType(requirement)) {
            throw new IllegalStateException(
                    " The criterion "
                            + requirement.getCriterion().getName()
                            + " can not be assigned to this hoursGroup because its resource type is diferent.");
        }
        if (existSameCriterionRequirement(requirement)) {
            throw new IllegalStateException(
                    " The criterion "
                            + requirement.getCriterion().getName()
                            + " can not be assigned to this hoursGroup because it already exist into the hoursGroup");

        }
        requirement.setHoursGroup(this);
        criterionRequirements.add(requirement);
    }

    public boolean canAddCriterionRequirement(
            CriterionRequirement newRequirement) {
        if ((isValidResourceType(newRequirement))
                && (!existSameCriterionRequirement(newRequirement))) {
            return false;
        }
        return true;
    }


    public void removeCriterionRequirement(CriterionRequirement requirement) {
        criterionRequirements.remove(requirement);
        if (requirement instanceof IndirectCriterionRequirement) {
            ((IndirectCriterionRequirement) requirement).getParent()
                    .getChildren().remove(
                            (IndirectCriterionRequirement) requirement);
        }
        requirement.setCriterion(null);
        requirement.setHoursGroup(null);
        requirement.setOrderElement(null);
    }

    public void setParentOrderLine(OrderLine parentOrderLine) {
        this.parentOrderLine = parentOrderLine;
    }

    public OrderLine getParentOrderLine() {
        return parentOrderLine;
    }

    public void updateMyCriterionRequirements() {
        OrderElement newParent = this.getParentOrderLine();
        Set<CriterionRequirement> requirementsParent = criterionRequirementHandler
                .getRequirementWithSameResourType(newParent
                        .getCriterionRequirements(), resourceType);
        Set<IndirectCriterionRequirement> currentIndirects = criterionRequirementHandler
                .getCurrentIndirectRequirements(
                        getIndirectCriterionRequirement(), requirementsParent);
        criterionRequirementHandler.removeOldIndirects(this, currentIndirects);
        criterionRequirementHandler.addNewsIndirects(this, currentIndirects);
    }

    public Set<IndirectCriterionRequirement> getIndirectCriterionRequirement() {
        Set<IndirectCriterionRequirement> list = new HashSet<IndirectCriterionRequirement>();
        for(CriterionRequirement criterionRequirement : criterionRequirements ){
            if(criterionRequirement instanceof IndirectCriterionRequirement){
                list.add((IndirectCriterionRequirement) criterionRequirement);
            }
        }
        return list;
    }

    public Set<DirectCriterionRequirement> getDirectCriterionRequirement() {
        Set<DirectCriterionRequirement> list = new HashSet<DirectCriterionRequirement>();
        for(CriterionRequirement criterionRequirement : criterionRequirements ){
            if(criterionRequirement instanceof DirectCriterionRequirement){
                list.add((DirectCriterionRequirement) criterionRequirement);
            }
        }
        return list;
    }

    public boolean isValidResourceType(CriterionRequirement newRequirement) {
        ResourceEnum resourceTypeRequirement = newRequirement.getCriterion()
                .getType().getResource();
        if (resourceType != null) {
            return resourceType.equals(resourceTypeRequirement);
        }
        return true;
    }

    boolean existSameCriterionRequirement(
            CriterionRequirement newRequirement) {
        Criterion criterion = newRequirement.getCriterion();
        for(CriterionRequirement requirement : getCriterionRequirements()){
            if (requirement.getCriterion().equals(criterion)) {
                return true;
            }
        }
        return false;
    }

}
