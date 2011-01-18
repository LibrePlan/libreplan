/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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
package org.navalplanner.business.test.calendars.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.workingday.EffortDuration.hours;

import org.junit.Test;
import org.navalplanner.business.calendars.entities.Capacity;
import org.navalplanner.business.workingday.EffortDuration;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CapacityTest {

    @Test
    public void itHasStandardEffortAndAllowedExtraEffort() {
        Capacity capacity = Capacity.create(hours(8)).extraEffort(hours(2));
        assertThat(capacity.getStandardEffort(),
                equalTo(EffortDuration.hours(8)));
        assertThat(capacity.getAllowedExtraEffort(), equalTo(hours(2)));
    }

    @Test
    public void ifOnlyTheStandardCapacitySpecifiedIsOverAssignableWithoutLimit() {
        Capacity capacity = Capacity.create(hours(8));
        assertTrue(capacity.isOverAssignableWithoutLimit());
    }

    @Test
    public void ifOnlyTheStandardCapacitySpecifiedTheExtraHoursAreNull() {
        Capacity capacity = Capacity.create(hours(8));
        assertThat(capacity.getAllowedExtraEffort(), nullValue());
    }

    @Test
    public void ifHasAllowedExtraEffortItsNotOverassignableWithoutLimit() {
        Capacity capacity = Capacity.create(hours(8)).extraEffort(hours(0));
        assertFalse(capacity.isOverAssignableWithoutLimit());
    }

    @Test
    public void hasAnEqualsAndHashCodeBasedOnStandardEffortAndExtraHours() {
        Capacity a1 = Capacity.create(hours(8)).extraEffort(hours(2));
        Capacity a2 = Capacity.create(hours(8)).extraEffort(hours(2));

        Capacity b1 = Capacity.create(hours(8));

        assertThat(a1, equalTo(a2));
        assertThat(a1.hashCode(), equalTo(a2.hashCode()));
        assertThat(a1, not(equalTo(b1)));
    }

    @Test
    public void aZeroCapacityIsNotOverAssignable() {
        assertFalse(Capacity.zero().isOverAssignableWithoutLimit());
    }

    @Test
    public void albeitTheCapacityIsCreatedFromHibernateAndTheFieldsAreNullDontReturnANullExtraEffort() {
        Capacity capacity = new Capacity();
        assertThat(capacity.getStandardEffort(), equalTo(EffortDuration.zero()));
    }
}
