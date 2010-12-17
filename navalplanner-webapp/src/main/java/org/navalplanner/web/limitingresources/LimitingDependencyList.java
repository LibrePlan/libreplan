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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.DependencyList;
import org.zkoss.ganttz.TaskComponent;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponent;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

/**
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class LimitingDependencyList extends XulElement implements AfterCompose {

    private static final Log LOG = LogFactory.getLog(DependencyList.class);

    private transient IZoomLevelChangedListener listener;

    private final LimitingResourcesPanel panel;

    public LimitingDependencyList(LimitingResourcesPanel panel) {
        this.panel = panel;
    }

    @SuppressWarnings("unchecked")
    private List<LimitingDependencyComponent> getLimitingDependencyComponents() {
        return ComponentsFinder.findComponentsOfType(
                LimitingDependencyComponent.class, getChildren());
    }

    public void addDependencyComponent(
            final LimitingDependencyComponent dependencyComponent) {
        dependencyComponent.redrawDependency();
        dependencyComponent.setParent(this);
    }

    public void removeDependencyComponents(QueueTask queueTask) {
        for (LimitingDependencyComponent dependency : getLimitingDependencyComponents()) {
            if (dependency.getSource().equals(queueTask)
                    || dependency.getDestination().equals(queueTask)) {
                removeChild(dependency);
            }
        }
    }

    public void setDependencyComponents(
            List<LimitingDependencyComponent> dependencyComponents) {
        for (LimitingDependencyComponent dependencyComponent : dependencyComponents) {
            addDependencyComponent(dependencyComponent);
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

    private TimeTracker getTimeTracker() {
        return getTimeTrackerComponent().getTimeTracker();
    }

    private TimeTrackerComponent getTimeTrackerComponent() {
        return panel.getTimeTrackerComponent();
    }

    public void redrawDependenciesConnectedTo(TaskComponent taskComponent) {
        redrawDependencyComponents(getDependencyComponentsConnectedTo(taskComponent));
    }

    private List<LimitingDependencyComponent> getDependencyComponentsConnectedTo(
            TaskComponent taskComponent) {
        ArrayList<LimitingDependencyComponent> result = new ArrayList<LimitingDependencyComponent>();
        List<LimitingDependencyComponent> dependencies = getLimitingDependencyComponents();
        for (LimitingDependencyComponent dependencyComponent : dependencies) {
            if (dependencyComponent.getSource().equals(taskComponent)
                    || dependencyComponent.getDestination().equals(
                            taskComponent)) {
                result.add(dependencyComponent);
            }
        }
        return result;
    }

    public void redrawDependencies() {
        redrawDependencyComponents(getLimitingDependencyComponents());
    }

    public void redrawDependencyComponents(
            List<LimitingDependencyComponent> dependencyComponents) {
        for (LimitingDependencyComponent dependencyComponent : dependencyComponents) {
            dependencyComponent.redrawDependency();
        }
    }

}
