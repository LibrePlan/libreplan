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

package org.libreplan.web.calendars;

import java.util.ArrayList;
import java.util.List;

import org.libreplan.business.calendars.entities.BaseCalendar;

/**
 * Class that represents a root node for the {@link BaseCalendar} tree.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class BaseCalendarTreeRoot {

    private List<BaseCalendar> rootCalendars = new ArrayList<BaseCalendar>();
    private List<BaseCalendar> derivedCalendars = new ArrayList<BaseCalendar>();

    /**
     * Creates a {@link BaseCalendarTreeRoot} using the list of {@link BaseCalendar}
     * passed as argument.
     *
     * @param baseCalendars
     *            All the {@link BaseCalendar} that will be shown in the tree.
     */
    public BaseCalendarTreeRoot(List<BaseCalendar> baseCalendars) {
        for (BaseCalendar baseCalendar : baseCalendars) {
            if (baseCalendar.isDerived()) {
                getDerivedCalendars().add(baseCalendar);
            } else {
                getRootCalendars().add(baseCalendar);
            }
        }
    }

    /**
     * Returns the {@link BaseCalendar} that has no parent.
     */
    public List<BaseCalendar> getRootCalendars() {
        return rootCalendars;
    }

    /**
     * Returns all the {@link BaseCalendar} that has a parent.
     */
    public List<BaseCalendar> getDerivedCalendars() {
        return derivedCalendars;
    }

}
