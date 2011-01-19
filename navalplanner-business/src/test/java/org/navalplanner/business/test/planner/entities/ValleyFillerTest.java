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
package org.navalplanner.business.test.planner.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.navalplanner.business.planner.entities.ValleyFiller;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class ValleyFillerTest {

    @Test
    public void ifIsMonotonicIncreasingDoesntDoAnything() {
        int[] param = { 2, 4, 8 };
        int[] result = ValleyFiller.fillValley(param);
        assertThat(result, equalTo(param));
    }

    @Test
    public void ifHasValleyItFillsIt() {
        int[] result = ValleyFiller.fillValley(2, 4, 8, 5, 6, 10);
        assertThat(result, equalTo(new int[] { 2, 4, 8, 8, 8, 10 }));
    }

    @Test
    public void ifEndsInValleyEndsAtLastValue() {
        int[] result = ValleyFiller.fillValley(2, 4, 8, 5, 6);
        assertThat(result, equalTo(new int[] { 2, 4, 6, 6, 6 }));
    }

    @Test
    public void ifHasSeveralValleysWorksOk() {
        int[] result = ValleyFiller.fillValley(2, 4, 8, 5, 6, 7, 9, 8, 11);
        int[] expectedResult = { 2, 4, 8, 8, 8, 8, 9, 9, 11 };
        assertThat(result, equalTo(expectedResult));
    }

}
