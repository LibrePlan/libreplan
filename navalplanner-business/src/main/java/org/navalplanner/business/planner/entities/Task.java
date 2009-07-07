package org.navalplanner.business.planner.entities;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.orders.entities.HoursGroup;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Task extends TaskElement {

    public static Task createTask(HoursGroup hoursGroup) {
        return new Task(hoursGroup);
    }

    private HoursGroup hoursGroup;

    /**
     * For hibernate, DO NOT USE
     */
    public Task() {

    }

    private Task(HoursGroup hoursGroup) {
        Validate.notNull(hoursGroup);
        this.hoursGroup = hoursGroup;
    }

    public HoursGroup getHoursGroup() {
        return this.hoursGroup;
    }

    public Integer getHours() {
        return hoursGroup.getWorkingHours();
    }

}
