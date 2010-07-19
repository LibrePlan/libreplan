/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.common.daos;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.entities.OrderSequence;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.i18n.I18nHelper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link OrderSequence}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderSequenceDAO extends GenericDAOHibernate<OrderSequence, Long>
        implements IOrderSequenceDAO {

    @Override
    public List<OrderSequence> getAll() {
        return list(OrderSequence.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OrderSequence> findOrderSquencesNotIn(
            List<OrderSequence> orderSequences) {
        List<Long> orderSequenceIds = new ArrayList<Long>();
        for (OrderSequence orderSequence : orderSequences) {
            orderSequenceIds.add(orderSequence.getId());
        }

        return getSession().createCriteria(OrderSequence.class).add(
                Restrictions.not(Restrictions.in("id", orderSequenceIds)))
                .list();
    }

    @Override
    public void remove(OrderSequence orderSequence)
            throws InstanceNotFoundException, IllegalArgumentException {
        if (orderSequence.getLastValue() > 0) {
            throw new IllegalArgumentException(
                    I18nHelper
                            ._("You can not remove this order sequence, it is already in use"));
        }

        remove(orderSequence.getId());
    }

    @Override
    public OrderSequence getActiveOrderSequence() {
        return (OrderSequence) getSession().createCriteria(OrderSequence.class)
                .add(Restrictions.eq("active", true)).uniqueResult();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String getNextOrderCode() {
        for (int i = 0; i < 5; i++) {
            try {
                OrderSequence orderSequence = getActiveOrderSequence();
                orderSequence.incrementLastValue();
                save(orderSequence);
                return orderSequence.getCode();
            } catch (HibernateOptimisticLockingFailureException e) {
                // Do nothing (optimistic approach 5 attempts)
            }
        }

        return null;
    }

}