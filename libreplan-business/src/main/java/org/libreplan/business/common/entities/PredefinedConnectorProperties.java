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

import static org.libreplan.business.i18n.I18nHelper._t;

/**
 * Simply class to keep constants of {@link ConnectorProperty properties} for LibrePlan {@link Connector connectors}.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public class PredefinedConnectorProperties {

    // Generic
    public static String ACTIVATED = _t("Activated");
    public static String SERVER_URL = _t("Server URL");
    public static String USERNAME = _t("Username");
    public static String PASSWORD = _t("Password");

    // Specific for Tim
    public static String TIM_NR_DAYS_TIMESHEET = _t("Number of days timesheet to Tim");
    public static String TIM_NR_DAYS_ROSTER = _t("Number of days roster from Tim");
    public static String TIM_PRODUCTIVITY_FACTOR = _t("Productivity factor");
    public static String TIM_DEPARTAMENTS_IMPORT_ROSTER = _t("Department IDs to import toster");

    // Specific for JIRA
    public static String JIRA_LABELS = _t("JIRA labels: comma-separated list of labels or URL");
    public static String JIRA_HOURS_TYPE = _t("Hours type");

    /**
     * Code prefix for different entities integrated with JIRA.
     */
    public static final String JIRA_CODE_PREFIX = "JIRA-";

    // Specific for E-mail
    public static String PROTOCOL = _t("Protocol");
    public static String HOST = _t("Host");
    public static String PORT = _t("Port");
    public static String EMAIL_SENDER = _t("From address (no reply)");
    public static String EMAIL_USERNAME = _t("Username (optional)");
    public static String EMAIL_PASSWORD = _t("Password (optional)");

}
