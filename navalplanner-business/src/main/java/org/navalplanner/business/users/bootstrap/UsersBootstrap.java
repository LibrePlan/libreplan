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

package org.navalplanner.business.users.bootstrap;

import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Service
@Transactional
public class UsersBootstrap implements IUsersBootstrap {

    @Autowired
    private IUserDAO userDAO;

    @Override
    public void loadRequiredData() {

        for (MandatoryUser u : MandatoryUser.values()) {
            createUserIfNotExists(u);
        }

    }

    private void createUserIfNotExists(MandatoryUser u) {

        if (!userDAO.existsByLoginName(u.name())) {

            userDAO.save(User.create(u.name(), u.name(), u.getInitialRoles()));

        }

    }

}
