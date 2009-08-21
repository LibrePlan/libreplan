package org.zkoss.ganttz.resourceload;

import java.util.Collections;

import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.TimeTracker;
import org.zkoss.ganttz.TimeTrackerComponent;
import org.zkoss.ganttz.data.ResourceLoad;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

public class ResourcesLoadPanel extends XulElement implements AfterCompose {

    private TimeTrackerComponent timeTrackerComponent;

    private ResourceLoadList resourceLoadList;

    public ResourcesLoadPanel(TimeTracker timeTracker) {
        timeTrackerComponent = timeTrackerForResourcesLoadPanel(timeTracker);
        resourceLoadList = new ResourceLoadList(Collections
                .<ResourceLoad> emptyList());
        appendChild(timeTrackerComponent);
        appendChild(resourceLoadList);
    }

    private TimeTrackerComponent timeTrackerForResourcesLoadPanel(
            TimeTracker timeTracker) {
        return new TimeTrackerComponent(timeTracker) {

            @Override
            protected void scrollHorizontalPercentage(int pixelsDisplacement) {
                // TODO do the scroll displacement
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