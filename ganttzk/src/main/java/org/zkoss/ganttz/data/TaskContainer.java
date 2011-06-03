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

package org.zkoss.ganttz.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.WeakReferencedListeners.IListenerNotification;
import org.zkoss.zk.ui.util.Clients;

/**
 * This class contains the information of a task container. It can be modified
 * and notifies of the changes to the interested parties. <br/>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class TaskContainer extends Task {

    public TaskContainer(ITaskFundamentalProperties fundamentalProperties,
            boolean expanded) {
        super(fundamentalProperties);
        this.expanded = expanded;
    }

    public interface IExpandListener {
        public void expandStateChanged(boolean isNowExpanded);
    }

    private static <T> List<T> removeNulls(Collection<T> elements) {
        ArrayList<T> result = new ArrayList<T>();
        for (T e : elements) {
            if (e != null) {
                result.add(e);
            }
        }
        return result;
    }

    private static <T extends Comparable<? super T>> T getSmallest(
            Collection<T> elements) {
        return Collections.min(removeNulls(elements));
    }

    private static <T extends Comparable<? super T>> T getBiggest(
            Collection<T> elements) {
        return Collections.max(removeNulls(elements));
    }

    private List<Task> tasks = new ArrayList<Task>();

    private boolean expanded = false;

    private WeakReferencedListeners<IExpandListener> expandListeners = WeakReferencedListeners
            .create();

    public void addExpandListener(IExpandListener expandListener) {
        expandListeners.addListener(expandListener);
    }

    public void add(Task task) {
        tasks.add(task);
        task.setVisible(expanded);
    }

    @Override
    public List<Task> getTasks() {
        return tasks;
    }

    private List<GanttDate> getEndDates() {
        ArrayList<GanttDate> result = new ArrayList<GanttDate>();
        for (Task task : tasks) {
            result.add(task.getEndDate());
        }
        return result;
    }

    public GanttDate getBiggestDateFromChildren() {
        if (tasks.isEmpty()) {
            return getEndDate();
        }
        return getBiggest(getEndDates());
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!this.expanded) {
            return;
        }
        for (Task task : tasks) {
            task.setVisible(true);
        }
    }

    public void setExpanded(boolean expanded) {
        boolean valueChanged = expanded != this.expanded;
        this.expanded = expanded;
        if (valueChanged) {
            expandListeners
                    .fireEvent(new IListenerNotification<IExpandListener>() {

                        @Override
                        public void doNotify(IExpandListener listener) {
                            listener
                                    .expandStateChanged(TaskContainer.this.expanded);
                        }
                    });
        }
        refreshTooltips();
    }

    private void refreshTooltips() {
        // Could be optimized asking planner for tooltips display state to
        // create expanded elements with the proper state
        Clients.evalJavaScript("zkTasklist.refreshTooltips();");
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    public void remove(Task toBeRemoved) {
        tasks.remove(toBeRemoved);
    }

    public boolean contains(Task task) {
        return tasks.contains(task);
    }

    public void addAll(int position, Collection<? extends Task> tasksCreated) {
        tasks.addAll(position, tasksCreated);
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canBeExplicitlyMoved() {
        return false;
    }

    @Override
    public List<Task> getAllTaskLeafs() {
        List<Task> result = new ArrayList<Task>();
        for (Task task : tasks) {
            result.addAll(task.getAllTaskLeafs());
        }
        return result;
    }

}