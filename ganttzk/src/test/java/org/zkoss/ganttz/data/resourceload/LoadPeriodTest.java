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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.joda.time.LocalDate;
import org.junit.Test;

public class LoadPeriodTest {

    private LocalDate start;
    private LocalDate end;
    private LoadLevel loadLevel;

    private LoadPeriod loadPeriod;
    private List<LoadPeriod> unsortedList;

    private void givenExampleLoadPeriod() {
        start = new LocalDate(2009, 11, 4);
        end = new LocalDate(2009, 12, 8);
        givenExampleLoadPeriod(start, end);
    }

    private void givenExampleLoadPeriod(LocalDate start, LocalDate end) {
        loadLevel = new LoadLevel(50);
        givenExampleLoadPeriod(start, end, loadLevel);
    }

    private void givenExampleLoadPeriod(LocalDate start, LocalDate end,
            LoadLevel loadLevel) {
        loadPeriod = new LoadPeriod(start, end, totalHours, assignedHours,
                loadLevel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void aLoadPeriodMustHaveAStartDate() {
        new LoadPeriod(null, new LocalDate(), totalHours, assignedHours,
                correctLoadLevel());
    }

    private static final int totalHours = 100;

    private static final int assignedHours = 40;

    private static LoadLevel correctLoadLevel() {
        return new LoadLevel(40);
    }

    @Test(expected = IllegalArgumentException.class)
    public void aLoadPeriodMustHaveAnEndDate() {
        new LoadPeriod(new LocalDate(), null, totalHours, assignedHours,
                correctLoadLevel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void theEndDateCantBeBeforeTheStartDate() {
        LocalDate start = new LocalDate(2009, 10, 4);
        LocalDate end = new LocalDate(2009, 10, 3);
        new LoadPeriod(start, end, totalHours, assignedHours,
                correctLoadLevel());
    }

    @Test
    public void theEndDateCanBeTheSameThanTheStartDate() {
        LocalDate start = new LocalDate(2009, 10, 4);
        LocalDate end = new LocalDate(2009, 10, 4);
        new LoadPeriod(start, end, totalHours, assignedHours,
                correctLoadLevel());
    }

    @Test
    public void theStartDateCanBeRetrieved() {
        givenExampleLoadPeriod();
        assertThat(loadPeriod.getStart(), equalTo(start));
    }

    @Test
    public void theEndDateCanBeRetrieved() {
        givenExampleLoadPeriod();
        assertThat(loadPeriod.getEnd(), equalTo(end));
    }

    @Test
    public void twoLoadPeriodOverlapCanOverlap() {
        givenExampleLoadPeriod(new LocalDate(2009, 11, 1), new LocalDate(2009,
                12, 1));
        assertTrue(loadPeriod.overlaps(create(2009, 11, 2, 2009, 11, 3)));
        assertTrue(loadPeriod.overlaps(create(2009, 10, 30, 2009, 11, 2)));
        assertTrue(loadPeriod.overlaps(create(2009, 10, 20, 2009, 12, 2)));

        assertFalse(loadPeriod.overlaps(create(2009, 12, 2, 2009, 12, 3)));
        assertFalse(loadPeriod.overlaps(create(2009, 10, 20, 2009, 10, 21)));
    }

    @Test
    // [start, end)
    public void startInclusiveButEndExclusive() {
        givenExampleLoadPeriod(new LocalDate(2009, 11, 1), new LocalDate(2009,
                12, 1));
        assertFalse(loadPeriod.overlaps(create(2009, 12, 1, 2009, 12, 3)));
        assertFalse(loadPeriod.overlaps(create(2009, 10, 1, 2009, 11, 1)));
    }

    @Test
    public void pointDontOverlap() {
        givenExampleLoadPeriod(new LocalDate(2009, 11, 1), new LocalDate(2009,
                12, 1));
        assertFalse(loadPeriod.overlaps(create(2009, 11, 1, 2009, 11, 1)));
    }

    private static LoadPeriod create(int startYear, int startMonth,
            int startDay, int endYear, int endMonth, int endDay) {
        return new LoadPeriod(new LocalDate(startYear, startMonth, startDay),
                new LocalDate(endYear, endMonth, endDay), totalHours,
                assignedHours, correctLoadLevel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadPeriodsThatOverlapCannotBeSorted() {
        LoadPeriod.sort(Arrays.asList(create(2009, 4, 10, 2010, 1, 12), create(
                2009, 4, 11, 2011, 1, 20)));
    }

    @Test
    public void loadPeriodsThatDontOverlapCanBeSorted() {
        givenUnsortedListOfPeriods();
        List<LoadPeriod> sorted = LoadPeriod.sort(unsortedList);
        thenIsSorted(sorted);
    }

    @Test
    public void aZeroDaysLoadPeriodStartingTheSameDateThanANoZeroDaysLoadPeriodGoesAfter() {
        givenUnsortedListOfPeriods(create(2009, 4, 10, 2010, 1, 12), create(
                2009, 4, 10, 2009, 4, 10));
        List<LoadPeriod> sorted = LoadPeriod.sort(unsortedList);
        thenIsSorted(sorted);
    }

    private void givenUnsortedListOfPeriods() {
        givenUnsortedListOfPeriods(create(2009, 4, 10, 2010, 1, 12), create(
                2010, 1, 12, 2010, 1, 12), create(2010, 2, 13, 2010, 5, 7),
                create(2009, 3, 5, 2009, 3, 10));
    }

    private void givenUnsortedListOfPeriods(LoadPeriod... periods) {
        unsortedList = Arrays.asList(periods);
    }

    private void thenIsSorted(List<LoadPeriod> sorted) {
        ListIterator<LoadPeriod> listIterator = sorted.listIterator();
        LoadPeriod previous = null;
        LoadPeriod current = null;
        while (listIterator.hasNext()) {
            previous = current;
            current = listIterator.next();
            if (previous != null) {
                assertFalse(current.getStart().isBefore(previous.getEnd()));
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void aLoadPeriodMustHaveANotNullLoadLevel() {
        givenExampleLoadPeriod();
        new LoadPeriod(start, end, totalHours, assignedHours, null);
    }

    @Test
    public void theLoadLevelCanBeRetrieved() {
        givenExampleLoadPeriod();
        assertThat(loadPeriod.getLoadLevel(), equalTo(loadLevel));
    }

}
