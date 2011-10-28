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

package org.libreplan.web.planner.chart;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.zkforge.timeplot.Timeplot;
import org.zkoss.ganttz.util.Interval;

/**
 * Tests for {@link ChartFiller}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ChartFillerTest {

    private ChartFiller chartFiller = new ChartFiller() {

        @Override
        public void fillChart(Timeplot chart, Interval interval, Integer size) {
        }

    };

    private static final LocalDate START_DAY = new LocalDate(2009, 12, 1);
    private static final LocalDate FIRST_DAY = new LocalDate(2009, 12, 5);
    private static final LocalDate LAST_DAY = new LocalDate(2009, 12, 15);
    private static final LocalDate FINISH_DAY = new LocalDate(2009, 12, 30);

    private SortedMap<LocalDate, BigDecimal> givenExampleMap() {
        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();

        result.put(FIRST_DAY, new BigDecimal(100));
        result.put(LAST_DAY, new BigDecimal(150));

        return result;
    }

    @Test
    public void testCalculatedValueForEveryDay() {
        SortedMap<LocalDate, BigDecimal> result = chartFiller
                .calculatedValueForEveryDay(givenExampleMap(), START_DAY,
                        FINISH_DAY);

        assertThat(result.get(START_DAY), equalTo(BigDecimal.ZERO.setScale(2)));
        assertThat(result.get(START_DAY.plusDays(1)),
                equalTo(new BigDecimal(25).setScale(2)));

        assertThat(result.get(FIRST_DAY), equalTo(new BigDecimal(100)
                .setScale(2)));
        assertThat(result.get(FIRST_DAY.plusDays(1)), equalTo(new BigDecimal(
                105).setScale(2)));

        assertThat(result.get(LAST_DAY), equalTo(new BigDecimal(150)
                .setScale(2)));
        assertThat(result.get(LAST_DAY.plusDays(1)),
                equalTo(new BigDecimal(150).setScale(2)));

        assertThat(result.get(FINISH_DAY), equalTo(new BigDecimal(150)
                .setScale(2)));
    }

}
