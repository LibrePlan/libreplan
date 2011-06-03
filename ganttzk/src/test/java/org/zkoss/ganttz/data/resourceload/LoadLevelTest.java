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

import org.junit.Test;
import org.zkoss.ganttz.data.resourceload.LoadLevel.Category;

public class LoadLevelTest {

    private LoadLevel loadLevel;

    @Test
    public void thePercengateCanBeZero() {
        givenLoadLevelWithPercentage(0);
        assertThat(loadLevel.getPercentage(), equalTo(0));
    }

    private void givenLoadLevelWithPercentage(int percentage) {
        this.loadLevel = new LoadLevel(percentage);
    }

    @Test
    public void thePercentageCanBeGreaterThan100() {
        givenLoadLevelWithPercentage(101);
        assertThat(loadLevel.getPercentage(), equalTo(101));
    }

    @Test(expected = IllegalArgumentException.class)
    public void thePercentageCannotBeNegative() {
        givenLoadLevelWithPercentage(-1);
    }

    @Test
    public void categoryForZero() {
        givenLoadLevelWithPercentage(0);
        thenTheCategoryIs(Category.NO_LOAD);
    }

    private void thenTheCategoryIs(Category category) {
        assertThat(loadLevel.getCategory(), sameInstance(category));
    }

    @Test
    public void categoryForLessThan100AndMoreThanZero() {
        givenLoadLevelWithPercentage(60);
        thenTheCategoryIs(Category.SOME_LOAD);
    }

    @Test
    public void categoryFor100() {
        givenLoadLevelWithPercentage(100);
        thenTheCategoryIs(Category.FULL_LOAD);
    }

    @Test
    public void categoryForMoreThan100() {
        givenLoadLevelWithPercentage(101);
        thenTheCategoryIs(Category.OVERLOAD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void theCategoryThrowsExceptionIfCantHandleThePercentage() {
        Category.categoryFor(-1);
    }

}
