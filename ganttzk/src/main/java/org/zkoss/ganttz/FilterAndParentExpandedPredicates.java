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

package org.zkoss.ganttz;

import org.zkoss.ganttz.data.Position;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.extensions.IContext;

/**
 * A predicate over {@link Task} elements checking if it is visible. A
 * {@link Task} should visible if it fulfill a filter and its parent (if any) is
 * expanded.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class FilterAndParentExpandedPredicates implements IPredicate {

    private final IContext<?> context;

    private boolean filterContainers = false;

    public FilterAndParentExpandedPredicates(IContext<?> context) {
        this.context = context;
    }

    @Override
    public boolean accepts(Object object) {
        return accepts((Task) object);
    }

    private boolean accepts(Task task) {
        boolean result = true;
        if (filterContainers) {
            result &= acceptsContainers(task);
        } else {
            result &= getParentExpandedPredicate().accepts(task);
        }

        return result && accpetsFilterPredicate(task);
    }

    public boolean acceptsContainers(Task task) {
        if (filterContainers) {
            if (task.isContainer()) {
                return false;
            }
        }
        return true;
    }


    public boolean accpetsFilterPredicateAndContainers(Task task) {
        return acceptsContainers(task) && accpetsFilterPredicate(task);
    }

    public abstract boolean accpetsFilterPredicate(Task task);

    private IPredicate getParentExpandedPredicate() {
        return new IPredicate() {

            @Override
            public boolean accepts(Object object) {
                return accepts((Task) object);
            }

            private boolean accepts(Task task) {
                Position position = context.getMapper().findPositionFor(task);
                if (position.isAtTop()) {
                    return true;
                } else {
                    for (TaskContainer taskContainer : position.getAncestors()) {
                        if (!taskContainer.isExpanded()) {
                            return false;
                        }
                    }
                    return true;
                }
            }

        };
    }

    public void setFilterContainers(boolean filterContainers) {
        this.filterContainers = filterContainers;
    }

    public boolean isFilterContainers() {
        return filterContainers;
    }

}
