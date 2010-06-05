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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class QueuesState {

    private final List<LimitingResourceQueue> queues;

    private final List<LimitingResourceQueueElement> unassignedElements;

    public QueuesState(
            List<LimitingResourceQueue> limitingResourceQueues,
            List<LimitingResourceQueueElement> unassignedLimitingResourceQueueElements) {
        this.queues = new ArrayList<LimitingResourceQueue>(
                limitingResourceQueues);
        this.unassignedElements = new ArrayList<LimitingResourceQueueElement>(
                unassignedLimitingResourceQueueElements);
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

    public void addUnassigned(LimitingResourceQueueElement queueElement) {
        unassignedElements.add(queueElement);
    }

    public void removeUnassigned(LimitingResourceQueueElement queueElement) {
        unassignedElements.remove(queueElement);
    }

}
