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
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.users.daos.IUserDAO;

/**
 * Entity for modeling a user.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 *
 */
public class User extends BaseEntity {

    private String loginName = "";

    private String password = "";

    private Set<UserRole> roles = new HashSet<UserRole>();

    private Set<Profile> profiles = new HashSet<Profile>();

    private String email;

    private Boolean disabled = false;

    private Scenario lastConnectedScenario;

    // TODO if a user is a navalplan user or not (ldap)
    private Boolean navalplanUser = true;

    /**
     * Necessary for Hibernate. Please, do not call it.
     */
    public User() {
    }

    private User(String loginName, String password, Set<UserRole> roles) {
        this.loginName = loginName;
        this.password = password;
        this.roles = roles;
    }

    public static User create(String loginName, String password,
            Set<UserRole> roles) {

        return create(new User(loginName, password, roles));

    }

    public static User create() {
        return create(new User());
    }

    @NotEmpty(message = "login name not specified")
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public void addRole(UserRole role) {
        roles.add(role);
    }

    public void removeRole(UserRole role) {
        roles.remove(role);
    }

    /**
     * Retrieves UserRoles from related Profiles and returns them together with
     * the UserRoles related directly to the User entity
     *
     * @return A list of UserRole objects
     */
    public Set<UserRole> getAllRoles() {
        Set<UserRole> allRoles = new HashSet<UserRole>(roles);
        for (Profile profile : getProfiles()) {
            allRoles.addAll(profile.getRoles());
        }
        return allRoles;
    }

    /**
     * Checks if current user is in the requested role
     */
    public boolean isInRole(UserRole role) {
        if (roles.contains(role)) {
            return true;
        }
        for (Profile profile : profiles) {
            if (profile.getRoles().contains(role)) {
                return true;
            }
        }
        return false;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public Set<Profile> getProfiles() {
        return profiles;
    }

    public void addProfile(Profile profile) {
        profiles.add(profile);
    }

    public void removeProfile(Profile profile) {
        profiles.remove(profile);
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isAdministrator() {
        return isInRole(UserRole.ROLE_ADMINISTRATION);
    }

    @AssertTrue(message = "login name is already being used by another user")
    public boolean checkConstraintUniqueLoginName() {

        IUserDAO userDAO = Registry.getUserDAO();

        if (isNewObject()) {
            return !userDAO.existsByLoginNameAnotherTransaction(loginName);
        } else {
            try {
                User u = userDAO.findByLoginNameAnotherTransaction(loginName);
                return u.getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            }

        }

    }

    public void setLastConnectedScenario(Scenario lastConnectedScenario) {
        this.lastConnectedScenario = lastConnectedScenario;
    }

    public Scenario getLastConnectedScenario() {
        return lastConnectedScenario;
    }

    public Boolean isNavalplanUser() {
        return navalplanUser;
    }

    public void setNavalplanUser(Boolean navalplanUser) {
        this.navalplanUser = navalplanUser;
    }

}
