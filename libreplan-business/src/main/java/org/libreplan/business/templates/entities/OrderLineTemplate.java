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
package org.libreplan.business.templates.entities;

import static org.libreplan.business.i18n.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.OrderLineGroup;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class OrderLineTemplate extends OrderElementTemplate {

    @Valid
    private Set<HoursGroup> hoursGroups = new HashSet<HoursGroup>();

    private Integer lastHoursGroupSequenceCode = 0;

    public static OrderLineTemplate create(OrderLine orderLine) {
        OrderLineTemplate beingBuilt = new OrderLineTemplate();
        copyHoursGroup(orderLine.getHoursGroups(), beingBuilt);
        beingBuilt.setBudget(orderLine.getBudget());
        return create(beingBuilt, orderLine);
    }

    private static void copyHoursGroup(
            final Collection<HoursGroup> hoursGroups,
            OrderLineTemplate orderLineTemplate) {
        for (HoursGroup each: hoursGroups) {
            orderLineTemplate.addHoursGroup(HoursGroup.copyFrom(each,
                    orderLineTemplate));
        }
    }

    public static OrderLineTemplate createNew() {
        return createNew(new OrderLineTemplate());
    }

    private BigDecimal budget = BigDecimal.ZERO.setScale(2);

    protected <T extends OrderElement> T setupElementParts(T orderElement) {
        super.setupElementParts(orderElement);
        setupHoursGroups((OrderLine) orderElement);
        setupBudget((OrderLine) orderElement);
        return orderElement;
    }

    private void setupHoursGroups(OrderLine orderLine) {
        Set<HoursGroup> result = new HashSet<HoursGroup>();
        for (HoursGroup each: getHoursGroups()) {
            result.add(HoursGroup.copyFrom(each, orderLine));
        }
        orderLine.setHoursGroups(result);
    }

    private void setupBudget(OrderLine orderLine) {
        orderLine.setBudget(getBudget());
    }

    @Override
    public List<OrderElementTemplate> getChildrenTemplates() {
        return Collections.emptyList();
    }

    @Override
    public OrderElementTemplate toLeaf() {
        return this;
    }

    @Override
    public OrderLineGroupTemplate toContainer() {
        OrderLineGroupTemplate result = OrderLineGroupTemplate.createNew();
        copyTo(result);
        return result;
    }

    @Override
    public List<OrderElementTemplate> getChildren() {
        return new ArrayList<OrderElementTemplate>();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public OrderElement createElement(OrderLineGroup parent) {
        OrderLine line = setupSchedulingStateType(setupVersioningInfo(parent,
                OrderLine.createOrderLineWithUnfixedPercentage(getWorkHours())));
        line.initializeTemplate(this);
        parent.add(line);
        return setupElementParts(line);
    }

    @Override
    public String getType() {
        return _("Line");
    }

    public Integer getWorkHours() {
        return hoursGroupOrderLineTemplateHandler.calculateTotalHours(hoursGroups);
    }

    public void incrementLastHoursGroupSequenceCode() {
        if(lastHoursGroupSequenceCode==null){
            lastHoursGroupSequenceCode = 0;
        }
        lastHoursGroupSequenceCode++;
    }

    @NotNull(message = "last hours group sequence code not specified")
    public Integer getLastHoursGroupSequenceCode() {
        return lastHoursGroupSequenceCode;
    }

    /**
     * Operations for manipulating {@link HoursGroup}
     */

    @Override
    public List<HoursGroup> getHoursGroups() {
        return new ArrayList<HoursGroup>(hoursGroups);
    }

    public Set<HoursGroup> myHoursGroups() {
        return hoursGroups;
    }

    public void setHoursGroups(final Set<HoursGroup> hoursGroups) {
        this.hoursGroups.clear();
        this.hoursGroups.addAll(hoursGroups);
    }

    public void addHoursGroup(HoursGroup hoursGroup) {
        hoursGroup.setOrderLineTemplate(this);
        hoursGroup.updateMyCriterionRequirements();
        doAddHoursGroup(hoursGroup);
        recalculateHoursGroups();
    }

    public void doAddHoursGroup(HoursGroup hoursGroup) {
        hoursGroups.add(hoursGroup);
    }

    public void deleteHoursGroup(HoursGroup hoursGroup) {
        hoursGroups.remove(hoursGroup);
        recalculateHoursGroups();
    }

    private HoursGroupOrderLineTemplateHandler hoursGroupOrderLineTemplateHandler = HoursGroupOrderLineTemplateHandler
            .getInstance();

    public void setWorkHours(Integer workHours) throws IllegalArgumentException {
        hoursGroupOrderLineTemplateHandler.setWorkHours(this, workHours);
    }

    public boolean isTotalHoursValid(Integer total) {
        return hoursGroupOrderLineTemplateHandler.isTotalHoursValid(total, hoursGroups);
    }

    public boolean isPercentageValid() {
        return hoursGroupOrderLineTemplateHandler.isPercentageValid(hoursGroups);
    }

    public void recalculateHoursGroups() {
        hoursGroupOrderLineTemplateHandler.recalculateHoursGroups(this);
    }

    public void setBudget(BigDecimal budget) {
        Validate.isTrue(budget.compareTo(BigDecimal.ZERO) >= 0,
                "budget cannot be negative");
        this.budget = budget.setScale(2);
    }

    @Override
    @NotNull(message = "budget not specified")
    public BigDecimal getBudget() {
        return budget;
    }

}
