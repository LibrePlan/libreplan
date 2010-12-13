package org.navalplanner.web.common;

import static org.navalplanner.web.planner.TaskElementAdapter.toGantt;
import static org.navalplanner.web.planner.TaskElementAdapter.toIntraDay;
import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.biggerOrEqualThan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskElement.IDatesInterceptor;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.web.common.TemplateModel.DependencyWithVisibility;
import org.navalplanner.web.planner.TaskElementAdapter;
import org.zkoss.ganttz.data.DependencyType;
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

    private LocalDate deadline;

    public static TemplateModelAdapter create(Scenario scenario, LocalDate deadline) {
        return new TemplateModelAdapter(scenario, deadline);
    }

    private TemplateModelAdapter(Scenario scenario, LocalDate deadline) {
        Validate.notNull(scenario);
        this.scenario = scenario;
        this.deadline = deadline;
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
    public List<Constraint<GanttDate>> getEndConstraintsGivenIncoming(
            Set<DependencyWithVisibility> incoming) {
        return DependencyWithVisibility.getEndConstraintsGiven(this, incoming);
    }

    @Override
    public List<Constraint<GanttDate>> getCurrentLenghtConstraintFor(
            TaskElement task) {
        if (isContainer(task)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(biggerOrEqualThan(this
                .getEndDateFor(task)));
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
    public GanttDate getSmallestBeginDateFromChildrenFor(TaskElement container) {
        TaskGroup taskGroup = (TaskGroup) container;
        return toGantt(taskGroup.getSmallestStartDateFromChildren());
    }

    @Override
    public TaskElement getSource(DependencyWithVisibility dependency) {
        return dependency.getSource();
    }

    @Override
    public List<Constraint<GanttDate>> getStartConstraintsFor(TaskElement task) {
        return TaskElementAdapter.getStartConstraintsFor(task);
    }

    @Override
    public List<Constraint<GanttDate>> getEndConstraintsFor(TaskElement task) {
        return TaskElementAdapter.getEndConstraintsFor(task, deadline);
    }

    @Override
    public List<Constraint<GanttDate>> getStartConstraintsGiven(
            Set<DependencyWithVisibility> withDependencies) {
        return DependencyWithVisibility.getStartConstraintsGiven(this,
                withDependencies);
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
        task.moveEndTo(scenario, toIntraDay(newEnd));
    }

    @Override
    public void setStartDateFor(TaskElement task, GanttDate newStart) {
        task.moveTo(scenario, toIntraDay(newStart));
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
