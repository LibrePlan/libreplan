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

package org.libreplan.web.orders.criterionrequirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.requirements.entities.CriterionRequirement;
import org.libreplan.business.requirements.entities.DirectCriterionRequirement;
import org.libreplan.business.requirements.entities.IndirectCriterionRequirement;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.CriterionWithItsType;
import org.libreplan.web.orders.CriterionRequirementWrapper;
import org.libreplan.web.orders.HoursGroupWrapper;
import org.libreplan.web.orders.IOrderModel;
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
public class AssignedCriterionRequirementToOrderElementModel extends
    AssignedCriterionRequirementModel<OrderElement, IOrderModel> implements
    IAssignedCriterionRequirementToOrderElementModel {

    @Autowired
    private IOrderElementDAO orderElementDAO;

    private IOrderModel orderModel;

    private OrderElement orderElement;

    @Override
    public IOrderModel getModel() {
        return orderModel;
    }

    @Override
    public OrderElement getElement() {
        return orderElement;
    }

    @Override
    public void setElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    @Override
    public void setModel(IOrderModel model) {
        orderModel = model;
    }

    @Override
    @Transactional(readOnly = true)
    public void init(OrderElement element) {
        setElement(element);
        if(element != null){
            reattachOrderElement();
            initializeWrappers();
            initializeCriterionWithItsType();
            reloadHoursGroupWrappers();
        }
    }

    private void reattachOrderElement() {
        orderElementDAO.reattach(orderElement);
        for (OrderElement child : orderElement.getAllChildren()) {
            child.getName();
            reattachCriterionRequirement(child.getCriterionRequirements());
            if (child instanceof OrderLine) {
                for (HoursGroup hoursGroup : child.getHoursGroups()) {
                    hoursGroup.getWorkingHours();
                    reattachCriterionRequirement(hoursGroup.getCriterionRequirements());
                }
            }
        }
    }

    private void reattachCriterionRequirement(Set<CriterionRequirement> list){
        for (CriterionRequirement requirement : list) {
            requirement.getCriterion().getName();
            requirement.getCriterion().getType().getName();
        }
    }

    private void initializeWrappers() {
        criterionRequirementWrappers = new ArrayList<CriterionRequirementWrapper>();
        for(CriterionRequirement requirement :
            orderElement.getCriterionRequirements()){
            CriterionRequirementWrapper Wrapper = new CriterionRequirementWrapper(
                    requirement, null, false);
            criterionRequirementWrappers.add(Wrapper);
        }
    }

    private void  initializeCriterionWithItsType() {
        criterionWithItsTypes = new ArrayList<CriterionWithItsType>();
        for(CriterionType type : getTypes()){
            if (type.isEnabled()) {
                for (Criterion criterion : getModel().getCriterionsFor(type)) {
                    if(criterion.isActive()){
                        CriterionWithItsType criterionAndType =
                                new CriterionWithItsType(type,criterion);
                        criterionWithItsTypes.add(criterionAndType);
                    }
                }
            }
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

    private boolean existIntohoursGroupsWrappers(HoursGroup hoursGroup) {
        for (HoursGroupWrapper hoursGroupWrapper : hoursGroupsWrappers) {
            if (hoursGroupWrapper.getHoursGroup().equals(hoursGroup)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public void confirm() throws ValidationException{
        reattachOrderElement();
    }

    public Set<CriterionType> getTypes() {
        return getMapCriterions().keySet();
    }

    private Map<CriterionType, List<Criterion>> getMapCriterions() {
        return getModel().getMapCriterions();
    }

    public Integer getTotalHours() {
        return (getElement() != null) ? getElement().getWorkHours() : 0;
    }

    protected void addDirectCriterionRequirement(DirectCriterionRequirement requirement) {
        getElement().addDirectCriterionRequirement(requirement);
    }

    protected void removeDirectCriterionRequirement(DirectCriterionRequirement requirement) {
        getElement().removeDirectCriterionRequirement(requirement);
    }

    protected void setValidCriterionRequirement(IndirectCriterionRequirement requirement, boolean valid) {
        getElement().setValidCriterionRequirement(requirement, valid);
    }

    @Override
    protected void addNewHoursGroupWrapper(HoursGroup newHoursGroup,
            boolean newObject) {
        HoursGroupWrapper newHoursGroupWrapper = new HoursGroupWrapper(
                newHoursGroup, getElement(), newObject);
        hoursGroupsWrappers.add(newHoursGroupWrapper);
    }

    @Override
    public boolean isCodeAutogenerated() {
        if (getElement() != null && getElement().getOrder() != null) {
            return getElement().getOrder().isCodeAutogenerated();
        }
        return false;
    }

}
