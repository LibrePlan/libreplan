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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Valid;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.daos.IConnectorDAO;

/**
 * Connector entity, represents a connector in order that LibrePlan interchange
 * some data with other application.
 *
 * A connector is identified by a string called <code>majorId</code> and it has
 * a list of pairs key-value in order to store the configuration parameters of
 * the connector.
 *
 * This entity should be used to create new connectors in LibrePlan.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class Connector extends BaseEntity {

    public static Connector create(String majorId) {
        return create(new Connector(majorId));
    }

    private String majorId;

    private List<ConnectorProperty> properties = new ArrayList<ConnectorProperty>();

    /**
     * Constructor for Hibernate. Do not use!
     */
    protected Connector() {
    }

    private Connector(String majorId) {
        this.majorId = majorId;
    }

    @NotEmpty(message = "major id not specified")
    public String getMajorId() {
        return majorId;
    }

    public void setMajorId(String majorId) {
        this.majorId = majorId;
    }

    @Valid
    public List<ConnectorProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    public void setProperties(List<ConnectorProperty> properties) {
        this.properties = properties;
    }

    public void addProperty(ConnectorProperty property) {
        properties.add(property);
    }

    public Map<String, String> getPropertiesAsMap() {
        Map<String, String> map = new HashMap<String, String>();
        for (ConnectorProperty property : properties) {
            map.put(property.getKey(), property.getValue());
        }
        return map;
    }

    @AssertTrue(message = "connector major id is already being used")
    public boolean checkConstraintUniqueConnectorMajorId() {
        if (StringUtils.isBlank(majorId)) {
            return true;
        }

        IConnectorDAO connectorDAO = Registry.getConnectorDAO();
        if (isNewObject()) {
            return !connectorDAO.existsByNameAnotherTransaction(this);
        } else {
            Connector found = connectorDAO
                    .findUniqueByMajorIdAnotherTransaction(majorId);
            return found == null || found.getId().equals(getId());
        }

    }

}
