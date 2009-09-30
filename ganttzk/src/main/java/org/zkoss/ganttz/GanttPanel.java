package org.zkoss.ganttz;

import java.util.List;

import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponent;
import org.zkoss.zk.au.out.AuInvoke;
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
        timeTrackerComponent = timeTrackerForGanttPanel(context
                .getTimeTracker());
        appendChild(timeTrackerComponent);
        dependencyList = new DependencyList(context);
        tasksLists = TaskList.createFor(context, editTaskCommand,
                commandsOnTasksContextualized);
        appendChild(tasksLists);
        appendChild(dependencyList);
    }

    private TimeTrackerComponent timeTrackerForGanttPanel(
            TimeTracker timeTracker) {
        return new TimeTrackerComponent(timeTracker) {
            @Override
            protected void scrollHorizontalPercentage(int pixelsDisplacement) {
                response("scroll_horizontal", new AuInvoke(GanttPanel.this,
                        "scroll_horizontal", "" + pixelsDisplacement));
            }
        };
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

    public void comingFromAnotherTab() {
        timeTrackerComponent.recreate();
    }
}