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

package org.navalplanner.web.resourceload;

import static org.navalplanner.business.workingday.IntraDayDate.max;
import static org.navalplanner.business.workingday.IntraDayDate.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.Fraction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionCompounder;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate;
import org.zkoss.ganttz.data.resourceload.LoadLevel;
import org.zkoss.ganttz.data.resourceload.LoadPeriod;

interface LoadPeriodGeneratorFactory {
    LoadPeriodGenerator create(ResourceAllocation<?> allocation);
}


abstract class LoadPeriodGenerator {

    private static final Log LOG = LogFactory.getLog(LoadPeriodGenerator.class);

    public static LoadPeriodGeneratorFactory onResource(Resource resource) {
        return new OnResourceFactory(resource);
    }

    public static LoadPeriodGeneratorFactory onResourceSatisfying(
            Resource resource, Collection<Criterion> criterions) {
        return new OnResourceFactory(resource, criterions);
    }

    private static class OnResourceFactory implements
            LoadPeriodGeneratorFactory {

        private final Resource resource;

        private final ICriterion criterion;

        public OnResourceFactory(Resource resource) {
            this(resource, Collections.<Criterion> emptyList());
        }

        public OnResourceFactory(Resource resource,
                Collection<Criterion> criterionsToSatisfy) {
            Validate.notNull(resource);
            this.resource = resource;
            this.criterion = CriterionCompounder.buildAnd(criterionsToSatisfy)
                            .getResult();
        }

        @Override
        public LoadPeriodGenerator create(ResourceAllocation<?> allocation) {
            return new LoadPeriodGeneratorOnResource(resource, allocation,
                    criterion);
        }

    }

    public static LoadPeriodGeneratorFactory onCriterion(
            final Criterion criterion, final IResourceDAO resourcesDAO) {
        final List<Resource> potentialResources = resourcesDAO
                .findSatisfyingAllCriterionsAtSomePoint(Collections
                        .singletonList(criterion));
        return new LoadPeriodGeneratorFactory() {

            @Override
            public LoadPeriodGenerator create(ResourceAllocation<?> allocation) {
                return new LoadPeriodGeneratorOnCriterion(criterion,
                        allocation, potentialResources);
            }
        };
    }

    protected final IntraDayDate start;
    protected final IntraDayDate end;

    private List<ResourceAllocation<?>> allocationsOnInterval = new ArrayList<ResourceAllocation<?>>();

    protected LoadPeriodGenerator(IntraDayDate start, IntraDayDate end,
            List<ResourceAllocation<?>> allocationsOnInterval) {
        Validate.notNull(start);
        Validate.notNull(end);
        Validate.notNull(allocationsOnInterval);
        this.start = start;
        this.end = end;
        this.allocationsOnInterval = ResourceAllocation
                .getSatisfied(allocationsOnInterval);
    }

    public List<LoadPeriodGenerator> join(LoadPeriodGenerator next) {
        if (!overlaps(next)) {
            return stripEmpty(this, next);
        }
        if (isIncluded(next)) {
            return stripEmpty(this.until(next.start), intersect(next), this
                    .from(next.end));
        }
        assert overlaps(next) && !isIncluded(next);
        return stripEmpty(this.until(next.start), intersect(next), next
                .from(end));
    }

    protected List<ResourceAllocation<?>> getAllocationsOnInterval() {
        return allocationsOnInterval;
    }

    private List<LoadPeriodGenerator> stripEmpty(
            LoadPeriodGenerator... generators) {
        List<LoadPeriodGenerator> result = new ArrayList<LoadPeriodGenerator>();
        for (LoadPeriodGenerator loadPeriodGenerator : generators) {
            if (!loadPeriodGenerator.isEmpty()) {
                result.add(loadPeriodGenerator);
            }
        }
        return result;
    }

    private boolean isEmpty() {
        return start.equals(end);
    }

    protected abstract LoadPeriodGenerator create(IntraDayDate start,
            IntraDayDate end, List<ResourceAllocation<?>> allocationsOnInterval);

    private LoadPeriodGenerator intersect(LoadPeriodGenerator other) {
        return create(max(this.start, other.start),
                min(this.end, other.end), plusAllocations(other));
    }

