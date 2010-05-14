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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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

    private List<Task> topLevelTasks = new ArrayList<Task>();

    private Map<Task, TaskContainer> fromChildToParent = new HashMap<Task, TaskContainer>();

    private final List<Constraint<Date>> globalStartConstraints;

    private final List<Constraint<Date>> globalEndConstraints;

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

    public GanttDiagramGraph(List<Constraint<Date>> globalStartConstraints,
            List<Constraint<Date>> globalEndConstraints,
            boolean dependenciesConstraintsHavePriority) {
        this.globalStartConstraints = globalStartConstraints;
        this.globalEndConstraints = globalEndConstraints;
        this.dependenciesConstraintsHavePriority = dependenciesConstraintsHavePriority;
    }

    public void enforceAllRestrictions() {
        enforcer.enforceRestrictionsOn(getTopLevelTasks());
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
        task.registerDependenciesEnforcerHook(enforcer);
        if (task.isContainer()) {
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

    public interface IDependenciesEnforcerHook {
        public void setStartDate(Date previousStart, long previousLength,
                Date newStart);

        public void setLengthMilliseconds(long previousLengthMilliseconds,
                long newLengthMilliseconds);
    }

    public interface IDependenciesEnforcerHookFactory {
        public IDependenciesEnforcerHook create(Task task,
                INotificationAfterDependenciesEnforcement notification);
    }

    public interface INotificationAfterDependenciesEnforcement {
        public void onStartDateChange(Date previousStart, long previousLength,
                Date newStart);

        public void onLengthChange(long previousLength, long newLength);
    }

    public class DeferedNotifier {

        private Map<Task, NotificationPendingForTask> notificationsPending = new LinkedHashMap<Task, NotificationPendingForTask>();

        public void add(Task task, StartDateNofitication notification) {
            retrieveOrCreateFor(task).setStartDateNofitication(notification);
        }

        private NotificationPendingForTask retrieveOrCreateFor(Task task) {
            NotificationPendingForTask result = notificationsPending.get(task);
            if (result == null) {
                result = new NotificationPendingForTask();
                notificationsPending.put(task, result);
            }
            return result;
        }

        void add(Task task, LengthNotification notification) {
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

    private static class NotificationPendingForTask {
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
        private final Date previousStart;
        private final long previousLength;
        private final Date newStart;

        public StartDateNofitication(
                INotificationAfterDependenciesEnforcement notification,
                Date previousStart, long previousLength, Date newStart) {
            this.notification = notification;
            this.previousStart = previousStart;
            this.previousLength = previousLength;
            this.newStart = newStart;
        }

        public StartDateNofitication coalesce(
                StartDateNofitication startDateNofitication) {
            return new StartDateNofitication(notification, previousStart,
                    previousLength, startDateNofitication.newStart);
        }

        void doNotification() {
            notification.onStartDateChange(previousStart, previousLength,
                    newStart);
        }
    }

    private class LengthNotification {

        private final INotificationAfterDependenciesEnforcement notification;
        private final long previousLengthMilliseconds;
        private final long newLengthMilliseconds;

        public LengthNotification(
                INotificationAfterDependenciesEnforcement notification,
                long previousLengthMilliseconds, long lengthMilliseconds) {
            this.notification = notification;
            this.previousLengthMilliseconds = previousLengthMilliseconds;
            this.newLengthMilliseconds = lengthMilliseconds;

        }

        public LengthNotification coalesce(LengthNotification lengthNofitication) {
            return new LengthNotification(notification,
                    previousLengthMilliseconds,
                    lengthNofitication.newLengthMilliseconds);
        }

        void doNotification() {
            notification.onLengthChange(previousLengthMilliseconds,
                    newLengthMilliseconds);
        }
    }

    private class DependenciesEnforcer implements
            IDependenciesEnforcerHookFactory {

        private ThreadLocal<DeferedNotifier> deferedNotifier = new ThreadLocal<DeferedNotifier>();

        @Override
        public IDependenciesEnforcerHook create(Task task,
                INotificationAfterDependenciesEnforcement notificator) {
            return onlyEnforceDependenciesOnEntrance(onEntrance(task),
                    onNotification(task, notificator));
        }

        private IDependenciesEnforcerHook onEntrance(final Task task) {
            return new IDependenciesEnforcerHook() {

                @Override
                public void setStartDate(Date previousStart,
                        long previousLength, Date newStart) {
                    taskPositionModified(task);
                }

                @Override
                public void setLengthMilliseconds(
                        long previousLengthMilliseconds, long lengthMilliseconds) {
                    taskPositionModified(task);
                }
            };
        }

        private IDependenciesEnforcerHook onNotification(final Task task,
                final INotificationAfterDependenciesEnforcement notification) {
            return new IDependenciesEnforcerHook() {

                @Override
                public void setStartDate(Date previousStart,
                        long previousLength, Date newStart) {
                    StartDateNofitication startDateNotification = new StartDateNofitication(
                            notification,
                                    previousStart, previousLength, newStart);
                    deferedNotifier.get().add(task, startDateNotification);

                }

                @Override
                public void setLengthMilliseconds(
                        long previousLengthMilliseconds,
                        long newLengthMilliseconds) {
                    LengthNotification lengthNotification = new LengthNotification(
                            notification, previousLengthMilliseconds,
                            newLengthMilliseconds);
                    deferedNotifier.get().add(task, lengthNotification);
                }
            };

        }

        private IDependenciesEnforcerHook onlyEnforceDependenciesOnEntrance(
                final IDependenciesEnforcerHook onEntrance,
                final IDependenciesEnforcerHook notification) {
            return new IDependenciesEnforcerHook() {

                @Override
                public void setStartDate(final Date previousStart,
                        final long previousLength, final Date newStart) {
                    positionsUpdatingGuard
                            .entranceRequested(new IReentranceCases() {

                                @Override
                                public void ifNewEntrance() {
                                    onNewEntrance(new IAction() {

                                        @Override
                                        public void doAction() {
                                            notification.setStartDate(
                                                    previousStart,
                                                    previousLength, newStart);
                                            onEntrance.setStartDate(
                                                    previousStart,
                                                    previousLength, newStart);
                                        }
                                    });
                                }

                                @Override
                                public void ifAlreadyInside() {
                                    notification.setStartDate(previousStart,
                                            previousLength, newStart);

                                }
                            });
                }

                @Override
                public void setLengthMilliseconds(
                        final long previousLengthMilliseconds,
                        final long lengthMilliseconds) {
                    positionsUpdatingGuard
                            .entranceRequested(new IReentranceCases() {

                                @Override
                                public void ifNewEntrance() {
                                    onNewEntrance(new IAction() {

                                        @Override
                                        public void doAction() {
                                            notification.setLengthMilliseconds(
                                                    previousLengthMilliseconds,
                                                    lengthMilliseconds);
                                            onEntrance.setLengthMilliseconds(
                                                    previousLengthMilliseconds,
                                                    lengthMilliseconds);
                                        }
                                    });
                                }

                                @Override
                                public void ifAlreadyInside() {
                                    notification.setLengthMilliseconds(
                                            previousLengthMilliseconds,
                                            lengthMilliseconds);
                                }
                            });
                }
            };

        }

        void enforceRestrictionsOn(Collection<? extends Task> tasks) {
            List<Recalculation> allRecalculations = new ArrayList<Recalculation>();
            for (Task each : tasks) {
                allRecalculations.addAll(getRecalculationsNeededFrom(each));
            }
            enforceRestrictionsOn(allRecalculations);
        }

        void enforceRestrictionsOn(Task task) {
            enforceRestrictionsOn(getRecalculationsNeededFrom(task));
        }

        void enforceRestrictionsOn(final List<Recalculation> recalculations) {
            executeWithPreAndPostActionsOnlyIfNewEntrance(new IAction() {
                @Override
                public void doAction() {
                    doRecalculations(recalculations);
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

        private void taskPositionModified(final Task task) {
            executeWithPreAndPostActionsOnlyIfNewEntrance(new IAction() {
                @Override
                public void doAction() {
                    List<Recalculation> recalculationsNeededFrom = getRecalculationsNeededFrom(task);
                    doRecalculations(recalculationsNeededFrom);
                }
            });
        }

        private void doRecalculations(List<Recalculation> recalculationsNeeded) {
            Set<Task> allModified = new HashSet<Task>();
            List<Recalculation> calculated = new ArrayList<Recalculation>();
            for (Recalculation each : recalculationsNeeded) {
                if (each.haveToDoCalculation()) {
                    calculated.add(each);
                }
                boolean modified = each.doRecalculation();
                if (modified) {
                    allModified.add(each.taskPoint.task);
                }
            }
            List<TaskContainer> shrunkContainers = shrunkContainersOfModified(allModified);
            for (Task each : getTaskAffectedByShrinking(shrunkContainers)) {
                doRecalculations(getRecalculationsNeededFrom(each));
            }
        }

        private List<Task> getTaskAffectedByShrinking(
                List<TaskContainer> shrunkContainers) {
            List<Task> tasksAffectedByShrinking = new ArrayList<Task>();
            for (TaskContainer each : shrunkContainers) {
                for (Dependency eachDependency : graph.outgoingEdgesOf(each)) {
                    if (eachDependency.getType() == DependencyType.START_START
                            && eachDependency.isVisible()) {
                        tasksAffectedByShrinking.add(eachDependency
                                .getDestination());
                    }
                }
            }
            return tasksAffectedByShrinking;
        }

        private List<TaskContainer> shrunkContainersOfModified(
                Set<Task> allModified) {
            Set<TaskContainer> topmostToShrink = getTopMostThatCouldPotentiallyNeedShrinking(allModified);
            List<TaskContainer> allToShrink = new ArrayList<TaskContainer>();
            for (TaskContainer each : topmostToShrink) {
                allToShrink.addAll(getContainersBottomUp(each));
            }
            List<TaskContainer> result = new ArrayList<TaskContainer>();
            for (TaskContainer each : allToShrink) {
                boolean modified = enforceParentShrinkage(each);
                if (modified) {
                    result.add(each);
                }
            }
            return result;
        }

        private Set<TaskContainer> getTopMostThatCouldPotentiallyNeedShrinking(
                Collection<Task> modified) {
            Set<TaskContainer> result = new HashSet<TaskContainer>();
            for (Task each : modified) {
                Task t = getTopmostFor(each);
                if (t.isContainer()) {
                    result.add((TaskContainer) t);
                }
            }
            return result;
        }

        private Collection<? extends TaskContainer> getContainersBottomUp(
                TaskContainer container) {
            List<TaskContainer> result = new ArrayList<TaskContainer>();
            List<Task> tasks = container.getTasks();
            for (Task each : tasks) {
                if (each.isContainer()) {
                    TaskContainer childContainer = (TaskContainer) each;
                    result.addAll(getContainersBottomUp(childContainer));
                    result.add(childContainer);
                }
            }
            result.add(container);
            return result;
        }



        boolean enforceParentShrinkage(TaskContainer container) {
            Date oldBeginDate = container.getBeginDate();
            Date firstStart = container.getSmallestBeginDateFromChildren();
            Date previousEnd = container.getEndDate();
            if (firstStart.after(oldBeginDate)) {
                container.setBeginDate(firstStart);
                container.setEndDate(previousEnd);
                return true;
            }
            return false;
        }








    }

    List<Recalculation> getRecalculationsNeededFrom(Task task) {
        List<Recalculation> result = new LinkedList<Recalculation>();
        Set<Recalculation> parentRecalculationsAlreadyDone = new HashSet<Recalculation>();
        Recalculation first = recalculationFor(TaskPoint.both(task));
        first.couldHaveBeenModifiedBeforehand();
        Queue<Recalculation> pendingOfNavigate = new LinkedList<Recalculation>();
        result.addAll(getParentsRecalculations(parentRecalculationsAlreadyDone,
                first.taskPoint));
        result.add(first);
        pendingOfNavigate.offer(first);
        while (!pendingOfNavigate.isEmpty()) {
            Recalculation current = pendingOfNavigate.poll();
            for (TaskPoint each : getImmendiateReachableFrom(current.taskPoint)) {
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
                recalculationToAdd.fromParent(current);
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
        if (current.pointType == PointType.BOTH) {
            List<Task> path = fromTaskToTop(current.task);
            if (path.size() > 1) {
                path = path.subList(1, path.size());
                Collections.reverse(path);
                result.addAll(asBothPoints(path));
            }
        }
        return result;
    }

    private Collection<? extends TaskPoint> asBothPoints(List<Task> parents) {
        List<TaskPoint> result = new ArrayList<TaskPoint>();
        for (Task each : parents) {
            result.add(TaskPoint.both(each));
        }
        return result;
    }

    private List<Task> fromTaskToTop(Task task) {
        List<Task> result = new ArrayList<Task>();
        Task current = task;
        while (current != null) {
            result.add(current);
            current = fromChildToParent.get(current);
        }
        return result;
    }

    private Recalculation parentRecalculation(Task task) {
        return new Recalculation(TaskPoint.both(task), true);
    }

    private Recalculation recalculationFor(TaskPoint taskPoint) {
        return new Recalculation(taskPoint, false);
    }

    private class Recalculation {

        private final boolean parentRecalculation;

        private final TaskPoint taskPoint;

        private Set<Recalculation> parents = new HashSet<Recalculation>();

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

        public void fromParent(Recalculation parent) {
            parents.add(parent);
        }

        boolean doRecalculation() {
            recalculationCalled = true;
            dataPointModified = haveToDoCalculation()
                    && taskChangesPosition();
            return dataPointModified;
        }

        private boolean haveToDoCalculation() {
            return (parents.isEmpty() || parentsHaveBeenModified());
        }

        private boolean taskChangesPosition() {
            PointType pointType = taskPoint.pointType;
            Task task = taskPoint.task;
            switch (pointType) {
            case BOTH:
                return enforceStartAndEnd(task);
            case END:
                return enforceEnd(task);
            default:
                return false;
            }
        }

        private boolean parentsHaveBeenModified() {
            for (Recalculation each : parents) {
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

        private boolean enforceStartAndEnd(Task task) {
            Set<Dependency> incoming = graph.incomingEdgesOf(task);
            Date previousEndDate = task.getEndDate();
            boolean startDateChanged = enforceStartDate(task, incoming);
            boolean endDateChanged = enforceEndDate(task, previousEndDate,
                    incoming);
            return startDateChanged || endDateChanged;
        }

        private boolean enforceEnd(Task task) {
            Set<Dependency> incoming = graph.incomingEdgesOf(task);
            Date previousEndDate = task.getEndDate();
            return enforceEndDate(task, previousEndDate, incoming);
        }

        @SuppressWarnings("unchecked")
        private boolean enforceEndDate(Task task, Date previousEndDate,
                Set<Dependency> incoming) {
            Constraint<Date> currentLength = task.getCurrentLengthConstraint();
            Constraint<Date> respectStartDate = task
                    .getEndDateBiggerThanStartDate();
            Date newEnd = Constraint.<Date> initialValue(null).withConstraints(
                    currentLength).withConstraints(
                    Dependency.getEndConstraints(incoming)).withConstraints(
                    respectStartDate).apply();
            if (!task.getEndDate().equals(newEnd)) {
                task.setEndDate(newEnd);
            }
            return !previousEndDate.equals(newEnd);
        }

        private boolean enforceStartDate(Task task, Set<Dependency> incoming) {
            Date newStart = calculateStartDateFor(task, incoming);
            if (!task.getBeginDate().equals(newStart)) {
                task.setBeginDate(newStart);
                return true;
            }
            return false;
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
                        .withConstraints(globalStartConstraints).apply();

            } else {
                newStart = Constraint.<Date> initialValue(null)
                        .withConstraints(dependencyConstraints)
                        .withConstraints(task.getStartConstraints())
                        .withConstraints(globalStartConstraints).apply();
            }
            return newStart;
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
                    taskPoint, parentRecalculation, asSimpleString(parents));
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
            if (obj instanceof Recalculation) {
                Recalculation other = (Recalculation) obj;
                return new EqualsBuilder().append(parentRecalculation, other.parentRecalculation)
                                          .append(taskPoint, other.taskPoint)
                                          .isEquals();
            }
            return false;
        }
    }

    public void remove(final Task task) {
        Set<Task> needingEnforcing = getOutgoingTasksFor(task);
        graph.removeVertex(task);
        topLevelTasks.remove(task);
        fromChildToParent.remove(task);
        if (task.isContainer()) {
            for (Task t : task.getTasks()) {
                remove(t);
            }
        }
        enforcer.enforceRestrictionsOn(needingEnforcing);
    }

    public void remove(Dependency dependency) {
        graph.removeEdge(dependency);
        Task destination = dependency.getDestination();
        enforcer.enforceRestrictionsOn(destination);
    }

    public void add(Dependency dependency) {
        Task source = dependency.getSource();
        Task destination = dependency.getDestination();
        graph.addEdge(source, destination, dependency);
        enforceRestrictions(destination);
    }

    public void enforceRestrictions(final Task task) {
        enforcer.taskPositionModified(task);
    }

    public DeferedNotifier manualNotificationOn(IAction action) {
        return enforcer.manualNotification(action);
    }

    public boolean contains(Dependency dependency) {
        return graph.containsEdge(dependency);
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
        enforcer.enforceRestrictionsOn(task);
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
        public String toString() {
            return String.format("%s(%s)", task, pointType);
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



    private Task getTopmostFor(Task task) {
        Task result = task;
        while (fromChildToParent.containsKey(result)) {
            result = fromChildToParent.get(result);
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