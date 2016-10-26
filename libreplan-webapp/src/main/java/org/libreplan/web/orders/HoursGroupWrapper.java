/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.web.orders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.INewObject;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLineGroup;
import org.libreplan.business.requirements.entities.CriterionRequirement;
import org.libreplan.business.requirements.entities.IndirectCriterionRequirement;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.CriterionWithItsType;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.business.templates.entities.OrderLineGroupTemplate;

/**
 * Wrapper represents the handled data in the form of assigning criterion requirement.
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class HoursGroupWrapper implements INewObject, Comparable<HoursGroupWrapper> {

    private static final Log LOG = LogFactory.getLog(HoursGroupWrapper.class);

    private Boolean newObject = false;

    private List<CriterionRequirementWrapper> directRequirementWrappers = new ArrayList<>();

    private List<CriterionRequirementWrapper> exceptionRequirementWrappers = new ArrayList<>();

    private OrderElement orderElement;

    private OrderElementTemplate template;

    private HoursGroup hoursGroup;

    public HoursGroupWrapper(HoursGroup hoursGroup, OrderElement orderElement, boolean newObject) {
        this.newObject = newObject;
        this.orderElement = orderElement;
        this.template = null;
        this.hoursGroup = hoursGroup;
        initRequirementWrappers(hoursGroup);
    }

    public HoursGroupWrapper(HoursGroup hoursGroup, OrderElementTemplate template, boolean newObject) {
        this.newObject = newObject;
        this.orderElement = null;
        this.template = template;
        this.hoursGroup = hoursGroup;
        initRequirementWrappers(hoursGroup);
    }

    private void initRequirementWrappers(HoursGroup hoursGroup) {
        directRequirementWrappers = new ArrayList<>();
        for (CriterionRequirement requirement : hoursGroup.getDirectCriterionRequirement()) {
            CriterionRequirementWrapper wrapper = new CriterionRequirementWrapper(requirement, this, false);
            directRequirementWrappers.add(wrapper);
        }

        exceptionRequirementWrappers = new ArrayList<>();
        for (CriterionRequirement requirement : getInvalidIndirectCriterionRequirement()) {
            CriterionRequirementWrapper wrapper = new CriterionRequirementWrapper(requirement, this, false);
            exceptionRequirementWrappers.add(wrapper);
        }
    }

    public String getCode() {
        return this.hoursGroup.getCode();
    }

    public void setCode(String code) {
        if (hoursGroup != null) {
            hoursGroup.setCode(code);
        }
    }

    public ResourceEnum getResourceType() {
        return hoursGroup.getResourceType();
    }

    /**
     * In ComboBox, value is ResourceEnum, but ZK returns String.
     */
    public void setResourceType(String resource) {
        if ( ResourceEnum.MACHINE.toString().equals(resource) ) {
            hoursGroup.setResourceType(ResourceEnum.MACHINE);
        } else {
            hoursGroup.setResourceType(ResourceEnum.WORKER);
        }
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

    /**
     * Used in _listOrderElementCriterionRequirements.zul
     * Should be public!
     */
    public Integer getWorkingHours() {
        return hoursGroup.getWorkingHours();
    }

    /**
     * Used in _listOrderElementCriterionRequirements.zul
     * Should be public!
     */
    public void setWorkingHours(Integer workingHours) {
        hoursGroup.setWorkingHours(workingHours);
    }

    public BigDecimal getPercentage() {
        return orderElementIsOrderLineGroup()
                ? getPercentageInOrderLineGroup(getWorkHours())
                : hoursGroup.getPercentage().scaleByPowerOfTen(2);
    }

    private boolean orderElementIsOrderLineGroup() {
        return getOrderElement() instanceof OrderLineGroup || getTemplate() instanceof OrderLineGroupTemplate;
    }

    private Integer getWorkHours() {
        return (getOrderElement() != null) ? getOrderElement().getWorkHours() : getTemplate().getWorkHours();
    }

    private OrderElement getOrderElement() {
        return orderElement;
    }

    private OrderElementTemplate getTemplate() {
        return template;
    }

    private BigDecimal getPercentageInOrderLineGroup(Integer workHours) {
        BigDecimal workingHours = new BigDecimal(hoursGroup.getWorkingHours()).setScale(2);
        BigDecimal total = new BigDecimal(workHours).setScale(2);

        return total.equals(new BigDecimal(0).setScale(2))
                ? new BigDecimal(0).setScale(2)
                : workingHours.divide(total, BigDecimal.ROUND_DOWN).scaleByPowerOfTen(2);
    }

    public void setPercentage(BigDecimal percentage) {
        if (percentage != null) {
            BigDecimal proportion = percentage.divide(new BigDecimal(100), BigDecimal.ROUND_DOWN);
            hoursGroup.setPercentage(proportion);
        } else {
            hoursGroup.setPercentage(new BigDecimal(0).setScale(2));
        }

    }

    /**
     * Used in _listOrderElementCriterionRequirements.zul
     * Should be public!
     */
    public Boolean getFixedPercentage() {
        return hoursGroup.isFixedPercentage();
    }

    /**
     * Used in _listOrderElementCriterionRequirements.zul
     * Should be public!
     */
    public void setFixedPercentage(Boolean fixedPercentage) {
        hoursGroup.setFixedPercentage(fixedPercentage);
    }

    public HoursGroup getHoursGroup() {
        return hoursGroup;
    }

    public void setHoursGroup(HoursGroup hoursGroup) {
        this.hoursGroup = hoursGroup;
    }

    /**
     * Used in _listOrderElementCriterionRequirements.zul
     * Should be public!
     */
    public boolean isPercentageReadOnly() {
        return (!hoursGroup.isFixedPercentage()) || (orderElementIsOrderLineGroup());
    }

    /**
     * Used in _listOrderElementCriterionRequirements.zul
     * Should be public!
     */
    public boolean isWorkingHoursReadOnly() {
        return (hoursGroup.isFixedPercentage()) || (orderElementIsOrderLineGroup());
    }

    /**
     * Operations to manage the criterions requirements
     */
    public void assignCriterionRequirementWrapper(CriterionRequirementWrapper newRequirementWrapper) {
        directRequirementWrappers.add(newRequirementWrapper);
    }

    public void addDirectCriterionToHoursGroup(CriterionRequirementWrapper requirementWrapper) {
        hoursGroup.addCriterionRequirement(requirementWrapper.getCriterionRequirement());
    }

    public void addExceptionRequirementWrappers(CriterionRequirementWrapper exceptionWrapper) {
        exceptionRequirementWrappers.add(exceptionWrapper);
    }

    public void selectCriterionToExceptionRequirementWrapper(
            CriterionRequirementWrapper exception, CriterionWithItsType criterionAndType) {

        exception.setCriterionWithItsType(criterionAndType);
        IndirectCriterionRequirement indirect = findValidRequirementByCriterion(criterionAndType.getCriterion());
        exception.setCriterionRequirement(indirect);
        exception.setValid(false);
    }

    public void removeDirectCriterionRequirementWrapper(CriterionRequirementWrapper directWrapper) {
        removeDirectCriterionRequirement(directWrapper);
        getDirectRequirementWrappers().remove(directWrapper);
    }

    public void removeDirectCriterionRequirement(CriterionRequirementWrapper directWrapper) {
        if (directWrapper.getCriterionWithItsType() != null) {
            CriterionRequirement direct = directWrapper.getCriterionRequirement();
            hoursGroup.removeCriterionRequirement(direct);
        }
    }

    public void removeExceptionCriterionRequirementWrapper(CriterionRequirementWrapper exception) {
        if (exception.getCriterionWithItsType() != null) {
            exception.setValid(true);
        }
        getExceptionRequirementWrappers().remove(exception);
    }

    public void updateListExceptionCriterionRequirementWrapper() {
        // Removes the old exception into list ExceptionRequirementWrappers
        List<CriterionRequirementWrapper> list = new ArrayList<>(getExceptionRequirementWrappers());

        for (CriterionRequirementWrapper exception : list) {

            if ( (exception.getCriterionWithItsType() != null) && (exceptionWasRemoved(exception)) ) {
                getExceptionRequirementWrappers().remove(exception);
            }
        }

        // Add new exception into list ExceptionRequirementWrappers
        for (CriterionRequirement requirement : getInvalidIndirectCriterionRequirement()) {
            CriterionRequirementWrapper exception = findRequirementWrapperByRequirement(requirement);
            if (exception == null) {
                exception = new CriterionRequirementWrapper(requirement, this, false);
                exceptionRequirementWrappers.add(exception);
            }
        }
    }

    private boolean exceptionWasRemoved(CriterionRequirementWrapper exception) {
        return !hoursGroup.getCriterionRequirements().contains(exception.getCriterionRequirement());
    }

    public void removeDirectCriterionsWithDifferentResourceType() {
        for (CriterionRequirement requirement : hoursGroup.getDirectCriterionRequirement()) {
            if (!hoursGroup.isValidResourceType(requirement)) {
                removeCriterionRequirementWrapper(requirement);
            } else if ( !hoursGroup.isValidResourceTypeChanged(requirement) ) {
                removeCriterionRequirementWrapper(requirement);
            }
        }
    }

    private void removeCriterionRequirementWrapper(CriterionRequirement requirement) {
        CriterionRequirementWrapper requirementWrapper = findRequirementWrapperByRequirement(requirement);
        if (requirementWrapper != null) {
            this.removeDirectCriterionRequirementWrapper(requirementWrapper);
        }
    }

    public List<CriterionRequirementWrapper> getCriterionRequirementWrappersView() {
        List<CriterionRequirementWrapper> list = new ArrayList<>();
        list.addAll(getDirectRequirementWrappers());
        list.addAll(getExceptionRequirementWrappers());

        return list;
    }

    /**
     * Used in _listHoursGroupCriterionRequirements.zul
     * Should be public!
     */
    public List<CriterionWithItsType> getValidCriterions() {
        List<CriterionWithItsType> list = new ArrayList<>();
        for (IndirectCriterionRequirement requirement : getValidIndirectCriterionRequirement()) {
            Criterion criterion = requirement.getCriterion();
            CriterionType type = criterion.getType();
            list.add(new CriterionWithItsType(type, criterion));
        }
        return list;
    }

    /**
     * Used in _listOrderElementCriterionRequirements.zul
     * Should be public!
     */
    public boolean dontExistValidCriterions() {
        return getValidCriterions().isEmpty();
    }

    private IndirectCriterionRequirement findValidRequirementByCriterion(Criterion criterion) {
        for (IndirectCriterionRequirement requirement : getValidIndirectCriterionRequirement()) {
            if (requirement.getCriterion().equals(criterion)) {
                return requirement;
            }
        }
        return null;
    }

    private CriterionRequirementWrapper findRequirementWrapperByRequirement(CriterionRequirement requirement) {
        for (CriterionRequirementWrapper requirementWrapper : this.getCriterionRequirementWrappersView()) {

            if ( (requirementWrapper.getCriterionRequirement() != null) &&
                    (requirementWrapper.getCriterionRequirement()).equals(requirement) ) {

                return requirementWrapper;
            }
        }
        return null;
    }

    private List<IndirectCriterionRequirement> getInvalidIndirectCriterionRequirement() {
        List<IndirectCriterionRequirement> result = new ArrayList<>();
        for (IndirectCriterionRequirement requirement : hoursGroup.getIndirectCriterionRequirement()) {
            if (!requirement.isValid()) {
                result.add(requirement);
            }
        }
        return result;
    }

    public List<IndirectCriterionRequirement> getValidIndirectCriterionRequirement() {
        List<IndirectCriterionRequirement> result = new ArrayList<>();
        for (IndirectCriterionRequirement requirement : hoursGroup.getIndirectCriterionRequirement()) {
            if (requirement.isValid()) {
                result.add(requirement);
            }
        }
        return result;
    }

    @Override
    public int compareTo(HoursGroupWrapper hoursGroupWrapper) {
        final String code = getCode();
        final String otherCode = hoursGroupWrapper.getCode();

        if (code == null) {
            LOG.warn(hoursGroup + " has a null code");
            return -1;
        }

        if (otherCode == null) {
            LOG.warn(hoursGroupWrapper.hoursGroup + " has a null code");
            return 1;
        }
        return code.compareTo(otherCode);
    }
}