package org.zkoss.ganttz;

import org.zkoss.ganttz.data.ResourceLoad;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

public class ResourcesLoadPanel extends XulElement implements AfterCompose {

    private TimeTrackerComponent timeTrackerComponent;

    public ResourcesLoadPanel() {
        ResourceLoadComponent rlc1 = new ResourceLoadComponent(
                new ResourceLoad("ResourceLoad 1"));
        ResourceLoadComponent rlc2 = new ResourceLoadComponent(
                new ResourceLoad("ResourceLoad 1"));

        rlc1.addInterval(40, 100);
        rlc1.addInterval(20, 80);
        rlc1.addInterval(30, 150);
        rlc1.addInterval(10, 0);

        rlc2.addInterval(10, 100);
        rlc2.addInterval(20, 60);
        rlc2.addInterval(30, 100);
        rlc2.addInterval(20, 0);
        rlc2.addInterval(20, 60);

        appendChild(rlc1);
        appendChild(rlc2);

    }

    @Override
    public void afterCompose() {
        //timeTracker.afterCompose();
    }

    public TimeTrackerComponent getTimeTrackerComponent() {
        return timeTrackerComponent;
    }

    public Planner getPlanner() {
        return (Planner) getParent();
    }

}