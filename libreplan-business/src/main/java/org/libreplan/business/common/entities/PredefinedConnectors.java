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

import java.util.Arrays;
import java.util.List;

/**
 * Defines the LibrePlan {@link Connector Connectors} together with its configuration properties.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public enum PredefinedConnectors {

    TIM("Tim",
            ConnectorProperty.create(PredefinedConnectorProperties.ACTIVATED, "N"),
            ConnectorProperty.create(PredefinedConnectorProperties.SERVER_URL, ""),
            ConnectorProperty.create(PredefinedConnectorProperties.USERNAME, ""),
            ConnectorProperty.create(PredefinedConnectorProperties.PASSWORD, ""),
            ConnectorProperty.create(PredefinedConnectorProperties.TIM_NR_DAYS_TIMESHEET, "7"),
            ConnectorProperty.create(PredefinedConnectorProperties.TIM_NR_DAYS_ROSTER, "90"),
            ConnectorProperty.create(PredefinedConnectorProperties.TIM_PRODUCTIVITY_FACTOR, "100"),
            ConnectorProperty.create(PredefinedConnectorProperties.TIM_DEPARTAMENTS_IMPORT_ROSTER, "0")),

    JIRA("Jira",
            ConnectorProperty.create(PredefinedConnectorProperties.ACTIVATED, "N"),
            ConnectorProperty.create(PredefinedConnectorProperties.SERVER_URL, ""),
            ConnectorProperty.create(PredefinedConnectorProperties.USERNAME, ""),
            ConnectorProperty.create(PredefinedConnectorProperties.PASSWORD, ""),
            ConnectorProperty.create(PredefinedConnectorProperties.JIRA_LABELS, ""),
            ConnectorProperty.create(PredefinedConnectorProperties.JIRA_HOURS_TYPE, "Default")),

    EMAIL("E-mail",
            ConnectorProperty.create(PredefinedConnectorProperties.ACTIVATED, "N"),
            ConnectorProperty.create(PredefinedConnectorProperties.PROTOCOL, ""),
            ConnectorProperty.create(PredefinedConnectorProperties.HOST, ""),
            ConnectorProperty.create(PredefinedConnectorProperties.PORT, ""),
            ConnectorProperty.create(PredefinedConnectorProperties.EMAIL_SENDER, ""),
            ConnectorProperty.create(PredefinedConnectorProperties.EMAIL_USERNAME, ""),
            ConnectorProperty.create(PredefinedConnectorProperties.EMAIL_PASSWORD, "")

    );

    private String name;
    
    private List<ConnectorProperty> properties;

    PredefinedConnectors(String name, ConnectorProperty... properties) {
        this.name = name;
        this.properties = Arrays.asList(properties);
    }

    public String getName() {
        return name;
    }

    public List<ConnectorProperty> getProperties() {
        return properties;
    }

}
