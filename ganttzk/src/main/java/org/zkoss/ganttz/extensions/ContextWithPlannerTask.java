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

package org.zkoss.ganttz.extensions;

import java.util.List;

import org.zkoss.ganttz.adapters.IDomainAndBeansMapper;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.Position;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.zk.ui.Component;

/**
 * An implementation of {@link IContextWithPlannerTask} that wraps another
 * context and specifies the task to be returned by
 * {@link IContextWithPlannerTask#getTask()}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ContextWithPlannerTask<T> implements IContextWithPlannerTask<T> {

    private final IContext<T> context;
    private final Task task;

    public static <T> IContextWithPlannerTask<T> create(IContext<T> context,
            Task task) {
        return new ContextWithPlannerTask<T>(context, task);
    }

    public ContextWithPlannerTask(IContext<T> context, Task task) {
        this.context = context;
        this.task = task;

    }

    public void add(T domainObject) {
        context.add(domainObject);
    }

    public void reload(PlannerConfiguration<?> configuration) {
        context.reload(configuration);
    }

    public Position remove(T domainObject) {
        return context.remove(domainObject);
    }

    public void add(Position position, T domainObject) {
        context.add(position, domainObject);
    };

    @Override
    public Component getRelativeTo() {
        return context.getRelativeTo();
    }

    @Override
    public void replace(T oldDomainObject, T newDomainObject) {
        context.replace(oldDomainObject, newDomainObject);
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public TimeTracker getTimeTracker() {
        return context.getTimeTracker();
    }

    @Override
    public IDomainAndBeansMapper<T> getMapper() {
        return context.getMapper();
    }

    @Override
    public void recalculatePosition(T domainObject) {
        context.recalculatePosition(domainObject);
    }

    @Override
    public void showCriticalPath() {
        context.showCriticalPath();
    }

    @Override
    public void hideCriticalPath() {
        context.hideCriticalPath();
    }

    @Override
    public void reloadCharts() {
        context.reloadCharts();
    }

    @Override
    public List<Task> getTasksOrderedByStartDate() {
        return context.getTasksOrderedByStartDate();
    }

    @Override
    public GanttDiagramGraph getGanttDiagramGraph() {
        return context.getGanttDiagramGraph();
    }

    @Override
    public List<T> getCriticalPath() {
        return context.getCriticalPath();
    }

    @Override
    public void hideAdvances() {
        context.hideAdvances();
    }

    @Override
    public void showAdvances() {
        context.showAdvances();
    }

    @Override
    public void hideReportedHours() {
        context.hideReportedHours();
    }

    @Override
    public void showReportedHours() {
        context.showReportedHours();
    }
}
