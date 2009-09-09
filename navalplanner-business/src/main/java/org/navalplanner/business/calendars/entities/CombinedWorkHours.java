package org.navalplanner.business.calendars.entities;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;

public abstract class CombinedWorkHours implements IWorkHours {

    private final List<IWorkHours> workHours;

    public CombinedWorkHours(List<IWorkHours> workHours) {
        Validate.notNull(workHours);
        Validate.noNullElements(workHours);
        Validate.isTrue(!workHours.isEmpty());
        this.workHours = workHours;
    }

    public static CombinedWorkHours minOf(IWorkHours... workHours) {
        Validate.notNull(workHours);
        return new Min(Arrays.asList(workHours));
    }

    @Override
    public Integer getWorkableHours(LocalDate date) {
        Integer current = null;
        for (IWorkHours workHour : workHours) {
            current = current == null ? workHours(workHour, date)
                    : updateWorkHours(current, workHour, date);
        }
        return current;
    }

    protected abstract Integer workHours(IWorkHours workHour, LocalDate date);

    protected abstract Integer updateWorkHours(Integer current,
            IWorkHours workHour, LocalDate date);

}

class Min extends CombinedWorkHours {

    public Min(List<IWorkHours> workHours) {
        super(workHours);
    }

    @Override
    protected Integer updateWorkHours(Integer current, IWorkHours workHour,
            LocalDate date) {
        return Math.min(current, workHour.getWorkableHours(date));
    }

    @Override
    protected Integer workHours(IWorkHours workHour, LocalDate date) {
        return workHour.getWorkableHours(date);
    }
}
