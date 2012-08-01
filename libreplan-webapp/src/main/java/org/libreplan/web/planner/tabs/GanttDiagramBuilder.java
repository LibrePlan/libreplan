/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.web.planner.tabs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.common.TemplateModel.DependencyWithVisibility;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.GanttDiagramGraph.IAdapter;
import org.zkoss.ganttz.data.constraint.Constraint;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Builds a Gantt Diagram in memory, useful for doing operations with a
 *         Gantt Diagram out of the Gantt View
 *
 */
public class GanttDiagramBuilder {

    public static GanttDiagramGraph<TaskElement, DependencyWithVisibility> createForcingDependencies(
            Order order, IAdapter<TaskElement, DependencyWithVisibility> adapter) {
        GanttDiagramGraph<TaskElement, DependencyWithVisibility> graph = createFor(
                order, adapter);
        TaskSource taskSource = order.getTaskSource();
        if (taskSource == null) {
            return graph;
        }
        graph.addTopLevel(taskSource.getTask());
        for (Dependency each : getAllDependencies(order)) {
            graph.addWithoutEnforcingConstraints(DependencyWithVisibility
                    .existent(each));
        }
        return graph;
    }

    private static GanttDiagramGraph<TaskElement, DependencyWithVisibility> createFor(
            Order order, IAdapter<TaskElement, DependencyWithVisibility> adapter) {
        List<Constraint<GanttDate>> startConstraints = PlannerConfiguration
                .getStartConstraintsGiven(GanttDate.createFrom(order.getInitDate()));
        List<Constraint<GanttDate>> endConstraints = PlannerConfiguration
                .getEndConstraintsGiven(GanttDate.createFrom(order.getDeadline()));
        GanttDiagramGraph<TaskElement, DependencyWithVisibility> result = GanttDiagramGraph
                .create(order.isScheduleBackwards(), adapter, startConstraints,
                        endConstraints,
                        order.getDependenciesConstraintsHavePriority());
        return result;
    }

    private static Set<Dependency> getAllDependencies(Order order) {
        Set<Dependency> dependencies = new HashSet<Dependency>();
        for (TaskElement each : getTaskElementsFrom(order)) {
            Set<Dependency> dependenciesWithThisOrigin = each
                    .getDependenciesWithThisOrigin();
            dependencies.addAll(dependenciesWithThisOrigin);
        }
        return dependencies;
    }

    private static List<TaskElement> getTaskElementsFrom(Order order) {
        List<TaskElement> result = new ArrayList<TaskElement>();
        for (TaskSource each : order.getTaskSourcesFromBottomToTop()) {
            result.add(each.getTask());
        }
        return result;
    }

}