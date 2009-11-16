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

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedCriterionRequirementToOrderElementModel  implements
        IAssignedCriterionRequirementToOrderElementModel{

    @Autowired
    IOrderElementDAO orderElementDAO;

    OrderElement orderElement;

    IOrderModel orderModel;

    private List<CriterionWithItsType> criterionWithItsTypes =
            new ArrayList<CriterionWithItsType>();

    private List<CriterionRequirementWrapper> criterionRequirementWrappers =
            new ArrayList<CriterionRequirementWrapper>();

    private List<HoursGroupWrapper> hoursGroupsWrappers = new ArrayList<HoursGroupWrapper>();

    @Override
    public OrderElement getOrderElement() {
        return orderElement;
    }

    @Override
    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    @Override
    @Transactional(readOnly = true)
    public void init(OrderElement orderElement) {
        this.orderElement = orderElement;
        if(orderElement != null){
            reattachOrderElement();
            initializeWrappers();
            initializeCriterionWithItsType();
            reloadHoursGroupWrappers();
        }
    }

    private void reattachOrderElement() {
        orderElementDAO.reattach(orderElement);
        for(OrderElement child : orderElement.getAllChildren()){
            child.getName();
            reattachCriterionRequirement(child.getCriterionRequirements());
            if(child instanceof OrderLine){
                for(HoursGroup hoursGroup : child.getHoursGroups()){
                    hoursGroup.getWorkingHours();
                    reattachCriterionRequirement(hoursGroup.getCriterionRequirements());
                }
            }
        }
    }

    private void reattachCriterionRequirement(Set<CriterionRequirement> list){
        for(CriterionRequirement requirement : list){
            requirement.getCriterion().getName();
            requirement.getCriterion().getType().getName();
        }
    }

    private void initializeWrappers() {
        criterionRequirementWrappers = new ArrayList<CriterionRequirementWrapper>();
        for(CriterionRequirement requirement :
            orderElement.getCriterionRequirements()){
            CriterionRequirementWrapper Wrapper =
 new CriterionRequirementWrapper(
                    requirement, false);
            criterionRequirementWrappers.add(Wrapper);
        }
    }

    private void  initializeCriterionWithItsType() {
        criterionWithItsTypes = new ArrayList<CriterionWithItsType>();
        for(CriterionType type : getTypes()){
            if(type.isEnabled()){
                for (Criterion criterion : orderModel.getCriterionsFor(type)) {
                    if(criterion.isActive()){
                        CriterionWithItsType criterionAndType =
                                new CriterionWithItsType(type,criterion);
                        criterionWithItsTypes.add(criterionAndType);
                    }
                }
            }
        }
    }

    private Set<CriterionType> getTypes(){
        return getMapCriterions().keySet();
    }

    private Map<CriterionType, List<Criterion>> getMapCriterions(){
        return orderModel.getMapCriterions();
    }

    @Override
    public List<CriterionWithItsType> getCriterionWithItsTypes(){
        return criterionWithItsTypes;
    }

    public Integer getTotalHours() {
        if (getOrderElement() != null) {
            return getOrderElement().getWorkHours();
        }
        return 0;
    }

    @Override
    @Transactional(readOnly = true)
    public void assignCriterionRequirementWrapper() {
        if((orderModel != null) && (orderElement != null)){
            CriterionRequirementWrapper newRequirementWrapper = createCriterionRequirementWreapper();
            criterionRequirementWrappers.add(newRequirementWrapper);
        }
    }

    private CriterionRequirementWrapper createCriterionRequirementWreapper() {
        CriterionRequirement newRequirement = DirectCriterionRequirement
                .create();
        CriterionRequirementWrapper newRequirementWrapper = new CriterionRequirementWrapper(
                newRequirement, true);
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
        orderElement.addDirectCriterionRequirement(requirement);
    }

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
        orderElement.removeDirectCriterionRequirement(requirement);
    }

    @Override
    @Transactional(readOnly = true)
    public void confirm() throws ValidationException{
        reattachOrderElement();
    }

    @Override
    public void setOrderModel(IOrderModel orderModel) {
        this.orderModel = orderModel;
    }

    @Override
    public List<CriterionRequirementWrapper> getCriterionRequirementWrappers() {
        if((orderModel != null)&&(getOrderElement() != null)){
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
        getOrderElement().setValidCriterionRequirement(requirement, valid);
        if (requirementWrapper.getCriterionWithItsType() != null) {
            updateExceptionInHoursGroups();
        }
    }

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
        if (getOrderElement() instanceof OrderLine) {
            return (OrderLine) getOrderElement();
        } else {
            return null;
        }
    }

    private void reloadHoursGroupWrappers() {
        hoursGroupsWrappers = new ArrayList<HoursGroupWrapper>();
        for (HoursGroup hoursGroup : orderElement.getHoursGroups()) {
            if (!existIntohoursGroupsWrappers(hoursGroup)) {
            addNewHoursGroupWrapper(hoursGroup, false);
            }
        }
    }

    public void addNewHoursGroupWrapper() {
        if ((orderModel != null) && (getOrderElement() != null)) {
            HoursGroup newHoursGroup = createNewHoursGroup();
            addNewHoursGroupWrapper(newHoursGroup, true);
        }
    }

    private void addNewHoursGroupWrapper(HoursGroup newHoursGroup,
            boolean newObject) {
        HoursGroupWrapper newHoursGroupWrapper = new HoursGroupWrapper(
                newHoursGroup, getOrderElement(), newObject);
        hoursGroupsWrappers.add(newHoursGroupWrapper);
    }

    private HoursGroup createNewHoursGroup() {
        if (asOrderLine() != null) {
            HoursGroup newHoursGroup = HoursGroup.create(asOrderLine());
            (asOrderLine()).addHoursGroup(newHoursGroup);

            // Set generated name
            int number = (asOrderLine()).getHoursGroups().size();
            newHoursGroup.setName(_("New hours group ") + number);

            return newHoursGroup;
        }
        return null;
    }

    public List<HoursGroupWrapper> getHoursGroupsWrappers() {
        if ((orderModel != null) && (getOrderElement() != null)) {
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

    private boolean existIntohoursGroupsWrappers(HoursGroup hoursGroup) {
        for (HoursGroupWrapper hoursGroupWrapper : hoursGroupsWrappers) {
            if (hoursGroupWrapper.getHoursGroup().equals(hoursGroup)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Operation to manage the criterion Requirements for the hoursGroups. The
     * operations is add new direct criterion requirement, delete criterion
     * requirement, add new Exception and delete Exception
     */
    public void addCriterionToHoursGroupWrapper(
            HoursGroupWrapper hoursGroupWrapper) {
        if ((orderModel != null) && (orderElement != null)) {
            CriterionRequirementWrapper requirement = createCriterionRequirementWreapper();
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
            CriterionWithItsType criterionAndType) {
        direct.setCriterionWithItsType(criterionAndType);
        hoursGroupWrapper.selectCriterionToDirectRequirementWrapper(direct);
    }

    public CriterionRequirementWrapper addExceptionToHoursGroupWrapper(
            HoursGroupWrapper hoursGroupWrapper) {
        if ((orderModel != null) && (orderElement != null)) {
            CriterionRequirementWrapper exceptionWrapper = new CriterionRequirementWrapper(
                    CriterionRequirementWrapper.INDIRECT);
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
        hoursGroupWrapper
.selectCriterionToExceptionRequirementWrapper(
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

}