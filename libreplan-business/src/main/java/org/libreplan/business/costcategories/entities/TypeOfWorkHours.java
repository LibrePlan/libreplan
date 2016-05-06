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

package org.libreplan.business.costcategories.entities;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import javax.validation.constraints.AssertTrue;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.libreplan.business.common.IHumanIdentifiable;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.PredefinedConnectorProperties;
import org.libreplan.business.common.entities.PredefinedConnectors;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.daos.ITypeOfWorkHoursDAO;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public class TypeOfWorkHours extends IntegrationEntity implements IHumanIdentifiable {

    private String name;

    private BigDecimal defaultPrice;

    boolean enabled = true;

    // Default constructor, needed by Hibernate
    protected TypeOfWorkHours() {

    }

    public static TypeOfWorkHours create() {
        return (TypeOfWorkHours) create(new TypeOfWorkHours());
    }

    public static TypeOfWorkHours create(String code, String name) {
        return (TypeOfWorkHours) create(new TypeOfWorkHours(code, name));
    }

    public static TypeOfWorkHours createUnvalidated(String code, String name,
            Boolean enabled, BigDecimal defaultPrice) {

        TypeOfWorkHours typeOfWorkHours = create(
                new TypeOfWorkHours(code, name), code);

        if (enabled != null) {
            typeOfWorkHours.enabled = enabled;
        }
        if (defaultPrice != null) {
            typeOfWorkHours.defaultPrice = defaultPrice;
        }
        return typeOfWorkHours;
    }

    public void updateUnvalidated(String name, Boolean enabled,
            BigDecimal defaultPrice) {
        if (!StringUtils.isBlank(name)) {
            this.name = name;
        }
        if (enabled != null) {
            this.enabled = enabled;
        }
        if (defaultPrice != null) {
            this.defaultPrice = defaultPrice;
        }
    }

    protected TypeOfWorkHours(String code, String name) {
        setCode(code);
        this.name = name;
    }

    @NotEmpty(message = "name not specified")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull(message = "default price not specified")
    public BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(BigDecimal price) {
        this.defaultPrice = price;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected ITypeOfWorkHoursDAO getIntegrationEntityDAO() {
        return Registry.getTypeOfWorkHoursDAO();
    }

    @AssertTrue(message = "the type of work hours name has to be unique. It is already used")
    public boolean isUniqueNameConstraint() {

        if (StringUtils.isBlank(name)) {
            return true;
        }

        try {
        /* Check the constraint. */
            TypeOfWorkHours type = Registry.getTypeOfWorkHoursDAO()
                    .findUniqueByNameInAnotherTransaction(name);
            if (isNewObject()) {
                return false;
            } else {
                return type.getId().equals(getId());
            }
        } catch (InstanceNotFoundException e) {
            return true;
        }
    }

    @AssertTrue(message = "type of work hours for personal timesheets cannot be disabled")
    public boolean isPersonalTimesheetsTypeOfWorkHoursNotDisabledConstraint() {
        if (!isNewObject() && !getEnabled()) {
            TypeOfWorkHours typeOfWorkHours = Registry.getConfigurationDAO()
                    .getConfiguration().getPersonalTimesheetsTypeOfWorkHours();
            if (typeOfWorkHours.getId().equals(getId())) {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        return name;
    }

    @Override
    public String getHumanId() {
        return name;
    }

    @AssertTrue(message = "type of work hours for JIRA connector cannot be disabled")
    public boolean isJiraConnectorTypeOfWorkHoursNotDisabledConstraint() {
        if (!isNewObject() && !getEnabled()) {
            Connector connector = Registry.getConnectorDAO().findUniqueByName(
                    PredefinedConnectors.JIRA.getName());
            if (connector != null) {
                if (this.name.equals(connector.getPropertiesAsMap().get(
                        PredefinedConnectorProperties.JIRA_HOURS_TYPE))) {
                    return false;
                }
            }
        }

        return true;
    }

}
