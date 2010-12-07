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

package org.zkoss.ganttz.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues;
import org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.ComparisonType;
import org.zkoss.ganttz.data.criticalpath.ICriticalPathCalculable;
import org.zkoss.ganttz.util.IAction;
import org.zkoss.ganttz.util.PreAndPostNotReentrantActionsWrapper;

/**
 * This class contains a graph with the {@link Task tasks} as vertexes and the
 * {@link Dependency dependency} as arcs. It enforces the rules embodied in the
 * dependencies and in the duration of the tasks using listeners. <br/>
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GanttDiagramGraph<V, D extends IDependency<V>> implements
        ICriticalPathCalculable<V> {

    private static final Log LOG = LogFactory.getLog(GanttDiagramGraph.class);

    public static IDependenciesEnforcerHook doNothingHook() {
        return new IDependenciesEnforcerHook() {

            @Override
            public void setNewEnd(GanttDate previousEnd, GanttDate newEnd) {
            }

            @Override
            public void setStartDate(GanttDate previousStart,
                    GanttDate previousEnd, GanttDate newStart) {
            }
        };
    }

    private static final GanttZKAdapter GANTTZK_ADAPTER = new GanttZKAdapter();

    public static IAdapter<Task, Dependency> taskAdapter() {
        return GANTTZK_ADAPTER;
    }

    public interface IAdapter<V, D extends IDependency<V>> {
        List<V> getChildren(V task);

        boolean isContainer(V task);

        void registerDependenciesEnforcerHookOn(V task,
                IDependenciesEnforcerHookFactory<V> hookFactory);

        GanttDate getStartDate(V task);

        void setStartDateFor(V task, GanttDate newStart);

        GanttDate getEndDateFor(V task);

        void setEndDateFor(V task, GanttDate newEnd);

        GanttDate getSmallestBeginDateFromChildrenFor(V container);

        List<Constraint<GanttDate>> getCurrentLenghtConstraintFor(V task);

        List<Constraint<GanttDate>> getEndConstraintsGivenIncoming(
                Set<D> incoming);

        List<Constraint<GanttDate>> getStartConstraintsGiven(
                Set<D> withDependencies);

        List<Constraint<GanttDate>> getStartConstraintsFor(V task);

        List<Constraint<GanttDate>> getEndConstraintsFor(V task);

        V getSource(D dependency);

        V getDestination(D dependency);

        Class<D> getDependencyType();

        D createInvisibleDependency(V origin, V destination, DependencyType type);

        DependencyType getType(D dependency);

        TaskPoint<V, D> getDestinationPoint(D dependency);

        boolean isVisible(D dependency);

        boolean isFixed(V task);

    }

    public static class GanttZKAdapter implements IAdapter<Task, Dependency> {

        @Override
        public List<Task> getChildren(Task task) {
            return task.getTasks();
        }

        @Override
        public Task getDestination(Dependency dependency) {
            return dependency.getDestination();
        }

        @Override
        public Task getSource(Dependency dependency) {
            return dependency.getSource();
        }

        @Override
        public boolean isContainer(Task task) {
            return task.isContainer();
        }

        @Override
        public void registerDependenciesEnforcerHookOn(Task task,
                IDependenciesEnforcerHookFactory<Task> hookFactory) {
            task.registerDependenciesEnforcerHook(hookFactory);
        }

        @Override
        public Dependency createInvisibleDependency(Task origin,
                Task destination, DependencyType type) {
            return new Dependency(origin, destination, type, false);
        }

        @Override
        public Class<Dependency> getDependencyType() {
            return Dependency.class;
        }

        @Override
        public DependencyType getType(Dependency dependency) {
            return dependency.getType();
        }

        @Override
        public TaskPoint<Task, Dependency> getDestinationPoint(
                Dependency dependency) {
            return dependency.getDestinationPoint();
        }

        @Override
        public boolean isVisible(Dependency dependency) {
            return dependency.isVisible();
        }

        @Override
        public GanttDate getEndDateFor(Task task) {
            return task.getEndDate();
        }

        @Override
        public List<Constraint<GanttDate>> getCurrentLenghtConstraintFor(
                Task task) {
            return task.getCurrentLengthConstraint();
        }

        @Override
        public List<Constraint<GanttDate>> getEndConstraintsGivenIncoming(
                Set<Dependency> incoming) {
            return Dependency.getEndConstraints(incoming);
        }

        @Override
        public void setEndDateFor(Task task, GanttDate newEnd) {
            task.setEndDate(newEnd);
        }

        @Override
        public GanttDate getStartDate(Task task) {
            return task.getBeginDate();
        }

        @Override
        public void setStartDateFor(Task task, GanttDate newStart) {
            task.setBeginDate(newStart);
        }

        @Override
        public List<Constraint<GanttDate>> getStartConstraintsGiven(
                Set<Dependency> withDependencies) {
            return Dependency.getStartConstraints(withDependencies);
        }

        @Override
        public List<Constraint<GanttDate>> getStartConstraintsFor(Task task) {
            return task.getStartConstraints();
        }

        @Override
        public List<Constraint<GanttDate>> getEndConstraintsFor(Task task) {
            return task.getEndConstraints();
        }

        @Override
        public GanttDate getSmallestBeginDateFromChildrenFor(Task container) {
            return ((TaskContainer) container)
                    .getSmallestBeginDateFromChildren();
        }

        @Override
        public boolean isFixed(Task task) {
            return task.isFixed();
        }

    }

    public static class GanttZKDiagramGraph extends
            GanttDiagramGraph<Task, Dependency> {

        private GanttZKDiagramGraph(
                List<Constraint<GanttDate>> globalStartConstraints,
                List<Constraint<GanttDate>> globalEndConstraints,
                boolean dependenciesConstraintsHavePriority) {
            super(GANTTZK_ADAPTER, globalStartConstraints,
                    globalEndConstraints,
                    dependenciesConstraintsHavePriority);
        }

    }

    public interface IGraphChangeListener {
        public void execute();
    }

    public static GanttZKDiagramGraph create(
            List<Constraint<GanttDate>> globalStartConstraints,
            List<Constraint<GanttDate>> globalEndConstraints,
            boolean dependenciesConstraintsHavePriority) {
        return new GanttZKDiagramGraph(globalStartConstraints,
                globalEndConstraints, dependenciesConstraintsHavePriority);
    }

    private final IAdapter<V, D> adapter;

    private final DirectedGraph<V, D> graph;

    private List<V> topLevelTasks = new ArrayList<V>();

    private Map<V, V> fromChildToParent = new HashMap<V, V>();

    private final List<Constraint<GanttDate>> globalStartConstraints;

    private final List<Constraint<GanttDate>> globalEndConstraints;

    private DependenciesEnforcer enforcer = new DependenciesEnforcer();

    private final boolean dependenciesConstraintsHavePriority;

    private final ReentranceGuard positionsUpdatingGuard = new ReentranceGuard();

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

    public void addPreChangeListeners(
            Collection<? extends IGraphChangeListener> preChangeListeners) {
        for (IGraphChangeListener each : preChangeListeners) {
            addPreGraphChangeListener(each);
        }
    }

    public void addPostChangeListeners(
            Collection<? extends IGraphChangeListener> postChangeListeners) {
        for (IGraphChangeListener each : postChangeListeners) {
            addPostGraphChangeListener(each);
        }
    }

    public static <V, D extends IDependency<V>> GanttDiagramGraph<V, D> create(
            IAdapter<V, D> adapter,
            List<Constraint<GanttDate>> globalStartConstraints,
            List<Constraint<GanttDate>> globalEndConstraints,
            boolean dependenciesConstraintsHavePriority) {
        return new GanttDiagramGraph<V, D>(adapter, globalStartConstraints,
                globalEndConstraints, dependenciesConstraintsHavePriority);
    }

    protected GanttDiagramGraph(IAdapter<V, D> adapter,
            List<Constraint<GanttDate>> globalStartConstraints,
            List<Constraint<GanttDate>> globalEndConstraints,
            boolean dependenciesConstraintsHavePriority) {
        this.adapter = adapter;
        this.globalStartConstraints = globalStartConstraints;
        this.globalEndConstraints = globalEndConstraints;
        this.dependenciesConstraintsHavePriority = dependenciesConstraintsHavePriority;
        this.graph = new SimpleDirectedGraph<V, D>(adapter.getDependencyType());
    }

    public void enforceAllRestrictions() {
        enforcer.enforceRestrictionsOn(getTopLevelTasks());
    }

    public void addTopLevel(V task) {
        topLevelTasks.add(task);
        addTask(task);
    }

    public void addTopLevel(Collection<? extends V> tasks) {
        for (V task : tasks) {
            addTopLevel(task);
        }
    }

    public void addTasks(Collection<? extends V> tasks) {
        for (V t : tasks) {
            addTask(t);
        }
    }

    public void addTask(V original) {
        List<V> stack = new LinkedList<V>();
        stack.add(original);
        List<D> dependenciesToAdd = new ArrayList<D>();
        while (!stack.isEmpty()){
            V task = stack.remove(0);
            graph.addVertex(task);
            adapter.registerDependenciesEnforcerHookOn(task, enforcer);
            if (adapter.isContainer(task)) {
                for (V child : adapter.getChildren(task)) {
                    fromChildToParent.put(child, task);
                    stack.add(0, child);
                    dependenciesToAdd.add(adapter.createInvisibleDependency(
                            child, task, DependencyType.END_END));
                    dependenciesToAdd.add(adapter.createInvisibleDependency(
                            task, child, DependencyType.START_START));
                }
            }
        }
        for (D each : dependenciesToAdd) {
            add(each, false);
        }
    }

    public interface IDependenciesEnforcerHook {
        public void setStartDate(GanttDate previousStart,
                GanttDate previousEnd, GanttDate newStart);

        public void setNewEnd(GanttDate previousEnd, GanttDate newEnd);
    }

    public interface IDependenciesEnforcerHookFactory<T> {
        public IDependenciesEnforcerHook create(T task,
                INotificationAfterDependenciesEnforcement notification);

        public IDependenciesEnforcerHook create(T task);
    }

    public interface INotificationAfterDependenciesEnforcement {
        public void onStartDateChange(GanttDate previousStart,
                GanttDate previousEnd, GanttDate newStart);

        public void onEndDateChange(GanttDate previousEnd, GanttDate newEnd);
    }

    private static final INotificationAfterDependenciesEnforcement EMPTY_NOTIFICATOR  = new INotificationAfterDependenciesEnforcement() {

        @Override
        public void onStartDateChange(GanttDate previousStart,
                GanttDate previousEnd, GanttDate newStart) {
        }

        @Override
        public void onEndDateChange(GanttDate previousEnd, GanttDate newEnd) {
        }
    };

    public class DeferedNotifier {

        private Map<V, NotificationPendingForTask> notificationsPending = new LinkedHashMap<V, NotificationPendingForTask>();

        public void add(V task, StartDateNofitication notification) {
            retrieveOrCreateFor(task).setStartDateNofitication(notification);
        }

        private NotificationPendingForTask retrieveOrCreateFor(V task) {
            NotificationPendingForTask result = notificationsPending.get(task);
            if (result == null) {
                result = new NotificationPendingForTask();
                notificationsPending.put(task, result);
            }
            return result;
        }

        void add(V task, LengthNotification notification) {
            retrieveOrCreateFor(task).setLengthNofitication(notification);
        }

        public void doNotifications() {
            for (NotificationPendingForTask each : notificationsPending
                    .values()) {
                each.doNotification();
            }
            notificationsPending.clear();
        }

    }

    private class NotificationPendingForTask {
        private StartDateNofitication startDateNofitication;

        private LengthNotification lengthNofitication;

        void setStartDateNofitication(
                StartDateNofitication startDateNofitication) {
            this.startDateNofitication = this.startDateNofitication == null ? startDateNofitication
                    : this.startDateNofitication
                            .coalesce(startDateNofitication);
        }

        void setLengthNofitication(LengthNotification lengthNofitication) {
            this.lengthNofitication = this.lengthNofitication == null ? lengthNofitication
                    : this.lengthNofitication.coalesce(lengthNofitication);
        }

        void doNotification() {
            if (startDateNofitication != null) {
                startDateNofitication.doNotification();
            }
            if (lengthNofitication != null) {
                lengthNofitication.doNotification();
            }
        }
    }

    private class StartDateNofitication {

        private final INotificationAfterDependenciesEnforcement notification;
        private final GanttDate previousStart;
        private final GanttDate previousEnd;
        private final GanttDate newStart;

        public StartDateNofitication(
                INotificationAfterDependenciesEnforcement notification,
                GanttDate previousStart, GanttDate previousEnd,
                GanttDate newStart) {
            this.notification = notification;
            this.previousStart = previousStart;
            this.previousEnd = previousEnd;
            this.newStart = newStart;
        }

        public StartDateNofitication coalesce(
                StartDateNofitication startDateNofitication) {
            return new StartDateNofitication(notification, previousStart,
                    previousEnd, startDateNofitication.newStart);
        }

        void doNotification() {
            notification
                    .onStartDateChange(previousStart, previousEnd, newStart);
        }
    }

    private class LengthNotification {

        private final INotificationAfterDependenciesEnforcement notification;
        private final GanttDate previousEnd;
        private final GanttDate newEnd;

        public LengthNotification(
                INotificationAfterDependenciesEnforcement notification,
                GanttDate previousEnd, GanttDate newEnd) {
            this.notification = notification;
            this.previousEnd = previousEnd;
            this.newEnd = newEnd;

        }

        public LengthNotification coalesce(LengthNotification lengthNofitication) {
            return new LengthNotification(notification, previousEnd,
                    lengthNofitication.newEnd);
        }

        void doNotification() {
            notification.onEndDateChange(previousEnd, newEnd);
        }
    }

    private class DependenciesEnforcer implements
            IDependenciesEnforcerHookFactory<V> {

        private ThreadLocal<DeferedNotifier> deferedNotifier = new ThreadLocal<DeferedNotifier>();

        @Override
        public IDependenciesEnforcerHook create(V task,
                INotificationAfterDependenciesEnforcement notificator) {
            return onlyEnforceDependenciesOnEntrance(onEntrance(task),
                    onNotification(task, notificator));
        }

        @Override
        public IDependenciesEnforcerHook create(V task) {
            return create(task, EMPTY_NOTIFICATOR);
        }

        private IDependenciesEnforcerHook onEntrance(final V task) {
            return new IDependenciesEnforcerHook() {

                public void setStartDate(GanttDate previousStart,
                        GanttDate previousEnd, GanttDate newStart) {
                    taskPositionModified(task);
                }

                @Override
                public void setNewEnd(GanttDate previousEnd, GanttDate newEnd) {
                    taskPositionModified(task);
                }

            };
        }

        private IDependenciesEnforcerHook onNotification(final V task,
                final INotificationAfterDependenciesEnforcement notification) {
            return new IDependenciesEnforcerHook() {

                @Override
                public void setStartDate(GanttDate previousStart,
                        GanttDate previousEnd, GanttDate newStart) {
                    StartDateNofitication startDateNotification = new StartDateNofitication(
                            notification, previousStart, previousEnd,
                            newStart);
                    deferedNotifier.get().add(task, startDateNotification);

                }

                @Override
                public void setNewEnd(GanttDate previousEnd, GanttDate newEnd) {
                    LengthNotification lengthNotification = new LengthNotification(
                            notification, previousEnd, newEnd);
                    deferedNotifier.get().add(task, lengthNotification);
                }
            };

        }

        private IDependenciesEnforcerHook onlyEnforceDependenciesOnEntrance(
                final IDependenciesEnforcerHook onEntrance,
                final IDependenciesEnforcerHook notification) {
            return new IDependenciesEnforcerHook() {

                @Override
                public void setStartDate(final GanttDate previousStart,
                        final GanttDate previousEnd, final GanttDate newStart) {
                    positionsUpdatingGuard
                            .entranceRequested(new IReentranceCases() {

                                @Override
                                public void ifNewEntrance() {
                                    onNewEntrance(new IAction() {

                                        @Override
                                        public void doAction() {
                                            notification.setStartDate(
                                                    previousStart,
                                                    previousEnd, newStart);
                                            onEntrance.setStartDate(
                                                    previousStart, previousEnd,
                                                    newStart);
                                        }
                                    });
                                }

                                @Override
                                public void ifAlreadyInside() {
                                    notification.setStartDate(previousStart,
                                            previousEnd, newStart);

                                }
                            });
                }

                @Override
                public void setNewEnd(final GanttDate previousEnd,
                        final GanttDate newEnd) {
                    positionsUpdatingGuard
                            .entranceRequested(new IReentranceCases() {

                                @Override
                                public void ifNewEntrance() {
                                    onNewEntrance(new IAction() {

                                        @Override
                                        public void doAction() {
                                            notification.setNewEnd(previousEnd,
                                                    newEnd);
                                            onEntrance.setNewEnd(previousEnd,
                                                    newEnd);
                                        }
                                    });
                                }

                                @Override
                                public void ifAlreadyInside() {
                                    notification.setNewEnd(previousEnd, newEnd);
                                }
                            });
                }
            };

        }

        void enforceRestrictionsOn(Collection<? extends V> tasks) {
            List<Recalculation> allRecalculations = new ArrayList<Recalculation>();
            for (V each : tasks) {
                allRecalculations.addAll(getRecalculationsNeededFrom(each));
            }
            enforceRestrictionsOn(allRecalculations, tasks);
        }

        void enforceRestrictionsOn(V task) {
            enforceRestrictionsOn(getRecalculationsNeededFrom(task),
                    Collections.singleton(task));
        }

        void enforceRestrictionsOn(final List<Recalculation> recalculations,
                final Collection<? extends V> initiallyModified) {
            executeWithPreAndPostActionsOnlyIfNewEntrance(new IAction() {
                @Override
                public void doAction() {
                    doRecalculations(recalculations, initiallyModified);
                }
            });
        }

        private void executeWithPreAndPostActionsOnlyIfNewEntrance(
                final IAction action) {
            positionsUpdatingGuard.entranceRequested(new IReentranceCases() {

                @Override
                public void ifAlreadyInside() {
                    action.doAction();
                }

                @Override
                public void ifNewEntrance() {
                    onNewEntrance(action);
                }
            });
        }

        private void onNewEntrance(final IAction action) {
            preAndPostActions.doAction(decorateWithNotifications(action));
        }

        private IAction decorateWithNotifications(final IAction action) {
            return new IAction() {

                @Override
                public void doAction() {
                    deferedNotifier.set(new DeferedNotifier());
                    try {
                        action.doAction();
                    } finally {
                        DeferedNotifier notifier = deferedNotifier.get();
                        notifier.doNotifications();
                        deferedNotifier.set(null);
                    }
                }
            };
        }

        DeferedNotifier manualNotification(final IAction action) {
            final DeferedNotifier result = new DeferedNotifier();
            positionsUpdatingGuard.entranceRequested(new IReentranceCases() {

                @Override
                public void ifAlreadyInside() {
                    throw new RuntimeException("it cannot do a manual notification if it's already inside");
                }

                @Override
                public void ifNewEntrance() {
                    preAndPostActions.doAction(new IAction() {

                        @Override
                        public void doAction() {
                            deferedNotifier.set(result);
                            try {
                                action.doAction();
                            } finally {
                                deferedNotifier.set(null);
                            }
                        }
                    });
                }
            });
            return result;
        }

        private void taskPositionModified(final V task) {
            executeWithPreAndPostActionsOnlyIfNewEntrance(new IAction() {
                @Override
                public void doAction() {
                    List<Recalculation> recalculationsNeededFrom = getRecalculationsNeededFrom(task);
                    doRecalculations(recalculationsNeededFrom,
                            Collections.singletonList(task));
                }
            });
        }

        private void doRecalculations(List<Recalculation> recalculationsNeeded,
                Collection<? extends V> initiallyModified) {
            Set<V> allModified = new HashSet<V>();
            allModified.addAll(initiallyModified);
            for (Recalculation each : recalculationsNeeded) {
                boolean modified = each.doRecalculation();
                if (modified) {
                    allModified.add(each.taskPoint.task);
                }
            }
            List<V> shrunkContainers = shrunkContainersOfModified(allModified);
            for (V each : getTaskAffectedByShrinking(shrunkContainers)) {
                doRecalculations(getRecalculationsNeededFrom(each),
                        Collections.singletonList(each));
            }
        }

        private List<V> getTaskAffectedByShrinking(List<V> shrunkContainers) {
            List<V> tasksAffectedByShrinking = new ArrayList<V>();
            for (V each : shrunkContainers) {
                for (D eachDependency : graph.outgoingEdgesOf(each)) {
                    if (adapter.getType(eachDependency) == DependencyType.START_START
                            && adapter.isVisible(eachDependency)) {
                        tasksAffectedByShrinking.add(adapter
                                .getDestination(eachDependency));
                    }
                }
            }
            return tasksAffectedByShrinking;
        }

        private List<V> shrunkContainersOfModified(
                Set<V> allModified) {
            Set<V> topmostToShrink = getTopMostThatCouldPotentiallyNeedShrinking(allModified);
            List<V> allToShrink = new ArrayList<V>();
            for (V each : topmostToShrink) {
                allToShrink.addAll(getContainersBottomUp(each));
            }
            List<V> result = new ArrayList<V>();
            for (V each : allToShrink) {
                boolean modified = enforceParentShrinkage(each);
                if (modified) {
                    result.add(each);
                }
            }
            return result;
        }

        private Set<V> getTopMostThatCouldPotentiallyNeedShrinking(
                Collection<V> modified) {
            Set<V> result = new HashSet<V>();
            for (V each : modified) {
                V t = getTopmostFor(each);
                if (adapter.isContainer(t)) {
                    result.add(t);
                }
            }
            return result;
        }

        private Collection<? extends V> getContainersBottomUp(
                V container) {
            List<V> result = new ArrayList<V>();
            List<V> tasks = adapter.getChildren(container);
            for (V each : tasks) {
                if (adapter.isContainer(each)) {
                    result.addAll(getContainersBottomUp(each));
                    result.add(each);
                }
            }
            result.add(container);
            return result;
        }

        boolean enforceParentShrinkage(V container) {
            GanttDate oldBeginDate = adapter.getStartDate(container);
            GanttDate firstStart = adapter
                    .getSmallestBeginDateFromChildrenFor(container);
            GanttDate previousEnd = adapter.getEndDateFor(container);
            if (firstStart.after(oldBeginDate)) {
                adapter.setStartDateFor(container, firstStart);
                adapter.setEndDateFor(container, previousEnd);
                return true;
            }
            return false;
        }
    }

    List<Recalculation> getRecalculationsNeededFrom(V task) {
        List<Recalculation> result = new LinkedList<Recalculation>();
        Set<Recalculation> parentRecalculationsAlreadyDone = new HashSet<Recalculation>();
        Recalculation first = recalculationFor(TaskPoint.both(adapter, task));
        first.couldHaveBeenModifiedBeforehand();
        Queue<Recalculation> pendingOfNavigate = new LinkedList<Recalculation>();
        result.addAll(getParentsRecalculations(parentRecalculationsAlreadyDone,
                first.taskPoint));
        result.add(first);
        pendingOfNavigate.offer(first);
        while (!pendingOfNavigate.isEmpty()) {
            Recalculation current = pendingOfNavigate.poll();
            for (TaskPoint<V, D> each : getImmendiateReachableFrom(current.taskPoint)) {
                Recalculation recalculationToAdd = recalculationFor(each);
                ListIterator<Recalculation> listIterator = result
                        .listIterator();
                while (listIterator.hasNext()) {
                    Recalculation previous = listIterator.next();
                    if (previous.equals(recalculationToAdd)) {
                        listIterator.remove();
                        recalculationToAdd = previous;
                        break;
                    }
                }
                recalculationToAdd.comesFromPredecessor(current);
                result.addAll(getParentsRecalculations(
                        parentRecalculationsAlreadyDone, each));
                result.add(recalculationToAdd);
                pendingOfNavigate.offer(recalculationToAdd);
            }
        }
        return result;
    }

    private List<Recalculation> getParentsRecalculations(
            Set<Recalculation> parentRecalculationsAlreadyDone,
            TaskPoint<V, D> taskPoint) {
        List<Recalculation> result = new ArrayList<Recalculation>();
        for (TaskPoint<V, D> eachParent : parentsRecalculationsNeededFor(taskPoint)) {
            Recalculation parentRecalculation = parentRecalculation(eachParent.task);
            if (!parentRecalculationsAlreadyDone
                    .contains(parentRecalculation)) {
                parentRecalculationsAlreadyDone.add(parentRecalculation);
                result.add(parentRecalculation);
            }
        }
        return result;
    }

    private Set<TaskPoint<V, D>> parentsRecalculationsNeededFor(
            TaskPoint<V, D> current) {
        Set<TaskPoint<V, D>> result = new LinkedHashSet<TaskPoint<V, D>>();
        if (current.pointType == PointType.BOTH) {
            List<V> path = fromTaskToTop(current.task);
            if (path.size() > 1) {
                path = path.subList(1, path.size());
                Collections.reverse(path);
                result.addAll(asBothPoints(path));
            }
        }
        return result;
    }

    private Collection<? extends TaskPoint<V, D>> asBothPoints(List<V> parents) {
        List<TaskPoint<V, D>> result = new ArrayList<TaskPoint<V, D>>();
        for (V each : parents) {
            result.add(TaskPoint.both(adapter, each));
        }
        return result;
    }

    private List<V> fromTaskToTop(V task) {
        List<V> result = new ArrayList<V>();
        V current = task;
        while (current != null) {
            result.add(current);
            current = fromChildToParent.get(current);
        }
        return result;
    }

    private Recalculation parentRecalculation(V task) {
        return new Recalculation(TaskPoint.both(adapter, task), true);
    }

    private Recalculation recalculationFor(TaskPoint<V, D> taskPoint) {
        return new Recalculation(taskPoint, false);
    }

    private class Recalculation {

        private final boolean parentRecalculation;

        private final TaskPoint<V, D> taskPoint;

        private Set<Recalculation> recalculationsCouldAffectThis = new HashSet<Recalculation>();

        private boolean recalculationCalled = false;

        private boolean dataPointModified = false;

        private boolean couldHaveBeenModifiedBeforehand = false;

        Recalculation(TaskPoint<V, D> taskPoint, boolean isParentRecalculation) {
            Validate.notNull(taskPoint);
            this.taskPoint = taskPoint;
            this.parentRecalculation = isParentRecalculation;
        }

        public void couldHaveBeenModifiedBeforehand() {
            couldHaveBeenModifiedBeforehand = true;
        }

        public void comesFromPredecessor(Recalculation predecessor) {
            recalculationsCouldAffectThis.add(predecessor);
        }

        boolean doRecalculation() {
            recalculationCalled = true;
            dataPointModified = haveToDoCalculation()
                    && taskChangesPosition();
            return dataPointModified;
        }

        private boolean haveToDoCalculation() {
            return (recalculationsCouldAffectThis.isEmpty() || parentsHaveBeenModified());
        }

        private boolean parentsHaveBeenModified() {
            for (Recalculation each : recalculationsCouldAffectThis) {
                if (!each.recalculationCalled) {
                    throw new RuntimeException(
                            "the parent must be called first");
                }
                if (each.dataPointModified
                        || each.couldHaveBeenModifiedBeforehand) {
                    return true;
                }
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        private boolean taskChangesPosition() {
            ChangeTracker tracker = trackTaskChanges();
            Constraint.initialValue(noRestrictions())
                    .withConstraints(new BackwardsForces(), new ForwardForces())
                    .apply();
            return tracker.taskHasChanged();
        }


        abstract class PositionRestrictions {

            abstract List<Constraint<GanttDate>> getStartConstraints();

            abstract List<Constraint<GanttDate>> getEndConstraints();

            abstract boolean satisfies(PositionRestrictions other);

        }


        private final class NoRestrictions extends PositionRestrictions {
            @Override
            List<Constraint<GanttDate>> getStartConstraints() {
                return Collections.emptyList();
            }

            @Override
            List<Constraint<GanttDate>> getEndConstraints() {
                return Collections.emptyList();
            }

            @Override
            boolean satisfies(PositionRestrictions restrictions) {
                return true;
            }
        }

        PositionRestrictions noRestrictions() {
            return new NoRestrictions();
        }

        DatesBasedPositionRestrictions biggerThan(GanttDate start, GanttDate end) {
            return new DatesBasedPositionRestrictions(
                    ComparisonType.BIGGER_OR_EQUAL_THAN, start, end);
        }

        DatesBasedPositionRestrictions lessThan(GanttDate start, GanttDate end) {
            return new DatesBasedPositionRestrictions(
                    ComparisonType.LESS_OR_EQUAL_THAN_RIGHT_FLOATING, start,
                    end);
        }

        class DatesBasedPositionRestrictions extends PositionRestrictions {

            private Constraint<GanttDate> startConstraint;
            private Constraint<GanttDate> endConstraint;
            private final GanttDate start;
            private final GanttDate end;

            public DatesBasedPositionRestrictions(
                    ComparisonType comparisonType, GanttDate start,
                    GanttDate end) {
                this.start = start;
                this.end = end;
                this.startConstraint = ConstraintOnComparableValues
                        .instantiate(comparisonType, start);
                this.endConstraint = ConstraintOnComparableValues
                        .biggerOrEqualThan(end);
            }

            boolean satisfies(PositionRestrictions other) {
                if (DatesBasedPositionRestrictions.class.isInstance(other)) {
                    return satisfies(DatesBasedPositionRestrictions.class
                            .cast(other));
                }
                return false;
            }

            private boolean satisfies(DatesBasedPositionRestrictions other) {
                return startConstraint.isSatisfiedBy(other.start)
                        && endConstraint.isSatisfiedBy(other.end);
            }

            @Override
            List<Constraint<GanttDate>> getStartConstraints() {
                return Collections.singletonList(startConstraint);
            }

            @Override
            List<Constraint<GanttDate>> getEndConstraints() {
                return Collections.singletonList(endConstraint);
            }

        }

        class ChangeTracker {
            private GanttDate start;
            private GanttDate end;
            private final V task;

            public ChangeTracker(V task) {
                this.task = task;
                this.start = adapter.getStartDate(task);
                this.end = adapter.getEndDateFor(task);
            }

            public boolean taskHasChanged() {
                return areNotEqual(adapter.getStartDate(task), this.start)
                        || areNotEqual(adapter.getEndDateFor(task), this.end);
            }

        }

        boolean areNotEqual(GanttDate a, GanttDate b) {
            return a != b && a.compareTo(b) != 0;
        }

        protected ChangeTracker trackTaskChanges() {
            return new ChangeTracker(taskPoint.task);
        }


        abstract class Forces extends Constraint<PositionRestrictions> {

            protected final V task;

            protected final PointType pointType;

            public Forces() {
                this.pointType = taskPoint.pointType;
                this.task = taskPoint.task;
            }

            protected PositionRestrictions result;

            protected PositionRestrictions applyConstraintTo(
                    PositionRestrictions restrictions) {
                if (adapter.isFixed(task)) {
                    return restrictions;
                }
                result = enforceUsingPreviousRestrictions(restrictions);
                return result;
            }

            public boolean isSatisfiedBy(PositionRestrictions value) {
                return result.satisfies(value);
            }

            public void checkSatisfiesResult(PositionRestrictions finalResult) {
                super.checkSatisfiesResult(finalResult);
                if (DatesBasedPositionRestrictions.class
                        .isInstance(finalResult)) {
                    checkConstraintsAgainst(DatesBasedPositionRestrictions.class
                            .cast(finalResult));
                }
            }

            private void checkConstraintsAgainst(
                    DatesBasedPositionRestrictions finalResult) {
                checkStartConstraints(finalResult.start);
                checkEndConstraints(finalResult.end);
            }

            private void checkStartConstraints(GanttDate finalStart) {
                Constraint
                        .checkSatisfyResult(getStartConstraints(), finalStart);
            }


            private void checkEndConstraints(GanttDate finalEnd) {
                Constraint.checkSatisfyResult(getEndConstraints(), finalEnd);
            }

            abstract List<Constraint<GanttDate>> getStartConstraints();

            abstract List<Constraint<GanttDate>> getEndConstraints();

            abstract PositionRestrictions enforceUsingPreviousRestrictions(
                    PositionRestrictions restrictions);
        }

        class ForwardForces extends Forces {

            @Override
            PositionRestrictions enforceUsingPreviousRestrictions(
                    PositionRestrictions restrictions) {
                switch (pointType) {
                case BOTH:
                    return enforceStartAndEnd(restrictions);
                case END:
                    return enforceEndDate(restrictions);
                default:
                    return restrictions;
                }
            }

            private PositionRestrictions enforceStartAndEnd(
                    PositionRestrictions restrictions) {
                ChangeTracker changeTracker = trackTaskChanges();
                PositionRestrictions currentRestrictions = enforceStartDate(restrictions);
                if (changeTracker.taskHasChanged() || parentRecalculation
                        || couldHaveBeenModifiedBeforehand) {
                    return enforceEndDate(currentRestrictions);
                }
                return currentRestrictions;
            }

            private PositionRestrictions enforceStartDate(
                    PositionRestrictions originalRestrictions) {
                GanttDate newStart = calculateStartDate(originalRestrictions);
                return enforceRestrictionsForStartIfNeeded(newStart);
            }

            private PositionRestrictions enforceRestrictionsForStartIfNeeded(
                    GanttDate newStart) {
                GanttDate old = adapter.getStartDate(task);
                if (areNotEqual(old, newStart)) {
                    adapter.setStartDateFor(task, newStart);
                }
                return biggerThan(newStart, adapter.getEndDateFor(task));
            }

            /**
             * Calculates the new start date based on the present constraints.
             * If there are no constraints this method will return the existent
             * start date value.
             * @param originalRestrictions
             */
            private GanttDate calculateStartDate(
                    PositionRestrictions originalRestrictions) {
                GanttDate  newStart = Constraint
                            .<GanttDate> initialValue(null)
                            .withConstraints(originalRestrictions.getStartConstraints())
                            .withConstraints(getStartConstraints())
                        .applyWithoutFinalCheck();
                if (newStart == null) {
                    return adapter.getStartDate(task);
                }
                return newStart;
            }

            @Override
            List<Constraint<GanttDate>> getStartConstraints() {
                List<Constraint<GanttDate>> result = new ArrayList<Constraint<GanttDate>>();
                if (dependenciesConstraintsHavePriority) {
                    result.addAll(adapter.getStartConstraintsFor(task));
                    result.addAll(getDependenciesCosntraintsForStart());

                } else {
                    result.addAll(getDependenciesCosntraintsForStart());
                    result.addAll(adapter.getStartConstraintsFor(task));
                }
                result.addAll(globalStartConstraints);
                return result;
            }

            private List<Constraint<GanttDate>> getDependenciesCosntraintsForStart() {
                final Set<D> withDependencies = graph.incomingEdgesOf(task);
                return adapter.getStartConstraintsGiven(withDependencies);
            }

            private PositionRestrictions enforceEndDate(
                    PositionRestrictions restrictions) {
                GanttDate newEnd = Constraint
                        .<GanttDate> initialValue(null)
                        .withConstraints(restrictions.getEndConstraints())
                        .withConstraints(getEndConstraints())
                        .applyWithoutFinalCheck();
                if (newEnd == null) {
                    return restrictions;
                }
                restrictions = enforceRestrictionsForEndIfNeeded(newEnd);
                if (pointType == PointType.END) {
                    // start constraints could be the ones "commanding" now
                    GanttDate newStart = calculateStartDate(restrictions);
                    if (newStart.compareTo(adapter.getStartDate(task)) > 0) {
                        return enforceRestrictionsForStartIfNeeded(newStart);
                    }
                }
                return restrictions;
            }

            @Override
            List<Constraint<GanttDate>> getEndConstraints() {
                Set<D> incoming = graph.incomingEdgesOf(task);
                return adapter.getEndConstraintsGivenIncoming(incoming);
            }

            private PositionRestrictions enforceRestrictionsForEndIfNeeded(
                    GanttDate newEnd) {
                GanttDate previousEndDate = adapter.getEndDateFor(task);
                if (areNotEqual(previousEndDate, newEnd)) {
                    adapter.setEndDateFor(task, newEnd);
                }
                return biggerThan(adapter.getStartDate(task), newEnd);
            }

        }

        class BackwardsForces extends Forces {

            @Override
            PositionRestrictions enforceUsingPreviousRestrictions(
                    PositionRestrictions restrictions) {
                GanttDate result = Constraint.<GanttDate> initialValue(null)
                        .withConstraints(restrictions.getEndConstraints())
                        .withConstraints(getEndConstraints())
                        .applyWithoutFinalCheck();
                if (result != null) {
                    return enforceRestrictions(result);
                }
                return restrictions;
            }

            @Override
            List<Constraint<GanttDate>> getStartConstraints() {
                return Collections.emptyList();
            }

            @Override
            List<Constraint<GanttDate>> getEndConstraints() {
                return adapter.getEndConstraintsFor(task);
            }

            private PositionRestrictions enforceRestrictions(GanttDate newEnd) {
                adapter.setEndDateFor(task, newEnd);
                return lessThan(adapter.getStartDate(task), newEnd);
            }

        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                            .append(parentRecalculation)
                            .append(taskPoint)
                            .toHashCode();
        }

        @Override
        public String toString() {
            return String.format("%s, parentRecalculation: %s, parents: %s",
                    taskPoint, parentRecalculation,
                    asSimpleString(recalculationsCouldAffectThis));
        }

        private String asSimpleString(
                Collection<? extends Recalculation> recalculations) {
            StringBuilder result = new StringBuilder();
            result.append("[");
            for (Recalculation each : recalculations) {
                result.append(each.taskPoint).append(", ");
            }
            result.append("]");
            return result.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (Recalculation.class.isInstance(obj)) {
                Recalculation other = Recalculation.class.cast(obj);
                return new EqualsBuilder().append(parentRecalculation, other.parentRecalculation)
                                          .append(taskPoint, other.taskPoint)
                                          .isEquals();
            }
            return false;
        }
    }

    public void remove(final V task) {
        Set<V> needingEnforcing = getOutgoingTasksFor(task);
        graph.removeVertex(task);
        topLevelTasks.remove(task);
        fromChildToParent.remove(task);
        if (adapter.isContainer(task)) {
            for (V t : adapter.getChildren(task)) {
                remove(t);
            }
        }
        enforcer.enforceRestrictionsOn(needingEnforcing);
    }

    public void removeDependency(D dependency) {
        graph.removeEdge(dependency);
        V destination = adapter.getDestination(dependency);
        enforcer.enforceRestrictionsOn(destination);
    }

    public boolean canAddDependency(D dependency) {
        return !isForbidden(dependency) && doesNotProvokeLoop(dependency);
    }

    private boolean isForbidden(D dependency) {
        if (!adapter.isVisible(dependency)) {
            // the invisible dependencies, the ones used to implement container
            // behavior are not forbidden
            return false;
        }

        boolean endEndDependency = DependencyType.END_END == dependency
                .getType();
        boolean startStartDependency = DependencyType.START_START == dependency
                .getType();

        V source = adapter.getSource(dependency);
        V destination = adapter.getDestination(dependency);
        boolean destinationIsContainer = adapter.isContainer(destination);
        boolean sourceIsContainer = adapter.isContainer(source);

        return (destinationIsContainer && endEndDependency)
                || (sourceIsContainer && startStartDependency);
    }


    public void add(D dependency) {
        add(dependency, true);
    }

    public void addWithoutEnforcingConstraints(D dependency) {
        add(dependency, false);
    }

    private void add(D dependency, boolean enforceRestrictions) {
        if (isForbidden(dependency)) {
            return;
        }
        V source = adapter.getSource(dependency);
        V destination = adapter.getDestination(dependency);
        graph.addEdge(source, destination, dependency);
        if (enforceRestrictions) {
            enforceRestrictions(destination);
        }
    }

    public void enforceRestrictions(final V task) {
        enforcer.taskPositionModified(task);
    }

    public DeferedNotifier manualNotificationOn(IAction action) {
        return enforcer.manualNotification(action);
    }

    public boolean contains(D dependency) {
        return graph.containsEdge(dependency);
    }

    public List<V> getTasks() {
        return new ArrayList<V>(graph.vertexSet());
    }

    public List<D> getVisibleDependencies() {
        ArrayList<D> result = new ArrayList<D>();
        for (D dependency : graph.edgeSet()) {
            if (adapter.isVisible(dependency)) {
                result.add(dependency);
            }
        }
        return result;
    }

    public List<V> getTopLevelTasks() {
        return Collections.unmodifiableList(topLevelTasks);
    }

    public void childrenAddedTo(V task) {
        enforcer.enforceRestrictionsOn(task);
    }

    public List<V> getInitialTasks() {
        List<V> result = new ArrayList<V>();
        for (V task : graph.vertexSet()) {
            int dependencies = graph.inDegreeOf(task);
            if ((dependencies == 0)
                    || (dependencies == getNumberOfIncomingDependenciesByType(
                            task, DependencyType.END_END))) {
                result.add(task);
            }
        }
        return result;
    }

    public IDependency<V> getDependencyFrom(V from, V to) {
        return graph.getEdge(from, to);
    }

    public Set<V> getOutgoingTasksFor(V task) {
        Set<V> result = new HashSet<V>();
        for (D dependency : graph.outgoingEdgesOf(task)) {
            result.add(adapter.getDestination(dependency));
        }
        return result;
    }

    public Set<V> getIncomingTasksFor(V task) {
        Set<V> result = new HashSet<V>();
        for (D dependency : graph.incomingEdgesOf(task)) {
            result.add(adapter.getSource(dependency));
        }
        return result;
    }

    public List<V> getLatestTasks() {
        List<V> tasks = new ArrayList<V>();

        for (V task : graph.vertexSet()) {
            int dependencies = graph.outDegreeOf(task);
            if ((dependencies == 0)
                    || (dependencies == getNumberOfOutgoingDependenciesByType(
                            task, DependencyType.START_START))) {
                tasks.add(task);
            }
        }

        return tasks;
    }

    private int getNumberOfIncomingDependenciesByType(V task,
            DependencyType dependencyType) {
        int count = 0;
        for (D dependency : graph.incomingEdgesOf(task)) {
            if (adapter.getType(dependency).equals(dependencyType)) {
                count++;
            }
        }
        return count;
    }

    private int getNumberOfOutgoingDependenciesByType(V task,
            DependencyType dependencyType) {
        int count = 0;
        for (D dependency : graph.outgoingEdgesOf(task)) {
            if (adapter.getType(dependency).equals(dependencyType)) {
                count++;
            }
        }
        return count;
    }

    public boolean isContainer(V task) {
        if (task == null) {
            return false;
        }
        return adapter.isContainer(task);
    }

    public boolean contains(V container, V task) {
        if ((container == null) || (task == null)) {
            return false;
        }
        if (adapter.isContainer(container)) {
            return adapter.getChildren(container).contains(task);
        }
        return false;
    }

    public boolean doesNotProvokeLoop(D dependency) {
        Set<TaskPoint<V, D>> reachableFromDestination = getReachableFrom(adapter
                .getDestinationPoint(dependency));
        for (TaskPoint<V, D> each : reachableFromDestination) {
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
    public enum PointType {
        BOTH, END, NONE;

        public <V> boolean sendsModificationsThrough(IAdapter<V, ?> adapter,
                V source, DependencyType type) {
            if (!adapter.isContainer(source)) {
                return true;
            }
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

    public static class TaskPoint<T, D extends IDependency<T>> {

        public static <T, D extends IDependency<T>> TaskPoint<T, D> both(
                IAdapter<T, D> adapter, T task) {
            return new TaskPoint<T, D>(adapter, task, PointType.BOTH);
        }

        public static <T, D extends IDependency<T>> TaskPoint<T, D> endOf(
                IAdapter<T, D> adapter,
                T task) {
            return new TaskPoint<T, D>(adapter, task, PointType.END);
        }

        final T task;

        final PointType pointType;

        private final IAdapter<T, D> adapter;

        public TaskPoint(IAdapter<T, D> adapter, T task, PointType pointType) {
            this.adapter = adapter;
            this.task = task;
            this.pointType = pointType;
        }

        @Override
        public String toString() {
            return String.format("%s(%s)", task, pointType);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TaskPoint<?, ?>) {
                TaskPoint<?, ?> other = (TaskPoint<?, ?>) obj;
                return new EqualsBuilder().append(task, other.task).append(
                        pointType, other.pointType).isEquals();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(task).append(pointType).toHashCode();
        }

        public boolean sendsModificationsThrough(D dependency) {
            DependencyType type = adapter.getType(dependency);
            T source = adapter.getSource(dependency);
            return source.equals(task)
                    && pointType.sendsModificationsThrough(adapter, source,
                            type);
        }
    }

    private Set<TaskPoint<V, D>> getReachableFrom(TaskPoint<V, D> task) {
        Set<TaskPoint<V, D>> result = new HashSet<TaskPoint<V, D>>();
        Queue<TaskPoint<V, D>> pending = new LinkedList<TaskPoint<V, D>>();
        result.add(task);
        pending.offer(task);
        while (!pending.isEmpty()) {
            TaskPoint<V, D> current = pending.poll();
            Set<TaskPoint<V, D>> immendiate = getImmendiateReachableFrom(current);
            for (TaskPoint<V, D> each : immendiate) {
                if (!result.contains(each)) {
                    result.add(each);
                    pending.offer(each);
                }
            }
        }
        return result;
    }



    private V getTopmostFor(V task) {
        V result = task;
        while (fromChildToParent.containsKey(result)) {
            result = fromChildToParent.get(result);
        }
        return result;
    }

    private Set<TaskPoint<V, D>> getImmendiateReachableFrom(
            TaskPoint<V, D> current) {
        Set<TaskPoint<V, D>> result = new HashSet<TaskPoint<V, D>>();
        Set<D> outgoingEdgesOf = graph.outgoingEdgesOf(current.task);
        for (D each : outgoingEdgesOf) {
            if (current.sendsModificationsThrough(each)) {
                result.add(adapter.getDestinationPoint(each));
            }
        }
        return result;
    }

    @Override
    public GanttDate getEndDateFor(V task) {
        return adapter.getEndDateFor(task);
    }

    @Override
    public List<Constraint<GanttDate>> getStartConstraintsFor(V task) {
        return adapter.getStartConstraintsFor(task);
    }

    @Override
    public GanttDate getStartDate(V task) {
        return adapter.getStartDate(task);
    }

    @Override
    public List<V> getChildren(V task) {
        if (!isContainer(task)) {
            return Collections.emptyList();
        }
        return adapter.getChildren(task);
    }

}

interface IReentranceCases {
    public void ifNewEntrance();

    public void ifAlreadyInside();
}

class ReentranceGuard {
    private final ThreadLocal<Boolean> inside = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        };
    };

    public void entranceRequested(IReentranceCases reentranceCases) {
        if (inside.get()) {
            reentranceCases.ifAlreadyInside();
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