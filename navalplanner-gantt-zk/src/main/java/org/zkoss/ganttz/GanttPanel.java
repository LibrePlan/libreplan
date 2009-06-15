package org.zkoss.ganttz;

import org.zkoss.ganttz.util.DependencyRegistry;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

public class GanttPanel extends XulElement implements AfterCompose {

    private TaskList tasksLists;

    private TimeTracker timeTracker;

    private DependencyList dependencyList;

    private final DependencyRegistry dependencyRegistry;

    public GanttPanel(DependencyRegistry dependencyRegistry) {
        this.dependencyRegistry = dependencyRegistry;
        timeTracker = new TimeTracker();
        appendChild(timeTracker);
        tasksLists = TaskList.createFor(dependencyRegistry.getTasks());
        dependencyList = new DependencyList();
        appendChild(tasksLists);
        appendChild(dependencyList);
    }

    @Override
    public void afterCompose() {
        tasksLists.afterCompose();
        dependencyList.setDependencies(tasksLists
                .asDependencies(dependencyRegistry.getDependencies()));
        timeTracker.afterCompose();
        dependencyList.afterCompose();
    }

    public TimeTracker getTimeTracker() {
        return timeTracker;
    }

    public TaskList getTaskList() {
        return tasksLists;
    }

    public DependencyList getDependencyList() {
        return dependencyList;
    }

    public Planner getPlanner() {
        return (Planner) getParent();
    }

}