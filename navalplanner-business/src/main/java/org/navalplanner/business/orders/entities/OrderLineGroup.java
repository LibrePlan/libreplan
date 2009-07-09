package org.navalplanner.business.orders.entities;

import java.util.ArrayList;
import java.util.List;

public class OrderLineGroup extends OrderElement implements IOrderLineGroup {

    private List<OrderElement> children = new ArrayList<OrderElement>();

    @Override
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
        return new OrderLineGroupManipulator(children);
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
    public void forceLoadHourGroups() {
        for (OrderElement orderElement : children) {
            orderElement.forceLoadHourGroups();
        }
    }

    @Override
    public void forceLoadHourGroupsCriterions() {
        for (OrderElement orderElement : children) {
            orderElement.forceLoadHourGroupsCriterions();
        }
    }

}
