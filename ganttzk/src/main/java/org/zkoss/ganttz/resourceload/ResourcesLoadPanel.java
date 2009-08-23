package org.zkoss.ganttz.resourceload;

import java.util.List;

import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.TimeTracker;
import org.zkoss.ganttz.TimeTrackerComponent;
import org.zkoss.ganttz.data.resourceload.LoadTimelinesGroup;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

public class ResourcesLoadPanel extends XulElement implements AfterCompose {

    private TimeTrackerComponent timeTrackerComponent;

    private ResourceLoadLeftPane leftPane;

    private ResourceLoadList resourceLoadList;

    private final List<LoadTimelinesGroup> groups;

    public ResourcesLoadPanel(List<LoadTimelinesGroup> groups,
            TimeTracker timeTracker) {
        this.groups = groups;
        timeTrackerComponent = timeTrackerForResourcesLoadPanel(timeTracker);
        resourceLoadList = new ResourceLoadList(timeTracker, groups);
        leftPane = new ResourceLoadLeftPane(groups);
        appendChild(timeTrackerComponent);
        appendChild(leftPane);
        appendChild(resourceLoadList);
    }

    private TimeTrackerComponent timeTrackerForResourcesLoadPanel(
            TimeTracker timeTracker) {
        return new TimeTrackerComponent(timeTracker) {

            @Override
            protected void scrollHorizontalPercentage(int pixelsDisplacement) {
                response("", new AuInvoke(resourceLoadList,
                        "adjustScrollHorizontalPosition", pixelsDisplacement
                                + ""));
            }
        };
    }

    @Override
    public void afterCompose() {
        timeTrackerComponent.afterCompose();
    }

    public TimeTrackerComponent getTimeTrackerComponent() {
        return timeTrackerComponent;
    }

    public Planner getPlanner() {
        return (Planner) getParent();
    }

}