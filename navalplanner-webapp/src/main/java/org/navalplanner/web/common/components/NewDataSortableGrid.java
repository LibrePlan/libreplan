/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 * Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.common.components;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelExt;
import org.zkoss.zul.api.Column;

/**
 * NewDataSortableGrid is a macrocomponent which extends {@link Grid}
 *<p>
 * NewDataSortableGrid objects must include inside it
 * {@link NewDataSortableColumn} components. They notify their grid when they
 * are sorted. The first time it is rendered the NewDataSortableGrid the first
 * of the {@link Column} is used to order the model. So, in the case the model
 * is not given in a proper order, it is ordered before showing the data.
 *<p>
 *
 * @author Javier Moran Rua <jmoran@igalia.com>
 */

public class NewDataSortableGrid extends Grid implements AfterCompose {

    private Column lastSortedColumn;
    private boolean lastSortedColumnAscending;

    public NewDataSortableGrid() {
        addEventListener(Events.ON_SORT, new EventListener() {
            @Override
            public void onEvent(Event event) {
                    sortByLastColumn();
            }
        });
    }

    public void setSortedColumn(Column c) {
        if (c == null) {
            throw new IllegalArgumentException("The column parameter cannot"
                    + "cannot be null");
        }
        this.lastSortedColumn = c;
    }

    public Column getSortedColumn() {
        return lastSortedColumn;
    }

    public void setLastSortedColumnAscending(boolean ascending) {
        this.lastSortedColumnAscending = ascending;
    }


    public boolean getLastSortedColumnAscending() {
        return lastSortedColumnAscending;
    }

    public void afterCompose() {
        // We post the ON_SORT event to order the grid. It is needed
        // to use an event because at this point the columns
        // inside the component are not still accessible
        Events.postEvent(Events.ON_SORT, this, null);
    }

    @Override
    public void setModel(ListModel listModel) {
        super.setModel(listModel);
        sortByLastColumn();
    }

    private void sortByLastColumn() {
        if (!(getModel() instanceof ListModelExt)) {
            return;
        }

        ListModelExt model = (ListModelExt) getModel();
        if (lastSortedColumnAscending) {
            model.sort(lastSortedColumn.getSortAscending(),
                    lastSortedColumnAscending);
        } else {
            model.sort(lastSortedColumn.getSortDescending(),
                    lastSortedColumnAscending);
        }
    }
}