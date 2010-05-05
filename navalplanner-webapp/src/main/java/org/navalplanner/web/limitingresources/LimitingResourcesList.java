/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.limitingresources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.ext.AfterCompose;

/**
 * Component to include a list of ResourceLoads inside the ResourcesLoadPanel.
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class LimitingResourcesList extends HtmlMacroComponent implements
        AfterCompose {

    private final IZoomLevelChangedListener zoomListener;

    private Map<LimitingResourceQueue, LimitingResourcesComponent> fromTimeLineToComponent = new HashMap<LimitingResourceQueue, LimitingResourcesComponent>();

    private final MutableTreeModel<LimitingResourceQueue> timelinesTree;

    private List<LimitingResourcesComponent> limitingResourcesComponents = new ArrayList<LimitingResourcesComponent>();

    public LimitingResourcesList(TimeTracker timeTracker,
            MutableTreeModel<LimitingResourceQueue> timelinesTree) {
        this.timelinesTree = timelinesTree;
        zoomListener = adjustTimeTrackerSizeListener();
        timeTracker.addZoomListener(zoomListener);
        LimitingResourceQueue current = timelinesTree.getRoot();
        List<LimitingResourceQueue> toInsert = new ArrayList<LimitingResourceQueue>();
        fill(timelinesTree, current, toInsert);
        insertAsComponents(timeTracker, toInsert);
    }

    private void fill(MutableTreeModel<LimitingResourceQueue> timelinesTree,
            LimitingResourceQueue current, List<LimitingResourceQueue> result) {
        final int length = timelinesTree.getChildCount(current);
        for (int i = 0; i < length; i++) {
            LimitingResourceQueue child = timelinesTree.getChild(current, i);
            result.add(child);
            fill(timelinesTree, child, result);
        }
    }

    private IZoomLevelChangedListener adjustTimeTrackerSizeListener() {
        return new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                response(null, new AuInvoke(LimitingResourcesList.this,
                        "adjustTimeTrackerSize"));
                response(null, new AuInvoke(LimitingResourcesList.this,
                        "adjustResourceLoadRows"));
            }
        };
    }

    private void insertAsComponents(TimeTracker timetracker,
            List<LimitingResourceQueue> children) {
        for (LimitingResourceQueue LimitingResourceQueue : children) {
            LimitingResourcesComponent component = LimitingResourcesComponent
                    .create(timetracker, LimitingResourceQueue);
            limitingResourcesComponents.add(component);
            appendChild(component);
            fromTimeLineToComponent.put(LimitingResourceQueue, component);
        }
    }

    public void collapse(LimitingResourceQueue line) {
    }

    private LimitingResourcesComponent getComponentFor(LimitingResourceQueue l) {
        LimitingResourcesComponent resourceLoadComponent = fromTimeLineToComponent
                .get(l);
        return resourceLoadComponent;
    }

    public void expand(LimitingResourceQueue line,
            List<LimitingResourceQueue> closed) {
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        for (LimitingResourcesComponent each : limitingResourcesComponents) {
            each.afterCompose();
        }
    }
}
