/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.templates.entities.OrderLineTemplate;

public class HoursGroup extends IntegrationEntity implements Cloneable,
        ICriterionRequirable {

    private static final Log LOG = LogFactory.getLog(HoursGroup.class);

    private ResourceEnum resourceType = ResourceEnum.WORKER;

    private Integer workingHours = 0;

    private BigDecimal percentage = new BigDecimal(0).setScale(2);

    private Boolean fixedPercentage = false;

    private Set<CriterionRequirement> criterionRequirements = new HashSet<CriterionRequirement>();

    private OrderLine parentOrderLine;

    private OrderLineTemplate orderLineTemplate;

    private HoursGroup origin;

    protected CriterionRequirementOrderElementHandler criterionRequirementHandler =
        CriterionRequirementOrderElementHandler.getInstance();


    public static HoursGroup create(OrderLine parentOrderLine) {
        HoursGroup result = new HoursGroup(parentOrderLine);
        result.setNewObject(true);
        return result;
    }

    public static HoursGroup create(OrderLineTemplate orderLineTemplate) {
        HoursGroup result = new HoursGroup(orderLineTemplate);
        result.setNewObject(true);
        return result;
    }

    public static HoursGroup createUnvalidated(String code,
            ResourceEnum resourceType, Integer workingHours) {
        HoursGroup result = create(new HoursGroup());
        result.setCode(code);
        result.setResourceType(resourceType);
        result.setWorkingHours(workingHours);
        return result;
    }

    /**
     * Returns a copy of hoursGroup, and sets parent as its parent
     *
     * @param hoursGroup
     * @param parent
     * @return
     */
    public static HoursGroup copyFrom(HoursGroup hoursGroup, OrderLineTemplate parent) {
        HoursGroup result = copyFrom(hoursGroup);
        result.setCriterionRequirements(copyDirectCriterionRequirements(
                result, parent, hoursGroup.getDirectCriterionRequirement()));
        result.setOrderLineTemplate(parent);
        result.setParentOrderLine(null);
        return result;
    }

    private static Set<CriterionRequirement> copyDirectCriterionRequirements(
            HoursGroup hoursGroup,
            Object orderLine,
            Collection<DirectCriterionRequirement> criterionRequirements) {
        Set<CriterionRequirement> result = new HashSet<CriterionRequirement>();

        for (DirectCriterionRequirement each: criterionRequirements) {
            final DirectCriterionRequirement directCriterionRequirement = (DirectCriterionRequirement) each;
            DirectCriterionRequirement newDirectCriterionRequirement = DirectCriterionRequirement
                    .copyFrom(directCriterionRequirement, hoursGroup);
            newDirectCriterionRequirement.setHoursGroup(hoursGroup);
            result.add(newDirectCriterionRequirement);
        }
        return result;
    }

    public static HoursGroup copyFrom(HoursGroup hoursGroup, OrderLine parent) {
        HoursGroup result = copyFrom(hoursGroup);
        result.setCriterionRequirements(copyDirectCriterionRequirements(
                result, parent, hoursGroup.getDirectCriterionRequirement()));
        result.setOrderLineTemplate(null);
        result.setParentOrderLine(parent);
        return result;
    }

    private static HoursGroup copyFrom(HoursGroup hoursGroup) {
        HoursGroup result = createUnvalidated(
                hoursGroup.getCode(),
                hoursGroup.getResourceType(),
                hoursGroup.getWorkingHours());
        result.setCode(UUID.randomUUID().toString());
        result.percentage = hoursGroup.getPercentage();
        result.fixedPercentage = hoursGroup.isFixedPercentage();
        result.origin = hoursGroup;
        return result;
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public HoursGroup() {

    }

    private HoursGroup(OrderLine parentOrderLine) {
        this.parentOrderLine = parentOrderLine;
        String code = parentOrderLine.getCode();
        this.setCode(code != null ? code : "");
        this.setOrderLineTemplate(null);
    }

    private HoursGroup(OrderLineTemplate orderLineTemplate) {
        this.orderLineTemplate = orderLineTemplate;
        this.setParentOrderLine(null);
    }

    public ResourceEnum getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceEnum resource) {
        Validate.notNull(resource);
        this.resourceType = resource;
    }

    public void setWorkingHours(Integer workingHours)
            throws IllegalArgumentException {
        if ((workingHours != null) && (workingHours < 0)) {
            throw new IllegalArgumentException(
                    _("Working hours shouldn't be negative"));
        }
        if (workingHours == null) {
            workingHours = 0;
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

        if (!isPercentageValidForParent()) {
            this.percentage = oldPercentage;
            throw new IllegalArgumentException(
                    _("Total percentage should be less than 100%"));
        }
    }

    private boolean isPercentageValidForParent() {
        return (parentOrderLine != null) ? parentOrderLine.isPercentageValid()
                : orderLineTemplate.isPercentageValid();
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
        Set<CriterionRequirement> requirementsParent = criterionRequirementHandler
                .getRequirementWithSameResourType(
                        getCriterionRequirementsFromParent(), resourceType);
        Set<IndirectCriterionRequirement> currentIndirects = criterionRequirementHandler
                .getCurrentIndirectRequirements(
                        getIndirectCriterionRequirement(), requirementsParent);
        criterionRequirementHandler.removeOldIndirects(this, currentIndirects);
        criterionRequirementHandler.addNewsIndirects(this, currentIndirects);
    }

    public void propagateIndirectCriterionRequirementsKeepingValid() {
        updateMyCriterionRequirements();

        // Set valid value as original value for every indirect
        Map<Criterion, Boolean> mapCriterionToValid = createCriterionToValidMap(origin
                .getIndirectCriterionRequirement());

        for (CriterionRequirement each : criterionRequirements) {
            if (each instanceof IndirectCriterionRequirement) {
                IndirectCriterionRequirement indirect = (IndirectCriterionRequirement) each;
                indirect.setValid(mapCriterionToValid.get(each.getCriterion()));
            }
        }
    }

    private Map<Criterion, Boolean> createCriterionToValidMap(
            Set<IndirectCriterionRequirement> indirects) {
        Map<Criterion, Boolean> result = new HashMap<Criterion, Boolean>();

        for (IndirectCriterionRequirement each : indirects) {
            result.put(each.getCriterion(), each.isValid());
        }
        return result;
    }

    private Set<CriterionRequirement> getCriterionRequirementsFromParent() {
        return (parentOrderLine != null) ? parentOrderLine
                .getCriterionRequirements() : orderLineTemplate
                .getCriterionRequirements();
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

    public OrderLineTemplate getOrderLineTemplate() {
        return orderLineTemplate;
    }

    public void setOrderLineTemplate(OrderLineTemplate orderLineTemplate) {
        this.orderLineTemplate = orderLineTemplate;
    }

    public HoursGroup getOrigin() {
        return origin;
    }

    public void setOrigin(HoursGroup origin) {
        this.origin = origin;
    }

    @Override
    protected IIntegrationEntityDAO<? extends IntegrationEntity> getIntegrationEntityDAO() {
        return Registry.getHoursGroupDAO();
    }

    @Override
    public boolean checkConstraintUniqueCode() {
        // the automatic checking of this constraint is avoided because it uses
        // the wrong code property
        return true;
    }

    public static void checkConstraintHoursGroupUniqueCode(OrderElement order) {
        HoursGroup repeatedHoursGroup;

        if (order instanceof OrderLineGroup) {
            repeatedHoursGroup = ((OrderLineGroup) order).findRepeatedHoursGroupCode();
            if (repeatedHoursGroup != null) {
                throw new ValidationException(_(
                        "Repeated Hours Group code {0} in Project {1}",
                        repeatedHoursGroup.getCode(), repeatedHoursGroup
                                .getParentOrderLine().getName()));
            }
        }

        repeatedHoursGroup = Registry.getHoursGroupDAO()
                .findRepeatedHoursGroupCodeInDB(order.getHoursGroups());
        if (repeatedHoursGroup != null) {
            throw new ValidationException(_(
                    "Repeated Hours Group code {0} in Project {1}",
                    repeatedHoursGroup.getCode(), repeatedHoursGroup
                            .getParentOrderLine().getName()));
        }
    }

}
