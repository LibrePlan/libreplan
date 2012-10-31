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

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.BaseEntity;
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

    public static EffortSummary create(LocalDate startDate, LocalDate endDate,
            int[] availableEffort, int[] assignedEffort,
            Resource resource) {
        EffortSummary newObject = new EffortSummary();
        newObject.setStartDate(startDate);
        newObject.setEndDate(endDate);
        newObject.setAvailableEffort(availableEffort);
        newObject.setAssignedEffort(assignedEffort);
        newObject.setResource(resource);
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
        if (availableEffort.length < positionInArray) {
            return EffortDuration.zero();
        }
        return EffortDuration.seconds(availableEffort[positionInArray]);
    }

    public EffortDuration getAssignedEffortForDate(LocalDate date) {
        int positionInArray = Days.daysBetween(startDate, date).getDays();
        if (assignedEffort.length < positionInArray) {
            return EffortDuration.zero();
        }
        return EffortDuration.seconds(assignedEffort[positionInArray]);
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

}
