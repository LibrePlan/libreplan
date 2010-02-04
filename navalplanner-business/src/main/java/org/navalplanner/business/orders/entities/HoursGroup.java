/*
 * This file is part of NavalPlan
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

import static org.navalplanner.business.i18n.I18nHelper._;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IHoursGroupDAO;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ResourceEnum;

public class HoursGroup extends BaseEntity implements Cloneable,
        ICriterionRequirable {

    private static final Log LOG = LogFactory.getLog(HoursGroup.class);

    public static HoursGroup create(OrderLine parentOrderLine) {
        HoursGroup result = new HoursGroup(parentOrderLine);
        result.setNewObject(true);
        return result;
    }

    public static HoursGroup createUnvalidated(String name,
            ResourceEnum resourceType, Integer workingHours) {
        HoursGroup result = new HoursGroup();
        result.setCode(name);
        result.setResourceType(resourceType);
        result.setWorkingHours(workingHours);
        return create(result);
    }

    private String code;

    private ResourceEnum resourceType = ResourceEnum.WORKER;

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

    @NotEmpty(message = "code not specified")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ResourceEnum getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceEnum resource) {
        if ((resource != null) && (resource.equals(ResourceEnum.getDefault()))) {
            throw new IllegalArgumentException(
                    _("the resource type should be Worker or Machine"));
        }
        this.resourceType = resource;
    }

    public void setWorkingHours(Integer workingHours)
            throws IllegalArgumentException {
        if ((workingHours != null) && (workingHours < 0)) {
            throw new IllegalArgumentException(
                    _("Working hours shouldn't be neagtive"));
        }

        this.workingHours = workingHours;
    }

    @NotNull(message = "working hours not specified")
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
                    _("Total percentage should be less than 100%"));
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
    @Override
    public Set<CriterionRequirement> getCriterionRequirements() {
        return criterionRequirements;
    }

    public Set<Criterion> getValidCriterions() {
        Set<Criterion> criterions = new HashSet<Criterion>();
        for (CriterionRequirement criterionRequirement : getDirectCriterionRequirement()) {
            criterions.add(criterionRequirement.getCriterion());
        }
        for (IndirectCriterionRequirement requirement : getIndirectCriterionRequirement()) {
            if (requirement.isValid()) {
                criterions.add(requirement.getCriterion());
            }
        }
        return Collections.unmodifiableSet(criterions);
    }

    @Override
    public void addCriterionRequirement(CriterionRequirement requirement) {
        if (!isValidResourceType(requirement)) {
            throw new IllegalStateException(
                    _(
                            " The criterion {0} can not be assigned to this hoursGroup because its resource type is diferent.",
                            requirement.getCriterion().getName()));
        }
        if (existSameCriterionRequirement(requirement)) {
            throw new IllegalStateException(
                    _(
                            " The criterion  {0} can not be assigned to this hoursGroup because it already exist into the hoursGroup.",
                            requirement.getCriterion().getName()));

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
            return (resourceType.equals(resourceTypeRequirement) || (resourceTypeRequirement
                    .equals(ResourceEnum.getDefault())));
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

    @AssertTrue(message = "code is already being used")
    public boolean checkConstraintUniqueCode() {
        if (code == null) {
            LOG.warn("Hours group code is null. "
                    + "Not checking unique code since it would fail");
            return true;
        }
        IHoursGroupDAO hoursGroupDAO = Registry.getHoursGroupDAO();
        if (isNewObject()) {
            return !hoursGroupDAO.existsByCodeAnotherTransaction(this);
        } else {
            try {
                HoursGroup hoursGroup = hoursGroupDAO
                        .findUniqueByCodeAnotherTransaction(this);
                return hoursGroup.getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            }
        }
    }

}
