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

package org.zkoss.ganttz.data.resourceload;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.zkoss.ganttz.data.GanttDate;

public class LoadTimelineTest {

    private LoadTimeLine loadTimeLine;
    private String conceptName;

    @Test(expected = IllegalArgumentException.class)
    public void aLoadTimelineMustHaveANotNullName() {
        new LoadTimeLine(null, Collections.<LoadPeriod> emptyList(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void aLoadTimelineMustHaveANotEmptyName() {
        new LoadTimeLine("", Collections.<LoadPeriod> emptyList(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void aLoadTimelineCannotHaveNullLoadPeriods() {
        new LoadTimeLine("bla", null, null);
    }

    @Test
    public void theConceptNameCanBeRetrieved() {
        givenValidLoadTimeLine();
        assertThat(conceptName, equalTo(loadTimeLine.getConceptName()));
    }

    private void givenValidLoadTimeLine() {
        conceptName = "bla";
        loadTimeLine = new LoadTimeLine(conceptName,
                Arrays.asList(new LoadPeriod(GanttDate
                        .createFrom(new LocalDate(2009, 10, 5)), GanttDate
                        .createFrom(new LocalDate(2009, 10, 11)), 100, 20,
                        new LoadLevel(20))), null);
    }

    @Test
    public void aLoadTimelineWithZeroLoadPeriodsIsEmpty() {
        LoadTimeLine timeline = new LoadTimeLine("bla", Collections
                .<LoadPeriod> emptyList(), null);
        assertTrue(timeline.isEmpty());
    }

    @Test
    public void aLoadTimelineSortsItsReceivedPeriods() {
        LoadPeriod l1 = new LoadPeriod(GanttDate.createFrom(new LocalDate(2009,
                10, 5)), GanttDate.createFrom(new LocalDate(2009, 10, 11)),
                100, 20, new LoadLevel(20));
        LoadPeriod l2 = new LoadPeriod(GanttDate.createFrom(new LocalDate(2009,
                5, 3)), GanttDate.createFrom(new LocalDate(2009, 6, 3)), 100,
                20, new LoadLevel(20));
        LoadTimeLine loadTimeLine = new LoadTimeLine("bla", Arrays.asList(l1,
                l2), null);

        List<LoadPeriod> loadPeriods = loadTimeLine.getLoadPeriods();
        assertThat(loadPeriods.get(0), sameInstance(l2));
        assertThat(loadPeriods.get(1), sameInstance(l1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void theLoadPeriodsMustNotOverlap() {
        LoadPeriod l1 = new LoadPeriod(GanttDate.createFrom(new LocalDate(2009,
                10, 5)), GanttDate.createFrom(new LocalDate(2009, 10, 11)),
                100, 20, new LoadLevel(20));
        LoadPeriod l2 = new LoadPeriod(GanttDate.createFrom(new LocalDate(2009,
                5, 3)), GanttDate.createFrom(new LocalDate(2009, 10, 10)), 100,
                20, new LoadLevel(20));
        new LoadTimeLine("bla", Arrays.asList(l1, l2), null);
    }

}
