/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.common.daos;

import org.navalplanner.business.common.entities.Configuration;

/**
 * DAO interface for {@link ConfigurationDAO}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IConfigurationDAO extends IGenericDAO<Configuration, Long> {

    /**
     * @return The application configuration.
     */
    Configuration getConfiguration();

    /**
     * @return The application configuration open a read-only transaction.
     */
    Configuration getConfigurationWithReadOnlyTransaction();

    /**
     * It changes the value in the field changedDefaultAdminPassword of a
     * specified user.
     */
    void saveChangedDefaultPassword(String user, boolean change);
}