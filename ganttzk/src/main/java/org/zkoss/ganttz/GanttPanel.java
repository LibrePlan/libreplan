/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

import java.util.List;

import org.zkoss.ganttz.adapters.IDisabilityConfiguration;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponent;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

public class GanttPanel extends XulElement implements AfterCompose {

    private TaskList tasksLists;

    private TimeTrackerComponent timeTrackerComponent;

    private DependencyList dependencyList;

    private final GanttDiagramGraph diagramGraph;

    private final Planner planner;

    public GanttPanel(
            Planner planner,
            List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized,
            CommandOnTaskContextualized<?> doubleClickCommand,
            IDisabilityConfiguration disabilityConfiguration,
            FilterAndParentExpandedPredicates predicate) {
        this.planner = planner;
        FunctionalityExposedForExtensions<?> context = (FunctionalityExposedForExtensions<?>) planner
                .getContext();
        this.diagramGraph = context.getDiagramGraph();
        timeTrackerComponent = timeTrackerForGanttPanel(context
                .getTimeTracker());
        appendChild(timeTrackerComponent);
        dependencyList = new DependencyList(context);
        tasksLists = TaskList.createFor(context, doubleClickCommand,
                commandsOnTasksContextualized, disabilityConfiguration,
                predicate);
        appendChild(tasksLists);
        appendChild(dependencyList);
    }

    private TimeTrackerComponent timeTrackerForGanttPanel(
            TimeTracker timeTracker) {
        return new TimeTrackerComponent(timeTracker) {
            @Override
            protected void scrollHorizontalPercentage(int pixelsDisplacement) {
                response("scroll_horizontal", new AuInvoke(GanttPanel.this,
                        "scroll_horizontal", "" + pixelsDisplacement));
            }
        };
    }

    @Override
    public void afterCompose() {
        tasksLists.afterCompose();
        dependencyList.setDependencyComponents(tasksLists
                .asDependencyComponents(diagramGraph.getVisibleDependencies()));
        timeTrackerComponent.afterCompose();
        dependencyList.afterCompose();
    }

    public TimeTrackerComponent getTimeTrackerComponent() {
        return timeTrackerComponent;
    }

    public TaskList getTaskList() {
        return tasksLists;
    }

    public DependencyList getDependencyList() {
        return dependencyList;
    }

    public void comingFromAnotherTab() {
        timeTrackerComponent.recreate();
    }

    public void zoomIncrease() {
        getTimeTracker().zoomIncrease();
    }

    public void zoomDecrease() {
        getTimeTracker().zoomDecrease();
    }

    public TimeTracker getTimeTracker() {
        return timeTrackerComponent.getTimeTracker();
    }

    public void setZoomLevel(ZoomLevel zoomLevel) {
        getTimeTracker().setZoomLevel(zoomLevel);
    }

    public Planner getPlanner() {
        return planner;
    }

}