package org.navalplanner.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.timetracker.ICellForDetailItemRenderer;
import org.zkoss.ganttz.timetracker.IConvertibleToColumn;
import org.zkoss.ganttz.timetracker.PairOfLists;
import org.zkoss.ganttz.timetracker.TimeTrackedTableWithLeftPane;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.api.Column;

public class FakeTimeTrackableTableController extends GenericForwardComposer {

    private Div insertionPoint;
    private TimeTrackedTableWithLeftPane<FakeDataLeft, FakeData> timeTrackedTableWithLeftPane;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.timeTrackedTableWithLeftPane = new TimeTrackedTableWithLeftPane<FakeDataLeft, FakeData>(
                getDataSource(), getColumnsForLeft(), getLeftRenderer(),
                getRightRenderer(), new TimeTracker(createExampleInterval()));
        insertionPoint.appendChild(timeTrackedTableWithLeftPane);
    }

    private ICellForDetailItemRenderer<FakeColumn, FakeDataLeft> getLeftRenderer() {
        return new ICellForDetailItemRenderer<FakeColumn, FakeDataLeft>() {

            @Override
            public Component cellFor(FakeColumn column, FakeDataLeft data) {
                return new Label(column.getName() + data.getRowNumber());
            }
        };
    }

    private List<FakeColumn> getColumnsForLeft() {
        String[] names = { "A", "B", "C" };
        List<FakeColumn> result = new ArrayList<FakeColumn>();
        for (final String columnName : names) {
            result.add(createColumnWithLabel(columnName));
        }
        return result;
    }

    private FakeColumn createColumnWithLabel(final String columnName) {
        return new FakeColumn(columnName);
    }

    private Callable<PairOfLists<FakeDataLeft, FakeData>> getDataSource() {
        return new Callable<PairOfLists<FakeDataLeft, FakeData>>() {

            @Override
            public PairOfLists<FakeDataLeft, FakeData> call() throws Exception {
                List<FakeData> right = new ArrayList<FakeData>();
                List<FakeDataLeft> left = new ArrayList<FakeDataLeft>();
                for (int i = 0; i < 10; i++) {
                    right.add(new FakeData(6));
                    left.add(new FakeDataLeft(i + 1));
                }
                return new PairOfLists<FakeDataLeft, FakeData>(left, right);
            }
        };
    }

    private ICellForDetailItemRenderer<DetailItem, FakeData> getRightRenderer() {
        return new ICellForDetailItemRenderer<DetailItem, FakeData>() {

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

class FakeColumn implements IConvertibleToColumn {
    private final String columnName;

    FakeColumn(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public Column toColumn() {
        Column column = new org.zkoss.zul.Column();
        column.setLabel(columnName);
        return column;
    }

    public String getName() {
        return columnName;
    }
}

class FakeDataLeft {
    private final int row;

    FakeDataLeft(int row) {
        this.row = row;
    }

    public int getRowNumber() {
        return row;
    }

}

class FakeData {
    private final int hoursPerDay;

    FakeData(int hoursPerDay) {
        this.hoursPerDay = hoursPerDay;
    }

    public int getHoursForDetailItem(DetailItem detail) {
        Days daysBetween = Days.daysBetween(detail.getStartDate(), detail
                .getEndDate());
        return daysBetween.getDays() * hoursPerDay;
    }
}
