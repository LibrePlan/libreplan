package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.ganttz.util.DependencyRegistry;
import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

public class Planner extends XulElement implements AfterCompose {

    private DependencyAddedListener dependencyAddedListener;

    private DependencyRegistry dependencyRegistry = new DependencyRegistry();

    private DependencyRemovedListener dependencyRemovedListener;

    private TaskRemovedListener taskRemovedListener;

    private ListDetails listDetails;

    private GanttPanel ganttPanel;

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

    public String getContextPath() {
        return Executions.getCurrent().getContextPath();
    }

    private void removePreviousGanntPanel() {
        List<Object> children = getChildren();
        for (GanttPanel ganttPanel : findComponentsOfType(GanttPanel.class,
                children)) {
            removeChild(ganttPanel);
        }
    }

    public DependencyList getDependencyList() {
        List<Object> children = ganttPanel.getChildren();
        List<DependencyList> found = findComponentsOfType(DependencyList.class,
                children);
        if (found.isEmpty())
            return null;
        return found.get(0);
    }

    private void removePreviousDetails() {
        List<Object> children = getChildren();
        for (ListDetails l : Planner.findComponentsOfType(ListDetails.class,
                children)) {
            removeChild(l);
        }
    }

    public TaskEditFormComposer getModalFormComposer() {
        return getTaskList().getModalFormComposer();
    }

    @Override
    public void afterCompose() {
        if (dependencyRegistry == null)
            throw new IllegalStateException("dependencyRegistry must be set");
        ganttPanel.afterCompose();
        TaskList taskList = getTaskList();
        dependencyAddedListener = new DependencyAddedListener() {
            @Override
            public void dependenceAdded(Dependency dependency) {
                getDependencyList().addDependency(dependency);
                publishDependency(dependency);
            }
        };
        taskList.addDependencyListener(dependencyAddedListener);
        taskRemovedListener = new TaskRemovedListener() {
            @Override
            public void taskRemoved(Task taskRemoved) {
                dependencyRegistry.remove(taskRemoved.getTaskBean());
                listDetails.taskRemoved(taskRemoved.getTaskBean());
                ganttPanel.invalidate();
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

    public void addTask(TaskBean newTask) {
        getTaskList().addTask(newTask);
        dependencyRegistry.add(newTask);
    }

    private void publishDependency(Dependency dependency) {
        dependencyRegistry.add(dependency.getDependencyBean());
    }

    public DependencyRegistry getDependencyRegistry() {
        return dependencyRegistry;
    }

    public void setDependencyRegistry(DependencyRegistry dependencyRegistry) {
        this.dependencyRegistry = dependencyRegistry;
        removePreviousDetails();
        this.listDetails = new ListDetails(dependencyRegistry.getTasks());
        insertBefore(this.listDetails,
                (Component) (getChildren().isEmpty() ? null : getChildren()
                        .get(0)));
        this.listDetails.afterCompose();
        removePreviousGanntPanel();
        this.ganttPanel = new GanttPanel(this.dependencyRegistry);
        appendChild(ganttPanel);
    }

}
