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

package org.libreplan.business.planner.entities;

import static org.libreplan.business.i18n.I18nHelper._;

public enum TaskStatusEnum {
    ALL(_("All")),
    FINISHED(_("Finished")),
    IN_PROGRESS(_("In progress")),
    PENDING(_("Pending")),
    BLOCKED(_("Blocked")),
    READY_TO_START(_("Ready to start"));

    private String value;

    private TaskStatusEnum(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
