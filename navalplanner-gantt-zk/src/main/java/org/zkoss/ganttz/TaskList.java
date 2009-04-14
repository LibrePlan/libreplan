/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zkoss.ganttz;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.zkoss.ganttz.util.MenuBuilder;
import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.MenuBuilder.ItemAction;
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
 * 
 * @author Francisco Javier Moran Rúa
 * 
 */
public class TaskList extends XulElement implements AfterCompose {

    private static final int HEIGHT_PER_ROW = 40;

    private List<WeakReference<DependencyAddedListener>> listeners = new LinkedList<WeakReference<DependencyAddedListener>>();

    private ZoomLevelChangedListener zoomLevelChangedListener;

    private final WeakReferencedListeners<TaskRemovedListener> taskRemovedListeners = WeakReferencedListeners
            .create();

    private Menupopup contextMenu;

    private TaskEditFormComposer taskEditFormComposer = new TaskEditFormComposer();

    public synchronized void addTask(Task task) {
        task.setParent(this);
        invalidate();
        getDependencyList().invalidate();
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
    }

    private DependencyList getDependencyList() {
        return getGanttPanel().getDependencyList();
    }

    private void addListenersForTaskEditForm() {
        for (Task task : getTasks()) {
            addListenerForTaskEditForm(task);
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
                getContextMenuForTasks().open(task);
            }
        });
    }

    private void addContextMenu() {
        for (Task task : getTasks()) {
            addContextMenu(task);
        }
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
        return fakeRow.getUuid();
    }

    private TimeTracker getTimeTracker() {
        return (getGanttPanel()).getTimeTracker();
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
        if (zoomLevelChangedListener == null) {
            zoomLevelChangedListener = new ZoomLevelChangedListener() {
                @Override
                public void zoomLevelChanged(ZoomLevel detailLevel) {
                    for (Task task : getTasks()) {
                        task.zoomChanged();
                    }
                    response("adjust_height", new AuInvoke(TaskList.this,
                            "adjust_height"));
                }
            };
            getTimeTracker().addZoomListener(zoomLevelChangedListener);
        }
        addListenersForTaskEditForm();
        addContextMenu();
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
}
