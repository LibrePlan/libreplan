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

package org.navalplanner.web.limitingresources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificDayAssignment;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionCompounder;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.Resource;

interface QueueTaskGeneratorFactory {
    QueueTaskGenerator create(ResourceAllocation<?> allocation);
}


abstract class QueueTaskGenerator {

    private static final Log LOG = LogFactory.getLog(QueueTaskGenerator.class);

    public static QueueTaskGeneratorFactory onResource(Resource resource) {
        return new OnResourceFactory(resource);
    }

    public static QueueTaskGeneratorFactory onResourceSatisfying(
            Resource resource, Collection<Criterion> criterions) {
        return new OnResourceFactory(resource, criterions);
    }

    private static class OnResourceFactory implements
            QueueTaskGeneratorFactory {

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
        public QueueTaskGenerator create(ResourceAllocation<?> allocation) {
            return new QueueTaskGeneratorOnResource(resource, allocation,
                    criterion);
        }

    }

    public static QueueTaskGeneratorFactory onCriterion(
            final Criterion criterion, final IResourceDAO resourcesDAO) {
        return new QueueTaskGeneratorFactory() {

            @Override
            public QueueTaskGenerator create(ResourceAllocation<?> allocation) {
                return new QueueTaskGeneratorOnCriterion(criterion,
                        allocation, findResources(criterion, resourcesDAO));
            }

            private List<Resource> findResources(final Criterion criterion,
                    final IResourceDAO resourcesDAO) {
                return resourcesDAO
                        .findSatisfyingAllCriterionsAtSomePoint(Collections
                                .singletonList(criterion));
            }
        };
    }

    protected final LocalDate start;
    protected final LocalDate end;

    private List<ResourceAllocation<?>> allocationsOnInterval = new ArrayList<ResourceAllocation<?>>();

    protected QueueTaskGenerator(LocalDate start,
            LocalDate end, List<ResourceAllocation<?>> allocationsOnInterval) {
        Validate.notNull(start);
        Validate.notNull(end);
        Validate.notNull(allocationsOnInterval);
        this.start = start;
        this.end = end;
        this.allocationsOnInterval = ResourceAllocation
                .getSatisfied(allocationsOnInterval);
    }

    public List<QueueTaskGenerator> join(QueueTaskGenerator next) {
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

    private List<QueueTaskGenerator> stripEmpty(
            QueueTaskGenerator... generators) {
        List<QueueTaskGenerator> result = new ArrayList<QueueTaskGenerator>();
        for (QueueTaskGenerator loadPeriodGenerator : generators) {
            if (!loadPeriodGenerator.isEmpty()) {
                result.add(loadPeriodGenerator);
            }
        }
        return result;
    }

    private boolean isEmpty() {
        return start.equals(end);
    }

    protected abstract QueueTaskGenerator create(LocalDate start,
            LocalDate end, List<ResourceAllocation<?>> allocationsOnInterval);

    private QueueTaskGenerator intersect(QueueTaskGenerator other) {
        return create(max(this.start, other.start),
                min(this.end, other.end), plusAllocations(other));
    }

    private static LocalDate max(LocalDate l1, LocalDate l2) {
        return l1.compareTo(l2) < 0 ? l2 : l1;
    }

    private static LocalDate min(LocalDate l1, LocalDate l2) {
        return l1.compareTo(l2) < 0 ? l1 : l2;
    }

    private List<ResourceAllocation<?>> plusAllocations(
            QueueTaskGenerator other) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        result.addAll(allocationsOnInterval);
        result.addAll(other.allocationsOnInterval);
        return result;
    }

    private QueueTaskGenerator from(LocalDate newStart) {
        return create(newStart, end,
                allocationsOnInterval);
    }

    private QueueTaskGenerator until(LocalDate newEnd) {
        return create(start, newEnd,
                allocationsOnInterval);
    }

    boolean overlaps(QueueTaskGenerator other) {
        return (start.compareTo(other.end) < 0 && other.start
                .compareTo(this.end) < 0);
    }

