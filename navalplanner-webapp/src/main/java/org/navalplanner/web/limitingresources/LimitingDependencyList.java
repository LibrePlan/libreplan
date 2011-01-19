/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueDependency;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.zkoss.ganttz.DependencyList;
import org.zkoss.zul.impl.XulElement;

/**
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public class LimitingDependencyList extends XulElement {

    private static final Log LOG = LogFactory.getLog(DependencyList.class);

    private final LimitingResourcesPanel panel;

    private Map<LimitingResourceQueueDependency, LimitingDependencyComponent> dependencies = new HashMap<LimitingResourceQueueDependency, LimitingDependencyComponent>();

    public LimitingDependencyList(LimitingResourcesPanel panel) {
        this.panel = panel;
    }

    public void addDependenciesFor(LimitingResourceQueueElement queueElement) {
        for (LimitingResourceQueueDependency origin: queueElement.getDependenciesAsOrigin()) {
            addDependency(origin);
        }
        for (LimitingResourceQueueDependency destiny: queueElement.getDependenciesAsDestiny()) {
            addDependency(destiny);
        }
    }

    /**
     * Inserts a new dependency and creates a dependency component between task
     * components only if both are shown in the planner
     *
     * @param dependency
     */
    private void addDependency(LimitingResourceQueueDependency dependency) {
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

    public void clear() {
        dependencies.clear();
    }

}
