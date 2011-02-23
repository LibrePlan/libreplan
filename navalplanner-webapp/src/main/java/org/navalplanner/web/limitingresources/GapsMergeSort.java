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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.comparators.BooleanComparator;
import org.navalplanner.business.planner.limiting.entities.Gap.GapOnQueue;


/**
 * Utility class for doing a merge sort of several ordered list of Gaps <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GapsMergeSort {

    private GapsMergeSort() {
    }

    private static class CurrentGap implements Comparable<CurrentGap> {

        static List<CurrentGap> convert(
                Collection<? extends Iterator<GapOnQueue>> iterators) {
            List<CurrentGap> result = new ArrayList<CurrentGap>();
            for (Iterator<GapOnQueue> iterator : iterators) {
                result.add(new CurrentGap(iterator));
            }
            return result;
        }

        private Iterator<GapOnQueue> iterator;

        private GapOnQueue current;

        private CurrentGap(Iterator<GapOnQueue> iterator) {
            this.iterator = iterator;
        }

        public GapOnQueue consume() {
            GapOnQueue result = getCurrent();
            current = null;
            return result;
        }

        boolean hasFinished() {
            return current == null && !iterator.hasNext();
        }

        private GapOnQueue getCurrent() {
            if (hasFinished()) {
                throw new IllegalStateException("already finished");
            }
            if (current != null) {
                return current;
            }
            return current = iterator.next();
        }

        /**
         * Ordering by hasFinished and the gap. An already finished is
         * considered bigger than a not finished
         */
        @Override
        public int compareTo(CurrentGap other) {
            int finishComparison = BooleanComparator.getFalseFirstComparator()
                    .compare(hasFinished(), other.hasFinished());
            if (finishComparison != 0) {
                return finishComparison;
            } else if (hasFinished()) {
                assert other.hasFinished();
                return 0;
            } else {
                assert !hasFinished() && !other.hasFinished();
                return getCurrent().getGap().compareTo(
                        other.getCurrent().getGap());
            }
        }
    }

    public static List<GapOnQueue> sort(
            List<List<GapOnQueue>> orderedListsOfGaps) {

        List<GapOnQueue> result = new ArrayList<GapOnQueue>();

        if (orderedListsOfGaps.isEmpty()) {
            return result;
        }
        if (orderedListsOfGaps.size() == 1) {
            return orderedListsOfGaps.get(0);
        }

        List<CurrentGap> currentGaps = CurrentGap.convert(iteratorsFor(orderedListsOfGaps));
        CurrentGap min = Collections.min(currentGaps);
        while (!currentGaps.isEmpty() && !min.hasFinished()) {
            result.add(min.consume());
            if (min.hasFinished()) {
                currentGaps.remove(min);
                if (!currentGaps.isEmpty()) {
                    min = Collections.min(currentGaps);
                }
            }
        }
        return result;
    }

    private static List<Iterator<GapOnQueue>> iteratorsFor(
            List<List<GapOnQueue>> orderedListsOfGaps) {
        List<Iterator<GapOnQueue>> result = new ArrayList<Iterator<GapOnQueue>>();
        for (List<GapOnQueue> each : orderedListsOfGaps) {
            result.add(each.iterator());
        }
        return result;
    }

}
