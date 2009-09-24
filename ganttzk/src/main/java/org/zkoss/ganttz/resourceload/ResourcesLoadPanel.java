package org.zkoss.ganttz.resourceload;

import java.util.List;

import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.data.resourceload.LoadTimelinesGroup;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponent;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.HtmlMacroComponent;

public class ResourcesLoadPanel extends HtmlMacroComponent {

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
        super.afterCompose();
        getFellow("insertionPointLeftPanel").appendChild(leftPane);
        leftPane.afterCompose();

        getFellow("insertionPointRightPanel").appendChild(timeTrackerComponent);
        getFellow("insertionPointRightPanel").appendChild(resourceLoadList);

        TimeTrackerComponent timetrackerheader = (TimeTrackerComponent) timeTrackerComponent
                .clone();
        getFellow("insertionPointTimetracker").appendChild(timetrackerheader);

        timetrackerheader.afterCompose();
        timeTrackerComponent.afterCompose();
    }

    public Planner getPlanner() {
        return (Planner) getParent();
    }

}