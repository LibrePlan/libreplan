/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;

/**
 * Model for UI operations related to {@link User}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public interface IUserModel {

    /**
     * Get all {@link User} elements
     *
     * @return
     */
    List<User> getUsers();

    /**
     * Makes some operations needed before edit a {@link User}.
     *
     * @param user
     *            The object to be edited
     */
    void initEdit(User user);

    /**
     * Makes some operations needed before create a new {@link User}.
     */
    void initCreate();

    /**
     * Gets the current {@link User}.
     *
     * @return A {@link User}
     */
    User getUser();

    /**
     * Stores the current {@link User}.
     *
     * @throws ValidationException
     *             If validation fails
     */
    void confirmSave() throws ValidationException;

    /**
     * Retrieves the list of UserRoles associated with the current User.
     *
     * @return List of {@link UserRole} objects.
     */
    List<UserRole> getRoles();

    /**
     * Removes a role from the UserRoles associated with the current User.
     *
     * @param role The {@link UserRole} object to be removed.
     */
    void removeRole(UserRole role);

    /**
     * Adds a role to the UserRoles associated with the current User.
     *
     * @param role The {@link UserRole} object to be added.
     */
    void addRole(UserRole role);

    /**
     * Retrieves the list of Profiles associated with the current user.
     * @return List of {@link Profile} objects.
     */
    List<Profile> getProfiles();

    /**
     * Removes a profile from the list of {@link Profile} objects associated
     * with the current User.
     *
     * @param role The {@link Profile} object to be removed.
     */
    void removeProfile(Profile profile);

    /**
     * Adds a profile to the list of {@link Profile} objects associated
     * with the current User.
     *
     * @param role The {@link Profile} object to be added.
     */
    void addProfile(Profile profile);

    /**
     * Sets the password attribute to the inner {@ link User} object.
     *
     * @param password String with the <b>unencrypted</b> password.
     */
    void setPassword(String password);

    String getClearNewPassword();

    void confirmRemove(User user) throws InstanceNotFoundException;

    boolean isLDAPBeingUsed();

    boolean isLDAPRolesBeingUsed();

    void unboundResource();

}
