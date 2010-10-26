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
package org.zkoss.ganttz.data.constraint;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.biggerOrEqualThan;

import java.util.Date;

import org.junit.Test;

public class ConstraintOnComparableValuesTest {

    private Date now = new Date();

    private Constraint<Date> biggerOrEqualThanNow = biggerOrEqualThan(now);

    private Constraint<Date> equalToNow = ConstraintOnComparableValues
            .<Date> equalTo(now);

    @Test
    public void canCreateConstraintBiggerThan() {
        assertThat(biggerOrEqualThanNow.applyTo(now), equalTo(new Date(now
                .getTime())));
    }

    @Test
    public void biggerOrEqualThanNullLeaveValuesUnmodified() {
        Constraint<Date> biggerThanNull = ConstraintOnComparableValues
                .<Date> biggerOrEqualThan(null);
        Date eraStart = new Date(0);
        assertThat(biggerThanNull.applyConstraintTo(new Date(0)),
                equalTo(eraStart));
    }

    @Test
    public void applyingBiggerOrEqualThanConstraintToNullNotFails() {
        assertThat(biggerOrEqualThanNow.applyTo(null), equalTo(new Date(now
                .getTime())));
    }

    @Test
    public void canCreateConstraintEqualTo(){
        assertThat(equalToNow.applyTo(new Date(now.getTime() + 1000)),
                equalTo(now));
    }

    @Test
    public void equalToNullLeaveValuesUnmodified() {
        Constraint<Date> equalToNull = ConstraintOnComparableValues
                .<Date> equalTo(null);
        Date value = new Date(now.getTime() + 1000);
        assertThat(equalToNull.applyTo(value), equalTo(value));
    }

}
