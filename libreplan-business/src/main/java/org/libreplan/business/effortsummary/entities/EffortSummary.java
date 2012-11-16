/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.business.effortsummary.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;

public class EffortSummary extends BaseEntity {

    private LocalDate startDate;

    private LocalDate endDate;

    private int[] availableEffort;

    private int[] assignedEffort;

    private String availableEffortString;

    private String assignedEffortString;

    private Resource resource;

    /**
     * This property might be null. In that case, it means that this
     * EffortSummary object contains the global assigned hours of this resource.
     */
    private Task task;

    public static EffortSummary create(LocalDate startDate, LocalDate endDate,
            int[] availableEffort, int[] assignedEffort, Resource resource) {
        return create(startDate, endDate, availableEffort, assignedEffort,
                resource, null);
    }

    public static EffortSummary create(LocalDate startDate, LocalDate endDate,
            int[] availableEffort, int[] assignedEffort,
            Resource resource, Task task) {
        EffortSummary newObject = new EffortSummary();
        newObject.setStartDate(startDate);
        newObject.setEndDate(endDate);
        newObject.setAvailableEffort(availableEffort);
        newObject.setAssignedEffort(assignedEffort);
        newObject.setResource(resource);
        newObject.setTask(task);
        return create(newObject);
    }

    public static EffortSummary createFromNewResource(Resource resource) {
        final int defaultNumberOfElements = 1000;

        // get start and end dates
        ResourceCalendar resourceCalendar = resource.getCalendar();
        LocalDate startDate = resourceCalendar.getFistCalendarAvailability()
                .getStartDate();
        LocalDate endDate = resourceCalendar.getLastCalendarAvailability()
                .getEndDate();
        if (endDate == null) {
            endDate = startDate.plusDays(defaultNumberOfElements - 1);
        }
        int numberOfElements = Days.daysBetween(startDate, endDate).getDays() + 1;

        // fill availability data
        int[] availableEffort = new int[numberOfElements];
        int[] assignedEffort = new int[numberOfElements];
        for (int i = 0; i < numberOfElements; i++) {
            PartialDay day = PartialDay.wholeDay(startDate.plusDays(i));
            availableEffort[i] = resourceCalendar.getCapacityOn(day)
                    .getSeconds();
            assignedEffort[i] = 0; // because the object is new
        }
        return EffortSummary.create(startDate, endDate,
                    availableEffort, assignedEffort, resource);

    }

