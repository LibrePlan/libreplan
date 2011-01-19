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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.navalplanner.business.planner.entities.Share;

public class ShareTest {

    private Share share;

    private void givenHours(int hours) {
        share = new Share(hours);
    }

    @Test
    public void canHaveNegativeHours() {
        new Share(-1);
    }

    @Test
    public void canHaveZeroHours() {
        new Share(0);
    }

    @Test
    public void hasPropertyToRetrieveHours() {
        givenHours(4);
        assertThat(share.getHours(), equalTo(4));
    }

    @Test
    public void toStringContainsHours(){
        givenHours(133);
        assertTrue(share.toString().contains("133"));
    }

    @Test
    public void canAddHours() {
        givenHours(10);
        Share newShare = share.plus(5);
        assertThat(newShare.getHours(), equalTo(15));
    }

    @Test
    public void isImmutable(){
        givenHours(4);
        share.plus(3);
        assertThat(share.getHours(), equalTo(4));
    }

    @Test
    public void canSubstract() {
        givenHours(5);
        Share newShare = share.plus(-2);
        assertThat(newShare.getHours(), equalTo(3));
    }

    @Test
    public void canAddToANegativeNuber() {
        givenHours(-2);
        share.plus(1);
    }

    @Test
    public void twoSharesEqualIfHaveTheSameHours() {
        givenHours(10);
        assertThat(share, equalTo(new Share(10)));
        assertThat(share.hashCode(), equalTo(new Share(10).hashCode()));
    }

}
