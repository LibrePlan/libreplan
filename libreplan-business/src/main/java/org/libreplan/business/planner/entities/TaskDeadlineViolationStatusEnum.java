/*
 * This file is part of LibrePlan
 *
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

/**
 * Enumerate of {@Link Task} deadline violation statuses.
 *
 * NO_DEADLINE: Task has no deadline set.
 * DEADLINE_VIOLATED: Task didn't finish on time.
 * ON_SCHEDULE: Either Task is ahead schedule or finished just in time.
 *
 * @author Nacho Barrientos <nacho@igalia.com>
 */
public enum TaskDeadlineViolationStatusEnum {
    NO_DEADLINE(_("No deadline")),
    DEADLINE_VIOLATED(_("Deadline violated")),
    ON_SCHEDULE(_("On schedule"));

    private String value;

    private TaskDeadlineViolationStatusEnum(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
