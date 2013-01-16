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
import org.libreplan.importers.tim.Roster;

/**
 * Helper class to convert the Roster response to the RosterExceptions
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class RosterException {
    private Worker worker;
    List<RosterExceptionItem> rosterExceptionItems = new ArrayList<RosterExceptionItem>();

    public RosterException(Worker worker) {
        this.worker = worker;
    }

    /**
     * Reads the rosters and add the exceptions to
     * <code>rosterExceptionItems</code>
     *
     * @param rosters
     */
    public void addRosterExceptions(List<Roster> rosters) {
        Map<LocalDate, List<Roster>> mapDateRoster = new TreeMap<LocalDate, List<Roster>>();

        for (Roster roster : rosters) {
            if (!mapDateRoster.containsKey(roster.getDate())) {
                mapDateRoster.put(roster.getDate(), new ArrayList<Roster>());
            }
            mapDateRoster.get(roster.getDate()).add(roster);

        }

        for (Map.Entry<LocalDate, List<Roster>> entry : mapDateRoster
                .entrySet()) {
            RosterExceptionItem item = new RosterExceptionItem(entry.getKey());
            updateExceptionTypeAndEffort(item, entry.getValue());
            rosterExceptionItems.add(item);
        }
    }

    /**
     * updates the <code>exceptionType</code> and <code>effortDuration</code>
     *
     * @param rosterExceptionItem
     *            the rosterException item
     * @param rosters
     *            list of rosters
     */
    private void updateExceptionTypeAndEffort(
            RosterExceptionItem rosterExceptionItem, List<Roster> rosters) {
        EffortDuration max = EffortDuration.zero();
        EffortDuration sum = EffortDuration.zero();
        String rosterCatName = rosters.get(0).getRosterCategories().get(0)
                .getName();
        for (Roster roster : rosters) {
            EffortDuration duration = EffortDuration
                    .parseFromFormattedString(roster.getDuration());
            if (duration.compareTo(max) > 0) {
                rosterCatName = roster.getRosterCategories().get(0).getName();
            }
            max = EffortDuration.max(max, duration);
            sum = EffortDuration.sum(sum, duration);

        }
        rosterExceptionItem.setExceptionType(rosterCatName);
        rosterExceptionItem.setEffortDuration(sum);
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