    private boolean isIncluded(QueueTaskGenerator other) {
        return other.start.compareTo(start) >= 0
                && other.end.compareTo(end) <= 0;
    }

    protected abstract int getTotalWorkHours();

    protected abstract int getHoursAssigned();

    protected final int sumAllocations() {
        int sum = 0;
        for (ResourceAllocation<?> resourceAllocation : allocationsOnInterval) {
            sum += getAssignedHoursFor(resourceAllocation);
        }
        return sum;
    }

    protected abstract int getAssignedHoursFor(
            ResourceAllocation<?> resourceAllocation);

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }
}

class QueueTaskGeneratorOnResource extends QueueTaskGenerator {

    private Resource resource;

    private final ICriterion criterion;

    QueueTaskGeneratorOnResource(Resource resource, LocalDate start,
            LocalDate end, List<ResourceAllocation<?>> allocationsOnInterval,
            ICriterion criterion) {
        super(start, end, allocationsOnInterval);
        this.resource = resource;
        this.criterion = criterion;
    }

    QueueTaskGeneratorOnResource(Resource resource,
            ResourceAllocation<?> initial, ICriterion criterion) {
        super(initial.getStartDate(), initial.getEndDate(), Arrays.<ResourceAllocation<?>> asList(initial));
        this.resource = resource;
        this.criterion = criterion;
    }

    @Override
    protected QueueTaskGenerator create(LocalDate start, LocalDate end,
            List<ResourceAllocation<?>> allocationsOnInterval) {
        return new QueueTaskGeneratorOnResource(resource, start, end,
                allocationsOnInterval, criterion);
    }

    @Override
    protected int getTotalWorkHours() {
        return resource.getTotalWorkHours(start, end, criterion);
    }

    @Override
    protected int getAssignedHoursFor(ResourceAllocation<?> resourceAllocation) {
        return resourceAllocation.getAssignedHours(resource, start, end);
    }

    @Override
    protected int getHoursAssigned() {
        return sumAllocations();
    }

}

class QueueTaskGeneratorOnCriterion extends QueueTaskGenerator {

    private final Criterion criterion;
    private final List<Resource> resourcesSatisfyingCriterionAtSomePoint;

    public QueueTaskGeneratorOnCriterion(Criterion criterion,
            ResourceAllocation<?> allocation,
            List<Resource> resourcesSatisfyingCriterionAtSomePoint) {
        this(criterion, allocation.getStartDate(), allocation.getEndDate(),
                Arrays.<ResourceAllocation<?>> asList(allocation),
                resourcesSatisfyingCriterionAtSomePoint);
    }

    public QueueTaskGeneratorOnCriterion(Criterion criterion,
            LocalDate startDate, LocalDate endDate,
            List<ResourceAllocation<?>> allocations,
            List<Resource> resourcesSatisfyingCriterionAtSomePoint) {
        super(startDate, endDate, allocations);
        this.criterion = criterion;
        this.resourcesSatisfyingCriterionAtSomePoint = resourcesSatisfyingCriterionAtSomePoint;
    }

    @Override
    protected QueueTaskGenerator create(LocalDate start, LocalDate end,
            List<ResourceAllocation<?>> allocationsOnInterval) {
        QueueTaskGeneratorOnCriterion result = new QueueTaskGeneratorOnCriterion(
                criterion, start, end, allocationsOnInterval,
                resourcesSatisfyingCriterionAtSomePoint);
        result.specificByResourceCached = specificByResourceCached;
        return result;
    }

    @Override
    protected int getAssignedHoursFor(ResourceAllocation<?> resourceAllocation) {
        return resourceAllocation.getAssignedHours(start, end);
    }

    @Override
    protected int getTotalWorkHours() {
        int sum = 0;
        for (Resource resource : resourcesSatisfyingCriterionAtSomePoint) {
            sum += resource.getTotalWorkHours(start, end, criterion);
        }
        return sum;
    }

    @Override
    protected int getHoursAssigned() {
        return sumAllocations();
    }

    private Map<Resource, List<SpecificDayAssignment>> specificByResourceCached = new HashMap<Resource, List<SpecificDayAssignment>>();

}
