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

package org.navalplanner.business.planner.entities;

import static org.navalplanner.business.workingday.EffortDuration.zero;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.entities.Dependency.Type;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.util.deepcopy.OnCopy;
import org.navalplanner.business.util.deepcopy.Strategy;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.business.workingday.ResourcesPerDay;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class TaskElement extends BaseEntity {

    public interface IDatesInterceptor {
        public void setStartDate(IntraDayDate previousStart,
                IntraDayDate previousEnd, IntraDayDate newStart);

        public void setNewEnd(IntraDayDate previousEnd, IntraDayDate newEnd);
    }

    private static final IDatesInterceptor EMPTY_INTERCEPTOR = new IDatesInterceptor() {

        @Override
        public void setStartDate(IntraDayDate previousStart,
                IntraDayDate previousEnd, IntraDayDate newStart) {
        }

        @Override
        public void setNewEnd(IntraDayDate previousEnd, IntraDayDate newEnd) {
        }

    };

    public static Comparator<TaskElement> getByStartDateComparator() {
        Comparator<TaskElement> result = new Comparator<TaskElement>() {

            @Override
            public int compare(TaskElement o1, TaskElement o2) {
                return o1.getStartDate().compareTo(o2.getStartDate());
            }
        };
        return result;
    }

    public static Comparator<? super TaskElement> getByEndDateComparator() {
        Comparator<TaskElement> result = new Comparator<TaskElement>() {

            @Override
            public int compare(TaskElement o1, TaskElement o2) {
                return o1.getEndDate().compareTo(o2.getEndDate());
            }
        };
        return result;
    }

    protected static <T extends TaskElement> T create(T taskElement,
            TaskSource taskSource) {
        taskElement.taskSource = taskSource;
        taskElement.updateDeadlineFromOrderElement();
        taskElement.setName(taskElement.getOrderElement().getName());
        taskElement.setStartDate(taskElement.getOrderElement().getOrder()
                .getInitDate());
        return create(taskElement);
    }

    protected static <T extends TaskElement> T createWithoutTaskSource(
            T taskElement) {
        return create(taskElement);
    }

    @OnCopy(Strategy.SHARE)
    private IDatesInterceptor datesInterceptor = EMPTY_INTERCEPTOR;

    private IntraDayDate startDate;

    private IntraDayDate endDate;

    private LocalDate deadline;

    private String name;

    private String notes;

    private TaskGroup parent;

    private Set<Dependency> dependenciesWithThisOrigin = new HashSet<Dependency>();

    private Set<Dependency> dependenciesWithThisDestination = new HashSet<Dependency>();

    @OnCopy(Strategy.SHARE)
    private BaseCalendar calendar;

    private TaskSource taskSource;

    private BigDecimal advancePercentage = BigDecimal.ZERO;

    private Boolean simplifiedAssignedStatusCalculationEnabled = false;

    public void initializeEndDateIfDoesntExist() {
        if (getEndDate() == null) {
            initializeEndDate();
        }
    }

    protected abstract void initializeEndDate();

    public void updateDeadlineFromOrderElement() {
        Date newDeadline = this.taskSource.getOrderElement().getDeadline();
        setDeadline(newDeadline == null ? null : new LocalDate(newDeadline));
    }

    public void setDatesInterceptor(IDatesInterceptor datesIntercerptor) {
        Validate.notNull(datesIntercerptor);
        this.datesInterceptor = datesIntercerptor;
    }

    public Integer getWorkHours() {
        if (taskSource == null) {
            return 0;
        }
        return taskSource.getTotalHours();
    }

    protected void copyPropertiesFrom(TaskElement task) {
        this.name = task.getName();
        this.notes = task.getNotes();
        this.startDate = task.startDate;
        this.taskSource = task.getTaskSource();
    }

    public TaskSource getTaskSource() {
        return taskSource;
    }

    protected void copyDependenciesTo(TaskElement result) {
        for (Dependency dependency : getDependenciesWithThisOrigin()) {
            Dependency.create(result, dependency.getDestination(),
                    dependency.getType());
        }
        for (Dependency dependency : getDependenciesWithThisDestination()) {
            Dependency.create(dependency.getOrigin(), result,
                    dependency.getType());
        }
    }

    protected void copyParenTo(TaskElement result) {
        if (this.getParent() != null) {
            this.getParent().addTaskElement(result);
        }
    }

    public TaskGroup getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OrderElement getOrderElement() {
        if (getTaskSource() == null) {
            return null;
        }
        return getTaskSource().getOrderElement();
    }

    public Set<Dependency> getDependenciesWithThisOrigin() {
        return Collections.unmodifiableSet(dependenciesWithThisOrigin);
    }

    public Set<Dependency> getDependenciesWithThisDestination() {
        return Collections.unmodifiableSet(dependenciesWithThisDestination);
    }

    @NotNull
    public Date getStartDate() {
        return startDate != null ? startDate.getDate().toDateTimeAtStartOfDay()
                .toDate() : null;
    }

    public IntraDayDate getIntraDayStartDate() {
        return startDate;
    }

    public LocalDate getStartAsLocalDate() {
        return startDate == null ? null : startDate.getDate();
    }

    public LocalDate getEndAsLocalDate() {
        return endDate == null ? null : endDate.getDate();
    }

    public void setStartDate(Date startDate) {
        setIntraDayStartDate(IntraDayDate.startOfDay(LocalDate
                .fromDateFields(startDate)));
    }

    public void setIntraDayStartDate(IntraDayDate startDate) {
        IntraDayDate previousStart = getIntraDayStartDate();
        IntraDayDate previousEnd = getIntraDayEndDate();
        this.startDate = startDate;
        datesInterceptor.setStartDate(previousStart, previousEnd,
                getIntraDayStartDate());
    }

    /**
     * Sets the startDate to newStartDate. It can update the endDate
     *
     * @param scenario
     * @param newStartDate
     */
    public void moveTo(Scenario scenario, IntraDayDate newStartDate) {
        if (newStartDate == null) {
            return;
        }
        IntraDayDate previousStart = this.startDate;
        this.endDate = calculateNewEndGiven(newStartDate);
        setIntraDayStartDate(newStartDate);
        if (!previousStart.equals(newStartDate)) {
            moveAllocations(scenario);
        }
    }

    protected abstract IntraDayDate calculateNewEndGiven(
            IntraDayDate newStartDate);

    protected abstract void moveAllocations(Scenario scenario);

    @NotNull
    public Date getEndDate() {
        return endDate != null ? endDate.toDateTimeAtStartOfDay().toDate()
                : null;
    }

    public void setEndDate(Date endDate) {
        setIntraDayEndDate(endDate != null ? IntraDayDate.create(
                LocalDate.fromDateFields(endDate), EffortDuration.zero())
                : null);
    }

    public void setIntraDayEndDate(IntraDayDate endDate) {
        IntraDayDate previousEnd = getIntraDayEndDate();
        this.endDate = endDate;
        datesInterceptor.setNewEnd(previousEnd, this.endDate);
    }

    public IntraDayDate getIntraDayEndDate() {
        return endDate;
    }

    public void resizeTo(Scenario scenario, Date endDate) {
        resizeTo(scenario,
                IntraDayDate.startOfDay(LocalDate.fromDateFields(endDate)));
    }

    public void resizeTo(Scenario scenario, IntraDayDate endDate) {
        if (!canBeResized()) {
            return;
        }
        boolean sameDay = this.endDate.areSameDay(endDate.getDate());
        setIntraDayEndDate(endDate);
        if (!sameDay) {
            moveAllocations(scenario);
        }
    }

    protected abstract boolean canBeResized();

    /**
     * @return if this task can be resized by an explicit action
     */
    public abstract boolean canBeExplicitlyResized();

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    void add(Dependency dependency) {
        if (this.equals(dependency.getOrigin())) {
            dependenciesWithThisOrigin.add(dependency);
        }
        if (this.equals(dependency.getDestination())) {
            dependenciesWithThisDestination.add(dependency);
        }
    }

    private void removeDependenciesWithThisOrigin(TaskElement origin, Type type) {
        ArrayList<Dependency> toBeRemoved = new ArrayList<Dependency>();
        for (Dependency dependency : dependenciesWithThisDestination) {
            if (dependency.getOrigin().equals(origin)
                    && dependency.getType().equals(type)) {
                toBeRemoved.add(dependency);
            }
        }
        dependenciesWithThisDestination.removeAll(toBeRemoved);
    }

    public void removeDependencyWithDestination(TaskElement destination, Type type) {
        ArrayList<Dependency> toBeRemoved = new ArrayList<Dependency>();
        for (Dependency dependency : dependenciesWithThisOrigin) {
            if (dependency.getDestination().equals(destination)
                    && dependency.getType().equals(type)) {
                toBeRemoved.add(dependency);
            }
        }
        destination.removeDependenciesWithThisOrigin(this, type);
        dependenciesWithThisOrigin.removeAll(toBeRemoved);
    }

    public abstract boolean isLeaf();

    public abstract List<TaskElement> getChildren();

    protected void setParent(TaskGroup taskGroup) {
        this.parent = taskGroup;
    }

    public void detach() {
        detachDependencies();
        detachFromParent();
    }

    public void detachFromParent() {
        if (parent != null) {
            parent.remove(this);
        }
    }

    private void removeDependenciesWithOrigin(TaskElement t) {
        List<Dependency> dependenciesToRemove = getDependenciesWithOrigin(t);
        dependenciesWithThisDestination.removeAll(dependenciesToRemove);
    }

    private void removeDependenciesWithDestination(TaskElement t) {
        List<Dependency> dependenciesToRemove = getDependenciesWithDestination(t);
        dependenciesWithThisOrigin.removeAll(dependenciesToRemove);
    }

    private List<Dependency> getDependenciesWithDestination(TaskElement t) {
        ArrayList<Dependency> result = new ArrayList<Dependency>();
        for (Dependency dependency : dependenciesWithThisOrigin) {
            if (dependency.getDestination().equals(t)) {
                result.add(dependency);
            }
        }
        return result;
    }

    private List<Dependency> getDependenciesWithOrigin(TaskElement t) {
        ArrayList<Dependency> result = new ArrayList<Dependency>();
        for (Dependency dependency : dependenciesWithThisDestination) {
            if (dependency.getOrigin().equals(t)) {
                result.add(dependency);
            }
        }
        return result;
    }

    private void detachDependencies() {
        detachOutcomingDependencies();
        detachIncomingDependencies();
    }

    private void detachIncomingDependencies() {
        Set<TaskElement> tasksToNotify = new HashSet<TaskElement>();
        for (Dependency dependency : dependenciesWithThisDestination) {
            tasksToNotify.add(dependency.getOrigin());
        }
        for (TaskElement taskElement : tasksToNotify) {
            taskElement.removeDependenciesWithDestination(this);
        }
    }

    private void detachOutcomingDependencies() {
        Set<TaskElement> tasksToNotify = new HashSet<TaskElement>();
        for (Dependency dependency : dependenciesWithThisOrigin) {
            tasksToNotify.add(dependency.getDestination());
        }
        for (TaskElement taskElement : tasksToNotify) {
            taskElement.removeDependenciesWithOrigin(this);
        }
    }

    public void setCalendar(BaseCalendar calendar) {
        this.calendar = calendar;
    }

    public BaseCalendar getCalendar() {
        if (calendar == null) {
            OrderElement orderElement = getOrderElement();
            return orderElement != null ? orderElement.getOrder().getCalendar()
                    : null;
        }
        return calendar;
    }

    public abstract Set<ResourceAllocation<?>> getSatisfiedResourceAllocations();

    public abstract Set<ResourceAllocation<?>> getAllResourceAllocations();

    public SortedMap<LocalDate, EffortDuration> getDurationsAssignedByDay() {
        SortedMap<LocalDate, EffortDuration> result = new TreeMap<LocalDate, EffortDuration>();
        for (ResourceAllocation<?> resourceAllocation : getSatisfiedResourceAllocations()) {
            for (DayAssignment each : resourceAllocation
                    .getAssignments()) {
                addToResult(result, each.getDay(), each.getDuration());
            }
        }
        return result;
    }

    private void addToResult(SortedMap<LocalDate, EffortDuration> result,
            LocalDate date, EffortDuration duration) {
        EffortDuration current = result.get(date) != null ? result.get(date)
                : zero();
        result.put(date, current.plus(duration));
    }

    public List<DayAssignment> getDayAssignments() {
        List<DayAssignment> dayAssignments = new ArrayList<DayAssignment>();
        Set<ResourceAllocation<?>> resourceAllocations = getSatisfiedResourceAllocations();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            dayAssignments.addAll(resourceAllocation.getAssignments());
        }
        return dayAssignments;
    }

    public boolean isSubcontracted() {
        // Just Task could be subcontracted
        return false;
    }

    public boolean isLimiting() {
        return false;
    }

    public boolean isLimitingAndHasDayAssignments() {
        return false;
    }

    public boolean hasConsolidations() {
        // Just Task could be consolidated
        return false;
    }

    public TaskElement getTopMost() {
        TaskElement result = this;
        while (result.getParent() != null) {
            result = result.getParent();
        }
        return result;
    }

    public abstract boolean isMilestone();

    public Boolean isSimplifiedAssignedStatusCalculationEnabled() {
        return simplifiedAssignedStatusCalculationEnabled;
    }

    public void setSimplifiedAssignedStatusCalculationEnabled(Boolean enabled) {
        this.simplifiedAssignedStatusCalculationEnabled = enabled;
    }

    public String getAssignedStatus() {
        if(isSimplifiedAssignedStatusCalculationEnabled()) {
            //simplified calculation has only two states:
            //unassigned, when hours allocated is zero, and
            //assigned otherwise
            if (getSumOfHoursAllocated() == 0) {
                return "unassigned";
            }
            return "assigned";
        }
        Set<ResourceAllocation<?>> resourceAllocations = getSatisfiedResourceAllocations();
        if (resourceAllocations.isEmpty()) {
            return "unassigned";
        }
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            final ResourcesPerDay resourcesPerDay = resourceAllocation.getResourcesPerDay();
            if (resourcesPerDay != null && resourcesPerDay.isZero()) {
                return "partially-assigned";
            }
        }
        return "assigned";
    }

    public abstract boolean hasLimitedResourceAllocation();

    public void removePredecessorsDayAssignmentsFor(Scenario scenario) {
        for (ResourceAllocation<?> each : getAllResourceAllocations()) {
            each.removePredecessorsDayAssignmentsFor(scenario);
        }
    }

    public void removeDayAssignmentsFor(Scenario scenario) {
        for (ResourceAllocation<?> each : getAllResourceAllocations()) {
            each.removeDayAssigmentsFor(scenario);
        }
    }

    public BigDecimal getAdvancePercentage() {
        return (advancePercentage == null) ? BigDecimal.ZERO
                : advancePercentage;
    }

    public void setAdvancePercentage(BigDecimal advancePercentage) {
        this.advancePercentage = advancePercentage;
    }

    public void detachFromDependencies() {
        for (Dependency each : copy(getDependenciesWithThisDestination())) {
            detachDependency(each);
        }
        for (Dependency each : copy(getDependenciesWithThisOrigin())) {
            detachDependency(each);
        }
    }

    /**
     * Copy the dependencies to a list in order to avoid
     * {@link ConcurrentModificationException}
     */
    private List<Dependency> copy(Set<Dependency> dependencies) {
        return new ArrayList<Dependency>(dependencies);
    }

    private void detachDependency(Dependency each) {
        each.getOrigin().removeDependencyWithDestination(each.getDestination(),
                each.getType());
    }

    private Integer sumOfHoursAllocated = new Integer(0);

    public void setSumOfHoursAllocated(Integer sumOfHoursAllocated) {
        this.sumOfHoursAllocated = sumOfHoursAllocated;
    }

    public void addSumOfHoursAllocated(Integer sumOfHoursAllocated) {
        this.sumOfHoursAllocated += sumOfHoursAllocated;
    }

    public Integer getSumOfHoursAllocated() {
        return sumOfHoursAllocated;
    }

}
