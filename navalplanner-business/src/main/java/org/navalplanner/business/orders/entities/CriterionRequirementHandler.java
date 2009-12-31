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

package org.navalplanner.business.orders.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ResourceEnum;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CriterionRequirementHandler implements
        ICriterionRequirementHandler {

    private static final CriterionRequirementHandler singleton = new CriterionRequirementHandler();

    private CriterionRequirementHandler() {

    }

    public static CriterionRequirementHandler getInstance() {
        return singleton;
    }

    // Operations to add a criterionRequirement
    public void propagateDirectCriterionRequirementAddition(
            OrderElement orderElement, CriterionRequirement directo) {
        propagateIndirectCriterionRequirement(orderElement,
                (DirectCriterionRequirement) directo);
    }

    private void propagateIndirectCriterionRequirement(
            OrderElement orderElement,
            CriterionRequirement directo) {
        if (orderElement instanceof OrderLine) {
            propagateIndirectCriterionRequirementToChildren(
                    (OrderLine) orderElement,
                    (DirectCriterionRequirement) directo);
        } else {
            propagateIndirectCriterionRequirementToChildren(
                    (OrderLineGroup) orderElement,
                    (DirectCriterionRequirement) directo);
        }
    }

    void propagateIndirectCriterionRequirementToChildren(
            OrderLineGroup orderLineGroup, DirectCriterionRequirement parent) {
        Criterion criterion = parent.getCriterion();
        for (OrderElement child : orderLineGroup.getChildren()) {
            IndirectCriterionRequirement indirect = IndirectCriterionRequirement
                    .create(parent, criterion);
            child.addIndirectCriterionRequirement(indirect);
            propagateIndirectCriterionRequirement(child, parent);
        }
    }

    void propagateIndirectCriterionRequirementToChildren(OrderLine orderLine,
            DirectCriterionRequirement parent) {
        Criterion criterion = parent.getCriterion();
        for (HoursGroup hoursGroup : orderLine.getHoursGroups()) {
            hoursGroup.updateMyCriterionRequirements();
        }
    }

    boolean canAddCriterionRequirement(OrderElement orderElement,
            CriterionRequirement newRequirement) {
        List<OrderElement> listOrderElements = orderElement.getAllChildren();
        listOrderElements.add(orderElement);
        for (OrderElement element : listOrderElements) {
            if (existSameCriterionRequirement(element, newRequirement))
                return false;
        }
        return true;
    }

    private boolean existSameCriterionRequirement(OrderElement orderElement,
            CriterionRequirement newRequirement){
        if (orderElement instanceof OrderLine) {
            return existSameCriterionRequirementIntoOrderLine(
                    (OrderLine) orderElement,newRequirement);
        } else {
            return existSameCriterionRequirementIntoOrderElement(
                    (OrderLineGroup) orderElement, newRequirement);
        }
    }

    private boolean existSameCriterionRequirementIntoOrderElement(
            OrderElement orderElement,
            CriterionRequirement newRequirement) {
        Criterion criterion = newRequirement.getCriterion();
        for (CriterionRequirement requirement : orderElement
                .getCriterionRequirements()) {
            if (requirement.getCriterion().equals(criterion))
                return true;
        }
        return false;
    }

    boolean existSameCriterionRequirementIntoOrderLine(OrderLine orderLine,
            CriterionRequirement newRequirement) {
        if (existSameCriterionRequirementIntoOrderElement(orderLine,
                newRequirement)) {
            return true;
        }
        for (HoursGroup hoursGroup : orderLine.getHoursGroups()) {
            if (hoursGroup.existSameCriterionRequirement(newRequirement))
                return true;
        }
        return false;
    }

    /*
     * Operations to set the valid value of a criterion Requirements and the
     * criterion Requirement of its children.
     */

    public void propagateValidCriterionRequirement(OrderElement orderElement,
            DirectCriterionRequirement parent, boolean valid) {
        if (orderElement instanceof OrderLine) {
            setValidCriterionRequirementChildren((OrderLine) orderElement,
                    parent, valid);
        } else {
            setValidCriterionRequirementChildren((OrderLineGroup) orderElement,
                    parent, valid);
        }
    }

    protected void setValidCriterionRequirementChildren(
            OrderLineGroup orderLineGroup,
            DirectCriterionRequirement parent, boolean valid) {
        for (OrderElement child : orderLineGroup.getChildren()) {
            IndirectCriterionRequirement indirect = findIndirectRequirementByParent(
                    child.getIndirectCriterionRequirement(), parent);
            if (indirect != null) {
                indirect.setIsValid(valid);
            }
            propagateValidCriterionRequirement(child, parent, valid);
        }
    }

    protected void setValidCriterionRequirementChildren(OrderLine orderLine,
            DirectCriterionRequirement parent, boolean valid) {
        for (HoursGroup hoursGroup : orderLine.getHoursGroups()) {
            IndirectCriterionRequirement indirect = findIndirectRequirementByParent(
                    hoursGroup.getIndirectCriterionRequirement(), parent);
            if (indirect != null) {
                indirect.setIsValid(valid);
            }
        }
    }

    /*
     * Operation to update the criterions requirements of the orderElement and
     * its children
     */
    public void propagateRemoveCriterionRequirement(OrderElement orderElement,
            DirectCriterionRequirement parent) {
        if (orderElement instanceof OrderLine) {
            removeIndirectCriterionRequirement((OrderLine) orderElement, parent);
        } else {
            removeIndirectCriterionRequirement((OrderLineGroup) orderElement,
                    parent);
        }
    }

    protected void removeIndirectCriterionRequirement(
            OrderLineGroup orderLineGroup, DirectCriterionRequirement parent) {
        for (OrderElement child : orderLineGroup.getChildren()) {
            IndirectCriterionRequirement indirect = findIndirectRequirementByParent(
                    child.getIndirectCriterionRequirement(), parent);
            if (indirect != null) {
                propagateRemoveCriterionRequirement(child, parent);
                child.removeCriterionRequirement(indirect);
            }
        }
    }

    protected void removeIndirectCriterionRequirement(OrderLine orderLine,
            DirectCriterionRequirement parent) {
        for (HoursGroup hoursGroup : orderLine.getHoursGroups()) {
            IndirectCriterionRequirement indirect = findIndirectRequirementByParent(
                    hoursGroup.getIndirectCriterionRequirement(), parent);
            if (indirect != null) {
                hoursGroup.removeCriterionRequirement(indirect);
            }
        }
    }

    /*
     * Operation to update the criterions requirements of the orderElement and
     * its children
     */

    public void propagateUpdateCriterionRequirements(OrderElement orderElement) {
        if (orderElement instanceof OrderLine) {
            updateCriterionRequirementsIntoOrderLine((OrderLine) orderElement);
        } else {
            updateCriterionRequirementsIntoOrderLineGroup((OrderLineGroup) orderElement);
        }
    }

    private void updateCriterionRequirementsIntoOrderLineGroup(
            OrderElement orderLineGroup) {
        for (OrderElement child : orderLineGroup.getChildren()) {
            child.updateMyCriterionRequirements();
            propagateUpdateCriterionRequirements(child);
        }
    }

    private void updateCriterionRequirementsIntoOrderLine(OrderLine orderLine) {
        for (HoursGroup hoursGroup : orderLine.getHoursGroups()) {
            hoursGroup.updateMyCriterionRequirements();
        }
    }

    void transformDirectToIndirectIfNeeded(OrderElement orderElement,
            Set<IndirectCriterionRequirement> currents) {
        for (DirectCriterionRequirement direct : orderElement
                .getDirectCriterionRequirement()) {
            IndirectCriterionRequirement indirect = findIndirectRequirementByCriterion(
                    currents, direct.getCriterion());
            if (indirect != null) {
                orderElement.removeDirectCriterionRequirement(direct);
            }
        }
    }

    void addNewsIndirects(OrderElement orderElement,
            Set<IndirectCriterionRequirement> currents) {
        Set<IndirectCriterionRequirement> indirects = orderElement
                .getIndirectCriterionRequirement();
        for (IndirectCriterionRequirement current : currents) {
            if (!indirects.contains(current)) {
                orderElement.basicAddCriterionRequirement(current);
            }
        }
    }

    void removeOldIndirects(OrderElement orderElement,
            Set<IndirectCriterionRequirement> currents) {
        for (IndirectCriterionRequirement indirect : orderElement
                .getIndirectCriterionRequirement()) {
            if (!currents.contains(indirect)) {
                orderElement.removeCriterionRequirement(indirect);
            }
        }
    }

    void transformDirectToIndirectIfNeeded(HoursGroup hoursGroup,
            Set<IndirectCriterionRequirement> currents) {
        for (DirectCriterionRequirement direct : hoursGroup
                .getDirectCriterionRequirement()) {
            IndirectCriterionRequirement indirect = findIndirectRequirementByCriterion(
                    currents, direct.getCriterion());
            if (indirect != null) {
                hoursGroup.removeCriterionRequirement(direct);
            }
        }
    }

    void addNewsIndirects(HoursGroup hoursGroup,
            Set<IndirectCriterionRequirement> currents) {
        Set<IndirectCriterionRequirement> indirects = hoursGroup
                .getIndirectCriterionRequirement();
        for (IndirectCriterionRequirement current : currents) {
            if (!indirects.contains(current)) {
                hoursGroup.addCriterionRequirement(current);
            }
        }
    }

    void removeOldIndirects(HoursGroup hoursGroup,
            Set<IndirectCriterionRequirement> currents) {
        for (IndirectCriterionRequirement indirect : hoursGroup
                .getIndirectCriterionRequirement()) {
            if (!currents.contains(indirect)) {
                hoursGroup.removeCriterionRequirement(indirect);
            }
        }
    }

    Set<IndirectCriterionRequirement> getCurrentIndirectRequirements(
            Set<IndirectCriterionRequirement> oldIndirects,
            Set<CriterionRequirement> requirementsParent) {
        Set<IndirectCriterionRequirement> currentIndirects = new HashSet<IndirectCriterionRequirement>();
        for (CriterionRequirement requirement : requirementsParent) {
            IndirectCriterionRequirement indirect = getCurrentIndirectRequirement(
                    oldIndirects, requirement);
            currentIndirects.add(indirect);
        }
        return currentIndirects;
    }

    IndirectCriterionRequirement getCurrentIndirectRequirement(
            Set<IndirectCriterionRequirement> oldIndirects,
            CriterionRequirement requirement) {

        IndirectCriterionRequirement indirect;
        DirectCriterionRequirement parent;
        boolean valid = true;
        if (requirement instanceof DirectCriterionRequirement) {
            parent = (DirectCriterionRequirement) requirement;
        } else {
            parent = ((IndirectCriterionRequirement) requirement).getParent();
            valid = ((IndirectCriterionRequirement) requirement).isIsValid();
        }

        indirect = findIndirectRequirementByParent(oldIndirects, parent);
        if (indirect == null) {
            indirect = IndirectCriterionRequirement.create(parent, requirement
                    .getCriterion());
            indirect.setIsValid(valid);
        }
        return (IndirectCriterionRequirement) indirect;
    }

    private IndirectCriterionRequirement findIndirectRequirementByParent(
            Set<IndirectCriterionRequirement> indirects,
            DirectCriterionRequirement newParent) {
        for (IndirectCriterionRequirement requirement : indirects) {
            if (requirement.getParent().equals(newParent)) {
                return requirement;
            }
        }
        return null;
    }

    private IndirectCriterionRequirement findIndirectRequirementByCriterion(
            Set<IndirectCriterionRequirement> indirects, Criterion criterion) {
        for (IndirectCriterionRequirement requirement : indirects) {
            if (requirement.getCriterion().equals(criterion)) {
                return requirement;
            }
        }
        return null;
    }
    /*
     * Operation to create and add to a orderElement new criterion requirements
     * that it is copied of the criterion requirements of other orderElement
     */
    protected void copyRequirementToOrderElement(OrderLine orderLine,
            OrderLineGroup container) {
        // copy the directCriterionRequirement
        for (DirectCriterionRequirement newRequirement : copyDirectRequirements(orderLine
                .getDirectCriterionRequirement())) {
            container.basicAddCriterionRequirement(newRequirement);
        }
        // copy the IndirectCriterionRequirement
        for (IndirectCriterionRequirement newRequirement : copyIndirectRequirements(orderLine
                .getIndirectCriterionRequirement())) {
            container.basicAddCriterionRequirement(newRequirement);
        }
    }

    protected void copyRequirementToOrderElement(OrderLineGroup orderLineGroup,
            OrderLine leaf) {
        // copy the directCriterionRequirement
        for (DirectCriterionRequirement newRequirement : copyDirectRequirements(orderLineGroup
                .getDirectCriterionRequirement())) {
            leaf.addDirectCriterionRequirement(newRequirement);
        }
        // copy the IndirectCriterionRequirement
        for (IndirectCriterionRequirement newRequirement : copyIndirectRequirements(orderLineGroup
                .getIndirectCriterionRequirement())) {
            leaf.addIndirectCriterionRequirement(newRequirement);
            propagateIndirectCriterionRequirementToChildren(leaf,
                    newRequirement.getParent());
        }
    }

    private Set<DirectCriterionRequirement> copyDirectRequirements(Set<DirectCriterionRequirement> collection){
        Set<DirectCriterionRequirement> result = new HashSet<DirectCriterionRequirement>();
        for (DirectCriterionRequirement requirement : collection) {
            DirectCriterionRequirement newRequirement = DirectCriterionRequirement
                    .create(requirement.getCriterion());
            result.add(newRequirement);
        }
        return result;
    }

    private Set<IndirectCriterionRequirement> copyIndirectRequirements(
            Set<IndirectCriterionRequirement> collection) {
        Set<IndirectCriterionRequirement> result = new HashSet<IndirectCriterionRequirement>();
        for (IndirectCriterionRequirement requirement : collection) {
            DirectCriterionRequirement parent = requirement.getParent();
            IndirectCriterionRequirement newRequirement = IndirectCriterionRequirement
                    .create(parent, requirement.getCriterion());
            result.add(newRequirement);
        }
        return result;
    }

    Set<CriterionRequirement> getRequirementWithSameResourType(
            Set<CriterionRequirement> requirements,
            ResourceEnum resourceType) {
        Set<CriterionRequirement> result = new HashSet<CriterionRequirement>();
        for (CriterionRequirement requirement : requirements) {
            ResourceEnum resourceTypeParent = requirement.getCriterion()
                    .getType().getResource();
            if ((resourceTypeParent.equals(resourceType))
                    || (resourceTypeParent.equals(ResourceEnum.RESOURCE)))
                result.add(requirement);
        }
        return result;
    }
}