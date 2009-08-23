package org.zkoss.ganttz.resourceload;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.ganttz.TimeTracker;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.data.resourceload.LoadTimelinesGroup;
import org.zkoss.ganttz.util.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.util.zoom.ZoomLevel;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zul.impl.XulElement;

/**
 * Component to include a list of ResourceLoads inside the ResourcesLoadPanel.
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class ResourceLoadList extends XulElement {

    private final IZoomLevelChangedListener zoomListener;

    public ResourceLoadList(TimeTracker timeTracker,
            List<LoadTimelinesGroup> groups) {
        zoomListener = adjustTimeTrackerSizeListener();
        timeTracker.addZoomListener(zoomListener);
        for (LoadTimelinesGroup l : groups) {
            ArrayList<LoadTimeLine> toInsert = new ArrayList<LoadTimeLine>();
            toInsert.add(l.getPrincipal());
            toInsert.addAll(l.getChildren());
            insertAsComponents(timeTracker, toInsert);
        }
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
            appendChild(ResourceLoadComponent.create(timetracker, loadTimeLine));
        }
    }
}
