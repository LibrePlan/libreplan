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

import org.navalplanner.business.planner.entities.LimitingResourceQueueDependency;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
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
public class QueueListComponent extends HtmlMacroComponent implements
        AfterCompose {

    private final IZoomLevelChangedListener zoomListener;

    private MutableTreeModel<LimitingResourceQueue> model;

    private TimeTracker timeTracker;

    private Map<LimitingResourceQueue, QueueComponent> fromQueueToComponent = new HashMap<LimitingResourceQueue, QueueComponent>();

    public QueueListComponent(TimeTracker timeTracker,
            MutableTreeModel<LimitingResourceQueue> timelinesTree) {

        this.model = timelinesTree;

        zoomListener = adjustTimeTrackerSizeListener();
        timeTracker.addZoomListener(zoomListener);
        this.timeTracker = timeTracker;

        insertAsComponents(timelinesTree.asList());
    }

    private void insertAsComponents(List<LimitingResourceQueue> children) {
        for (LimitingResourceQueue each : children) {
            insertAsComponent(each);
        }
    }

    private void insertAsComponent(LimitingResourceQueue queue) {
        QueueComponent component = QueueComponent.create(timeTracker, queue);
        this.appendChild(component);
        fromQueueToComponent.put(queue, component);
    }

    public void setModel(MutableTreeModel<LimitingResourceQueue> model) {
        this.model = model;
    }

    public void invalidate() {
        fromQueueToComponent.clear();
        this.getChildren().clear();
        insertAsComponents(model.asList());
        super.invalidate();
    }

    public void appendQueueElement(LimitingResourceQueueElement element) {
        QueueComponent queueComponent = fromQueueToComponent.get(element
                .getLimitingResourceQueue());
        queueComponent.appendQueueElement(element);
        addDependenciesInPanel(element);
    }

    private void addDependenciesInPanel(LimitingResourceQueueElement element) {
        LimitingResourcesPanel panel = LimitingResourcesPanel
                .getLimitingResourcesPanel(this);
        for (LimitingResourceQueueDependency each : element
                .getDependenciesAsDestiny()) {
            panel.addDependencyComponent(each);
        }
        for (LimitingResourceQueueDependency each : element
                .getDependenciesAsOrigin()) {
            panel.addDependencyComponent(each);
        }
    }

    private IZoomLevelChangedListener adjustTimeTrackerSizeListener() {
        return new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {

                invalidate();
                afterCompose();
                response(null, new AuInvoke(QueueListComponent.this,
                        "adjustTimeTrackerSize"));
                response(null, new AuInvoke(QueueListComponent.this,
                        "adjustResourceLoadRows"));
            }
        };
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        for (QueueComponent each : fromQueueToComponent.values()) {
            each.afterCompose();
        }
    }

    public List<QueueTask> getQueueTasks() {
        List<QueueTask> result = new ArrayList<QueueTask>();
        for (QueueComponent each : fromQueueToComponent.values()) {
            result.addAll(each.getQueueTasks());
        }
        return result;
    }

    public Map<LimitingResourceQueueElement, QueueTask> getLimitingResourceElementToQueueTaskMap() {
        Map<LimitingResourceQueueElement, QueueTask> result = new HashMap<LimitingResourceQueueElement, QueueTask>();
        for (QueueTask each : getQueueTasks()) {
            result.put(each.getLimitingResourceQueueElement(), each);
        }
        return result;
    }

}
