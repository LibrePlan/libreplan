package org.zkoss.ganttz.timetracker;

import org.zkoss.zul.ListModel;
import org.zkoss.zul.RowRenderer;

public class TimeTrackedTable extends TimeTrackerComponent {

    private final ListModel listModel;

    private final RowRenderer rowRenderer;

    public TimeTrackedTable(ListModel listModel, RowRenderer rowRenderer,
            TimeTracker timeTracker,
            String idTimeTrackerElement) {
        super(timeTracker, "~./ganttz/zul/timetracker/secondlevelgrid.zul",
                idTimeTrackerElement);
        this.listModel = listModel;
        this.rowRenderer = rowRenderer;
    }

    @Override
    protected void scrollHorizontalPercentage(int pixelsDisplacement) {
    }

    public ListModel getTableModel() {
        return listModel;
    }

    public RowRenderer getGridRenderer() {
        return rowRenderer;
    }

}
