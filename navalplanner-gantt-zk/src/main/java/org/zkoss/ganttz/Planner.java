package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.ganttz.util.DependencyRegistry;
import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

public class Planner extends XulElement implements AfterCompose {

    private DependencyAddedListener dependencyAddedListener;

    private Map<String, TaskBean> tasksById = new HashMap<String, TaskBean>();

    private DependencyRegistry dependencyRegistry = new DependencyRegistry();

    private DependencyRemovedListener dependencyRemovedListener;

    private TaskRemovedListener taskRemovedListener;

    public Planner() {
    }

    TaskList getTaskList() {
        List<Object> children = findOneComponentOfType(GanttPanel.class)
                .getChildren();
        return Planner.findComponentsOfType(TaskList.class, children).get(0);
    }

    private <T> T findOneComponentOfType(Class<T> type) {
        List<T> result = findComponentsOfType(type, getChildren());
        if (result.isEmpty()) {
            throw new RuntimeException("it should have found a "
                    + type.getSimpleName() + " in "
                    + Planner.class.getSimpleName());
        }
        return result.get(0);
    }

    void publish(String taskId, TaskBean task) {
        if (taskId == null)
            throw new IllegalArgumentException("taskId cannot be null");
        if (task == null)
            throw new IllegalArgumentException("task cannot be null");
        if (tasksById.containsKey(taskId))
            throw new IllegalArgumentException("task with id " + taskId
                    + " is already in " + tasksById);
        tasksById.put(taskId, task);
        dependencyRegistry.add(task);
    }

    TaskBean retrieve(String taskId) {
        return tasksById.get(taskId);
    }

    public static <T> List<T> findComponentsOfType(Class<T> type,
            List<? extends Object> children) {
        ArrayList<T> result = new ArrayList<T>();
        for (Object child : children) {
            if (type.isInstance(child)) {
                result.add(type.cast(child));
            }
        }
        return result;
    }

    private GanttPanel getGanntPanel() {
        return findOneComponentOfType(GanttPanel.class);
    }

    public DependencyList getDependencyList() {
        List<Object> children = getGanntPanel().getChildren();
        List<DependencyList> found = findComponentsOfType(DependencyList.class,
                children);
        if (found.isEmpty())
            return null;
        return found.get(0);
    }

    private ListDetails getDetails() {
        List<Object> children = getChildren();
        return Planner.findComponentsOfType(ListDetails.class, children).get(0);
    }

    @Override
    public void afterCompose() {
        TaskList taskList = getTaskList();
        dependencyAddedListener = new DependencyAddedListener() {
            @Override
            public void dependenceAdded(Dependency dependency) {
                getDependencyList().addDependency(dependency);
            }
        };
        taskList.addDependencyListener(dependencyAddedListener);
        taskRemovedListener = new TaskRemovedListener() {
            @Override
            public void taskRemoved(Task taskRemoved) {
                dependencyRegistry.remove(taskRemoved.getTaskBean());
                getDetails().taskRemoved(taskRemoved);
                getGanntPanel().invalidate();
            }
        };
        taskList.addTaskRemovedListener(taskRemovedListener);
        dependencyRemovedListener = new DependencyRemovedListener() {

            @Override
            public void dependenceRemoved(Dependency dependency) {
                dependencyRegistry.remove(dependency);
            }
        };
        getDependencyList().addDependencyRemovedListener(
                dependencyRemovedListener);
    }

    public void addTask(Task task) {
        getTaskList().addTask(task);
    }

    public void publishDependency(Dependency dependency) {
        dependencyRegistry.add(dependency);
    }

}
