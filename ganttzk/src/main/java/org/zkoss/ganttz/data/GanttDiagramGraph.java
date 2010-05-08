/*
 * This file is part of NavalPlan
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.data.criticalpath.ICriticalPathCalculable;
import org.zkoss.ganttz.util.PreAndPostNotReentrantActionsWrapper;
import org.zkoss.ganttz.util.PreAndPostNotReentrantActionsWrapper.IAction;

/**
 * This class contains a graph with the {@link Task tasks} as vertexes and the
 * {@link Dependency dependency} as arcs. It enforces the rules embodied in the
 * dependencies and in the duration of the tasks using listeners. <br/>
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GanttDiagramGraph implements ICriticalPathCalculable<Task> {

    private static final Log LOG = LogFactory.getLog(GanttDiagramGraph.class);

    public interface IGraphChangeListener {
        public void execute();
    }

    private final DirectedGraph<Task, Dependency> graph = new SimpleDirectedGraph<Task, Dependency>(
            Dependency.class);

    private Map<Task, DependencyRulesEnforcer> rulesEnforcersByTask = new HashMap<Task, DependencyRulesEnforcer>();

    private Map<Task, ParentShrinkingEnforcer> parentShrinkingEnforcerByTask = new WeakHashMap<Task, ParentShrinkingEnforcer>();

    private List<Task> topLevelTasks = new ArrayList<Task>();

    private Map<Task, TaskContainer> fromChildToParent = new HashMap<Task, TaskContainer>();

    private final List<Constraint<Date>> globalStartConstraints;

    private final List<Constraint<Date>> globalEndConstraints;

    private final boolean dependenciesConstraintsHavePriority;

    private final PreAndPostNotReentrantActionsWrapper preAndPostActions = new PreAndPostNotReentrantActionsWrapper() {

        @Override
        protected void postAction() {
            executeGraphChangeListeners(new ArrayList<IGraphChangeListener>(
                    postGraphChangeListeners));
        }

        @Override
        protected void preAction() {
            executeGraphChangeListeners(new ArrayList<IGraphChangeListener>(
                    preGraphChangeListeners));
        }

        private void executeGraphChangeListeners(List<IGraphChangeListener> graphChangeListeners) {
            for (IGraphChangeListener each : graphChangeListeners) {
                try {
                    each.execute();
                } catch (Exception e) {
                    LOG.error("error executing execution listener", e);
                }
            }
        }
    };

    private List<IGraphChangeListener> preGraphChangeListeners = new ArrayList<IGraphChangeListener>();

    private List<IGraphChangeListener> postGraphChangeListeners = new ArrayList<IGraphChangeListener>();

    public void addPreGraphChangeListener(IGraphChangeListener preGraphChangeListener) {
        preGraphChangeListeners.add(preGraphChangeListener);
    }

    public void removePreGraphChangeListener(IGraphChangeListener preGraphChangeListener) {
        preGraphChangeListeners.remove(preGraphChangeListener);
    }

    public void addPostGraphChangeListener(IGraphChangeListener postGraphChangeListener) {
        postGraphChangeListeners.add(postGraphChangeListener);
    }

    public void removePostGraphChangeListener(IGraphChangeListener postGraphChangeListener) {
        postGraphChangeListeners.remove(postGraphChangeListener);
    }

    private PropertyChangeListener decorateWithPreAndPostActions(
            final PropertyChangeListener listener) {
        return new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                preAndPostActions.doAction(new IAction() {
                    @Override
                    public void doAction() {
                        listener.propertyChange(evt);
                    }
                });
            }
        };
    }

    public GanttDiagramGraph(List<Constraint<Date>> globalStartConstraints,
            List<Constraint<Date>> globalEndConstraints,
            boolean dependenciesConstraintsHavePriority) {
        this.globalStartConstraints = globalStartConstraints;
        this.globalEndConstraints = globalEndConstraints;
        this.dependenciesConstraintsHavePriority = dependenciesConstraintsHavePriority;
    }

    private List<DependencyRulesEnforcer> getOutgoing(Task task) {
        ArrayList<DependencyRulesEnforcer> result = new ArrayList<DependencyRulesEnforcer>();
        for (Dependency dependency : graph.outgoingEdgesOf(task)) {
            result.add(rulesEnforcersByTask.get(dependency.getDestination()));
        }
        return result;
    }

    private class ParentShrinkingEnforcer {

        private final TaskContainer container;

        private final Map<Task, Object> alreadyRegistered = new WeakHashMap<Task, Object>();

        private ParentShrinkingEnforcer(final TaskContainer container) {
            if (container == null) {
                throw new IllegalArgumentException("container cannot be null");
            }
            this.container = container;
            registerListeners();
        }

        void registerListeners() {
            for (Task subtask : this.container.getTasks()) {
                if (alreadyRegistered.containsKey(subtask)) {
                    continue;
                }
                subtask
                        .addFundamentalPropertiesChangeListener(decorateWithPreAndPostActions(new PropertyChangeListener() {

                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                enforce();
                            }
                        }));
            }
        }

        void enforce() {
            Date newBeginDate = this.container
                    .getSmallestBeginDateFromChildren();
            this.container.setBeginDate(newBeginDate);
            Date newEndDate = this.container.getBiggestDateFromChildren();
            this.container.setEndDate(newEndDate);
        }

    }

    private class DependencyRulesEnforcer {
        private final Task task;

        private DependencyRulesEnforcer(Task task) {
            if (task == null) {
                throw new IllegalArgumentException("task cannot be null");
            }
            this.task = task;
            this.task
                    .addFundamentalPropertiesChangeListener(decorateWithPreAndPostActions(new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            DependencyRulesEnforcer.this.enforce();
                            updateOutgoing(DependencyRulesEnforcer.this.task);
                        }
                    }));
        }

        void enforce() {
            Set<Dependency> incoming = graph.incomingEdgesOf(task);
            enforceStartDate(incoming);
            enforceEndDate(incoming);
        }

        @SuppressWarnings("unchecked")
        private void enforceEndDate(Set<Dependency> incoming) {
            Constraint<Date> currentLength = task.getCurrentLengthConstraint();
            Constraint<Date> respectStartDate = task
                    .getEndDateBiggerThanStartDate();
            Date newEnd = Constraint.<Date> initialValue(null)
                                    .withConstraints(currentLength)
                                    .withConstraints(Dependency
                                            .getEndConstraints(incoming))
                                    .withConstraints(respectStartDate)
                                    .apply();
            if (!task.getEndDate().equals(newEnd)) {
                task.setEndDate(newEnd);
            }
        }

        private void enforceStartDate(Set<Dependency> incoming) {
            Date newStart = calculateStartDateFor(task, incoming);
            Date childrenEarliest = getEarliestStartDateOfChildren(task);
            newStart = maxNotNull(newStart, childrenEarliest);
            if (!task.getBeginDate().equals(newStart)) {
                task.setBeginDate(newStart);
            }
        }

        private Date getEarliestStartDateOfChildren(Task task) {
            if (!task.isContainer()) {
                return null;
            }
            List<Date> startDates = getChildrenStartDates((TaskContainer) task);
            if (!startDates.isEmpty()) {
                return Collections.min(startDates);
            }
            return null;
        }

        private List<Date> getChildrenStartDates(TaskContainer container) {
            List<Task> children = container.getTasks();
            List<Date> startDates = new ArrayList<Date>();
            for (Task each : children) {
                Set<Dependency> incomingDependencies = withoutDependencyFrom(
                        container, graph.incomingEdgesOf(each));
                Date dateWithoutContainerInfluence = calculateStartDateFor(
                        each, incomingDependencies);
                if (dateWithoutContainerInfluence != null) {
                    startDates.add(dateWithoutContainerInfluence);
                }
            }
            return startDates;
        }

        private Set<Dependency> withoutDependencyFrom(TaskContainer container,
                Set<Dependency> incoming) {
            Set<Dependency> result = new HashSet<Dependency>();
            for (Dependency each : incoming) {
                if (!each.getSource().equals(container)) {
                    result.add(each);
                }
            }
            return result;
        }

        private Date maxNotNull(Date... dates) {
            List<Date> list = new ArrayList<Date>();
            for (Date each : dates) {
                if (each != null) {
                    list.add(each);
                }
            }
            if (list.isEmpty()) {
                return null;
            }
            return Collections.max(list);
        }
    }

    private Date calculateStartDateFor(Task task,
            Set<Dependency> withDependencies) {
        List<Constraint<Date>> dependencyConstraints = Dependency
                .getStartConstraints(withDependencies);
        Date newStart;
        if (dependenciesConstraintsHavePriority) {
            newStart = Constraint.<Date> initialValue(null)
                                 .withConstraints(task.getStartConstraints())
                                 .withConstraints(dependencyConstraints)
                                 .withConstraints(globalStartConstraints)
                                 .apply();

        } else {
            newStart = Constraint.<Date> initialValue(null)
                                 .withConstraints(dependencyConstraints)
                                 .withConstraints(task.getStartConstraints())
                                 .withConstraints(globalStartConstraints)
                                 .apply();
        }
        return newStart;
    }

    public void enforceAllRestrictions() {
        preAndPostActions.doAction(new IAction() {

            @Override
            public void doAction() {
                for (DependencyRulesEnforcer rulesEnforcer : rulesEnforcersByTask
                        .values()) {
                    rulesEnforcer.enforce();
                }
                for (ParentShrinkingEnforcer parentShrinkingEnforcer : parentShrinkingEnforcerByTask
                        .values()) {
                    parentShrinkingEnforcer.enforce();
                }
            }
        });
    }

    public void addTopLevel(Task task) {
        topLevelTasks.add(task);
        addTask(task);
    }

    public void addTopLevel(Collection<? extends Task> tasks) {
        for (Task task : tasks) {
            addTopLevel(task);
        }
    }

    public void addTasks(Collection<? extends Task> tasks) {
        for (Task t : tasks) {
            addTask(t);
        }
    }

    public void addTask(Task task) {
        graph.addVertex(task);
        rulesEnforcersByTask.put(task, new DependencyRulesEnforcer(task));
        if (task.isContainer()) {
            ParentShrinkingEnforcer parentShrinkingEnforcer = new ParentShrinkingEnforcer(
                    (TaskContainer) task);
            parentShrinkingEnforcerByTask.put(task, parentShrinkingEnforcer);
            List<Dependency> dependenciesToAdd = new ArrayList<Dependency>();
            for (Task child : task.getTasks()) {
                fromChildToParent.put(child, (TaskContainer) task);
                addTask(child);
                dependenciesToAdd.add(new Dependency(child, task,
                        DependencyType.END_END, false));
                dependenciesToAdd.add(new Dependency(task, child,
                        DependencyType.START_START,
                        false));
            }
            for (Dependency each : dependenciesToAdd) {
                add(each);
            }
        }
    }

    public void remove(Task task) {
        List<DependencyRulesEnforcer> outgoing = getOutgoing(task);
        graph.removeVertex(task);
        rulesEnforcersByTask.remove(task);
        topLevelTasks.remove(task);
        fromChildToParent.remove(task);
        update(outgoing);
        if (task.isContainer()) {
            for (Task t : task.getTasks()) {
                remove(t);
            }
        }
    }

    private void updateOutgoing(Task task) {
        update(getOutgoing(task));
    }

    private void update(List<DependencyRulesEnforcer> outgoing) {
        for (DependencyRulesEnforcer rulesEnforcer : outgoing) {
            rulesEnforcer.enforce();
        }
    }

    public void remove(Dependency dependency) {
        graph.removeEdge(dependency);
        Task destination = dependency.getDestination();
        rulesEnforcersByTask.get(destination).enforce();
    }

    public void add(Dependency dependency) {
        Task source = dependency.getSource();
        Task destination = dependency.getDestination();
        graph.addEdge(source, destination, dependency);
        enforceRestrictions(destination);
    }

    public void enforceRestrictions(final Task task) {
        preAndPostActions.doAction(new IAction() {
            @Override
            public void doAction() {
                getEnforcer(task).enforce();
            }
        });
    }

    public boolean contains(Dependency dependency) {
        return graph.containsEdge(dependency);
    }

    private DependencyRulesEnforcer getEnforcer(Task destination) {
        return rulesEnforcersByTask.get(destination);
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<Task>(graph.vertexSet());
    }

    public List<Dependency> getVisibleDependencies() {
        Set<Dependency> edgeSet = graph.edgeSet();
        ArrayList<Dependency> result = new ArrayList<Dependency>();
        for (Dependency dependency : edgeSet) {
            if (dependency.isVisible()) {
                result.add(dependency);
            }
        }
        return result;
    }

    public List<Task> getTopLevelTasks() {
        return Collections.unmodifiableList(topLevelTasks);
    }

    public void childrenAddedTo(TaskContainer task) {
        ParentShrinkingEnforcer parentShrinkingEnforcer = parentShrinkingEnforcerByTask
                .get(task);
        parentShrinkingEnforcer.registerListeners();
        parentShrinkingEnforcer.enforce();
    }

    @Override
    public List<Task> getInitialTasks() {
        List<Task> tasks = new ArrayList<Task>();

        for (Task task : graph.vertexSet()) {
            int dependencies = graph.inDegreeOf(task);
            if ((dependencies == 0)
                    || (dependencies == getNumberOfIncomingDependenciesByType(
                            task, DependencyType.END_END))) {
                tasks.add(task);
            }
        }

        return tasks;
    }

    @Override
    public Dependency getDependencyFrom(Task from, Task to) {
        return graph.getEdge(from, to);
    }

    @Override
    public Set<Task> getOutgoingTasksFor(Task task) {
        Set<Task> tasks = new HashSet<Task>();

        for (Dependency dependency : graph.outgoingEdgesOf(task)) {
            tasks.add(dependency.getDestination());
        }

        return tasks;
    }

    @Override
    public Set<Task> getIncomingTasksFor(Task task) {
        Set<Task> tasks = new HashSet<Task>();

        for (Dependency dependency : graph.incomingEdgesOf(task)) {
            tasks.add(dependency.getSource());
        }

        return tasks;
    }

    @Override
    public List<Task> getLatestTasks() {
        List<Task> tasks = new ArrayList<Task>();

        for (Task task : graph.vertexSet()) {
            int dependencies = graph.outDegreeOf(task);
            if ((dependencies == 0)
                    || (dependencies == getNumberOfOutgoingDependenciesByType(
                            task, DependencyType.START_START))) {
                tasks.add(task);
            }
        }

        return tasks;
    }

    private int getNumberOfIncomingDependenciesByType(Task task,
            DependencyType dependencyType) {
        int count = 0;
        for (Dependency dependency : graph.incomingEdgesOf(task)) {
            if (dependency.getType().equals(dependencyType)) {
                count++;
            }
        }
        return count;
    }

    private int getNumberOfOutgoingDependenciesByType(Task task,
            DependencyType dependencyType) {
        int count = 0;
        for (Dependency dependency : graph.outgoingEdgesOf(task)) {
            if (dependency.getType().equals(dependencyType)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean isContainer(Task task) {
        if (task == null) {
            return false;
        }
        return task.isContainer();
    }

    @Override
    public boolean contains(Task container, Task task) {
        if ((container == null) || (task == null)) {
            return false;
        }
        if (container.isContainer()) {
            return container.getTasks().contains(task);
        }
        return false;
    }

    public boolean doesNotProvokeLoop(Dependency dependency) {
        Set<TaskPoint> reachableFromDestination = getReachableFrom(dependency
                .getDestinationPoint());
        for (TaskPoint each : reachableFromDestination) {
            if (each.sendsModificationsThrough(dependency)) {
                return false;
            }
        }
        return true;
    }

    /**
     * It indicates if the task is modified both the start and end, only the end
     * property or none of the properties
     * @author Óscar González Fernández <ogonzalez@igalia.com>
     */
    enum PointType {
        BOTH, END, NONE;

        public boolean sendsModificationsThrough(DependencyType type) {
            switch (this) {
            case NONE:
                return false;
            case BOTH:
                return true;
            case END:
                return type == DependencyType.END_END || type == DependencyType.END_START;
            default:
                throw new RuntimeException("unexpected value: " + this);
            }
        }
    }

    static class TaskPoint {

        public static TaskPoint both(Task task){
            return new TaskPoint(task, PointType.BOTH);
        }

        public static TaskPoint endOf(Task task) {
            return new TaskPoint(task, PointType.END);
        }

        final Task task;

        final PointType pointType;

        TaskPoint(Task task, PointType pointType) {
            this.task = task;
            this.pointType = pointType;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TaskPoint) {
                TaskPoint other = (TaskPoint) obj;
                return new EqualsBuilder().append(task, other.task).append(
                        pointType, other.pointType).isEquals();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(task).append(pointType).toHashCode();
        }

        public boolean sendsModificationsThrough(Dependency dependency) {
            DependencyType type = dependency.getType();
            return dependency.getSource().equals(task)
                    && pointType.sendsModificationsThrough(type);
        }
    }

    private Set<TaskPoint> getReachableFrom(TaskPoint task) {
        Set<TaskPoint> result = new HashSet<TaskPoint>();
        Queue<TaskPoint> pending = new LinkedList<TaskPoint>();
        result.add(task);
        pending.offer(task);
        while (!pending.isEmpty()) {
            TaskPoint current = pending.poll();
            Set<TaskPoint> immendiate = getImmendiateReachableFrom(current);
            for (TaskPoint each : immendiate) {
                if (!result.contains(each)) {
                    result.add(each);
                    pending.offer(each);
                }
            }
        }
        return result;
    }

    private Set<TaskPoint> getImmendiateReachableFrom(TaskPoint current) {
        Set<TaskPoint> result = new HashSet<TaskPoint>();
        Set<Dependency> outgoingEdgesOf = graph.outgoingEdgesOf(current.task);
        for (Dependency each : outgoingEdgesOf) {
            if (current.sendsModificationsThrough(each)) {
                result.add(each.getDestinationPoint());
            }
        }
        return result;
    }

}

interface IReentranceCases {
    public void ifNewEntrance();
}

class ReentranceGuard {
    private final ThreadLocal<Boolean> inside = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        };
    };

    public void entranceRequested(IReentranceCases reentranceCases) {
        if (inside.get()) {
            return;
        }
        inside.set(true);
        try {
            reentranceCases.ifNewEntrance();
        } finally {
            inside.set(false);
        }
    }
}