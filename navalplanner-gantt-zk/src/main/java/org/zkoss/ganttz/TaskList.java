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

import org.zkoss.ganttz.util.DependencyBean;
import org.zkoss.ganttz.util.MenuBuilder;
import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.MenuBuilder.ItemAction;
import org.zkoss.ganttz.util.TaskContainerBean;
import org.zkoss.ganttz.util.WeakReferencedListeners.ListenerNotification;
import org.zkoss.ganttz.util.zoom.ZoomLevel;
import org.zkoss.ganttz.util.zoom.ZoomLevelChangedListener;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.AbstractComponent;
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

    private static final int HEIGHT_PER_ROW = 20; /* 30  */

    private List<WeakReference<DependencyAddedListener>> listeners = new LinkedList<WeakReference<DependencyAddedListener>>();

    private ZoomLevelChangedListener zoomLevelChangedListener;

    private final WeakReferencedListeners<TaskRemovedListener> taskRemovedListeners = WeakReferencedListeners
            .create();

    private Menupopup contextMenu;

    private TaskEditFormComposer taskEditFormComposer = new TaskEditFormComposer();

    private List<TaskBean> originalTasks;

    public TaskList(List<TaskBean> tasks) {
        this.originalTasks = tasks;
    }

    public static TaskList createFor(List<TaskBean> tasks) {
        TaskList result = new TaskList(tasks);
        return result;
    }

    public List<Dependency> asDependencies(List<DependencyBean> dependencies) {
        List<? extends Object> children = getChildren();
        List<Task> tasks = Planner.findComponentsOfType(Task.class, children);
        Map<TaskBean, Task> taskByTaskBean = new HashMap<TaskBean, Task>();
        for (Task task : tasks) {
            taskByTaskBean.put(task.getTaskBean(), task);
        }
        List<Dependency> result = new ArrayList<Dependency>();
        for (DependencyBean dependencyBean : dependencies) {
            result.add(new Dependency(taskByTaskBean.get(dependencyBean
                    .getSource()), taskByTaskBean.get(dependencyBean
                    .getDestination())));
        }
        return result;
    }

    public void addTask(TaskBean newTask) {
        addTask(Task.asTask(newTask), true);
    }

    public void addTaskContainer(TaskContainerBean newTaskContainer) {
        addTask(TaskContainer.asTask(newTaskContainer), true);
    }

    public synchronized void addTask(final Task task, boolean relocate) {
        task.setParent(this);
        addContextMenu(task);
        addListenerForTaskEditForm(task);
        ListIterator<WeakReference<DependencyAddedListener>> iterator = listeners
                .listIterator();
        while (iterator.hasNext()) {
            DependencyAddedListener listener = iterator.next().get();
            if (listener != null) {
                task.addDependencyListener(listener);
            } else {
                iterator.remove();
            }
        }
        task.afterCompose();
        if (relocate) {
            response(null, new AuInvoke(task, "relocateAfterAdding"));
            setHeight(getHeight());// forcing smart update
            adjustZoomColumnsHeight();
            getGanttPanel().getDependencyList().redrawDependencies();
        }
    }

    private void addListenerForTaskEditForm(final Task task) {
        task.addEventListener("onDoubleClick", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                taskEditFormComposer.showEditFormFor(task);
            }
        });
    }

    private void addContextMenu(final Task task) {
        task.addEventListener("onRightClick", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                try {
                    getContextMenuForTasks().open(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addRemoveListener(TaskRemovedListener listener) {
        taskRemovedListeners.addListener(listener);
    }

    public void removeTask(final Task task) {
        removeChild(task);
        task.detach();
        taskRemovedListeners
                .fireEvent(new ListenerNotification<TaskRemovedListener>() {
                    @Override
                    public void doNotify(TaskRemovedListener listener) {
                        listener.taskRemoved(task);
                    }
                });
    }

    @Override
    public String getHeight() {
        return getTasksNumber() * HEIGHT_PER_ROW + "px";
    }

    public String getSameHeightElementId() {
        TimeTracker timeTracker = getTimeTracker();
        AbstractComponent fakeRow = timeTracker.getFakeRow();
        if (fakeRow == null)
            return "";
        return fakeRow.getUuid();
    }

    private TimeTracker getTimeTracker() {
        return getGanttPanel().getTimeTracker();
    }

    DatesMapper getMapper() {
        return getTimeTracker().getMapper();
    }

    private List<Task> getTasks() {
        ArrayList<Task> result = new ArrayList<Task>();
        for (Object child : getChildren()) {
            if (child instanceof Task) {
                result.add((Task) child);
            }
        }
        return result;
    }

    private int getTasksNumber() {
        return getTasks().size();
    }

    public void addTaskRemovedListener(TaskRemovedListener taskRemovedListener) {
        taskRemovedListeners.addListener(taskRemovedListener);
    }

    public void addDependencyListener(DependencyAddedListener listener) {
        listeners.add(new WeakReference<DependencyAddedListener>(listener));
        for (Task task : getTasks()) {
            task.addDependencyListener(listener);
        }
    }

    @Override
    public void afterCompose() {
        for (TaskBean taskBean : originalTasks) {
            addTask(Task.asTask(taskBean), false);
        }
        if (zoomLevelChangedListener == null) {
            zoomLevelChangedListener = new ZoomLevelChangedListener() {
                @Override
                public void zoomLevelChanged(ZoomLevel detailLevel) {
                    for (Task task : getTasks()) {
                        task.zoomChanged();
                    }
                    adjustZoomColumnsHeight();
                }
            };
            getTimeTracker().addZoomListener(zoomLevelChangedListener);
        }
    }

    private Menupopup getContextMenuForTasks() {
        if (contextMenu == null) {
            contextMenu = MenuBuilder.on(getPage(), getTasks()).item(
                    "Add Dependency", new ItemAction<Task>() {

                        @Override
                        public void onEvent(Task choosen, Event event) {
                            choosen.addDependency();
                        }
                    }).item("Erase", new ItemAction<Task>() {
                @Override
                public void onEvent(Task choosen, Event event) {
                    choosen.remove();
                }
            }).createWithoutSettingContext();
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

}
