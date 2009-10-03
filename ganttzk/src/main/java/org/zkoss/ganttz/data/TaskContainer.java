/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.WeakReferencedListeners.IListenerNotification;

/**
 * This class contains the information of a task container. It can be modified
 * and notifies of the changes to the interested parties. <br/>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class TaskContainer extends Task {

    public TaskContainer(ITaskFundamentalProperties fundamentalProperties) {
        super(fundamentalProperties);
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

    private static <T> T getSmallest(Collection<T> elements,
            Comparator<T> comparator) {
        List<T> withoutNulls = removeNulls(elements);
        if (withoutNulls.isEmpty())
            throw new IllegalArgumentException("at least one required");
        T result = null;
        for (T element : withoutNulls) {
            result = result == null ? element : (comparator.compare(result,
                    element) < 0 ? result : element);
        }
        return result;
    }

    private static <T extends Comparable<? super T>> T getSmallest(
            Collection<T> elements) {
        return getSmallest(elements, new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                return o1.compareTo(o2);
            }
        });
    }

    private static <T extends Comparable<? super T>> T getBiggest(
            Collection<T> elements) {
        return getSmallest(elements, new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                return o2.compareTo(o1);
            }
        });
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

    public Date getSmallestBeginDateFromChildren() {
        if (tasks.isEmpty())
            return getBeginDate();
        return getSmallest(getStartDates());
    }

    private List<Date> getStartDates() {
        ArrayList<Date> result = new ArrayList<Date>();
        for (Task task : tasks) {
            result.add(task.getBeginDate());
        }
        return result;
    }

    private List<Date> getEndDates() {
        ArrayList<Date> result = new ArrayList<Date>();
        for (Task task : tasks) {
            result.add(task.getEndDate());
        }
        return result;
    }

    public Date getBiggestDateFromChildren() {
        if (tasks.isEmpty())
            return getEndDate();
        return getBiggest(getEndDates());
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }

    @Override
    protected void setVisible(boolean visible) {
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
        for (Task task : tasks) {
            task.setVisible(this.expanded);
        }
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

}