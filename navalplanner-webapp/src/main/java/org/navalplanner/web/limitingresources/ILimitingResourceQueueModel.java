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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.limiting.entities.DateAndHour;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;

/**
 * Contains operations for showing {@link LimitingResourceQueue} and its
 * elements ({@link LimitingResourceQueueElement}), plus showing all
 * {@link LimitingResourceQueueElement} which are not assigned to any
 * {@link LimitingResourceQueue}
 *
 * <strong>Conversational protocol:</strong>
 * <ul>
 * <li>
 * Initial conversation step: <code>initGlobalView</code></li>
 * <li>
 * Intermediate conversation steps:
 * <code>assignLimitingResourceQueueElement</code>,
 * <code>getLimitingResourceQueues</code>,
 * <code>getUnassignedLimitingResourceQueueElements</code></li>
 * <li>
 * Final conversation step: <code>confirm</code></li>
 *
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public interface ILimitingResourceQueueModel {

    List<LimitingResourceQueueElement> nonAppropriativeAllocation(
            LimitingResourceQueueElement element, LimitingResourceQueue queue, DateAndHour time);

    /**
     * Assigns a {@link LimitingResourceQueueElement} to its corresponding
     * {@link LimitingResourceQueue} There is one and only one queue for every
     * limiting resource. An element is assigned to its queue searching by
     * element.resource. Allocation within the queue is done by finding the
     * first gap in the queue that fits the initial intented hours assigned to
     * element.resourceallocation. The method also generates
     * {@link DayAssignment} once the allocation is done Returns the inserted
     * queue elements. More than one can be inserted because inserting
     * <code>element</code> can imply to move its sucessors.<br />
     * The only case were an allocation cannot be done is if there's not any
     * queue that can hold the element (only for a generic allocation, there's
     * not any queue that matches the criteria of the element). In this case an
     * empty list is returned
     * @param element
     */
    List<LimitingResourceQueueElement> assignLimitingResourceQueueElement(
            LimitingResourceQueueElement element);

    ZoomLevel calculateInitialZoomLevel();

    /**
     * Saves all {@link LimitingResourceQueue}
     */
    void confirm();

    List<LimitingResourceQueue> getAssignableQueues(
            LimitingResourceQueueElement element);

    LimitingResourceQueueElement getLimitingResourceQueueElement();

    /**
     * Return all {@link LimitingResourceQueue}
     *
     * @return
     */
    List<LimitingResourceQueue> getLimitingResourceQueues();

    Order getOrderByTask(TaskElement task);

    /**
     * Returns all existing {@link LimitingResourceQueueElement} which are not
     * assigned to any {@link LimitingResourceQueue}
     *
     * @return
     */
    List<LimitingResourceQueueElement> getUnassignedLimitingResourceQueueElements();

    Interval getViewInterval();

    void init(LimitingResourceQueueElement element);

    /**
     * Loads {@link LimitingResourceQueue} and unassigned {@link LimitingResourceQueueElement} from DB
     */
    void initGlobalView();

    /**
     * Inserts element into queue at a specific time
     *
     * If it's not possible to insert element at that time because there's no
     * room, for instance, then the task that is at that moment occupying that
     * space is moved it to the end of the queue. This process is done
     * recursively until there's room enough for holding the element to insert
     *
     * @param element
     * @param queue
     * @param allocationTime
     */
    Set<LimitingResourceQueueElement> appropriativeAllocation(
            LimitingResourceQueueElement element, LimitingResourceQueue queue,
            DateAndHour allocationTime);

    void unschedule(LimitingResourceQueueElement element);


    void removeUnassignedLimitingResourceQueueElement(
            LimitingResourceQueueElement element);

    boolean userCanRead(Order order, String loginName);

    List<LimitingResourceQueueElement> replaceLimitingResourceQueueElement(
            LimitingResourceQueueElement oldElement,
            LimitingResourceQueueElement newElement);

    Set<LimitingResourceQueueElement> assignLimitingResourceQueueElements(
            List<LimitingResourceQueueElement> elements);

}
