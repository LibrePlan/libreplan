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

package org.zkoss.ganttz;

import static org.zkoss.ganttz.i18n.I18nHelper._;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.impl.XulElement;

/**
 * @author Francisco Javier Moran Rúa <jmoran@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class DependencyList extends XulElement implements AfterCompose {

    private final class ChangeTypeAction implements
            ItemAction<DependencyComponent> {
        private final DependencyType type;

        private ChangeTypeAction(DependencyType type) {
            this.type = type;
        }

        @Override
        public void onEvent(final DependencyComponent choosen, Event event) {
            boolean canBeAdded = context.changeType(choosen.getDependency(),
                    type);
            if (!canBeAdded) {
                warnUser(_("The specified dependency is not allowed"));
            }
        }

        private void warnUser(String message) {
            try {
                Messagebox.show(message, null, Messagebox.OK,
                        Messagebox.EXCLAMATION, 0, null);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final class DependencyVisibilityToggler implements
            PropertyChangeListener {
        private final Task source;
        private final Task destination;
        private final DependencyComponent dependencyComponent;

        private DependencyVisibilityToggler(Task source, Task destination,
                DependencyComponent dependencyComponent) {
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

    public DependencyList(FunctionalityExposedForExtensions<?> context) {
        this.context = context;
    }

    private List<DependencyComponent> getDependencyComponents() {
        List<Object> children = getChildren();
        return ComponentsFinder
                .findComponentsOfType(DependencyComponent.class, children);
    }

    void addDependencyComponent(final DependencyComponent dependencyComponent) {
        TaskComponent source = dependencyComponent.getSource();
        TaskComponent destination = dependencyComponent.getDestination();
        DependencyVisibilityToggler visibilityToggler = new DependencyVisibilityToggler(
                source.getTask(), destination.getTask(), dependencyComponent);
        source.getTask().addVisibilityPropertiesChangeListener(
                visibilityToggler);
        destination.getTask().addVisibilityPropertiesChangeListener(
                visibilityToggler);
        boolean dependencyMustBeVisible = visibilityToggler
                .dependencyMustBeVisible();
        visibilityToggler.toggleDependencyExistence(dependencyMustBeVisible);
        if (dependencyMustBeVisible) {
            dependencyComponent.redrawDependency();
        }
    }

    private void addContextMenu(DependencyComponent dependencyComponent) {
        dependencyComponent.setContext(getContextMenu());
    }

    private GanttPanel getGanttPanel() {
        return (GanttPanel) getParent();
    }

    public void setDependencyComponents(
            List<DependencyComponent> dependencyComponents) {
        for (DependencyComponent dependencyComponent : dependencyComponents) {
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
                    for (DependencyComponent dependencyComponent : getDependencyComponents()) {
                        dependencyComponent.zoomChanged();
                    }
                }
            };
            getTimeTracker().addZoomListener(listener);
        }
        addContextMenu();
    }

    private boolean isInPage() {
        return getParent() != null && getGanttPanel() != null
                && getGanttPanel().getParent() != null;
    }

    private TimeTracker getTimeTracker() {
        return getTimeTrackerComponent().getTimeTracker();
    }

    private void addContextMenu() {
        for (DependencyComponent dependencyComponent : getDependencyComponents()) {
            addContextMenu(dependencyComponent);
        }
    }

    private Menupopup contextMenu;

    private Menupopup getContextMenu() {
        if (contextMenu == null) {
            MenuBuilder<DependencyComponent> contextMenuBuilder = MenuBuilder
                    .on(getPage(), getDependencyComponents()).item(_("Erase"),
                            "/common/img/ico_borrar.png",
                            new ItemAction<DependencyComponent>() {
                                @Override
                                public void onEvent(
                                        final DependencyComponent choosen,
                                        Event event) {
                                    context
                                            .removeDependency(choosen.getDependency());
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

    private List<DependencyComponent> getDependencyComponentsConnectedTo(
            TaskComponent taskComponent) {
        ArrayList<DependencyComponent> result = new ArrayList<DependencyComponent>();
        List<DependencyComponent> dependencies = getDependencyComponents();
        for (DependencyComponent dependencyComponent : dependencies) {
            if (dependencyComponent.getSource().equals(taskComponent)
                    || dependencyComponent.getDestination().equals(
                            taskComponent)) {
                result.add(dependencyComponent);
            }
        }
        return result;
    }

    public void redrawDependencies() {
        redrawDependencyComponents(getDependencyComponents());
    }

    public void redrawDependencyComponents(
            List<DependencyComponent> dependencyComponents) {
        for (DependencyComponent dependencyComponent : dependencyComponents) {
            dependencyComponent.redrawDependency();
        }
    }

    public void taskRemoved(Task task) {
        for (DependencyComponent dependencyComponent : DependencyList.this
                .getDependencyComponents()) {
            if (dependencyComponent.contains(task)) {
                this.removeChild(dependencyComponent);
            }
        }
    }

    public void remove(Dependency dependency) {
        for (DependencyComponent dependencyComponent : DependencyList.this
                .getDependencyComponents()) {
            if (dependencyComponent.hasSameSourceAndDestination(dependency)) {
                this.removeChild(dependencyComponent);
            }
        }
    }
}
