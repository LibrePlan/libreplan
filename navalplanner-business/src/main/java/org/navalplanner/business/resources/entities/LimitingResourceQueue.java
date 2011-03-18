/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import static org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement.isAfter;
import static org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement.isInTheMiddle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.navalplanner.business.calendars.entities.CalendarAvailability;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.planner.limiting.entities.DateAndHour;
import org.navalplanner.business.planner.limiting.entities.Gap;
import org.navalplanner.business.planner.limiting.entities.Gap.GapOnQueue;
import org.navalplanner.business.planner.limiting.entities.GapInterval;
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

    public static Collection<LimitingResourceQueue> queuesOf(
            Collection<LimitingResourceQueueElement> queueElements) {
        Set<LimitingResourceQueue> result = new HashSet<LimitingResourceQueue>();
        for (LimitingResourceQueueElement each: queueElements) {
            result.add(each.getLimitingResourceQueue());
        }
        return result;
    }

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
        ResourceCalendar calendar = resource.getCalendar();
        List<CalendarAvailability> activationPeriods = calendar.getCalendarAvailabilities();

        for (LimitingResourceQueueElement each : limitingResourceQueueElements) {
            DateAndHour startTime = each.getStartTime();
            if (previousEnd == null || startTime.isAfter(previousEnd)) {
                List<GapInterval> gapIntervals = GapInterval.
                        create(previousEnd, startTime).
                        delimitByActivationPeriods(activationPeriods);
                if (!gapIntervals.isEmpty()) {
                    result.addAll(GapInterval.gapsOn(gapIntervals, resource));
                }
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

    public List<LimitingResourceQueueElement> getElementsSince(DateAndHour time) {
        List<LimitingResourceQueueElement> result = new ArrayList<LimitingResourceQueueElement>();

        for (LimitingResourceQueueElement each: getLimitingResourceQueueElements()) {
            if (isInTheMiddle(each, time) || isAfter(each, time)) {
                result.add(each);
            }
        }
        return result;
    }

    public void queueElementMoved(
            LimitingResourceQueueElement limitingResourceQueueElement) {
        invalidCachedGaps();
    }

    public String toString() {
        return getResource() != null ? getResource().getName() : "";
    }

}
