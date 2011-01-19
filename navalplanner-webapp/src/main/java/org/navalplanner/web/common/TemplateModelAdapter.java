/*
 * This file is part of NavalPlan
 *
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
package org.navalplanner.web.common;

import static org.navalplanner.web.planner.TaskElementAdapter.toGantt;
import static org.navalplanner.web.planner.TaskElementAdapter.toIntraDay;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskElement.IDatesHandler;
import org.navalplanner.business.planner.entities.TaskElement.IDatesInterceptor;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.web.common.TemplateModel.DependencyWithVisibility;
import org.navalplanner.web.planner.TaskElementAdapter;
import org.zkoss.ganttz.data.ConstraintCalculator;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.DependencyType.Point;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.GanttDiagramGraph.IAdapter;
import org.zkoss.ganttz.data.GanttDiagramGraph.IDependenciesEnforcerHook;
import org.zkoss.ganttz.data.GanttDiagramGraph.IDependenciesEnforcerHookFactory;
import org.zkoss.ganttz.data.constraint.Constraint;

/**
 *
 * @author Manuel Rego Casasnovas<mrego@igalia.com>
 *
 */
public class TemplateModelAdapter implements
        IAdapter<TaskElement, DependencyWithVisibility> {

    private final Scenario scenario;

    private final LocalDate orderInitDate;

    private final LocalDate deadline;

    private final IResourceDAO resourceDAO;

    public static TemplateModelAdapter create(Scenario scenario,
            LocalDate initDate, LocalDate deadline, IResourceDAO resourceDAO) {
        return new TemplateModelAdapter(scenario, initDate, deadline,
                resourceDAO);
    }

    private TemplateModelAdapter(Scenario scenario, LocalDate orderInitDate,
            LocalDate deadline, IResourceDAO resourceDAO) {
        Validate.notNull(scenario);
        Validate.notNull(resourceDAO);
        this.scenario = scenario;
        this.orderInitDate = orderInitDate;
        this.deadline = deadline;
        this.resourceDAO = resourceDAO;
    }

    @Override
    public DependencyWithVisibility createInvisibleDependency(
            TaskElement origin, TaskElement destination, DependencyType type) {
        return DependencyWithVisibility.createInvisible(origin, destination,
                type);
    }

    @Override
    public List<TaskElement> getChildren(TaskElement task) {
        if (!task.isLeaf()) {
            return task.getChildren();
        } else {
            return new ArrayList<TaskElement>();
        }
    }

    @Override
    public Class<DependencyWithVisibility> getDependencyType() {
        return DependencyWithVisibility.class;
    }

    @Override
    public TaskElement getDestination(DependencyWithVisibility dependency) {
        return dependency.getDestination();
    }

    @Override
    public GanttDate getEndDateFor(TaskElement task) {
        return toGantt(task.getIntraDayEndDate());
    }

    @Override
    public TaskElement getSource(DependencyWithVisibility dependency) {
        return dependency.getSource();
    }

    @Override
    public List<Constraint<GanttDate>> getStartConstraintsFor(TaskElement task) {
        return TaskElementAdapter.getStartConstraintsFor(task, orderInitDate);
    }

    @Override
    public List<Constraint<GanttDate>> getEndConstraintsFor(TaskElement task) {
        return TaskElementAdapter.getEndConstraintsFor(task, deadline);
    }

    @Override
    public List<Constraint<GanttDate>> getConstraints(
            ConstraintCalculator<TaskElement> calculator,
            Set<DependencyWithVisibility> withDependencies, Point point) {
        return DependencyWithVisibility.getConstraints(calculator,
                withDependencies, point);
    }

    @Override
    public GanttDate getStartDate(TaskElement task) {
        return toGantt(task.getIntraDayStartDate());
    }

    @Override
    public DependencyType getType(DependencyWithVisibility dependency) {
        return dependency.getType();
    }

    @Override
    public boolean isContainer(TaskElement task) {
        return !task.isLeaf() && !task.isMilestone();
    }

    @Override
    public boolean isVisible(DependencyWithVisibility dependency) {
        return dependency.isVisible();
    }

    @Override
    public void registerDependenciesEnforcerHookOn(TaskElement task,
            IDependenciesEnforcerHookFactory<TaskElement> hookFactory) {
        IDependenciesEnforcerHook enforcer = hookFactory.create(task);
        task.setDatesInterceptor(asIntercerptor(enforcer));
    }

    @Override
    public void setEndDateFor(TaskElement task, GanttDate newEnd) {
        getDatesHandler(task).moveEndTo(toIntraDay(newEnd));
    }

    @Override
    public void setStartDateFor(TaskElement task, GanttDate newStart) {
        getDatesHandler(task).moveTo(toIntraDay(newStart));
    }

    private IDatesHandler getDatesHandler(TaskElement taskElement) {
        return taskElement.getDatesHandler(scenario, resourceDAO);
    }

    @Override
    public boolean isFixed(TaskElement task) {
        return task.isLimitingAndHasDayAssignments();
    }

    private static IDatesInterceptor asIntercerptor(
            final IDependenciesEnforcerHook hook) {
        return new IDatesInterceptor() {

            @Override
            public void setStartDate(IntraDayDate previousStart,
                    IntraDayDate previousEnd, IntraDayDate newStart) {
                hook.setStartDate(convert(previousStart.getDate()),
                        convert(previousEnd.asExclusiveEnd()),
                        convert(newStart.getDate()));
            }

            @Override
            public void setNewEnd(IntraDayDate previousEnd, IntraDayDate newEnd) {
                hook.setNewEnd(convert(previousEnd.getDate()),
                        convert(newEnd.asExclusiveEnd()));
            }
        };
    }

    private static GanttDate convert(LocalDate date) {
        return GanttDate.createFrom(date);
    }


}
