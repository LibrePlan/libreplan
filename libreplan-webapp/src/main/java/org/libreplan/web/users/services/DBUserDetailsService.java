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

package org.libreplan.web.users.services;

import static org.libreplan.web.I18nHelper._;

import java.util.Set;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.scenarios.bootstrap.PredefinedScenarios;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of Spring Security's <code>UserDetailsService</code>.
 * The implementation retrieves the user's details from the database by using
 * <code>IUserDAO</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public class DBUserDetailsService implements UserDetailsService {

    @Autowired
    private IUserDAO userDAO;

    @Override
    @Transactional(readOnly=true)
    public UserDetails loadUserByUsername(String loginName)
        throws UsernameNotFoundException, DataAccessException {

        User user;

        try {
            user = userDAO.findByLoginName(loginName);
        } catch (InstanceNotFoundException e) {
            throw new UsernameNotFoundException(_(
                    "User with username '{0}': not found", loginName));
        }

        Scenario scenario = user.getLastConnectedScenario();
        if (scenario == null) {
            scenario = PredefinedScenarios.MASTER.getScenario();
        }

        return new CustomUser(
            user.getLoginName(),
            user.getPassword(),
            !user.isDisabled(),
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            getGrantedAuthorities(user.getAllRoles()),
            scenario);
    }

    private GrantedAuthority[] getGrantedAuthorities(Set<UserRole> roles) {

        GrantedAuthority[] grantedAuthorities =
            new GrantedAuthority[roles.size()];
        int i = 0;

        for (UserRole r : roles) {
            grantedAuthorities[i++] = new GrantedAuthorityImpl(r.name());
        }

        return grantedAuthorities;

    }

}
