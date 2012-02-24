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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.libreplan.business.advance.entities.AdvanceAssignment;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.advance.entities.IndirectAdvanceAssignment;
import org.libreplan.business.planner.entities.DayAssignment.FilterType;
import org.libreplan.business.requirements.entities.CriterionRequirement;
import org.libreplan.business.templates.entities.OrderLineTemplate;

public class OrderLine extends OrderElement {

    private HoursGroupOrderLineHandler hoursGroupOrderLineHandler = HoursGroupOrderLineHandler
            .getInstance();

    public static OrderLine create() {
        OrderLine result = new OrderLine();
        result.setNewObject(true);
        return result;
    }

    public static OrderLine createUnvalidated(String code) {
        OrderLine orderLine = create(new OrderLine(), code);
        return orderLine;
    }

    public static OrderLine createUnvalidatedWithUnfixedPercentage(String code,
            int hours) {
        OrderLine orderLine = createOrderLineWithUnfixedPercentage(hours);
        return create(orderLine, code);
    }

    public static OrderLine createOrderLineWithUnfixedPercentage(int hours) {
        OrderLine result = create();
        HoursGroup hoursGroup = HoursGroup.create(result);
        result.addHoursGroup(hoursGroup);
        hoursGroup.setFixedPercentage(false);
        hoursGroup.setPercentage(new BigDecimal(1));
        hoursGroup.setWorkingHours(hours);
        return result;
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public OrderLine() {

    }

    @Valid
    private Set<HoursGroup> hoursGroups = new HashSet<HoursGroup>();

    private Integer lastHoursGroupSequenceCode = 0;

    @Override
    public Integer getWorkHours() {
        return hoursGroupOrderLineHandler.calculateTotalHours(hoursGroups);
    }

    @Override
    public List<OrderElement> getChildren() {
        return new ArrayList<OrderElement>();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public boolean isEmptyLeaf() {
        if (getWorkHours() != 0) {
            return false;
        }
        if (!getDirectCriterionRequirement().isEmpty()) {
            return false;
        }
        if (!getDirectAdvanceAssignments().isEmpty()) {
            for (DirectAdvanceAssignment each : getDirectAdvanceAssignments()) {
                if (!each.getAdvanceMeasurements().isEmpty()) {
                    return false;
                }
            }
        }
        if (!getQualityForms().isEmpty()) {
            return false;
        }
        if (!getLabels().isEmpty()) {
            return false;
        }
        if (!getMaterialAssignments().isEmpty()) {
            return false;
        }
        if (!getSumChargedEffort().getDirectChargedEffort().isZero()) {
            return false;
        }
        if (!getTaskElements().isEmpty()) {
            if (!getTaskElements().iterator().next()
                    .getDayAssignments(FilterType.KEEP_ALL).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public OrderLine toLeaf() {
        return this;
    }

    @Override
    public OrderLineGroup toContainer() {
        OrderLineGroup result = OrderLineGroup.create();
        String name = "new container";
        if (isEmptyLeaf()) {
            name = getName();
        }
        result.setName(name);
        return result;
    }

    @Valid
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
        hoursGroup.setParentOrderLine(this);
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

    /**
     * Operations for manipulating {@link HoursGroup}
     */

    public void setWorkHours(Integer workHours) throws IllegalArgumentException {
        hoursGroupOrderLineHandler.setWorkHours(this, workHours);
    }

    public boolean isTotalHoursValid(Integer total) {
        return hoursGroupOrderLineHandler.isTotalHoursValid(total, hoursGroups);
    }

    public boolean isPercentageValid() {
        return hoursGroupOrderLineHandler.isPercentageValid(hoursGroups);
    }

    public void recalculateHoursGroups() {
        hoursGroupOrderLineHandler.recalculateHoursGroups(this);
    }

    @Override
    public BigDecimal getAdvancePercentage(LocalDate date) {
        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if (directAdvanceAssignment.getReportGlobalAdvance()) {
                if (date == null) {
                    return directAdvanceAssignment.getAdvancePercentage();
                }
                return directAdvanceAssignment.getAdvancePercentage(date);
            }
        }

        return BigDecimal.ZERO;
    }

    public Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments(
            AdvanceType advanceType) {
        Set<DirectAdvanceAssignment> result = new HashSet<DirectAdvanceAssignment>();
        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if (directAdvanceAssignment.getAdvanceType().getUnitName().equals(
                    advanceType.getUnitName())) {
                result.add(directAdvanceAssignment);
                return result;
            }
        }
        return result;
    }

    @Override
    public Set<IndirectAdvanceAssignment> getAllIndirectAdvanceAssignments(
            AdvanceType advanceType) {
        return Collections.emptySet();
    }

    @Override
    protected Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments() {
        return getDirectAdvanceAssignments();
    }

    @Override
    protected Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignmentsReportGlobal() {
        Set<DirectAdvanceAssignment> result = new HashSet<DirectAdvanceAssignment>();
        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if (directAdvanceAssignment.getReportGlobalAdvance()) {
                result.add(directAdvanceAssignment);
                return result;
            }
        }
        return result;
    }

    public boolean existSameCriterionRequirement(
            CriterionRequirement newRequirement) {
        return criterionRequirementHandler
                .existSameCriterionRequirementIntoOrderLine(this,
                        newRequirement);
    }

    @Override
    public DirectAdvanceAssignment getReportGlobalAdvanceAssignment() {
        for (DirectAdvanceAssignment directAdvanceAssignment : getDirectAdvanceAssignments()) {
            if (directAdvanceAssignment.getReportGlobalAdvance()) {
                return directAdvanceAssignment;
            }
        }
        return null;
    }

    @Override
    public void removeReportGlobalAdvanceAssignment() {
        AdvanceAssignment advanceAssignment = getReportGlobalAdvanceAssignment();
        if (advanceAssignment != null) {
            advanceAssignment.setReportGlobalAdvance(false);
        }
        markAsDirtyLastAdvanceMeasurementForSpreading();
    }

    public boolean containsHoursGroup(String code) {
        for (HoursGroup hoursGroup : getHoursGroups()) {
            if (hoursGroup.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    public HoursGroup getHoursGroup(String code) {
        if (code == null) {
            return null;
        }

        for (HoursGroup hoursGroup : getHoursGroups()) {
            if (hoursGroup.getCode().equals(code)) {
                return hoursGroup;
            }
        }
        return null;
    }

    @Override
    public DirectAdvanceAssignment getAdvanceAssignmentByType(AdvanceType type) {
        return getDirectAdvanceAssignmentByType(type);
    }

    public void incrementLastHoursGroupSequenceCode() {
        if (lastHoursGroupSequenceCode == null) {
            lastHoursGroupSequenceCode = 0;
        }
        lastHoursGroupSequenceCode++;
    }

    @NotNull(message = "last hours group sequence code not specified")
    public Integer getLastHoursGroupSequenceCode() {
        return lastHoursGroupSequenceCode;
    }

    @AssertTrue(message = "some code is repeated between hours group codes")
    public boolean checkConstraintHoursGroupsCodeNotRepeated() {
        Set<String> codes = new HashSet<String>();

        for (HoursGroup hoursGroup : getHoursGroups()) {
            String code = hoursGroup.getCode();
            if (codes.contains(code)) {
                return false;
            }
            codes.add(code);
        }
        return true;
    }

    @Override
    public OrderLineTemplate createTemplate() {
        return OrderLineTemplate.create(this);
    }

    @Override
    public DirectAdvanceAssignment calculateFakeDirectAdvanceAssignment(
            IndirectAdvanceAssignment indirectAdvanceAssignment) {
        return null;
    }

    @Override
    public BigDecimal getAdvancePercentageChildren() {
        return BigDecimal.ZERO;
    }

    @Override
    public Set<IndirectAdvanceAssignment> getIndirectAdvanceAssignments() {
        return Collections.emptySet();
    }

    @Override
    public OrderLine calculateOrderLineForSubcontract() {
        return this;
    }

    @Override
    public void setCode(String code) {
        super.setCode(code);

        Order order = getOrder();
        if ((order != null) && (!order.isCodeAutogenerated())) {
            for (HoursGroup hoursGroup : getHoursGroups()) {
                if ((hoursGroup.getCode() == null)
                        || (hoursGroup.getCode().isEmpty())) {
                    hoursGroup.setCode(code);
                }
            }
        }
    }

}
