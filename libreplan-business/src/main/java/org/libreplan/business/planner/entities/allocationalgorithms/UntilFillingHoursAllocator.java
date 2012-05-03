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

package org.libreplan.business.planner.entities.allocationalgorithms;

import static org.libreplan.business.workingday.EffortDuration.zero;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.ICalendar;
import org.libreplan.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.CapacityResult;
import org.libreplan.business.common.ProportionalDistributor;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.ResourceAllocation.Direction;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.util.Pair;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.business.workingday.ResourcesPerDay;

public abstract class UntilFillingHoursAllocator {

    private final Direction direction;

    private final Task task;

    private List<ResourcesPerDayModification> allocations;

    private Map<ResourcesPerDayModification, List<DayAssignment>> resultAssignments = new HashMap<ResourcesPerDayModification, List<DayAssignment>>();


    public UntilFillingHoursAllocator(Direction direction, Task task,
            List<ResourcesPerDayModification> allocations) {
        this.direction = direction;
        this.task = task;
        this.allocations = allocations;
        initializeResultsMap();
    }

    private void initializeResultsMap() {
        for (ResourcesPerDayModification r : allocations) {
            resultAssignments.put(r, new ArrayList<DayAssignment>());
        }
    }

    public IntraDayDate untilAllocating(EffortDuration effortToAllocate) {
        final IntraDayDate dateFromWhichToAllocate = direction
                .getDateFromWhichToAllocate(task);
        List<EffortPerAllocation> effortPerAllocation = effortPerAllocation(
                dateFromWhichToAllocate, effortToAllocate);
        if (effortPerAllocation.isEmpty()) {
            return null;
        }
        return untilAllocating(dateFromWhichToAllocate, effortPerAllocation);
    }

    private IntraDayDate untilAllocating(final IntraDayDate dateFromWhichToAllocate,
            List<EffortPerAllocation> effortPerAllocation) {
        IntraDayDate currentResult = dateFromWhichToAllocate;
        for (EffortPerAllocation each : effortPerAllocation) {
            IntraDayDate candidate = untilAllocating(dateFromWhichToAllocate,
                    each.allocation, each.duration);
            currentResult = pickCurrentOrCandidate(currentResult, candidate);
            setNewDataForAllocation(each.allocation, currentResult);

            // This is done in order that new assignments are taken into account
            // for the next allocations in the same task
            each.allocation.getResourceAllocation()
                    .associateAssignmentsToResource();
        }
        // Then we detach the day assignments as they are going to be associated
        // again later
        for (EffortPerAllocation each : effortPerAllocation) {
            each.allocation.getResourceAllocation().detach();
        }

        return currentResult;
    }

    private IntraDayDate pickCurrentOrCandidate(IntraDayDate current,
            IntraDayDate candidate) {
        if (direction == Direction.BACKWARD) {
            return IntraDayDate.min(current, candidate);
        }
        return IntraDayDate.max(current, candidate);
    }

    private List<EffortPerAllocation> effortPerAllocation(
            IntraDayDate dateFromWhichToAllocate, EffortDuration toBeAssigned) {
        return new HoursPerAllocationCalculator(allocations)
                .calculateEffortsPerAllocation(dateFromWhichToAllocate,
                        toBeAssigned);
    }

    public interface IAssignmentsCreator {

        List<? extends DayAssignment> createAssignmentsAtDay(PartialDay day,
                EffortDuration limit, ResourcesPerDay resourcesPerDay);
    }

    /**
     *
     * @param dateFromWhichToAllocate
     * @param resourcesPerDayModification
     * @param effortRemaining
     * @return the moment on which the allocation would be completed
     */
    private IntraDayDate untilAllocating(IntraDayDate dateFromWhichToAllocate,
            ResourcesPerDayModification resourcesPerDayModification,
            EffortDuration effortRemaining) {
        EffortDuration taken = zero();
        EffortDuration biggestLastAssignment = zero();
        IntraDayDate current = dateFromWhichToAllocate;
        IAssignmentsCreator assignmentsCreator = resourcesPerDayModification
                .createAssignmentsCreator();
        while (effortRemaining.compareTo(zero()) > 0) {
            PartialDay day = calculateDay(current);
            Pair<EffortDuration, EffortDuration> pair = assignForDay(
                    resourcesPerDayModification, assignmentsCreator, day,
                    effortRemaining);
            taken = pair.getFirst();
            biggestLastAssignment = pair.getSecond();
            effortRemaining = effortRemaining.minus(taken);

            if (effortRemaining.compareTo(zero()) > 0) {
                current = nextDay(current);
            }
        }
        return adjustFinish(resourcesPerDayModification, taken,
                biggestLastAssignment, current);
    }

