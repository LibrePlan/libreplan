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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.zkoss.ganttz.data.DependencyType.Point;
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

        public List<Constraint<GanttDate>> getConstraints(
                ConstraintCalculator<V> calculator, Set<D> withDependencies,
                Point point);

        List<Constraint<GanttDate>> getStartConstraintsFor(V task);

        List<Constraint<GanttDate>> getEndConstraintsFor(V task);

        V getSource(D dependency);

        V getDestination(D dependency);

        Class<D> getDependencyType();

        D createInvisibleDependency(V origin, V destination, DependencyType type);

        DependencyType getType(D dependency);

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
        public boolean isVisible(Dependency dependency) {
            return dependency.isVisible();
        }

        @Override
        public GanttDate getEndDateFor(Task task) {
            return task.getEndDate();
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
        public List<Constraint<GanttDate>> getConstraints(
                ConstraintCalculator<Task> calculator,
                Set<Dependency> withDependencies, Point pointBeingModified) {
            return Dependency.getConstraintsFor(calculator, withDependencies,
                    pointBeingModified);
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
        public boolean isFixed(Task task) {
            return task.isFixed();
        }

    }

    public static class GanttZKDiagramGraph extends
            GanttDiagramGraph<Task, Dependency> {

        private GanttZKDiagramGraph(boolean scheduleBackwards,
                List<Constraint<GanttDate>> globalStartConstraints,
                List<Constraint<GanttDate>> globalEndConstraints,
                boolean dependenciesConstraintsHavePriority) {
            super(scheduleBackwards, GANTTZK_ADAPTER, globalStartConstraints,
                    globalEndConstraints,
                    dependenciesConstraintsHavePriority);
        }

    }

    public interface IGraphChangeListener {
        public void execute();
    }

    public static GanttZKDiagramGraph create(boolean scheduleBackwards,
            List<Constraint<GanttDate>> globalStartConstraints,
            List<Constraint<GanttDate>> globalEndConstraints,
            boolean dependenciesConstraintsHavePriority) {
        return new GanttZKDiagramGraph(scheduleBackwards,
                globalStartConstraints,
                globalEndConstraints, dependenciesConstraintsHavePriority);
    }

    private final IAdapter<V, D> adapter;

    private final DirectedGraph<V, D> graph;

    private final TopologicalSorter topologicalSorter;

    private List<V> topLevelTasks = new ArrayList<V>();

    private Map<V, V> fromChildToParent = new HashMap<V, V>();

    private final List<Constraint<GanttDate>> globalStartConstraints;

    private final List<Constraint<GanttDate>> globalEndConstraints;

    private final boolean scheduleBackwards;

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
            boolean scheduleBackwards,
            IAdapter<V, D> adapter,
            List<Constraint<GanttDate>> globalStartConstraints,
            List<Constraint<GanttDate>> globalEndConstraints,
            boolean dependenciesConstraintsHavePriority) {
        return new GanttDiagramGraph<V, D>(scheduleBackwards, adapter,
                globalStartConstraints,
                globalEndConstraints, dependenciesConstraintsHavePriority);
    }

    protected GanttDiagramGraph(boolean scheduleBackwards,
            IAdapter<V, D> adapter,
            List<Constraint<GanttDate>> globalStartConstraints,
            List<Constraint<GanttDate>> globalEndConstraints,
            boolean dependenciesConstraintsHavePriority) {
        this.scheduleBackwards = scheduleBackwards;
        this.adapter = adapter;
        this.globalStartConstraints = globalStartConstraints;
        this.globalEndConstraints = globalEndConstraints;
        this.dependenciesConstraintsHavePriority = dependenciesConstraintsHavePriority;
        this.graph = new SimpleDirectedGraph<V, D>(adapter.getDependencyType());
        this.topologicalSorter = new TopologicalSorter();
    }

    public void enforceAllRestrictions() {
        enforcer.enforceRestrictionsOn(withoutVisibleIncomingDependencies(getTopLevelTasks()));
    }

    private List<V> withoutVisibleIncomingDependencies(
            Collection<? extends V> tasks) {
        List<V> result = new ArrayList<V>();
        for (V each : tasks) {
            if (noVisibleDependencies(isScheduleForward() ? graph
                    .incomingEdgesOf(each) : graph.outgoingEdgesOf(each))) {
                result.add(each);
            }
        }
        return result;
    }

    private boolean noVisibleDependencies(Collection<? extends D> dependencies) {
        for (D each : dependencies) {
            if (adapter.isVisible(each)) {
                return false;
            }
        }
        return true;
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

    class TopologicalSorter {

        private Map<TaskPoint, Integer> taskPointsByDepthCached = null;

        private Map<TaskPoint, Integer> taskPointsByDepth() {
            if (taskPointsByDepthCached != null) {
                return taskPointsByDepthCached;
            }

            Map<TaskPoint, Integer> result = new HashMap<TaskPoint, Integer>();
            Map<TaskPoint, Set<TaskPoint>> visitedBy = new HashMap<TaskPoint, Set<TaskPoint>>();

            Queue<TaskPoint> withoutIncoming = getInitial(withoutVisibleIncomingDependencies(getTopLevelTasks()));
            for (TaskPoint each : withoutIncoming) {
                initializeIfNeededForKey(result, each, 0);
            }

            while (!withoutIncoming.isEmpty()) {
                TaskPoint current = withoutIncoming.poll();
                for (TaskPoint each : current.getImmediateSuccessors()) {
                    initializeIfNeededForKey(visitedBy, each,
                            new HashSet<TaskPoint>());
                    Set<TaskPoint> visitors = visitedBy.get(each);
                    visitors.add(current);
                    Set<TaskPoint> predecessorsRequired = each
                            .getImmediatePredecessors();
                    if (visitors.containsAll(predecessorsRequired)) {
                        initializeIfNeededForKey(result, each,
                                result.get(current) + 1);
                        withoutIncoming.offer(each);
                    }
                }
            }
            return taskPointsByDepthCached = Collections
                    .unmodifiableMap(result);
        }

        private <K, T> void initializeIfNeededForKey(Map<K, T> map, K key,
                T initialValue) {
            if (!map.containsKey(key)) {
                map.put(key, initialValue);
            }
        }

        private LinkedList<TaskPoint> getInitial(List<V> initial) {
            LinkedList<TaskPoint> result = new LinkedList<TaskPoint>();
            for (V each : initial) {
                result.add(allPointsPotentiallyModified(each));
            }
            return result;
        }

        public void recalculationNeeded() {
            taskPointsByDepthCached = null;
        }

        public List<Recalculation> sort(
                Collection<? extends Recalculation> recalculationsToBeSorted) {

            List<Recalculation> result = new ArrayList<Recalculation>(
                    recalculationsToBeSorted);
            final Map<TaskPoint, Integer> taskPointsByDepth = taskPointsByDepth();
            Collections.sort(result, new Comparator<Recalculation>() {

                @Override
                public int compare(Recalculation o1, Recalculation o2) {
                    int o1Depth = onNullDefault(
                            taskPointsByDepth.get(o1.taskPoint),
                            Integer.MAX_VALUE, "no depth value for "
                                    + o1.taskPoint);
                    int o2Depth = onNullDefault(
                            taskPointsByDepth.get(o2.taskPoint),
                            Integer.MAX_VALUE, "no depth value for "
                                    + o2.taskPoint);
                    int result = o1Depth - o2Depth;
                    if (result == 0) {
                        return asInt(o1.parentRecalculation)
                                - asInt(o2.parentRecalculation);
                    }
                    return result;
                }

                private int asInt(boolean b) {
                    return b ? 1 : 0;
                }
            });
            return result;
        }
    }

    private static <T> T onNullDefault(T value, T defaultValue,
            String warnMessage) {
        if (value == null) {
            if (warnMessage != null) {
                LOG.warn(warnMessage);
            }
            return defaultValue;
        }
        return value;
    }

    public void addTask(V original) {
        List<V> stack = new LinkedList<V>();
        stack.add(original);
        List<D> dependenciesToAdd = new ArrayList<D>();
        while (!stack.isEmpty()){
            V task = stack.remove(0);
            graph.addVertex(task);
            topologicalSorter.recalculationNeeded();
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
            GanttDate firstStart = getSmallestBeginDateFromChildrenFor(container);
            GanttDate lastEnd = getBiggestEndDateFromChildrenFor(container);
            GanttDate previousEnd = adapter.getEndDateFor(container);
            if (firstStart.after(oldBeginDate) || previousEnd.after(lastEnd)) {
                adapter.setStartDateFor(container,
                        GanttDate.max(firstStart, oldBeginDate));
                adapter.setEndDateFor(container,
                        GanttDate.min(lastEnd, previousEnd));
                return true;
            }
            return false;
        }
    }

    private GanttDate getSmallestBeginDateFromChildrenFor(V container) {
        return Collections.min(getChildrenDates(container, Point.START));
    }

    private GanttDate getBiggestEndDateFromChildrenFor(V container) {
        return Collections.max(getChildrenDates(container, Point.END));
    }

    private List<GanttDate> getChildrenDates(V container, Point point) {
        List<V> children = adapter.getChildren(container);
        List<GanttDate> result = new ArrayList<GanttDate>();
        if (children.isEmpty()) {
            result.add(getDateFor(container, point));
        }
        for (V each : children) {
            result.add(getDateFor(each, point));
        }
        return result;
    }

    GanttDate getDateFor(V task, Point point) {
        if (point.equals(Point.START)) {
            return adapter.getStartDate(task);
        } else {
            return adapter.getEndDateFor(task);
        }
    }

    List<Recalculation> getRecalculationsNeededFrom(V task) {
        List<Recalculation> result = new ArrayList<Recalculation>();
        Set<Recalculation> parentRecalculationsAlreadyDone = new HashSet<Recalculation>();
        Recalculation first = recalculationFor(allPointsPotentiallyModified(task));
        first.couldHaveBeenModifiedBeforehand();

        result.addAll(getParentsRecalculations(parentRecalculationsAlreadyDone,
                first.taskPoint));
        result.add(first);

        Queue<Recalculation> pendingOfVisit = new LinkedList<Recalculation>();
        pendingOfVisit.offer(first);

        Map<Recalculation, Recalculation> alreadyVisited = new HashMap<Recalculation, Recalculation>();
        alreadyVisited.put(first, first);

        while (!pendingOfVisit.isEmpty()) {
            Recalculation current = pendingOfVisit.poll();
            for (TaskPoint each : current.taskPoint.getImmediateSuccessors()) {
                if (each.isImmediatelyDerivedFrom(current.taskPoint)) {
                    continue;
                }
                Recalculation recalculationToAdd = getRecalcualtionToAdd(each,
                        alreadyVisited);
                recalculationToAdd.comesFromPredecessor(current);
                if (!alreadyVisited.containsKey(recalculationToAdd)) {
                    result.addAll(getParentsRecalculations(
                            parentRecalculationsAlreadyDone, each));
                    result.add(recalculationToAdd);
                    pendingOfVisit.offer(recalculationToAdd);
                    alreadyVisited.put(recalculationToAdd, recalculationToAdd);
                }
            }
        }
        return topologicalSorter.sort(result);
    }

    private Recalculation getRecalcualtionToAdd(TaskPoint taskPoint,
            Map<Recalculation, Recalculation> alreadyVisited) {
        Recalculation result = recalculationFor(taskPoint);
        if (alreadyVisited.containsKey(result)) {
            return alreadyVisited.get(result);
        } else {
            return result;
        }
    }

    private List<Recalculation> getParentsRecalculations(
            Set<Recalculation> parentRecalculationsAlreadyDone,
            TaskPoint taskPoint) {
        List<Recalculation> result = new ArrayList<Recalculation>();
        for (TaskPoint eachParent : parentsRecalculationsNeededFor(taskPoint)) {
            Recalculation parentRecalculation = parentRecalculation(eachParent.task);
            if (!parentRecalculationsAlreadyDone
                    .contains(parentRecalculation)) {
                parentRecalculationsAlreadyDone.add(parentRecalculation);
                result.add(parentRecalculation);
            }
        }
        return result;
    }

    private Set<TaskPoint> parentsRecalculationsNeededFor(TaskPoint current) {
        Set<TaskPoint> result = new LinkedHashSet<TaskPoint>();
        if (current.areAllPointsPotentiallyModified()) {
            List<V> path = fromTaskToTop(current.task);
            if (path.size() > 1) {
                path = path.subList(1, path.size());
                Collections.reverse(path);
                result.addAll(asBothPoints(path));
            }
        }
        return result;
    }

    private Collection<? extends TaskPoint> asBothPoints(List<V> parents) {
        List<TaskPoint> result = new ArrayList<TaskPoint>();
        for (V each : parents) {
            result.add(allPointsPotentiallyModified(each));
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
        return new Recalculation(allPointsPotentiallyModified(task), true);
    }

    private Recalculation recalculationFor(TaskPoint taskPoint) {
        return new Recalculation(taskPoint, false);
    }

    private class Recalculation {

        private final boolean parentRecalculation;

        private final TaskPoint taskPoint;

        private Set<Recalculation> recalculationsCouldAffectThis = new HashSet<Recalculation>();

        private boolean recalculationCalled = false;

        private boolean dataPointModified = false;

        private boolean couldHaveBeenModifiedBeforehand = false;

        Recalculation(TaskPoint taskPoint, boolean isParentRecalculation) {
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

        private boolean taskChangesPosition() {
            ChangeTracker tracker = trackTaskChanges();
            Constraint.initialValue(noRestrictions())
                    .withConstraints(getConstraintsToApply())
                    .apply();
            return tracker.taskHasChanged();
        }

        @SuppressWarnings("unchecked")
        private List<Constraint<PositionRestrictions>> getConstraintsToApply() {
            Constraint<PositionRestrictions> weakForces = scheduleBackwards ? new WeakForwardForces()
                    : new WeakBackwardsForces();
            Constraint<PositionRestrictions> dominatingForces = scheduleBackwards ? new DominatingBackwardForces()
                    : new DominatingForwardForces();
            if (dependenciesConstraintsHavePriority) {
                return asList(weakForces, dominatingForces);
            } else {
                return asList(weakForces, dominatingForces, weakForces);
            }
        }

        abstract class PositionRestrictions {

            private final GanttDate start;

            private final GanttDate end;

            PositionRestrictions(GanttDate start, GanttDate end) {
                this.start = start;
                this.end = end;
            }

            GanttDate getStart() {
                return start;
            }

            GanttDate getEnd() {
                return end;
            }

            abstract List<Constraint<GanttDate>> getStartConstraints();

            abstract List<Constraint<GanttDate>> getEndConstraints();

            abstract boolean satisfies(PositionRestrictions other);

        }


        private final class NoRestrictions extends PositionRestrictions {

            public NoRestrictions(TaskPoint taskPoint) {
                super(adapter.getStartDate(taskPoint.task), adapter
                        .getEndDateFor(taskPoint.task));
            }

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
            return new NoRestrictions(taskPoint);
        }

        DatesBasedPositionRestrictions biggerThan(GanttDate start, GanttDate end) {
            ComparisonType type = isScheduleForward() ? ComparisonType.BIGGER_OR_EQUAL_THAN
                    : ComparisonType.BIGGER_OR_EQUAL_THAN_LEFT_FLOATING;
            return new DatesBasedPositionRestrictions(type, start, end);
        }

        DatesBasedPositionRestrictions lessThan(GanttDate start, GanttDate end) {
            ComparisonType type = isScheduleForward() ? ComparisonType.LESS_OR_EQUAL_THAN_RIGHT_FLOATING
                    : ComparisonType.LESS_OR_EQUAL_THAN;
            return new DatesBasedPositionRestrictions(type, start, end);
        }

        class DatesBasedPositionRestrictions extends PositionRestrictions {

            private Constraint<GanttDate> startConstraint;
            private Constraint<GanttDate> endConstraint;

            public DatesBasedPositionRestrictions(
                    ComparisonType comparisonType, GanttDate start,
                    GanttDate end) {
                super(start, end);
                this.startConstraint = ConstraintOnComparableValues
                        .instantiate(comparisonType, start);
                this.endConstraint = ConstraintOnComparableValues.instantiate(
                        comparisonType, end);
            }

            boolean satisfies(PositionRestrictions other) {
                if (DatesBasedPositionRestrictions.class.isInstance(other)) {
                    return satisfies(DatesBasedPositionRestrictions.class
                            .cast(other));
                }
                return false;
            }

            private boolean satisfies(DatesBasedPositionRestrictions other) {
                return startConstraint.isSatisfiedBy(other.getStart())
                        && endConstraint.isSatisfiedBy(other.getEnd());
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

            public Forces() {
                this.task = taskPoint.task;
            }

            private PositionRestrictions resultingRestrictions = noRestrictions();

            protected PositionRestrictions applyConstraintTo(
                    PositionRestrictions restrictions) {
                if (adapter.isFixed(task)) {
                    return restrictions;
                }
                resultingRestrictions = enforceUsingPreviousRestrictions(restrictions);
                return resultingRestrictions;
            }

            public boolean isSatisfiedBy(PositionRestrictions value) {
                return resultingRestrictions.satisfies(value);
            }

            public void checkSatisfiesResult(PositionRestrictions finalResult) {
                super.checkSatisfiesResult(finalResult);
                checkStartConstraints(finalResult.getStart());
                checkEndConstraints(finalResult.getEnd());
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

        abstract class Dominating extends Forces {

            private final Point primary;
            private final Point secondary;

            public Dominating(Point primary, Point secondary) {
                Validate.isTrue(isSupportedPoint(primary));
                Validate.isTrue(isSupportedPoint(secondary));
                Validate.isTrue(!primary.equals(secondary));

                this.primary = primary;
                this.secondary = secondary;
            }

            private boolean isSupportedPoint(Point point) {
                EnumSet<Point> validPoints = EnumSet.of(Point.START, Point.END);
                return validPoints.contains(point);
            }

            private Point getPrimaryPoint() {
                return primary;
            }

            private Point getSecondaryPoint() {
                return secondary;
            }

            @Override
            PositionRestrictions enforceUsingPreviousRestrictions(
                    PositionRestrictions restrictions) {
                if (parentRecalculation) {
                    // avoid interference from task containers shrinking
                    return enforcePrimaryPoint(restrictions);
                } else if (taskPoint.areAllPointsPotentiallyModified()) {
                    return enforceBoth(restrictions);
                } else if (taskPoint.somePointPotentiallyModified()) {
                    return enforceSecondaryPoint(restrictions);
                }
                return restrictions;
            }

            private PositionRestrictions enforceBoth(
                    PositionRestrictions restrictions) {
                ChangeTracker changeTracker = trackTaskChanges();
                PositionRestrictions currentRestrictions = enforcePrimaryPoint(restrictions);
                if (changeTracker.taskHasChanged() || parentRecalculation
                        || couldHaveBeenModifiedBeforehand) {
                    return enforceSecondaryPoint(currentRestrictions);
                }
                return currentRestrictions;
            }

            private PositionRestrictions enforcePrimaryPoint(
                    PositionRestrictions originalRestrictions) {
                GanttDate newDominatingPointDate = calculatePrimaryPointDate(originalRestrictions);
                return enforceRestrictionsFor(primary, newDominatingPointDate);
            }

            /**
             * Calculates the new date for the primary point based on the
             * present constraints. If there are no constraints this method will
             * return the existent commanding point date
             * @param originalRestrictions
             */
            private GanttDate calculatePrimaryPointDate(
                    PositionRestrictions originalRestrictions) {
                GanttDate newDate = Constraint
                        .<GanttDate> initialValue(null)
                        .withConstraints(
                                getConstraintsFrom(originalRestrictions,
                                        getPrimaryPoint()))
                        .withConstraints(getConstraintsFor(getPrimaryPoint()))
                        .applyWithoutFinalCheck();
                if (newDate == null) {
                    return getTaskDateFor(getPrimaryPoint());
                }
                return newDate;
            }

            private List<Constraint<GanttDate>> getConstraintsFor(Point point) {
                Validate.isTrue(isSupportedPoint(point));
                switch (point) {
                case START:
                    return getStartConstraints();
                case END:
                    return getEndConstraints();
                default:
                    throw new RuntimeException("shouldn't happen");
                }
            }

            private PositionRestrictions enforceSecondaryPoint(
                    PositionRestrictions restrictions) {
                GanttDate newSecondaryPointDate = calculateSecondaryPointDate(restrictions);
                if (newSecondaryPointDate == null) {
                    return restrictions;
                }
                restrictions = enforceRestrictionsFor(getSecondaryPoint(),
                        newSecondaryPointDate);
                if (taskPoint.onlyModifies(getSecondaryPoint())) {
                    // primary point constraints could be the ones "commanding"
                    // now
                    GanttDate potentialPrimaryDate = calculatePrimaryPointDate(restrictions);
                    if (!doSatisfyOrderCondition(potentialPrimaryDate,
                            getTaskDateFor(getPrimaryPoint()))) {
                        return enforceRestrictionsFor(getPrimaryPoint(),
                                potentialPrimaryDate);
                    }
                }
                return restrictions;
            }


            private GanttDate calculateSecondaryPointDate(
                    PositionRestrictions restrictions) {
                GanttDate newEnd = Constraint
                        .<GanttDate> initialValue(null)
                        .withConstraints(
                                getConstraintsFrom(restrictions,
                                        getSecondaryPoint()))
                        .withConstraints(getConstraintsFor(getSecondaryPoint()))
                        .applyWithoutFinalCheck();
                return newEnd;
            }

            protected abstract boolean doSatisfyOrderCondition(
                    GanttDate supposedlyBefore, GanttDate supposedlyAfter);

            private PositionRestrictions enforceRestrictionsFor(Point point,
                    GanttDate newDate) {
                GanttDate old = getTaskDateFor(point);
                if (areNotEqual(old, newDate)) {
                    setTaskDateFor(point, newDate);
                }
                return createRestrictionsFor(getTaskDateFor(Point.START),
                        getTaskDateFor(Point.END));
            }

            GanttDate getTaskDateFor(Point point) {
                Validate.isTrue(isSupportedPoint(point));
                return getDateFor(task, point);
            }

            protected abstract PositionRestrictions createRestrictionsFor(
                    GanttDate start, GanttDate end);

            private void setTaskDateFor(Point point, GanttDate date) {
                Validate.isTrue(isSupportedPoint(point));
                switch (point) {
                case START:
                    adapter.setStartDateFor(task, date);
                    break;
                case END:
                    adapter.setEndDateFor(task, date);
                }
            }

            private List<Constraint<GanttDate>> getConstraintsFrom(
                    PositionRestrictions restrictions, Point point) {
                Validate.isTrue(isSupportedPoint(point));
                switch (point) {
                case START:
                    return restrictions.getStartConstraints();
                case END:
                    return restrictions.getEndConstraints();
                default:
                    throw new RuntimeException("shouldn't happen");
                }
            }

            protected List<Constraint<GanttDate>> getConstraintsForPrimaryPoint() {
                List<Constraint<GanttDate>> result = new ArrayList<Constraint<GanttDate>>();
                if (dependenciesConstraintsHavePriority) {
                    result.addAll(getTaskConstraints(getPrimaryPoint()));
                    result.addAll(getDependenciesConstraintsFor(getPrimaryPoint()));

                } else {
                    result.addAll(getDependenciesConstraintsFor(getPrimaryPoint()));
                    result.addAll(getTaskConstraints(getPrimaryPoint()));
                }
                result.addAll(getGlobalConstraintsToApply(getPrimaryPoint()));
                return result;
            }

            private Collection<Constraint<GanttDate>> getGlobalConstraintsToApply(
                    Point point) {
                Validate.isTrue(isSupportedPoint(point));
                switch (point) {
                case START:
                    return globalStartConstraints;
                case END:
                    return globalEndConstraints;
                default:
                    throw new RuntimeException("shouldn't happen");
                }
            }

            protected List<Constraint<GanttDate>> getConstraintsForSecondaryPoint() {
                return getDependenciesConstraintsFor(getSecondaryPoint());
            }

            private List<Constraint<GanttDate>> getDependenciesConstraintsFor(
                    Point point) {
                final Set<D> withDependencies = getDependenciesAffectingThisTask();
                return adapter.getConstraints(getCalculator(),
                        withDependencies, point);
            }

            protected abstract Set<D> getDependenciesAffectingThisTask();

            private List<Constraint<GanttDate>> getTaskConstraints(Point point) {
                Validate.isTrue(isSupportedPoint(point));
                switch (point) {
                case START:
                    return adapter.getStartConstraintsFor(task);
                case END:
                    return adapter.getEndConstraintsFor(task);
                default:
                    throw new RuntimeException("shouldn't happen");
                }
            }

            protected abstract ConstraintCalculator<V> getCalculator();

            protected ConstraintCalculator<V> createNormalCalculator() {
                return createCalculator(false);
            }

            protected ConstraintCalculator<V> createBackwardsCalculator() {
                return createCalculator(true);
            }

            private ConstraintCalculator<V> createCalculator(boolean inverse) {
                return new ConstraintCalculator<V>(inverse) {

                    @Override
                    protected GanttDate getStartDate(V vertex) {
                        return adapter.getStartDate(vertex);
                    }

                    @Override
                    protected GanttDate getEndDate(V vertex) {
                        return adapter.getEndDateFor(vertex);
                    }
                };
            }

        }

        class DominatingForwardForces extends Dominating {

            public DominatingForwardForces() {
                super(Point.START, Point.END);
            }

            @Override
            List<Constraint<GanttDate>> getStartConstraints() {
                return getConstraintsForPrimaryPoint();
            }

            @Override
            List<Constraint<GanttDate>> getEndConstraints() {
                return getConstraintsForSecondaryPoint();
            }

            @Override
            protected Set<D> getDependenciesAffectingThisTask() {
                return graph.incomingEdgesOf(task);
            }

            @Override
            protected ConstraintCalculator<V> getCalculator() {
                return createNormalCalculator();
            }

            @Override
            protected PositionRestrictions createRestrictionsFor(
                    GanttDate start, GanttDate end) {
                return biggerThan(start, end);
            }

            @Override
            protected boolean doSatisfyOrderCondition(
                    GanttDate supposedlyBefore,
                    GanttDate supposedlyAfter) {
                return supposedlyBefore.compareTo(supposedlyAfter) <= 0;
            }

        }

        class DominatingBackwardForces extends Dominating {

            public DominatingBackwardForces() {
                super(Point.END, Point.START);
            }

            @Override
            List<Constraint<GanttDate>> getStartConstraints() {
                return getConstraintsForSecondaryPoint();
            }

            @Override
            List<Constraint<GanttDate>> getEndConstraints() {
                return getConstraintsForPrimaryPoint();
            }

            @Override
            protected Set<D> getDependenciesAffectingThisTask() {
                return graph.outgoingEdgesOf(task);
            }

            @Override
            protected ConstraintCalculator<V> getCalculator() {
                return createBackwardsCalculator();
            }

            @Override
            protected PositionRestrictions createRestrictionsFor(
                    GanttDate start, GanttDate end) {
                return lessThan(start, end);
            }

            @Override
            protected boolean doSatisfyOrderCondition(
                    GanttDate supposedlyBefore,
                    GanttDate supposedlyAfter) {
                return supposedlyBefore.compareTo(supposedlyAfter) >= 0;
            }

        }

        class WeakForwardForces extends Forces {

            @Override
            List<Constraint<GanttDate>> getStartConstraints() {
                return adapter.getStartConstraintsFor(task);
            }

            @Override
            List<Constraint<GanttDate>> getEndConstraints() {
                return Collections.emptyList();
            }

            @Override
            PositionRestrictions enforceUsingPreviousRestrictions(
                    PositionRestrictions restrictions) {
                GanttDate result = Constraint.<GanttDate> initialValue(null)
                        .withConstraints(restrictions.getStartConstraints())
                        .withConstraints(getStartConstraints())
                        .applyWithoutFinalCheck();
                if (result != null && !result.equals(getStartDate(task))) {
                    return enforceRestrictions(result);
                }
                return restrictions;
            }

            private PositionRestrictions enforceRestrictions(GanttDate result) {
                adapter.setStartDateFor(task, result);
                return biggerThan(result, adapter.getEndDateFor(task));
            }

        }

        class WeakBackwardsForces extends Forces {

            @Override
            PositionRestrictions enforceUsingPreviousRestrictions(
                    PositionRestrictions restrictions) {
                GanttDate result = Constraint.<GanttDate> initialValue(null)
                        .withConstraints(restrictions.getEndConstraints())
                        .withConstraints(getEndConstraints())
                        .applyWithoutFinalCheck();
                if (result != null && !result.equals(getEndDateFor(task))) {
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
            return String.format(
                    "%s, parentRecalculation: %s, predecessors: %s",
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
        topologicalSorter.recalculationNeeded();
        enforcer.enforceRestrictionsOn(needingEnforcing);
    }

    public void removeDependency(D dependency) {
        graph.removeEdge(dependency);
        topologicalSorter.recalculationNeeded();
        V destination = adapter.getDestination(dependency);
        V source = adapter.getSource(dependency);
        enforcer.enforceRestrictionsOn(destination);
        enforcer.enforceRestrictionsOn(source);
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
        topologicalSorter.recalculationNeeded();
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

    public boolean hasVisibleIncomingDependencies(V task) {
        return isSomeVisible(graph.incomingEdgesOf(task));
    }

    public boolean hasVisibleOutcomingDependencies(V task) {
        return isSomeVisible(graph.outgoingEdgesOf(task));
    }

    private boolean isSomeVisible(Set<D> dependencies) {
        for (D each : dependencies) {
            if (adapter.isVisible(each)) {
                return true;
            }
        }
        return false;
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
        Set<TaskPoint> reachableFromDestination = destinationPoint(dependency)
                .getReachable();
        for (TaskPoint each : reachableFromDestination) {
            if (each.sendsModificationsThrough(dependency)) {
                return false;
            }
        }
        return true;
    }

    TaskPoint destinationPoint(D dependency) {
        V destination = getDependencyDestination(dependency);
        return new TaskPoint(destination,
                getDestinationPoint(dependency.getType()));
    }

    private Point getDestinationPoint(DependencyType type) {
        return type.getSourceAndDestination()[isScheduleForward() ? 1 : 0];
    }

    TaskPoint sourcePoint(D dependency) {
        V source = getDependencySource(dependency);
        return new TaskPoint(source, getSourcePoint(dependency.getType()));
    }

    /**
     * The dominating point is the one that causes the other point to be
     * modified; e.g. when doing forward scheduling the dominating point is the
     * start.
     */
    private boolean isDominatingPoint(Point point) {
        return point == getDominatingPoint();
    }

    private Point getDominatingPoint() {
        return isScheduleForward() ? Point.START : Point.END;
    }

    private Point getSourcePoint(DependencyType type) {
        return type.getSourceAndDestination()[isScheduleForward() ? 0 : 1];
    }

    private V getDependencySource(D dependency) {
        return isScheduleForward() ? adapter.getSource(dependency) : adapter
                .getDestination(dependency);
    }

    private V getDependencyDestination(D dependency) {
        return isScheduleForward() ? adapter.getDestination(dependency)
                : adapter.getSource(dependency);
    }

    TaskPoint allPointsPotentiallyModified(V task) {
        return new TaskPoint(task, getDominatingPoint());
    }

    private class TaskPoint {

        private final V task;

        private final boolean isContainer;

        private final Set<Point> pointsModified;

        private final Point entryPoint;

        TaskPoint(V task, Point entryPoint) {
            Validate.notNull(task);
            Validate.notNull(entryPoint);
            this.task = task;
            this.entryPoint = entryPoint;
            this.pointsModified = isDominatingPoint(entryPoint) ? EnumSet.of(
                    Point.START, Point.END) : EnumSet.of(entryPoint);
            this.isContainer = adapter.isContainer(task);
        }


        @Override
        public String toString() {
            return String.format("%s(%s)", task, pointsModified);
        }

        @Override
        public boolean equals(Object obj) {
            if (TaskPoint.class.isInstance(obj)) {
                TaskPoint other = TaskPoint.class.cast(obj);
                return new EqualsBuilder().append(task, other.task)
                        .append(pointsModified, other.pointsModified)
                        .isEquals();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(task).append(pointsModified)
                    .toHashCode();
        }

        public boolean areAllPointsPotentiallyModified() {
            return pointsModified.size() > 1;
        }

        public boolean somePointPotentiallyModified() {
            return pointsModified.contains(Point.START)
                    || pointsModified.contains(Point.END);
        }

        public boolean onlyModifies(Point point) {
            return pointsModified.size() == 1 && pointsModified.contains(point);
        }

        Set<TaskPoint> getReachable() {
            Set<TaskPoint> result = new HashSet<TaskPoint>();
            Queue<TaskPoint> pending = new LinkedList<TaskPoint>();
            result.add(this);
            pending.offer(this);
            while (!pending.isEmpty()) {
                TaskPoint current = pending.poll();
                Set<TaskPoint> immendiate = current.getImmediateSuccessors();
                for (TaskPoint each : immendiate) {
                    if (!result.contains(each)) {
                        result.add(each);
                        pending.offer(each);
                    }
                }
            }
            return result;
        }

        public boolean isImmediatelyDerivedFrom(TaskPoint other) {
            return this.task.equals(other.task)
                    && other.pointsModified.containsAll(this.pointsModified);
        }

        private Set<TaskPoint> cachedInmmediateSuccesors = null;

        public Set<TaskPoint> getImmediateSuccessors() {
            if (cachedInmmediateSuccesors != null) {
                return cachedInmmediateSuccesors;
            }

            Set<TaskPoint> result = new HashSet<TaskPoint>();
            result.addAll(getImmediatelyDerivedOnSameTask());

            Set<D> candidates = immediateDependencies();
            for (D each : candidates) {
                if (this.sendsModificationsThrough(each)) {
                    result.add(destinationPoint(each));
                }
            }
            return cachedInmmediateSuccesors = Collections
                    .unmodifiableSet(result);
        }

        private Set<TaskPoint> cachedImmediatePredecessors = null;

        public Set<TaskPoint> getImmediatePredecessors() {
            if (cachedImmediatePredecessors != null) {
                return cachedImmediatePredecessors;
            }
            Set<TaskPoint> result = new HashSet<TaskPoint>();
            if (!isDominatingPoint(entryPoint)) {
                TaskPoint dominating = allPointsPotentiallyModified(task);
                assert isDominatingPoint(dominating.entryPoint);
                assert this.isImmediatelyDerivedFrom(dominating);
                result.add(dominating);
            }
            for (D each : immediateIncomingDependencies()) {
                if (this.receivesModificationsThrough(each)) {
                    TaskPoint sourcePoint = sourcePoint(each);
                    result.add(sourcePoint);
                }
            }
            return cachedImmediatePredecessors = Collections
                    .unmodifiableSet(result);
        }

        private Collection<TaskPoint> getImmediatelyDerivedOnSameTask() {
            for (Point each : pointsModified) {
                if (isDominatingPoint(each)) {
                    return Collections.singletonList(new TaskPoint(task, each
                            .getOther()));
                }
            }
            return Collections.emptyList();
        }

        private Set<D> immediateDependencies() {
            return isScheduleForward() ? graph.outgoingEdgesOf(this.task)
                    : graph.incomingEdgesOf(this.task);
        }

        private Set<D> immediateIncomingDependencies() {
            return isScheduleForward() ? graph.incomingEdgesOf(this.task)
                    : graph.outgoingEdgesOf(this.task);
        }

        public boolean sendsModificationsThrough(D dependency) {
            V source = getDependencySource(dependency);
            Point dependencySourcePoint = getSourcePoint(adapter
                    .getType(dependency));

            return source.equals(task)
                    && (!isContainer || pointsModified
                            .contains(dependencySourcePoint));
        }

        private Point getSourcePoint(DependencyType type) {
            Point[] sourceAndDestination = type.getSourceAndDestination();
            return sourceAndDestination[isScheduleForward() ? 0 : 1];
        }

        private boolean receivesModificationsThrough(D dependency) {
            V destination = getDependencyDestination(dependency);
            Point destinationPoint = getDestinationPoint(adapter
                    .getType(dependency));

            return destination.equals(task) && entryPoint == destinationPoint;
        }

    }



    private V getTopmostFor(V task) {
        V result = task;
        while (fromChildToParent.containsKey(result)) {
            result = fromChildToParent.get(result);
        }
        return result;
    }

    public boolean isScheduleForward() {
        return !isScheduleBackwards();
    }

    public boolean isScheduleBackwards() {
        return scheduleBackwards;
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
    public List<Constraint<GanttDate>> getEndConstraintsFor(V task) {
        return adapter.getEndConstraintsFor(task);
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