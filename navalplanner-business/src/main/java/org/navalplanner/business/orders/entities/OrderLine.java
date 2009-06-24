package org.navalplanner.business.orders.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderLine extends OrderElement {

    private Boolean fixedHours = false;

    private Set<HoursGroup> hoursGroups = new HashSet<HoursGroup>();

    @Override
    public Integer getWorkHours() {
        int result = 0;
        List<HoursGroup> hoursGroups = getHoursGroups();
        for (HoursGroup hoursGroup : hoursGroups) {
            Integer workingHours = hoursGroup.getWorkingHours();
            if (workingHours != null) {
                result += workingHours;
            }
        }
        return result;
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
    public OrderLineGroup asContainer() {
        OrderLineGroup result = new OrderLineGroup();
        result.setName(getName());
        result.setInitDate(getInitDate());
        result.setEndDate(getEndDate());
        // FIXME
        // result.setHoursGroups(getHoursGroups());
        return result;
    }

    public void setWorkHours(Integer workingHours) {
        List<HoursGroup> hoursGroups = getHoursGroups();

        // FIXME For the moment we have just one HoursGroup for each OrderLine
        if (hoursGroups.isEmpty()) {
            HoursGroup hourGroup = new HoursGroup();
            hourGroup.setWorkingHours(workingHours);

            hoursGroups.add(hourGroup);
        } else {
            HoursGroup hourGroup = hoursGroups.get(0);
            hourGroup.setWorkingHours(workingHours);
        }

        setHoursGroups(hoursGroups);
    }

    public void setHoursGroups(List<HoursGroup> hoursGroups) {
        this.hoursGroups = new HashSet<HoursGroup>(hoursGroups);
    }

    public void addHoursGroup(HoursGroup hoursGroup) {
        hoursGroups.add(hoursGroup);
    }

    public void deleteHoursGroup(HoursGroup hoursGroup) {
        hoursGroups.remove(hoursGroup);
    }

    @Override
    public List<HoursGroup> getHoursGroups() {
        return new ArrayList<HoursGroup>(hoursGroups);
    }

    @Override
    public void forceLoadHourGroups() {
        for (HoursGroup hoursGroup : hoursGroups) {
            hoursGroup.getWorkingHours();
        }
    }

    public void setFixedHours(Boolean fixedHours) {
        this.fixedHours = fixedHours;
    }

    public Boolean isFixedHours() {
        return fixedHours;
    }

}
