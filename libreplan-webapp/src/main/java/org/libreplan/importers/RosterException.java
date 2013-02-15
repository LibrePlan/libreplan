/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.importers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.importers.tim.RosterDTO;

/**
 * Class to convert the Roster response DTO to the <code>RosterException<code>
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class RosterException {
    private Worker worker;
    private int productivityFactor;

    List<RosterExceptionItem> rosterExceptionItems = new ArrayList<RosterExceptionItem>();

    public RosterException(Worker worker, int productivityFactor) {
        this.worker = worker;
        this.productivityFactor = productivityFactor;
    }

    /**
     * Reads the rosters and add the exceptions to
     * <code>rosterExceptionItems</code>
     *
     * @param rosterDTOs
     *            list of rosterDTO
     */
    public void addRosterExceptions(List<RosterDTO> rosterDTOs) {
        Map<LocalDate, List<RosterDTO>> mapDateRosterDTO = new TreeMap<LocalDate, List<RosterDTO>>();

        for (RosterDTO rosterDTO : rosterDTOs) {
            if (!mapDateRosterDTO.containsKey(rosterDTO.getDate())) {
                mapDateRosterDTO.put(rosterDTO.getDate(), new ArrayList<RosterDTO>());
            }
            mapDateRosterDTO.get(rosterDTO.getDate()).add(rosterDTO);

        }

        for (Map.Entry<LocalDate, List<RosterDTO>> entry : mapDateRosterDTO
                .entrySet()) {
            RosterExceptionItem item = new RosterExceptionItem(entry.getKey());
            updateExceptionTypeAndEffort(item, entry.getValue());
            rosterExceptionItems.add(item);
        }
    }

    /**
     * updates the <code>exceptionType</code> and <code>effortDuration</code>
     *
     * In Tim you can divide your exception day in different
     * <code>exceptionType</code>, for example on Monday:
     * <ul>
     * <li>4 hours RESOURCE_HOLIDAY</li>
     * <li>2 hours STRIKE</li>
     * <li>2 hours BANK_HOLIDAY</li>
     * </ul>
     *
     * But Libreplan allows only one <code>exceptionType</code> per day.
     *
     * In order to store different <code>exceptionTypes</code> per day as one
     * <code>exceptionType</code>, this method gets the
     * <code>exceptionType</code> from the <code>rosterDTO</code> with the
     * highest duration, in this example RESOURCE_HOLIDAY as a valid exception
     * type, but the total duration is the sum of all these exception types
     *
     * Again in Tim this total duration means that the worker is on holiday for
     * that total duration, but Libreplan does the opposite. In Libreplan, the
     * total duration in this case means that the worker is not on holiday. If
     * he is then the total duration should be Zero. And this method does this
     * translation
     *
     * @param rosterExceptionItem
     *            the rosterException item
     * @param rosterDTOs
     *            list of rosterDTO
     */
    private void updateExceptionTypeAndEffort(
            RosterExceptionItem rosterExceptionItem, List<RosterDTO> rosterDTOs) {
        EffortDuration max = EffortDuration.zero();
        EffortDuration sum = EffortDuration.zero();
        String rosterCatName = rosterDTOs.get(0).getRosterCategories().get(0)
                .getName();
        for (RosterDTO rosterDTO : rosterDTOs) {
            EffortDuration duration = EffortDuration
                    .parseFromFormattedString(rosterDTO.getDuration());
            if (duration.compareTo(max) > 0) {
                rosterCatName = rosterDTO.getRosterCategories().get(0)
                        .getName();
            }
            max = EffortDuration.max(max, duration);
            sum = EffortDuration.sum(sum, duration);

        }
        EffortDuration exceptionTime = EffortDuration.zero();

        EffortDuration workableTime = worker.getCalendar().getCapacityOn(
                PartialDay.wholeDay(rosterExceptionItem.getDate()));

        // Convert the total duration from Tim to the productivity time as is
        // configured in resource calendar
        EffortDuration productivityTime = sum.multiplyBy(productivityFactor)
                .divideBy(100);

        // Calculate the exception time
        if (workableTime.compareTo(productivityTime) >= 0) {
            exceptionTime = workableTime.minus(productivityTime);
        }

        rosterExceptionItem.setExceptionType(rosterCatName);
        rosterExceptionItem.setEffortDuration(exceptionTime);
    }

    /**
     * returns {@link Worker}
     */
    public Worker getWorker() {
        return worker;
    }

    /**
     * returns list of {@link RosterExceptionItem}
     */
    public List<RosterExceptionItem> getRosterExceptionItems() {
        return rosterExceptionItems;
    }

    /**
     * class representing RosterExceptionItem
     */
    public class RosterExceptionItem {
        private LocalDate date;
        private String exceptionType;
        private EffortDuration effortDuration;

        public RosterExceptionItem(LocalDate date) {
            this.date = date;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getExceptionType() {
            return exceptionType;
        }

        public void setExceptionType(String exceptionType) {
            this.exceptionType = exceptionType;
        }

        public EffortDuration getEffortDuration() {
            return effortDuration;
        }

        public void setEffortDuration(EffortDuration effortDuration) {
            this.effortDuration = effortDuration;
        }
    }

}
