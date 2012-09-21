/*
 * This file is part of LibrePlan
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

package org.zkoss.ganttz;

import static org.zkoss.ganttz.i18n.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.lang.math.Fraction;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.adapters.IDisabilityConfiguration;
import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.Position;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.data.TaskContainer.IExpandListener;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponent;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.ganttz.util.MenuBuilder;
import org.zkoss.ganttz.util.MenuBuilder.ItemAction;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.impl.XulElement;

/**
 * Component to show the list of task in the planner
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
public class TaskList extends XulElement implements AfterCompose {

    private transient IZoomLevelChangedListener zoomLevelChangedListener;

    private List<Task> currentTotalTasks;

    private final CommandOnTaskContextualized<?> doubleClickCommand;

    private final List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized;

    private final FunctionalityExposedForExtensions<?> context;

    private final IDisabilityConfiguration disabilityConfiguration;

    private FilterAndParentExpandedPredicates predicate;

    private Set<Task> visibleTasks = new HashSet<Task>();

    private Map<Task, TaskComponent> taskComponentByTask;

    public TaskList(
            FunctionalityExposedForExtensions<?> context,
            CommandOnTaskContextualized<?> doubleClickCommand,
            List<Task> tasks,
            List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized,
            IDisabilityConfiguration disabilityConfiguration,
            FilterAndParentExpandedPredicates predicate) {
        this.context = context;
        this.doubleClickCommand = doubleClickCommand;
        this.currentTotalTasks = new ArrayList<Task>(tasks);
        this.commandsOnTasksContextualized = commandsOnTasksContextualized;
        this.disabilityConfiguration = disabilityConfiguration;
        this.predicate = predicate;
    }

    public void updateCompletion(String progressType) {
        for (TaskComponent task: getTaskComponents()) {
            task.updateCompletion(progressType);
            task.updateCompletionReportedHours();
            task.updateTooltipText(progressType);
        }
    }

    public List<Task> getAllTasks() {
        return new ArrayList<Task>(currentTotalTasks);
    }

    public static TaskList createFor(
            FunctionalityExposedForExtensions<?> context,
            CommandOnTaskContextualized<?> doubleClickCommand,
            List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized,
            IDisabilityConfiguration disabilityConfiguration,
            FilterAndParentExpandedPredicates predicate) {
        TaskList result = new TaskList(context, doubleClickCommand, context
                .getDiagramGraph().getTopLevelTasks(),
                commandsOnTasksContextualized, disabilityConfiguration,
                predicate);
        return result;
    }

    public List<DependencyComponent> asDependencyComponents(
            Collection<? extends Dependency> dependencies) {
        List<DependencyComponent> result = new ArrayList<DependencyComponent>();
        for (Dependency dependency : dependencies) {
            result.add(new DependencyComponent(taskComponentByTask
                    .get(dependency.getSource()), taskComponentByTask
                    .get(dependency.getDestination()), dependency));
        }
        return result;
    }

    public DependencyComponent asDependencyComponent(Dependency dependency) {
        return asDependencyComponents(Arrays.asList(dependency)).get(0);
    }

    private synchronized void addTaskComponent(TaskRow beforeThis,
            final TaskComponent taskComponent, boolean relocate) {
        insertBefore(taskComponent.getRow(), beforeThis);
        addContextMenu(taskComponent);
        addListenerForTaskComponentEditForm(taskComponent);
        taskComponent.afterCompose();
        if (relocate) {
            getGanttPanel().adjustZoomColumnsHeight();
        }
    }

    public void addTasks(Position position, Collection<? extends Task> newTasks) {
        createAndPublishComponentsIfNeeded(newTasks);
        if (position.isAppendToTop()) {
            currentTotalTasks.addAll(newTasks);
        } else if (position.isAtTop()) {
            final int insertionPosition = position.getInsertionPosition();
            currentTotalTasks.addAll(insertionPosition, newTasks);
        }
        // if the position is children of some already existent task when
        // reloading it will be added if the predicate tells so
        reload(true);
    }

    public TaskComponent find(Task task) {
        List<TaskComponent> taskComponents = getTaskComponents();
        for (TaskComponent taskComponent : taskComponents) {
            if (taskComponent.getTask().equals(task)) {
                return taskComponent;
            }
        }
        return null;
    }

    private void addListenerForTaskComponentEditForm(
            final TaskComponent taskComponent) {
        if (doubleClickCommand == null) {
            return;
        }
        taskComponent.addEventListener("onDoubleClick", new EventListener() {

            @Override
            public void onEvent(Event event) {
                doubleClickCommand.doAction(taskComponent);
            }
        });
    }

    private void addContextMenu(final TaskComponent taskComponent) {
        taskComponent.setContext(getContextMenuFor(taskComponent));
    }

    private TimeTrackerComponent getTimeTrackerComponent() {
        return getGanttPanel().getTimeTrackerComponent();
    }

    IDatesMapper getMapper() {
        return getTimeTracker().getMapper();
    }

    private TimeTracker getTimeTracker() {
        return getTimeTrackerComponent().getTimeTracker();
    }

    protected List<TaskComponent> getTaskComponents() {
        ArrayList<TaskComponent> result = new ArrayList<TaskComponent>();
        for (Object child : getChildren()) {
            if (child instanceof TaskRow) {
                TaskRow row = (TaskRow) child;
                result.add(row.getChild());
            }
        }
        return result;
    }

    public int getTasksNumber() {
        return getTaskComponents().size();
    }

    @Override
    public void afterCompose() {
        publishOriginalTasksAsComponents();
        registerZoomLevelChangedListener();
        reload(false);
    }

    private void publishOriginalTasksAsComponents() {
        taskComponentByTask = new HashMap<Task, TaskComponent>();
        createAndPublishComponentsIfNeeded(currentTotalTasks);
    }

    private List<TaskComponent> createAndPublishComponentsIfNeeded(
            Collection<? extends Task> newTasks) {
        if (predicate.isFilterContainers()) {
            List<Task> taskLeafs = new ArrayList<Task>();
            for (Task task : newTasks) {
                taskLeafs.addAll(task.getAllTaskLeafs());
            }
            newTasks = taskLeafs;
        }

        List<TaskComponent> result = new ArrayList<TaskComponent>();
        for (Task task : newTasks) {
            TaskComponent taskComponent = taskComponentByTask.get(task);
            if (taskComponent == null) {
                taskComponent = TaskComponent.asTaskComponent(task,
                        disabilityConfiguration);
                taskComponent.publishTaskComponents(taskComponentByTask);
            }
            if (task.isContainer()) {
                addExpandListenerTo((TaskContainer) task);
            }
            result.add(taskComponent);
        }
        return result;
    }

    private Map<TaskContainer, IExpandListener> autoRemovedListers = new WeakHashMap<TaskContainer, IExpandListener>();

    private void addExpandListenerTo(TaskContainer container) {
        if (autoRemovedListers.containsKey(container)) {
            return;
        }
        IExpandListener expandListener = new IExpandListener() {

            @Override
            public void expandStateChanged(boolean isNowExpanded) {
                reload(true);
            }
        };
        container.addExpandListener(expandListener);
        autoRemovedListers.put(container, expandListener);
    }

    private void registerZoomLevelChangedListener() {
        if (zoomLevelChangedListener == null) {
            zoomLevelChangedListener = new IZoomLevelChangedListener() {
                @Override
                public void zoomLevelChanged(ZoomLevel detailLevel) {
                    for (TaskComponent taskComponent : getTaskComponents()) {
                        taskComponent.zoomChanged();
                    }
                    adjustZoomPositionScroll();
                }
            };
            getTimeTracker().addZoomListener(zoomLevelChangedListener);
        }
    }

    public LocalDate toDate(int pixels, Fraction pixelsPerDay, Interval interval) {
        int daysInto = Fraction.getFraction(pixels, 1).divideBy(pixelsPerDay)
                .intValue();
        return interval.getStart().plusDays(daysInto);
    }

    private Map<TaskComponent, Menupopup> contextMenus = new HashMap<TaskComponent, Menupopup>();

    private Menupopup getContextMenuFor(TaskComponent taskComponent) {
        if (contextMenus.get(taskComponent) == null) {
            MenuBuilder<TaskComponent> menuBuilder = MenuBuilder.on(getPage(),
                    getTaskComponents());
            if (disabilityConfiguration.isAddingDependenciesEnabled()) {
                menuBuilder.item(_("Add Dependency"),
                        "/common/img/ico_dependency.png",
                        new ItemAction<TaskComponent>() {

                            @Override
                            public void onEvent(TaskComponent choosen,
                                    Event event) {
                                choosen.addDependency();
                            }
                        });
            }
            for (CommandOnTaskContextualized<?> command : commandsOnTasksContextualized) {
                if (command.accepts(taskComponent)) {
                        menuBuilder.item(command.getName(), command.getIcon(),
                            command.toItemAction());
                }
            }
            Menupopup result = menuBuilder.createWithoutSettingContext();
            contextMenus.put(taskComponent, result);
            return result;
        }
        return contextMenus.get(taskComponent);
    }

    GanttPanel getGanttPanel() {
        return (GanttPanel) getParent();
    }

    private void adjustZoomPositionScroll() {
        getTimeTrackerComponent().movePositionScroll();
    }

    public void redrawDependencies() {
        getGanttPanel().getDependencyList().redrawDependencies();
    }

    public void remove(Task task) {
        currentTotalTasks.remove(task);
        for (TaskComponent taskComponent : getTaskComponents()) {
            if (taskComponent.getTask().equals(task)) {
                taskComponent.remove();
                return;
            }
        }
    }

    public void addDependency(TaskComponent source, TaskComponent destination) {
        context.addDependency(new Dependency(source.getTask(), destination
                .getTask(), DependencyType.END_START));
    }

    public IDisabilityConfiguration getDisabilityConfiguration() {
        return disabilityConfiguration;
    }

    private void reload(boolean relocate) {
        ArrayList<Task> tasksPendingToAdd = new ArrayList<Task>();
        reload(currentTotalTasks, tasksPendingToAdd, relocate);
        addPendingTasks(tasksPendingToAdd, null, relocate);
        getGanttPanel().getDependencyList().redrawDependencies();
    }

    private void reload(List<Task> tasks, List<Task> tasksPendingToAdd,
            boolean relocate) {
        for (Task task : tasks) {
            if (visibleTasks.contains(task)) {
                addPendingTasks(tasksPendingToAdd, rowFor(task),
                        relocate);
            }
            final boolean isShown = visibleTasks.contains(task);
            if (predicate.accepts(task) != isShown) {
                if (isShown) {
                    makeDisappear(task);
                } else {
                    tasksPendingToAdd.add(task);
                }
            }
            if (task instanceof TaskContainer) {
                reload(task.getTasks(), tasksPendingToAdd, relocate);
            }
        }
    }

    private void makeDisappear(Task task) {
        TaskComponent taskComponent = find(task);
        removeChild(taskComponent.getRow());
        visibleTasks.remove(task);
        task.setVisible(false);
    }

    private TaskRow rowFor(Task task) {
        TaskComponent taskComponent = find(task);
        return taskComponent == null ? null : taskComponent.getRow();
    }

    private void addPendingTasks(List<Task> tasksPendingToAdd,
            TaskRow insertBefore, boolean relocate) {
        if (tasksPendingToAdd.isEmpty()) {
            return;
        }
        for (TaskComponent each : createAndPublishComponentsIfNeeded(tasksPendingToAdd)) {
            addTaskComponent(insertBefore, each, relocate);
            visibleTasks.add(each.getTask());
            each.getTask().setVisible(true);
        }
        tasksPendingToAdd.clear();
    }

    public void setPredicate(FilterAndParentExpandedPredicates predicate) {
        this.predicate = predicate;
        reload(false);
    }

    public FunctionalityExposedForExtensions<?> getFunctionalityExposedForExtensionsContext() {
        return context;
    }

}
