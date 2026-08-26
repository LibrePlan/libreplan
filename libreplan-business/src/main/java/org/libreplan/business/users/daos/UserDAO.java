/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

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

        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.where(cb.equal(cb.lower(root.get("loginName")), loginName.toLowerCase()));
        User user = getSession().createQuery(cq).uniqueResult();

        if ( user == null ) {
            throw new InstanceNotFoundException(loginName, User.class.getName());
        } else {
            return user;
        }

    }

    @Override
    public User findByLoginNameNotDisabled(String loginName) throws InstanceNotFoundException {

        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.where(
                cb.equal(cb.lower(root.get("loginName")), loginName.toLowerCase()),
                cb.equal(root.get("disabled"), false));
        User user = getSession().createQuery(cq).uniqueResult();

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
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.where(cb.equal(root.get("disabled"), false));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public List<User> findByLastConnectedScenario(Scenario scenario) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.where(cb.equal(root.get("lastConnectedScenario"), scenario));
        return getSession().createQuery(cq).getResultList();
    }

    private List<OrderAuthorization> getOrderAuthorizationsByUser(User user) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        // Query typed as the supertype OrderAuthorization (the method's declared return type),
        // selecting from its subtype UserOrderAuthorization - mirrors the original Criteria
        // query, which also implicitly returned UserOrderAuthorization rows as a
        // List<OrderAuthorization>.
        CriteriaQuery<OrderAuthorization> cq = cb.createQuery(OrderAuthorization.class);
        Root<UserOrderAuthorization> root = cq.from(UserOrderAuthorization.class);
        cq.where(cb.equal(root.get("user"), user));
        cq.select(root);
        return getSession().createQuery(cq).getResultList();
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
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.orderBy(cb.asc(root.get("loginName")));
        return getSession().createQuery(cq).getResultList();
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
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);
        cq.select(cb.count(root));
        return getSession().createQuery(cq).uniqueResult();
    }
}
