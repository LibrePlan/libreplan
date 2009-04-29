/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.util.MenuBuilder;
import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.MenuBuilder.ItemAction;
import org.zkoss.ganttz.util.WeakReferencedListeners.ListenerNotification;
import org.zkoss.ganttz.util.zoom.ZoomLevel;
import org.zkoss.ganttz.util.zoom.ZoomLevelChangedListener;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.impl.XulElement;

/**
 * 
 * @author Francisco Javier Moran Rúa
 * 
 */
public class DependencyList extends XulElement implements AfterCompose {

    private static final Log LOG = LogFactory.getLog(DependencyList.class);

    private TaskRemovedListener taskRemovedListener;

    private ZoomLevelChangedListener listener;

    private final WeakReferencedListeners<DependencyRemovedListener> dependencyRemovedListeners = WeakReferencedListeners
            .create();

    public DependencyList() {
    }

    private List<Dependency> getDependencies() {
        List<Object> children = getChildren();
        return Planner.findComponentsOfType(Dependency.class, children);
    }

    void addDependency(Dependency dependency) {
        appendChild(dependency);
        addContextMenu(dependency);
        publishDependency(dependency);
    }

    private void addContextMenu(Dependency dependency) {
        dependency.setContext(getContextMenu());
    }

    private GanttPanel getGanttPanel() {
        return (GanttPanel) getParent();
    }

    @Override
    public void afterCompose() {
        if (listener == null) {
            listener = new ZoomLevelChangedListener() {
                @Override
                public void zoomLevelChanged(ZoomLevel detailLevel) {
                    for (Dependency dependency : getDependencies()) {
                        dependency.zoomChanged();
                    }
                }
            };
            getTimeTracker().addZoomListener(listener);
        }
        if (taskRemovedListener == null) {
            taskRemovedListener = new TaskRemovedListener() {

                @Override
                public void taskRemoved(Task taskRemoved) {
                    for (Dependency dependency : DependencyList.this
                            .getDependencies()) {
                        if (dependency.contains(taskRemoved)) {
                            dependency.detach();
                        }
                    }
                }

            };
            getGanttPanel().getTaskList().addTaskRemovedListener(
                    taskRemovedListener);
        }
        addContextMenu();
        publishDependencies();

    }

    private void publishDependencies() {
        for (Dependency dependency : getDependencies()) {
            publishDependency(dependency);
        }
    }

    private void publishDependency(Dependency dependency) {
        getPlanner().publishDependency(dependency);
    }

    private Planner getPlanner() {
        return getGanttPanel().getPlanner();
    }

    private void addContextMenu() {
        for (Dependency dependency : getDependencies()) {
            addContextMenu(dependency);
        }
    }

    private Menupopup contextMenu;

    private Menupopup getContextMenu() {
        if (contextMenu == null) {
            contextMenu = MenuBuilder.on(getPage(), getDependencies()).item(
                    "Erase", new ItemAction<Dependency>() {
                        @Override
                        public void onEvent(final Dependency choosen,
                                Event event) {
                            removeChild(choosen);
                            dependencyRemovedListeners
                                    .fireEvent(new ListenerNotification<DependencyRemovedListener>() {

                                        @Override
                                        public void doNotify(
                                                DependencyRemovedListener listener) {
                                            listener.dependenceRemoved(choosen);

                                        }
                                    });
                        }
                    }).create();
        }
        return contextMenu;
    }

    public void addDependencyRemovedListener(
            DependencyRemovedListener removedListener) {
        dependencyRemovedListeners.addListener(removedListener);
    }

    private TimeTracker getTimeTracker() {
        return getGanttPanel().getTimeTracker();
    }

    public void redrawDependenciesConnectedTo(Task task) {
        redrawDependencies(getDependenciesConnectedTo(task));
    }

    private List<Dependency> getDependenciesConnectedTo(Task task) {
        ArrayList<Dependency> result = new ArrayList<Dependency>();
        for (Dependency dependency : getDependencies()) {
            if (dependency.getSource().equals(task)
                    || dependency.getDestination().equals(task)) {
                result.add(dependency);
            }
        }
        return result;
    }

    public void redrawDependencies() {
        redrawDependencies(getDependencies());
    }

    public void redrawDependencies(List<Dependency> dependencies) {
        for (Dependency dependency : dependencies) {
            dependency.redrawDependency();
        }
    }

}
