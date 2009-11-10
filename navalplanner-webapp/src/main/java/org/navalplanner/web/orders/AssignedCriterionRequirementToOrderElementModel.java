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
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
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

    @Override
    @Transactional(readOnly = true)
    public void assignCriterionRequirementWrapper() {
        if((orderModel != null) && (orderElement != null)){
            CriterionRequirement newRequirement = DirectCriterionRequirement
                    .create();
            CriterionRequirementWrapper requirement = new CriterionRequirementWrapper(
                    newRequirement, true);
            criterionRequirementWrappers.add(requirement);
        }
    }

    public boolean canSetCriterionWithItsType(
            CriterionRequirementWrapper requirementWrapper,
            CriterionWithItsType criterionAndType) {
        requirementWrapper.setCriterionWithItsType(criterionAndType);
        return canAddNewCriterionRequirement(requirementWrapper);
    }

    private boolean canAddNewCriterionRequirement(
            CriterionRequirementWrapper requirementWrapper) {
        DirectCriterionRequirement requirement = (DirectCriterionRequirement) requirementWrapper
                .getCriterionRequirement();
        if (orderElement.canAddCriterionRequirement(requirement)) {
            orderElement.addDirectCriterionRequirement(requirement);
            return true;
        }
        return false;
    }

    @Override
    public void deleteCriterionRequirementWrapper(
            CriterionRequirementWrapper requirementWrapper) {
        DirectCriterionRequirement requirement = (DirectCriterionRequirement) requirementWrapper
                .getCriterionRequirement();
        orderElement.removeDirectCriterionRequirement(requirement);
        criterionRequirementWrappers.remove(requirementWrapper);
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
    }

    public CriterionRequirementWrapper validateWrappers() {
        for (CriterionRequirementWrapper requirementWrapper : criterionRequirementWrappers) {
            if (requirementWrapper.getCriterionWithItsType() == null) {
                return requirementWrapper;
            }
        }
        return null;
    }
}