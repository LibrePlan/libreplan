/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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
package org.libreplan.web.common.components.finders;

import org.libreplan.business.planner.entities.TaskGroup;

/**
 * Different filters for {@link TaskGroup} in company view Gantt.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public enum TaskGroupFilterEnum implements IFilterEnum {

    Criterion(_("Criterion")), Label(_("Label")), ExternalCompany(_("Customer")), State(
            _("State")), Code(_("Code")), CustomerReference(
            _("Customer Reference")), Resource(_("Resource"));
    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    private String description;

    private TaskGroupFilterEnum(String description) {
        this.description = description;
    }

    public String toString() {
        return this.description;
    }

}
