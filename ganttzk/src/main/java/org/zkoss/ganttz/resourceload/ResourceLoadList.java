package org.zkoss.ganttz.resourceload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.impl.XulElement;

/**
 * Component to include a list of ResourceLoads inside the ResourcesLoadPanel.
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class ResourceLoadList extends XulElement {

    private final IZoomLevelChangedListener zoomListener;

    private Map<LoadTimeLine, ResourceLoadComponent> fromTimeLineToComponent = new HashMap<LoadTimeLine, ResourceLoadComponent>();

    private final MutableTreeModel<LoadTimeLine> timelinesTree;

    public ResourceLoadList(TimeTracker timeTracker,
            MutableTreeModel<LoadTimeLine> timelinesTree) {
        this.timelinesTree = timelinesTree;
        zoomListener = adjustTimeTrackerSizeListener();
        timeTracker.addZoomListener(zoomListener);
        LoadTimeLine current = timelinesTree.getRoot();
        List<LoadTimeLine> toInsert = new ArrayList<LoadTimeLine>();
        fill(timelinesTree, current, toInsert);
        insertAsComponents(timeTracker, toInsert);
    }

    private void fill(MutableTreeModel<LoadTimeLine> timelinesTree,
            LoadTimeLine current, List<LoadTimeLine> result) {
        final int length = timelinesTree.getChildCount(current);
        for (int i = 0; i < length; i++) {
            LoadTimeLine child = timelinesTree.getChild(current, i);
            result.add(child);
            fill(timelinesTree, child, result);
        }
    }

    private List<LoadTimeLine> getChildrenOf(LoadTimeLine parent) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        final int length = timelinesTree.getChildCount(parent);
        for (int i = 0; i < length; i++) {
            result.add(timelinesTree.getChild(parent, i));
        }
        return result;
    }

    private IZoomLevelChangedListener adjustTimeTrackerSizeListener() {
        return new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                response(null, new AuInvoke(ResourceLoadList.this,
                        "adjustTimeTrackerSize"));
                response(null, new AuInvoke(ResourceLoadList.this,
                        "adjustResourceLoadRows"));
            }
        };
    }

    private void insertAsComponents(TimeTracker timetracker,
            List<LoadTimeLine> children) {
        for (LoadTimeLine loadTimeLine : children) {
            ResourceLoadComponent component = ResourceLoadComponent.create(
                    timetracker, loadTimeLine);
            appendChild(component);
            fromTimeLineToComponent.put(loadTimeLine, component);
        }
    }

    public void collapse(LoadTimeLine line) {
        for (LoadTimeLine l : getChildrenOf(line)) {
            getComponentFor(l).detach();
        }
    }

    private ResourceLoadComponent getComponentFor(LoadTimeLine l) {
        ResourceLoadComponent resourceLoadComponent = fromTimeLineToComponent
                .get(l);
        return resourceLoadComponent;
    }

    public void expand(LoadTimeLine line) {
        ResourceLoadComponent parentComponent = getComponentFor(line);
        Component nextSibling = parentComponent.getNextSibling();
        for (LoadTimeLine loadTimeLine : getChildrenReverseOrderFor(line)) {
            ResourceLoadComponent child = getComponentFor(loadTimeLine);
            insertBefore(child, nextSibling);
            nextSibling = child;
        }

    }

    private List<LoadTimeLine> getChildrenReverseOrderFor(LoadTimeLine line) {
        List<LoadTimeLine> childrenOf = getChildrenOf(line);
        Collections.reverse(childrenOf);
        return childrenOf;
    }
}
