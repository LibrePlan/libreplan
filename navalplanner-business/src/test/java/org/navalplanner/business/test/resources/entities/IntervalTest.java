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

package org.navalplanner.business.test.resources.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest.year;

import org.junit.Test;
import org.navalplanner.business.resources.entities.Interval;
import org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest;

public class IntervalTest {

    @Test(expected = IllegalArgumentException.class)
    public void testStartDateMustBeBeforeThanEndDate() throws Exception {
        Interval.range(CriterionSatisfactionDAOTest.year(2000),
                CriterionSatisfactionDAOTest.year(1999));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartDateMustBeNotNull() throws Exception {
        Interval.range(null, CriterionSatisfactionDAOTest.year(1999));
    }

    @Test
    public void intervalCanBeOpenEnded() throws Exception {
        Interval.range(CriterionSatisfactionDAOTest.year(1990), null);
    }

    @Test
    public void testContainsPointInTime() {
        Interval openEnded = Interval.from(CriterionSatisfactionDAOTest
                .year(1990));
        Interval range = Interval.range(
                CriterionSatisfactionDAOTest.year(1990),
                CriterionSatisfactionDAOTest.year(2000));
        Interval startPoint = Interval.point(year(1990));
        Interval endPoint = Interval.point(year(2000));
        assertTrue(openEnded.contains(CriterionSatisfactionDAOTest.year(5000)));
        assertFalse(range.contains(year(5000)));
        assertTrue(range.contains(year(1990)));
        assertFalse(range.contains(year(2000)));
        assertTrue(range.contains(year(1991)));
        assertFalse(range.contains(year(1989)));
        assertFalse(range.includes(startPoint));
        assertFalse(range.includes(endPoint));
    }

    @Test
    public void testPointsOnlyContainsThemselves() throws Exception {
        Interval point = Interval
                .point(CriterionSatisfactionDAOTest.year(1990));
        assertTrue(point.contains(CriterionSatisfactionDAOTest.year(1990)));
        assertFalse(point.contains(CriterionSatisfactionDAOTest.year(2010)));
    }

    @Test
    public void testIntervalsAreStartInclusiveAndEndExclusive()
            throws Exception {
        Interval range = Interval.range(year(1990), year(2000));
        assertTrue(range.contains(year(1990)));
        assertFalse(range.contains(year(2000)));
        assertFalse(range.contains(year(1990).minusDays(1)));
        assertFalse(range.contains(year(2000).plusDays(1)));
    }

    @Test
    public void testIncludes() throws Exception {
        Interval bigRange = Interval.range(year(1990), year(2000));
        Interval included = Interval.range(year(1990), year(1998));
        Interval point = Interval.point(year(1996));
        assertTrue(bigRange.includes(included));
        assertTrue(bigRange.includes(point));
        assertTrue(included.includes(point));
    }

    @Test
    public void testStartPointDoesntOverlapsWithRange() throws Exception {
        Interval range = Interval.range(year(1990), year(2005));
        Interval openEndedRange = Interval.from(year(1990));
        Interval point = Interval.point(year(1990));
        Interval internalPoint = Interval.point(year(1991));

        assertFalse(point.overlapsWith(range));
        assertFalse(point.overlapsWith(openEndedRange));
        assertFalse(range.overlapsWith(point));
        assertFalse(openEndedRange.overlapsWith(point));

        assertTrue(internalPoint.overlapsWith(range));
        assertTrue(internalPoint.overlapsWith(openEndedRange));
        assertTrue(range.overlapsWith(internalPoint));
        assertTrue(openEndedRange.overlapsWith(internalPoint));
    }

    @Test
    public void testOverlapsWith() {
        Interval firstRange = Interval.range(year(1990), year(2005));
        Interval igalia = Interval.from(year(2001));
        Interval distantPoint = Interval.point(year(2030));
        Interval pointInFirstRange = Interval.point(year(2000));
        Interval outRange = Interval.range(year(2020), year(2030));
        assertTrue(firstRange.overlapsWith(igalia));
        assertTrue(firstRange.overlapsWith(Interval.range(year(1980),
                year(1991))));
        assertTrue(igalia.overlapsWith(firstRange));
        assertTrue(outRange.overlapsWith(igalia));
        assertFalse(outRange.overlapsWith(firstRange));
        assertTrue(distantPoint.overlapsWith(igalia));
        assertFalse(distantPoint.overlapsWith(firstRange));
        assertTrue(igalia.overlapsWith(distantPoint));
        assertTrue(distantPoint.overlapsWith(igalia));
        assertFalse(firstRange.overlapsWith(distantPoint));
        assertTrue(firstRange.overlapsWith(pointInFirstRange));
    }

    @Test
    public void testIntervalFinishingAtTheStartOfOtherDontOverlap()
            throws Exception {
        Interval range = Interval.range(year(2000), year(2005));
        Interval from = Interval.from(year(2000));
        Interval before = Interval.range(year(1995), year(2000));
        assertFalse(range.overlapsWith(before));
        assertFalse(before.overlapsWith(range));

        assertFalse(from.overlapsWith(before));
        assertFalse(before.overlapsWith(from));
    }

    @Test
    public void testStartPointDoesntOverlapsWithRanges() {
        Interval range = Interval.range(year(2010), year(2030));
        Interval point = Interval.point(year(2010));
        Interval otherRange = Interval.from(year(2010));
        assertFalse(point.overlapsWith(range));
        assertFalse(range.overlapsWith(point));
        assertFalse(point.overlapsWith(otherRange));
        assertFalse(otherRange.overlapsWith(point));
    }

    @Test
    public void testCreatingPointWithRange() throws Exception {
        Interval point = Interval.point(year(1990));
        Interval range = Interval.range(year(1990), year(1990));
        assertEquals(point, range);
    }

    @Test
    public void testOverlappingWhenContained() {
        Interval range = Interval.range(year(1990), year(2000));
        Interval contained = Interval.range(year(1995), year(1997));
        assertTrue(range.overlapsWith(contained));
        assertTrue(contained.overlapsWith(range));
    }
}
