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
package org.navalplanner.business.orders.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ResourceEnum;

/**
 * A collection of HoursGroup with the same name required <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class AggregatedHoursGroup {

    private static class GroupingCriteria {

        private static Map<GroupingCriteria, List<HoursGroup>> byCriterions(
                Collection<? extends HoursGroup> hours) {
            Map<GroupingCriteria, List<HoursGroup>> result = new HashMap<GroupingCriteria, List<HoursGroup>>();
            for (HoursGroup each : hours) {
                GroupingCriteria key = asGroupingCriteria(each);
                if (!result.containsKey(key)) {
                    result.put(key,
                            new ArrayList<HoursGroup>());
                }
                result.get(key).add(each);
            }
            return result;
        }

        private static GroupingCriteria asGroupingCriteria(HoursGroup hoursGroup) {
            return new GroupingCriteria(hoursGroup.getResourceType(),
                    hoursGroup.getValidCriterions());
        }

        private final ResourceEnum type;

        private final Set<Criterion> criterions;

        private GroupingCriteria(ResourceEnum type, Set<Criterion> criterions) {
            this.type = type;
            this.criterions = criterions;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(type).append(criterions)
                    .toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof GroupingCriteria) {
                GroupingCriteria other = (GroupingCriteria) obj;
                return new EqualsBuilder().append(type, other.type).append(
                        criterions,
                        other.criterions).isEquals();
            }
            return false;
        }
    }

    public static List<AggregatedHoursGroup> aggregate(HoursGroup... hours) {
        return aggregate(Arrays.asList(hours));
    }

    public static List<AggregatedHoursGroup> aggregate(
            Collection<? extends HoursGroup> hours) {
        List<AggregatedHoursGroup> result = new ArrayList<AggregatedHoursGroup>();
        for (Entry<GroupingCriteria, List<HoursGroup>> entry : GroupingCriteria
                .byCriterions(hours)
                .entrySet()) {
            result.add(new AggregatedHoursGroup(entry.getKey(), entry
                    .getValue()));
        }
        return result;
    }

    public static int sum(Collection<? extends AggregatedHoursGroup> aggregated) {
        int result = 0;
        for (AggregatedHoursGroup each : aggregated) {
            result += each.getHours();
        }
        return result;
    }

    private final Set<Criterion> criterions;

    private final List<HoursGroup> hoursGroup;

    private ResourceEnum resourceType;


    private AggregatedHoursGroup(GroupingCriteria groupingCriteria,
            List<HoursGroup> hours) {
        this.criterions = Collections
                .unmodifiableSet(groupingCriteria.criterions);
        this.hoursGroup = Collections.unmodifiableList(hours);
        this.resourceType = groupingCriteria.type;
    }

    public Set<Criterion> getCriterions() {
        return criterions;
    }

    public List<HoursGroup> getHoursGroup() {
        return hoursGroup;
    }

    public ResourceEnum getResourceType() {
        return resourceType;
    }

    public int getHours() {
        int result = 0;
        for (HoursGroup each : hoursGroup) {
            result += each.getWorkingHours();
        }
        return result;
    }

    public String getCriterionsJoinedByComma() {
        List<String> criterionNames = asNames(criterions);
        Collections.sort(criterionNames);
        return StringUtils.join(criterionNames, ", ");
    }

    private List<String> asNames(Set<Criterion> criterions) {
        List<String> result = new ArrayList<String>();
        for (Criterion each : criterions) {
            result.add(each.getName());
        }
        return result;
    }

}
