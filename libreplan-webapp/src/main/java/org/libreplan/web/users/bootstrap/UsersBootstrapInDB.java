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

package org.libreplan.web.users.bootstrap;

import java.util.HashSet;
import java.util.Set;

import org.libreplan.business.BootstrapOrder;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.users.bootstrap.PredefinedProfiles;
import org.libreplan.business.users.daos.IProfileDAO;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.users.services.IDBPasswordEncoderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bootstrapt to create the default {@link User Users}.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Transactional
@BootstrapOrder(1)
public class UsersBootstrapInDB implements IUsersBootstrapInDB {

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IProfileDAO profileDAO;

    private IDBPasswordEncoderService dbPasswordEncoderService;

    public void setDbPasswordEncoderService(
        IDBPasswordEncoderService dbPasswordEncoderService) {

        this.dbPasswordEncoderService = dbPasswordEncoderService;

    }

    @Override
    public void loadRequiredData() {

        if (userDAO.list(User.class).isEmpty()) {
            for (PredefinedUsers u : PredefinedUsers.values()) {
                User user = User.create(u.getLoginName(),
                        getEncodedPassword(u), u.getInitialRoles(),
                        getProfiles(u.getInitialProfiles()));
                user.setDisabled(u.isUserDisabled());
                user.setApplicationLanguage(u.getApplicationLanguage());

                userDAO.save(user);
            }
        }

    }

    private Set<Profile> getProfiles(Set<PredefinedProfiles> initialProfiles) {
        Set<Profile> profiles = new HashSet<Profile>();
        for (PredefinedProfiles each : initialProfiles) {
            try {
                profiles.add(profileDAO.findByProfileName(each.getName()));
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return profiles;
    }

    private String getEncodedPassword(PredefinedUsers u) {

        return dbPasswordEncoderService.encodePassword(u.getClearPassword(),
            u.getLoginName());

    }

}
