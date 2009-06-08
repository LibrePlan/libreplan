package org.navalplanner.business.test.resources.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Interval;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.test.resources.daos.CriterionDAOTest;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest.year;

/**
 * Tests for {@link CriterionSatisfaction} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionSatisfactionTest {

    @Test
    public void testFinish() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                year(2000), criterion, worker);
        Date end = year(2006);
        criterionSatisfaction.finish(end);
        assertTrue(criterionSatisfaction.isFinished());
        assertEquals(end, criterionSatisfaction.getEndDate());
        criterionSatisfaction.getEndDate().setTime(end.getTime() + 2000);
        assertEquals("endDate must be well encapsulated", end,
                criterionSatisfaction.getEndDate());
    }

    @Test
    public void canFinishWhenItStarted() throws Exception {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        Date start = year(2000);
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                start, criterion, worker);
        criterionSatisfaction.finish(start);
        assertTrue(criterionSatisfaction.isFinished());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCantFinishBeforeStart() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                year(2000), criterion, worker);
        criterionSatisfaction.finish(year(1999));
    }

    @Test
    public void testIsEnforcedAtDate() {
        final Criterion criterion = CriterionDAOTest.createValidCriterion();
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                year(2000), criterion, worker);
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
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        CriterionSatisfaction criterionSatisfaction = new CriterionSatisfaction(
                year(2000), criterion, worker);
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
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        Interval[] intervals = { Interval.from(year(1000)),
                Interval.range(year(1100), year(9000)),
                Interval.point(year(1101)), Interval.from(year(1200)),
                Interval.range(year(3000), year(4000)) };
        List<CriterionSatisfaction> orderedSatisfactions = new ArrayList<CriterionSatisfaction>();
        for (Interval interval : intervals) {
            orderedSatisfactions.add(new CriterionSatisfaction(criterion,
                    worker, interval));
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
        Worker worker = new Worker("firstName", "surName", "2333232", 10);
        CriterionSatisfaction posterior = new CriterionSatisfaction();
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