    private IntraDayDate adjustFinish(
            ResourcesPerDayModification resourcesPerDayModification,
            EffortDuration allocatedLastDay,
            EffortDuration biggestLastAssignment,
            IntraDayDate end) {
        IntraDayDate result;
        ResourcesPerDay adjustBy = allocatedLastDay
                .equals(biggestLastAssignment) ? resourcesPerDayModification
                .getGoal() : ResourcesPerDay.amount(1);
        if (isForwardScheduling()) {
            result = plusEffort(end, allocatedLastDay);
            if (!resourcesPerDayModification
                    .thereAreMoreSpaceAvailableAt(result)) {
                result = nextDay(result);
            } else {
                result = end.increaseBy(adjustBy, biggestLastAssignment);
            }
        } else {
            result = minusEffort(end, allocatedLastDay,
                    resourcesPerDayModification
                    .getBeingModified().getAllocationCalendar(), adjustBy);
        }
        return result;
    }

    private IntraDayDate nextDay(IntraDayDate current) {
        if (isForwardScheduling()) {
            return current.nextDayAtStart();
        } else {
            if (current.isStartOfDay()) {
                return current.previousDayAtStart();
            } else {
                return IntraDayDate.startOfDay(current.getDate());
            }
        }
    }

    private PartialDay calculateDay(IntraDayDate current) {
        if (isForwardScheduling()) {
            return dayStartingAt(current);
        } else {
            return dayEndingAt(current);
        }
    }

    private PartialDay dayStartingAt(IntraDayDate start) {
        return new PartialDay(start, nextDay(start));
    }

    private PartialDay dayEndingAt(IntraDayDate current) {
        if (!current.isStartOfDay()) {
            return new PartialDay(IntraDayDate.startOfDay(current.getDate()),
                    current);
        }
        return PartialDay.wholeDay(current.getDate().minusDays(1));
    }

    protected boolean isForwardScheduling() {
        return Direction.FORWARD == direction;
    }

    private IntraDayDate plusEffort(IntraDayDate current, EffortDuration taken) {
        return IntraDayDate.create(current.getDate(),
                taken.plus(current.getEffortDuration()));
    }

    private IntraDayDate minusEffort(IntraDayDate current,
            EffortDuration taken, ICalendar calendar, ResourcesPerDay adjustBy) {
        if (!current.isStartOfDay()) {
            return current.decreaseBy(adjustBy,
                    taken);
        } else {
            LocalDate day = current.getDate().minusDays(1);
            EffortDuration effortAtDay = calendar
                    .asDurationOn(PartialDay.wholeDay(day),
                            ResourcesPerDay.amount(1));
            return IntraDayDate.create(day, effortAtDay).decreaseBy(adjustBy,
                    taken);
        }
    }

    private <T extends DayAssignment> void setNewDataForAllocation(
            ResourcesPerDayModification resourcesPerDayModification,
            IntraDayDate resultDate) {
        @SuppressWarnings("unchecked")
        ResourceAllocation<T> allocation = (ResourceAllocation<T>) resourcesPerDayModification
                .getBeingModified();
        ResourcesPerDay resourcesPerDay = resourcesPerDayModification.getGoal();
        @SuppressWarnings("unchecked")
        List<T> value = (List<T>) resultAssignments
                .get(resourcesPerDayModification);
        setNewDataForAllocation(allocation, resultDate, resourcesPerDay,
                value);
    }

    protected Direction getDirection() {
        return direction;
    }

    protected abstract <T extends DayAssignment> void setNewDataForAllocation(
            ResourceAllocation<T> allocation, IntraDayDate resultDate,
            ResourcesPerDay resourcesPerDay, List<T> dayAssignments);

    protected abstract CapacityResult thereAreAvailableHoursFrom(
            IntraDayDate dateFromWhichToAllocate,
            ResourcesPerDayModification resourcesPerDayModification,
            EffortDuration remainingDuration);

    protected abstract void markUnsatisfied(
            ResourcesPerDayModification allocationAttempt,
            CapacityResult capacityResult);

