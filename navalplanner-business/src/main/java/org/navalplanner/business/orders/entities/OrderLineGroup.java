package org.navalplanner.business.orders.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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

public class OrderLineGroup extends OrderElement implements IOrderLineGroup {

    public static OrderLineGroup create() {
        OrderLineGroup result = new OrderLineGroup();
        result.setNewObject(true);

        IndirectAdvanceAssignment indirectAdvanceAssignment = IndirectAdvanceAssignment
                .create(true);
        AdvanceType advanceType = PredefinedAdvancedTypes.CHILDREN.getType();
        indirectAdvanceAssignment
                .setAdvanceType(advanceType);
        indirectAdvanceAssignment.setOrderElement(result);
        result.addIndirectAdvanceAssignment(indirectAdvanceAssignment);

        return result;
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
        for (DirectAdvanceAssignment directAdvanceAssignment : orderElement.directAdvanceAssignments) {
            IndirectAdvanceAssignment indirectAdvanceAssignment = IndirectAdvanceAssignment
                    .create();
            indirectAdvanceAssignment.setAdvanceType(directAdvanceAssignment
                    .getAdvanceType());
            indirectAdvanceAssignment.setOrderElement(this);
            this.addIndirectAdvanceAssignment(indirectAdvanceAssignment);
        }

        if (orderElement instanceof OrderLineGroup) {
            for (IndirectAdvanceAssignment indirectAdvanceAssignment : ((OrderLineGroup) orderElement)
                    .getIndirectAdvanceAssignments()) {
                this.addIndirectAdvanceAssignment(indirectAdvanceAssignment);
            }
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
    }

    @Override
    public void up(OrderElement orderElement) {
        getManipulator().up(orderElement);
    }

    private OrderLineGroupManipulator getManipulator() {
        return OrderLineGroupManipulator.createManipulatorForOrderLineGroup(
                this, children);
    }

    @Override
    public OrderLine toLeaf() {
        OrderLine result = OrderLine.create();

        result.setName(getName());
        result.setInitDate(getInitDate());
        result.setEndDate(getEndDate());
        result.setWorkHours(0);
        result.directAdvanceAssignments = new HashSet<DirectAdvanceAssignment>(
                this.directAdvanceAssignments);

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
        children.add(position, orderElement);

        addIndirectAdvanceAssignments(orderElement);
    }

    @Override
    public Integer getWorkHours() {
        int result = 0;
        List<OrderElement> children = getChildren();
        for (OrderElement orderElement : children) {
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

    @Override
    protected BigDecimal getAdvancePercentage(LocalDate date) {
        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if (directAdvanceAssignment.getReportGlobalAdvance()) {
                return directAdvanceAssignment.getAdvancePercentage(date);
            }
        }

        for (IndirectAdvanceAssignment indirectAdvanceAssignment : indirectAdvanceAssignments) {
            if (indirectAdvanceAssignment.getReportGlobalAdvance()) {
                if (indirectAdvanceAssignment.getAdvanceType().getUnitName()
                        .equals(PredefinedAdvancedTypes.CHILDREN.getTypeName())) {
                    return getAdvancePercentageChildren(date);
                } else {
                    DirectAdvanceAssignment directAdvanceAssignment = calculateFakeDirectAdvanceAssignment(indirectAdvanceAssignment);
                    return directAdvanceAssignment.getAdvancePercentage(date);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    public BigDecimal getAdvancePercentageChildren() {
        return getAdvancePercentageChildren(new LocalDate());
    }

    protected BigDecimal getAdvancePercentageChildren(LocalDate date) {
        Integer hours = getWorkHours();
        BigDecimal result = new BigDecimal(0);

        if (hours > 0) {
            for (OrderElement orderElement : children) {
                BigDecimal childPercentage = orderElement
                        .getAdvancePercentage(date);
                Integer childHours = orderElement.getWorkHours();
                result = result.add(childPercentage.multiply(new BigDecimal(
                        childHours)));
            }

            result = result.setScale(2).divide(new BigDecimal(hours),
                    RoundingMode.DOWN);
        }

        return result;
    }

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

        BigDecimal previous1 = new BigDecimal(0);
        BigDecimal previous2 = new BigDecimal(0);
        BigDecimal previousResult = new BigDecimal(0);

        LocalDate date;
        BigDecimal add;

        BigDecimal totalHours = null;
        if (advanceAssignment.getAdvanceType().getPercentage()) {
            totalHours = new BigDecimal(advanceAssignment.getOrderElement()
                    .getWorkHours());
        }

        while ((next1 != null) && (next2 != null)) {
            if (next1.getDate().compareTo(next2.getDate()) < 0) {
                date = next1.getDate();
                add = next1.getValue().subtract(previous1);
                previous1 = next1.getValue();

                if (advanceAssignment.getAdvanceType().getPercentage()) {
                    BigDecimal orderElementHours = new BigDecimal(next1
                            .getAdvanceAssignment().getOrderElement()
                            .getWorkHours());
                    add = add.multiply(orderElementHours).divide(totalHours,
                            RoundingMode.DOWN);
                }

                if (iterator1.hasNext()) {
                    next1 = iterator1.next();
                } else {
                    next1 = null;
                }
            } else if (next1.getDate().compareTo(next2.getDate()) > 0) {
                date = next2.getDate();
                add = next2.getValue().subtract(previous2);
                previous2 = next2.getValue();

                if (advanceAssignment.getAdvanceType().getPercentage()) {
                    BigDecimal orderElementHours = new BigDecimal(next2
                            .getAdvanceAssignment().getOrderElement()
                            .getWorkHours());
                    add = add.multiply(orderElementHours).divide(totalHours,
                            RoundingMode.DOWN);
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
                previous1 = next1.getValue();
                previous2 = next2.getValue();

                if (advanceAssignment.getAdvanceType().getPercentage()) {
                    BigDecimal orderElementHours1 = new BigDecimal(next1
                            .getAdvanceAssignment().getOrderElement()
                            .getWorkHours());
                    add1 = add1.multiply(orderElementHours1).divide(totalHours,
                            RoundingMode.DOWN);

                    BigDecimal orderElementHours2 = new BigDecimal(next2
                            .getAdvanceAssignment().getOrderElement()
                            .getWorkHours());
                    add2 = add2.multiply(orderElementHours2).divide(totalHours,
                            RoundingMode.DOWN);

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
            advanceMeasurement.setValue(previousResult);
            result.add(advanceMeasurement);
        }

        while (next1 != null) {
            date = next1.getDate();
            add = next1.getValue().subtract(previous1);
            previous1 = next1.getValue();

            if (advanceAssignment.getAdvanceType().getPercentage()) {
                BigDecimal orderElementHours = new BigDecimal(next1
                        .getAdvanceAssignment().getOrderElement()
                        .getWorkHours());
                add = add.multiply(orderElementHours).divide(totalHours,
                        RoundingMode.DOWN);
            }

            if (iterator1.hasNext()) {
                next1 = iterator1.next();
            } else {
                next1 = null;
            }

            AdvanceMeasurement advanceMeasurement = AdvanceMeasurement.create();
            advanceMeasurement.setAdvanceAssignment(advanceAssignment);
            advanceMeasurement.setDate(date);
            advanceMeasurement.setValue(previousResult.add(add));
            previousResult = advanceMeasurement.getValue();
            result.add(advanceMeasurement);
        }

        while (next2 != null) {
            date = next2.getDate();
            add = next2.getValue().subtract(previous2);
            previous2 = next2.getValue();

            if (advanceAssignment.getAdvanceType().getPercentage()) {
                BigDecimal orderElementHours = new BigDecimal(next2
                        .getAdvanceAssignment().getOrderElement()
                        .getWorkHours());
                add = add.multiply(orderElementHours).divide(totalHours,
                        RoundingMode.DOWN);
            }

            if (iterator2.hasNext()) {
                next2 = iterator2.next();
            } else {
                next2 = null;
            }

            AdvanceMeasurement advanceMeasurement = AdvanceMeasurement.create();
            advanceMeasurement.setAdvanceAssignment(advanceAssignment);
            advanceMeasurement.setDate(date);
            advanceMeasurement.setValue(previousResult.add(add));
            previousResult = advanceMeasurement.getValue();
            result.add(advanceMeasurement);
        }

    }

    @Override
    protected Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments(
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

    public Set<IndirectAdvanceAssignment> getIndirectAdvanceAssignments() {
        return Collections.unmodifiableSet(indirectAdvanceAssignments);
    }

    public void addIndirectAdvanceAssignment(
            IndirectAdvanceAssignment indirectAdvanceAssignment) {
        if (!existsIndirectAdvanceAssignmentWithTheSameType(indirectAdvanceAssignment)) {
            indirectAdvanceAssignments.add(indirectAdvanceAssignment);
        }
        if (parent != null) {
            parent.addIndirectAdvanceAssignment(indirectAdvanceAssignment);
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

    private boolean existsIndirectAdvanceAssignmentWithTheSameType(
            IndirectAdvanceAssignment newIndirectAdvanceAssignment) {
        String unitName = newIndirectAdvanceAssignment.getAdvanceType()
                .getUnitName();
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
                        "Duplicate Value True ReportGlobalAdvance For Order Element",
                        this, OrderElement.class);
            }
        }
    }
}
