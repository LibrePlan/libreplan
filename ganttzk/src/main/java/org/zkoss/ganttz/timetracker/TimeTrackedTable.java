package org.zkoss.ganttz.timetracker;

public class TimeTrackedTable extends TimeTrackerComponent {

    public TimeTrackedTable(TimeTracker timeTracker, String idTimeTrackerElement) {
        super(timeTracker, "~./ganttz/zul/timetrackersecondlevel.zul",
                idTimeTrackerElement);
    }

    @Override
    protected void scrollHorizontalPercentage(int pixelsDisplacement) {
    }

}
