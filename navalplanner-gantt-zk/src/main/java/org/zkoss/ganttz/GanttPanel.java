package org.zkoss.ganttz;

import org.zkoss.ganttz.util.GanttDiagramGraph;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.Command;
import org.zkoss.zk.au.ComponentCommand;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

public class GanttPanel extends XulElement implements AfterCompose {

    private TaskList tasksLists;

    private TimeTracker timeTracker;

    private DependencyList dependencyList;

    private final GanttDiagramGraph diagramGraph;

    public GanttPanel(GanttDiagramGraph ganttDiagramGraph,
            TaskEditFormComposer taskEditFormComposer) {
        this.diagramGraph = ganttDiagramGraph;
        timeTracker = new TimeTracker(this);
        appendChild(timeTracker);
        tasksLists = TaskList.createFor(taskEditFormComposer,
                ganttDiagramGraph.getTopLevelTasks());
        dependencyList = new DependencyList();
        appendChild(tasksLists);
        appendChild(dependencyList);
    }

    @Override
    public void afterCompose() {
        tasksLists.afterCompose();
        dependencyList.setDependencies(tasksLists
                .asDependencies(diagramGraph.getVisibleDependencies()));
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

    private Command _onincreasecmd = new ComponentCommand("onIncrease", 0) {

        protected void process(AuRequest request) {
            String[] requestData = request.getData();
            int offset = Integer.parseInt(requestData[0]);
            getTimeTracker().onIncrease(offset);
        }

    };

    private Command _ondecreasecmd = new ComponentCommand("onDecrease", 0) {

        protected void process(AuRequest request) {
            String[] requestData = request.getData();
            int offset = Integer.parseInt(requestData[0]);
            getTimeTracker().onDecrease(offset);
        }

    };

    public Command getCommand(String cmdId) {

        Command c = null;

        if ("onIncrease".equals(cmdId))
            c = _onincreasecmd;
        else if ("onDecrease".equals(cmdId))
            c = _ondecreasecmd;

        return c;
    }

}