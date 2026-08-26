/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import org.libreplan.business.common.Registry;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate DAO for the {@link OrderAuthorization} entity.
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Repository
public class OrderAuthorizationDAO extends GenericDAOHibernate<OrderAuthorization, Long>
    implements IOrderAuthorizationDAO {

    @Override
    public List<OrderAuthorization> listByOrder(Order order) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<OrderAuthorization> cq = cb.createQuery(OrderAuthorization.class);
        Root<OrderAuthorization> root = cq.from(OrderAuthorization.class);
        cq.where(cb.equal(root.get("order"), order));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public List<OrderAuthorization> listByUser(User user) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<OrderAuthorization> cq = cb.createQuery(OrderAuthorization.class);
        Root<OrderAuthorization> root = cq.from(OrderAuthorization.class);
        cq.where(cb.equal(root.get("user"), user));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public List<OrderAuthorization> listByProfile(Profile profile) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<OrderAuthorization> cq = cb.createQuery(OrderAuthorization.class);
        Root<OrderAuthorization> root = cq.from(OrderAuthorization.class);
        cq.where(cb.equal(root.get("profile"), profile));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public List<OrderAuthorization> listByUserAndItsProfiles(User user) {
        List<OrderAuthorization> list = new ArrayList<OrderAuthorization>();
        list.addAll(listByUser(user));
        Registry.getUserDAO().reattach(user);
        for(Profile profile : user.getProfiles()) {
            list.addAll(listByProfile(profile));
        }
        return list;
    }

    @Override
    public boolean userOrItsProfilesHaveAnyAuthorization(User user) {
        if (!listByUser(user).isEmpty()) {
            return true;
        }
        Registry.getUserDAO().reattach(user);
        for (Profile profile : user.getProfiles()) {
            if (!listByProfile(profile).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<OrderAuthorization> listByOrderAndUser(Order order, User user) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<OrderAuthorization> cq = cb.createQuery(OrderAuthorization.class);
        Root<OrderAuthorization> root = cq.from(OrderAuthorization.class);
        cq.where(cb.equal(root.get("order"), order), cb.equal(root.get("user"), user));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public List<OrderAuthorization> listByOrderAndProfile(Order order, Profile profile) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<OrderAuthorization> cq = cb.createQuery(OrderAuthorization.class);
        Root<OrderAuthorization> root = cq.from(OrderAuthorization.class);
        cq.where(cb.equal(root.get("order"), order), cb.equal(root.get("profile"), profile));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderAuthorization> listByOrderUserAndItsProfiles(Order order, User user) {
        List<OrderAuthorization> list = new ArrayList<OrderAuthorization>();
        list.addAll(listByOrderAndUser(order,user));
        Registry.getUserDAO().reattach(user);
        for(Profile profile : user.getProfiles()) {
            list.addAll(listByOrderAndProfile(order, profile));
        }
        return list;
    }
}
