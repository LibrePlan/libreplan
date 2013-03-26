/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.business.orders.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderSyncInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link OrderSyncInfo}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderSyncInfoDAO extends GenericDAOHibernate<OrderSyncInfo, Long>
        implements IOrderSyncInfoDAO {

    @Override
    public OrderSyncInfo findLastSynchronizedInfoByOrderAndConnectorName(
            Order order, String connectorName) {
        List<OrderSyncInfo> orderSyncInfoList = findLastSynchronizedInfosByOrderAndConnectorName(
                order, connectorName);
        if (orderSyncInfoList == null || orderSyncInfoList.isEmpty()) {
            return null;
        }
        return orderSyncInfoList.get(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OrderSyncInfo> findLastSynchronizedInfosByOrderAndConnectorName(
            Order order, String connectorName) {
        Criteria criteria = getSession().createCriteria(OrderSyncInfo.class);
        criteria.add(Restrictions.eq("order", order));
        criteria.add(Restrictions.eq("connectorName", connectorName));
        criteria.addOrder(org.hibernate.criterion.Order.desc("lastSyncDate"));
        return criteria.list();
    }

    @Override
    public OrderSyncInfo findByKeyOrderAndConnectorName(String key,
            Order order, String connectorName) {
        Criteria criteria = getSession().createCriteria(OrderSyncInfo.class);
        criteria.add(Restrictions.eq("key", key));
        criteria.add(Restrictions.eq("order", order));
        criteria.add(Restrictions.eq("connectorName", connectorName));
        return (OrderSyncInfo) criteria.uniqueResult();
    }

    @Override
    public List<OrderSyncInfo> findByConnectorName(String connectorName) {
        Criteria criteria = getSession().createCriteria(OrderSyncInfo.class);
        criteria.add(Restrictions.eq("connectorName", connectorName));
        return criteria.list();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByKeyOrderAndConnectorNameAnotherTransaction(
            OrderSyncInfo orderSyncInfo) {
        return existsOtherOrderSyncInfoByKeyOrderAndConnectorName(orderSyncInfo);
    }

    /**
     * Returns true if other {@link OrderSyncInfo} which is the same
     * as the given <code>{@link OrderSyncInfo}</code> already exists
     *
     * @param orderSyncInfo
     *            the {@link OrderSyncInfo}
     */
    private boolean existsOtherOrderSyncInfoByKeyOrderAndConnectorName(
            OrderSyncInfo orderSyncInfo) {
        OrderSyncInfo found = findByKeyOrderAndConnectorName(
                orderSyncInfo.getKey(), orderSyncInfo.getOrder(),
                orderSyncInfo.getConnectorName());
        return found != null && found != orderSyncInfo;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public OrderSyncInfo findUniqueByKeyOrderAndConnectorNameAnotherTransaction(
            String key, Order order, String connectorName) {
        return findByKeyOrderAndConnectorName(key, order, connectorName);
    }

}
