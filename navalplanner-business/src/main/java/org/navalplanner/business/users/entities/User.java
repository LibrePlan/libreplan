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

package org.navalplanner.business.users.entities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.users.daos.IUserDAO;

/**
 * Entity for modeling a user.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class User extends BaseEntity {

    @NotEmpty(message="login name not specified")
    private String loginName;

    @NotEmpty(message="password not specified")
    private String password;

    @NotEmpty(message="user roles not specified")
    private Set<UserRole> roles = new HashSet<UserRole>();

    private String email;

    /**
     * Necessary for Hibernate. Please, do not call it.
     */
    public User() {}

    private User(String loginName, String password, Set<UserRole> roles) {
        this.loginName = loginName;
        this.password = password;
        this.roles = roles;
    }

    public static User create(String loginName, String password,
        Set<UserRole> roles) {

        return create(new User(loginName, password, roles));

    }

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

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @AssertTrue(message="login name is already being used by another user")
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

}
