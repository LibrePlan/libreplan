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
package org.navalplanner.business.test.orders.entities;

import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.navalplanner.business.orders.entities.AggregatedHoursGroup;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class AggregatedHoursGroupTest {

    private Criterion criterion1;
    private Criterion criterion2;
    private Criterion criterion3;

    @Before
    public void setUpCriterions() {
        criterion1 = createNiceMock(Criterion.class);
        criterion2 = createNiceMock(Criterion.class);
        criterion3 = createNiceMock(Criterion.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void theRightAggregatedHoursGroupsAreCreated() {
        HoursGroup h1 = createHoursGroupWithCriterions(criterion1, criterion2);
        HoursGroup h2 = createHoursGroupWithCriterions(criterion1);
        HoursGroup h3 = createHoursGroupWithCriterions(criterion2, criterion1);
        HoursGroup h4 = createHoursGroupWithCriterions(criterion2);
        HoursGroup h5 = createHoursGroupWithCriterions(criterion2);
        List<AggregatedHoursGroup> aggregates = AggregatedHoursGroup.aggregate(
                h1, h2, h3, h4, h5);
        assertThat(aggregates.size(), equalTo(3));
        assertThat(aggregates, hasItem(allOf(withCriterions(criterion1,
                criterion2), withHours(h1, h3))));
        assertThat(aggregates, hasItem(allOf(withCriterions(criterion1),
                withHours(h2))));
        assertThat(aggregates, hasItem(allOf(withCriterions(criterion2),
                withHours(h4, h5))));
    }

    private static abstract class AggregatedHoursGroupMatcher extends
            BaseMatcher<AggregatedHoursGroup> {

        @Override
        public boolean matches(Object object) {
            if (object instanceof AggregatedHoursGroup) {
                return matches((AggregatedHoursGroup) object);
            }
            return false;
        }

        public abstract boolean matches(AggregatedHoursGroup aggregated);


    }

    private Matcher<AggregatedHoursGroup> withCriterions(
            Criterion... criterions) {
        final HashSet<Criterion> criterionsSet = new HashSet<Criterion>(Arrays
                .asList(criterions));
        return new AggregatedHoursGroupMatcher() {

            @Override
            public void describeTo(Description description) {
                description.appendText("having " + criterionsSet);
            }

            @Override
            public boolean matches(AggregatedHoursGroup aggregated) {
                return aggregated.getCriterions().equals(criterionsSet);
            }
        };
    }

    private Matcher<AggregatedHoursGroup> withHours(
            HoursGroup... hours) {
        final HashSet<HoursGroup> hoursSet = new HashSet<HoursGroup>(
                Arrays.asList(hours));
        return new AggregatedHoursGroupMatcher() {

            @Override
            public void describeTo(Description description) {
                description.appendText("having " + hoursSet);
            }

            @Override
            public boolean matches(AggregatedHoursGroup aggregated) {
                return new HashSet<HoursGroup>(aggregated.getHoursGroup())
                        .equals(hoursSet);
            }
        };
    }

    private HoursGroup createHoursGroupWithCriterions(Criterion... criterions) {
        HoursGroup result = new HoursGroup();
        result
                .setCriterionRequirements(asCriterionRequirements(criterions));
        return result;
    }

    private Set<CriterionRequirement> asCriterionRequirements(
            Criterion... criterions) {
        Set<CriterionRequirement> result = new HashSet<CriterionRequirement>();
        for (Criterion each : criterions) {
            result.add(new DirectCriterionRequirement(each));
        }
        return result;
    }

}
