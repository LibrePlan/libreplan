package org.zkoss.ganttz.timetracker;

public class TimeTrackedTable extends TimeTrackerComponent {

    public TimeTrackedTable(TimeTracker timeTracker) {
        super(timeTracker, "~./ganttz/zul/timetrackersecondlevel.zul");
    }

    @Override
    protected void scrollHorizontalPercentage(int pixelsDisplacement) {
    }

}
