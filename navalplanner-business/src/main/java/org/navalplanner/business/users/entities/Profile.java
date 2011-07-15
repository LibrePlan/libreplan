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

package org.navalplanner.business.users.entities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.IHumanIdentifiable;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.users.daos.IProfileDAO;

/**
 * Entity for modeling a profile.
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public class Profile extends BaseEntity implements IHumanIdentifiable{

    private String profileName = "";

    private Set<UserRole> roles = new HashSet<UserRole>();

    /**
     * Necessary for Hibernate.
     */
    public Profile() {}

    private Profile(String profileName, Set<UserRole> roles) {
        this.profileName = profileName;
        this.setRoles(roles);
    }

    public static Profile create() {
        return create(new Profile());
    }

    public static Profile create(String loginName, Set<UserRole> roles) {
        return create(new Profile(loginName, roles));
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    @NotEmpty(message="profile name not specified")
    public String getProfileName() {
        return profileName;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void addRole(UserRole role) {
        roles.add(role);
    }

    public void removeRole(UserRole role) {
        roles.remove(role);
    }

    @AssertTrue(message="profile name is already being used by another profile")
    public boolean checkConstraintUniqueLoginName() {

        IProfileDAO dao = Registry.getProfileDAO();

        if (isNewObject()) {
            return !dao.existsByProfileNameAnotherTransaction(profileName);
        } else {
            try {
                Profile p = dao.findByProfileNameAnotherTransaction(profileName);
                return p.getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            }

        }

    }

    @Override
    public String getHumanId() {
        return profileName;
    }
}
