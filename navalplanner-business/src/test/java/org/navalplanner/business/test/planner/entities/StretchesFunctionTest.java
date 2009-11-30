/*
 * This file is part of ###PROJECT_NAME###
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

package org.navalplanner.business.test.planner.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.planner.entities.Stretch;
import org.navalplanner.business.planner.entities.StretchesFunction;
import org.navalplanner.business.planner.entities.StretchesFunction.Interval;

/**
 * Tests for {@link StretchesFunction} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class StretchesFunctionTest {
    private StretchesFunction stretchesFunction;

    private StretchesFunction givenStretchesFunction() {
        stretchesFunction = StretchesFunction.create();
        return stretchesFunction;
    }

    private Stretch givenStretchAsChild() {
        Stretch result = new Stretch();
        stretchesFunction.addStretch(result);
        return result;
    }

    private Stretch givenStretchAsChild(LocalDate date,
            BigDecimal lengthPercentage,
            BigDecimal amountWorkPercentage) {
        Stretch stretch = givenStretchAsChild();
        stretch.setDate(date);
        stretch.setLengthPercentage(lengthPercentage);
        stretch.setAmountWorkPercentage(amountWorkPercentage);
        return stretch;
    }

    private Stretch givenStretchAsChild(LocalDate date,
            BigDecimal amountWorkPercentage) {
        return givenStretchAsChild(date, BigDecimal.ZERO, amountWorkPercentage);
    }

    @Test
    public void stretchesFunctionCheckNoEmpty1() {
        givenStretchesFunction();
        assertFalse(stretchesFunction.checkNoEmpty());
    }

    @Test
    public void stretchesFunctionCheckNoEmpty2() {
        givenStretchesFunction();
        givenStretchAsChild();
        assertTrue(stretchesFunction.checkNoEmpty());
    }

    @Test
    public void stretchesFunctionCheckOneHundredPercent1() {
        givenStretchesFunction();
        assertFalse(stretchesFunction.checkOneHundredPercent());
    }

    @Test
    public void stretchesFunctionCheckOneHundredPercent2() {
        givenStretchesFunction();
        givenStretchAsChild();
        assertFalse(stretchesFunction.checkOneHundredPercent());
    }

    @Test
    public void stretchesFunctionCheckOneHundredPercent3() {
        givenStretchesFunction();
        givenStretchAsChild(new LocalDate(), BigDecimal.ONE, BigDecimal.ZERO);
        assertFalse(stretchesFunction.checkOneHundredPercent());
    }

    @Test
    public void stretchesFunctionCheckOneHundredPercent4() {
        givenStretchesFunction();
        givenStretchAsChild(new LocalDate(), BigDecimal.ZERO, BigDecimal.ONE);
        assertFalse(stretchesFunction.checkOneHundredPercent());
    }

    @Test
    public void stretchesFunctionCheckOneHundredPercent5() {
        givenStretchesFunction();
        givenStretchAsChild(new LocalDate(), BigDecimal.ONE, BigDecimal.ONE);
        assertTrue(stretchesFunction.checkOneHundredPercent());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder1() {
        givenStretchesFunction();
        assertFalse(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder2() {
        givenStretchesFunction();
        givenStretchAsChild();
        assertTrue(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder3() {
        givenStretchesFunction();
        givenStretchAsChild();
        givenStretchAsChild();
        assertFalse(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder4() {
        givenStretchesFunction();
        givenStretchAsChild();
        givenStretchAsChild(new LocalDate(), BigDecimal.ONE, BigDecimal.ONE);
        assertFalse(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder5() {
        givenStretchesFunction();
        givenStretchAsChild();
        givenStretchAsChild(new LocalDate().plusMonths(1), BigDecimal.ZERO,
                BigDecimal.ZERO);
        assertFalse(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder7() {
        givenStretchesFunction();
        givenStretchAsChild();
        givenStretchAsChild(new LocalDate().minusMonths(1), BigDecimal.ONE,
                BigDecimal.ONE);
        assertFalse(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder6() {
        givenStretchesFunction();
        givenStretchAsChild();
        givenStretchAsChild(new LocalDate().plusMonths(1), BigDecimal.ONE,
                BigDecimal.ONE);
        assertTrue(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void ifNoStrechesNoIntervalDefinedByStreches() {
        givenStretchesFunction();
        assertTrue(stretchesFunction.getIntervalsDefinedByStreches().isEmpty());
    }

    @Test
    public void oneStrechImpliesTwoIntervals() {
        givenStretchesFunction();
        givenStretchAsChild(new LocalDate().plusMonths(1), new BigDecimal(0.5));
        assertThat(stretchesFunction.getIntervalsDefinedByStreches().size(),
                equalTo(2));
    }

    @Test
    public void oneStrechImpliesOneIntervalUntilDateWithLoadSpecifiedByStrech() {
        givenStretchesFunction();
        LocalDate strechDate = new LocalDate().plusMonths(1);
        BigDecimal amountOfWorkProportion = new BigDecimal(0.5).setScale(2);
        givenStretchAsChild(strechDate, amountOfWorkProportion);
        Interval firstInterval = stretchesFunction
                .getIntervalsDefinedByStreches().get(0);
        assertThat(firstInterval.getEnd(), equalTo(strechDate));
        assertTrue(firstInterval.hasNoStart());
        assertThat(firstInterval.getLoadProportion(),
                equalTo(amountOfWorkProportion));
    }

    @Test
    public void theLastIntervalHasStart() {
        givenStretchesFunction();
        LocalDate strechDate = new LocalDate().plusMonths(1);
        givenStretchAsChild(strechDate, new BigDecimal(0.5));
        Interval lastInterval = stretchesFunction
                .getIntervalsDefinedByStreches().get(1);
        assertThat(lastInterval.getStart(), equalTo(strechDate));
    }

    @Test
    public void aIntervalInTheMiddleHasStartAndEnd() {
        givenStretchesFunction();
        LocalDate start = new LocalDate().plusMonths(1);
        givenStretchAsChild(start, new BigDecimal(0.5));
        LocalDate middleEnd = start.plusMonths(2);
        givenStretchAsChild(middleEnd, new BigDecimal(0.6));
        Interval middle = stretchesFunction.getIntervalsDefinedByStreches().get(
                1);
        assertFalse(middle.hasNoStart());
        assertFalse(middle.hasNoEnd());
        assertThat(middle.getStart(), equalTo(start));
        assertThat(middle.getEnd(), equalTo(middleEnd));
    }

    @Test
    public void eachIntervalAccumulatesAllTheLoads() {
        givenStretchesFunction();
        LocalDate start = new LocalDate().plusMonths(1);
        givenStretchAsChild(start, new BigDecimal(0.5));
        LocalDate middleEnd = start.plusMonths(2);
        givenStretchAsChild(middleEnd, new BigDecimal(0.8));
        Interval first = stretchesFunction.getIntervalsDefinedByStreches().get(
                0);
        Interval middle = stretchesFunction.getIntervalsDefinedByStreches()
                .get(1);
        Interval last = stretchesFunction.getIntervalsDefinedByStreches()
                .get(2);
        assertThat(first.getLoadProportion(), equalTo(new BigDecimal(0.5)
                .setScale(2)));
        assertThat(middle.getLoadProportion(), equalTo(new BigDecimal(0.3)
                .setScale(2, RoundingMode.HALF_EVEN)));
        assertThat(last.getLoadProportion(), equalTo(new BigDecimal(0.2)
                .setScale(2, RoundingMode.HALF_EVEN)));
    }

    @Test
    public void theLastIntervalHasTheRemainingLoad() {
        givenStretchesFunction();
        LocalDate start = new LocalDate().plusMonths(1);
        givenStretchAsChild(start, new BigDecimal(0.3));
        givenStretchAsChild(start.plusMonths(2), new BigDecimal(0.5));
        givenStretchAsChild(start.plusMonths(3), new BigDecimal(0.7));
        Interval lastInterval = stretchesFunction
                .getIntervalsDefinedByStreches().get(3);
        BigDecimal expectedRemaining = new BigDecimal(0.3).setScale(2,
                RoundingMode.HALF_UP);
        assertThat(lastInterval.getLoadProportion(), equalTo(expectedRemaining));
    }

    @Test
    public void ifTheIntervalStartIsNullReturnsThePassedStartDate() {
        LocalDate end = new LocalDate().plusMonths(1);
        Interval interval = new Interval(new BigDecimal(0.3), null, end);
        LocalDate now = new LocalDate();
        assertThat(interval.getStartFor(now), equalTo(now));
    }

    @Test
    public void ifTheIntervalEndIsNullReturnsThePassedStartDate() {
        LocalDate start = new LocalDate().plusMonths(1);
        Interval interval = new Interval(new BigDecimal(0.3), start, null);
        LocalDate now = new LocalDate();
        assertThat(interval.getEndFor(now), equalTo(now));
    }

    @Test
    public void ifTheIntervalStartIsNotNullReturnsItsStartDate() {
        LocalDate start = new LocalDate().plusMonths(1);
        Interval interval = new Interval(new BigDecimal(0.3), start, null);
        assertThat(interval.getStartFor(new LocalDate()), equalTo(start));
    }

    @Test
    public void ifTheIntervalEndIsNotNullReturnsItsEndDate() {
        LocalDate start = new LocalDate().plusMonths(1);
        LocalDate end = new LocalDate().plusMonths(2);
        Interval interval = new Interval(new BigDecimal(0.3), start, end);
        assertThat(interval.getEndFor(new LocalDate()), equalTo(end));
    }

}
