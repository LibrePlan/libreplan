package org.zkoss.ganttz.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.zkoss.ganttz.DependencyComponent;

/**
 * This class contains a graph with the {@link Task tasks} as vertexes and the
 * {@link Dependency dependency} as arcs. It enforces the rules embodied in the
 * dependencies and in the duration of the tasks using listeners. <br/>
 * Created at Apr 24, 2009
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GanttDiagramGraph {

    private final DirectedGraph<Task, Dependency> graph = new SimpleDirectedGraph<Task, Dependency>(
            Dependency.class);

    private Map<Task, DependencyRulesEnforcer> rulesEnforcersByTask = new HashMap<Task, DependencyRulesEnforcer>();

    private Map<Task, ParentShrinkingEnforcer> parentShrinkingEnforcerByTask = new WeakHashMap<Task, ParentShrinkingEnforcer>();

    private List<Task> topLevelTasks = new ArrayList<Task>();

    private List<DependencyRulesEnforcer> getOutgoing(Task task) {
        ArrayList<DependencyRulesEnforcer> result = new ArrayList<DependencyRulesEnforcer>();
        for (Dependency dependency : graph.outgoingEdgesOf(task)) {
            result.add(rulesEnforcersByTask.get(dependency.getDestination()));
        }
        return result;
    }

    private class ParentShrinkingEnforcer {

        private final TaskContainer container;

        private ParentShrinkingEnforcer(final TaskContainer container) {
            if (container == null)
                throw new IllegalArgumentException("container cannot be null");
            this.container = container;
            for (Task subtask : this.container.getTasks()) {
                subtask
                        .addFundamentalPropertiesChangeListener(new PropertyChangeListener() {

                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                update();
                            }
                        });
            }
        }

        void update() {
            Date newBeginDate = this.container
                    .getSmallestBeginDateFromChildren();
            this.container.setBeginDate(newBeginDate);
            Date newEndDate = this.container.getBiggestDateFromChildren();
            this.container.setEndDate(newEndDate);
        }

    }

    private class DependencyRulesEnforcer {
        private final Task task;

        private DependencyRulesEnforcer(Task task) {
            if (task == null)
                throw new IllegalArgumentException("task cannot be null");
            this.task = task;
            this.task
                    .addFundamentalPropertiesChangeListener(new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            DependencyRulesEnforcer.this.update();
                            updateOutgoing(DependencyRulesEnforcer.this.task);
                        }
                    });
        }

        void update() {
            Set<Dependency> incoming = graph.incomingEdgesOf(task);
            Date beginDate = task.getBeginDate();
            Date newStart = Dependency
                    .calculateStart(task, beginDate, incoming);
            if (!beginDate.equals(newStart))
                task.setBeginDate(newStart);
            Date endDate = task.getEndDate();
            Date newEnd = Dependency.calculateEnd(task, endDate, incoming);
            if (!endDate.equals(newEnd)) {
                task.setEndDate(newEnd);
            }
        }
    }

    public void applyAllRestrictions() {
        for (DependencyRulesEnforcer rulesEnforcer : rulesEnforcersByTask
                .values()) {
            rulesEnforcer.update();
        }
        for (ParentShrinkingEnforcer parentShrinkingEnforcer : parentShrinkingEnforcerByTask
                .values()) {
            parentShrinkingEnforcer.update();
        }
    }

    public void addTopLevel(Task task) {
        topLevelTasks.add(task);
        addTask(task);
    }

    private void addTask(Task task) {
        graph.addVertex(task);
        rulesEnforcersByTask.put(task, new DependencyRulesEnforcer(task));
        if (task.isContainer()) {
            ParentShrinkingEnforcer parentShrinkingEnforcer = new ParentShrinkingEnforcer(
                    (TaskContainer) task);
            parentShrinkingEnforcerByTask.put(task, parentShrinkingEnforcer);
            for (Task child : task.getTasks()) {
                addTask(child);
                add(new Dependency(child, task, DependencyType.END_END, false));
                add(new Dependency(task, child, DependencyType.START_START,
                        false));
            }
        }
    }

    public void remove(Task task) {
        List<DependencyRulesEnforcer> outgoing = getOutgoing(task);
        graph.removeVertex(task);
        rulesEnforcersByTask.remove(task);
        update(outgoing);
    }

    private void updateOutgoing(Task task) {
        update(getOutgoing(task));
    }

    private void update(List<DependencyRulesEnforcer> outgoing) {
        for (DependencyRulesEnforcer rulesEnforcer : outgoing) {
            rulesEnforcer.update();
        }
    }

    public void remove(DependencyComponent dependencyComponent) {
        graph.removeEdge(dependencyComponent.getDependency());
        Task destination = dependencyComponent.getDependency().getDestination();
        rulesEnforcersByTask.get(destination).update();
    }

    public void add(Dependency dependency) {
        Task source = dependency.getSource();
        Task destination = dependency.getDestination();
        graph.addEdge(source, destination, dependency);
        getEnforcer(destination).update();
    }

    private DependencyRulesEnforcer getEnforcer(Task destination) {
        return rulesEnforcersByTask.get(destination);
    }

    public List<Task> getTasks() {
        return new ArrayList<Task>(graph.vertexSet());
    }

    public List<Dependency> getVisibleDependencies() {
        Set<Dependency> edgeSet = graph.edgeSet();
        ArrayList<Dependency> result = new ArrayList<Dependency>();
        for (Dependency dependency : edgeSet) {
            if (dependency.isVisible()) {
                result.add(dependency);
            }
        }
        return result;
    }

    public List<Task> getTopLevelTasks() {
        return Collections.unmodifiableList(topLevelTasks);
    }

}
