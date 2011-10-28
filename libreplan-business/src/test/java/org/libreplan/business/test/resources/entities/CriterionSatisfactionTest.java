/*
 * This file is part of LibrePlan
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

package org.libreplan.business.test.resources.entities;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.test.resources.daos.CriterionSatisfactionDAOTest.year;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.business.resources.entities.Interval;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.test.resources.daos.CriterionDAOTest;

/**
 * Tests for {@link CriterionSatisfaction} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionSatisfactionTest {

    @Test
    public void testFinish() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232");
        CriterionSatisfaction criterionSatisfaction = CriterionSatisfaction.create(year(2000), criterion, worker);
        LocalDate end = year(2006);
        criterionSatisfaction.finish(end);
        assertTrue(criterionSatisfaction.isFinished());
        assertEquals(end, criterionSatisfaction.getEndDate());
    }

    @Test
    public void canFinishWhenItStarted() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232");
        LocalDate start = year(2000);
        CriterionSatisfaction criterionSatisfaction = CriterionSatisfaction.create(start, criterion, worker);
        criterionSatisfaction.finish(start);
        assertTrue(criterionSatisfaction.isFinished());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCantFinishBeforeStart() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232");
        CriterionSatisfaction criterionSatisfaction = CriterionSatisfaction.create(year(2000), criterion, worker);
        criterionSatisfaction.finish(year(1999));
    }

    @Test
    public void testIsEnforcedAtDate() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232");
        CriterionSatisfaction criterionSatisfaction = CriterionSatisfaction.create(year(2000), criterion, worker);
        assertTrue(criterionSatisfaction.isEnforcedAt(year(3000)));
        assertTrue(criterionSatisfaction.isEnforcedAt(year(2000)));
        assertFalse(criterionSatisfaction.isEnforcedAt(year(1999)));
        criterionSatisfaction.finish(year(2005));
        assertFalse(criterionSatisfaction.isEnforcedAt(year(3000)));
        assertFalse(criterionSatisfaction.isAlwaysEnforcedIn(Interval.range(
                year(2001), year(2006))));
        assertTrue(criterionSatisfaction.isEnforcedAt(year(2000)));
        assertTrue(criterionSatisfaction.isEnforcedAt(year(2001)));
        assertFalse(criterionSatisfaction.isEnforcedAt(year(1999)));
    }

    @Test
    public void testEnforcedAtSomePointInInterval() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232");
        CriterionSatisfaction criterionSatisfaction = CriterionSatisfaction.create(year(2000), criterion, worker);
        assertTrue(criterionSatisfaction.overlapsWith(Interval.range(
                year(2001), year(4000))));
        assertTrue(criterionSatisfaction.overlapsWith(Interval.range(
                year(2005), year(4000))));
        assertTrue(criterionSatisfaction.overlapsWith(Interval.range(
                year(1999), year(2001))));

        criterionSatisfaction.finish(year(2004));

        assertTrue(criterionSatisfaction.overlapsWith(Interval.range(
                year(2002), year(4000))));
        assertTrue(criterionSatisfaction.overlapsWith(Interval.range(
                year(2002), null)));
        assertFalse(criterionSatisfaction.overlapsWith(Interval.range(
                year(2005), year(4000))));
        assertTrue(criterionSatisfaction.overlapsWith(Interval.range(
                year(1999), null)));
        assertFalse(criterionSatisfaction.overlapsWith(Interval.range(
                year(1990), year(1995))));
    }

    @Test
    public void testCriterionSatisfactionsStartComparator() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = Worker.create("firstName", "surName", "2333232");
        Interval[] intervals = { Interval.from(year(1000)),
                Interval.range(year(1100), year(9000)),
                Interval.point(year(1101)), Interval.from(year(1200)),
                Interval.range(year(3000), year(4000)) };
        List<CriterionSatisfaction> orderedSatisfactions = new ArrayList<CriterionSatisfaction>();
        for (Interval interval : intervals) {
            orderedSatisfactions.add(CriterionSatisfaction.create(criterion, worker, interval));
        }
        List<CriterionSatisfaction> copy = new ArrayList<CriterionSatisfaction>(
                orderedSatisfactions);
        assertThat(copy, equalTo(orderedSatisfactions));
        for (int i = 0; i < 20; i++) {
            Collections.shuffle(copy);
            Collections.sort(copy, CriterionSatisfaction.BY_START_COMPARATOR);
            assertThat(copy, equalTo(orderedSatisfactions));
        }
    }

    @Test
    public void testGoesBeforeWithoutOverlapping() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        CriterionSatisfaction posterior = CriterionSatisfaction.create();
        posterior.setCriterion(criterion);
        posterior.setStartDate(year(2000));
        posterior.setEndDate(year(2008));
        Interval[] goesAfterOrOverlapsIntervals = { Interval.from(year(2000)),
                Interval.from(year(2001)), Interval.from(year(1999)),
                Interval.range(year(1999), year(2001)),
                Interval.from(year(2009)),
                Interval.range(year(2009), year(2012)) };
        for (Interval interval : goesAfterOrOverlapsIntervals) {
            CriterionSatisfaction copied = posterior.copy();
            copied.setStartDate(interval.getStart());
            copied.setEndDate(interval.getEnd());
            assertFalse(interval + " shouldn't go before", copied
                    .goesBeforeWithoutOverlapping(posterior));
        }
        Interval[] goesBeforeWithoutOverlappingInterval = {
                Interval.point(year(2000)),
                Interval.range(year(1990), year(2000)),
                Interval.range(year(1990), year(1997)) };
        for (Interval interval : goesBeforeWithoutOverlappingInterval) {
            CriterionSatisfaction copied = posterior.copy();
            copied.setStartDate(interval.getStart());
            copied.setEndDate(interval.getEnd());
            assertTrue(copied.goesBeforeWithoutOverlapping(posterior));
        }

    }
}
