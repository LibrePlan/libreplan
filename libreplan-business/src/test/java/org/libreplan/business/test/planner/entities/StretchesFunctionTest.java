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

package org.libreplan.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.Stretch;
import org.libreplan.business.planner.entities.StretchesFunction;
import org.libreplan.business.planner.entities.StretchesFunction.Interval;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;

/**
 * Tests for {@link StretchesFunction} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class StretchesFunctionTest {

    private static final LocalDate START_DATE = new LocalDate();

    private static final LocalDate END_DATE = START_DATE.plusDays(10);

    private StretchesFunction stretchesFunction;

    private ResourceAllocation<?> resourceAllocation;

    private ResourceAllocation<?> givenResourceAllocation() {
        resourceAllocation = createNiceMock(ResourceAllocation.class);

        expect(resourceAllocation.getStartDate()).andReturn(START_DATE).anyTimes();
        expect(resourceAllocation.getEndDate()).andReturn(END_DATE).anyTimes();

        expect(resourceAllocation.getIntraDayStartDate())
                .andReturn(IntraDayDate.create(START_DATE, EffortDuration.zero()))
                .anyTimes();

        expect(resourceAllocation.getIntraDayEndDate())
                .andReturn(IntraDayDate.create(END_DATE, EffortDuration.zero()))
                .anyTimes();

        replay(resourceAllocation);
        return resourceAllocation;
    }

    private StretchesFunction givenStretchesFunction() {
        stretchesFunction = StretchesFunction.create();
        stretchesFunction.setResourceAllocation(givenResourceAllocation());
        return stretchesFunction;
    }

    private Stretch givenStretchAsChild() {
        Stretch result = new Stretch();
        stretchesFunction.addStretch(result);
        return result;
    }

    private Stretch givenStretchAsChild(BigDecimal lengthPercentage, BigDecimal amountWorkPercentage) {
        Stretch stretch = givenStretchAsChild();
        stretch.setLengthPercentage(lengthPercentage);
        stretch.setAmountWorkPercentage(amountWorkPercentage);

        return stretch;
    }

    private Stretch givenStretchAsChild(LocalDate date, BigDecimal amountWorkPercentage) {
        return givenStretchAsChild(Stretch.getLengthProportionByDate(resourceAllocation, date), amountWorkPercentage);
    }

    @Test
    public void stretchesFunctionCheckNoEmpty1() {
        givenStretchesFunction();
        assertFalse(stretchesFunction.isNoEmptyConstraint());
    }

    @Test
    public void stretchesFunctionCheckNoEmpty2() {
        givenStretchesFunction();
        givenStretchAsChild();
        assertTrue(stretchesFunction.isNoEmptyConstraint());
    }

    @Test
    public void stretchesFunctionCheckOneHundredPercent1() {
        givenStretchesFunction();
        assertTrue(stretchesFunction.isOneHundredPercentConstraint());
    }

    @Test
    public void stretchesFunctionCheckOneHundredPercent2() {
        givenStretchesFunction();
        givenStretchAsChild();
        assertTrue(stretchesFunction.isOneHundredPercentConstraint());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder1() {
        givenStretchesFunction();
        assertTrue(stretchesFunction.isStretchesOrderConstraint());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder2() {
        givenStretchesFunction();
        givenStretchAsChild();
        assertFalse(stretchesFunction.isStretchesOrderConstraint());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder3() {
        givenStretchesFunction();
        givenStretchAsChild();
        givenStretchAsChild();
        assertFalse(stretchesFunction.isStretchesOrderConstraint());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder4() {
        givenStretchesFunction();
        givenStretchAsChild();
        givenStretchAsChild(BigDecimal.ONE, BigDecimal.ONE);
        assertFalse(stretchesFunction.isStretchesOrderConstraint());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder5() {
        givenStretchesFunction();
        givenStretchAsChild();
        givenStretchAsChild(BigDecimal.ZERO, BigDecimal.ZERO);
        assertFalse(stretchesFunction.isStretchesOrderConstraint());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder7() {
        givenStretchesFunction();
        givenStretchAsChild();
        givenStretchAsChild(BigDecimal.ONE, BigDecimal.ONE);
        assertFalse(stretchesFunction.isStretchesOrderConstraint());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder6() {
        givenStretchesFunction();
        givenStretchAsChild();
        givenStretchAsChild(BigDecimal.ONE, BigDecimal.ONE);
        assertFalse(stretchesFunction.isStretchesOrderConstraint());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder8() {
        givenStretchesFunction();
        givenStretchAsChild(BigDecimal.ONE, BigDecimal.ONE);
        assertFalse(stretchesFunction.isStretchesOrderConstraint());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder9() {
        givenStretchesFunction();
        givenStretchAsChild(BigDecimal.ZERO, BigDecimal.ZERO);
        assertFalse(stretchesFunction.isStretchesOrderConstraint());
    }

    @Test
    public void ifNoStretchesNoIntervalDefinedByStretches() {
        givenStretchesFunction();
        assertThat(stretchesFunction.getIntervalsDefinedByStretches().size(), equalTo(2));
    }

    @Test
    public void theLastStretchMustHaveAllTheLoad() {
        givenStretchesFunction();
        givenStretchAsChild(new LocalDate().plusDays(1), BigDecimal.valueOf(0.6));
        List<Interval> intervals = stretchesFunction.getIntervalsDefinedByStretches();
        assertThat(intervals.size(), equalTo(3));
        assertThat(intervals.get(intervals.size() - 1).getLoadProportion(), equalTo(BigDecimal.valueOf(0.4).setScale(2)));
    }

    @Test
    public void oneStretchImpliesThreeInterval() {
        givenStretchesFunction();
        givenStretchAsChild(new LocalDate().plusDays(1), new BigDecimal(1));
        assertThat(stretchesFunction.getIntervalsDefinedByStretches().size(), equalTo(3));
    }

    @Test
    public void oneStretchImpliesOneIntervalUntilDateWithLoadSpecifiedByStretch() {
        givenStretchesFunction();
        LocalDate stretchDate = new LocalDate().plusDays(1);
        BigDecimal amountOfWorkProportion = BigDecimal.valueOf(0.5).setScale(2);
        givenStretchAsChild(stretchDate, amountOfWorkProportion);
        givenStretchAsChild(new LocalDate().plusDays(2), BigDecimal.valueOf(1.0));
        Interval firstInterval = stretchesFunction.getIntervalsDefinedByStretches().get(1);
        assertThat(firstInterval.getEnd(), equalTo(stretchDate));
        assertFalse(firstInterval.hasNoStart());
        assertThat(firstInterval.getLoadProportion(), equalTo(amountOfWorkProportion));
    }

    @Test
    public void theLastIntervalHasStart() {
        givenStretchesFunction();
        LocalDate stretchDate = new LocalDate().plusDays(1); 
        givenStretchAsChild(stretchDate, new BigDecimal(0.5));
        givenStretchAsChild(stretchDate.plusDays(2), new BigDecimal(1));
        Interval lastInterval = stretchesFunction.getIntervalsDefinedByStretches().get(2);
        assertThat(lastInterval.getStart(), equalTo(stretchDate));
    }

    @Test
    public void aIntervalInTheMiddleHasStart() {
        givenStretchesFunction();
        LocalDate start = new LocalDate().plusDays(1);
        givenStretchAsChild(start, new BigDecimal(0.5));
        LocalDate middleEnd = start.plusDays(2);
        givenStretchAsChild(middleEnd, new BigDecimal(0.6));
        givenStretchAsChild(middleEnd.plusDays(3), new BigDecimal(1));
        Interval middle = stretchesFunction.getIntervalsDefinedByStretches().get(2);
        assertFalse(middle.hasNoStart());
        assertThat(middle.getStart(), equalTo(start));
        assertThat(middle.getEnd(), equalTo(middleEnd));
    }

    @Test
    public void eachIntervalHasTheCorrespondingLoadForThatInterval() {
        givenStretchesFunction();
        LocalDate start = new LocalDate().plusDays(1);
        givenStretchAsChild(start, new BigDecimal(0.5));
        LocalDate middleEnd = start.plusDays(2);
        givenStretchAsChild(middleEnd, new BigDecimal(0.8));
        givenStretchAsChild(middleEnd.plusDays(3), new BigDecimal(1));
        Interval first = stretchesFunction.getIntervalsDefinedByStretches().get(1);
        Interval middle = stretchesFunction.getIntervalsDefinedByStretches().get(2);
        Interval last = stretchesFunction.getIntervalsDefinedByStretches().get(3);
        assertThat(first.getLoadProportion(), equalTo(new BigDecimal(0.5).setScale(2)));
        assertThat(middle.getLoadProportion(), equalTo(new BigDecimal(0.3).setScale(2, RoundingMode.HALF_EVEN)));
        assertThat(last.getLoadProportion(), equalTo(new BigDecimal(0.2).setScale(2, RoundingMode.HALF_EVEN)));
    }

    @Test
    public void ifTheIntervalStartIsNullReturnsThePassedStartDate() {
        LocalDate end = new LocalDate().plusMonths(1);
        Interval interval = new Interval(new BigDecimal(0.3), null, end);
        LocalDate now = new LocalDate();
        assertThat(interval.getStartFor(now), equalTo(now));
    }

    @Test(expected = NullPointerException.class)
    public void endDateCannotBeNull() {
        LocalDate start = new LocalDate().plusMonths(1);
        new Interval(new BigDecimal(0.3), start, null);
    }

    @Test
    public void ifTheIntervalStartIsNotNullReturnsItsStartDate() {
        LocalDate start = new LocalDate().plusMonths(1);
        Interval interval = new Interval(new BigDecimal(0.3), start, start.plusDays(20));
        assertThat(interval.getStartFor(new LocalDate()), equalTo(start));
    }

    @Test
    public void ifTheIntervalEndIsNotNullReturnsItsEndDate() {
        LocalDate start = new LocalDate().plusMonths(1);
        LocalDate end = new LocalDate().plusMonths(2);
        Interval interval = new Interval(new BigDecimal(0.3), start, end);
        assertThat(interval.getEnd(), equalTo(end));
    }

    @Test
    public void checkCalculatedDateForStretches() {
        givenStretchesFunction();
        givenStretchAsChild(BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.5));
        givenStretchAsChild(BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.8));

        List<Interval> intervals = stretchesFunction.getIntervalsDefinedByStretches();
        assertNull(intervals.get(0).getStart());
        assertThat(intervals.get(0).getEnd(), equalTo(START_DATE));
        assertThat(intervals.get(1).getStart(), equalTo(START_DATE));
        assertThat(intervals.get(1).getEnd(), equalTo(START_DATE.plusDays(2)));
        assertThat(intervals.get(2).getStart(), equalTo(START_DATE.plusDays(2)));
        assertThat(intervals.get(2).getEnd(), equalTo(START_DATE.plusDays(5)));
        assertThat(intervals.get(3).getStart(), equalTo(START_DATE.plusDays(5)));
        assertThat(intervals.get(3).getEnd(), equalTo(END_DATE));
    }

}
