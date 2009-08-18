package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.util.MenuBuilder;
import org.zkoss.ganttz.util.MenuBuilder.ItemAction;
import org.zkoss.ganttz.util.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.util.zoom.ZoomLevel;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Menupopup;
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
            context.changeType(dependencyFor(choosen), type);
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
            if (!evt.getPropertyName().equals("visible"))
                return;
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

    private IZoomLevelChangedListener listener;

    private final FunctionalityExposedForExtensions<?> context;

    public DependencyList(FunctionalityExposedForExtensions<?> context) {
        this.context = context;
    }

    private List<DependencyComponent> getDependencyComponents() {
        List<Object> children = getChildren();
        return Planner
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
                    for (DependencyComponent dependencyComponent : getDependencyComponents()) {
                        dependencyComponent.zoomChanged();
                    }
                }
            };
            getTimeTracker().addZoomListener(listener);
        }
        addContextMenu();
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
                    .on(getPage(), getDependencyComponents()).item("Erase",
                            new ItemAction<DependencyComponent>() {
                                @Override
                                public void onEvent(
                                        final DependencyComponent choosen,
                                        Event event) {
                                    context
                                            .removeDependency(dependencyFor(choosen));
                                }
                            });
            contextMenuBuilder.item("Set End-Start", new ChangeTypeAction(
                    DependencyType.END_START));

            contextMenuBuilder.item("Set Start-Start", new ChangeTypeAction(
                    DependencyType.START_START));

            contextMenuBuilder.item("Set End-End", new ChangeTypeAction(
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

    private Dependency dependencyFor(
            final DependencyComponent dependencyComponent) {
        return dependencyComponent.getDependency(context.getDiagramGraph());
    }
}
