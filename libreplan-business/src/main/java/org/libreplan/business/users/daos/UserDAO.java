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

package org.libreplan.business.users.daos;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserOrderAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate DAO for the <code>User</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
@Repository
public class UserDAO extends GenericDAOHibernate<User, Long> implements IUserDAO {

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    @Override
    @Transactional(readOnly = true)
    public User findByLoginName(String loginName) throws InstanceNotFoundException {

        User user = (User) getSession()
                .createCriteria(User.class)
                .add(Restrictions.eq("loginName", loginName).ignoreCase())
                .uniqueResult();

        if ( user == null ) {
            throw new InstanceNotFoundException(loginName, User.class.getName());
        } else {
            return user;
        }

    }

    @Override
    public User findByLoginNameNotDisabled(String loginName) throws InstanceNotFoundException {

        User user = (User) getSession()
                .createCriteria(User.class)
                .add(Restrictions.eq("loginName", loginName).ignoreCase())
                .add(Restrictions.eq("disabled", false))
                .uniqueResult();

        if (user == null) {
            throw new InstanceNotFoundException(loginName, User.class.getName());
        } else {
            return user;
        }

    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public User findByLoginNameAnotherTransaction(String loginName) throws InstanceNotFoundException {
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
        return getSession()
                .createCriteria(User.class)
                .add(Restrictions.eq("disabled", false))
                .list();
    }

    @Override
    public List<User> findByLastConnectedScenario(Scenario scenario) {
        return getSession()
                .createCriteria(User.class)
                .add(Restrictions.eq("lastConnectedScenario", scenario))
                .list();
    }

    private List<OrderAuthorization> getOrderAuthorizationsByUser(User user) {
        return getSession()
                .createCriteria(UserOrderAuthorization.class)
                .add(Restrictions.eq("user", user))
                .list();
    }

    @Override
    public List<User> getUnboundUsers(Worker worker) {
        List<User> result = new ArrayList<>();
        boolean condition;
        for (User user : getUsersOrderByLoginName()) {

            condition = (user.getWorker() == null) ||
                    (worker != null && !worker.isNewObject() && worker.getId().equals(user.getWorker().getId()));

            if ( condition ) {
                result.add(user);
            }
        }
        return result;
    }

    private List<User> getUsersOrderByLoginName() {
        return getSession()
                .createCriteria(User.class)
                .addOrder(Order.asc("loginName"))
                .list();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public User findOnAnotherTransaction(Long id) {
        try {
            return find(id);
        } catch (InstanceNotFoundException e) {
            return null;
        }
    }

    @Override
    public void remove(User user) throws InstanceNotFoundException {
        List<OrderAuthorization> orderAuthorizations = getOrderAuthorizationsByUser(user);
        if ( !orderAuthorizations.isEmpty() ) {
            for (OrderAuthorization orderAuthorization : orderAuthorizations) {
                orderAuthorizationDAO.remove(orderAuthorization.getId());
            }
        }
        super.remove(user.getId());
    }

    @Override
    public List<User> findAll() {
        return list(User.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Number getRowCount() {
        return (Number) getSession().createCriteria(User.class).setProjection(Projections.rowCount()).uniqueResult();
    }
}
