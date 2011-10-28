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

package org.libreplan.business.orders.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.libreplan.business.requirements.entities.CriterionRequirement;
import org.libreplan.business.requirements.entities.DirectCriterionRequirement;
import org.libreplan.business.requirements.entities.IndirectCriterionRequirement;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.templates.entities.OrderElementTemplate;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class CriterionRequirementOrderElementHandler extends
        CriterionRequirementHandler<OrderElement, OrderLine, OrderLineGroup> {

    private static final CriterionRequirementOrderElementHandler singleton =
        new CriterionRequirementOrderElementHandler();

    private CriterionRequirementOrderElementHandler() {

    }

    public static CriterionRequirementOrderElementHandler getInstance() {
        return singleton;
    }

    @Override
    protected List<OrderElement> getOrderLineGroupChildren(OrderLineGroup orderLineGroup) {
        return orderLineGroup.getChildren();
    }

    @Override
    protected List<OrderElement> getAllChildren(OrderElement orderElement) {
        return orderElement.getAllChildren();
    }

    @Override
    protected List<OrderElement> getChildren(OrderElement orderElement) {
        return orderElement.getChildren();
    }

    @Override
    protected Set<CriterionRequirement> getCriterionRequirements(OrderElement orderElement) {
        return orderElement.getCriterionRequirements();
    }

    @Override
    protected Set<IndirectCriterionRequirement> getIndirectCriterionRequirement(OrderElement orderElement) {
        return orderElement.getIndirectCriterionRequirement();
    }

    @Override
    protected void removeCriterionRequirement(OrderElement orderElement,
            CriterionRequirement criterionRequirement) {
        orderElement.removeCriterionRequirement(criterionRequirement);
    }

    @Override
    protected List<HoursGroup> getOrderLineHoursGroups(OrderLine orderLine) {
        return orderLine.getHoursGroups();
    }

    @Override
    protected Set<DirectCriterionRequirement> getDirectCriterionRequirement(OrderElement orderElement) {
        return orderElement.getDirectCriterionRequirement();
    }

    @Override
    protected void basicAddCriterionRequirement(OrderElement orderElement, CriterionRequirement criterionRequirement) {
        orderElement.basicAddCriterionRequirement(criterionRequirement);
    }

    @Override
    protected List<HoursGroup> getHoursGroups(OrderLine orderline) {
        return orderline.getHoursGroups();
    }

    @Override
    protected boolean isOrderLine(OrderElement orderElement) {
        return (orderElement instanceof OrderLine);
    }

    @Override
    protected OrderLine toOrderLine(OrderElement orderElement) {
        return (OrderLine) orderElement;
    }

    @Override
    protected OrderLineGroup toOrderLineGroup(OrderElement orderElement) {
        return (OrderLineGroup) orderElement;
    }

    @Override
    protected OrderElement toOrderElement(OrderLine orderLine) {
        return (OrderElement) orderLine;
    }

    @Override
    protected Set<DirectCriterionRequirement>
        getDirectCriterionRequirementFromOrderLineGroup(OrderLineGroup orderLineGroup) {
        return orderLineGroup.getDirectCriterionRequirement();
    }

    @Override
    protected void addIndirectCriterionRequirementToOrderLine(OrderLine orderLine, IndirectCriterionRequirement indirect) {
        orderLine.addIndirectCriterionRequirement(indirect);
    }

    @Override
    protected Set<IndirectCriterionRequirement> getIndirectCriterionRequirementFromOrderLineGroup(
            OrderLineGroup orderLine) {
        return orderLine.getIndirectCriterionRequirement();
    }

    @Override
    protected Set<DirectCriterionRequirement> getDirectCriterionRequirementFromOrderLine(OrderLine orderLine) {
        return orderLine.getDirectCriterionRequirement();
    }

    @Override
    protected Set<IndirectCriterionRequirement> getIndirectCriterionRequirementFromOrderLine(OrderLine orderLine) {
        return orderLine.getIndirectCriterionRequirement();
    }

    @Override
    protected void basicAddCriterionRequirementIntoOrderLineGroup(OrderLineGroup orderLineGroup, CriterionRequirement criterionRequirement) {
        orderLineGroup.basicAddCriterionRequirement(criterionRequirement);
    }

    @Override
    protected OrderElement getParent(OrderElement orderElement) {
        return orderElement.getParent();
    }

    @Override
    protected boolean isOrderLineGroup(OrderElement orderElement) {
        return (orderElement instanceof OrderLineGroup);
    }

    @Override
    protected Collection<HoursGroup> myHoursGroups(OrderLine orderline) {
        return orderline.myHoursGroups();
    }

    /**
     * For every OrderLineGroup, its {@link DirectCriterionRequirement} are
     * copied to their children ({@link IndirectCriterionRequirement}). Every entry of
     * {@link DirectCriterionRequirement} keeps a reference to its original
     * (_origin_). Original {@link IndirectCriterionRequirement} can be accessed
     * from _origin_.
     *
     * A unique IndirectCriterionRequirements can be referenced knowing two
     * parameters: {@link OrderElement} or {@link OrderElementTemplate}, and
     * {@link Criterion}. A map containing the original valid value of an
     * indirect criterion is created, indexing by order element and criterion.
     *
     * Every order element keeps a reference to its template, and vice-versa. So
     * when propagating a criterion it's possible to know the original value of
     * valid via the map previously created
     */
    @Override
    public void copyIndirectCriterionRequirementsFromOriginalToOrderLineGroupChildren(
            OrderLineGroup orderLineGroup, DirectCriterionRequirement parent) {

        final List<OrderElement> orderElements = orderLineGroup.getChildren();
        final Criterion criterion = parent.getCriterion();
        final Set<IndirectCriterionRequirement> originalIndirectCriterionRequirements = parent
                .getOrigin().getChildren();
        final Map<OrderElementTemplate, Map<Criterion, Boolean>> mapTemplateCriterion =
            createTemplateCriterionMap(originalIndirectCriterionRequirements);

        for (OrderElement each : orderElements) {
            Map<Criterion, Boolean> criterionMap = mapTemplateCriterion
                    .get(each.getTemplate());
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

    /**
     * Map containing {@link IndirectCriterionRequirement} valid attribute,
     * indexing by {@link OrderElementTemplate} and {@link Criterion}
     *
     * @param indirects
     * @return
     */
    private Map<OrderElementTemplate, Map<Criterion, Boolean>> createTemplateCriterionMap(
            Set<IndirectCriterionRequirement> indirects) {

        Map<OrderElementTemplate, Map<Criterion, Boolean>> result =
            new HashMap<OrderElementTemplate, Map<Criterion, Boolean>>();

        for (IndirectCriterionRequirement each: indirects) {
            final OrderElementTemplate template = each.getOrderElementTemplate();
            if (template != null) {
                Map<Criterion, Boolean> value = result.get(template);
                if (value == null) {
                    value = new HashMap<Criterion, Boolean>();
                }
                value.put(each.getCriterion(), each.isValid());
                result.put(template, value);
            }
        }
        return result;
    }

}
