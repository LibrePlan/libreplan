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

package org.libreplan.business.logs.entities;

import org.libreplan.business.common.IHumanIdentifiable;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.orders.entities.Order;

/**
 * This class is the base class for all logs like issue-log, risk-log etc.
 * 
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
public abstract class ProjectLog extends IntegrationEntity implements
        IHumanIdentifiable {

    protected Order project;
    protected String description;

    public Order getOrder() {
        return project;
    }

    public void setOrder(Order order) {
        this.project = order;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
