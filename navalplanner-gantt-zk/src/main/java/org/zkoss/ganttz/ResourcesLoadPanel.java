package org.zkoss.ganttz;

import org.zkoss.ganttz.data.ResourceLoad;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

public class ResourcesLoadPanel extends XulElement implements AfterCompose {

    private TimeTracker timeTracker;

    public ResourcesLoadPanel() {
        ResourceLoadComponent rlc1 = new ResourceLoadComponent(
                new ResourceLoad("ResourceLoad 1"));
        ResourceLoadComponent rlc2 = new ResourceLoadComponent(
                new ResourceLoad("ResourceLoad 1"));

        rlc1.addInterval(40, "fullplanificated");
        rlc1.addInterval(20, "planificated");
        rlc1.addInterval(30, "overplanificated");
        rlc1.addInterval(10, "not_planificated");

        rlc2.addInterval(10, "overplanificated");
        rlc2.addInterval(20, "planificated");
        rlc2.addInterval(30, "fullplanificated");
        rlc2.addInterval(20, "planificated");
        rlc2.addInterval(20, "not_planificated");

        appendChild(rlc1);
        appendChild(rlc2);

    }

    @Override
    public void afterCompose() {
        //timeTracker.afterCompose();
    }

    public TimeTracker getTimeTracker() {
        return timeTracker;
    }

    public Planner getPlanner() {
        return (Planner) getParent();
    }

}