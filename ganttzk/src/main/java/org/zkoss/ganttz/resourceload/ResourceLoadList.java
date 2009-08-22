package org.zkoss.ganttz.resourceload;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.ganttz.TimeTracker;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.data.resourceload.LoadTimelinesGroup;
import org.zkoss.zul.impl.XulElement;

/**
 * Component to include a list of ResourceLoads inside the ResourcesLoadPanel.
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class ResourceLoadList extends XulElement {


    public ResourceLoadList(TimeTracker timeTracker,
            List<LoadTimelinesGroup> groups) {
        for (LoadTimelinesGroup l : groups) {
            ArrayList<LoadTimeLine> toInsert = new ArrayList<LoadTimeLine>();
            toInsert.add(l.getPrincipal());
            toInsert.addAll(l.getChildren());
            insertAsComponents(timeTracker, toInsert);
        }
    }

    private void insertAsComponents(TimeTracker timetracker,
            List<LoadTimeLine> children) {
        for (LoadTimeLine loadTimeLine : children) {
            appendChild(ResourceLoadComponent.create(timetracker, loadTimeLine));
        }
    }
}

