/*
 * This file is part of LibrePlan
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.RowRenderer;

public class TimeTrackedTable<T> extends HtmlMacroComponent {

    private final Callable<List<T>> data;
    private final ICellForDetailItemRenderer<DetailItem, T> cellRenderer;
    private final TimeTracker timeTracker;
    private transient IZoomLevelChangedListener zoomListener;

    public TimeTrackedTable(Callable<List<T>> dataSource,
            ICellForDetailItemRenderer<DetailItem, T> cellRenderer,
            TimeTracker timeTracker) {
        this.data = dataSource;
        this.cellRenderer = cellRenderer;
        this.timeTracker = timeTracker;
        zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                recreate();
            }
        };
        this.timeTracker.addZoomListener(zoomListener);
    }

    public ListModel getTableModel() {
        List<T> list = getData();
        return new ListModelList(list);
    }

    private List<T> getData() {
        try {
            return data.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RowRenderer getRowRenderer() {
        return OnColumnsRowRenderer.create(cellRenderer, timeTracker
                .getDetailsSecondLevel());
    }

    public Collection<DetailItem> getDetailsSecondLevel() {
        return timeTracker.getDetailsSecondLevel();
    }

    public int getHorizontalSize() {
        return timeTracker.getHorizontalSize();
    }

}
