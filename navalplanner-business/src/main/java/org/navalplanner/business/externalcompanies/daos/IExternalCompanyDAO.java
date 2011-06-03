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

package org.navalplanner.business.externalcompanies.daos;

import java.util.List;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;

/**
 * Interface of the DAO for {@link ExternalCompany}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public interface IExternalCompanyDAO extends IGenericDAO<ExternalCompany, Long> {

    boolean existsByName(String name);

    boolean existsByNameInAnotherTransaction(String name);

    ExternalCompany findUniqueByName(String name) throws InstanceNotFoundException;

    ExternalCompany findUniqueByNameInAnotherTransaction(String name)
        throws InstanceNotFoundException;

    boolean existsByNif(String nif);

    boolean existsByNifInAnotherTransaction(String nif);

    ExternalCompany findUniqueByNif(String nif) throws InstanceNotFoundException;

    ExternalCompany findUniqueByNifInAnotherTransaction(String nif)
        throws InstanceNotFoundException;

    List<ExternalCompany> findSubcontractor();

    List<ExternalCompany> getAll();

    List<ExternalCompany> getExternalCompaniesAreClient();

    boolean isAlreadyInUse(ExternalCompany company);
}
