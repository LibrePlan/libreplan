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

package org.zkoss.ganttz.limitingresources;

import static org.zkoss.ganttz.i18n.I18nHelper._;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.DependencyList;
import org.zkoss.ganttz.FunctionalityExposedForExtensions;
import org.zkoss.ganttz.GanttPanel;
import org.zkoss.ganttz.TaskComponent;
import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponent;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.ganttz.util.MenuBuilder;
import org.zkoss.ganttz.util.MenuBuilder.ItemAction;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Div;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.impl.XulElement;

/**
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class LimitingDependencyList extends XulElement implements AfterCompose {

    private final class ChangeTypeAction implements
            ItemAction<LimitingDependencyComponent> {
        private final DependencyType type;

        private ChangeTypeAction(DependencyType type) {
            this.type = type;
        }

        @Override
        public void onEvent(final LimitingDependencyComponent choosen,
                Event event) {
            // context.changeType(choosen.getDependency(), type);
        }
    }

    private final class DependencyVisibilityToggler implements
            PropertyChangeListener {
        private final Task source;
        private final Task destination;
        private final LimitingDependencyComponent dependencyComponent;

        private DependencyVisibilityToggler(Task source, Task destination,
                LimitingDependencyComponent dependencyComponent) {
            this.source = source;
            this.destination = destination;
            this.dependencyComponent = dependencyComponent;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!evt.getPropertyName().equals("visible")) {
                return;
            }
            if (dependencyMustBeVisible() != isDependencyNowVisible()) {
                toggleDependencyExistence(dependencyMustBeVisible());
            }
        }

        void toggleDependencyExistence(boolean visible) {
            if (visible) {
                appendChild(dependencyComponent);
                addContextMenu(dependencyComponent);
            } else {
                removeChild(dependencyComponent);
            }
        }

        boolean isDependencyNowVisible() {
            return dependencyComponent.getParent() != null;
        }

        boolean dependencyMustBeVisible() {
            return source.isVisible() && destination.isVisible();
        }
    }

    private static final Log LOG = LogFactory.getLog(DependencyList.class);

    private transient IZoomLevelChangedListener listener;

    private final FunctionalityExposedForExtensions<?> context;

    public LimitingDependencyList(FunctionalityExposedForExtensions<?> context) {
        this.context = context;
    }

    private List<LimitingDependencyComponent> getLimitingDependencyComponents() {
        List<Object> children = getChildren();
        return ComponentsFinder.findComponentsOfType(
                LimitingDependencyComponent.class, children);
    }

    void addDependencyComponent(
            final LimitingDependencyComponent dependencyComponent) {
        Div source = dependencyComponent.getSource();
        Div destination = dependencyComponent.getDestination();
        // DependencyVisibilityToggler visibilityToggler = new
        // DependencyVisibilityToggler(
        // source.getTask(), destination.getTask(), dependencyComponent);
        // source.getTask().addVisibilityPropertiesChangeListener(
        // visibilityToggler);
        // destination.getTask().addVisibilityPropertiesChangeListener(
        // visibilityToggler);
        // boolean dependencyMustBeVisible = visibilityToggler
        // .dependencyMustBeVisible();
        // visibilityToggler.toggleDependencyExistence(dependencyMustBeVisible);
        // if (dependencyMustBeVisible) {
        // dependencyComponent.redrawDependency();
        // }
    }

    private void addContextMenu(LimitingDependencyComponent dependencyComponent) {
        dependencyComponent.setContext(getContextMenu());
    }

    private GanttPanel getGanttPanel() {
        return (GanttPanel) getParent();
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
                    if (!isInPage()) {
                        return;
                    }
                    for (LimitingDependencyComponent dependencyComponent : getLimitingDependencyComponents()) {
                        dependencyComponent.zoomChanged();
                    }
                }
            };
            // getTimeTracker().addZoomListener(listener);
        }
        // addContextMenu();
    }

    private boolean isInPage() {
        return getParent() != null && getGanttPanel() != null
                && getGanttPanel().getParent() != null;
    }

    private TimeTracker getTimeTracker() {
        return getTimeTrackerComponent().getTimeTracker();
    }

    private void addContextMenu() {
        for (LimitingDependencyComponent dependencyComponent : getLimitingDependencyComponents()) {
            addContextMenu(dependencyComponent);
        }
    }

    private Menupopup contextMenu;

    private Menupopup getContextMenu() {
        if (contextMenu == null) {
            MenuBuilder<LimitingDependencyComponent> contextMenuBuilder = MenuBuilder
                    .on(getPage(), getLimitingDependencyComponents()).item(_("Erase"),
                            "/common/img/ico_borrar.png",
                            new ItemAction<LimitingDependencyComponent>() {
                                @Override
                                public void onEvent(
                                        final LimitingDependencyComponent choosen,
                                        Event event) {
//                                    context
//                                            .removeDependency(choosen.getDependency());
                                }
                            });
            contextMenuBuilder.item(_("Set End-Start"), null,
                    new ChangeTypeAction(
                    DependencyType.END_START));

            contextMenuBuilder.item(_("Set Start-Start"), null,
                    new ChangeTypeAction(
                    DependencyType.START_START));

            contextMenuBuilder.item(_("Set End-End"), null,
                    new ChangeTypeAction(
                    DependencyType.END_END));

            contextMenu = contextMenuBuilder.create();

        }
        return contextMenu;
    }

    private TimeTrackerComponent getTimeTrackerComponent() {
        return getGanttPanel().getTimeTrackerComponent();
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

    public void taskRemoved(Task task) {
        for (LimitingDependencyComponent dependencyComponent : LimitingDependencyList.this
                .getLimitingDependencyComponents()) {
            if (dependencyComponent.contains(task)) {
                this.removeChild(dependencyComponent);
            }
        }
    }

    public void remove(Dependency dependency) {
        for (LimitingDependencyComponent dependencyComponent : LimitingDependencyList.this
                .getLimitingDependencyComponents()) {
            if (dependencyComponent.hasSameSourceAndDestination(dependency)) {
                this.removeChild(dependencyComponent);
            }
        }
    }
}
