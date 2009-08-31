package org.zkoss.ganttz.timetracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.api.Column;

public class TimeTrackedTableWithLeftPane<A, B> extends Div {

    private final TimeTrackedTable<B> timeTrackedTable;
    private Grid leftPane;
    private final Callable<PairOfLists<A, B>> dataSource;
    private IZoomLevelChangedListener zoomLevelListener;

    public <C extends IConvertibleToColumn> TimeTrackedTableWithLeftPane(
            Callable<PairOfLists<A, B>> dataSource,
            List<C> leftPaneColumns,
            ICellForDetailItemRenderer<C, A> leftPaneCellRenderer,
            ICellForDetailItemRenderer<DetailItem, B> cellRendererForTimeTracker,
            TimeTracker timeTracker) {
        this.dataSource = dataSource;
        timeTrackedTable = new TimeTrackedTable<B>(
                dataForTimeTracker(dataSource), cellRendererForTimeTracker,
                timeTracker, "timetrackedtable");
        leftPane = new Grid();
        zoomLevelListener = new IZoomLevelChangedListener() {
            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                loadModelForLeftPane();
            }
        };
        timeTracker.addZoomListener(zoomLevelListener);
        leftPane.appendChild(createColumns(leftPaneColumns));
        leftPane.setRowRenderer(OnColumnsRowRenderer.create(
                leftPaneCellRenderer, leftPaneColumns));
        appendChild(leftPane);
        appendChild(timeTrackedTable);
        loadModelForLeftPane();
    }

    private static Columns createColumns(
            Collection<? extends IConvertibleToColumn> convertibleToColumns) {
        Columns result = new Columns();
        for (Column column : toColumns(convertibleToColumns)) {
            result.appendChild(column);
        }
        return result;
    }

    private static List<Column> toColumns(
            Collection<? extends IConvertibleToColumn> convertibleToColumns) {
        List<Column> columns = new ArrayList<Column>();
        for (IConvertibleToColumn c : convertibleToColumns) {
            columns.add(c.toColumn());
        }
        return columns;
    }

    private void loadModelForLeftPane() {
        leftPane.setModel(createModelForLeftPane());
    }

    private ListModel createModelForLeftPane() {
        return new ListModelList(retrieveLeftPaneList());
    }

    private List<A> retrieveLeftPaneList() {
        PairOfLists<A, B> pair = loadPairOfListsFromCallable();
        return pair.getFirst();
    }

    private PairOfLists<A, B> loadPairOfListsFromCallable() {
        try {
            return dataSource.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Callable<List<B>> dataForTimeTracker(
            final Callable<PairOfLists<A, B>> dataSource) {
        return new Callable<List<B>>() {

            @Override
            public List<B> call() throws Exception {
                return loadPairOfListsFromCallable().getSecond();
            }
        };
    }

    private boolean afterComposeCalled = false;

    public TimeTrackedTable<B> getTimeTrackedTable() {
        if (!afterComposeCalled) {
            timeTrackedTable.afterCompose();
            afterComposeCalled = true;
        }
        return timeTrackedTable;
    }

    public Grid getLeftPane() {
        return leftPane;
    }

}
