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

package org.libreplan.web.users.bootstrap;

import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.users.services.IDBPasswordEncoderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Transactional
public class UsersBootstrapInDB implements IUsersBootstrapInDB {

    @Autowired
    private IUserDAO userDAO;

    private IDBPasswordEncoderService dbPasswordEncoderService;

    public void setDbPasswordEncoderService(
        IDBPasswordEncoderService dbPasswordEncoderService) {

        this.dbPasswordEncoderService = dbPasswordEncoderService;

    }

    @Override
    public void loadRequiredData() {

        for (MandatoryUser u : MandatoryUser.values()) {
            createUserIfNotExists(u);
        }

    }

    private void createUserIfNotExists(MandatoryUser u) {

        if (!userDAO.existsByLoginName(u.getLoginName())) {

            User user = User.create(u.getLoginName(), getEncodedPassword(u),
                u.getInitialRoles());
            user.setApplicationLanguage(u.getApplicationLanguage());
            userDAO.save(user);

        }

    }

    private String getEncodedPassword(MandatoryUser u) {

        return dbPasswordEncoderService.encodePassword(u.getClearPassword(),
            u.getLoginName());

    }

}
