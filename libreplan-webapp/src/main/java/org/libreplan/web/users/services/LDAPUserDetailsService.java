/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 ComtecSF S.L.
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
package org.libreplan.web.users.services;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.scenarios.bootstrap.PredefinedScenarios;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * A Class which is used to implement the UserDetailsService interfaces
 *
 * At this time it takes values from authenticated user (LDAP or DB) and gets
 * from DB the user properties.
 *
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 *
 */
public class LDAPUserDetailsService implements UserDetailsService {

    @Autowired
    private IUserDAO userDAO;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException, DataAccessException {

        User user;

        try {
            user = userDAO.findByLoginName(loginName);
        } catch (InstanceNotFoundException e) {
            throw new UsernameNotFoundException(MessageFormat.format("User with username {0}: not found", loginName));
        }

        Scenario scenario = user.getLastConnectedScenario();
        if ( scenario == null ) {
            scenario = PredefinedScenarios.MASTER.getScenario();
        }

        String password = user.getPassword();
        if ( null == password )
            password = "foo";

        return new CustomUser(
                user.getLoginName(), password,
                !user.isDisabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                getGrantedAuthorities(user.getAllRoles()), scenario);
    }

    private List<GrantedAuthority> getGrantedAuthorities(Set<UserRole> roles) {
        List<GrantedAuthority> result = new ArrayList<>();
        for (UserRole r : roles) {
            result.add(new SimpleGrantedAuthority(r.name()));
        }

        return result;
    }
}
