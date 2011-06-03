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

package org.navalplanner.web.common.components;

import java.util.Comparator;

import org.apache.commons.collections.comparators.BooleanComparator;
import org.apache.commons.lang.Validate;
import org.navalplanner.business.INewObject;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Column;
import org.zkoss.zul.api.Grid;

/**
 * {@link NewDataSortableColumn} is a macrocomponent which extends {@link Column}
 *<p>
 * NewDataSortableColumn extends the {@link Column} component and adds the
 * following behaviour: It creates a {@link Comparator} which is a
 * decorator of the comparator the component has configured. This comparator
 * delegates in the encapsulated comparator configured by the user, except
 * when the objects implement the interface {@link INewObject}. In that case,
 * the new objects are considered always that are placed before than the
 * object which are not new.
 * <p>
 * The {@link NewDataSortableColumn} must be included inside {@link NewDataSortableGrid}
 * in order to work properly. They notify the {@link NewDataSortableGrid} in which are
 * included when they are requested to be sorted.
 *<p>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */

public class NewDataSortableColumn extends Column implements AfterCompose {

    private static class NewObjectDecoratorComparator implements
            Comparator<Object> {
        private Comparator<Object> decoratedComparator;

        public NewObjectDecoratorComparator(Comparator<Object> c) {
            Validate.notNull(c);
            this.decoratedComparator = c;
        }

        @Override
        public int compare(Object o1, Object o2) {
            if (!doComparingObjectsSupportInterface(o1, o2)) {
                return decoratedComparator.compare(o1, o2);
            } else {
                return decorateBehaviour((INewObject) o1, (INewObject) o2);
            }
        }

        private boolean doComparingObjectsSupportInterface(Object o1, Object o2) {
            return (o1 instanceof INewObject) && (o2 instanceof INewObject);
        }

        private int decorateBehaviour(INewObject o1, INewObject o2) {
            if (o1.isNewObject() == o2.isNewObject()) {
                return decoratedComparator.compare(o1, o2);
            }
            return BooleanComparator.getTrueFirstComparator().compare(
                    o1.isNewObject(), o2.isNewObject());
        }
    }

    @Override
    public void setSortAscending(Comparator c) {
        super.setSortAscending(new NewObjectDecoratorComparator(c));
    }

    @Override
    public void setSortDescending(Comparator c) {
        super.setSortDescending(new NewObjectDecoratorComparator(c));
    }

    @Override
    public boolean sort(boolean ascending) {
        Grid grid = getGrid();
        if (grid instanceof NewDataSortableGrid) {
            ((NewDataSortableGrid) grid).setSortedColumn(this);
            ((NewDataSortableGrid) grid).setLastSortedColumnAscending(ascending);
        }
        return super.sort(ascending);
    }

    @Override
    public void afterCompose() {
        Grid g = getGrid();

        if (g instanceof NewDataSortableGrid) {
            NewDataSortableGrid castedGrid = (NewDataSortableGrid) g;

            // The first registered column is responsible for ordering
            if (castedGrid.getSortedColumn() == null) {
                markInGridAsColumnToOrder(castedGrid);
            }
        }
    }

    private void markInGridAsColumnToOrder(NewDataSortableGrid parentGrid) {
        parentGrid.setSortedColumn(this);

        if ("ascending".equals(getSortDirection())) {
            parentGrid.setLastSortedColumnAscending(
                    Boolean.TRUE.booleanValue());
        } else if ("descending".equals(getSortDirection())) {
            parentGrid.setLastSortedColumnAscending(
                    Boolean.FALSE.booleanValue());
        }
    }

}