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
package org.navalplanner.business.planner.entities.allocationalgorithms;

import static org.navalplanner.business.workingday.EffortDuration.seconds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.calendars.entities.Capacity;
import org.navalplanner.business.planner.entities.Share;
import org.navalplanner.business.planner.entities.ShareDivision;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.EffortDuration.IEffortFrom;

/**
 * Distributes an EffortDuration among several capacities. It respects the extra
 * hours requirements of the {@link Capacity capacities} and distributes the
 * effort evenly.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Distributor {

    public static Distributor among(Capacity... capacities) {
        return among(Arrays.asList(capacities));
    }

    public static Distributor among(Collection<? extends Capacity> capacities) {
        Validate.noNullElements(capacities);

        return new Distributor(capacities.toArray(new Capacity[0]));
    }

    private final Capacity[] capacities;

    private final List<Share> normalCapacityShares;

    private final List<Share> limitedOverloadShares;

    private final List<Share> unlimitedOverloadShares;

    private final List<List<Share>> phases = new ArrayList<List<Share>>();

    private Distributor(Capacity[] capacities) {
        Validate.notNull(capacities);
        this.capacities = capacities;
        this.normalCapacityShares = createNormalCapacityShares(capacities);
        this.limitedOverloadShares = createOverloadShares(capacities);
        this.unlimitedOverloadShares = createUnlimitedShares(capacities);
        this.phases.add(normalCapacityShares);
        this.phases.add(limitedOverloadShares);
        this.phases.add(this.unlimitedOverloadShares);
    }

    private static List<Share> createNormalCapacityShares(Capacity[] capacities) {
        List<Share> result = new ArrayList<Share>();
        for (Capacity each : capacities) {
            result.add(createNormalCapacityShare(each));
        }
        return result;
    }

    private List<Share> createOverloadShares(Capacity[] capacities) {
        List<Share> result = new ArrayList<Share>();
        EffortDuration maxExtraEffort = getMaxExtraEffort(capacities);
        for (Capacity each : capacities) {
            result.add(maxExtraEffort == null ? noSpaceAvailable()
                    : createOverloadShare(each, maxExtraEffort));
        }
        return result;
    }

    private EffortDuration getMaxExtraEffort(Capacity[] capacities) {
        if (capacities.length == 0) {
            return null;
        }
        Capacity max = Collections.max(Arrays.asList(capacities), new Comparator<Capacity>(){

            @Override
            public int compare(Capacity o1, Capacity o2) {
                if (o1.getAllowedExtraEffort() == o2.getAllowedExtraEffort()) {
                    return 0;
                } else if (o1.getAllowedExtraEffort() == null) {
                    return -1;
                } else if (o2.getAllowedExtraEffort() == null) {
                    return 1;
                }
                return o1.getAllowedExtraEffort().compareTo(
                        o2.getAllowedExtraEffort());
            }
        });
        return max.getAllowedExtraEffort();

    }

    private static Share createNormalCapacityShare(Capacity each) {
        return new Share(-each.getStandardEffort().getSeconds());
    }

    private Share createOverloadShare(Capacity each,
            EffortDuration maxExtraEffort) {
        if (each.getAllowedExtraEffort() == null && !each.isOverAssignableWithoutLimit()) {
            return noSpaceAvailable();
        }
        EffortDuration effort = each.getAllowedExtraEffort() != null ? each
                .getAllowedExtraEffort() : maxExtraEffort;
        return new Share(-effort.getSeconds());
    }

    private Share noSpaceAvailable() {
        return new Share(Integer.MAX_VALUE);
    }

    private List<Share> createUnlimitedShares(Capacity[] capacities) {
        List<Share> result = new ArrayList<Share>();
        for (Capacity each : capacities) {
            result.add(each.isOverAssignableWithoutLimit() ? new Share(0)
                    : noSpaceAvailable());
        }
        return result;
    }

    public List<EffortDuration> distribute(EffortDuration effort) {
        EffortDuration[] result = new EffortDuration[capacities.length];
        Arrays.fill(result, EffortDuration.zero());

        for (List<Share> shares : phases) {
            EffortDuration remaining = effort.minus(sum(result));
            if (remaining.isZero()) {
                return asList(result);
            }
            result = limitByCapacities(sum(result,
                    distribute(remaining, shares)));
        }
        return asList(result);
    }

    private EffortDuration[] sum(EffortDuration[] a, EffortDuration[] b) {
        EffortDuration[] result = new EffortDuration[a.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = a[i].plus(b[i]);
        }
        return result;
    }

    private EffortDuration[] distribute(EffortDuration effort,
            List<Share> shares) {
        ShareDivision division = ShareDivision.create(shares);
        return fromSecondsToDurations(division.to(division.plus(effort
                .getSeconds())));
    }

    private List<EffortDuration> asList(EffortDuration[] acc) {
        return new ArrayList<EffortDuration>(Arrays.asList(acc));
    }

    private EffortDuration[] limitByCapacities(EffortDuration[] efforts) {
        EffortDuration[] result = new EffortDuration[efforts.length];
        for (int i = 0; i < efforts.length; i++) {
            result[i] = capacities[i].limitDuration(efforts[i]);
        }
        return result;
    }

    private EffortDuration[] fromSecondsToDurations(int[] seconds) {
        EffortDuration[] result = new EffortDuration[seconds.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = seconds(seconds[i]);
        }
        return result;
    }

    private EffortDuration sum(EffortDuration[] durations) {
        return EffortDuration.sum(asList(durations),
                new IEffortFrom<EffortDuration>() {

            @Override
            public EffortDuration from(EffortDuration each) {
                return each;
            }});
    }

}
