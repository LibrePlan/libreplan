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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.navalplanner.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.navalplanner.business.advance.entities.AdvanceAssignment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceMeasurementComparator;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.entities.IndirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.qualityforms.entities.TaskQualityForm;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.business.templates.entities.OrderLineGroupTemplate;
import org.navalplanner.business.trees.ITreeParentNode;


public class OrderLineGroup extends OrderElement implements
        ITreeParentNode<OrderElement> {

    private final class ChildrenManipulator extends
            TreeNodeOnListWithSchedulingState<OrderElement> {


        private ChildrenManipulator(OrderLineGroup parent,
                List<OrderElement> children) {
            super(children);
        }

        @Override
        protected void setParentIfRequired(OrderElement newChild) {
            newChild.setParent(getThis());
        }

        @Override
        protected void updateWithNewChild(SchedulingState newChildState) {
            getThis().getSchedulingState().add(newChildState);
        }

        @Override
        protected SchedulingState getSchedulingStateFrom(OrderElement node) {
            if (!node.isSchedulingDataInitialized()) {
                node.useSchedulingDataFor(getCurrentSchedulingData()
                        .getOriginOrderVersion());
            }
            return node.getSchedulingState();
        }

        @Override
        protected void onChildAddedAdditionalActions(OrderElement newChild) {
            updateCriterionRequirements();
            newChild.updateLabels();
        }

        @Override
        protected void onChildRemovedAdditionalActions(OrderElement removedChild) {
            if (removedChild.isScheduled() && getThis().isScheduled()) {
                removeChildTask(removedChild);
            }
            updateCriterionRequirements();
        }

        private void removeChildTask(OrderElement removedChild) {
            TaskSource taskSource = removedChild.getTaskSource();
            TaskElement childTask = taskSource.getTask();
            TaskGroup group = (TaskGroup) getThis().getTaskSource()
                    .getTask();
            group.remove(childTask);
            childTask.detachFromDependencies();
        }

        @Override
        public ITreeParentNode<OrderElement> getParent() {
            return getThis().getParent();
        }

        @Override
        public ITreeParentNode<OrderElement> toContainer() {
            return OrderLineGroup.this.toContainer();
        }

        @Override
        public OrderElement toLeaf() {
            return getThis().toLeaf();
        }

        @Override
        public OrderLineGroup getThis() {
            return OrderLineGroup.this;
        }
    }

    public static OrderLineGroup create() {
        OrderLineGroup result = new OrderLineGroup();
        result.setNewObject(true);
        return result;
    }

    public static OrderLineGroup createUnvalidated(String code) {
        OrderLineGroup orderLineGroup = create(new OrderLineGroup(), code);
        return orderLineGroup;
    }

    public void addChildrenAdvanceOrderLineGroup() {
        boolean spread = (getReportGlobalAdvanceAssignment() == null);
        IndirectAdvanceAssignment indirectAdvanceAssignment = IndirectAdvanceAssignment
                .create(spread);
        AdvanceType advanceType = PredefinedAdvancedTypes.CHILDREN.getType();
        indirectAdvanceAssignment.setAdvanceType(advanceType);
        indirectAdvanceAssignment.setOrderElement(this);
        addIndirectAdvanceAssignment(indirectAdvanceAssignment);
    }

    public void removeChildrenAdvanceOrderLineGroup() {
        for (IndirectAdvanceAssignment advance : getIndirectAdvanceAssignments()) {
            if (advance.getAdvanceType().getUnitName().equals(
                    PredefinedAdvancedTypes.CHILDREN.getTypeName())) {
                indirectAdvanceAssignments.remove(advance);
                updateSpreadAdvance();
            }
        }
    }

    private void updateSpreadAdvance(){
        if(getReportGlobalAdvanceAssignment() == null){
            AdvanceType type = PredefinedAdvancedTypes.PERCENTAGE.getType();
            DirectAdvanceAssignment advancePercentage = getAdvanceAssignmentByType(type);
            if(advancePercentage != null) {
                if(advancePercentage.isFake()){
                    for (IndirectAdvanceAssignment each : getIndirectAdvanceAssignments()) {
                        if (type != null && each.getAdvanceType().getId().equals(type.getId())) {
                            each.setReportGlobalAdvance(true);
                        }
                    }
                }else{
                    advancePercentage.setReportGlobalAdvance(true);
                }
            } else {
                for (DirectAdvanceAssignment advance : getDirectAdvanceAssignments()) {
                    advance.setReportGlobalAdvance(true);
                    return;
                }
                for (IndirectAdvanceAssignment advance : getIndirectAdvanceAssignments()) {
                    advance.setReportGlobalAdvance(true);
                    return;
                }
            }
        }
    }

    public boolean existChildrenAdvance() {
        for (IndirectAdvanceAssignment advance : getIndirectAdvanceAssignments()) {
            if (advance.getAdvanceType().getUnitName().equals(
                    PredefinedAdvancedTypes.CHILDREN.getTypeName())) {
                return true;
            }
        }
        return false;
    }

    private List<OrderElement> children = new ArrayList<OrderElement>();

    private Set<IndirectAdvanceAssignment> indirectAdvanceAssignments = new HashSet<IndirectAdvanceAssignment>();

    /**
     * Constructor for hibernate. Do not use!
     */
    public OrderLineGroup() {

    }

    @Override
    @Valid
    public List<OrderElement> getChildren() {
        return new ArrayList<OrderElement>(children);
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void remove(OrderElement child) {
        getManipulator().remove(child);
        removeIndirectAdvanceAssignments(child);
    }

    @Override
    public void replace(OrderElement oldOrderElement, OrderElement orderElement) {
        getManipulator().replace(oldOrderElement, orderElement);

        addIndirectAdvanceAssignments(orderElement);
        removeIndirectAdvanceAssignments(oldOrderElement);
    }

    @Override
    public void add(OrderElement orderElement) {
        getManipulator().add(orderElement);
        addIndirectAdvanceAssignments(orderElement);
    }

    private void addIndirectAdvanceAssignments(OrderElement orderElement) {
        Set<AdvanceAssignment> toRemove = new HashSet<AdvanceAssignment>();

        for (DirectAdvanceAssignment directAdvanceAssignment : orderElement.directAdvanceAssignments) {
            if (getDirectAdvanceAssignmentByType(directAdvanceAssignment
                    .getAdvanceType()) != null) {
                toRemove.add(directAdvanceAssignment);
            } else {
                IndirectAdvanceAssignment indirectAdvanceAssignment = IndirectAdvanceAssignment
                        .create();
                indirectAdvanceAssignment
                        .setAdvanceType(directAdvanceAssignment
                                .getAdvanceType());
                indirectAdvanceAssignment.setOrderElement(this);
                this.addIndirectAdvanceAssignment(indirectAdvanceAssignment);
            }
        }

        for (AdvanceAssignment each : toRemove) {
            orderElement.removeAdvanceAssignment(each);
        }

        if (orderElement instanceof OrderLineGroup) {
            for (IndirectAdvanceAssignment indirectAdvanceAssignment : ((OrderLineGroup) orderElement)
                    .getIndirectAdvanceAssignments()) {
                this.addIndirectAdvanceAssignment(indirectAdvanceAssignment);
            }
        }

        if (indirectAdvanceAssignments.isEmpty()) {
            addChildrenAdvanceOrderLineGroup();
        }
    }

    private void removeIndirectAdvanceAssignments(OrderElement orderElement) {
        for (DirectAdvanceAssignment directAdvanceAssignment : orderElement.directAdvanceAssignments) {
            this.removeIndirectAdvanceAssignment(directAdvanceAssignment
                    .getAdvanceType());
        }

        if (orderElement instanceof OrderLineGroup) {
            for (IndirectAdvanceAssignment indirectAdvanceAssignment : ((OrderLineGroup) orderElement)
                    .getIndirectAdvanceAssignments()) {
                this.removeIndirectAdvanceAssignment(indirectAdvanceAssignment
                        .getAdvanceType());
            }
        }

        if (children.isEmpty() && (indirectAdvanceAssignments.size() == 1)
                && existChildrenAdvance()) {
            removeChildrenAdvanceOrderLineGroup();
        }
    }

    @Override
    public void up(OrderElement orderElement) {
        getManipulator().up(orderElement);
    }

    private ChildrenManipulator getManipulator() {
        return new ChildrenManipulator(this, children);
    }

    @Override
    public OrderLine toLeaf() {
        OrderLine result = OrderLine.create();

        result.infoComponent = getInfoComponent().copy();
        result.setCode(null);
        result.setInitDate(getInitDate());
        result.setDeadline(getDeadline());

        result.setWorkHours(0);

        result.directAdvanceAssignments = copyDirectAdvanceAssignments(this,
                result);
        result.materialAssignments = copyMaterialAssignments(this, result);
        result.labels = copyLabels(this, result);
        result.taskQualityForms = copyTaskQualityForms(this, result);

        copyRequirementToOrderElement(result);

        result.initializeTemplate(this.getTemplate());

        result.setExternalCode(getExternalCode());

        return result;
    }

    private static Set<DirectAdvanceAssignment> copyDirectAdvanceAssignments(
            OrderElement origin, OrderElement destination) {
        Set<DirectAdvanceAssignment> result = new HashSet<DirectAdvanceAssignment>();
        for (DirectAdvanceAssignment each : origin.directAdvanceAssignments) {
            result.add(DirectAdvanceAssignment.copy(each, destination));
        }
        return result;
    }

    private static Set<MaterialAssignment> copyMaterialAssignments(
            OrderElement origin, OrderElement destination) {
        Set<MaterialAssignment> result = new HashSet<MaterialAssignment>();
        for (MaterialAssignment each : origin.materialAssignments) {
            result.add(MaterialAssignment.copy(each, destination));
        }
        return result;
    }

    private static Set<Label> copyLabels(
            OrderElement origin, OrderElement destination) {
        Set<Label> result = new HashSet<Label>();
        for (Label each : origin.labels) {
            destination.addLabel(each);
            result.add(each);
        }
        return result;
    }

    private static Set<TaskQualityForm> copyTaskQualityForms(
            OrderElement origin, OrderElement destination) {
        Set<TaskQualityForm> result = new HashSet<TaskQualityForm>();
        for (TaskQualityForm each : origin.taskQualityForms) {
            result.add(TaskQualityForm.copy(each, destination));
        }
        return result;
    }

    @Override
    public OrderLineGroup toContainer() {
        return this;
    }

    @Override
    public void down(OrderElement orderElement) {
        getManipulator().down(orderElement);
    }

    @Override
    public void add(int position, OrderElement orderElement) {
        getManipulator().add(position, orderElement);
        addIndirectAdvanceAssignments(orderElement);
    }

    @Override
    public Integer getWorkHours() {
        int result = 0;
        for (OrderElement orderElement : getChildren()) {
            result += orderElement.getWorkHours();
        }
        return result;
    }

    @Override
    public List<HoursGroup> getHoursGroups() {
        List<HoursGroup> hoursGroups = new ArrayList<HoursGroup>();
        for (OrderElement orderElement : children) {
            hoursGroups.addAll(orderElement.getHoursGroups());
        }
        return hoursGroups;
    }

    public BigDecimal getAdvancePercentage(AdvanceType advanceType, LocalDate date) {
        final DirectAdvanceAssignment directAdvanceAssignment = this.getAdvanceAssignmentByType(advanceType);
        if (directAdvanceAssignment != null) {
            return directAdvanceAssignment.getAdvancePercentage(date);
        }
        return null;
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

        for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirectAdvanceAssignments) {
            if (indirectAdvanceAssignment.getReportGlobalAdvance()) {
                if (indirectAdvanceAssignment.getAdvanceType().getUnitName()
                        .equals(PredefinedAdvancedTypes.CHILDREN.getTypeName())) {
                    if (date == null) {
                        return getAdvancePercentageChildren();
                    }
                    return getAdvancePercentageChildren(date);
                } else {
                    DirectAdvanceAssignment directAdvanceAssignment = calculateFakeDirectAdvanceAssignment(indirectAdvanceAssignment);
                    if (date == null) {
                        return directAdvanceAssignment.getAdvancePercentage();
                    }
                    return directAdvanceAssignment.getAdvancePercentage(date);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getAdvancePercentageChildren() {
        return getAdvancePercentageChildren(null);
    }

    public BigDecimal getAdvancePercentageChildren(LocalDate date) {
        Integer hours = getWorkHours();
        BigDecimal result = new BigDecimal(0);

        if (hours > 0) {
            for (OrderElement orderElement : children) {
                BigDecimal childPercentage;
                if (date == null) {
                    childPercentage = orderElement.getAdvancePercentage();
                } else {
                    childPercentage = orderElement.getAdvancePercentage(date);
                }
                Integer childHours = orderElement.getWorkHours();
                result = result.add(childPercentage.multiply(new BigDecimal(
                        childHours)));
            }
            result = result.divide(new BigDecimal(hours).setScale(2), 4,
                    RoundingMode.DOWN);

        }

        return result;
    }

    @Override
    public DirectAdvanceAssignment calculateFakeDirectAdvanceAssignment(
            IndirectAdvanceAssignment indirectAdvanceAssignment) {
        if (indirectAdvanceAssignment.getAdvanceType().getUnitName().equals(
                PredefinedAdvancedTypes.CHILDREN.getTypeName())) {
            return calculateFakeDirectAdvanceAssignmentChildren(indirectAdvanceAssignment);
        } else {
            Set<DirectAdvanceAssignment> directAdvanceAssignments = getAllDirectAdvanceAssignments(indirectAdvanceAssignment
                    .getAdvanceType());
            return mergeAdvanceAssignments(new ArrayList<DirectAdvanceAssignment>(
                    directAdvanceAssignments));
        }
    }

    private DirectAdvanceAssignment calculateFakeDirectAdvanceAssignmentChildren(
            IndirectAdvanceAssignment indirectAdvanceAssignment) {
        DirectAdvanceAssignment newDirectAdvanceAssignment = DirectAdvanceAssignment
                .create();
        newDirectAdvanceAssignment.setFake(true);
        newDirectAdvanceAssignment.setMaxValue(new BigDecimal(100));
        newDirectAdvanceAssignment.setAdvanceType(indirectAdvanceAssignment
                .getAdvanceType());
        newDirectAdvanceAssignment.setOrderElement(this);

        Set<DirectAdvanceAssignment> directAdvanceAssignments = new HashSet<DirectAdvanceAssignment>();
        for (OrderElement orderElement : children) {
            directAdvanceAssignments.addAll(orderElement
                    .getAllDirectAdvanceAssignmentsReportGlobal());
        }

        List<AdvanceMeasurement> advanceMeasurements = new ArrayList<AdvanceMeasurement>();
        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            advanceMeasurements.addAll(directAdvanceAssignment
                    .getAdvanceMeasurements());
        }

        List<LocalDate> measurementDates = getMeasurementDates(advanceMeasurements);
        SortedSet<AdvanceMeasurement> newAdvanceMeasurements = new TreeSet<AdvanceMeasurement>(
                new AdvanceMeasurementComparator());
        for (LocalDate localDate : measurementDates) {
            BigDecimal value = getAdvancePercentageChildren(localDate)
                    .multiply(new BigDecimal(100));
            AdvanceMeasurement advanceMeasurement = AdvanceMeasurement.create(
                    localDate, value);
            advanceMeasurement.setAdvanceAssignment(newDirectAdvanceAssignment);
            newAdvanceMeasurements.add(advanceMeasurement);
        }
        newDirectAdvanceAssignment
                .setAdvanceMeasurements(newAdvanceMeasurements);

        return newDirectAdvanceAssignment;
    }

    private List<LocalDate> getMeasurementDates(
            List<AdvanceMeasurement> advanceMeasurements) {
        List<LocalDate> result = new ArrayList<LocalDate>();
        for (AdvanceMeasurement advanceMeasurement : advanceMeasurements) {
            LocalDate date = advanceMeasurement.getDate();
            if (result.indexOf(date) < 0) {
                result.add(date);
            }
        }

        Collections.sort(result);

        return result;
    }

    private DirectAdvanceAssignment mergeAdvanceAssignments(
            List<DirectAdvanceAssignment> list) {
        if (list.isEmpty()) {
            return null;
        }

        Iterator<DirectAdvanceAssignment> iterator = list.iterator();
        DirectAdvanceAssignment origAdvanceAssignment = iterator.next();
        DirectAdvanceAssignment directAdvanceAssignment = DirectAdvanceAssignment
                .create();
        directAdvanceAssignment.setFake(true);
        directAdvanceAssignment
                .setMaxValue(origAdvanceAssignment.getMaxValue());
        directAdvanceAssignment
                .setAdvanceType(origAdvanceAssignment.getAdvanceType());
        directAdvanceAssignment.setOrderElement(this);
        directAdvanceAssignment.setAdvanceMeasurements(origAdvanceAssignment
                .getAdvanceMeasurements());

        while (iterator.hasNext()) {
            DirectAdvanceAssignment tempAssignment = iterator.next();
            if (!directAdvanceAssignment.getAdvanceType().getPercentage()) {
                BigDecimal maxValue = tempAssignment.getMaxValue();
                maxValue = maxValue.add(directAdvanceAssignment.getMaxValue());
                directAdvanceAssignment.setMaxValue(maxValue);
            }

            SortedSet<AdvanceMeasurement> advanceMeasurements = new TreeSet<AdvanceMeasurement>(
                    new AdvanceMeasurementComparator());
            advanceMeasurements.addAll(mergeAdvanceMeasurements(
                    directAdvanceAssignment, new ArrayList<AdvanceMeasurement>(
                            directAdvanceAssignment.getAdvanceMeasurements()),
                    new ArrayList<AdvanceMeasurement>(tempAssignment
                            .getAdvanceMeasurements())));
            directAdvanceAssignment.setAdvanceMeasurements(advanceMeasurements);
        }

        return directAdvanceAssignment;
    }

    private List<AdvanceMeasurement> mergeAdvanceMeasurements(
            AdvanceAssignment advanceAssignment, List<AdvanceMeasurement> one,
            List<AdvanceMeasurement> other) {
        Collections.reverse(one);
        Collections.reverse(other);

        ArrayList<AdvanceMeasurement> list = new ArrayList<AdvanceMeasurement>();
        mergeAdvanceMeasurements(advanceAssignment, one, other, list);

        return list;
    }

    private void mergeAdvanceMeasurements(AdvanceAssignment advanceAssignment,
            List<AdvanceMeasurement> list1, List<AdvanceMeasurement> list2,
            List<AdvanceMeasurement> result) {

        Iterator<AdvanceMeasurement> iterator1 = list1.iterator();
        Iterator<AdvanceMeasurement> iterator2 = list2.iterator();

        AdvanceMeasurement next1;
        if (iterator1.hasNext()) {
            next1 = iterator1.next();
        } else {
            next1 = null;
        }
        AdvanceMeasurement next2;
        if (iterator2.hasNext()) {
            next2 = iterator2.next();
        } else {
            next2 = null;
        }

        BigDecimal previous1 = BigDecimal.ZERO;
        BigDecimal previous2 = BigDecimal.ZERO;
        BigDecimal previousResult = BigDecimal.ZERO;

        LocalDate date;
        BigDecimal add;
        Date communicationDate;

        BigDecimal totalHours = null;
        if (advanceAssignment.getAdvanceType().getPercentage()) {
            totalHours = new BigDecimal(advanceAssignment.getOrderElement()
                    .getWorkHours());
        }

        while ((next1 != null) && (next2 != null)) {
            if (next1.getDate().compareTo(next2.getDate()) < 0) {
                date = next1.getDate();
                add = next1.getValue().subtract(previous1);
                communicationDate = next1.getCommunicationDate();
                previous1 = next1.getValue();

                if (advanceAssignment.getAdvanceType().getPercentage()) {
                    BigDecimal orderElementHours = new BigDecimal(next1
                            .getAdvanceAssignment().getOrderElement()
                            .getWorkHours());
                    add = addMeasure(add, totalHours, orderElementHours);
                }

                if (iterator1.hasNext()) {
                    next1 = iterator1.next();
                } else {
                    next1 = null;
                }
            } else if (next1.getDate().compareTo(next2.getDate()) > 0) {
                date = next2.getDate();
                add = next2.getValue().subtract(previous2);
                communicationDate = next2.getCommunicationDate();
                previous2 = next2.getValue();

                if (advanceAssignment.getAdvanceType().getPercentage()) {
                    BigDecimal orderElementHours = new BigDecimal(next2
                            .getAdvanceAssignment().getOrderElement()
                            .getWorkHours());
                    add = addMeasure(add, totalHours, orderElementHours);
                }

                if (iterator2.hasNext()) {
                    next2 = iterator2.next();
                } else {
                    next2 = null;
                }
            } else {
                date = next1.getDate();
                BigDecimal add1 = next1.getValue().subtract(previous1);
                BigDecimal add2 = next2.getValue().subtract(previous2);
                add = add1.add(add2);
                communicationDate = getGreatestDate(next1
                        .getCommunicationDate(), next2.getCommunicationDate());
                previous1 = next1.getValue();
                previous2 = next2.getValue();

                if (advanceAssignment.getAdvanceType().getPercentage()) {
                    BigDecimal orderElementHours1 = new BigDecimal(next1
                            .getAdvanceAssignment().getOrderElement()
                            .getWorkHours());
                    add1 = addMeasure(add1, totalHours, orderElementHours1);

                    BigDecimal orderElementHours2 = new BigDecimal(next2
                            .getAdvanceAssignment().getOrderElement()
                            .getWorkHours());
                    add2 = addMeasure(add2, totalHours, orderElementHours2);
                    add = add1.add(add2);
                }

                if (iterator1.hasNext()) {
                    next1 = iterator1.next();
                } else {
                    next1 = null;
                }
                if (iterator2.hasNext()) {
                    next2 = iterator2.next();
                } else {
                    next2 = null;
                }
            }

            AdvanceMeasurement advanceMeasurement = AdvanceMeasurement.create();
            advanceMeasurement.setAdvanceAssignment(advanceAssignment);
            advanceMeasurement.setDate(date);
            previousResult = previousResult.add(add);
            checkAndSetValue(advanceMeasurement, previousResult);
            advanceMeasurement.setCommunicationDate(communicationDate);
            result.add(advanceMeasurement);
        }

        while (next1 != null) {
            date = next1.getDate();
            add = next1.getValue().subtract(previous1);
            communicationDate = next1.getCommunicationDate();
            previous1 = next1.getValue();

            if (advanceAssignment.getAdvanceType().getPercentage()) {
                BigDecimal orderElementHours = new BigDecimal(next1
                        .getAdvanceAssignment().getOrderElement()
                        .getWorkHours());
                add = addMeasure(add, totalHours, orderElementHours);
            }

            if (iterator1.hasNext()) {
                next1 = iterator1.next();
            } else {
                next1 = null;
            }

            AdvanceMeasurement advanceMeasurement = AdvanceMeasurement.create();
            advanceMeasurement.setAdvanceAssignment(advanceAssignment);
            advanceMeasurement.setDate(date);
            checkAndSetValue(advanceMeasurement, previousResult.add(add));
            previousResult = advanceMeasurement.getValue();
            advanceMeasurement.setCommunicationDate(communicationDate);
            result.add(advanceMeasurement);
        }

        while (next2 != null) {
            date = next2.getDate();
            add = next2.getValue().subtract(previous2);
            communicationDate = next2.getCommunicationDate();
            previous2 = next2.getValue();

            if (advanceAssignment.getAdvanceType().getPercentage()) {
                BigDecimal orderElementHours = new BigDecimal(next2
                        .getAdvanceAssignment().getOrderElement()
                        .getWorkHours());
                add = addMeasure(add, totalHours, orderElementHours);
            }

            if (iterator2.hasNext()) {
                next2 = iterator2.next();
            } else {
                next2 = null;
            }

            AdvanceMeasurement advanceMeasurement = AdvanceMeasurement.create();
            advanceMeasurement.setAdvanceAssignment(advanceAssignment);
            advanceMeasurement.setDate(date);
            checkAndSetValue(advanceMeasurement, previousResult.add(add));
            previousResult = advanceMeasurement.getValue();
            advanceMeasurement.setCommunicationDate(communicationDate);
            result.add(advanceMeasurement);
        }

    }

    private void checkAndSetValue(AdvanceMeasurement advanceMeasurement,
            BigDecimal previousResult) {
        advanceMeasurement.setValue(previousResult);
        boolean checkPrecision = advanceMeasurement
                .checkConstraintValidPrecision();
        if (!checkPrecision) {
            AdvanceAssignment advanceAssignment = advanceMeasurement
                    .getAdvanceAssignment();
            if ((previousResult == null) || (advanceAssignment == null)
                    || (advanceAssignment.getAdvanceType() == null)) {
                return;
            }

            BigDecimal precision = advanceAssignment.getAdvanceType()
                    .getUnitPrecision();
            BigDecimal result[] = previousResult.divideAndRemainder(precision);
            BigDecimal checkResult;
            if (previousResult.compareTo(result[1]) >= 0) {
                checkResult = previousResult.subtract(result[1]);
            } else {
                checkResult = previousResult.add(result[1]);
            }
            advanceMeasurement.setValue(checkResult);
        }
    }

    private BigDecimal addMeasure(BigDecimal add, BigDecimal totalHours,
            BigDecimal orderElementHours) {
        if ((totalHours != null) && (totalHours.compareTo(BigDecimal.ZERO) > 0)) {
            add = add.multiply(orderElementHours).divide(totalHours,
                    RoundingMode.DOWN);
        } else {
            add = add.multiply(orderElementHours);
        }
        return add;
    }

    private Date getGreatestDate(Date communicationDate, Date communicationDate2) {
        if ((communicationDate == null) || (communicationDate2 == null)) {
            return null;
        }
        return (communicationDate.compareTo(communicationDate2) >= 0) ? communicationDate
                : communicationDate2;
    }

    @Override
    public Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments(
            AdvanceType advanceType) {
        Set<DirectAdvanceAssignment> result = new HashSet<DirectAdvanceAssignment>();

        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if (directAdvanceAssignment.getAdvanceType().getUnitName().equals(advanceType.getUnitName())) {
                result.add(directAdvanceAssignment);
                return result;
            }
        }

        for (OrderElement orderElement : children) {
             result
                    .addAll(orderElement
                            .getAllDirectAdvanceAssignments(advanceType));
        }

        return result;
    }

    @Override
    public Set<IndirectAdvanceAssignment> getAllIndirectAdvanceAssignments(
            AdvanceType advanceType) {
        Set<IndirectAdvanceAssignment> result = new HashSet<IndirectAdvanceAssignment>();

        IndirectAdvanceAssignment indirectAdvanceAssignment = getIndirectAdvanceAssignment(advanceType);
        if(indirectAdvanceAssignment != null){
            result.add(indirectAdvanceAssignment);
        }

        for (OrderElement orderElement : children) {
            result.addAll(orderElement
                    .getAllIndirectAdvanceAssignments(advanceType));
        }

        return result;
    }

    public IndirectAdvanceAssignment getIndirectAdvanceAssignment(
            AdvanceType advanceType) {
        for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirectAdvanceAssignments) {
            if (indirectAdvanceAssignment.getAdvanceType().getUnitName()
                    .equals(advanceType.getUnitName())) {
                return indirectAdvanceAssignment;
            }
        }
        return null;
    }

    @Override
    protected Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignmentsReportGlobal() {
        Set<DirectAdvanceAssignment> result = new HashSet<DirectAdvanceAssignment>();

        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if (directAdvanceAssignment.getReportGlobalAdvance()) {
                result.add(directAdvanceAssignment);
            }
        }

        for (OrderElement orderElement : children) {
            result.addAll(orderElement
                    .getAllDirectAdvanceAssignmentsReportGlobal());
        }

        return result;
    }

    @Override
    protected Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments() {
        Set<DirectAdvanceAssignment> result = new HashSet<DirectAdvanceAssignment>();

        result.addAll(directAdvanceAssignments);

        for (OrderElement orderElement : children) {
            result.addAll(orderElement.getAllDirectAdvanceAssignments());
        }

        return result;
    }

    @Override
    public Set<IndirectAdvanceAssignment> getIndirectAdvanceAssignments() {
        return Collections.unmodifiableSet(indirectAdvanceAssignments);
    }

    public void addIndirectAdvanceAssignment(
            IndirectAdvanceAssignment indirectAdvanceAssignment) {
        if ((!existsIndirectAdvanceAssignmentWithTheSameType(indirectAdvanceAssignment
                .getAdvanceType()))
                && (!existsDirectAdvanceAssignmentWithTheSameType(indirectAdvanceAssignment
                        .getAdvanceType()))) {
            indirectAdvanceAssignments.add(indirectAdvanceAssignment);
        }
        if (parent != null) {
            parent.addIndirectAdvanceAssignment(indirectAdvanceAssignment
                    .createIndirectAdvanceFor(parent));
        }
    }

    public void removeIndirectAdvanceAssignment(AdvanceType advanceType) {
        DirectAdvanceAssignment tempAdavanceAssignmet = DirectAdvanceAssignment
                .create();
        tempAdavanceAssignmet.setAdvanceType(advanceType);

        try {
            checkChildrenNoOtherAssignmentWithSameAdvanceType(this,
                    tempAdavanceAssignmet);

            String unitName = advanceType.getUnitName();
            IndirectAdvanceAssignment toRemove = null;
            for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirectAdvanceAssignments) {
                if (unitName.equals(indirectAdvanceAssignment.getAdvanceType()
                        .getUnitName())) {
                    toRemove = indirectAdvanceAssignment;
                }
            }
            if (toRemove != null) {
                indirectAdvanceAssignments.remove(toRemove);
            }

            if (parent != null) {
                parent.removeIndirectAdvanceAssignment(advanceType);
            }
        } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
            // If exist another DirectAdvanceAssignment with the same
            // AdvanceType in some children, the IndirectAdvanceAssignment
            // should persist
        }
    }

    public boolean existsIndirectAdvanceAssignmentWithTheSameType(
            AdvanceType type) {
        String unitName = type.getUnitName();
        for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirectAdvanceAssignments) {
            if (unitName.equals(indirectAdvanceAssignment.getAdvanceType()
                    .getUnitName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void checkNoOtherGlobalAdvanceAssignment(
            DirectAdvanceAssignment newAdvanceAssignment)
            throws DuplicateValueTrueReportGlobalAdvanceException {
        if (!newAdvanceAssignment.getReportGlobalAdvance()) {
            return;
        }
        Set<AdvanceAssignment> advanceAssignments = new HashSet<AdvanceAssignment>();
        advanceAssignments.addAll(directAdvanceAssignments);
        advanceAssignments.addAll(indirectAdvanceAssignments);
        for (AdvanceAssignment advanceAssignment : advanceAssignments) {
            if (advanceAssignment.getReportGlobalAdvance()) {
                throw new DuplicateValueTrueReportGlobalAdvanceException(
                        _("Cannot spread two progress in the same task"),
                        this, OrderElement.class);
            }
        }
    }

    protected void copyRequirementToOrderElement(OrderLine leaf) {
        criterionRequirementHandler.copyRequirementToOrderLine(this, leaf);
    }

    @Override
    public DirectAdvanceAssignment getReportGlobalAdvanceAssignment() {
        for (DirectAdvanceAssignment directAdvanceAssignment : getDirectAdvanceAssignments()) {
            if (directAdvanceAssignment.getReportGlobalAdvance()) {
                return directAdvanceAssignment;
            }
        }

        for (IndirectAdvanceAssignment indirectAdvanceAssignment : getIndirectAdvanceAssignments()) {
            if (indirectAdvanceAssignment.getReportGlobalAdvance()) {
                return calculateFakeDirectAdvanceAssignment(indirectAdvanceAssignment);
            }
        }
        return null;
    }

    public DirectAdvanceAssignment getAdvanceAssignmentByType(AdvanceType type) {
        DirectAdvanceAssignment result = getDirectAdvanceAssignmentByType(type);
        if (result != null) {
            return result;
        }

        for (IndirectAdvanceAssignment each : getIndirectAdvanceAssignments()) {
            if (type != null && each.getAdvanceType().getId().equals(type.getId())) {
                return calculateFakeDirectAdvanceAssignment(each);
            }
        }
        return null;
    }

    @Override
    public OrderElementTemplate createTemplate() {
        return OrderLineGroupTemplate.create(this);
    }

    @Override
    public OrderLineGroup getThis() {
        return this;
    }

    @Override
    public OrderLine calculateOrderLineForSubcontract() {
        OrderLine orderLine = OrderLine.create();

        orderLine.setCode(getCode());
        orderLine.setName(getName());
        orderLine.setDescription(getDescription());

        orderLine.setInitDate(getInitDate());
        orderLine.setDeadline(getDeadline());

        // HoursGroups from all its child nodes
        for (HoursGroup hoursGroup : getHoursGroups()) {
            orderLine.addHoursGroup(hoursGroup);
        }

        // CriterionRequirements from this node
        orderLine.setCriterionRequirements(getCriterionRequirements());

        // Labels from this node
        orderLine.setLabels(getLabels());

        // MaterialAssignments from this node and all its child nodes
        for (MaterialAssignment materialAssignment : getMaterialAssignments()) {
            orderLine.addMaterialAssignment(materialAssignment);
        }

        return orderLine;
    }

    public OrderVersion getCurrentOrderVersion() {
        return getCurrentSchedulingData().getOriginOrderVersion();
    }

    public OrderElement findRepeatedOrderCode() {
        Set<String> codes = new HashSet<String>();
        codes.add(getCode());

        for (OrderElement each : getAllOrderElements()) {
            String code = each.getCode();
            if (code != null && !code.isEmpty()) {
                if (codes.contains(code)) {
                    return each;
                }
                codes.add(code);
            }
        }

        return null;
    }

    public HoursGroup findRepeatedHoursGroupCode() {
        Set<String> codes = new HashSet<String>();

        for (HoursGroup hoursGroup : getHoursGroups()) {
            String code = hoursGroup.getCode();
            if (code != null && !code.isEmpty()) {
                if (codes.contains(code)) {
                    return hoursGroup;
                }
                codes.add(code);
            }
        }

        return null;
    }

    public List<OrderElement> getAllOrderElements() {
        List<OrderElement> result = new ArrayList<OrderElement>(
                this.getChildren());
        for (OrderElement orderElement : this.getChildren()) {
            result.addAll(orderElement.getAllChildren());
        }
        return result;
    }

    @AssertTrue(message = "indirect advance assignments should have different types")
    public boolean checkConstraintIndirectAdvanceAssignmentsWithDifferentType() {
        Set<String> types = new HashSet<String>();
        for (IndirectAdvanceAssignment each : indirectAdvanceAssignments) {
            String type = each.getAdvanceType().getUnitName();
            if (types.contains(type)) {
                return false;
            }
            types.add(type);
        }
        return true;
    }

}
