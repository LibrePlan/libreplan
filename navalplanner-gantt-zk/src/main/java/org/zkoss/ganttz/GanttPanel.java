package org.zkoss.ganttz;

import java.util.List;

import org.zkoss.zul.impl.XulElement;

public class GanttPanel extends XulElement {

    public TimeTracker getTimeTracker() {
        List<Object> children = getChildren();
        return Planner.findComponentsOfType(TimeTracker.class, children).get(0);
    }

    public TaskList getTaskList() {
        List<Object> children = getChildren();
        return Planner.findComponentsOfType(TaskList.class, children).get(0);
    }

    public DependencyList getDependencyList() {
        List<Object> children = getChildren();
        return Planner.findComponentsOfType(DependencyList.class, children)
                .get(0);
    }

    public Planner getPlanner() {
        return (Planner) getParent();
    }

}