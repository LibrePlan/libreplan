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

package org.navalplanner.web.limitingresources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueDependency;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.zkoss.ganttz.DependencyList;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponent;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

/**
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public class LimitingDependencyList extends XulElement implements AfterCompose {

    private static final Log LOG = LogFactory.getLog(DependencyList.class);

    private transient IZoomLevelChangedListener listener;

    private final LimitingResourcesPanel panel;

    private Map<LimitingResourceQueueDependency, LimitingDependencyComponent> dependencies = new HashMap<LimitingResourceQueueDependency, LimitingDependencyComponent>();

    public LimitingDependencyList(LimitingResourcesPanel panel) {
        this.panel = panel;
    }

    /**
     * Inserts a new dependency and creates a dependency component between task
     * components only if both are shown in the planner
     *
     * @param dependency
     */
    public void addDependency(LimitingResourceQueueDependency dependency) {
        if (!dependencies.keySet().contains(dependency)) {
            LimitingDependencyComponent dependencyComponent = createDependencyComponent(dependency);
            if (dependencyComponent != null) {
                addDependencyComponent(dependencyComponent);
                dependencies.put(dependency, dependencyComponent);
            }
        }
    }

    /**
     * Creates a new dependency component out of a dependency
     *
     * Returns null if at least one of the edges of the dependency is not yet in the planner
     *
     * @param dependency
     * @return
     */
    private LimitingDependencyComponent createDependencyComponent(
            LimitingResourceQueueDependency dependency) {

        Map<LimitingResourceQueueElement, QueueTask> queueElementsMap = panel.getQueueTaskMap();

        QueueTask origin = queueElementsMap.get(dependency.getHasAsOrigin());
        QueueTask destination = queueElementsMap.get(dependency
                .getHasAsDestiny());

        return (origin != null && destination != null) ? new LimitingDependencyComponent(
                origin, destination)
                : null;
    }

    private void addDependencyComponent(
            LimitingDependencyComponent dependencyComponent) {
        dependencyComponent.redrawDependency();
        dependencyComponent.setParent(this);
    }

    public void removeDependenciesFor(LimitingResourceQueue queue) {
        for (LimitingResourceQueueElement each: queue.getLimitingResourceQueueElements()) {
            removeDependenciesFor(each);
        }
    }

    public void removeDependenciesFor(LimitingResourceQueueElement queueElement) {
        for (LimitingResourceQueueDependency dependency : queueElement
                .getDependenciesAsOrigin()) {
            removeDependency(dependency);
        }
        for (LimitingResourceQueueDependency dependency : queueElement
                .getDependenciesAsDestiny()) {
            removeDependency(dependency);
        }
    }

    private void removeDependency(LimitingResourceQueueDependency dependency) {
        LimitingDependencyComponent comp = dependencies.get(dependency);
        if (comp != null) {
            removeChild(comp);
            dependencies.remove(dependency);
        }
    }

    @Override
    public void afterCompose() {
        if (listener == null) {
            listener = new IZoomLevelChangedListener() {
                @Override
                public void zoomLevelChanged(ZoomLevel detailLevel) {
                    for (LimitingDependencyComponent dependencyComponent : getLimitingDependencyComponents()) {
                        dependencyComponent.zoomChanged();
                    }
                }
            };
            getTimeTracker().addZoomListener(listener);
        }
        redrawDependencies();
    }

    @SuppressWarnings("unchecked")
    private List<LimitingDependencyComponent> getLimitingDependencyComponents() {
        return new ArrayList<LimitingDependencyComponent>(dependencies.values());
    }

    private TimeTracker getTimeTracker() {
        return getTimeTrackerComponent().getTimeTracker();
    }

    private TimeTrackerComponent getTimeTrackerComponent() {
        return panel.getTimeTrackerComponent();
    }

    private void redrawDependencies() {
        redrawDependencyComponents(getLimitingDependencyComponents());
    }

    private void redrawDependencyComponents(
            List<LimitingDependencyComponent> dependencyComponents) {
        for (LimitingDependencyComponent dependencyComponent : dependencyComponents) {
            dependencyComponent.redrawDependency();
        }
    }

    public void clear() {
        dependencies.clear();
    }

}
