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

package org.libreplan.business.common.entities;

import static org.libreplan.business.i18n.I18nHelper._;

/**
 * Simply class to keep constants of {@link ConnectorProperty properties} for LibrePlan {@link Connector connectors}.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public class PredefinedConnectorProperties {

    // Generic
    public static String ACTIVATED = _("Activated");
    public static String SERVER_URL = _("Server URL");
    public static String USERNAME = _("Username");
    public static String PASSWORD = _("Password");

    // Specific for Tim
    public static String TIM_NR_DAYS_TIMESHEET = _("Number of days timesheet to Tim");
    public static String TIM_NR_DAYS_ROSTER = _("Number of days roster from Tim");
    public static String TIM_PRODUCTIVITY_FACTOR = _("Productivity factor");
    public static String TIM_DEPARTAMENTS_IMPORT_ROSTER = _("Department IDs to import toster");

    // Specific for JIRA
    public static String JIRA_LABELS = _("JIRA labels: comma-separated list of labels or URL");
    public static String JIRA_HOURS_TYPE = _("Hours type");

    /**
     * Code prefix for different entities integrated with JIRA.
     */
    public static final String JIRA_CODE_PREFIX = "JIRA-";

    // Specific for E-mail
    public static String PROTOCOL = _("Protocol");
    public static String HOST = _("Host");
    public static String PORT = _("Port");
    public static String EMAIL_SENDER = _("From address (no reply)");
    public static String EMAIL_USERNAME = _("Username (optional)");
    public static String EMAIL_PASSWORD = _("Password (optional)");

}
