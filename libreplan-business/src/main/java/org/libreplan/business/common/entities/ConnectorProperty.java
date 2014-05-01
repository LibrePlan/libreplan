/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 Igalia, S.L.
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

import org.hibernate.validator.constraints.NotEmpty;

/**
 * This class is intended to work as a Hibernate component. It's formed by two
 * attributes, the key and the value of the property. It represents the
 * different configuration parameters of a {@link Connector}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ConnectorProperty {

    public static ConnectorProperty create(String key, String value) {
        return new ConnectorProperty(key, value);
    }

    private String key;
    private String value;

    /**
     * Default constructor for Hibernate. Do not use!
     */
    protected ConnectorProperty() {
    }

    private ConnectorProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @NotEmpty(message = "property key not specified")
    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
