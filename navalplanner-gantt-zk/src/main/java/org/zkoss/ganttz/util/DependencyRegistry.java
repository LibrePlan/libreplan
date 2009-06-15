package org.zkoss.ganttz.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
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
public class DependencyRegistry {

    private final DirectedGraph<TaskBean, DependencyBean> graph = new SimpleDirectedGraph<TaskBean, DependencyBean>(
            DependencyBean.class);

    private Map<TaskBean, RulesEnforcer> rulesEnforcersByTask = new HashMap<TaskBean, RulesEnforcer>();

    private List<RulesEnforcer> getOutgoing(TaskBean task) {
        ArrayList<RulesEnforcer> result = new ArrayList<RulesEnforcer>();
        for (DependencyBean dependencyBean : graph.outgoingEdgesOf(task)) {
            result.add(rulesEnforcersByTask
                    .get(dependencyBean.getDestination()));
        }
        return result;
    }

    private class RulesEnforcer {
        private final TaskBean task;

        private RulesEnforcer(TaskBean task) {
            if (task == null)
                throw new IllegalArgumentException("task cannot be null");
            this.task = task;
            this.task.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    RulesEnforcer.this.update();
                    updateOutgoing(RulesEnforcer.this.task);
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

    public void add(TaskBean task) {
        graph.addVertex(task);
        rulesEnforcersByTask.put(task, new RulesEnforcer(task));
    }

    public void remove(TaskBean task) {
        List<RulesEnforcer> outgoing = getOutgoing(task);
        graph.removeVertex(task);
        rulesEnforcersByTask.remove(task);
        update(outgoing);
    }

    private void updateOutgoing(TaskBean task) {
        update(getOutgoing(task));
    }

    private void update(List<RulesEnforcer> outgoing) {
        for (RulesEnforcer rulesEnforcer : outgoing) {
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

    private RulesEnforcer getEnforcer(TaskBean destination) {
        return rulesEnforcersByTask.get(destination);
    }

    public List<TaskBean> getTasks() {
        return new ArrayList<TaskBean>(graph.vertexSet());
    }

    public List<DependencyBean> getDependencies() {
        Set<DependencyBean> edgeSet = graph.edgeSet();
        return new ArrayList<DependencyBean>(edgeSet);
    }

}
