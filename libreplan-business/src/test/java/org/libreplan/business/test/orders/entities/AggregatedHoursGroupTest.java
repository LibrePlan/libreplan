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
package org.libreplan.business.test.orders.entities;

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
import org.libreplan.business.orders.entities.AggregatedHoursGroup;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.requirements.entities.CriterionRequirement;
import org.libreplan.business.requirements.entities.DirectCriterionRequirement;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.ResourceEnum;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class AggregatedHoursGroupTest {

    private Criterion criterion1;
    private Criterion criterion2;

    @Before
    public void setUpCriterions() {
        criterion1 = createNiceMock(Criterion.class);
        criterion2 = createNiceMock(Criterion.class);
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

    @Test
    public void getHoursReturnTheSumOfAllHours() {
        HoursGroup h1 = createHoursGroupWithCriterions(criterion1, criterion2);
        h1.setWorkingHours(10);
        HoursGroup h2 = createHoursGroupWithCriterions(criterion1, criterion2);
        h2.setWorkingHours(5);
        AggregatedHoursGroup aggregate = AggregatedHoursGroup.aggregate(h1, h2)
                .get(0);
        assertThat(aggregate.getHours(), equalTo(15));
    }

    @Test
    public void sumAllAggregatedHours() {
        HoursGroup h1 = createHoursGroupWithCriterions(criterion1, criterion2);
        h1.setWorkingHours(10);
        HoursGroup h2 = createHoursGroupWithCriterions(criterion1);
        h2.setWorkingHours(5);
        List<AggregatedHoursGroup> list = AggregatedHoursGroup.aggregate(h1, h2);
        assertThat(AggregatedHoursGroup.sum(list), equalTo(15));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void theResourceTypeIsTakingIntoAccountWhenGrouping() {
        HoursGroup h1 = createHoursGroupWithCriterions(ResourceEnum.MACHINE, criterion1,
                criterion2);
        h1.setWorkingHours(10);
        HoursGroup h2 = createHoursGroupWithCriterions(ResourceEnum.WORKER,
                criterion1, criterion2);
        h1.setWorkingHours(5);
        List<AggregatedHoursGroup> aggregate = AggregatedHoursGroup.aggregate(h1,h2);
        assertThat(aggregate.size(), equalTo(2));
        assertThat(aggregate, hasItem(allOf(withCriterions(criterion1,
                criterion2), withHours(h1))));
        assertThat(aggregate, hasItem(allOf(withCriterions(criterion1,
                criterion2), withHours(h2))));
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
        return createHoursGroupWithCriterions(ResourceEnum.WORKER, criterions);
    }

    private HoursGroup createHoursGroupWithCriterions(
            ResourceEnum resourceType, Criterion... criterions) {
        HoursGroup result = new HoursGroup();
        result.setCriterionRequirements(asCriterionRequirements(criterions));
        result.setResourceType(resourceType);
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