    public static Set<EffortSummary> createFromResourceAllocations(
            Set<ResourceAllocation<?>> allocations) {
        Set<EffortSummary> efforts = new HashSet<EffortSummary>();

        for (ResourceAllocation<?> allocation : allocations) {
            // TODO: we assume the allocation is specific to simplify the
            // implementation
            SpecificResourceAllocation specificAllocation =
                    (SpecificResourceAllocation) allocation;

            Resource resource = specificAllocation.getResource();
            ResourceCalendar resourceCalendar = resource.getCalendar();
            Task task = allocation.getTask();

            LocalDate startDate = allocation.getStartDate();
            LocalDate endDate = allocation.getEndDate();
            int numberOfElements = Days.daysBetween(startDate, endDate)
                    .getDays() + 1;

            int[] availableEffort = new int[numberOfElements];
            int[] assignedEffort = new int[numberOfElements];
            for (int i = 0; i < numberOfElements; i++) {
                PartialDay day = PartialDay.wholeDay(startDate.plusDays(i));
                availableEffort[i] = resourceCalendar.getCapacityOn(day)
                        .getSeconds();
                assignedEffort[i] = allocation.getAssignedDurationAt(resource,
                        startDate.plusDays(i)).getSeconds();
            }
            efforts.add(EffortSummary.create(startDate, endDate,
                    availableEffort, assignedEffort, resource, task));
        }

        return efforts;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public int[] getAvailableEffort() {
        if (availableEffort == null) {
            availableEffort = parseSerializedArray(availableEffortString);
        }
        return availableEffort;
    }

    public void setAvailableEffort(int[] availableEffort) {
        this.availableEffort = availableEffort;
        this.availableEffortString = serializeIntArray(availableEffort);
    }

    public int[] getAssignedEffort() {
        if (assignedEffort == null) {
            assignedEffort = parseSerializedArray(assignedEffortString);
        }
        return assignedEffort;
    }

    public void setAssignedEffort(int[] assignedEffort) {
        this.assignedEffort = assignedEffort;
        this.assignedEffortString = serializeIntArray(assignedEffort);
    }

    public EffortDuration getAvailableEffortForDate(LocalDate date) {
        int positionInArray = Days.daysBetween(startDate, date).getDays();
        if (positionInArray < 0
                || positionInArray >= getAvailableEffort().length) {
            return EffortDuration.zero();
        }
        return EffortDuration.seconds(getAvailableEffort()[positionInArray]);
    }

    public EffortDuration getAssignedEffortForDate(LocalDate date) {
        int positionInArray = Days.daysBetween(startDate, date).getDays();
        if (positionInArray < 0
                || positionInArray >= getAssignedEffort().length) {
            return EffortDuration.zero();
        }
        return EffortDuration.seconds(getAssignedEffort()[positionInArray]);
    }

    public EffortDuration getAccumulatedAvailableEffort() {
        int accumulated = 0;
        for (int effort : getAvailableEffort()) {
            accumulated += effort;
        }
        return EffortDuration.seconds(accumulated);
    }

    public EffortDuration getAccumulatedAssignedEffort() {
        int accumulated = 0;
        for (int effort : getAssignedEffort()) {
            accumulated += effort;
        }
        return EffortDuration.seconds(accumulated);
    }

    public int getLoadPercentage() {
        BigDecimal assigned = new BigDecimal(getAccumulatedAssignedEffort()
                .getSeconds());
        BigDecimal available = new BigDecimal(getAccumulatedAvailableEffort()
                .getSeconds());
        return assigned.divide(available, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).intValue();
    }

    /**
     * Update the availability data in the EffortSummary object reading it again
     * from the attached resource.
     */
    public void updateAvailabilityFromResource() {
        // calculate correct start date
        ResourceCalendar resourceCalendar = resource.getCalendar();
        LocalDate startDate = resourceCalendar.getFistCalendarAvailability()
                .getStartDate();
        if (getStartDate().isAfter(startDate)) {
            // the start date has been moved
            // fill the gap in assignedEffort array with 0s
            int numberOfElements = Days.daysBetween(startDate, getStartDate())
                    .getDays();
            int[] filler = new int[numberOfElements];
            for (int i = 0; i < numberOfElements; i++) {
                filler[i] = 0;
            }
            int[] assignedEffort = new int[numberOfElements
                    + getAssignedEffort().length];
            System.arraycopy(filler, 0, assignedEffort, 0, numberOfElements);
            System.arraycopy(getAssignedEffort(), 0, assignedEffort,
                    numberOfElements, getAssignedEffort().length);
            setAssignedEffort(assignedEffort);
        } else {
            startDate = getStartDate();
        }

        // calculate correct end date
        LocalDate endDate = resourceCalendar.getFistCalendarAvailability()
                .getEndDate();
        if (endDate == null || endDate.isBefore(getEndDate())) {
            endDate = getEndDate();
        }
        int numberOfElements = Days.daysBetween(startDate, endDate).getDays() + 1;

        // fill availability data
        int[] availableEffort = new int[numberOfElements];
        for (int i = 0; i < numberOfElements; i++) {
            PartialDay day = PartialDay.wholeDay(startDate.plusDays(i));
            availableEffort[i] = resourceCalendar.getCapacityOn(day)
                    .getSeconds();
        }

        // update fields
        setStartDate(startDate);
        setEndDate(endDate);
        setAvailableEffort(availableEffort);

    }

    /**
     * String getter only supposed to be used by Hibernate.
     *
     * @return availableEffort int array serialized as a String
     */
    public String getAvailableEffortString() {
        return this.availableEffortString;
    }

    /**
     * String setter only supposed to be used by Hibernate.
     *
     * @param availableEffort int array serialized as a String
     */
    public void setAvailableEffortString(String availableEffort) {
        this.availableEffortString = availableEffort;
        this.availableEffort = parseSerializedArray(availableEffort);
    }

    /**
     * String getter only supposed to be used by Hibernate.
     *
     * @return assignedEffort int array serialized as a String
     */
    public String getAssignedEffortString() {
        return this.assignedEffortString;
    }

    /**
     * String setter only supposed to be used by Hibernate.
     *
     * @param assignedEffort int array serialized as a String
     */
    public void setAssignedEffortString(String assignedEffort) {
        this.assignedEffortString = assignedEffort;
        this.assignedEffort = parseSerializedArray(assignedEffort);
    }

    private int[] parseSerializedArray(String serializedArray) {
        String[] split = serializedArray.split(",");
        int[] result = new int[split.length];

        for (int i = 0; i < split.length; i++) {
            result[i] = Integer.parseInt(split[i]);
        }

        return result;
    }

    private String serializeIntArray(int[] array) {
        String result = "";

        for (int i = 0; i < array.length; i++) {
            result += Integer.toString(array[i]) + ",";
        }

        return result;
    }

    public void addAssignedEffort(EffortSummary operand) {
        operateWithAssignedEffort(operand, Operation.ADD);
    }

    public void substractAssignedEffort(EffortSummary operand) {
        operateWithAssignedEffort(operand, Operation.SUBSTRACT);
    }

    private enum Operation {
        ADD, SUBSTRACT
    };

    private void operateWithAssignedEffort(EffortSummary operand,
            Operation operation) {
        int[] assignedEffort = getAssignedEffort();
        int[] availableEffort = getAvailableEffort();

        // calculate correct start date
        LocalDate startDate = operand.getStartDate();
        if (getStartDate().isAfter(startDate)) {
            int numberOfElements = Days.daysBetween(startDate, getStartDate())
                    .getDays();

            // the start date has to be moved into the past:

            // fill the gap in assignedEffort array with 0s
            int[] blankFiller = new int[numberOfElements];
            for (int i = 0; i < numberOfElements; i++) {
                blankFiller[i] = 0;
            }
            int[] newAssignedEffort = new int[numberOfElements
                    + assignedEffort.length];
            System.arraycopy(blankFiller, 0, newAssignedEffort, 0,
                    numberOfElements);
            System.arraycopy(assignedEffort, 0, newAssignedEffort,
                    numberOfElements, assignedEffort.length);

            // fill the gap in availableEffort array with the data from the
            // operand
            int[] newAvailableEffort = new int[numberOfElements
                    + availableEffort.length];
            System.arraycopy(operand.getAvailableEffort(), 0,
                    newAvailableEffort, 0, numberOfElements);
            System.arraycopy(availableEffort, 0, newAvailableEffort,
                    numberOfElements, availableEffort.length);

            setAssignedEffort(newAssignedEffort);
            setAvailableEffort(newAvailableEffort);
        } else {
            startDate = getStartDate();
        }

        // calculate correct end date
        LocalDate endDate = operand.getEndDate();
        if (getEndDate().isBefore(endDate)) {
            int numberOfElements = Days.daysBetween(endDate, getEndDate())
                    .getDays();

            // the start date has to be moved into the past:

            // fill the gap in assignedEffort array with 0s
            int[] blankFiller = new int[numberOfElements];
            for (int i = 0; i < numberOfElements; i++) {
                blankFiller[i] = 0;
            }
            int[] newAssignedEffort = new int[numberOfElements
                    + assignedEffort.length];
            System.arraycopy(assignedEffort, 0, newAssignedEffort, 0,
                    assignedEffort.length);
            System.arraycopy(blankFiller, 0, newAssignedEffort,
                    assignedEffort.length, numberOfElements);

            // fill the gap in availableEffort array with the data from the
            // operand
            int[] newAvailableEffort = new int[numberOfElements
                    + availableEffort.length];
            System.arraycopy(availableEffort, 0, newAvailableEffort, 0,
                    availableEffort.length);
            System.arraycopy(operand.getAvailableEffort(), 0,
                    newAvailableEffort, availableEffort.length,
                    numberOfElements);
        } else {
            endDate = getEndDate();
        }
        int numberOfElements = Days.daysBetween(startDate, endDate).getDays() + 1;

        // operate with assignments data
        switch (operation) {

            case ADD:
                for (int i = 0; i < numberOfElements; i++) {
                    LocalDate day = startDate.plusDays(i);
                    assignedEffort[i] += operand.getAssignedEffortForDate(day)
                            .getSeconds();
                }
                break;

            case SUBSTRACT:
                for (int i = 0; i < numberOfElements; i++) {
                    LocalDate day = startDate.plusDays(i);
                    assignedEffort[i] -= operand.getAssignedEffortForDate(day)
                            .getSeconds();
                }
                break;
        }

        // update fields
        setStartDate(startDate);
        setEndDate(endDate);
        setAssignedEffort(assignedEffort);
        setAvailableEffort(availableEffort);

    }

    /**
     * Checks if this EffortSummary object contains the global assigned hours
     * for a resource, or if it only contains the hours for one task.
     *
     * @return True if it's global, false otherwise.
     */
    public boolean isGlobal() {
        return (task == null);
    }

}