    private List<ResourceAllocation<?>> plusAllocations(
            LoadPeriodGenerator other) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        result.addAll(allocationsOnInterval);
        result.addAll(other.allocationsOnInterval);
        return result;
    }

    private LoadPeriodGenerator from(IntraDayDate newStart) {
        return create(newStart, end, allocationsOnInterval);
    }

    private LoadPeriodGenerator until(IntraDayDate newEnd) {
        return create(start, newEnd, allocationsOnInterval);
    }

    boolean overlaps(LoadPeriodGenerator other) {
        return (start.compareTo(other.end) < 0 && other.start
                .compareTo(this.end) < 0);
    }

    private boolean isIncluded(LoadPeriodGenerator other) {
        return other.start.compareTo(start) >= 0
                && other.end.compareTo(end) <= 0;
    }

    /**
     * @return <code>null</code> if the data is invalid
     */
    public LoadPeriod build() {
        if (start.compareTo(end) > 0) {
            LOG
                    .warn("the start date is after end date. Inconsistent state for "
                            + allocationsOnInterval + ". LoadPeriod ignored");
            return null;
        }
        EffortDuration totalEffort = getTotalAvailableEffort();
        EffortDuration effortAssigned = getEffortAssigned();
        return new LoadPeriod(start.getDate(), end.asExclusiveEnd(),
                totalEffort.roundToHours(), effortAssigned.roundToHours(),
                new LoadLevel(calculateLoadPercentage(totalEffort,
                        effortAssigned)));
    }

    protected abstract EffortDuration getTotalAvailableEffort();

    private static int calculateLoadPercentage(EffortDuration totalEffort,
            EffortDuration effortAssigned) {
        if (totalEffort.isZero()) {
            return effortAssigned.isZero() ? 0 : Integer.MAX_VALUE;
        }
        if (effortAssigned.isZero()) {
            LOG.warn("total effort is " + totalEffort
                    + " but effortAssigned is zero");
            return Integer.MAX_VALUE;
        }
        Fraction fraction = totalEffort.divivedBy(effortAssigned);
        Fraction percentage = fraction.multiplyBy(Fraction.getFraction(100, 1));
        return percentage.intValue();
    }

    protected abstract EffortDuration getEffortAssigned();

    protected final EffortDuration sumAllocations() {
        EffortDuration sum = EffortDuration.zero();
        for (ResourceAllocation<?> resourceAllocation : allocationsOnInterval) {
            sum = sum.plus(getAssignedEffortFor(resourceAllocation));
        }
        return sum;
    }

    protected abstract EffortDuration getAssignedEffortFor(
            ResourceAllocation<?> resourceAllocation);

    public IntraDayDate getStart() {
        return start;
    }

    public IntraDayDate getEnd() {
        return end;
    }
}

class LoadPeriodGeneratorOnResource extends LoadPeriodGenerator {

    private Resource resource;

    private final ICriterion criterion;

    LoadPeriodGeneratorOnResource(Resource resource, IntraDayDate start,
            IntraDayDate end,
            List<ResourceAllocation<?>> allocationsOnInterval,
            ICriterion criterion) {
        super(start, end, allocationsOnInterval);
        this.resource = resource;
        this.criterion = criterion;
    }

    LoadPeriodGeneratorOnResource(Resource resource,
            ResourceAllocation<?> initial, ICriterion criterion) {
        super(initial.getIntraDayStartDate(), initial.getIntraDayEndDate(),
                Arrays.<ResourceAllocation<?>> asList(initial));
        this.resource = resource;
        this.criterion = criterion;
    }

    @Override
    protected LoadPeriodGenerator create(IntraDayDate start, IntraDayDate end,
            List<ResourceAllocation<?>> allocationsOnInterval) {
        return new LoadPeriodGeneratorOnResource(resource, start, end,
                allocationsOnInterval, criterion);
    }

    @Override
    protected EffortDuration getTotalAvailableEffort() {
        return resource.getTotalEffortFor(start, end, criterion);
    }

    @Override
    protected EffortDuration getAssignedEffortFor(
            ResourceAllocation<?> resourceAllocation) {
        return resourceAllocation.getAssignedEffort(resource, start.getDate(),
                end.asExclusiveEnd());
    }

    @Override
    protected EffortDuration getEffortAssigned() {
        return sumAllocations();
    }

}

class LoadPeriodGeneratorOnCriterion extends LoadPeriodGenerator {

    private final Criterion criterion;
    private final List<Resource> resourcesSatisfyingCriterionAtSomePoint;

    public LoadPeriodGeneratorOnCriterion(Criterion criterion,
            ResourceAllocation<?> allocation,
            List<Resource> resourcesSatisfyingCriterionAtSomePoint) {
        this(criterion, allocation.getIntraDayStartDate(), allocation
                .getIntraDayEndDate(),
                Arrays.<ResourceAllocation<?>> asList(allocation),
                resourcesSatisfyingCriterionAtSomePoint);
    }

    public LoadPeriodGeneratorOnCriterion(Criterion criterion,
            IntraDayDate startDate, IntraDayDate endDate,
            List<ResourceAllocation<?>> allocations,
            List<Resource> resourcesSatisfyingCriterionAtSomePoint) {
        super(startDate, endDate, allocations);
        this.criterion = criterion;
        this.resourcesSatisfyingCriterionAtSomePoint = resourcesSatisfyingCriterionAtSomePoint;
    }

    @Override
    protected LoadPeriodGenerator create(IntraDayDate start, IntraDayDate end,
            List<ResourceAllocation<?>> allocationsOnInterval) {
        LoadPeriodGeneratorOnCriterion result = new LoadPeriodGeneratorOnCriterion(
                criterion, start, end, allocationsOnInterval,
                resourcesSatisfyingCriterionAtSomePoint);
        return result;
    }

    @Override
    protected EffortDuration getAssignedEffortFor(
            ResourceAllocation<?> resourceAllocation) {
        return resourceAllocation.getAssignedEffort(criterion, start.getDate(),
                end.asExclusiveEnd());
    }

    @Override
    protected EffortDuration getTotalAvailableEffort() {
        EffortDuration sum = EffortDuration.zero();
        for (Resource resource : resourcesSatisfyingCriterionAtSomePoint) {
            sum = sum.plus(resource.getTotalEffortFor(start, end, criterion));
        }
        return sum;
    }

    @Override
    protected EffortDuration getEffortAssigned() {
        return sumAllocations();
    }

}
