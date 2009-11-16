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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.planner.entities.Stretch;
import org.navalplanner.business.planner.entities.StretchesFunction;

/**
 * Tests for {@link StretchesFunction} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class StretchesFunctionTest {

    private StretchesFunction givenStretchesFunction() {
        StretchesFunction stretchesFunction = StretchesFunction.create();
        return stretchesFunction;
    }

    private Stretch givenStretch() {
        return new Stretch();
    }

    private Stretch givenStretch(LocalDate date, BigDecimal lengthPercentage,
            BigDecimal amountWorkPercentage) {
        Stretch stretch = givenStretch();
        stretch.setDate(date);
        stretch.setLengthPercentage(lengthPercentage);
        stretch.setAmountWorkPercentage(amountWorkPercentage);
        return stretch;
    }

    @Test
    public void stretchesFunctionCheckNoEmpty1() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        assertFalse(stretchesFunction.checkNoEmpty());
    }

    @Test
    public void stretchesFunctionCheckNoEmpty2() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        stretchesFunction.addStretch(givenStretch());
        assertTrue(stretchesFunction.checkNoEmpty());
    }

    @Test
    public void stretchesFunctionCheckOneHundredPercent1() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        assertFalse(stretchesFunction.checkOneHundredPercent());
    }

    @Test
    public void stretchesFunctionCheckOneHundredPercent2() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        stretchesFunction.addStretch(givenStretch());
        assertFalse(stretchesFunction.checkOneHundredPercent());
    }

    @Test
    public void stretchesFunctionCheckOneHundredPercent3() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        stretchesFunction.addStretch(givenStretch(new LocalDate(),
                BigDecimal.ONE, BigDecimal.ZERO));
        assertFalse(stretchesFunction.checkOneHundredPercent());
    }

    @Test
    public void stretchesFunctionCheckOneHundredPercent4() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        stretchesFunction.addStretch(givenStretch(new LocalDate(),
                BigDecimal.ZERO, BigDecimal.ONE));
        assertFalse(stretchesFunction.checkOneHundredPercent());
    }

    @Test
    public void stretchesFunctionCheckOneHundredPercent5() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        stretchesFunction.addStretch(givenStretch(new LocalDate(),
                BigDecimal.ONE, BigDecimal.ONE));
        assertTrue(stretchesFunction.checkOneHundredPercent());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder1() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        assertFalse(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder2() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        stretchesFunction.addStretch(givenStretch());
        assertTrue(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder3() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        stretchesFunction.addStretch(givenStretch());
        stretchesFunction.addStretch(givenStretch());
        assertFalse(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder4() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        stretchesFunction.addStretch(givenStretch());
        stretchesFunction.addStretch(givenStretch(new LocalDate(),
                BigDecimal.ONE, BigDecimal.ONE));
        assertFalse(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder5() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        stretchesFunction.addStretch(givenStretch());
        stretchesFunction
                .addStretch(givenStretch(new LocalDate().plusMonths(1),
                        BigDecimal.ZERO, BigDecimal.ZERO));
        assertFalse(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder7() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        stretchesFunction.addStretch(givenStretch());
        stretchesFunction.addStretch(givenStretch(new LocalDate()
                .minusMonths(1), BigDecimal.ONE, BigDecimal.ONE));
        assertFalse(stretchesFunction.checkStretchesOrder());
    }

    @Test
    public void stretchesFunctionCheckStretchesOrder6() {
        StretchesFunction stretchesFunction = givenStretchesFunction();
        stretchesFunction.addStretch(givenStretch());
        stretchesFunction.addStretch(givenStretch(
                new LocalDate().plusMonths(1), BigDecimal.ONE, BigDecimal.ONE));
        assertTrue(stretchesFunction.checkStretchesOrder());
    }

}
