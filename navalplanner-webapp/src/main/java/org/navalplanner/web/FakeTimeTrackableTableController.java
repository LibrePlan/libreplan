package org.navalplanner.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.timetracker.ICellForDetailItemRenderer;
import org.zkoss.ganttz.timetracker.TimeTrackedTable;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

public class FakeTimeTrackableTableController extends GenericForwardComposer {

    private Div insertionPoint;
    private TimeTrackedTable<FakeData> timeTrackedTable;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        timeTrackedTable = new TimeTrackedTable<FakeData>(getDataSource(),
                getRenderer(), new TimeTracker(createExampleInterval()),
                "timetrackedtable");
        insertionPoint.appendChild(timeTrackedTable);
        timeTrackedTable.afterCompose();
    }

    private Callable<List<FakeData>> getDataSource() {
        return new Callable<List<FakeData>>() {

            @Override
            public List<FakeData> call() throws Exception {
                List<FakeData> result = new ArrayList<FakeData>();
                for (int i = 0; i < 10; i++) {
                    result.add(new FakeData(6));
                }
                return result;
            }
        };
    }

    private ICellForDetailItemRenderer<FakeData> getRenderer() {
        return new ICellForDetailItemRenderer<FakeData>() {

            @Override
            public Component cellFor(DetailItem item, FakeData data) {
                Label label = new Label();
                label.setValue(data.getHoursForDetailItem(item) + "h");
                return label;
            }
        };
    }

    private Interval createExampleInterval() {
        LocalDate start = new LocalDate(2008, 1, 1);
        LocalDate end = new LocalDate(2009, 1, 1);
        return new Interval(asDate(start), asDate(end));
    }

    private Date asDate(LocalDate start) {
        return start.toDateMidnight().toDate();
    }

}

class FakeData {
    private final int hoursPerDay;

    FakeData(int hoursPerDay) {
        this.hoursPerDay = hoursPerDay;
    }

    public int getHoursForDetailItem(DetailItem detail) {
        Days daysBetween = Days.daysBetween(detail.getStartDate(),
                detail.getEndDate());
        return daysBetween.getDays() * hoursPerDay;
    }
}
