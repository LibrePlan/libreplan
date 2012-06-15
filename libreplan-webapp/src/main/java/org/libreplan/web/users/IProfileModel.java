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

package org.libreplan.web.users;

import java.util.List;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.UserRole;

/**
 * Model for UI operations related to {@link Profile}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface IProfileModel {

    /**
     * Adds a role to the current {@link Profile}
     *
     * @param role {@link UserRole} element to be added
     */
    void addRole(UserRole role);

    void checkHasUsers(Profile profile) throws ValidationException;

    /**
     *  Stores the removal of the passed {@link Profile}
     * @param profile {@link Profile} element to be removed.
     * @throws InstanceNotFoundException
     */
    void confirmRemove(Profile profile) throws InstanceNotFoundException;

    /**
     * Stores the current {@link Profile}.
     *
     * @throws ValidationException
     *             If validation fails
     */
    void confirmSave() throws ValidationException;

    /**
     * Gets the current {@link Profile}.
     *
     * @return A {@link Profile}
     */
    Profile getProfile();

    /**
     * Get all {@link Profile} elements
     *
     * @return
     */
    List<Profile> getProfiles();

    /**
     * Returns a list of the {@link UserRole} objects related with the current profile.
     * @return a list of {@link UserRole} objects.
     */
    List<UserRole> getRoles();

    /**
     * Makes some operations needed before create a new {@link Profile}.
     */
    void initCreate();

    /**
     * Makes some operations needed before edit a {@link Profile}.
     *
     * @param profile
     *            The object to be edited
     */
    void initEdit(Profile profile);

    /**
     * Removes a role from the current {@link Profile}
     *
     * @param role {@link UserRole} element to be removed
     */
    void removeRole(UserRole role);

    /**
     * Checks if the role is included in the current profile.
     *
     * @param role {@link UserRole} element to be checked
     * @return true if the role belongs to the profile,
     *  false otherwise.
     */
    boolean roleBelongs(UserRole role);
}
