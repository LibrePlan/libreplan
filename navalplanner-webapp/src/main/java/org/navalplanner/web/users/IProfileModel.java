/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.users;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.users.entities.Profile;

/**
 * Model for UI operations related to {@link Profile}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public interface IProfileModel {

    /**
     * Makes some operations needed before edit a {@link Profile}.
     *
     * @param profile
     *            The object to be edited
     */
    void initEdit(Profile profile);

    /**
     * Makes some operations needed before create a new {@link Profile}.
     */
    void initCreate();

    /**
     * Get all {@link Profile} elements
     *
     * @return
     */
    List<Profile> getProfiles();

    /**
     * Gets the current {@link Profile}.
     *
     * @return A {@link Profile}
     */
    Profile getProfile();

    /**
     * Stores the current {@link Profile}.
     *
     * @throws ValidationException
     *             If validation fails
     */
    void confirmSave() throws ValidationException;
}
