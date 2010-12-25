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

package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.planner.limiting.entities.DateAndHour;
import org.navalplanner.business.planner.limiting.entities.Gap;
import org.navalplanner.business.planner.limiting.entities.Gap.GapOnQueue;
import org.navalplanner.business.planner.limiting.entities.InsertionRequirements;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class LimitingResourceQueue extends BaseEntity {

    private Resource resource;

    private SortedSet<LimitingResourceQueueElement> limitingResourceQueueElements =
        new TreeSet<LimitingResourceQueueElement>(new LimitingResourceQueueElementComparator());

    private List<GapOnQueue> cachedGaps;

    public static LimitingResourceQueue create() {
        return create(new LimitingResourceQueue());
    }

    protected LimitingResourceQueue() {

    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void addLimitingResourceQueueElement(LimitingResourceQueueElement element) {
        element.setLimitingResourceQueue(this);
        limitingResourceQueueElements.add(element);
        invalidCachedGaps();
    }

    public void removeLimitingResourceQueueElement(LimitingResourceQueueElement element) {
        limitingResourceQueueElements.remove(element);
        element.detach();
        invalidCachedGaps();
    }

    private void invalidCachedGaps() {
        cachedGaps = null;
    }

    public List<GapOnQueue> getGaps() {
        if (cachedGaps == null) {
            cachedGaps = calculateGaps();
        }
        return cachedGaps;
    }

    private List<GapOnQueue> calculateGaps() {
        List<Gap> result = new ArrayList<Gap>();
        DateAndHour previousEnd = null;
        for (LimitingResourceQueueElement each : limitingResourceQueueElements) {
            DateAndHour startTime = each.getStartTime();
            if (previousEnd == null || startTime.isAfter(previousEnd)) {
                result.add(Gap.create(resource, previousEnd, startTime));
            }
            previousEnd = each.getEndTime();
        }
        result.add(Gap.create(resource, previousEnd, null));
        return GapOnQueue.onQueue(this, result);
    }

    public SortedSet<LimitingResourceQueueElement> getLimitingResourceQueueElements() {
        return Collections.unmodifiableSortedSet(limitingResourceQueueElements);
    }

    /**
     * @return the gaps that could potentially be valid for
     *         <code>requirements</code> ordered by start date
     */
    public List<GapOnQueue> getGapsPotentiallyValidFor(
            InsertionRequirements requirements) {
        List<GapOnQueue> result = new ArrayList<GapOnQueue>();
        for (GapOnQueue each : getGaps()) {
            if (requirements.isPotentiallyValid(each.getGap())) {
                result.add(each);
            }
        }
        return result;
    }

    public List<LimitingResourceQueueElement> getElementsAfter(
            LimitingResourceQueueElement element) {
        List<LimitingResourceQueueElement> queueElements = new ArrayList<LimitingResourceQueueElement>(
                        limitingResourceQueueElements);
        int position = Collections.binarySearch(queueElements, element,
                LimitingResourceQueueElement.byStartTimeComparator());
        assert position >= 0 : "the element must be in the list";
        return queueElements.subList(position + 1, queueElements.size());
    }

    public void queueElementMoved(
            LimitingResourceQueueElement limitingResourceQueueElement) {
        invalidCachedGaps();
    }

    /**
     * limitingResourceQueueElements is a SortedSet of
     * {@link LimitingResourceQueueElement} ordered by startDate
     *
     * When an element if shifted visually in a list of queue elements, it might
     * be necessary to rearrange the SortedSet so the new element occupies its
     * real position. Instead of rearranging the SortedSet again, the element is
     * removed from the set and inserted again. Insert operation guarantees that
     * an element is inserted at the right position
     *
     */
    public void reloadLimitingResourceQueueElement(
            LimitingResourceQueueElement element) {
        limitingResourceQueueElements.remove(element);
        limitingResourceQueueElements.add(element);
    }

    public String toString() {
        return getResource() != null ? getResource().getName() : "";
    }

}
