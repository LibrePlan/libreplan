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

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.zkoss.ganttz.Dependency;

/**
 * This class contains a graph with the {@link TaskBean tasks} as vertexes and
 * the {@link DependencyBean dependency} as arcs. It enforces the rules embodied
 * in the dependencies and in the duration of the tasks using listeners. <br/>
 * Created at Apr 24, 2009
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GanttDiagramGraph {

    private final DirectedGraph<TaskBean, DependencyBean> graph = new SimpleDirectedGraph<TaskBean, DependencyBean>(
            DependencyBean.class);

    private Map<TaskBean, DependencyRulesEnforcer> rulesEnforcersByTask = new HashMap<TaskBean, DependencyRulesEnforcer>();

    private List<TaskBean> topLevelTasks = new ArrayList<TaskBean>();

    private List<DependencyRulesEnforcer> getOutgoing(TaskBean task) {
        ArrayList<DependencyRulesEnforcer> result = new ArrayList<DependencyRulesEnforcer>();
        for (DependencyBean dependencyBean : graph.outgoingEdgesOf(task)) {
            result.add(rulesEnforcersByTask
                    .get(dependencyBean.getDestination()));
        }
        return result;
    }

    private class ParentShrinkingEnforcer {

        private final TaskContainerBean container;

        private ParentShrinkingEnforcer(final TaskContainerBean container) {
            if (container == null)
                throw new IllegalArgumentException("container cannot be null");
            this.container = container;
            for (TaskBean subtask : this.container.getTasks()) {
                subtask
                        .addFundamentalPropertiesChangeListener(new PropertyChangeListener() {

                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                Date newBeginDate = container
                                        .getSmallestBeginDateFromChildren();
                                container.setBeginDate(newBeginDate);
                                Date newEndDate = container
                                        .getBiggestDateFromChildren();
                                container.setEndDate(newEndDate);
                            }
                        });
            }
        }

    }

    private class DependencyRulesEnforcer {
        private final TaskBean task;

        private DependencyRulesEnforcer(TaskBean task) {
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
            Set<DependencyBean> incoming = graph.incomingEdgesOf(task);
            Date beginDate = task.getBeginDate();
            Date newStart = DependencyBean.calculateStart(task, beginDate,
                    incoming);
            if (!beginDate.equals(newStart))
                task.setBeginDate(newStart);
            Date endDate = task.getEndDate();
            Date newEnd = DependencyBean.calculateEnd(task, endDate, incoming);
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
    }

    public void addTopLevel(TaskBean task) {
        topLevelTasks.add(task);
        addTask(task);
    }

    private void addTask(TaskBean task) {
        graph.addVertex(task);
        rulesEnforcersByTask.put(task, new DependencyRulesEnforcer(task));
        if (task.isContainer()) {
            new ParentShrinkingEnforcer((TaskContainerBean) task);
            for (TaskBean child : task.getTasks()) {
                addTask(child);
                add(new DependencyBean(child, task, DependencyType.END_END,
                        false));
                add(new DependencyBean(task, child, DependencyType.START_START,
                        false));
            }
        }
    }

    public void remove(TaskBean task) {
        List<DependencyRulesEnforcer> outgoing = getOutgoing(task);
        graph.removeVertex(task);
        rulesEnforcersByTask.remove(task);
        update(outgoing);
    }

    private void updateOutgoing(TaskBean task) {
        update(getOutgoing(task));
    }

    private void update(List<DependencyRulesEnforcer> outgoing) {
        for (DependencyRulesEnforcer rulesEnforcer : outgoing) {
            rulesEnforcer.update();
        }
    }

    public void remove(Dependency dependency) {
        graph.removeEdge(dependency.getDependencyBean());
        TaskBean destination = dependency.getDependencyBean().getDestination();
        rulesEnforcersByTask.get(destination).update();
    }

    public void add(DependencyBean dependency) {
        TaskBean source = dependency.getSource();
        TaskBean destination = dependency.getDestination();
        graph.addEdge(source, destination, dependency);
        getEnforcer(destination).update();
    }

    private DependencyRulesEnforcer getEnforcer(TaskBean destination) {
        return rulesEnforcersByTask.get(destination);
    }

    public List<TaskBean> getTasks() {
        return new ArrayList<TaskBean>(graph.vertexSet());
    }

    public List<DependencyBean> getVisibleDependencies() {
        Set<DependencyBean> edgeSet = graph.edgeSet();
        ArrayList<DependencyBean> result = new ArrayList<DependencyBean>();
        for (DependencyBean dependencyBean : edgeSet) {
            if (dependencyBean.isVisible()) {
                result.add(dependencyBean);
            }
        }
        return result;
    }

    public List<TaskBean> getTopLevelTasks() {
        return Collections.unmodifiableList(topLevelTasks);
    }

}
