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

package org.navalplanner.business.users.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserOrderAuthorization;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate DAO for the <code>User</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Repository
public class UserDAO extends GenericDAOHibernate<User, Long>
    implements IUserDAO {

    @Override
    public User findByLoginName(String loginName)
        throws InstanceNotFoundException {

        Criteria c = getSession().createCriteria(User.class);
        c.add(Restrictions.eq("loginName", loginName).ignoreCase());
        User user = (User) c.uniqueResult();

        if (user == null) {
            throw new InstanceNotFoundException(loginName,
                User.class.getName());
        } else {
            return user;
        }

    }

    @Override
    public User findByLoginNameNotDisabled(String loginName)
        throws InstanceNotFoundException {

        Criteria c = getSession().createCriteria(User.class);
        c.add(Restrictions.eq("loginName", loginName).ignoreCase());
        c.add(Restrictions.eq("disabled", false));
        User user = (User) c.uniqueResult();

        if (user == null) {
            throw new InstanceNotFoundException(loginName,
                User.class.getName());
        } else {
            return user;
        }

    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public User findByLoginNameAnotherTransaction(String loginName)
        throws InstanceNotFoundException {

        return findByLoginName(loginName);

    }

    @Override
    public boolean existsByLoginName(String loginName) {
        try {
            findByLoginName(loginName);
            return true;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByLoginNameAnotherTransaction(String loginName) {
        return existsByLoginName(loginName);
    }

    @Override
    public List<User> listNotDisabled() {
        Criteria c = getSession().createCriteria(User.class);
        c.add(Restrictions.eq("disabled", false));
        return c.list();
    }

    @Override
    public List<User> findByLastConnectedScenario(Scenario scenario) {
        Criteria c = getSession().createCriteria(User.class);
        c.add(Restrictions.eq("lastConnectedScenario", scenario));
        return c.list();
    }

    @Override
    public List<OrderAuthorization> getOrderAuthorizationsByUser(User user) {
        List orderAuthorizations = getSession()
                .createCriteria(UserOrderAuthorization.class)
                .add(Restrictions.eq("user", user)).list();
        return orderAuthorizations;
    }
}
