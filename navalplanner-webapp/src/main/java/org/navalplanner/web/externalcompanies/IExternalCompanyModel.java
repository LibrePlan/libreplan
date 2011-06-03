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

package org.navalplanner.web.externalcompanies;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.users.entities.User;

/**
 * Model for UI operations related to {@link ExternalCompany}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public interface IExternalCompanyModel {

    /**
     * Lists all the {@link ExternalCompany} objects available in the system.
     *
     * @return A list of {@link ExternalCompany} objects.
     */
    List<ExternalCompany> getCompanies();

    /**
     * Gets the current {@link ExternalCompany}.
     *
     * @return A {@link ExternalCompany}
     */
    ExternalCompany getCompany();

    /**
     * Makes some operations needed before create a new {@link ExternalCompany}.
     *
     */
    void initCreate();

    /**
     * Makes some operations needed before edit a {@link ExternalCompany}.
     *
     * @param company
     *            The object to be edited
     */
    void initEdit(ExternalCompany company);

    /**
     * Stores the current {@link ExternalCompany}.
     *
     * @throws ValidationException
     *             If validation fails
     */
    void confirmSave();

    /**
     * Changes the value of the attribute companyUser in the inner
     * {@link ExternalCompany} object.
     *
     * @param companyUser
     */
    void setCompanyUser(User companyUser);

    /**
     * Delete the selected {@link ExternalCompany} object.
     * @param company
     *            The object to be deleted
     * @return true if the {@link ExternalCompany} has been deleted correctly.
     */
    boolean deleteCompany(ExternalCompany company);

    /**
     * Check out if the company has been already used.
     * @param company
     * @return true if the company has been already used.
     */
    boolean isAlreadyInUse(ExternalCompany company);
}
