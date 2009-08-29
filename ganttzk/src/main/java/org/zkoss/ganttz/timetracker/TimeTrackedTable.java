package org.zkoss.ganttz.timetracker;

import java.util.List;
import java.util.concurrent.Callable;

import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.RowRenderer;

public class TimeTrackedTable<T> extends TimeTrackerComponent {

    private final Callable<List<T>> data;
    private final ICellForDetailItemRenderer<T> cellRenderer;

    public TimeTrackedTable(Callable<List<T>> dataSource,
            ICellForDetailItemRenderer<T> cellRenderer,
            TimeTracker timeTracker,
            String idTimeTrackerElement) {
        super(timeTracker, "~./ganttz/zul/timetracker/secondlevelgrid.zul",
                idTimeTrackerElement);
        this.data = dataSource;
        this.cellRenderer = cellRenderer;
    }

    @Override
    protected void scrollHorizontalPercentage(int pixelsDisplacement) {
    }

    public ListModel getTableModel() {
        return new ListModelList(getData());
    }

    private List<T> getData() {
        try {
            return data.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RowRenderer getRowRenderer() {
        return OnDetailItemsRowRenderer.create(cellRenderer,
                getDetailsSecondLevel());
    }

}