    /**
     *
     * @param resourcesPerDayModification
     * @return a pair composed of the effort assigned and the biggest assignment
     *         done.
     */
    private Pair<EffortDuration, EffortDuration> assignForDay(
            ResourcesPerDayModification resourcesPerDayModification,
            IAssignmentsCreator assignmentsCreator,
            PartialDay day, EffortDuration remaining) {
        List<? extends DayAssignment> newAssignments = assignmentsCreator
                .createAssignmentsAtDay(day, remaining,
                        resourcesPerDayModification.getGoal());
        resultAssignments.get(resourcesPerDayModification).addAll(
                newAssignments);
        return Pair.create(DayAssignment.sum(newAssignments),
                getMaxAssignment(newAssignments));
    }

    private EffortDuration getMaxAssignment(
            List<? extends DayAssignment> newAssignments) {
        if (newAssignments.isEmpty()) {
            return zero();
        }
        DayAssignment max = Collections.max(newAssignments,
                DayAssignment.byDurationComparator());
        return max.getDuration();
    }

    private static class EffortPerAllocation {
        final EffortDuration duration;

        final ResourcesPerDayModification allocation;

        private EffortPerAllocation(EffortDuration duration,
                ResourcesPerDayModification allocation) {
            this.duration = duration;
            this.allocation = allocation;
        }

        public static List<EffortPerAllocation> wrap(
                List<ResourcesPerDayModification> allocations,
                List<EffortDuration> durations) {
            Validate.isTrue(durations.size() == allocations.size());
            int i = 0;
            List<EffortPerAllocation> result = new ArrayList<EffortPerAllocation>();
            for(i = 0; i < allocations.size(); i++){
                result.add(new EffortPerAllocation(durations.get(i),
                        allocations.get(i)));
            }
            return result;
        }
    }

    private class HoursPerAllocationCalculator {
        private List<ResourcesPerDayModification> allocations;

        private HoursPerAllocationCalculator(
                List<ResourcesPerDayModification> allocations) {
            this.allocations = new ArrayList<ResourcesPerDayModification>(
                    allocations);
        }

        public List<EffortPerAllocation> calculateEffortsPerAllocation(
                IntraDayDate dateFromWhichToAllocate, EffortDuration toAssign) {
            do {
                List<EffortDuration> durations = divideEffort(toAssign);
                List<EffortPerAllocation> result = EffortPerAllocation.wrap(
                        allocations, durations);
                List<ResourcesPerDayModification> unsatisfied = getUnsatisfied(
                        dateFromWhichToAllocate, result);
                if (unsatisfied.isEmpty()) {
                    return result;
                }
                allocations.removeAll(unsatisfied);
            } while (!allocations.isEmpty());
            return Collections.emptyList();
        }

        private List<ResourcesPerDayModification> getUnsatisfied(
                IntraDayDate dateFromWhichToAllocate,
                List<EffortPerAllocation> hoursPerAllocations) {
            List<ResourcesPerDayModification> cannotSatisfy = new ArrayList<ResourcesPerDayModification>();
            for (EffortPerAllocation each : hoursPerAllocations) {
                CapacityResult capacityResult = thereAreAvailableHoursFrom(
                        dateFromWhichToAllocate, each.allocation, each.duration);
                if (!capacityResult.thereIsCapacityAvailable()) {
                    cannotSatisfy.add(each.allocation);
                    markUnsatisfied(each.allocation, capacityResult);
                }
            }
            return cannotSatisfy;
        }

        private List<EffortDuration> divideEffort(EffortDuration toBeDivided) {
            ProportionalDistributor distributor = ProportionalDistributor
                    .create(createShares());
            int[] secondsDivided = distributor.distribute(toBeDivided
                    .getSeconds());
            return asDurations(secondsDivided);
        }

        private int[] createShares() {
            int[] result = new int[allocations.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = normalize(allocations.get(i).getGoal()
                        .getAmount());
            }
            return result;
        }

        private List<EffortDuration> asDurations(int[] secondsDivided) {
            List<EffortDuration> result = new ArrayList<EffortDuration>();
            for (int each : secondsDivided) {
                result.add(EffortDuration.seconds(each));
            }
            return result;
        }

        /**
         * Returns a normalized amount for {@link ProportionalDistributor}. For
         * example, for 2.03, 203 is returned.
         *
         * @param amount
         * @return
         */
        private int normalize(BigDecimal amount) {
            return amount.movePointRight(2).intValue();
        }

    }

}
