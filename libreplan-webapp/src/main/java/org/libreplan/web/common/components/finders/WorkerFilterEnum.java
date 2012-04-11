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

package org.libreplan.web.common.components.finders;

import org.libreplan.business.resources.entities.Resource;

/**
 * Diferent filters for {@link Resource}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public enum WorkerFilterEnum implements IFilterEnum {

    RESOURCE(_("Resource")), CRITERION(_("Criterion"));

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    private String description;

    private WorkerFilterEnum(String description) {
        this.description = description;
    }

    public String toString() {
        return this.description;
    }

}
