/*
 * This file is part of ###PROJECT_NAME###
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

package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.ganttz.adapters.IDisabilityConfiguration;
import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.Position;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponent;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.ganttz.util.MenuBuilder;
import org.zkoss.ganttz.util.MenuBuilder.ItemAction;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;
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

    private static final int HEIGHT_PER_ROW = 20; /* 30 */

    private transient IZoomLevelChangedListener zoomLevelChangedListener;

    private List<Task> originalTasks;

    private final CommandOnTaskContextualized<?> doubleClickCommand;

    private final List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized;

    private final FunctionalityExposedForExtensions<?> context;

    private final IDisabilityConfiguration disabilityConfiguration;

    public TaskList(
            FunctionalityExposedForExtensions<?> context,
            CommandOnTaskContextualized<?> doubleClickCommand,
            List<Task> tasks,
            List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized,
            IDisabilityConfiguration disabilityConfiguration) {
        this.context = context;
        this.doubleClickCommand = doubleClickCommand;
        this.originalTasks = tasks;
        this.commandsOnTasksContextualized = commandsOnTasksContextualized;
        this.disabilityConfiguration = disabilityConfiguration;
    }

    public static TaskList createFor(
            FunctionalityExposedForExtensions<?> context,
            CommandOnTaskContextualized<?> doubleClickCommand,
            List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized,
            IDisabilityConfiguration disabilityConfiguration) {
        TaskList result = new TaskList(context, doubleClickCommand, context
                .getDiagramGraph().getTopLevelTasks(),
                commandsOnTasksContextualized, disabilityConfiguration);
        return result;
    }

    public List<DependencyComponent> asDependencyComponents(
            Collection<? extends Dependency> dependencies) {
        List<? extends Object> children = getChildren();
        List<TaskComponent> taskComponents = ComponentsFinder.findComponentsOfType(
                TaskComponent.class, children);
        Map<Task, TaskComponent> taskComponentByTask = new HashMap<Task, TaskComponent>();
        for (TaskComponent taskComponent : taskComponents) {
            taskComponent.publishTaskComponents(taskComponentByTask);
        }
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

    public synchronized void addTaskComponent(Component beforeThis,
            final TaskComponent taskComponent, boolean relocate) {
        final boolean isFirst = getFirstTopTaskComponent() == null
                || getFirstTopTaskComponent().equals(beforeThis);
        insertBefore(taskComponent, beforeThis);

        addContextMenu(taskComponent);
        addListenerForTaskComponentEditForm(taskComponent);
        taskComponent.afterCompose();
        if (relocate) {
            response(null, new AuInvoke(taskComponent,
                    isFirst ? "relocateFirstAfterAdding"
                            : "relocateAfterAdding"));
            setHeight(getHeight());// forcing smart update
            adjustZoomColumnsHeight();
            getGanttPanel().getDependencyList().redrawDependencies();
        }
        if (taskComponent instanceof TaskContainerComponent) {
            TaskContainerComponent container = (TaskContainerComponent) taskComponent;
            if (container.isExpanded()) {
                container.open();
            }
        }

    }

    public synchronized void addTaskComponent(
            final TaskComponent taskComponent, boolean relocate) {
        addTaskComponent(null, taskComponent, relocate);
    }

    private TaskComponent getFirstTopTaskComponent() {
        List<TaskComponent> taskComponents = getTopLevelTaskComponents();
        if (taskComponents.isEmpty()) {
            return null;
        }
        return taskComponents.get(0);
    }

    public void addTasks(Position position, Collection<? extends Task> newTasks) {
        if (position.isAppendToTop()) {
            for (Task t : newTasks) {
                addTaskComponent(TaskComponent.asTaskComponent(t, this), true);
            }
        } else if (position.isAtTop()) {
            final int insertionPosition = position.getInsertionPosition();
            List<TaskComponent> topTaskComponents = getTopLevelTaskComponents();
            Component beforeThis = insertionPosition < topTaskComponents.size() ? topTaskComponents
                    .get(insertionPosition)
                    : null;
            for (Task t : newTasks) {
                TaskComponent toAdd = TaskComponent.asTaskComponent(t, this);
                addTaskComponent(beforeThis, toAdd, true);
                beforeThis = toAdd.getNextSibling();
            }
        } else {
            Task mostRemoteAncestor = position.getMostRemoteAncestor();
            TaskComponent taskComponent = find(mostRemoteAncestor);
            if (taskComponent instanceof TaskContainerComponent) {
                TaskContainerComponent container = (TaskContainerComponent) taskComponent;
                container.insert(position, newTasks);
            } else {
                // TODO turn taskComponent into container
            }

        }
    }

    TaskComponent find(Task task) {
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
            public void onEvent(Event event) throws Exception {
                doubleClickCommand.doAction(taskComponent);
            }
        });
    }

    private void addContextMenu(final TaskComponent taskComponent) {
        taskComponent.addEventListener("onRightClick", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                try {
                    getContextMenuFor(taskComponent).open(taskComponent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public String getHeight() {
        return getTasksNumber() * HEIGHT_PER_ROW + "px";
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

    private List<TaskComponent> getTopLevelTaskComponents() {
        List<TaskComponent> result = new ArrayList<TaskComponent>();
        for (TaskComponent taskComponent : getTaskComponents()) {
            if (taskComponent.isTopLevel()) {
                result.add(taskComponent);
            }
        }
        return result;
    }

    private List<TaskComponent> getTaskComponents() {
        ArrayList<TaskComponent> result = new ArrayList<TaskComponent>();
        for (Object child : getChildren()) {
            if (child instanceof TaskComponent) {
                result.add((TaskComponent) child);
            }
        }
        return result;
    }

    public int getTasksNumber() {
        return getTaskComponents().size();
    }

    @Override
    public void afterCompose() {
        for (Task task : originalTasks) {
            addTaskComponent(TaskComponent.asTaskComponent(task, this), false);
        }
        if (zoomLevelChangedListener == null) {
            zoomLevelChangedListener = new IZoomLevelChangedListener() {
                @Override
                public void zoomLevelChanged(ZoomLevel detailLevel) {
                    for (TaskComponent taskComponent : getTaskComponents()) {
                        taskComponent.zoomChanged();
                    }
                    adjustZoomColumnsHeight();
                }
            };
            getTimeTracker().addZoomListener(zoomLevelChangedListener);
        }
    }

    private Map<TaskComponent, Menupopup> contextMenus = new HashMap<TaskComponent, Menupopup>();

    private Menupopup getContextMenuFor(TaskComponent taskComponent) {
        if (contextMenus.get(taskComponent) == null) {
            MenuBuilder<TaskComponent> menuBuilder = MenuBuilder.on(getPage(),
                    getTaskComponents());
            if (disabilityConfiguration.isAddingDependenciesEnabled()) {
                menuBuilder.item("Add Dependency",
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

    public void adjustZoomColumnsHeight() {
        response("adjust_height", new AuInvoke(TaskList.this, "adjust_height"));
    }

    public void hideTaskComponent(TaskComponent subtaskComponent) {
        removeChild(subtaskComponent);
        subtaskComponent.setParent(null);
    }

    public void redrawDependencies() {
        getGanttPanel().getDependencyList().redrawDependencies();
    }

    public void remove(Task task) {
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
}
