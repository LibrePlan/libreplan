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

package org.libreplan.importers.tim;

/**
 * Class containing all constants for Tim-options.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public final class TimOptions {

    private TimOptions() {

    }

    public static final String UPDATE_OR_INSERT = "@";
    public static final String UPDATE = "%";
    public static final String AUTO_INSERT = "!";
    public static final String QUOTED = "''";
    public static final String DECIMAL = "#";
    public static final String SUM_DOUBLE = "+";
    public static final String SUM_LONG = "&";
    public static final String PARENT = "^";
    public static final String ANY_PARENT = "~";

}
