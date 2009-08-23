package org.zkoss.ganttz;

import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.zk.ui.Component;

public class TasksPlanningTab implements ITab {

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

    @Override
    public void addToParent(Component parent) {
    }

    @Override
    public void hide() {
        leftPane.detach();
        ganttPanel.detach();
    }

    @Override
    public void show() {
        leftPane.setParent(planner);
        ganttPanel.setParent(planner);
        ganttPanel.comingFromAnotherTab();
    }

    @Override
    public String getName() {
        return "Tasks Planning";
    }
}
