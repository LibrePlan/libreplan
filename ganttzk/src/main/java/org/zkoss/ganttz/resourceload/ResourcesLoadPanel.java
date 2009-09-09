package org.zkoss.ganttz.resourceload;

import java.util.List;

import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.data.resourceload.LoadTimelinesGroup;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponent;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zkex.zul.Borderlayout;
import org.zkoss.zkex.zul.Center;
import org.zkoss.zkex.zul.West;
import org.zkoss.zul.Div;
import org.zkoss.zul.impl.XulElement;

public class ResourcesLoadPanel extends XulElement implements AfterCompose {

    private TimeTrackerComponent timeTrackerComponent;

    private ResourceLoadLeftPane leftPane;

    private ResourceLoadList resourceLoadList;

    private final List<LoadTimelinesGroup> groups;

    private MutableTreeModel<LoadTimeLine> treeModel;

    public ResourcesLoadPanel(List<LoadTimelinesGroup> groups,
            TimeTracker timeTracker) {
        this.groups = groups;
        treeModel = createModelForTree();
        timeTrackerComponent = timeTrackerForResourcesLoadPanel(timeTracker);
        resourceLoadList = new ResourceLoadList(timeTracker, treeModel);
        leftPane = new ResourceLoadLeftPane(treeModel, resourceLoadList);

        Borderlayout bl = new Borderlayout();
        bl.setHeight("300px");
        bl.setWidth("1600px");
        bl.setSclass("resourcesload");

        West w = new West();
        w.setSize("200px");
        w.setFlex(true);
        w.setSplittable(true);
        w.setCollapsible(true);
        w.setStyle("overflow: scroll");
        w.appendChild(leftPane);

        Center c = new Center();
        c.setFlex(true);
        c.setStyle("overflow: scroll");
        Div d = new Div();
        d.appendChild(getTimeTrackerComponent());
        d.appendChild(getResourceLoadList());
        c.appendChild(d);

        bl.appendChild(w);
        bl.appendChild(c);
        appendChild(bl);

    }

    private MutableTreeModel<LoadTimeLine> createModelForTree() {
        MutableTreeModel<LoadTimeLine> result = MutableTreeModel
                .create(LoadTimeLine.class);
        for (LoadTimelinesGroup loadTimelinesGroup : this.groups) {
            LoadTimeLine principal = loadTimelinesGroup.getPrincipal();
            result.addToRoot(principal);
            result.add(principal, loadTimelinesGroup.getChildren());
        }
        return result;
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
        leftPane.afterCompose();
    }

    public TimeTrackerComponent getTimeTrackerComponent() {
        // timeTrackerComponent.setWidth("100%");
        return timeTrackerComponent;
    }

    public ResourceLoadList getResourceLoadList() {
        return resourceLoadList;
    }

    public Planner getPlanner() {
        return (Planner) getParent();
    }

}