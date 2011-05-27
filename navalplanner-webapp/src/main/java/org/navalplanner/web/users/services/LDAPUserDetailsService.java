/*
 * This file is part of NavalPlan
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
package org.navalplanner.web.users.services;

import static org.navalplanner.web.I18nHelper._;

import java.util.Set;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.scenarios.bootstrap.PredefinedScenarios;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
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
    public UserDetails loadUserByUsername(String loginName)
            throws UsernameNotFoundException, DataAccessException {

        User user;

        try {
            user = userDAO.findByLoginName(loginName);
        } catch (InstanceNotFoundException e) {
            throw new UsernameNotFoundException(_("User with login name "
                    + "'{0}': not found", loginName));
        }

        Scenario scenario = user.getLastConnectedScenario();
        if (scenario == null) {
            scenario = PredefinedScenarios.MASTER.getScenario();
        }

        String password = user.getPassword();
        if (null == password)
            password = "foo";
        return new CustomUser(user.getLoginName(), password,
                !user.isDisabled(), true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                getGrantedAuthorities(user.getAllRoles()), scenario);
    }

    private GrantedAuthority[] getGrantedAuthorities(Set<UserRole> roles) {

        GrantedAuthority[] grantedAuthorities = new GrantedAuthority[roles
                .size()];
        int i = 0;

        for (UserRole r : roles) {
            grantedAuthorities[i++] = new GrantedAuthorityImpl(r.name());
        }

        return grantedAuthorities;
    }
}
