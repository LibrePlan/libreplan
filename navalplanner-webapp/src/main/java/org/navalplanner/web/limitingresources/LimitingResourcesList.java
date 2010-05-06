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
 * Component to include a list of {@link LimitingResourceQueue} inside the {@link LimitingResourcesPanel}
 *
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class LimitingResourcesList extends HtmlMacroComponent implements
        AfterCompose {

    private final IZoomLevelChangedListener zoomListener;

    private Map<LimitingResourceQueue, LimitingResourcesComponent> fromTimeLineToComponent = new HashMap<LimitingResourceQueue, LimitingResourcesComponent>();

    private MutableTreeModel<LimitingResourceQueue> model;

    private TimeTracker timeTracker;

    private List<LimitingResourcesComponent> limitingResourcesComponents = new ArrayList<LimitingResourcesComponent>();

    public LimitingResourcesList(TimeTracker timeTracker,
            MutableTreeModel<LimitingResourceQueue> timelinesTree) {

        this.model = timelinesTree;

        zoomListener = adjustTimeTrackerSizeListener();
        timeTracker.addZoomListener(zoomListener);
        this.timeTracker = timeTracker;

        insertAsComponents(timelinesTree.asList());
    }

    private void insertAsComponents(List<LimitingResourceQueue> children) {
        for (LimitingResourceQueue each : children) {
            LimitingResourcesComponent component = LimitingResourcesComponent
                    .create(timeTracker, each);
            this.appendChild(component);
            fromTimeLineToComponent.put(each, component);
        }
    }

    public void setModel(MutableTreeModel<LimitingResourceQueue> model) {
        this.model = model;
    }

    public void invalidate() {
        fromTimeLineToComponent.clear();
        this.getChildren().clear();
        insertAsComponents(model.asList());
        super.invalidate();
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

    public void collapse(LimitingResourceQueue line) {

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
