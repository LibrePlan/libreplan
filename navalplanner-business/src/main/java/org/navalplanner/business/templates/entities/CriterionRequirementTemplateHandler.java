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

package org.navalplanner.business.templates.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.business.orders.entities.CriterionRequirementHandler;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class CriterionRequirementTemplateHandler extends
    CriterionRequirementHandler<OrderElementTemplate, OrderLineTemplate, OrderLineGroupTemplate>{

    private static final CriterionRequirementTemplateHandler singleton =
        new CriterionRequirementTemplateHandler();

    private CriterionRequirementTemplateHandler() {

    }

    public static CriterionRequirementTemplateHandler getInstance() {
        return singleton;
    }

    @Override
    protected void addIndirectCriterionRequirementToOrderLine(
            OrderLineTemplate orderLine, IndirectCriterionRequirement indirect) {
        orderLine.addIndirectCriterionRequirement(indirect);
    }

    @Override
    protected void basicAddCriterionRequirement(
            OrderElementTemplate orderElement,
            CriterionRequirement criterionRequirement) {
        orderElement.basicAddCriterionRequirement(criterionRequirement);
    }

    @Override
    protected void basicAddCriterionRequirementIntoOrderLineGroup(
            OrderLineGroupTemplate orderLineGroup,
            CriterionRequirement criterionRequirement) {
        orderLineGroup.basicAddCriterionRequirement(criterionRequirement);
    }

    @Override
    protected List<OrderElementTemplate> getAllChildren(
            OrderElementTemplate orderElement) {
        return orderElement.getAllChildren();
    }

    @Override
    protected List<OrderElementTemplate> getChildren(
            OrderElementTemplate orderElement) {
        return orderElement.getChildren();
    }

    @Override
    protected Set<CriterionRequirement> getCriterionRequirements(
            OrderElementTemplate orderElement) {
        return orderElement.getCriterionRequirements();
    }

    @Override
    protected Set<DirectCriterionRequirement> getDirectCriterionRequirement(
            OrderElementTemplate orderElement) {
        return orderElement.getDirectCriterionRequirements();
    }

    @Override
    protected Set<DirectCriterionRequirement> getDirectCriterionRequirementFromOrderLine(
            OrderLineTemplate orderLine) {
        return orderLine.getDirectCriterionRequirements();
    }

    @Override
    protected Set<DirectCriterionRequirement> getDirectCriterionRequirementFromOrderLineGroup(
            OrderLineGroupTemplate orderLineGroup) {
        return orderLineGroup.getDirectCriterionRequirements();
    }

    @Override
    protected List<HoursGroup> getHoursGroups(OrderLineTemplate orderline) {
        return orderline.getHoursGroups();
    }

    @Override
    protected Set<IndirectCriterionRequirement> getIndirectCriterionRequirement(
            OrderElementTemplate orderElement) {
        return orderElement.getIndirectCriterionRequirement();
    }

    @Override
    protected Set<IndirectCriterionRequirement> getIndirectCriterionRequirementFromOrderLine(
            OrderLineTemplate orderLine) {
        return orderLine.getIndirectCriterionRequirement();
    }

    @Override
    protected Set<IndirectCriterionRequirement> getIndirectCriterionRequirementFromOrderLineGroup(
            OrderLineGroupTemplate orderLineGroup) {
        return orderLineGroup.getIndirectCriterionRequirement();
    }

    @Override
    protected List<OrderElementTemplate> getOrderLineGroupChildren(
            OrderLineGroupTemplate orderLineGroup) {
        return orderLineGroup.getChildren();
    }

    @Override
    protected List<HoursGroup> getOrderLineHoursGroups(
            OrderLineTemplate orderLine) {
        return orderLine.getHoursGroups();
    }

    @Override
    protected OrderElementTemplate getParent(OrderElementTemplate orderElement) {
        return orderElement.getParent();
    }

    @Override
    protected boolean isOrderLine(OrderElementTemplate orderElement) {
        return (orderElement instanceof OrderLineTemplate);
    }

    @Override
    protected boolean isOrderLineGroup(OrderElementTemplate orderElement) {
        return (orderElement instanceof OrderLineGroupTemplate);
    }

    @Override
    protected void removeCriterionRequirement(
            OrderElementTemplate orderElement,
            CriterionRequirement criterionRequirement) {
        orderElement.removeCriterionRequirement(criterionRequirement);
    }

    @Override
    protected OrderElementTemplate toOrderElement(OrderLineTemplate orderLine) {
        return (OrderElementTemplate) orderLine;
    }

    @Override
    protected OrderLineTemplate toOrderLine(OrderElementTemplate orderElement) {
        return (OrderLineTemplate) orderElement;
    }

    @Override
    protected OrderLineGroupTemplate toOrderLineGroup(
            OrderElementTemplate orderElement) {
        return (OrderLineGroupTemplate) orderElement;
    }

    @Override
    protected Collection<HoursGroup> myHoursGroups(OrderLineTemplate orderline) {
       return orderline.myHoursGroups();
    }

    @Override
    public void copyIndirectCriterionRequirementsFromOriginalToOrderLineGroupChildren(
            OrderLineGroupTemplate orderLineGroup,
            DirectCriterionRequirement parent) {

        final List<OrderElementTemplate> orderElements = orderLineGroup.getChildren();
        final Criterion criterion = parent.getCriterion();
        final Set<IndirectCriterionRequirement> originalIndirectCriterionRequirements = parent
                .getOrigin().getChildren();
        final Map<OrderElement, Map<Criterion, Boolean>> mapTemplateCriterion =
            createOrderElementCriterionMap(originalIndirectCriterionRequirements);

        for (OrderElementTemplate each : orderElements) {
            Map<Criterion, Boolean> criterionMap = mapTemplateCriterion
                    .get(each.getOrigin());
            if (criterionMap != null) {
                IndirectCriterionRequirement indirect = IndirectCriterionRequirement
                        .create(parent, criterion);
                indirect.setValid(criterionMap.get(criterion));
                addIndirectCriterionRequirement(each, indirect);
            }

            if (isOrderLineGroup(each)) {
                copyIndirectCriterionRequirementsFromOriginalToOrderLineGroupChildren(
                        toOrderLineGroup(each), parent);
            }
        }

    }

    private Map<OrderElement, Map<Criterion, Boolean>> createOrderElementCriterionMap(
            Set<IndirectCriterionRequirement> indirects) {

        Map<OrderElement, Map<Criterion, Boolean>> result =
            new HashMap<OrderElement, Map<Criterion, Boolean>>();

        for (IndirectCriterionRequirement each: indirects) {
            final OrderElement orderElement = each.getOrderElement();
            if (orderElement != null) {
                Map<Criterion, Boolean> value = result.get(orderElement);
                if (value == null) {
                    value = new HashMap<Criterion, Boolean>();
                }
                value.put(each.getCriterion(), each.isValid());
                result.put(orderElement, value);
            }
        }
        return result;
    }

}
