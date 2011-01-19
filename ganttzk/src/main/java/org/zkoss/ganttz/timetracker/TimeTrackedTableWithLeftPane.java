/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.zkoss.ganttz.timetracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.api.Column;

public class TimeTrackedTableWithLeftPane<A, B> {

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
                timeTracker);
        timeTrackedTable.setSclass("inner-timetracked-table");
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

    public TimeTrackedTable<B> getRightPane() {
        return timeTrackedTable;
    }

    public Grid getLeftPane() {
        return leftPane;
    }

    public void reload() {
        timeTrackedTable.recreate();
        loadModelForLeftPane();
    }

}
