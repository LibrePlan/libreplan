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
import java.util.Set;

import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.business.templates.entities.OrderLineTemplate;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.orders.CriterionRequirementWrapper;
import org.navalplanner.web.orders.HoursGroupWrapper;
import org.navalplanner.web.orders.criterionrequirements.AssignedCriterionRequirementController;
import org.navalplanner.web.templates.IOrderTemplatesModel;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class AssignedCriterionRequirementToTemplateController extends
    AssignedCriterionRequirementController<OrderElementTemplate, IOrderTemplatesModel> {

    protected IAssignedCriterionRequirementToTemplateModel assignedCriterionRequirementToTemplateModel;

    @Override
    public OrderElementTemplate getElement() {
       return getOrderElement();
    }

    public OrderElementTemplate getOrderElement() {
        return assignedCriterionRequirementToTemplateModel.getElement();
    }

    public Set<CriterionType> getCriterionTypes() {
        return assignedCriterionRequirementToTemplateModel.getTypes();
    }

    public void setOrderElement(OrderElementTemplate orderElement) {
        assignedCriterionRequirementToTemplateModel.setElement(orderElement);
    }

    public void openWindow(IOrderTemplatesModel orderTemplateModel, OrderElementTemplate template) {
        assignedCriterionRequirementToTemplateModel.setModel(orderTemplateModel);
        assignedCriterionRequirementToTemplateModel.init(template);
        Util.reloadBindings(self);
    }

    public void openWindow(IOrderTemplatesModel orderTemplateModel) {
        assignedCriterionRequirementToTemplateModel.setModel(orderTemplateModel);
        openWindow(orderTemplateModel.getTemplate());
    }

    public void openWindow(OrderElementTemplate orderElement) {
        assignedCriterionRequirementToTemplateModel.init(orderElement);
        Util.createBindingsFor(self);
        Util.reloadBindings(self);
    }

    public void confirm() {
        assignedCriterionRequirementToTemplateModel.confirm();
    }

    public List<CriterionRequirementWrapper> criterionRequirementWrappers() {
        return assignedCriterionRequirementToTemplateModel
                .getCriterionRequirementWrappers();
    }

    public List<CriterionWithItsType> getCriterionWithItsTypes() {
        return assignedCriterionRequirementToTemplateModel
                .getCriterionWithItsTypes();
    }

    public void addCriterionRequirementWrapper() {
        assignedCriterionRequirementToTemplateModel
                .assignCriterionRequirementWrapper();
        reload();
    }

    public void remove(CriterionRequirementWrapper requirement) {
        assignedCriterionRequirementToTemplateModel
                .deleteCriterionRequirementWrapper(requirement);
        reload();
    }

    public void invalidate(CriterionRequirementWrapper requirement) {
        assignedCriterionRequirementToTemplateModel
                .setValidCriterionRequirementWrapper(requirement, false);
        reload();
    }

    public void validate(CriterionRequirementWrapper requirement) {
        assignedCriterionRequirementToTemplateModel
                .setValidCriterionRequirementWrapper(requirement, true);
        reload();
    }

    protected void changeCriterionAndType(
            CriterionRequirementWrapper requirementWrapper,
            CriterionWithItsType newCriterionAndType) {
        assignedCriterionRequirementToTemplateModel.changeCriterionAndType(
                requirementWrapper, newCriterionAndType);
    }

    protected void updateCriterionsWithDiferentResourceType(
            HoursGroupWrapper hoursGroupWrapper) {
        assignedCriterionRequirementToTemplateModel
                .updateCriterionsWithDiferentResourceType(hoursGroupWrapper);
    }

    protected CriterionRequirementWrapper validateWrappers(
            List<CriterionRequirementWrapper> list) {
        return assignedCriterionRequirementToTemplateModel
                .validateWrappers(criterionRequirementWrappers());
    }

    protected CriterionRequirementWrapper validateHoursGroupWrappers() {
        return assignedCriterionRequirementToTemplateModel
                .validateHoursGroupWrappers();
    }

    public List<HoursGroupWrapper> getHoursGroupWrappers() {
        return assignedCriterionRequirementToTemplateModel
                .getHoursGroupsWrappers();
    }

    public void addHoursGroup() {
        assignedCriterionRequirementToTemplateModel
                .addNewHoursGroupWrapper();
        Util.reloadBindings(listHoursGroups);
    }

    protected void deleteHoursGroupWrapper(HoursGroupWrapper hoursGroupWrapper) {
        assignedCriterionRequirementToTemplateModel
                .deleteHoursGroupWrapper(hoursGroupWrapper);
    }

    protected void addCriterionToHoursGroupWrapper(
            HoursGroupWrapper hoursGroupWrapper) {
        assignedCriterionRequirementToTemplateModel
                .addCriterionToHoursGroupWrapper(hoursGroupWrapper);
    }

    protected CriterionRequirementWrapper addExceptionToHoursGroupWrapper(
            HoursGroupWrapper hoursGroupWrapper) {
        return assignedCriterionRequirementToTemplateModel
                .addExceptionToHoursGroupWrapper(hoursGroupWrapper);
    }

    public void deleteCriterionToHoursGroup(
            HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper requirementWrapper) {

        assignedCriterionRequirementToTemplateModel
                .deleteCriterionToHoursGroup(hoursGroupWrapper,
                        requirementWrapper);
    }

    protected void selectCriterionToHoursGroup(
            HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper requirementWrapper,
            CriterionWithItsType criterionAndType) {

        assignedCriterionRequirementToTemplateModel
                .selectCriterionToHoursGroup(hoursGroupWrapper,
                        requirementWrapper, criterionAndType);
    }

    public void recalculateHoursGroup() {
        ((OrderLineTemplate) assignedCriterionRequirementToTemplateModel
                .getElement()).recalculateHoursGroups();
    }

    public boolean isCodeAutogenerated() {
        return assignedCriterionRequirementToTemplateModel
                .isCodeAutogenerated();
    }

    public List<HoursGroup> getHoursGroups() {
        if ((getElement() == null)
                || (assignedCriterionRequirementToTemplateModel == null)) {
            return new ArrayList<HoursGroup>();
        }
        return super.getHoursGroups();
    }

    @Override
    protected List<HoursGroup> getHoursGroups(OrderElementTemplate orderElement) {
        return orderElement.getHoursGroups();
    }

    public boolean isEditableHoursGroup() {
        return getElement() != null && getElement() instanceof OrderLineTemplate;
    }

}
