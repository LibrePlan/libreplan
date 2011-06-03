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

package org.navalplanner.web.common.components.finders;

import java.util.List;

import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
*
* @author Jacobo Aragunde Perez <jaragunde@igalia.com>
*
* Implements a {@link IFinder} class for providing {@link User}
* elements
*
*/
@Repository
public class UserFinder extends Finder implements IFinder {

    @Autowired
    IUserDAO dao;

    @Override
    public String _toString(Object value) {
        User user = (User) value;
        return (user != null)? user.getLoginName() : "";
    }

    @Override
    public List<? extends BaseEntity> getAll() {
        return dao.listNotDisabled();
    }

}
