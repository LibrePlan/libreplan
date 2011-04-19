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

package org.navalplanner.business.orders.entities;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.sql.Template;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.templates.entities.OrderElementTemplate;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */

// <OrderElement, OrderLine, OrderLineGroup>
public abstract class CriterionRequirementHandler<T, S, R> implements
        ICriterionRequirementHandler {

    // Operations to add a criterionRequirement
    public void propagateDirectCriterionRequirementAddition(T orderElement,
            CriterionRequirement directo) {
        propagateIndirectCriterionRequirement(orderElement,
                (DirectCriterionRequirement) directo);
    }

    protected void propagateIndirectCriterionRequirement(T orderElement,
            CriterionRequirement direct) {

        if (isOrderLine(orderElement)) {
            propagateIndirectCriterionRequirementToOrderLineChildren(
                    toOrderLine(orderElement),
                    (DirectCriterionRequirement) direct);
        } else {
            propagateIndirectCriterionRequirementToOrderLineGroupChildren(
                    toOrderLineGroup(orderElement),
                    (DirectCriterionRequirement) direct);
        }
    }

    protected void propagateIndirectCriterionRequirementToOrderLineGroupChildren(
            R orderLineGroup, DirectCriterionRequirement parent) {
        Criterion criterion = parent.getCriterion();
        for (T child : getOrderLineGroupChildren(orderLineGroup)) {
            IndirectCriterionRequirement indirect = IndirectCriterionRequirement
                    .create(parent, criterion);
            addIndirectCriterionRequirement(child, indirect);
            propagateIndirectCriterionRequirement(child, parent);
        }
    }

    protected void propagateIndirectCriterionRequirementToOrderLineChildren(
            S orderLine, DirectCriterionRequirement parent) {
        for (HoursGroup hoursGroup : getHoursGroups(orderLine)) {
            hoursGroup.updateMyCriterionRequirements();
        }
    }

    protected abstract boolean isOrderLine(T orderElement);

    protected abstract S toOrderLine(T orderElement);

    protected abstract R toOrderLineGroup(T orderElement);

    protected abstract List<HoursGroup> getHoursGroups(S orderline);

    protected abstract List<T> getOrderLineGroupChildren(R orderLineGroup);

    public boolean canAddCriterionRequirement(T orderElement,
            CriterionRequirement newRequirement) {
        List<T> listOrderElements = getAllChildren(orderElement);
        listOrderElements.add(orderElement);
        for (T element : listOrderElements) {
            if (existSameCriterionRequirement(element, newRequirement)) {
                return false;
            }
        }
        return true;
    }

    protected abstract List<T> getAllChildren(T orderElement);

    public boolean existSameCriterionRequirement(T orderElement,
            CriterionRequirement newRequirement) {
        if (isOrderLine(orderElement)) {
            return existSameCriterionRequirementIntoOrderLine(toOrderLine(orderElement), newRequirement);
        } else {
            return existSameCriterionRequirementIntoOrderElement(orderElement,
                    newRequirement);
        }
    }

    public boolean existSameCriterionRequirementIntoOrderLine(S orderLine,
            CriterionRequirement newRequirement) {
        if (existSameCriterionRequirementIntoOrderElement(toOrderElement(orderLine),
                newRequirement)) {
            return true;
        }
        for (HoursGroup hoursGroup : getHoursGroups(orderLine)) {
            if (hoursGroup.existSameCriterionRequirement(newRequirement)) {
                return true;
            }
        }
        return false;
    }

    protected abstract T toOrderElement(S orderLine);

    public boolean existSameCriterionRequirementIntoOrderElement(
            T orderElement, CriterionRequirement newRequirement) {
        Criterion criterion = newRequirement.getCriterion();
        for (CriterionRequirement requirement : getCriterionRequirements(orderElement)) {
            if (requirement.getCriterion().equals(criterion)) {
                return true;
            }
        }
        return false;
    }

    protected abstract Set<CriterionRequirement> getCriterionRequirements(
            T orderElement);

    /*
     * Operations to set the valid value of a criterion Requirements and the
     * criterion Requirement of its children.
     */

    public void propagateValidCriterionRequirement(T orderElement,
            DirectCriterionRequirement parent, boolean valid) {
        if (isOrderLine(orderElement)) {
            setValidCriterionRequirementChildrenAsOrderLine(
                    toOrderLine(orderElement), parent, valid);
        } else {
            setValidCriterionRequirementChildrenAsOrderLineGroup(
                    toOrderLineGroup(orderElement), parent, valid);
        }
    }

    protected void setValidCriterionRequirementChildrenAsOrderLineGroup(
            R orderLineGroup, DirectCriterionRequirement parent,
            boolean valid) {
        for (T child : getOrderLineGroupChildren(orderLineGroup)) {
            IndirectCriterionRequirement indirect = findIndirectRequirementByParent(
                    getIndirectCriterionRequirement(child), parent);
            if (indirect != null) {
                indirect.setValid(valid);
            }
            propagateValidCriterionRequirement(child, parent, valid);
        }
    }

    protected abstract Set<IndirectCriterionRequirement> getIndirectCriterionRequirement(
            T orderElement);

    protected void setValidCriterionRequirementChildrenAsOrderLine(S orderLine,
            DirectCriterionRequirement parent, boolean valid) {
        for (HoursGroup hoursGroup : getHoursGroups(orderLine)) {
            IndirectCriterionRequirement indirect = findIndirectRequirementByParent(
                    hoursGroup.getIndirectCriterionRequirement(), parent);
            if (indirect != null) {
                indirect.setValid(valid);
            }
        }
    }

    /*
     * Operation to update the criterions requirements of the orderElement and
     * its children
     */
    public void propagateRemoveCriterionRequirement(T orderElement,
            DirectCriterionRequirement parent) {
        if (isOrderLine(orderElement)) {
            removeIndirectCriterionRequirementAsOrderLine(toOrderLine(orderElement), parent);
        } else {
            removeIndirectCriterionRequirement(toOrderLineGroup(orderElement),
                    parent);
        }
    }

    protected void removeIndirectCriterionRequirement(
            R orderLineGroup, DirectCriterionRequirement parent) {
        for (T child : getOrderLineGroupChildren(orderLineGroup)) {
            IndirectCriterionRequirement indirect = findIndirectRequirementByParent(
                    getIndirectCriterionRequirement(child), parent);
            if (indirect != null) {
                propagateRemoveCriterionRequirement(child, parent);
                removeCriterionRequirement(child, indirect);
            }
        }
    }

    protected abstract void removeCriterionRequirement(T orderElement,
            CriterionRequirement criterionRequirement);

    protected void removeIndirectCriterionRequirementAsOrderLine(S orderLine,
            DirectCriterionRequirement parent) {
        for (HoursGroup hoursGroup : getHoursGroups(orderLine)) {
            IndirectCriterionRequirement indirect = findIndirectRequirementByParent(
                    hoursGroup.getIndirectCriterionRequirement(), parent);
            if (indirect != null) {
                hoursGroup.removeCriterionRequirement(indirect);
            }
        }
    }

    public void removeDirectCriterionRequirement(T orderElement, DirectCriterionRequirement criterionRequirement){
        propagateRemoveCriterionRequirement(orderElement, criterionRequirement);
        removeCriterionRequirement(orderElement, criterionRequirement);
    }

    protected abstract List<HoursGroup> getOrderLineHoursGroups(S orderLine);

    /*
     * Operation to update the criterions requirements of the orderElement and
     * its children
     */

    public void propagateUpdateCriterionRequirements(T orderElement) {
        if (orderElement instanceof OrderLine) {
            updateCriterionRequirementsIntoOrderLine(toOrderLine(orderElement));
        } else {
            updateCriterionRequirementsIntoOrderLineGroup(toOrderLineGroup(orderElement));
        }
    }

    private void updateCriterionRequirementsIntoOrderLineGroup(
            R orderLineGroup) {
        for (T child : getOrderLineGroupChildren(orderLineGroup)) {
            updateMyCriterionRequirements(child);
            propagateUpdateCriterionRequirements(child);
        }
    }

    protected abstract List<T> getChildren(T orderElement);

    public void updateCriterionRequirementsIntoOrderLine(S orderLine) {
        for (HoursGroup hoursGroup : getHoursGroups(orderLine)) {
            hoursGroup.updateMyCriterionRequirements();
        }
    }

    public void propagateIndirectCriterionRequirementsKeepingValid(S orderLine) {
        for (HoursGroup hoursGroup : getHoursGroups(orderLine)) {
            hoursGroup.propagateIndirectCriterionRequirementsKeepingValid();
        }
    }

    void transformDirectToIndirectIfNeeded(T orderElement,
            Set<IndirectCriterionRequirement> currents) {
        for (DirectCriterionRequirement direct : getDirectCriterionRequirement(orderElement)) {
            IndirectCriterionRequirement indirect = findIndirectRequirementByCriterion(
                    currents, direct.getCriterion());
            if (indirect != null) {
                removeDirectCriterionRequirement(orderElement, direct);
            }
        }
    }

    protected abstract Set<DirectCriterionRequirement> getDirectCriterionRequirement(
            T orderElement);

    void addNewsIndirects(T orderElement,
            Set<IndirectCriterionRequirement> currents) {
        Set<IndirectCriterionRequirement> indirects = getIndirectCriterionRequirement(orderElement);
        for (IndirectCriterionRequirement current : currents) {
            if (!indirects.contains(current)) {
                basicAddCriterionRequirement(orderElement, current);
            }
        }
    }

    protected abstract void basicAddCriterionRequirement(T orderElement,
            CriterionRequirement criterionRequirement);

    void removeOldIndirects(T orderElement,
            Set<IndirectCriterionRequirement> currents) {
        for (IndirectCriterionRequirement indirect : getIndirectCriterionRequirement(orderElement)) {
            if (!currents.contains(indirect)) {
                removeCriterionRequirement(orderElement, indirect);
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
            valid = ((IndirectCriterionRequirement) requirement).isValid();
        }

        indirect = findIndirectRequirementByParent(oldIndirects, parent);
        if (indirect == null) {
            indirect = IndirectCriterionRequirement.create(parent, requirement
                    .getCriterion());
            indirect.setValid(valid);
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
    protected void copyRequirementToOrderLineGroup(S orderLine, R orderLineGroup) {
        // copy the directCriterionRequirement
        for (DirectCriterionRequirement newRequirement :
                copyDirectRequirements(getDirectCriterionRequirementFromOrderLine(orderLine))) {
            basicAddCriterionRequirementIntoOrderLineGroup(orderLineGroup, newRequirement);
        }

        // copy the IndirectCriterionRequirement
        for (IndirectCriterionRequirement newRequirement :
                copyIndirectRequirements(getIndirectCriterionRequirementFromOrderLine(orderLine))) {
            basicAddCriterionRequirementIntoOrderLineGroup(orderLineGroup, newRequirement);
        }
    }

    protected abstract Set<DirectCriterionRequirement> getDirectCriterionRequirementFromOrderLine(S orderLine);

    protected abstract Set<IndirectCriterionRequirement> getIndirectCriterionRequirementFromOrderLine(S orderLine);

    protected abstract void basicAddCriterionRequirementIntoOrderLineGroup(
            R orderLineGroup,
            CriterionRequirement criterionRequirement);

    protected void copyRequirementToOrderLine(R orderLineGroup, S orderLine) {
        // copy the directCriterionRequirement
        for (DirectCriterionRequirement newRequirement : copyDirectRequirements(
                getDirectCriterionRequirementFromOrderLineGroup(orderLineGroup))) {
            addDirectCriterionRequirementToOrderLine(orderLine, newRequirement);
        }
        // copy the IndirectCriterionRequirement
        for (IndirectCriterionRequirement newRequirement : copyIndirectRequirements(
                getIndirectCriterionRequirementFromOrderLineGroup(orderLineGroup))) {
            addIndirectCriterionRequirementToOrderLine(orderLine, newRequirement);
            propagateIndirectCriterionRequirementToOrderLineChildren(orderLine,
                    newRequirement.getParent());
        }
    }

    protected abstract Set<DirectCriterionRequirement>
        getDirectCriterionRequirementFromOrderLineGroup(R orderLineGroup);

    protected abstract void addIndirectCriterionRequirementToOrderLine(S orderLine, IndirectCriterionRequirement indirect);

    protected abstract Set<IndirectCriterionRequirement> getIndirectCriterionRequirementFromOrderLineGroup(R orderLineGroup);

    private Set<DirectCriterionRequirement> copyDirectRequirements(
            Set<DirectCriterionRequirement> collection) {
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
            Set<CriterionRequirement> requirements, ResourceEnum resourceType) {
        Set<CriterionRequirement> result = new HashSet<CriterionRequirement>();
        for (CriterionRequirement requirement : requirements) {
            ResourceEnum resourceTypeParent = requirement.getCriterion()
                    .getType().getResource();
            if (resourceTypeParent.equals(resourceType)) {
                result.add(requirement);
            }
        }
        return result;
    }

    /**
     * Filters {@link DirectCriterionRequirement} from criterionRequirements
     *
     * @param criterionRequirements
     * @return
     */
    public Set<DirectCriterionRequirement> getDirectCriterionRequirement(
            Set<CriterionRequirement> criterionRequirements) {

        Set<DirectCriterionRequirement> list = new HashSet<DirectCriterionRequirement>();

        for (CriterionRequirement criterionRequirement : criterionRequirements) {
            if (criterionRequirement instanceof DirectCriterionRequirement) {
                list.add((DirectCriterionRequirement) criterionRequirement);
            }
        }
        return list;
    }

    public Set<IndirectCriterionRequirement> getIndirectCriterionRequirement(
            Set<CriterionRequirement> criterionRequirements) {

        Set<IndirectCriterionRequirement> list = new HashSet<IndirectCriterionRequirement>();

        for (CriterionRequirement criterionRequirement : criterionRequirements) {
            if (criterionRequirement instanceof IndirectCriterionRequirement) {
                list.add((IndirectCriterionRequirement) criterionRequirement);
            }
        }
        return list;
    }

    public void addCriterionRequirement(T orderElement,
            CriterionRequirement criterionRequirement) {
        if (criterionRequirement instanceof DirectCriterionRequirement) {
            addDirectCriterionRequirement(orderElement, (DirectCriterionRequirement) criterionRequirement);
        } else { // criterionRequirement instanceof IndirectCriterionRequirement
            addIndirectCriterionRequirement(orderElement, (IndirectCriterionRequirement) criterionRequirement);
        }
    }

    public void addDirectCriterionRequirement(T orderElement,
            CriterionRequirement newRequirement) {
        if (canAddCriterionRequirement(orderElement,
                newRequirement)) {
            basicAddCriterionRequirement(orderElement, newRequirement);
            propagateDirectCriterionRequirementAddition(orderElement,
                            newRequirement);
        } else {
            final Criterion criterion = newRequirement.getCriterion();
            throw new IllegalStateException(_(
                    " The {0} already exist into other task",
                    criterion.getName()));
        }
    }

    public void addIndirectCriterionRequirement(T orderElement,
            IndirectCriterionRequirement criterionRequirement) {
        basicAddCriterionRequirement(orderElement, criterionRequirement);
    }

    protected void addDirectCriterionRequirementToOrderLine(S orderLine, DirectCriterionRequirement direct) {
        addDirectCriterionRequirement(toOrderElement(orderLine), direct);
    }

    public void updateMyCriterionRequirements(T orderElement) {
        final T parent = getParent(orderElement);

        if (parent != null) {
            Set<CriterionRequirement> requirementsParent = getCriterionRequirements(parent);
            Set<IndirectCriterionRequirement> currentIndirects = getCurrentIndirectRequirements(
                    getIndirectCriterionRequirement(orderElement),
                    requirementsParent);
            transformDirectToIndirectIfNeeded(orderElement, currentIndirects);
            removeOldIndirects(orderElement, currentIndirects);
            addNewsIndirects(orderElement, currentIndirects);
        }
    }

    protected abstract T getParent(T orderElement);

    /**
     * Propagates {@link IndirectCriterionRequirement} for an
     * {@link OrderElement} or {@link OrderElementTemplate} preserving its valid
     * attribute
     *
     */
    public void propagateIndirectCriterionRequirementsForOrderLineGroupKeepingValid(
            R orderLineGroup,
            DirectCriterionRequirement parent) {

        copyIndirectCriterionRequirementsFromOriginalToOrderLineGroupChildren(
                orderLineGroup, parent);
        copyIndirectCriterionRequirementsFromOriginalToHoursGroup(
                orderLineGroup, parent);
    }

    public abstract void copyIndirectCriterionRequirementsFromOriginalToOrderLineGroupChildren(
            R orderLineGroup,
            DirectCriterionRequirement parent);

    public void copyIndirectCriterionRequirementsFromOriginalToHoursGroup(
            R orderLineGroup,
            DirectCriterionRequirement parent) {

        final List<T> orderElements = getOrderLineGroupChildren(orderLineGroup);
        final Criterion criterion = parent.getCriterion();
        final Set<IndirectCriterionRequirement> originalIndirectCriterionRequirements = parent
                .getOrigin().getChildren();
        final Map<HoursGroup, Map<Criterion, Boolean>> mapHoursGroup =
            createHoursGroupCriterionMap(originalIndirectCriterionRequirements);

        for (T each: orderElements) {

            IndirectCriterionRequirement indirect = IndirectCriterionRequirement
                    .create(parent, criterion);

            if (isOrderLine(each)) {
                for (HoursGroup hoursGroup: myHoursGroups(toOrderLine(each))) {
                    Map<Criterion, Boolean> criterionMap = mapHoursGroup.get(hoursGroup.getOrigin());
                    if (criterionMap != null) {
                        final Boolean valid = criterionMap.get(indirect.getCriterion());
                        indirect.setValid(valid);
                    }
                    hoursGroup.addCriterionRequirement(indirect);
                }
            }

            if (isOrderLineGroup(each)) {
                copyIndirectCriterionRequirementsFromOriginalToHoursGroup(
                        toOrderLineGroup(each), parent);
            }

        }
    }

    protected abstract boolean isOrderLineGroup(T orderElement);

    protected abstract Collection<HoursGroup> myHoursGroups(S orderline);

    /**
     * Creates a mapping between {@link HoursGroup} and a tuple (criterion,
     * boolean) from a list of {@link IndirectCriterionRequirement}
     *
     * The valid value of an {@link IndirectCriterionRequirement} can be later
     * retrieve knowing its {@link HoursGroup} and its {@link Criterion}
     *
     * This data structure is used to keep the original valid value from
     * {@link IndirectCriterionRequirement} when copying an {@link Order} to a
     * {@link Template} or vice-versa
     *
     * @param indirects
     * @return
     */
    private Map<HoursGroup, Map<Criterion, Boolean>> createHoursGroupCriterionMap(
            Set<IndirectCriterionRequirement> indirects) {

        Map<HoursGroup, Map<Criterion, Boolean>> result =
            new HashMap<HoursGroup, Map<Criterion, Boolean>>();

        for (IndirectCriterionRequirement each: indirects) {
            final HoursGroup hoursGroup = each.getHoursGroup();
            if (hoursGroup != null) {
                Map<Criterion, Boolean> value = result.get(hoursGroup);
                if (value == null) {
                    value = new HashMap<Criterion, Boolean>();
                }
                value.put(each.getCriterion(), each.isValid());
                result.put(hoursGroup, value);
            }
        }
        return result;
    }

}
