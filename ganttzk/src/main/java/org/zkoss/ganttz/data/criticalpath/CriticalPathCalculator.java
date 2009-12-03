/*
 * This file is part of ###PROJECT_NAME###
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

package org.zkoss.ganttz.data.criticalpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;

/**
 * Class that calculates the critical path of a Gantt diagram graph.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CriticalPathCalculator<T extends ITaskFundamentalProperties> {

    private ICriticalPathCalculable<T> graph;

    private Map<T, Node<T>> nodes;

    private InitialNode<T> bop;
    private LastNode<T> eop;

    public List<T> calculateCriticalPath(ICriticalPathCalculable<T> graph) {
        this.graph = graph;
        bop = createBeginningOfProjectNode();
        eop = createEndOfProjectNode();

        nodes = createGraphNodes();

        forward(bop);
        eop.updateLatestValues();

        backward(eop);

        return getTasksOnCriticalPath();
    }

    private InitialNode<T> createBeginningOfProjectNode() {
        return new InitialNode<T>(new HashSet<T>(graph.getTopLevelTasks()));
    }

    private LastNode<T> createEndOfProjectNode() {
        return new LastNode<T>(new HashSet<T>(graph.getBottomLevelTasks()));
    }

    private Map<T, Node<T>> createGraphNodes() {
        Map<T, Node<T>> result = new HashMap<T, Node<T>>();

        for (T task : graph.getTasks()) {
            Node<T> node = new Node<T>(task, graph.getIncomingTasksFor(task),
                    graph.getOutgoingTasksFor(task));
            result.put(task, node);
        }

        return result;
    }

    private void forward(Node<T> currentNode) {
        T currentTask = currentNode.getTask();
        int earliestStart = currentNode.getEarliestStart();
        int earliestFinish = currentNode.getEarliestFinish();
        int duration = currentNode.getDuration();

        Set<T> nextTasks = currentNode.getNextTasks();
        if (nextTasks.isEmpty()) {
            eop.setEarliestStart(earliestFinish);
        } else {
            for (T task : nextTasks) {
                Node<T> node = nodes.get(task);

                DependencyType dependencyType = DependencyType.END_START;
                if (currentTask != null) {
                    Dependency dependency = graph.getDependencyFrom(
                            currentTask, task);
                    if (dependency != null) {
                        dependencyType = dependency.getType();
                    }
                }

                switch (dependencyType) {
                case START_START:
                    node.setEarliestStart(earliestStart);
                    break;
                case END_END:
                    node.setEarliestStart(earliestFinish - duration);
                    break;
                case END_START:
                default:
                    node.setEarliestStart(earliestFinish);
                    break;
                }

                forward(node);
            }
        }
    }

    private void backward(Node<T> currentNode) {
        T currentTask = currentNode.getTask();
        int latestStart = currentNode.getLatestStart();
        int latestFinish = currentNode.getLatestFinish();
        int duration = currentNode.getDuration();

        Set<T> previousTasks = currentNode.getPreviousTasks();
        if (previousTasks.isEmpty()) {
            bop.setLatestFinish(latestStart);
        } else {
            for (T task : previousTasks) {
                Node<T> node = nodes.get(task);

                DependencyType dependencyType = DependencyType.END_START;
                if (currentTask != null) {
                    Dependency dependency = graph.getDependencyFrom(task,
                            currentTask);
                    if (dependency != null) {
                        dependencyType = dependency.getType();
                    }
                }

                switch (dependencyType) {
                case START_START:
                    node.setLatestFinish(latestStart + duration);
                    break;
                case END_END:
                    node.setLatestFinish(latestFinish);
                    break;
                case END_START:
                default:
                    node.setLatestFinish(latestStart);
                    break;
                }

                backward(node);
            }
        }
    }

    private List<T> getTasksOnCriticalPath() {
        List<T> result = new ArrayList<T>();

        for (Node<T> node : nodes.values()) {
            if (node.getLatestStart() == node.getEarliestStart()) {
                result.add(node.getTask());
            }
        }

        return result;
    }

}
