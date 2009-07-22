/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zkoss.ganttz;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.util.MenuBuilder;
import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.MenuBuilder.ItemAction;
import org.zkoss.ganttz.util.WeakReferencedListeners.ListenerNotification;
import org.zkoss.ganttz.util.zoom.ZoomLevel;
import org.zkoss.ganttz.util.zoom.ZoomLevelChangedListener;
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

    private List<WeakReference<DependencyAddedListener>> listeners = new LinkedList<WeakReference<DependencyAddedListener>>();

    private ZoomLevelChangedListener zoomLevelChangedListener;

    private final WeakReferencedListeners<TaskRemovedListener> taskRemovedListeners = WeakReferencedListeners
            .create();

    private Menupopup contextMenu;

    private List<Task> originalTasks;

    private final TaskEditFormComposer taskEditFormComposer;

    private final List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized;

    public TaskList(TaskEditFormComposer formComposer, List<Task> tasks, List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized) {
        this.taskEditFormComposer = formComposer;
        this.originalTasks = tasks;
        this.commandsOnTasksContextualized = commandsOnTasksContextualized;
    }

    public static TaskList createFor(TaskEditFormComposer formComposer,
            List<Task> tasks, List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized) {
        TaskList result = new TaskList(formComposer, tasks, commandsOnTasksContextualized);
        return result;
    }

    public List<DependencyComponent> asDependencyComponents(List<Dependency> dependencies) {
        List<? extends Object> children = getChildren();
        List<TaskComponent> taskComponents = Planner.findComponentsOfType(TaskComponent.class, children);
        Map<Task, TaskComponent> taskComponentByTask = new HashMap<Task, TaskComponent>();
        for (TaskComponent taskComponent : taskComponents) {
            taskComponent.publishTaskComponents(taskComponentByTask);
        }
        List<DependencyComponent> result = new ArrayList<DependencyComponent>();
        for (Dependency dependency : dependencies) {
            result.add(new DependencyComponent(taskComponentByTask.get(dependency
                    .getSource()), taskComponentByTask.get(dependency
                    .getDestination())));
        }
        return result;
    }

    public void addTask(Task newTask) {
        addTaskComponent(TaskComponent.asTaskComponent(newTask, this), true);
    }

    public synchronized void addTaskComponent(Component afterThis, final TaskComponent taskComponent,
            boolean relocate) {
        insertBefore(taskComponent, afterThis == null ? null : afterThis
                .getNextSibling());
        addContextMenu(taskComponent);
        addListenerForTaskComponentEditForm(taskComponent);
        ListIterator<WeakReference<DependencyAddedListener>> iterator = listeners
                .listIterator();
        while (iterator.hasNext()) {
            DependencyAddedListener listener = iterator.next().get();
            if (listener != null) {
                taskComponent.addDependencyListener(listener);
            } else {
                iterator.remove();
            }
        }
        taskComponent.afterCompose();
        if (relocate) {
            response(null, new AuInvoke(taskComponent, "relocateAfterAdding"));
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

    public synchronized void addTaskComponent(final TaskComponent taskComponent, boolean relocate) {
        addTaskComponent(null, taskComponent, relocate);
    }

    private void addListenerForTaskComponentEditForm(final TaskComponent taskComponent) {
        taskComponent.addEventListener("onDoubleClick", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                taskEditFormComposer.showEditFormFor(taskComponent);
            }
        });
    }

    private void addContextMenu(final TaskComponent taskComponent) {
        taskComponent.addEventListener("onRightClick", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                try {
                    getContextMenuForTasks().open(taskComponent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addRemoveListener(TaskRemovedListener listener) {
        taskRemovedListeners.addListener(listener);
    }

    public void removeTaskComponent(final TaskComponent taskComponent) {
        removeChild(taskComponent);
        taskComponent.detach();
        taskRemovedListeners
                .fireEvent(new ListenerNotification<TaskRemovedListener>() {
                    @Override
                    public void doNotify(TaskRemovedListener listener) {
                        listener.taskComponentRemoved(taskComponent);
                    }
                });
    }

    @Override
    public String getHeight() {
        return getTasksNumber() * HEIGHT_PER_ROW + "px";
    }

    private TimeTracker getTimeTracker() {
        return getGanttPanel().getTimeTracker();
    }

    DatesMapper getMapper() {
        return getTimeTracker().getMapper();
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

    private int getTasksNumber() {
        return getTaskComponents().size();
    }

    public void addTaskRemovedListener(TaskRemovedListener taskRemovedListener) {
        taskRemovedListeners.addListener(taskRemovedListener);
    }

    public void addDependencyListener(DependencyAddedListener listener) {
        listeners.add(new WeakReference<DependencyAddedListener>(listener));
        for (TaskComponent taskComponent : getTaskComponents()) {
            taskComponent.addDependencyListener(listener);
        }
    }

    @Override
    public void afterCompose() {
        for (Task task : originalTasks) {
            addTaskComponent(TaskComponent.asTaskComponent(task, this), false);
        }
        if (zoomLevelChangedListener == null) {
            zoomLevelChangedListener = new ZoomLevelChangedListener() {
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

    private Menupopup getContextMenuForTasks() {
        if (contextMenu == null) {
            MenuBuilder<TaskComponent> menuBuilder = MenuBuilder.on(getPage(), getTaskComponents());
            menuBuilder.item(
                    "Add Dependency", new ItemAction<TaskComponent>() {

                        @Override
                        public void onEvent(TaskComponent choosen, Event event) {
                            choosen.addDependency();
                        }
                    }).item("Erase", new ItemAction<TaskComponent>() {
                @Override
                public void onEvent(TaskComponent choosen, Event event) {
                    choosen.remove();
                }
            });
            for (CommandOnTaskContextualized<?> command :  commandsOnTasksContextualized) {
                menuBuilder.item(command.getName(), command.toItemAction());
            }
            contextMenu = menuBuilder.createWithoutSettingContext();
        }
        return contextMenu;
    }

    public Planner getPlanner() {
        return getGanttPanel().getPlanner();
    }

    private GanttPanel getGanttPanel() {
        return (GanttPanel) getParent();
    }

    public TaskEditFormComposer getModalFormComposer() {
        return taskEditFormComposer;
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

}