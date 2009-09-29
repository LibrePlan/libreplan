package org.navalplanner.web.resourceload;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.resources.entities.Resource;
import org.zkoss.ganttz.data.resourceload.LoadLevel;
import org.zkoss.ganttz.data.resourceload.LoadPeriod;

interface LoadPeriodGeneratorFactory {
    LoadPeriodGenerator create(ResourceAllocation<?> allocation);
}

class OnResourceFactory implements LoadPeriodGeneratorFactory {

    private final Resource resource;

    public OnResourceFactory(Resource resource) {
        Validate.notNull(resource);
        this.resource = resource;
    }

    @Override
    public LoadPeriodGenerator create(ResourceAllocation<?> allocation) {
        return new LoadPeriodGeneratorOnResource(resource, allocation);
    }

}

abstract class LoadPeriodGenerator {

    protected final LocalDate start;
    protected final LocalDate end;

    private List<ResourceAllocation<?>> allocationsOnInterval = new ArrayList<ResourceAllocation<?>>();

    protected LoadPeriodGenerator(LocalDate start,
            LocalDate end, List<ResourceAllocation<?>> allocationsOnInterval) {
        this.start = start;
        this.end = end;
        this.allocationsOnInterval = allocationsOnInterval;
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

    protected abstract LoadPeriodGenerator create(LocalDate start,
            LocalDate end, List<ResourceAllocation<?>> allocationsOnInterval);

    private LoadPeriodGenerator intersect(LoadPeriodGenerator other) {
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
            LoadPeriodGenerator other) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        result.addAll(allocationsOnInterval);
        result.addAll(other.allocationsOnInterval);
        return result;
    }

    private LoadPeriodGenerator from(LocalDate newStart) {
        return create(newStart, end,
                allocationsOnInterval);
    }

    private LoadPeriodGenerator until(LocalDate newEnd) {
        return create(start, newEnd,
                allocationsOnInterval);
    }

    private boolean overlaps(LoadPeriodGenerator other) {
        return (start.compareTo(other.end) < 0 && other.start
                .compareTo(this.end) < 0);
    }

    private boolean isIncluded(LoadPeriodGenerator other) {
        return other.start.compareTo(start) >= 0
                && other.end.compareTo(end) <= 0;
    }

    public LoadPeriod build() {
        return new LoadPeriod(start, end, new LoadLevel(
                calculateLoadPercentage()));
    }

    protected abstract int getTotalWorkHours();

    private int calculateLoadPercentage() {
        final int totalResourceWorkHours = getTotalWorkHours();
        int assigned = sumAssigned();
        double proportion = assigned / (double) totalResourceWorkHours;
        try {
            return new BigDecimal(proportion).scaleByPowerOfTen(2).intValue();
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private int sumAssigned() {
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

class LoadPeriodGeneratorOnResource extends LoadPeriodGenerator {

    private Resource resource;

    LoadPeriodGeneratorOnResource(Resource resource, LocalDate start,
            LocalDate end, List<ResourceAllocation<?>> allocationsOnInterval) {
        super(start, end, allocationsOnInterval);
        this.resource = resource;
    }

    LoadPeriodGeneratorOnResource(Resource resource,
            ResourceAllocation<?> initial) {
        super(initial.getStartDate(), initial.getEndDate(), Arrays.<ResourceAllocation<?>> asList(initial));
        this.resource = resource;
    }

    @Override
    protected LoadPeriodGenerator create(LocalDate start, LocalDate end,
            List<ResourceAllocation<?>> allocationsOnInterval) {
        return new LoadPeriodGeneratorOnResource(resource, start, end,
                allocationsOnInterval);
    }

    @Override
    protected int getTotalWorkHours() {
        return resource.getTotalWorkHours(start, end);
    }

    @Override
    protected int getAssignedHoursFor(ResourceAllocation<?> resourceAllocation) {
        return resourceAllocation.getAssignedHours(resource, start, end);
    }

}
