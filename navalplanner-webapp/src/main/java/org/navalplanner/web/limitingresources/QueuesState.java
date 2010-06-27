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
package org.navalplanner.web.limitingresources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.limiting.entities.InsertionRequirements;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueDependency;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.navalplanner.business.planner.limiting.entities.Gap.GapOnQueue;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionCompounder;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.navalplanner.business.resources.entities.Resource;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class QueuesState {

    private final List<LimitingResourceQueue> queues;

    private final List<LimitingResourceQueueElement> unassignedElements;

    private final DirectedGraph<LimitingResourceQueueElement, LimitingResourceQueueDependency> graph;

    private final Map<Long, LimitingResourceQueue> queuesById;

    private final Map<Long, LimitingResourceQueueElement> elementsById;

    private final Map<Long, LimitingResourceQueue> queuesByResourceId;

    private static <T extends BaseEntity> Map<Long, T> byId(
            Collection<? extends T> entities) {
        Map<Long, T> result = new HashMap<Long, T>();
        for (T each : entities) {
            result.put(each.getId(), each);
        }
        return result;
    }

    private static Map<Long, LimitingResourceQueue> byResourceId(
            Collection<? extends LimitingResourceQueue> limitingResourceQueues) {
        Map<Long, LimitingResourceQueue> result = new HashMap<Long, LimitingResourceQueue>();
        for (LimitingResourceQueue each : limitingResourceQueues) {
            result.put(each.getResource().getId(), each);
        }
        return result;
    }

    public QueuesState(
            List<LimitingResourceQueue> limitingResourceQueues,
            List<LimitingResourceQueueElement> unassignedLimitingResourceQueueElements) {
        this.queues = new ArrayList<LimitingResourceQueue>(
                limitingResourceQueues);
        this.unassignedElements = new ArrayList<LimitingResourceQueueElement>(
                unassignedLimitingResourceQueueElements);
        this.queuesById = byId(queues);
        this.elementsById = byId(allElements(limitingResourceQueues,
                unassignedLimitingResourceQueueElements));
        this.queuesByResourceId = byResourceId(limitingResourceQueues);
        this.graph = buildGraph(getAllElements(unassignedElements, queues));
    }

    private static DirectedGraph<LimitingResourceQueueElement, LimitingResourceQueueDependency> buildGraph(
            List<LimitingResourceQueueElement> allElements) {
        DirectedGraph<LimitingResourceQueueElement, LimitingResourceQueueDependency> result = instantiateDirectedGraph();
        for (LimitingResourceQueueElement each : allElements) {
            result.addVertex(each);
        }
        for (LimitingResourceQueueElement each : allElements) {
            Set<LimitingResourceQueueDependency> dependenciesAsOrigin = each
                    .getDependenciesAsOrigin();
            for (LimitingResourceQueueDependency eachDependency : dependenciesAsOrigin) {
                addDependency(result, eachDependency);
            }
        }
        return result;
    }

    private static SimpleDirectedGraph<LimitingResourceQueueElement, LimitingResourceQueueDependency> instantiateDirectedGraph() {
        return new SimpleDirectedGraph<LimitingResourceQueueElement, LimitingResourceQueueDependency>(
                LimitingResourceQueueDependency.class);
    }

    private static void addDependency(
            DirectedGraph<LimitingResourceQueueElement, LimitingResourceQueueDependency> result,
            LimitingResourceQueueDependency dependency) {
        LimitingResourceQueueElement origin = dependency.getHasAsOrigin();
        LimitingResourceQueueElement destination = dependency.getHasAsDestiny();
        result.addVertex(origin);
        result.addVertex(destination);
        result.addEdge(origin, destination, dependency);
    }

    private static List<LimitingResourceQueueElement> getAllElements(
            List<LimitingResourceQueueElement> unassigned,
            List<LimitingResourceQueue> queues) {
        List<LimitingResourceQueueElement> result = new ArrayList<LimitingResourceQueueElement>();
        result.addAll(unassigned);
        for (LimitingResourceQueue each : queues) {
            result.addAll(each.getLimitingResourceQueueElements());
        }
        return result;
    }

    private List<LimitingResourceQueueElement> allElements(
            List<LimitingResourceQueue> queues,
            List<LimitingResourceQueueElement> unassigned) {
        List<LimitingResourceQueueElement> result = new ArrayList<LimitingResourceQueueElement>();
        for (LimitingResourceQueue each : queues) {
            result.addAll(each.getLimitingResourceQueueElements());
        }
        result.addAll(unassigned);
        return result;
    }

    public List<LimitingResourceQueue> getQueues() {
        return Collections.unmodifiableList(queues);
    }

    public List<LimitingResourceQueueElement> getUnassigned() {
        return Collections.unmodifiableList(unassignedElements);
    }

    public void assignedToQueue(LimitingResourceQueueElement element,
            LimitingResourceQueue queue) {
        Validate.isTrue(unassignedElements.contains(element));
        queue.addLimitingResourceQueueElement(element);
        unassignedElements.remove(element);
    }

    public LimitingResourceQueue getEquivalent(LimitingResourceQueue queue) {
        return queuesById.get(queue.getId());
    }

    public LimitingResourceQueueElement getEquivalent(
            LimitingResourceQueueElement element) {
        return elementsById.get(element.getId());
    }

    public void unassingFromQueue(LimitingResourceQueueElement externalElement) {
        LimitingResourceQueueElement queueElement = getEquivalent(externalElement);
        LimitingResourceQueue queue = queueElement.getLimitingResourceQueue();
        if (queue != null) {
            queue.removeLimitingResourceQueueElement(queueElement);
            unassignedElements.add(queueElement);
        }
    }

    public void removeUnassigned(LimitingResourceQueueElement queueElement) {
        unassignedElements.remove(queueElement);
    }

    public LimitingResourceQueue getQueueFor(Resource resource) {
        return queuesByResourceId.get(resource.getId());
    }

    public List<LimitingResourceQueue> getAssignableQueues(
            LimitingResourceQueueElement element) {
        final ResourceAllocation<?> resourceAllocation = element
                .getResourceAllocation();
        if (resourceAllocation instanceof SpecificResourceAllocation) {
            LimitingResourceQueue queue = getQueueFor(element
                    .getResource());
            Validate.notNull(queue);
            return Collections.singletonList(queue);
        } else if (resourceAllocation instanceof GenericResourceAllocation) {
            final GenericResourceAllocation generic = (GenericResourceAllocation) element
                    .getResourceAllocation();
            return findQueuesMatchingCriteria(generic.getCriterions());
        }
        throw new RuntimeException("unexpected type of: " + resourceAllocation);
    }

    public InsertionRequirements getRequirementsFor(
            LimitingResourceQueueElement element) {
        List<LimitingResourceQueueDependency> dependenciesStart = new ArrayList<LimitingResourceQueueDependency>();
        List<LimitingResourceQueueDependency> dependenciesEnd = new ArrayList<LimitingResourceQueueDependency>();
        fillIncoming(element, dependenciesStart, dependenciesEnd);
        return InsertionRequirements.forElement(getEquivalent(element),
                dependenciesStart, dependenciesEnd);
    }

    private void fillIncoming(LimitingResourceQueueElement element,
            List<LimitingResourceQueueDependency> dependenciesStart,
            List<LimitingResourceQueueDependency> dependenciesEnd) {
        Set<LimitingResourceQueueDependency> incoming = graph
                .incomingEdgesOf(element);
        for (LimitingResourceQueueDependency each : incoming) {
            List<LimitingResourceQueueDependency> addingTo = each
                    .modifiesDestinationStart() ? dependenciesStart
                    : dependenciesEnd;
            if (each.isOriginNotDetached()) {
                addingTo.add(each);
            } else {
                fillIncoming(each, addingTo);
            }
        }
    }

    private void fillIncoming(LimitingResourceQueueDependency next,
            List<LimitingResourceQueueDependency> result) {
        Set<LimitingResourceQueueDependency> incoming = graph
                .incomingEdgesOf(next.getHasAsOrigin());
        for (LimitingResourceQueueDependency each : incoming) {
            if (each.propagatesThrough(next)) {
                if (each.isOriginNotDetached()) {
                    result.add(each);
                } else {
                    fillIncoming(each, result);
                }
            }
        }
    }

    /**
     * @return all the gaps that could potentially fit <code>element</code>
     *         ordered by start date
     */
    public List<GapOnQueue> getPotentiallyValidGapsFor(
            InsertionRequirements requirements) {
        List<LimitingResourceQueue> assignableQueues = getAssignableQueues(requirements
                .getElement());
        List<List<GapOnQueue>> allGaps = gapsFor(assignableQueues, requirements);
        return GapsMergeSort.sort(allGaps);
    }

    private List<List<GapOnQueue>> gapsFor(
            List<LimitingResourceQueue> assignableQueues,
            InsertionRequirements requirements) {
        List<List<GapOnQueue>> result = new ArrayList<List<GapOnQueue>>();
        for (LimitingResourceQueue each : assignableQueues) {
            result.add(each.getGapsPotentiallyValidFor(requirements));
        }
        return result;
    }

    private List<LimitingResourceQueue> findQueuesMatchingCriteria(
            Set<Criterion> criteria) {
        List<LimitingResourceQueue> result = new ArrayList<LimitingResourceQueue>();
        final ICriterion compositedCriterion = CriterionCompounder.buildAnd(
                criteria).getResult();
        for (LimitingResourceQueue each : queues) {
            if (compositedCriterion.isSatisfiedBy(each.getResource())) {
                result.add(each);
            }
        }
        return result;
    }

    /**
     * @param externalQueueElement
     *            the queue element to insert
     * @return the list of elements that must be reinserted due to the insertion
     *         of <code>externalQueueElement</code>
     */
    public List<LimitingResourceQueueElement> getInsertionsToBeDoneFor(
            LimitingResourceQueueElement externalQueueElement) {
        LimitingResourceQueueElement queueElement = getEquivalent(externalQueueElement);
        DirectedGraph<LimitingResourceQueueElement, LimitingResourceQueueDependency> subGraph = buildOutgoingGraphFor(queueElement);
        CycleDetector<LimitingResourceQueueElement, LimitingResourceQueueDependency> cycleDetector = cycleDetector(subGraph);
        if (cycleDetector.detectCycles()) {
            throw new IllegalStateException("subgraph has cycles");
        }
        List<LimitingResourceQueueElement> result = new ArrayList<LimitingResourceQueueElement>();
        result.add(queueElement);
        result.addAll(getElementsOrderedTopologically(subGraph));
        unassignFromQueues(result);
        return result;
    }

    private DirectedGraph<LimitingResourceQueueElement, LimitingResourceQueueDependency> buildOutgoingGraphFor(
            LimitingResourceQueueElement queueElement) {
        SimpleDirectedGraph<LimitingResourceQueueElement, LimitingResourceQueueDependency> result = instantiateDirectedGraph();
        buildOutgoingGraphFor(result, queueElement);
        return result;
    }

    private void buildOutgoingGraphFor(
            DirectedGraph<LimitingResourceQueueElement, LimitingResourceQueueDependency> result,
            LimitingResourceQueueElement element) {
        Set<LimitingResourceQueueDependency> outgoingEdgesOf = graph
                .outgoingEdgesOf(element);
        result.addVertex(element);
        for (LimitingResourceQueueDependency each : outgoingEdgesOf) {
            addDependency(result, each);
            buildOutgoingGraphFor(result, each.getHasAsDestiny());
        }
    }

    private CycleDetector<LimitingResourceQueueElement, LimitingResourceQueueDependency> cycleDetector(
            DirectedGraph<LimitingResourceQueueElement, LimitingResourceQueueDependency> subGraph) {
        return new CycleDetector<LimitingResourceQueueElement, LimitingResourceQueueDependency>(
                subGraph);
    }

    private List<LimitingResourceQueueElement> getElementsOrderedTopologically(
            DirectedGraph<LimitingResourceQueueElement, LimitingResourceQueueDependency> subGraph) {
        return onlyAssigned(toList(topologicalIterator(subGraph)));
    }

    public static <V, E> TopologicalOrderIterator<V, E> topologicalIterator(
            DirectedGraph<V, E> subGraph) {
        return new TopologicalOrderIterator<V, E>(subGraph);
    }

    private static <T> List<T> toList(final Iterator<T> iterator) {
        List<T> result = new ArrayList<T>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    private List<LimitingResourceQueueElement> onlyAssigned(
            List<LimitingResourceQueueElement> list) {
        List<LimitingResourceQueueElement> result = new ArrayList<LimitingResourceQueueElement>();
        for (LimitingResourceQueueElement each : list) {
            if (!each.isDetached()) {
                result.add(each);
            }
        }
        return result;
    }

    private void unassignFromQueues(List<LimitingResourceQueueElement> result) {
        for (LimitingResourceQueueElement each : result) {
            if (!each.isDetached()) {
                unassingFromQueue(each);
            }
        }
    }

}
