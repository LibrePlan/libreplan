package org.zkoss.ganttz;

import java.util.List;

import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.Command;
import org.zkoss.zk.au.ComponentCommand;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

public class GanttPanel extends XulElement implements AfterCompose {

    private TaskList tasksLists;

    private TimeTrackerComponent timeTrackerComponent;

    private DependencyList dependencyList;

    private final GanttDiagramGraph diagramGraph;

    public GanttPanel(
            FunctionalityExposedForExtensions<?> context,
            List<? extends CommandOnTaskContextualized<?>> commandsOnTasksContextualized,
            CommandOnTaskContextualized<?> editTaskCommand) {
        this.diagramGraph = context.getDiagramGraph();
        timeTrackerComponent = new TimeTrackerComponent(this);
        appendChild(timeTrackerComponent);
        tasksLists = TaskList.createFor(context, editTaskCommand,
                commandsOnTasksContextualized);
        dependencyList = new DependencyList(context);
        appendChild(tasksLists);
        appendChild(dependencyList);
    }

    @Override
    public void afterCompose() {
        tasksLists.afterCompose();
        dependencyList.setDependencyComponents(tasksLists
                .asDependencyComponents(diagramGraph.getVisibleDependencies()));
        timeTrackerComponent.afterCompose();
        dependencyList.afterCompose();
    }

    public TimeTrackerComponent getTimeTrackerComponent() {
        return timeTrackerComponent;
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
            getTimeTrackerComponent().onIncrease(offset);
        }

    };

    private Command _ondecreasecmd = new ComponentCommand("onDecrease", 0) {

        protected void process(AuRequest request) {
            String[] requestData = request.getData();
            int offset = Integer.parseInt(requestData[0]);
            getTimeTrackerComponent().onDecrease(offset);
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