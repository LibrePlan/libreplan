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

package org.navalplanner.web.orders.criterionrequirements;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.web.orders.CriterionRequirementWrapper;
import org.navalplanner.web.orders.HoursGroupWrapper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public abstract class AssignedCriterionRequirementModel<T, M> implements
        IAssignedCriterionRequirementModel<T, M> {

    protected List<CriterionWithItsType> criterionWithItsTypes =
            new ArrayList<CriterionWithItsType>();

    protected List<CriterionRequirementWrapper> criterionRequirementWrappers =
            new ArrayList<CriterionRequirementWrapper>();

    protected List<HoursGroupWrapper> hoursGroupsWrappers = new ArrayList<HoursGroupWrapper>();


    @Override
    public abstract Set<CriterionType> getTypes();

    @Override
    public List<CriterionWithItsType> getCriterionWithItsTypes(){
        return criterionWithItsTypes;
    }

    public abstract Integer getTotalHours();

    @Override
    @Transactional(readOnly = true)
    public void assignCriterionRequirementWrapper() {
        if((getModel() != null) && (getElement() != null)){
            CriterionRequirementWrapper newRequirementWrapper = createCriterionRequirementWreapper(null);
            criterionRequirementWrappers.add(newRequirementWrapper);
        }
    }

    private CriterionRequirementWrapper createCriterionRequirementWreapper(
            HoursGroupWrapper hoursGroupWrapper) {
        CriterionRequirement newRequirement = DirectCriterionRequirement
                .create();
        CriterionRequirementWrapper newRequirementWrapper = new CriterionRequirementWrapper(
                newRequirement, hoursGroupWrapper, true);
        return newRequirementWrapper;
    }

    public void changeCriterionAndType(
            CriterionRequirementWrapper requirementWrapper,
            CriterionWithItsType newCriterionAndType) {
        CriterionWithItsType oldCriterionAndType = requirementWrapper
                .getCriterionWithItsType();
        if ((oldCriterionAndType == null)
                || (!oldCriterionAndType.equals(newCriterionAndType))) {
            removeOldCriterionAndType(requirementWrapper);
            requirementWrapper.setCriterionWithItsType(newCriterionAndType);
            addNewCriterionAndType(requirementWrapper);
            updateExceptionInHoursGroups();
        }
    }

    private void removeOldCriterionAndType(
            CriterionRequirementWrapper requirementWrapper) {
        if (requirementWrapper.getCriterionWithItsType() != null) {
            removeCriterionRequirement(requirementWrapper);
        }
    }

    private void addNewCriterionAndType(
            CriterionRequirementWrapper requirementWrapper) {
        DirectCriterionRequirement requirement = (DirectCriterionRequirement) requirementWrapper
                .getCriterionRequirement();
        addDirectCriterionRequirement(requirement);
    }

    protected abstract void addDirectCriterionRequirement(DirectCriterionRequirement requirement);

    @Override
    public void deleteCriterionRequirementWrapper(
            CriterionRequirementWrapper requirementWrapper) {
        removeCriterionRequirement(requirementWrapper);
        criterionRequirementWrappers.remove(requirementWrapper);
        updateExceptionInHoursGroups();
    }

    private void removeCriterionRequirement(
            CriterionRequirementWrapper requirementWrapper) {
        DirectCriterionRequirement requirement = (DirectCriterionRequirement) requirementWrapper
                .getCriterionRequirement();
        removeDirectCriterionRequirement(requirement);
    }

    protected abstract void removeDirectCriterionRequirement(DirectCriterionRequirement requirement);

    @Override
    public List<CriterionRequirementWrapper> getCriterionRequirementWrappers() {
        if ((getModel() != null) && (getElement() != null)) {
            return criterionRequirementWrappers;
        }
        return new ArrayList<CriterionRequirementWrapper>();
    }

    @Override
    public void setValidCriterionRequirementWrapper(
            CriterionRequirementWrapper requirementWrapper, boolean valid) {
        requirementWrapper.setValid(valid);
        IndirectCriterionRequirement requirement = (IndirectCriterionRequirement) requirementWrapper
                .getCriterionRequirement();
        setValidCriterionRequirement(requirement, valid);
        if (requirementWrapper.getCriterionWithItsType() != null) {
            updateExceptionInHoursGroups();
        }
    }

    protected abstract void setValidCriterionRequirement(IndirectCriterionRequirement requirement, boolean valid);

    public CriterionRequirementWrapper validateWrappers(
            List<CriterionRequirementWrapper> list) {
        for (CriterionRequirementWrapper requirementWrapper : list) {
            if (requirementWrapper.getCriterionWithItsType() == null) {
                return requirementWrapper;
            }
        }
        return null;
    }

    public CriterionRequirementWrapper validateHoursGroupWrappers() {
        for (HoursGroupWrapper hoursGroupWrapper : hoursGroupsWrappers) {
            CriterionRequirementWrapper requirementWrapper = validateWrappers(hoursGroupWrapper
                    .getCriterionRequirementWrappersView());
            if (requirementWrapper != null) {
                return requirementWrapper;
            }
        }
        return null;
    }

    /*
     * Operations to manage the hours groups (add new hours group, delete a
     * hours group , edit a hours group ), and assign criterions requirements to
     * the hoursGroup
     */

    private OrderLine asOrderLine() {
        return (getElement() instanceof OrderLine) ? (OrderLine) getElement() : null;
    }

    public void addNewHoursGroupWrapper() {
        if ((getModel() != null) && (getElement() != null)) {
            HoursGroup newHoursGroup = createNewHoursGroup();
            addNewHoursGroupWrapper(newHoursGroup, true);
        }
    }

    protected abstract void addNewHoursGroupWrapper(HoursGroup newHoursGroup, boolean newObject);

    private HoursGroup createNewHoursGroup() {
        if (asOrderLine() != null) {
            HoursGroup newHoursGroup = HoursGroup.create(asOrderLine());
            (asOrderLine()).addHoursGroup(newHoursGroup);

            // Set generated name
            int number = (asOrderLine()).getHoursGroups().size() - 1;
            newHoursGroup.setCode(_("New hours group ") + number);

            return newHoursGroup;
        }
        return null;
    }

    public List<HoursGroupWrapper> getHoursGroupsWrappers() {
        if ((getModel() != null) && (getElement() != null)) {
            return hoursGroupsWrappers;
        }
        return new ArrayList<HoursGroupWrapper>();
    }

    public void deleteHoursGroupWrapper(HoursGroupWrapper hoursGroupWrapper) {
        if (asOrderLine() != null) {
            HoursGroup hoursGroup = hoursGroupWrapper.getHoursGroup();
            asOrderLine().deleteHoursGroup(hoursGroup);
            hoursGroupsWrappers.remove(hoursGroupWrapper);
        }
    }

    /*
     * Operation to manage the criterion Requirements for the hoursGroups. The
     * operations is add new direct criterion requirement, delete criterion
     * requirement, add new Exception and delete Exception
     */
    public void addCriterionToHoursGroupWrapper(
            HoursGroupWrapper hoursGroupWrapper) {
        if ((getModel() != null) && (getElement() != null)) {
            CriterionRequirementWrapper requirement = createCriterionRequirementWreapper(hoursGroupWrapper);
            hoursGroupWrapper.assignCriterionRequirementWrapper(requirement);
        }
    }

    public void selectCriterionToHoursGroup(
            HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper requirementWrapper,
            CriterionWithItsType criterionAndType){
        if (requirementWrapper.isDirect()) {
            selectCriterionToDirectRequirementWrapper(hoursGroupWrapper,
                    requirementWrapper, criterionAndType);
        } else {
            selectCriterionToExceptionRequirementWrapper(
                    hoursGroupWrapper, requirementWrapper, criterionAndType);
        }
    }

    private void selectCriterionToDirectRequirementWrapper(
            HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper direct,
            CriterionWithItsType newCriterionAndType) {

        CriterionWithItsType oldCriterionAndType = direct
                .getCriterionWithItsType();
        if ((oldCriterionAndType == null)
                || (!oldCriterionAndType.equals(newCriterionAndType))) {
            hoursGroupWrapper.removeDirectCriterionRequirement(direct);
            direct.setCriterionWithItsType(newCriterionAndType);
            hoursGroupWrapper.addDirectCriterionToHoursGroup(direct);
        }
    }

    public CriterionRequirementWrapper addExceptionToHoursGroupWrapper(
            HoursGroupWrapper hoursGroupWrapper) {
        if ((getModel() != null) && (getElement() != null)) {
            CriterionRequirementWrapper exceptionWrapper = new CriterionRequirementWrapper(
                    CriterionRequirementWrapper.getIndirectTypeLabel());
            exceptionWrapper.setNewException(true);
            hoursGroupWrapper.addExceptionRequirementWrappers(exceptionWrapper);
            return exceptionWrapper;
        }
        return null;
    }

    private void selectCriterionToExceptionRequirementWrapper(
            HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper exception,
            CriterionWithItsType criterionAndType) {
        hoursGroupWrapper.selectCriterionToExceptionRequirementWrapper(
                exception, criterionAndType);
    }

    public void deleteCriterionToHoursGroup(
            HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper requirementWrapper) {
        if (requirementWrapper.isDirect()) {
            deleteDirectToHoursGroup(hoursGroupWrapper, requirementWrapper);
        } else {
            deleteExceptionToHoursGroup(hoursGroupWrapper, requirementWrapper);
        }
    }

    private void deleteDirectToHoursGroup(HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper direct) {
        hoursGroupWrapper.removeDirectCriterionRequirementWrapper(direct);
    }

    private void deleteExceptionToHoursGroup(
            HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper exception) {
        hoursGroupWrapper.removeExceptionCriterionRequirementWrapper(exception);
    }

    private void updateExceptionInHoursGroups() {
        for (HoursGroupWrapper hoursGroupWrapper : hoursGroupsWrappers) {
            hoursGroupWrapper.updateListExceptionCriterionRequirementWrapper();
        }
    }

    /* Operations to control and validate the data hoursGroup */

    public void updateCriterionsWithDiferentResourceType(
            HoursGroupWrapper hoursGroupWrapper) {
        hoursGroupWrapper.removeDirectCriterionsWithDiferentResourceType();
        hoursGroupWrapper.getHoursGroup().updateMyCriterionRequirements();
        hoursGroupWrapper.updateListExceptionCriterionRequirementWrapper();
    }

    public abstract boolean isCodeAutogenerated();

}