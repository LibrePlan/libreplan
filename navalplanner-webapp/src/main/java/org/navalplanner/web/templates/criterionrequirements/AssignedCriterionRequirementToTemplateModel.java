/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.templates.criterionrequirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.templates.daos.IOrderElementTemplateDAO;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.business.templates.entities.OrderLineTemplate;
import org.navalplanner.web.orders.CriterionRequirementWrapper;
import org.navalplanner.web.orders.HoursGroupWrapper;
import org.navalplanner.web.orders.criterionrequirements.AssignedCriterionRequirementModel;
import org.navalplanner.web.templates.IOrderTemplatesModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedCriterionRequirementToTemplateModel extends
        AssignedCriterionRequirementModel<OrderElementTemplate, IOrderTemplatesModel> implements
        IAssignedCriterionRequirementToTemplateModel {

    @Autowired
    private IOrderElementTemplateDAO orderElementTemplateDAO;

    private IOrderTemplatesModel orderTemplateModel;

    private OrderElementTemplate orderElementTemplate;

    @Override
    public IOrderTemplatesModel getModel() {
        return orderTemplateModel;
    }

    @Override
    public OrderElementTemplate getElement() {
        return orderElementTemplate;
    }

    @Override
    public void setElement(OrderElementTemplate orderElementTemplate) {
        this.orderElementTemplate = orderElementTemplate;
    }

    @Override
    public void setModel(IOrderTemplatesModel model) {
        orderTemplateModel = model;
    }

    @Override
    @Transactional(readOnly = true)
    public void init(OrderElementTemplate element) {
        setElement(element);
        if (element != null) {
            reattachOrderElementTemplate();
            initializeWrappers();
            initializeCriterionWithItsType();
            reloadHoursGroupWrappers();
        }
    }

    private void reattachOrderElementTemplate() {
        orderElementTemplateDAO.reattach(orderElementTemplate);
        for (OrderElementTemplate each : orderElementTemplate.getAllChildren()) {
            each.getName();
            reattachCriterionRequirement(each.getCriterionRequirements());
            if (each instanceof OrderLineTemplate) {
                for (HoursGroup hoursGroup : each.getHoursGroups()) {
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
            orderElementTemplate.getCriterionRequirements()) {
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
        for (HoursGroup hoursGroup : orderElementTemplate.getHoursGroups()) {
            if (!existIntohoursGroupsWrappers(hoursGroup)) {
                addNewHoursGroupWrapper(hoursGroup, false);
            }
        }
    }

    protected boolean existIntohoursGroupsWrappers(HoursGroup hoursGroup) {
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
        reattachOrderElementTemplate();
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
    public boolean isCodeAutogenerated() {
        if (getElement() != null && getElement().getOrder() != null) {
            return getElement().getOrder().isCodeAutogenerated();
        }
        return false;
    }

    @Override
    protected void addNewHoursGroupWrapper(HoursGroup newHoursGroup,
            boolean newObject) {
        HoursGroupWrapper newHoursGroupWrapper = new HoursGroupWrapper(
                newHoursGroup, getElement(), newObject);
        hoursGroupsWrappers.add(newHoursGroupWrapper);
    }

}
