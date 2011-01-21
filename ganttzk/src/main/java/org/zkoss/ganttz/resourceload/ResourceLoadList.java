/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.zkoss.ganttz.resourceload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.impl.XulElement;

/**
 * Component to include a list of ResourceLoads inside the ResourcesLoadPanel.
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class ResourceLoadList extends XulElement {

    private final IZoomLevelChangedListener zoomListener;

    private Map<LoadTimeLine, ResourceLoadComponent> fromTimeLineToComponent = new HashMap<LoadTimeLine, ResourceLoadComponent>();

    public ResourceLoadList(TimeTracker timeTracker,
            MutableTreeModel<LoadTimeLine> timelinesTree) {
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
        for (LoadTimeLine l : line.getAllChildren()) {
            getComponentFor(l).detach();
        }

        Clients.evalJavaScript(getWidgetClass() + ".getInstance().recalculateTimeTrackerHeight();");
    }

    private ResourceLoadComponent getComponentFor(LoadTimeLine l) {
        ResourceLoadComponent resourceLoadComponent = fromTimeLineToComponent
                .get(l);
        return resourceLoadComponent;
    }

    public void expand(LoadTimeLine line, List<LoadTimeLine> closed) {
        ResourceLoadComponent parentComponent = getComponentFor(line);
        Component nextSibling = parentComponent.getNextSibling();

        List<LoadTimeLine> childrenToOpen = getChildrenReverseOrderFor(line);
        childrenToOpen.removeAll(closed);

        for (LoadTimeLine loadTimeLine : childrenToOpen) {
            ResourceLoadComponent child = getComponentFor(loadTimeLine);
            insertBefore(child, nextSibling);
            nextSibling = child;
        }

        Clients.evalJavaScript(getWidgetClass() + ".getInstance().recalculateTimeTrackerHeight();");
    }

    private List<LoadTimeLine> getChildrenReverseOrderFor(LoadTimeLine line) {
        List<LoadTimeLine> childrenOf = line.getAllChildren();
        Collections.reverse(childrenOf);
        return childrenOf;
    }

    public void addSeeScheduledOfListener(
            ISeeScheduledOfListener seeScheduledOfListener) {
        for (Entry<LoadTimeLine, ResourceLoadComponent> entry : fromTimeLineToComponent
                .entrySet()) {
            entry.getValue().addSeeScheduledOfListener(seeScheduledOfListener);
        }
    }
}
