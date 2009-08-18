package org.zkoss.ganttz;

public class TasksPlanningTab {

    private final Planner planner;
    private final LeftPane leftPane;
    private final GanttPanel ganttPanel;

    public TasksPlanningTab(Planner planner, LeftPane leftPane,
            GanttPanel ganttPanel) {
        this.planner = planner;
        this.leftPane = leftPane;
        this.ganttPanel = ganttPanel;
    }

    public void afterCompose() {
        leftPane.setParent(planner);
        ganttPanel.setParent(planner);
        leftPane.afterCompose();
        ganttPanel.afterCompose();
    }
}
