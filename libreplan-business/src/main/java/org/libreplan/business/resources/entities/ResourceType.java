/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.business.resources.entities;

import static org.libreplan.business.i18n.I18nHelper._;

/**
 * Enumerate with the three basic types of resource: non-limiting, limiting and strategic.
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public enum ResourceType {

    NON_LIMITING_RESOURCE(_("Normal resource")),
    LIMITING_RESOURCE(_("Queue-based resource"));

    private String option;

    private ResourceType(String option) {
        this.option = option;
    }

    public String toString() {
        return option;
    }

}
