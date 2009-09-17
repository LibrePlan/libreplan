package org.navalplanner.business.orders.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.navalplanner.business.advance.entities.AdvanceAssigment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceMeasurementComparator;
import org.navalplanner.business.advance.entities.AdvanceAssigment.Type;

public class OrderLineGroup extends OrderElement implements IOrderLineGroup {

    public static OrderLineGroup create() {
        OrderLineGroup result = new OrderLineGroup();
        result.setNewObject(true);
        return result;
    }

    private List<OrderElement> children = new ArrayList<OrderElement>();

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
    }

    @Override
    public void replace(OrderElement oldOrderElement, OrderElement orderElement) {
        getManipulator().replace(oldOrderElement, orderElement);
    }

    @Override
    public void add(OrderElement orderElement) {
        getManipulator().add(orderElement);
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
    public BigDecimal getAdvancePercentage() {
        Integer hours = getWorkHours();
        BigDecimal temp = new BigDecimal(0);

        if (hours > 0) {
            for (OrderElement orderElement : children) {
                BigDecimal childPercentage = orderElement
                        .getAdvancePercentage();
                Integer childHours = orderElement.getWorkHours();
                temp = temp.add(childPercentage.multiply(new BigDecimal(
                        childHours)));
            }

            temp = temp.divide(new BigDecimal(hours));
        }

        Set<AdvanceAssigment> advanceAssigments = this.advanceAssigments;
        if (!advanceAssigments.isEmpty()) {
            for (AdvanceAssigment advanceAssigment : advanceAssigments) {
                BigDecimal percentage = advanceAssigment.getLastPercentage();
                temp = temp.add(percentage);
            }

            Integer number = advanceAssigments.size() + 1;
            temp = temp.divide(new BigDecimal(number));
        }

        return temp.setScale(2);
    }

    @Override
    public Set<AdvanceAssigment> getAdvanceAssigments() {
        Set<AdvanceAssigment> assigments = new HashSet<AdvanceAssigment>();

        for (OrderElement child : children) {
            assigments.addAll(child.getAdvanceAssigmentsWithoutMerge());
        }

        Map<String, List<AdvanceAssigment>> map = classifyByAdvanceType(assigments);

        Set<AdvanceAssigment> result = new HashSet<AdvanceAssigment>();
        result.addAll(this.advanceAssigments);

        for (String advanceType : map.keySet()) {
            result.add(mergeAdvanceAssigments(map.get(advanceType)));
        }

        return result;
    }

    private AdvanceAssigment mergeAdvanceAssigments(List<AdvanceAssigment> list) {
        if (list.isEmpty()) {
            return null;
        }

        Iterator<AdvanceAssigment> iterator = list.iterator();
        AdvanceAssigment origAdvanceAssigment = iterator.next();
        AdvanceAssigment advanceAssigment = AdvanceAssigment.create();
        advanceAssigment.setMaxValue(origAdvanceAssigment.getMaxValue());
        advanceAssigment.setAdvanceType(origAdvanceAssigment.getAdvanceType());
        advanceAssigment
                .setOrderElement(origAdvanceAssigment.getOrderElement());
        advanceAssigment.setAdvanceMeasurements(origAdvanceAssigment
                .getAdvanceMeasurements());

        advanceAssigment.setType(Type.CALCULATED);

        while (iterator.hasNext()) {
            AdvanceAssigment tempAssigment = iterator.next();
            BigDecimal maxValue = tempAssigment.getMaxValue();
            maxValue = maxValue.add(advanceAssigment.getMaxValue());
            advanceAssigment.setMaxValue(maxValue);

            SortedSet<AdvanceMeasurement> advanceMeasurements = new TreeSet<AdvanceMeasurement>(
                    new AdvanceMeasurementComparator());
            advanceMeasurements.addAll(mergeAdvanceMeasurements(
                    advanceAssigment,
                    new ArrayList<AdvanceMeasurement>(advanceAssigment
                            .getAdvanceMeasurements()),
                    new ArrayList<AdvanceMeasurement>(tempAssigment
                            .getAdvanceMeasurements())));
            advanceAssigment.setAdvanceMeasurements(advanceMeasurements);
        }

        return advanceAssigment;
    }

    private List<AdvanceMeasurement> mergeAdvanceMeasurements(
            AdvanceAssigment advanceAssigment, List<AdvanceMeasurement> one,
            List<AdvanceMeasurement> other) {
        Collections.reverse(one);
        Collections.reverse(other);

        ArrayList<AdvanceMeasurement> list = new ArrayList<AdvanceMeasurement>();
        mergeAdvanceMeasurements(advanceAssigment, one, other, list);

        return list;
    }

    private void mergeAdvanceMeasurements(
            AdvanceAssigment advanceAssigment,
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

        while ((next1 != null) && (next2 != null)) {
            if (next1.getDate().compareTo(next2.getDate()) < 0) {
                date = next1.getDate();
                add = next1.getValue().subtract(previous1);
                previous1 = next1.getValue();

                if (iterator1.hasNext()) {
                    next1 = iterator1.next();
                } else {
                    next1 = null;
                }
            } else if (next1.getDate().compareTo(next2.getDate()) > 0) {
                date = next2.getDate();
                add = next2.getValue().subtract(previous2);
                previous2 = next2.getValue();

                if (iterator2.hasNext()) {
                    next2 = iterator2.next();
                } else {
                    next2 = null;
                }
            } else {
                date = next1.getDate();
                add = next1.getValue().subtract(previous1).add(
                        next2.getValue().subtract(previous2));
                previous1 = next1.getValue();
                previous2 = next2.getValue();

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
            advanceMeasurement.setAdvanceAssigment(advanceAssigment);
            advanceMeasurement.setDate(date);
            advanceMeasurement.setValue(previousResult.add(add));
            previousResult = advanceMeasurement.getValue();
            result.add(advanceMeasurement);
        }

        while (next1 != null) {
            date = next1.getDate();
            add = next1.getValue().subtract(previous1);
            previous1 = next1.getValue();

            if (iterator2.hasNext()) {
                next1 = iterator1.next();
            } else {
                next1 = null;
            }

            AdvanceMeasurement advanceMeasurement = AdvanceMeasurement.create();
            advanceMeasurement.setAdvanceAssigment(advanceAssigment);
            advanceMeasurement.setDate(date);
            advanceMeasurement.setValue(previousResult.add(add));
            previousResult = advanceMeasurement.getValue();
            result.add(advanceMeasurement);
        }

        while (next2 != null) {
            date = next2.getDate();
            add = next2.getValue().subtract(previous2);
            previous2 = next2.getValue();

            if (iterator2.hasNext()) {
                next2 = iterator2.next();
            } else {
                next2 = null;
            }

            AdvanceMeasurement advanceMeasurement = AdvanceMeasurement.create();
            advanceMeasurement.setAdvanceAssigment(advanceAssigment);
            advanceMeasurement.setDate(date);
            advanceMeasurement.setValue(previousResult.add(add));
            previousResult = advanceMeasurement.getValue();
            result.add(advanceMeasurement);
        }

    }

    private Map<String, List<AdvanceAssigment>> classifyByAdvanceType(
            Set<AdvanceAssigment> advanceAssigments) {
        Map<String, List<AdvanceAssigment>> map = new HashMap<String, List<AdvanceAssigment>>();

        for (AdvanceAssigment advanceAssigment : advanceAssigments) {
            List<AdvanceAssigment> list = map.get(advanceAssigment
                    .getAdvanceType().getUnitName());
            if (list == null) {
                list = new ArrayList<AdvanceAssigment>();
            }
            list.add(advanceAssigment);

            map.put(advanceAssigment.getAdvanceType().getUnitName(), list);
        }

        return map;
    }

}
