/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2015 LibrePlan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.email.entities;

import static org.libreplan.business.i18n.I18nHelper._;

/**
 * Available E-mail templates.
 *
 * TEMPLATE_N(_("Template N")) - for i18n
 * TEMPLATE_A("Template A") - for general use (no internationalizing)
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public enum EmailTemplateEnum {

    TEMPLATE_TASK_ASSIGNED_TO_RESOURCE(_("Task assigned to resource")),
    TEMPLATE_RESOURCE_REMOVED_FROM_TASK(_("Resource removed from task")),
    TEMPLATE_MILESTONE_REACHED(_("Milestone reached")),
    TEMPLATE_TODAY_TASK_SHOULD_START(_("Task should start")),
    TEMPLATE_TODAY_TASK_SHOULD_FINISH(_("Task should finish")),
    TEMPLATE_ENTER_DATA_IN_TIMESHEET(_("Enter data in timesheet"));

    private final String templateType;

    EmailTemplateEnum(String templateType) {
        this.templateType = templateType;
    }

    public String getTemplateType() {
        return templateType;
    }
}
