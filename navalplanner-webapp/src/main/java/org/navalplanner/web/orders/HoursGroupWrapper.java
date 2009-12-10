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

package org.navalplanner.web.orders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.INewObject;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ResourceEnum;

/**
 * Wrapper represents the handled data in the form of assigning criterion
 * requirement.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class HoursGroupWrapper implements INewObject, Comparable {

    private Boolean newObject = false;

    private List<CriterionRequirementWrapper> directRequirementWrappers = new ArrayList<CriterionRequirementWrapper>();

    private List<CriterionRequirementWrapper> exceptionRequirementWrappers = new ArrayList<CriterionRequirementWrapper>();

    private OrderElement orderElement;

    private HoursGroup hoursGroup;

    public HoursGroupWrapper(HoursGroup hoursGroup, OrderElement orderElement,
            boolean newObject) {
        this.newObject = newObject;
        this.orderElement = orderElement;
        this.hoursGroup = hoursGroup;
        initRequirementWrappers(hoursGroup);
    }

    private void initRequirementWrappers(HoursGroup hoursGroup) {
        directRequirementWrappers = new ArrayList<CriterionRequirementWrapper>();
        for (CriterionRequirement requirement : hoursGroup
                .getDirectCriterionRequirement()) {
            CriterionRequirementWrapper wrapper = new CriterionRequirementWrapper(
                    requirement, this, false);
            directRequirementWrappers.add(wrapper);
        }

        exceptionRequirementWrappers = new ArrayList<CriterionRequirementWrapper>();
        for (CriterionRequirement requirement : getInvalidIndirectCriterionRequirement()) {
            CriterionRequirementWrapper wrapper = new CriterionRequirementWrapper(
                    requirement, this, false);
            exceptionRequirementWrappers.add(wrapper);
        }
    }

    public String getName() {
        return this.hoursGroup.getName();
    }

    public void setName(String name) {
        if (hoursGroup != null) {
            hoursGroup.setName(name);
        }
    }

    public ResourceEnum getResourceType() {
        return hoursGroup.getResourceType();
    }

    public void setResourceType(String resource) {
    }

    public void assignResourceType(ResourceEnum resource) {
        hoursGroup.setResourceType(resource);
    }

    public void setNewObject(Boolean isNewObject) {
        this.newObject = isNewObject;
    }

    public boolean isOldObject() {
        return !isNewObject();
    }

    @Override
    public boolean isNewObject() {
        return newObject == null ? false : newObject;
    }

    public List<CriterionRequirementWrapper> getDirectRequirementWrappers() {
        return directRequirementWrappers;
    }

    public List<CriterionRequirementWrapper> getExceptionRequirementWrappers() {
        return exceptionRequirementWrappers;
    }

    public Integer getWorkingHours() {
        return hoursGroup.getWorkingHours();
    }

    public void setWorkingHours(Integer workingHours) {
        hoursGroup.setWorkingHours(workingHours);
    }

    public BigDecimal getPercentage() {
        if (orderElement instanceof OrderLineGroup) {
            return getPercentageInOrderLineGroup();
        }
        return hoursGroup.getPercentage().scaleByPowerOfTen(2);
    }

    private BigDecimal getPercentageInOrderLineGroup() {
        BigDecimal workingHours = new BigDecimal(hoursGroup.getWorkingHours())
                .setScale(2);
        BigDecimal total = new BigDecimal(orderElement.getWorkHours())
                .setScale(2);
        if (total.equals(new BigDecimal(0).setScale(2))) {
            return new BigDecimal(0).setScale(2);
        }
        return workingHours.divide(total, BigDecimal.ROUND_DOWN)
                .scaleByPowerOfTen(2);
    }

    public void setPercentage(BigDecimal percentage) {
        if (percentage != null) {
            BigDecimal proportion = percentage.divide(
                    new BigDecimal(100), BigDecimal.ROUND_DOWN);
            hoursGroup.setPercentage(proportion);
        } else {
            hoursGroup.setPercentage(new BigDecimal(0).setScale(2));
        }

    }

    public Boolean getFixedPercentage() {
        return hoursGroup.isFixedPercentage();
    }

    public void setFixedPercentage(Boolean fixedPercentage) {
        hoursGroup.setFixedPercentage(fixedPercentage);
    }

    public HoursGroup getHoursGroup() {
        return hoursGroup;
    }

    public void setHoursGroup(HoursGroup hoursGroup) {
        this.hoursGroup = hoursGroup;
    }

    public boolean isPercentageReadOnly() {
        return (!hoursGroup.isFixedPercentage())
                || ((orderElement instanceof OrderLineGroup));
    }

    public boolean isWorkingHoursReadOnly() {
        return (hoursGroup.isFixedPercentage())
                || ((orderElement instanceof OrderLineGroup));
    }

    /* Operations to manage the criterions requirements */
    public void assignCriterionRequirementWrapper(
            CriterionRequirementWrapper newRequirementWrapper) {
            directRequirementWrappers.add(newRequirementWrapper);
    }

    public boolean canSelectCriterion(
            CriterionRequirementWrapper requirementWrapper) {
        return hoursGroup.canAddCriterionRequirement(requirementWrapper
                .getCriterionRequirement());
    }

    public void addDirectCriterionToHoursGroup(
            CriterionRequirementWrapper requirementWrapper) {
        hoursGroup.addCriterionRequirement(requirementWrapper
                .getCriterionRequirement());
    }

    public void addExceptionRequirementWrappers(
            CriterionRequirementWrapper exceptionWrapper) {
        exceptionRequirementWrappers.add(exceptionWrapper);
    }

    public void selectCriterionToExceptionRequirementWrapper(
            CriterionRequirementWrapper exception,
            CriterionWithItsType criterionAndType) {
        exception.setCriterionWithItsType(criterionAndType);
        IndirectCriterionRequirement indirect = findValidRequirementByCriterion(criterionAndType
                .getCriterion());
        exception.setCriterionRequirement(indirect);
        exception.setValid(false);
    }

    public void removeDirectCriterionRequirementWrapper(
            CriterionRequirementWrapper directWrapper) {
        removeDirectCriterionRequirement(directWrapper);
        getDirectRequirementWrappers().remove(directWrapper);
    }

    public void removeDirectCriterionRequirement(
            CriterionRequirementWrapper directWrapper) {
        if (directWrapper.getCriterionWithItsType() != null) {
            CriterionRequirement direct = directWrapper
                    .getCriterionRequirement();
            hoursGroup.removeCriterionRequirement(direct);
        }
    }

    public void removeExceptionCriterionRequirementWrapper(
            CriterionRequirementWrapper exception) {
        if (exception.getCriterionWithItsType() != null) {
            exception.setValid(true);
        }
        getExceptionRequirementWrappers().remove(exception);
    }

    public void updateListExceptionCriterionRequirementWrapper() {
        // removes the old exception into list ExceptionRequirementWrappers
        List<CriterionRequirementWrapper> list = new ArrayList<CriterionRequirementWrapper>(
                getExceptionRequirementWrappers());
        for (CriterionRequirementWrapper exception : list) {
            if ((exception.getCriterionWithItsType() != null)
                    && (exceptionWasRemoved(exception))) {
                getExceptionRequirementWrappers().remove(exception);
            }
        }
        // Add new exception into list ExceptionRequirementWrappers
        for (CriterionRequirement requirement : getInvalidIndirectCriterionRequirement()) {
            CriterionRequirementWrapper exception = findRequirementWrapperByRequirement(requirement);
            if (exception == null) {
                exception = new CriterionRequirementWrapper(requirement, this,
                        false);
                exceptionRequirementWrappers.add(exception);
            }
        }
    }

    private boolean exceptionWasRemoved(CriterionRequirementWrapper exception) {
        CriterionRequirement requirement = exception.getCriterionRequirement();
        if (hoursGroup.getCriterionRequirements().contains(requirement)) {
            return false;
        }
        return true;
    }

    public void removeDirectCriterionsWithDiferentResourceType() {
        for (CriterionRequirement requirement : hoursGroup
                .getDirectCriterionRequirement()) {
            if(!hoursGroup.isValidResourceType(requirement)){
                removeCriterionRequirementWrapper(requirement);
            }
        }
    }

    private void removeCriterionRequirementWrapper(
            CriterionRequirement requirement) {
        CriterionRequirementWrapper requirementWrapper = findRequirementWrapperByRequirement(requirement);
        if (requirementWrapper != null) {
            this.removeDirectCriterionRequirementWrapper(requirementWrapper);
        }
    }

    public List<CriterionRequirementWrapper> getCriterionRequirementWrappersView() {
        List<CriterionRequirementWrapper> list = new ArrayList<CriterionRequirementWrapper>();
        list.addAll(getDirectRequirementWrappers());
        list.addAll(getExceptionRequirementWrappers());
        return list;
    }

    public List<CriterionWithItsType> getValidCriterions() {
        List<CriterionWithItsType> list = new ArrayList<CriterionWithItsType>();
        for (IndirectCriterionRequirement requirement : getValidIndirectCriterionRequirement()) {
            Criterion criterion = requirement.getCriterion();
            CriterionType type = criterion.getType();
            list.add(new CriterionWithItsType(type, criterion));
        }
        return list;
    }

    public boolean dontExistValidCriterions() {
        return !(getValidCriterions().size() > 0);
    }

    private IndirectCriterionRequirement findValidRequirementByCriterion(
            Criterion criterion) {
        for (IndirectCriterionRequirement requirement : getValidIndirectCriterionRequirement()) {
            if (requirement.getCriterion().equals(criterion)) {
                return requirement;
            }
        }
        return null;
    }

    private CriterionRequirementWrapper findRequirementWrapperByRequirement(
            CriterionRequirement requirement) {
        for (CriterionRequirementWrapper requirementWrapper : this
                .getCriterionRequirementWrappersView()) {
            if ((requirementWrapper.getCriterionRequirement() != null)
                    && (requirementWrapper.getCriterionRequirement())
                    .equals(requirement)) {
                return requirementWrapper;
            }
        }
        return null;
    }

    private List<IndirectCriterionRequirement> getInvalidIndirectCriterionRequirement() {
        List<IndirectCriterionRequirement> result = new ArrayList<IndirectCriterionRequirement>();
        for (IndirectCriterionRequirement requirement : hoursGroup
                .getIndirectCriterionRequirement()) {
            if (!requirement.isIsValid()) {
                result.add(requirement);
            }
        }
        return result;
    }

    public List<IndirectCriterionRequirement> getValidIndirectCriterionRequirement() {
        List<IndirectCriterionRequirement> result = new ArrayList<IndirectCriterionRequirement>();
        for (IndirectCriterionRequirement requirement : hoursGroup
                .getIndirectCriterionRequirement()) {
            if (requirement.isIsValid()) {
                result.add(requirement);
            }
        }
        return result;
    }

    @Override
    public int compareTo(Object arg0) {
        final HoursGroupWrapper hoursGroupWrapper = (HoursGroupWrapper) arg0;
        return getName().compareTo(hoursGroupWrapper.getName());
    }
}