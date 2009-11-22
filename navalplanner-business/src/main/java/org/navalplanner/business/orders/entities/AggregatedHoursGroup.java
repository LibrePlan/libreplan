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

import org.navalplanner.business.resources.entities.Criterion;

/**
 * A collection of HoursGroup with the same name required <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class AggregatedHoursGroup {

    public static List<AggregatedHoursGroup> aggregate(HoursGroup... hours) {
        return aggregate(Arrays.asList(hours));
    }

    public static List<AggregatedHoursGroup> aggregate(
            Collection<? extends HoursGroup> hours) {
        List<AggregatedHoursGroup> result = new ArrayList<AggregatedHoursGroup>();
        for (Entry<Set<Criterion>, List<HoursGroup>> entry : byCriterions(hours)
                .entrySet()) {
            result.add(new AggregatedHoursGroup(entry.getKey(), entry
                    .getValue()));
        }
        return result;
    }

    private static Map<Set<Criterion>, List<HoursGroup>> byCriterions(
            Collection<? extends HoursGroup> hours) {
        Map<Set<Criterion>, List<HoursGroup>> result = new HashMap<Set<Criterion>, List<HoursGroup>>();
        for (HoursGroup each : hours) {
            if (!result.containsKey(each.getValidCriterions())) {
                result.put(each.getValidCriterions(),
                        new ArrayList<HoursGroup>());
            }
            result.get(each.getValidCriterions()).add(each);
        }
        return result;
    }

    private final Set<Criterion> criterions;

    private final List<HoursGroup> hoursGroup;

    private AggregatedHoursGroup(Set<Criterion> criterions,
            List<HoursGroup> hours) {
        this.criterions = Collections.unmodifiableSet(criterions);
        this.hoursGroup = Collections.unmodifiableList(hours);
    }

    public Set<Criterion> getCriterions() {
        return criterions;
    }

    public List<HoursGroup> getHoursGroup() {
        return hoursGroup;
    }

}
